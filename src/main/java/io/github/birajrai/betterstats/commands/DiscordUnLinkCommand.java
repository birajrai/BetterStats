package io.github.birajrai.betterstats.commands;

import java.util.UUID;

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

/**
 * @author Biraj Rai
 * 2025 - 2028
 */
public class DiscordUnLinkCommand implements CommandExecutor {

	private DataBaseManager mongoDB;
	private ServerManager serverMan;
	private LinkManager linkMan;
	
	public DiscordUnLinkCommand(DataBaseManager mongoDB, ServerManager serverMan, LinkManager linkMan) {
		this.mongoDB = mongoDB;
		this.serverMan = serverMan;
		this.linkMan = linkMan;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		String name = sender.getName();
		
		if(args.length==0) {
			if(!(sender instanceof Player)) {
				sender.sendMessage(Util.chat("&b[MineStats]&7 - You need to specify a player."));
				return false;
			}
		} else name = args[0];
		
		Document playerDoc = mongoDB.getPlayerByName(name);
		
		if(playerDoc==null) {
			sender.sendMessage(Util.chat("&b[MineStats]&7 - This player doesn't exist on DataBase."));
			return false;
		}
		
		ServerPlayer pp = serverMan.getPlayerFromHashMap((UUID) playerDoc.get(Stats.PLAYERID.getQuery()));
				
		if(mongoDB.getDiscordUserByPlayer(pp.getPlayerID())==null) {
			sender.sendMessage(Util.chat("&b[MineStats]&7 - &d&l"+name+"&7 is not linked yet. Try /link first."));
			return false;
		}
		
		linkMan.unlink(pp.getPlayerID());
		sender.sendMessage(Util.chat("&b[MineStats]&7 - &d&l"+name+"&7 is now unlinked!"));
		
		return true;
	}

}
