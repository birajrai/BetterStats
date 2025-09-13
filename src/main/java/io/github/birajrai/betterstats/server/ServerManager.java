package io.github.birajrai.betterstats.server;

import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.plugin.Plugin;
import io.github.birajrai.betterstats.Main;
import io.github.birajrai.betterstats.mongoDB.DataBase;
import io.github.birajrai.betterstats.mongoDB.DataBaseManager;
import io.github.birajrai.betterstats.player.ServerPlayer;

/**
 * @author Biraj Rai
 * 2025 - 2028
 */
public class ServerManager {
	
	private HashMap<UUID, ServerPlayer> stats;
	private DataBaseManager mongoDB;
	private static Plugin plugin;

	public ServerManager(DataBase mongoDB, Logger log, Plugin plugin) {
		this.stats = new HashMap<UUID, ServerPlayer>();
		this.mongoDB = new DataBaseManager(mongoDB, log, this);
		ServerManager.plugin = plugin;
	}
	
	public DataBaseManager getDataBaseManager() {
		return mongoDB;
	}
	
	public static Plugin getPlugin() {
		return plugin;
	}
	
	/**
	 * @return Minecraft version the server is currently running on.
	 */
	public static String getServerVersion() {
		String version = Main.currentServer.getVersion();
		int start = version.indexOf("MC: ") + 4;
		int end = version.length() - 1;
		return version.substring(start, end);
	}
	
	/**
	 * Uploads all players statistics to database
	 */
	public void uploadAll() {
		for(ServerPlayer ps : stats.values()) {
			ps.flushSessionPlaytime();
			ps.stopPersisting();
			mongoDB.uploadToDataBase(ps);
			ps.startPersisting();
		}
	}
	
	public void logOutAllPlayers() {
		synchronized(stats) {
			for(ServerPlayer pp : stats.values())
				pp.quit();
		}
		uploadAll();
	}
	
	public boolean playerAlreadyInServer(UUID playerId) {
		return stats.containsKey(playerId);
	}
	
	public ServerPlayer getPlayerStats(UUID playerId) {
		return stats.get(playerId);
	}
	
	public void newPlayerOnServer(ServerPlayer pp) {
		stats.put(pp.getPlayerID(), pp);
	}
	
	public ServerPlayer getPlayerFromHashMap(UUID player) {
		return stats.get(player);
	}
	
	public ServerPlayer deleteFromHashMap(UUID player) {
		return stats.remove(player);
	}
	
}
