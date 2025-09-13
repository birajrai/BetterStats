package io.github.birajrai.betterstats.commands;

import java.text.ParseException;
import java.util.UUID;
import java.util.Collection;
import java.util.Iterator;

import org.bson.Document;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import io.github.birajrai.betterstats.mongoDB.DataBaseManager;
import io.github.birajrai.betterstats.player.ServerPlayer;
import io.github.birajrai.betterstats.server.ServerManager;
import io.github.birajrai.betterstats.utils.Util;
import io.github.birajrai.betterstats.stats.Stats;
import java.util.logging.Logger;

/**
 * Prints all current specified player stats
 * @author Afonso Batista
 * 2021 - 2023
 */
public class PlayerStatsCommand implements CommandExecutor {

	private DataBaseManager mongoDB;
	private ServerManager serverMan;
	private static final Logger logger = Logger.getLogger(PlayerStatsCommand.class.getName());

	public PlayerStatsCommand( DataBaseManager mongoDB, ServerManager serverMan) {
		this.serverMan = serverMan;
		this.mongoDB = mongoDB;
	}
	

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		Document playerDoc;
		String name = sender.getName();
		
		if(args.length==0) {
			if(!(sender instanceof Player)) {
				sender.sendMessage(Util.chat("&b[MineStats]&7 - You need to specify a player."));
				return false;
			}
		} else name = args[0];
		
		playerDoc = mongoDB.getPlayerByName(name);
		
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
		
		UUID playerId = (UUID) playerDoc.get(Stats.PLAYERID.getQuery());
		
		ServerPlayer pp = serverMan.getPlayerFromHashMap(playerId);       
		if(pp==null) {
			pp = new ServerPlayer(playerId, mongoDB);
			try {
				mongoDB.downloadFromDataBase(pp, playerDoc);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		
		sender.sendMessage(Util.chat("&b[MineStats]&7 - &c<player>&7 Stats:").replace("<player>", name));
		for(Stats stat: Stats.values()) {
			if(stat.toPrint())
				sender.sendMessage(Util.chat("    &e<stat>&7: &b<variable>").replace("<stat>", stat.getText())
															  	  .replace("<variable>", String.valueOf(Util.getStatVariable(pp, stat))));
		}
						
		Object playerIdObj = playerDoc.get(Stats.PLAYERID.getQuery());
        logger.info("Player ID object type: " + playerIdObj.getClass().getName());
        UUID playerId = (UUID) playerIdObj;
		
		Document discUser = mongoDB.getDiscordUserByPlayer(playerId);
		
		if(discUser!=null) sender.sendMessage(Util.chat("    &a&lLink&7: &b&l<variable>").replace("<variable>", discUser.getString("userName")));
		else sender.sendMessage(Util.chat("    &c&lLink&7: &b&l???"));
		
		return true;
	}
}
