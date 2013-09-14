package io.github.basicmark.util;

import java.util.HashMap;
import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class TeleportQueue implements Runnable {
	Plugin plugin;
	HashMap<Player, Teleport> players;

	public TeleportQueue(Plugin plugin) {
		this.plugin = plugin;
		this.players = new HashMap<Player, Teleport>();
	}

	public void addPlayer(Player player, Location location) {
		players.put(player, new Teleport(player, location));
	}

	public void removePlayer(Player player) {
		players.remove(player);
	}
	
	public void startTeleport() {
		if (!players.isEmpty()) {
			run();
		}
	}

	public void run() {
		Iterator<Teleport> i = players.values().iterator();

		Teleport teleport = i.next();
		teleport.doTeleport();
		i.remove();
		/* Only schedule the task if we have more players */
		if (i.hasNext()) {
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, this, 5);
		}
	}
	
	private class Teleport {
		Player player;
		Location location;
		
		public Teleport(Player player, Location location) {
			this.player = player;
			this.location = location;
		}
		
		public void doTeleport() {
			player.teleport(location);
		}
	}
}
