package io.github.basicmark.ems.arenaevents;

import io.github.basicmark.ems.EMSArena;

import java.util.LinkedHashMap;
import java.util.Map;

public class EMSCheckTeamPlayerCount implements EMSArenaEvent{
	// Dynamic data
	EMSArena arena;
	boolean team;
	String triggerEvent;
	int count;
	String createEvent;
	boolean fired;

	public EMSCheckTeamPlayerCount(EMSArena arena, boolean team, String triggerEvent, int count, String createEvent) {
		this.arena = arena;
		this.team = team;
		this.triggerEvent = triggerEvent;
		this.count = count;
		this.createEvent = createEvent;
		this.fired = false; 
	}
	
	public EMSCheckTeamPlayerCount(Map<String, Object> values) {
		this.team = (boolean) values.get("team");
		this.triggerEvent = (String) values.get("triggerevent");
		this.count = (int) values.get("count");
		this.createEvent = (String) values.get("createevent");
		this.fired = false;
	}

	public String getListInfo() {
		String teamplayer;
		if (team) {
			teamplayer = "teams";
		} else {
			teamplayer = "players";
		}
		return "Check player event. Triggered by " + triggerEvent + " will check if " + count + " or less " + teamplayer + " reamin and if so " + createEvent + " event will trigger";
	}
	
	public void signalEvent(String trigger) {
		if (team) {
			if ((trigger.equals(triggerEvent)) && (count >= arena.getActiveTeamCount()) && (!fired)) {
				fired = true;
				arena.signalEvent(createEvent);
			}		
		} else {
			if ((trigger.equals(triggerEvent)) && (count >= arena.getActivePlayerCount()) && (!fired)) {
				fired = true;
				arena.signalEvent(createEvent);
			}
		}
	}


	public Map<String, Object> serialize() {
		Map<String, Object> values = new LinkedHashMap<String, Object>();
		values.put("team", team);
		values.put("triggerevent", triggerEvent);
		values.put("count", count);
		values.put("createevent", createEvent);
		return values;
	}
	
	public void cancelEvent() {
		fired = false;
	}
	
	public void destroy() {
		// Nothing to do as we didn't create any resources in the constructor
	}
	
	public void setArena(EMSArena arena) {
		this.arena = arena;
	}
}
