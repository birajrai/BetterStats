package io.github.birajrai.betterstats.mongoDB;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.HashMap;
import java.util.logging.Level;

import java.util.logging.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import io.github.birajrai.betterstats.medals.MLevel;
import io.github.birajrai.betterstats.medals.Medal;
import io.github.birajrai.betterstats.medals.Medals;
import io.github.birajrai.betterstats.player.ServerPlayer;
import io.github.birajrai.betterstats.server.ServerManager;
import io.github.birajrai.betterstats.stats.Block;
import io.github.birajrai.betterstats.stats.BlockStats;
import io.github.birajrai.betterstats.stats.Mob;
import io.github.birajrai.betterstats.stats.MobStats;
import io.github.birajrai.betterstats.stats.Stats;
import io.github.birajrai.betterstats.utils.Util;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

/**
 * @author Afonso Batista
 * 2021 - 2022
 */
public class DataBaseManager {
	
	private Logger log;
	private DataBase mongoDB;
	private ServerManager serverManager;
		
	public DataBaseManager(DataBase mongoDB, Logger log, ServerManager serverManager) {
		this.log = log;
		this.mongoDB = mongoDB;
		this.serverManager = serverManager;
	}
	
	public Document newPlayer(Player player) {
				
		Document playerDoc = new Document(Stats.PLAYERID.getQuery(), player.getUniqueId())
 				.append(Stats.NAME.getQuery(), player.getName())
				.append(Stats.ONLINE.getQuery(), player.isOnline());
				
		for(Stats stat: Stats.values()) {
			if(stat.getFirstValue()!=null)
				playerDoc.append(stat.getQuery(), stat.getFirstValue());
		}
							
		mongoDB.newDoc(playerDoc);
		
		Bukkit.broadcastMessage(
				Util.chat("&b[MineStats]&7 - Heyyy &a<player>&7! Welcome to Minecraft Nostalgia :D. Try ./stats to see your stats and ./pm to see your medals.".replace("<player>", player.getName())));
		
		return playerDoc;
	}
	
	public ServerPlayer getPlayerStats(Player player) {
		
		if(!serverManager.playerAlreadyInServer(player.getUniqueId())) {
			
			ServerPlayer pp = new ServerPlayer(player.getUniqueId(), this);
			
			Document playerDoc = mongoDB.getPlayer(player.getUniqueId());
			
			if(playerDoc==null) {
				
				playerDoc = getPlayerByName(player.getName());
				
				if(playerDoc!=null) {
					serverManager.deleteFromHashMap((UUID) playerDoc.get(Stats.PLAYERID.getQuery()));
					
					updateStat(Filters.eq(Stats.PLAYERID.getQuery(), playerDoc.get(Stats.PLAYERID.getQuery())),
							Updates.set(Stats.PLAYERID.getQuery(), player.getUniqueId()));
					
				}
					
				playerDoc = newPlayer(player);
			}
			
			try {
				downloadFromDataBase(pp, playerDoc);
			} catch (ParseException e) {
				log.log(Level.SEVERE, "[MineStats] - An error has occurred:", e);
			}
			
			return pp;
			
		}
		
		return serverManager.getPlayerStats(player.getUniqueId());
	}
	
	/**
	 * Downloads all stats from <playerDoc> in database to <sp>
	 * 
	 * @param sp - Local Player
	 * @param playerDoc - Document with all player stats 
	 */
	public void downloadFromDataBase(ServerPlayer sp, Document playerDoc) throws ParseException {
		synchronized (serverManager) {
			try {
				String[] time = playerDoc.getString(Stats.TIMEPLAYED.getQuery()).split(" ");
				int min = 0;
				if(time.length>2) min = Integer.parseInt(time[2]);
				
				sp.setName((String) playerDoc.getString(Stats.NAME.getQuery()));
								
				sp.setBlockStats(new BlockStats(playerDoc.getLong(Stats.BLOCKSDEST.getQuery()), 
						playerDoc.getLong(Stats.BLOCKSPLA.getQuery()), 
						playerDoc.getLong(Stats.REDSTONEUSED.getQuery()),
						playerDoc.getLong(Stats.BLOCKSMINED.getQuery()),
						loadBlockStats(playerDoc.getList(Stats.BLOCKS.getQuery(), Document.class))));
				
				sp.setMobStats(new MobStats(playerDoc.getLong(Stats.KILLS.getQuery()),
						playerDoc.getLong(Stats.MOBKILLS.getQuery()), playerDoc.getLong(Stats.ENDERDRAGONKILLS.getQuery()),
						playerDoc.getLong(Stats.WITHERKILLS.getQuery()), playerDoc.getLong(Stats.FISHCAUGHT.getQuery()),
						loadMobStats(playerDoc.getList(Stats.MOBSKILLED.getQuery(), Document.class))));
								
				sp.setNumberOfVersions(playerDoc.getList(Stats.VERSIONS.getQuery(), String.class).size());
				sp.setMetersTraveled(playerDoc.getLong(Stats.TRAVELLED.getQuery()));
				sp.setTimePlayed(Long.parseLong(time[0])*3600+min*60);
				sp.setDeaths(playerDoc.getLong(Stats.DEATHS.getQuery()));
				sp.setTimesLogin(playerDoc.getLong(Stats.TIMESLOGIN.getQuery()));
				sp.setSessionMarkTime(null);
				
				sp.setMedals(loadMedals(playerDoc.getList(Stats.MEDALS.getQuery(), Document.class)));
				
				sp.setPlayerSince(new SimpleDateFormat("dd/MM/yyyy").parse(playerDoc.getString(Stats.PLAYERSINCE.getQuery())));
				sp.setLastLogin(new SimpleDateFormat("dd/MM/yyyy h:mm a").parse(playerDoc.getString(Stats.LASTLOGIN.getQuery())));
								
				serverManager.newPlayerOnServer(sp);
			} catch(NullPointerException e) {
				for(Stats stat: Stats.values()) {
					if(!playerDoc.containsKey(stat.getQuery()))
						playerDoc.append(stat.getQuery(), stat.getFirstValue());
				}
				mongoDB.getServerCollection().replaceOne(Filters.eq(Stats.PLAYERID.getQuery(), sp.getPlayerID()), playerDoc);
				downloadFromDataBase(sp, playerDoc);
				e.printStackTrace();
			} catch(ParseException e) {
				
				String lastLogin = playerDoc.getString(Stats.LASTLOGIN.getQuery()).concat(" 12:00 AM"); 	
				
				mongoDB.getServerCollection().updateOne(Filters.eq(Stats.PLAYERID.getQuery(), sp.getPlayerID()), 
						Updates.set(Stats.LASTLOGIN.getQuery(), lastLogin));
				
				sp.setLastLogin(new SimpleDateFormat("dd/MM/yyyy h:mm a").parse(lastLogin));
				
			}
		}
	}
	
	/**
	 * Uploads all <sp> stats to the database, updating it.
	 * 
	 * @param sp - Local Player
	 */
	public synchronized void uploadToDataBase(ServerPlayer sp) {
		MongoCollection<Document> collection = mongoDB.getServerCollection();
		
		for(Stats stat: Stats.values()) {
			if(stat.toUpload()) {
				collection.updateOne(Filters.eq(Stats.PLAYERID.getQuery(), sp.getPlayerID()), 
						Updates.set(stat.getQuery(), Util.getStatVariable(sp, stat)));
			}
		}
	}
	
	public boolean checkVersions(String version, Document playerDoc) {
		if(playerDoc.getList(Stats.VERSIONS.getQuery(), String.class).contains(version)) return true;
		else {
			updateStat(Filters.eq(Stats.PLAYERID.getQuery(), playerDoc.get(Stats.PLAYERID.getQuery())),
					Updates.addToSet(Stats.VERSIONS.getQuery(), version));
			return false;
		}
	}
	
	public void levelUpMedal(Player player, Medal medal) {
		Document playerDoc = mongoDB.getPlayer(player.getUniqueId());
		Object[] list = playerDoc.getList(Stats.MEDALS.getQuery(), Document.class).toArray();
		List<Document> finalList = new ArrayList<Document>(list.length);
		
		for(int i=0; i<list.length; i++) {
			Document document = (Document) list[i];
			finalList.add(i, document);
			if(document.getString("medalName").equals(medal.getMedal().toString())) {
				finalList.remove(i); finalList.add(i, medal.createMedalDoc()); 
			}
		}
		
		mongoDB.getServerCollection().updateOne(Filters.eq(Stats.PLAYERID.getQuery(), player.getUniqueId()), Updates.set(Stats.MEDALS.getQuery(), finalList));
	}
	
	/**
	 * Loads all player <medals> from data base and convert them to an array of Medals 
	 * 
	 * @param medals - All player medals on data base.
	 * @return the converted list of medals to save locally.
	 */
	public Medal[] loadMedals(List<Document> medals) {
		
		Medal[] newList = new Medal[Medals.values().length];
		
		Medals medal; MLevel level;
		for(Document doc: medals) {
			
			medal = Medals.valueOf(doc.getString("medalName"));
			level = MLevel.valueOf(doc.getString("medalLevel"));
			
			newList[medal.getIndex()] = new Medal(medal, level) ;
		}
		
		return newList;
	}
	
	public boolean alreadyHadMedal(Medals medal, Document doc) {
		List<Document> medalDoc = doc.getList(Stats.MEDALS.getQuery(), Document.class);
		for(Document medals: medalDoc)
			if(medals.getString("medalName").equals(medal.toString())) return true;
		return false;
		
	}
	
	public void newMedalOnDataBase(Medal newMedal, Player player) {	
		Document doc = newMedal.createMedalDoc();								//NEED TO TEST IF PLAYER IDs CHANGE WHEN NAME CHANGED
		mongoDB.getServerCollection().updateOne(Filters.eq(Stats.PLAYERID.getQuery(), player.getUniqueId()), Updates.addToSet(Stats.MEDALS.getQuery(), doc));
	}
	
	public void newMedalOnDataBase(Document medalDoc, ServerPlayer sp) {	
		mongoDB.getServerCollection().updateOne(Filters.eq(Stats.PLAYERID.getQuery(), sp.getPlayerID()), Updates.addToSet(Stats.MEDALS.getQuery(), medalDoc));
	}
	
	public HashMap<String, Mob> loadMobStats(List<Document> mobStats) {
		HashMap<String, Mob> mapMobStats = new HashMap<>();
				
		int mId;
		String mName;
		long mNumKilled;
		
		for(Document doc : mobStats) {
			mId = doc.getInteger("mId");
			mName = doc.getString("mName");
			mNumKilled = doc.getLong("mNumKilled");
			Mob newMob = new Mob(mId, mName, mNumKilled);
			
			mapMobStats.put(mName, newMob);	
		}
		
		return mapMobStats;
	}
	
	public HashMap<String, Block> loadBlockStats(List<Document> blockStats) {
		HashMap<String, Block> mapBlockStats = new HashMap<>();
		
		String bName;
		long bNumDestroyed,
			 bNumPlaced;
		
		for(Document doc : blockStats) {
			bName = doc.getString("bName");
			bNumDestroyed = doc.getLong("bNumDestroyed");
			bNumPlaced = doc.getLong("bNumPlaced");
			Block newBlock = new Block(bName, bNumDestroyed, bNumPlaced);
			
			mapBlockStats.put(bName, newBlock);	
		}
		
		return mapBlockStats;
	}
	
	public void updateStat(Bson filter, Bson update) {
		mongoDB.getServerCollection().updateOne(filter, update);
	}
	
	public void updateMultStats(Bson filter, Bson update) {
		mongoDB.getServerCollection().updateMany(filter, update);
	}
	
	public void deleteDoc(Bson filter) {
		mongoDB.getServerCollection().deleteOne(filter);
	}
	
	public Document getPlayerByName(String name) {
		return mongoDB.getPlayerByName(name);
	}
	
	public MongoCursor<Document> getAllPlayersByName(String playerName) {
		return mongoDB.getAllPlayersByName(playerName);
	}
	
	public Document getPlayer(UUID playerId) {
		return mongoDB.getPlayer(playerId);
	}
	
	public Document getDiscordUser(String userId) {
		return mongoDB.getDiscordUser(userId);
	}
	
	public FileConfiguration getConfig() {
		return DataBase.getConfig();
	}
	
	public void updateOneDiscord(Bson filter, Bson update) {
		mongoDB.getDiscordCollection().updateOne(filter, update);
	}
	
	public void updateOneServer(Bson filter, Bson update) {
		mongoDB.getServerCollection().updateOne(filter, update);
	}
	
	public MongoCursor<Document> getCollectionIterator() {
		return mongoDB.getServerCollection().find().iterator();
	}
	
	public MongoCursor<Document> getDiscordCollectionIterator() {
		return mongoDB.getDiscordCollection().find().iterator();
	}
	
	public Document getPlayerByDiscordUser(String userId) {
		return getPlayer((UUID) getDiscordUser(userId).get("link"));
	}
	
	public Document getDiscordUserByPlayer(UUID playerId) {
		Document playerDoc = getPlayer(playerId);
		if (playerDoc != null) {
			String discordLink = playerDoc.getString("link");
			log.info("Discord link from player document: " + discordLink);
			return getDiscordUser(discordLink);
		} else {
			log.info("Player document not found for playerId: " + playerId);
			return null;
		}
	}
	
	
	
}
