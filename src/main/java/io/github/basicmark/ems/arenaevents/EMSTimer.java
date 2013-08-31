package io.github.basicmark.ems.arenaevents;

import io.github.basicmark.ems.EMSArena;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class EMSTimer implements EMSArenaEvent{
	// Static data
	String triggerEvent;
	boolean inSeconds;
	boolean repeat;
	String timeString;
	String createEvent;
	String createEventName;

	// Created when parsing the time string
	int timeArray[];
	int repeatCount;
	int repeatTime;
	int repeatMinTime;
	int repeatMaxTime;
	
	// Dynamic data
	EMSArena arena;
	EMSTimerTask timer;
	int singledCount;
	int spentTime;

	//eventTrigger, createName, inSec, repeat, timeString, displayName
	public EMSTimer(EMSArena arena, String triggerEvent, String createEvent, boolean inSeconds, boolean repeat, String timeString, String createEventName) {
		this.triggerEvent = triggerEvent;
		this.inSeconds = inSeconds;
		this.repeat = repeat;
		this.timeString = timeString;
		this.createEvent = createEvent;
		this.createEventName = createEventName;
		this.arena = arena;
		this.timer = null;
		
		singledCount = 0;
		spentTime = 0;
	}
	
	public EMSTimer(Map<String, Object> values) {
		this.triggerEvent = (String) values.get("triggerevent");
		try {
			this.inSeconds = (boolean) values.get("inseconds");
		} catch (Exception e) {
			this.inSeconds = false;
		}
		try {
			this.repeat = (boolean) values.get("repeat");
		} catch (Exception e) {
			this.repeat = false;
		}
		this.timeString = (String) values.get("times");
		this.createEvent = (String) values.get("event");
		this.createEventName = (String) values.get("eventname");
		
		parseTimeString();
		singledCount = 0;
		spentTime = 0;
	}
	
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> values = new LinkedHashMap<String, Object>();

		values.put("triggerevent", triggerEvent);
		values.put("inseconds", inSeconds);
		values.put("repeat", repeat);
		values.put("times", timeString);
		values.put("event", createEvent);
		values.put("eventname", createEventName);
		return values;
	}
	
	public String getListInfo() {
		String timeUnitName = inSeconds?"second(s)":"minute(s)";

		if (repeat) {
			if (repeatCount != 0) {
				return "Fixed repeating timer. Trigged by event " + triggerEvent + " which runs for " + repeatMinTime + " " + timeUnitName + " " + repeatCount + " time(s), each time triggering " + createEvent;
			} else {
				return "Varible repeating time. Triggered by event " + triggerEvent + " which runs for a total time of " + repeatTime + " " + timeUnitName + " and between " + repeatMinTime + "-" + repeatMaxTime + " " + timeUnitName + " it will trigger " + createEvent;  
			}
		} else {
			return "Timer. Triggered by event " + triggerEvent + " which runs for " + timeArray[0] + " " + timeUnitName + " after which it will trigger" + createEvent;
		}
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
			singledCount = 0;
			spentTime = 0;
		}
	}
	
	public void destroy() {
		// Nothing to do as we didn't create any resources in the constructor
	}
	
	public void setArena(EMSArena arena) {
		this.arena = arena;
	}
	
	public boolean parseTimeString() {
		String[] splitTimes = timeString.split(",");
		
		if (repeat == false) {
			timeArray = new int[splitTimes.length];
			int i;

			for (i=0;i<splitTimes.length;i++) {
				try {
					timeArray[i] = Integer.parseInt(splitTimes[i]);
				} catch (Exception e) {
					Bukkit.getLogger().severe("EMSTimer: Failed to parse time array (index = " + i + ", string = " + splitTimes[i] + ")");
					return false;
				}
			}
			return true;
		} else {
			if (splitTimes.length != 2) {
				Bukkit.getLogger().severe("EMSTimer: Failed to parse for repeat time, expected 2 strings but found " + splitTimes.length);
				return false;
			}
		
			String[] splitMinMax = splitTimes[1].split("-");
			if (splitMinMax.length == 1) {
				// No "-" in the 2nd param so we're looking at "repeatcount,repeattime"
				repeatTime = 0;
				try {
					repeatCount = Integer.parseInt(splitTimes[0]);
					repeatMinTime = Integer.parseInt(splitTimes[1]);
					repeatMaxTime = repeatMinTime;
				} catch (Exception e) {
					Bukkit.getLogger().severe("EMSTimer: Failed to parse for fixed repeat timer");
					return false;
				}
				return true;
			} else {
				// There is a "-" in the 2nd param so we're looking at "repeattime,mintime-maxtime"
				repeatCount = 0;
				try {
					repeatMinTime = Integer.parseInt(splitMinMax[0]);
					repeatMaxTime = Integer.parseInt(splitMinMax[1]);
					repeatTime = Integer.parseInt(splitTimes[0]);
				} catch (Exception e) {
					Bukkit.getLogger().severe("EMSTimer: Failed to parse for varible repeat timer");
					return false;
				}
				return true;
			}
		}
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
			int timeUint = inSeconds?20:60*20;

			if (repeat) {
				// Repeat timer processing
				if (repeatCount != 0) {
					// Fixed length repeat count timer
					if (singledCount != repeatCount) {
						int nextTime;

						nextTime = repeatMinTime * timeUint;
						taskID = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, this, nextTime);
					}
					
					// Don't signal when we schedule the 1st timer
					if ((timer.createEvent != null) && (singledCount != 0)) {
						if (timer.createEventName != null) {
							arena.broadcast(ChatColor.GRAY + "[EMS] " + timer.createEventName);
						}
						// Tell the arena an event has happened
						arena.signalEvent(timer.createEvent);
					}
					
					singledCount++;
				} else {
					// Ranged random time out processing
					Random rand = new Random(System.currentTimeMillis());
					int nextTime = rand.nextInt(repeatMaxTime-repeatMinTime);
					nextTime += repeatMinTime;
					// Spent time is in time units not ticks so add the next time before we convert to ticks
					spentTime += nextTime;
					
					// If there is enough time left schedule the timer
					if (spentTime<repeatTime) {
						// convert to ticks
						nextTime *= timeUint;
						taskID = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, this, nextTime);
					}

					// Don't signal when we schedule the 1st timer
					if ((timer.createEvent != null) && (spentTime != nextTime)) {
						if (timer.createEventName != null) {
							arena.broadcast(ChatColor.GRAY + "[EMS] " + timer.createEventName);
						}
						// Tell the arena an event has happened
						arena.signalEvent(timer.createEvent);
					}
				}
			} else {
				// Count time timer processing
				if (index != timer.timeArray.length) {
					String timeUnitName = inSeconds?"second":"minute";
					int nextTime;
					if (timer.createEventName != null) {
						arena.broadcast(ChatColor.GRAY + "[EMS] " + timer.timeArray[index] + " " + timeUnitName + "(s) until " + timer.createEventName);
					}

					// How many ticks until the next timer message?
					if ((index + 1) != timer.timeArray.length) {
						nextTime = timer.timeArray[index] - timer.timeArray[index+1];
					} else {
						// For the last index the value itself if the time to the next message
						nextTime = timer.timeArray[index];
					}
					// convert to ticks
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
		}

		public void cancel() {
			plugin.getServer().getScheduler().cancelTask(taskID);
		}
	}
}
