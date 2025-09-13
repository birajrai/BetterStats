package io.github.birajrai.betterstats.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import io.github.birajrai.betterstats.server.ServerManager;
import io.github.birajrai.betterstats.utils.DiscordUtil;

import net.dv8tion.jda.api.JDA;

/**
 * @author Biraj Rai
 * 2025 - 2028
 */
public class MessageListener implements Listener {
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onMessageReceived(AsyncPlayerChatEvent event) {
		
		Bukkit.getScheduler().runTaskAsynchronously(ServerManager.getPlugin(), () ->
        processChatMessage(
                event.getPlayer(),
                event.getMessage(),
                event.isCancelled()
        ));
	}
	
	private void processChatMessage(Player player, String message, boolean isCancelled) {
		
		if(!isCancelled) {
			JDA jda = DiscordUtil.getJda();
			
			String messageToDiscord = DiscordUtil.buildMinecraftToDiscord(player, message);
			
			jda.getGuildById(DiscordUtil.getGuildId()).getTextChannelById(DiscordUtil.getChannelId()).sendMessage(messageToDiscord).queue();
			
		}
	}
}
