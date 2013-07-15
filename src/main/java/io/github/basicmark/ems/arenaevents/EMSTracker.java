package io.github.basicmark.ems.arenaevents;

import io.github.basicmark.ems.EMSArena;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class EMSTracker implements EMSArenaEvent{
	// Dynamic data
	EMSArena arena;
	EMSTrackerTimerTask tracker;

	public EMSTracker(EMSArena arena) {
		this.arena = arena;
		this.tracker = null;
	}
	
	public EMSTracker(Map<String, Object> values) {
	}

	public String getListInfo() {
		return "Lightning tracking. Triggered by start-tracking event and canceled by end-tracking event";
	}
	
	public void signalEvent(String trigger) {
		// Start the timer task
		if (trigger.equals("start-tracking")) {
			// Only create the timer for tracking if one isn't already running
			if (tracker == null) {
				JavaPlugin plugin = arena.getPlugin();
				tracker = new EMSTrackerTimerTask(this);
				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, (Runnable) tracker);
			}
		} else if (trigger.equals("end-tracking")) {
			cancelEvent();
		}
	}


	public Map<String, Object> serialize() {
		Map<String, Object> values = new LinkedHashMap<String, Object>();
		return values;
	}
	
	public void cancelEvent() {
		if (tracker != null) {
			tracker.cancel();
			tracker = null;
		}
	}
	
	public void destroy() {
		// Nothing to do as we didn't create any resources in the constructor
	}
	
	public void setArena(EMSArena arena) {
		this.arena = arena;
	}
	
	// FIXME: Merge this class into EMSTimer?
	public class EMSTrackerTimerTask implements Runnable {
		EMSTracker tracker;
		JavaPlugin plugin;
		int taskID;
		int index;

		public EMSTrackerTimerTask(EMSTracker tracker) {
			this.tracker = tracker;
			this.plugin = tracker.arena.getPlugin();
		}

		@Override
		public void run() {
			Iterator<Player> i = arena.getActivePlayers().iterator();
			while(i.hasNext()) {
				Player player = i.next();
				World world = player.getWorld();
				world.strikeLightningEffect(player.getLocation());
			}
			taskID = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, this, 30*20);
		}

		public void cancel() {
			plugin.getServer().getScheduler().cancelTask(taskID);
		}
	}
}

