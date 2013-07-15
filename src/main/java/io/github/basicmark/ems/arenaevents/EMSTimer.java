package io.github.basicmark.ems.arenaevents;


import io.github.basicmark.config.ConfigUtils;
import io.github.basicmark.ems.EMSArena;

import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class EMSTimer implements EMSArenaEvent{
	// Static data
	String triggerEvent;
	boolean inSeconds;
	int[] times;
	String createEvent;
	String createEventName;

	// Dynamic data
	EMSArena arena;
	EMSTimerTask timer;

	public EMSTimer(EMSArena arena, String triggerEvent, String createEvent, boolean inSeconds, int[] times, String createEventName) {
		this.triggerEvent = triggerEvent;
		this.inSeconds = inSeconds;
		this.times = times.clone();
		this.createEvent = createEvent;
		this.createEventName = createEventName;
		this.arena = arena;
		this.timer = null;
	}
	
	public EMSTimer(Map<String, Object> values) {
		this.triggerEvent = (String) values.get("triggerevent");
		try {
			this.inSeconds = (boolean) values.get("inseconds");
		} catch (Exception e) {
			this.inSeconds = false;
		}
		this.times = ConfigUtils.DeserializeIntArray((String) values.get("times"));
		this.createEvent = (String) values.get("event");
		this.createEventName = (String) values.get("eventname");
	}
	
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> values = new LinkedHashMap<String, Object>();

		values.put("triggerevent", triggerEvent);
		values.put("inseconds", inSeconds);
		values.put("times", ConfigUtils.SerializeIntArray(times));
		values.put("event", createEvent);
		values.put("eventname", createEventName);
		return values;
	}
	
	public String getListInfo() {
		String timeUnitName = inSeconds?"second":"minute";
		return "Timer. Triggered by event " + triggerEvent + " which runs for " + times[0] + " " + timeUnitName + "(s) after which " + createEvent + " event will trigger";
	}
	
	@Override
	public void signalEvent(String trigger) {
		// Start the timer task
		if (trigger.equals(triggerEvent)) {
			JavaPlugin plugin = arena.getPlugin();
			timer = new EMSTimerTask(this);
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, (Runnable) timer);
		}
	}
	
	public void cancelEvent() {
		if (timer != null) { 
			timer.cancel();
		}
	}
	
	public void destroy() {
		// Nothing to do as we didn't create any resources in the constructor
	}
	
	public void setArena(EMSArena arena) {
		this.arena = arena;
	}
	
	// FIXME: Merge this class into EMSTimer?
	public class EMSTimerTask implements Runnable {
		EMSTimer timer;
		JavaPlugin plugin;
		int taskID;
		int index;

		public EMSTimerTask(EMSTimer timer) {
			this.plugin = timer.arena.getPlugin();
			this.timer = timer;
			index = 0;
		}

		@Override
		public void run() {
			if (index != timer.times.length) {
				String timeUnitName = inSeconds?"second":"minute";
				int timeUint = inSeconds?20:60*20;
				int nextTime;
				if (timer.createEventName != null) {
					arena.broadcast(ChatColor.GRAY + "[EMS] " + timer.times[index] + " " + timeUnitName + "(s) until " + timer.createEventName);
				}

				// How many ticks until the next timer message?
				if ((index + 1) != timer.times.length) {
					nextTime = timer.times[index] - timer.times[index+1];
				} else {
					// For the last index the value itself if the time to the next message
					nextTime = timer.times[index];
				}
				// Turn seconds into ticks
				nextTime *= timeUint;
				taskID = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, this, nextTime);
				index++;
			} else {
				if (timer.createEventName != null) {
					arena.broadcast(ChatColor.GRAY + "[EMS] " + timer.createEventName);
				}
				if (timer.createEvent != null) {
					// Tell the arena an event has happened
					arena.signalEvent(timer.createEvent);
				}
			}
		}

		public void cancel() {
			plugin.getServer().getScheduler().cancelTask(taskID);
		}
	}
}
