package io.github.birajrai.betterstats.commands;

import java.util.UUID;
import java.util.Collection;
import java.util.Iterator;

import org.bson.Document;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import io.github.birajrai.betterstats.stats.Stats;
import io.github.birajrai.betterstats.medals.Medal;
import io.github.birajrai.betterstats.mongoDB.DataBaseManager;
import io.github.birajrai.betterstats.player.ServerPlayer;
import io.github.birajrai.betterstats.server.ServerManager;
import io.github.birajrai.betterstats.utils.Util;

/**
 * Print all medals of the specified player
 * @author Afonso Batista
 * 2021 - 2023
 */
public class PlayerMedalsCommand implements CommandExecutor {

	private DataBaseManager mongoDB;
	private ServerManager serverMan;

	public PlayerMedalsCommand(DataBaseManager mongoDB, ServerManager serverMan) {
		this.mongoDB = mongoDB;
		this.serverMan = serverMan;
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
		
		ServerPlayer pp = serverMan.getPlayerFromHashMap((UUID) playerDoc.get(Stats.PLAYERID.getQuery()));       
		
		Medal[] medals;
		
		if(pp==null) medals = mongoDB.loadMedals(playerDoc.getList(Stats.MEDALS.getQuery(), Document.class));
		else medals = pp.getMedals();
		
		sender.sendMessage(Util.chat("&b[MineStats]&7 - &c<player>&7 Medals:").replace("<player>", name));
		for(Medal medal: medals) {
			if(medal!=null)
				sender.sendMessage(Util.chat("    &a<medal> &6<level>")
						.replace("<medal>", medal.getMedal().toString())
						.replace("<level>", medal.getMedalLevel().toString()));
		}
			
		return true;
	}

}
