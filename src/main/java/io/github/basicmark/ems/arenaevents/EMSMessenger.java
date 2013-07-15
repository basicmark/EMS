package io.github.basicmark.ems.arenaevents;

import io.github.basicmark.ems.EMSArena;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class EMSMessenger implements EMSArenaEvent{
	// Static data
	String triggerEvent;
	String message;

	// Dynamic data
	EMSArena arena;
	JavaPlugin plugin;

	public EMSMessenger(EMSArena arena, String triggerEvent, String message) {
		this.triggerEvent = triggerEvent;
		this.message = message;
		this.arena = arena;
	}
	
	public EMSMessenger(Map<String, Object> values) {
		this.triggerEvent = (String) values.get("triggerevent");
		this.message = (String) values.get("message");
	}

	public Map<String, Object> serialize() {
		Map<String, Object> values = new LinkedHashMap<String, Object>();
		values.put("triggerevent", triggerEvent);
		values.put("message", message);
		return values;
	}
	
	public String getListInfo() {
		return "Message. Triggered by " + triggerEvent + " with message " + message;
	}
	
	public void signalEvent(String trigger) {
		if (trigger.equals(triggerEvent)) {
			Iterator<Player> i = arena.getActivePlayers().iterator();
			while(i.hasNext()) {
				Player player = i.next();
				player.sendMessage(ChatColor.GOLD + message);
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
