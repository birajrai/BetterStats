package io.github.birajrai.betterstats.discord;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;

import io.github.birajrai.betterstats.mongoDB.DataBaseManager;
import io.github.birajrai.betterstats.server.ServerManager;
import io.github.birajrai.betterstats.stats.Stats;
import io.github.birajrai.betterstats.utils.DiscordUtil;
import org.bukkit.Bukkit;
import io.github.birajrai.betterstats.utils.Util;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import io.github.birajrai.betterstats.commands.DiscordLinkCommand;

import net.dv8tion.jda.api.entities.Guild;

import java.util.logging.Logger;

/**
 * @author Biraj Rai
 * 2025 - 2028
 */
public class LinkManager {
	
	private static final Logger log = Logger.getLogger(LinkManager.class.getName());
	
	private DataBaseManager mongoDB;
	private ServerManager serverMan;
	private DiscordLinkCommand discordLinkCommand;
	
	private HashMap<Integer, UUID> playerLinkCodes;
	
	public LinkManager(DataBaseManager mongoDB, ServerManager serverMan, DiscordLinkCommand discordLinkCommand) {
		this.mongoDB = mongoDB;
		this.serverMan = serverMan;
		this.discordLinkCommand = discordLinkCommand;
		this.playerLinkCodes = new HashMap<>();
	}
	
	private final int RAND_RATIO = 10000;
	private final String LINK = "link";
	
	public int generateNewCode(UUID playerId) {
		
		Random rand = new Random();
		int code = rand.nextInt(RAND_RATIO);
		
		Iterator<Entry<Integer, UUID>> it = playerLinkCodes.entrySet().iterator();
		while(it.hasNext()) {
			Entry<Integer,UUID> entry = it.next();
			if(entry.getValue().equals(playerId)) {
				playerLinkCodes.remove(entry.getKey());
				break;
			}
		}
		
		while(playerLinkCodes.containsKey(code))
			code = rand.nextInt(RAND_RATIO);
		
		playerLinkCodes.put(code, playerId);
		
		return code;
	}
	
	public UUID getPlayer(int code) {
		return playerLinkCodes.get(code);
	}
	
	public boolean hasGenCode(int code) {
		return playerLinkCodes.containsKey(code);
	}
	
	public String link(String rawCode, String userId) {
		
		Iterator<Integer> it = playerLinkCodes.keySet().iterator();
		
		int code;
		
		while(it.hasNext()) {
			
			code = it.next();
			
			if(rawCode.contains(String.valueOf(code))) {
				String playerName = serverMan.getPlayerFromHashMap(getPlayer(code)).getName();
				linkProcess(code, userId);
				return String.format("You were linked with player %s!", playerName);
			}
		}
			
		return "The code inserted dont match... try /link on minecraft to link your account";
	}
	
	public void unlink(UUID playerId) {
		
		String userId = mongoDB.getDiscordUserByPlayer(playerId).getString("userId");
		
		mongoDB.updateOneServer(Filters.eq(Stats.PLAYERID.getQuery(), playerId), Updates.unset(LINK));
		mongoDB.updateOneDiscord(Filters.eq("userId", userId), Updates.unset(LINK));
		
		Guild guild = DiscordUtil.getJda().getGuildById(DiscordUtil.getGuildId());
    	
    	guild.removeRoleFromMember(userId, guild.getRoleById(DiscordUtil.getRoleLinkedId())).complete();

	}
	
	private void linkProcess(int code, String userId) {

		UUID playerId = getPlayer(code);

		mongoDB.updateOneServer(Filters.eq(Stats.PLAYERID.getQuery(), playerId), Updates.set(LINK, userId));
		String userName = DiscordUtil.getUserById(userId).getName();
		log.info("Discord user name retrieved: " + userName);
		mongoDB.updateOneDiscord(Filters.eq("userId", userId), Updates.combine(Updates.set(LINK, playerId), Updates.set("userName", userName)));

		playerLinkCodes.remove(code);

		discordLinkCommand.resetLinkTrys(playerId);

		Guild guild = DiscordUtil.getJda().getGuildById(DiscordUtil.getGuildId());

    	guild.addRoleToMember(userId, guild.getRoleById(DiscordUtil.getRoleLinkedId())).complete();
		
		// Send in-game message to the player
		Bukkit.getPlayer(playerId).sendMessage(Util.chat("&b[MineStats]&7 - Your Discord account has been successfully linked!"));

	}
	
	

}
