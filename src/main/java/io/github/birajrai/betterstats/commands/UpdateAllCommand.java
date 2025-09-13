package io.github.birajrai.betterstats.commands;

import java.text.ParseException;
import java.util.UUID;

import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import io.github.birajrai.betterstats.stats.Stats;
import io.github.birajrai.betterstats.Main;
import io.github.birajrai.betterstats.medals.Medals;
import io.github.birajrai.betterstats.mongoDB.DataBaseManager;
import io.github.birajrai.betterstats.player.ServerPlayer;
import io.github.birajrai.betterstats.server.ServerManager;
import io.github.birajrai.betterstats.utils.Util;

import com.mongodb.client.MongoCursor;

/**
 * Update all player medals (make all players get medals if they can)
 * @author Afonso Batista
 * 2021 - 2023
 */
public class UpdateAllCommand implements CommandExecutor{
	private DataBaseManager mongoDB;
	private ServerManager serverMan;

	public UpdateAllCommand(DataBaseManager mongoDB, ServerManager serverMan) {
		this.serverMan = serverMan;
		this.mongoDB = mongoDB;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if(sender instanceof Player) {
			sender.sendMessage(Util.chat("&b[MineStats]&7 - Only the console can do this command...")); 
			return false;
		}
		
		
		MongoCursor<Document> it = mongoDB.getCollectionIterator();
		ServerPlayer sp; Document doc;
		while(it.hasNext()) {
			doc = it.next();
			
			sp = serverMan.getPlayerFromHashMap((UUID) doc.get(Stats.PLAYERID.getQuery())); 
			
			if(sp==null) {
				try {
					sp = new ServerPlayer((UUID) doc.get(Stats.PLAYERID.getQuery()), mongoDB);
					mongoDB.downloadFromDataBase(sp, doc);												//IF A PLAYER IS NOT ONLINE AT THE MOMENT
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		
			//Check for medals
			Player player = Main.currentServer.getPlayer(doc.getString(Stats.NAME.getQuery()));
			
			for(Medals medal: Medals.values()) {
				long variable = Util.getMedalVariable(sp, medal);
				if(variable!=0)
					sp.medalCheck(medal, variable, player);
			}
		}
		
		Bukkit.broadcastMessage(
				Util.chat("&b[MineStats]&7 - SUCCESS!!!!"));
	
		
		return true;
	}
}
