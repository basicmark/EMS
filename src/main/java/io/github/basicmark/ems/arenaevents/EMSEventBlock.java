package io.github.basicmark.ems.arenaevents;

import io.github.basicmark.config.ConfigUtils;
import io.github.basicmark.ems.EMSArena;

import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.plugin.java.JavaPlugin;

public class EMSEventBlock implements EMSArenaEvent{
	// Static data
	String triggerEvent;
	Location location;
	Boolean inverted;

	// Dynamic data
	EMSArena arena;
	JavaPlugin plugin;

	public EMSEventBlock(EMSArena arena, Location location, String triggerEvent, boolean inverted) {
		this.triggerEvent = triggerEvent;
		this.location = location;
		this.inverted = inverted;
		this.arena = arena;
		arena.addProtection(location);
	}
	
	@SuppressWarnings("unchecked")
	public EMSEventBlock(Map<String, Object> values) {
		this.triggerEvent = (String) values.get("triggerevent");
		this.location = ConfigUtils.DeserializeLocation((Map<String, Object>) values.get("location"));
		this.inverted = (Boolean) values.get("inverted");
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> values = new LinkedHashMap<String, Object>();
		values.put("triggerevent", triggerEvent);
		values.put("location", ConfigUtils.SerializeLocation(location));
		values.put("inverted", inverted);
		return values;
	}
	
	public String getListInfo() {
		return "Event block. Triggered by " + triggerEvent + " and is at " + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
	}
	
	public void signalEvent(String trigger) {
		// Start the timer task
		if (trigger.equals(triggerEvent)) {
			Block block = location.getBlock();
			BlockState state = block.getState();
			if (inverted) {
				state.setType(Material.IRON_BLOCK);
			} else {
				state.setType(Material.REDSTONE_BLOCK);
			}
			state.update(true);
		}
	}
	
	public void cancelEvent() {
		Block block = location.getBlock();
		BlockState state = block.getState();
		if (inverted) {
			state.setType(Material.REDSTONE_BLOCK);
		} else {
			state.setType(Material.IRON_BLOCK);
		}
		state.update(true);
	}
	
	public void destroy() {
		arena.removeProtection(location);
	}
	
	public void setArena(EMSArena arena) {
		this.arena = arena;
		arena.addProtection(location);
	}
}
