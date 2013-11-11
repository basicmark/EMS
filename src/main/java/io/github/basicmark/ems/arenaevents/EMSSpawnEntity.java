package io.github.basicmark.ems.arenaevents;

import io.github.basicmark.config.ConfigUtils;
import io.github.basicmark.ems.EMSArena;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public class EMSSpawnEntity implements EMSArenaEvent{
	EMSArena arena;
	String triggerEvent;
	Location location;
	EntityType entity;
	int count;
	
	Set<Entity> spawnedEntities = null;

	public EMSSpawnEntity(EMSArena arena, String triggerEvent, Location location, EntityType entity, int count) {
		this.arena = arena;
		this.triggerEvent = triggerEvent;
		if (entity == EntityType.ENDER_CRYSTAL) {
			this.location = new Location(location.getWorld(), location.getBlockX() + 0.5, location.getBlockY() - 1, location.getBlockZ() + 0.5);
		} else {
			this.location = location;
		}
		this.entity = entity;
		this.count = count;
		
		this.spawnedEntities = new HashSet<Entity>();
	}

	@SuppressWarnings("unchecked")
	public EMSSpawnEntity(Map<String, Object> values) {
		this.triggerEvent = (String) values.get("triggerevent");
		this.location = ConfigUtils.DeserializeLocation((Map<String, Object>) values.get("location"));
		this.entity = EntityType.fromName((String) (values.get("entity")));
		this.count = (int) values.get("count");
		
		this.spawnedEntities = new HashSet<Entity>();
	}

	public String getListInfo() {
		return "Spawn entity. Triggered by " + triggerEvent + " at will spawn " + count + " " + entity.getName() + " at " + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
	}
	
	public void signalEvent(String trigger) {
		// Start the timer task
		if (trigger.equals(triggerEvent)) {
			Set<Entity> newEntities = arena.spawnEntities(location, entity, count);
			spawnedEntities.addAll(newEntities);
		}
	}

	public Map<String, Object> serialize() {
		Map<String, Object> values = new LinkedHashMap<String, Object>();
		values.put("triggerevent", triggerEvent);
		values.put("location", ConfigUtils.SerializeLocation(location));
		values.put("entity", entity.getName());
		values.put("count", count);
		return values;
	}
	
	public void cancelEvent() {
		if (!spawnedEntities.isEmpty()) {
			arena.removeEntities(spawnedEntities);
		}
	}
	
	public void destroy() {
		// The gc will free our allocated resources
	}
	
	public void setArena(EMSArena arena) {
		this.arena = arena;
	}
}
