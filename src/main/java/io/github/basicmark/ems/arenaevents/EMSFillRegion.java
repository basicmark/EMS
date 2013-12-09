package io.github.basicmark.ems.arenaevents;

import io.github.basicmark.config.ConfigUtils;
import io.github.basicmark.ems.EMSArena;

import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

public class EMSFillRegion implements EMSArenaEvent{
	// Static data
	String triggerEvent;
	Location pos1;
	Location pos2;
	Material type;

	// Dynamic data
	EMSArena arena;

	public EMSFillRegion(EMSArena arena, String triggerEvent, Location pos1, Location pos2, String blockType) {
		this.triggerEvent = triggerEvent;
		this.pos1 = pos1;
		this.pos2 = pos2;
		this.type = Material.matchMaterial(blockType);
		if (this.type == null) {
			type = Material.AIR;
		} else if (!this.type.isBlock()) {
			type = Material.AIR;
		}
		this.arena = arena;
	}
	
	@SuppressWarnings("unchecked")
	public EMSFillRegion(Map<String, Object> values) {
		this.triggerEvent = (String) values.get("triggerevent");
		this.pos1 = ConfigUtils.DeserializeLocation((Map<String, Object>) values.get("pos1"));
		this.pos2 = ConfigUtils.DeserializeLocation((Map<String, Object>) values.get("pos2"));
		this.type = Material.getMaterial((String) values.get("type"));
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> values = new LinkedHashMap<String, Object>();
		values.put("triggerevent", triggerEvent);
		values.put("pos1", ConfigUtils.SerializeLocation(pos1));
		values.put("pos2", ConfigUtils.SerializeLocation(pos2));
		values.put("type", type.toString());
		return values;
	}
	
	public String getListInfo() {
		return "Fill region. Triggered by " + triggerEvent + " and fills " + pos1.getBlockX() + "," + pos1.getBlockY() + "," + pos1.getBlockZ() + " to " + pos2.getBlockX() + "," + pos2.getBlockY() + "," + pos2.getBlockZ() + " with " + type;
	}
	
	public void signalEvent(String trigger) {
		// Walk all the blocks in the selection clearing them (setting them to air)
		if (trigger.equals(triggerEvent)) {
			Location directionLocation = pos2.clone();
			directionLocation.subtract(pos1);
			int stepX = (directionLocation.getBlockX() < 0)?-1:1;
			int stepY = (directionLocation.getBlockY() < 0)?-1:1;
			int stepZ = (directionLocation.getBlockZ() < 0)?-1:1;

			for (int x=pos1.getBlockX();x!=pos2.getBlockX() + stepX;x += stepX) {
				for (int y=pos1.getBlockY();y!=pos2.getBlockY() + stepY;y += stepY) {
					for (int z=pos1.getBlockZ();z!=pos2.getBlockZ() + stepZ;z += stepZ) {
						Location location = new Location(pos1.getWorld(), x, y, z);
						Block block = location.getBlock();
						BlockState state = block.getState();
						state.setType(type);
						state.update(true);
					}
				}
			}
		}
	}
	
	public void cancelEvent() {
		// Nothing to do
	}
	
	public void destroy() {
		// Nothing to do
	}
	
	public void setArena(EMSArena arena) {
		this.arena = arena;
	}

}
