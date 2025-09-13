package io.github.birajrai.betterstats.commands;

import org.bson.Document;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import io.github.birajrai.betterstats.discord.LinkManager;
import io.github.birajrai.betterstats.mongoDB.DataBaseManager;
import io.github.birajrai.betterstats.player.ServerPlayer;
import io.github.birajrai.betterstats.server.ServerManager;
import io.github.birajrai.betterstats.stats.Stats;
import io.github.birajrai.betterstats.utils.Util;

import java.util.UUID;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author Biraj Rai
 * 2025 - 2028
 */
public class DiscordLinkCommand implements CommandExecutor {

	private DataBaseManager mongoDB;
	private ServerManager serverMan;
	private LinkManager linkMan;
	private final int MAX_NUMBER_OF_TRYS;
	
	public DiscordLinkCommand(DataBaseManager mongoDB, ServerManager serverMan, LinkManager linkMan) {
		this.mongoDB = mongoDB;
		this.serverMan = serverMan;
		this.linkMan = linkMan;
		
		MAX_NUMBER_OF_TRYS = mongoDB.getConfig().getInt("numberOfTrys");

	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
				
		Document playerDoc = mongoDB.getPlayerByName(sender.getName());
				
		if(playerDoc==null) {	
			Collection<? extends Player> players = sender.getServer().getOnlinePlayers();

                        Iterator<? extends Player> iterator = players.iterator();
			
                        while(iterator.hasNext()) {
                            Player player = iterator.next();
                            if(sender.getName().equals(player.getName())) {
                                playerDoc = mongoDB.getPlayer(player.getUniqueId());
                                break;
                            }
                        }
			
			if(playerDoc==null) {
				sender.sendMessage(Util.chat("&b[MineStats]&7 - You don't exist on DataBase."));
				return false;
			}
		}
		
		ServerPlayer pp = serverMan.getPlayerFromHashMap((UUID) playerDoc.get(Stats.PLAYERID.getQuery()));
		
		Document discUser = mongoDB.getDiscordUserByPlayer(pp.getPlayerID());
		
		if(discUser!=null) {
			sender.sendMessage(Util.chat("&b[MineStats]&7 - You are already linked with Discord user &d&l"+discUser.getString("userName")));
			return false;
		}
		
		int numberOfTrys = pp.getNumberOfLinkTrys();
		
		if(numberOfTrys == MAX_NUMBER_OF_TRYS) {
			sender.sendMessage(Util.chat("&b[MineStats]&7 - You exceeded the number of trys to link with Discord Account. Try again tomorrow."));
			return false;
		}
				
		pp.setLinkTrys(numberOfTrys+1);
				
		sender.sendMessage(Util.chat("&7Send &d&l"+linkMan.generateNewCode(pp.getPlayerID())+" &7to the Link discord Bot"));
		
		return true;
	}
	
}
