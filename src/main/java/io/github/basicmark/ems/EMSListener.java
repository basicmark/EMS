package io.github.basicmark.ems;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

public class EMSListener implements Listener {
	private EMSManager manager;
	
	public EMSListener(EMSManager manager) {
		this.manager = manager;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();

		manager.playerJoinServer(player);
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();

		manager.playerLeaveServer(player);
	}
	
	@EventHandler
	public void onPlayerDeathEvent(PlayerDeathEvent event) {
		Player player = event.getEntity();

		/* The following function will return true if the drops should be cleared */
		if (manager.playerDied(player)) {
			event.getDrops().clear();
		}
	}
	
	@EventHandler
	public void onPlayerRespawnEvent(PlayerRespawnEvent event) {
		Player player = event.getPlayer();
		
		manager.playerRespawn(player);
	}
	
	@EventHandler
	public void onPlayerChatEvent(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		EMSChatResponse response = manager.playerChat(player, event.getMessage());
		
		if (response != null) {
			if (response.isCanceled()){
				event.setCancelled(true);
			}
		
			if (response.getMessage() != null) {
				event.setMessage(response.getMessage());
			}
		}
	} 	
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		manager.playerInteract(event);
	}
	
	@EventHandler
	public void onSignChangeEvent(SignChangeEvent event) {
		
		manager.signChanged(event);
	}
	
	@EventHandler
	public void onBlockBreakEvent(BlockBreakEvent event) {
		Player player = event.getPlayer();
		Block block = event.getBlock();
		
		// The manager has consumed the event so cancel it
		if (manager.blockBroken(player, block.getLocation())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onWorldLoad(WorldLoadEvent event) {
		World world = event.getWorld();

		manager.loadWorld(world);
	}
	
	@EventHandler
	public void onWorldUnload(WorldUnloadEvent event) {
		World world = event.getWorld();

		manager.unloadWorld(world);
	}
}
