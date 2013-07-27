package io.github.basicmark.ems.arenaevents;

import io.github.basicmark.ems.EMSArena;

import java.util.LinkedHashMap;
import java.util.Map;


public class EMSAutoEnd implements EMSArenaEvent{
	// Dynamic data
	EMSArena arena;

	public EMSAutoEnd(EMSArena arena) {
		this.arena = arena;
	}
	
	public EMSAutoEnd(Map<String, Object> values) {
	}

	public String getListInfo() {
		return "Automatically event the event. Triggered by end-event";
	}
	
	public void signalEvent(String trigger) {
		// Start the timer task
		if (trigger.equals("end-event")) {
			arena.endEvent();
		}
	}


	public Map<String, Object> serialize() {
		Map<String, Object> values = new LinkedHashMap<String, Object>();
		return values;
	}
	
	public void cancelEvent() {
		// Nothing to do as we don't create any resources in signalEvent
	}
	
	public void destroy() {
		// Nothing to do as we didn't create any resources in the constructor
	}
	
	public void setArena(EMSArena arena) {
		this.arena = arena;
	}
}
