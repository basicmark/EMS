package io.github.basicmark.ems.arenaevents;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import io.github.basicmark.config.ConfigUtils;
import io.github.basicmark.ems.EMSArena;

public class EMSTeleport implements EMSArenaEvent{
	EMSArena arena;
	String triggerEvent;
	Location location;

	public EMSTeleport(EMSArena arena, String triggerEvent, Location location) {
		this.arena = arena;
		this.triggerEvent = triggerEvent;
		this.location = location;
	}

	@SuppressWarnings("unchecked")
	public EMSTeleport(Map<String, Object> values) {
		this.triggerEvent = (String) values.get("triggerevent");
		this.location = ConfigUtils.DeserializeLocation((Map<String, Object>) values.get("location"));
	}

	public String getListInfo() {
		return "Teleport all players. Triggered by " + triggerEvent + " at will teleport all remaining players to " + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
	}
	
	public void signalEvent(String trigger) {
		// Start the timer task
		if (trigger.equals(triggerEvent)) {
			Iterator<Player> i = arena.getActivePlayers().iterator();
			while(i.hasNext()) {
				Player player = i.next();
				player.teleport(location);
			}
		}
	}

	public Map<String, Object> serialize() {
		Map<String, Object> values = new LinkedHashMap<String, Object>();
		values.put("triggerevent", triggerEvent);
		values.put("location", ConfigUtils.SerializeLocation(location));
		return values;
	}
	
	public void cancelEvent() {
		// Nothing to do
	}
	
	public void destroy() {
		// Nothing to do as we didn't create any resources in the constructor
	}
	
	public void setArena(EMSArena arena) {
		this.arena = arena;
	}
}
