package io.github.basicmark.ems.arenaevents;

import io.github.basicmark.ems.EMSArena;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class EMSPotionEffect implements EMSArenaEvent{
	EMSArena arena;
	String triggerEvent;
	PotionEffectType effect;
	int duration;
	int amplifier;
	
	public EMSPotionEffect(EMSArena arena, String triggerEvent, PotionEffectType effect, int duration, int amplifier) {
		this.triggerEvent = triggerEvent;
		this.effect = effect;
		this.duration = duration;
		this.amplifier = amplifier;
		this.arena = arena;
	}
	
	public EMSPotionEffect(Map<String, Object> values) {
		this.triggerEvent = (String) values.get("triggerevent");
		this.effect = PotionEffectType.getByName((String) values.get("effect"));
		this.duration = (int) values.get("duration");
		try {
			this.amplifier = (int) values.get("amplifier");
		} catch (Exception e) {
			this.amplifier = 0;
		}
	}

	public Map<String, Object> serialize() {
		Map<String, Object> values = new LinkedHashMap<String, Object>();
		values.put("triggerevent", triggerEvent);
		values.put("effect", effect.getName());
		values.put("duration", duration);
		values.put("amplifier", amplifier);
		return values;
	}
	
	public String getListInfo() {
		return "Potion effect. Triggered by event " + triggerEvent + " and creates " + effect.getName() + " for all players for " + duration + " seconds and with amplifier " + amplifier;
	}
	
	public void signalEvent(String trigger) {
		if (trigger.equals(triggerEvent)) {
			Iterator<Player> i = arena.getActivePlayers().iterator();
			while(i.hasNext()) {
				Player player = i.next();
				player.addPotionEffect(new PotionEffect(effect, 20 * duration, amplifier));
			}
		}
	}

	public void cancelEvent() {
		/*
		 *  We don't need to worry about cancelling the portion effect as when
		 *  the player leaves the arena all effects will be cancelled anyway 
		 */
	}
	
	public void destroy() {
		// Nothing to do as we didn't create any resources in the constructor
	}
	
	public void setArena(EMSArena arena) {
		this.arena = arena;
	}
}

