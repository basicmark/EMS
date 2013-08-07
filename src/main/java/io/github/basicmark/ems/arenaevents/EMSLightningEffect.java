package io.github.basicmark.ems.arenaevents;

import io.github.basicmark.config.ConfigUtils;
import io.github.basicmark.ems.EMSArena;

import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

public class EMSLightningEffect implements EMSArenaEvent{
	// Static data
	String triggerEvent;
	Location location;

	// Dynamic data
	EMSArena arena;
	JavaPlugin plugin;

	public EMSLightningEffect(EMSArena arena, String triggerEvent, Location location) {
		this.triggerEvent = triggerEvent;
		this.location = location;
		this.arena = arena;
	}
	
	@SuppressWarnings("unchecked")
	public EMSLightningEffect(Map<String, Object> values) {
		this.triggerEvent = (String) values.get("triggerevent");
		this.location = ConfigUtils.DeserializeLocation((Map<String, Object>) values.get("location"));
	}

	public String getListInfo() {
		return "Lightning effect. Triggered by " + triggerEvent + " will cause a lightning effect to happen at " + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
	}
	
	public void signalEvent(String trigger) {
		// Start the timer task
		if (trigger.equals(triggerEvent)) {
			// Only create the timer for tracking if one isn't already running
			location.getWorld().strikeLightningEffect(location);
		}
	}


	public Map<String, Object> serialize() {
		Map<String, Object> values = new LinkedHashMap<String, Object>();
		values.put("triggerevent", triggerEvent);
		values.put("location", ConfigUtils.SerializeLocation(location));
		return values;
	}
	
	public void cancelEvent() {
		// Nothing to do as we didn't create any resources in the trigger
	}
	
	public void destroy() {
		// Nothing to do as we didn't create any resources in the constructor
	}
	
	public void setArena(EMSArena arena) {
		this.arena = arena;
	}
}
