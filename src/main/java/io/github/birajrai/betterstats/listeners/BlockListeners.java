package io.github.birajrai.betterstats.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 * @author Biraj Rai
 * 2025 - 2028
 */
public class BlockListeners implements Listener {
	
	private ListenersController controller;
	
	public BlockListeners(ListenersController controller) {
		this.controller = controller;
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onBlockPlace(BlockPlaceEvent event) {
		if(!event.isCancelled()) { 
			controller.placeBlock(event.getPlayer(), event.getBlock());
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onBlockBreak(BlockBreakEvent event) {
		if(!event.isCancelled()) { 
			controller.breakBlock(event.getPlayer(), event.getBlock());
		}
	}
	
}
