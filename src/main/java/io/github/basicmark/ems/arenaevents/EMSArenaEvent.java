package io.github.basicmark.ems.arenaevents;

import io.github.basicmark.ems.EMSArena;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

public interface EMSArenaEvent extends ConfigurationSerializable {
	public void setArena(EMSArena arena);
	
	/*
	 * Return a string describing the event
	 */
	public String getListInfo();

	/*
	 * Notify the arena event that an event of the specified name has happened
	 */
	public void signalEvent(String eventName);

	/*
	 * Cancel the arena event. This is called on the termination of an event
	 * being hosted in the arena this arena event is registered with regardless
	 * of if the event was singled or not 
	 */
	public void cancelEvent();

	/*
	 * Destroy the arena event, called when the event is to be removed from arena.
	 * Any resources allocated, protections made etc should be "undone" in this function
	 */
	public void destroy();
}
