package io.github.basicmark.ems;

import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import io.github.basicmark.config.PlayerState;
import io.github.basicmark.config.ConfigUtils;

public class EMSPlayerRejoinData extends PlayerState {
	Location location;
	String teamName;
	boolean autoRejoin;
	int activeTime;

	public EMSPlayerRejoinData(Player player, String team, boolean rejoin, int time) {
		super(player, true, true, true);
		location = player.getLocation();
		teamName = team;
		autoRejoin = rejoin;
		activeTime = time;
	}
	
	@SuppressWarnings("unchecked")
	public EMSPlayerRejoinData(Map<String, Object> values) {
		super(values);
		location = ConfigUtils.DeserializeLocation((Map<String, Object>) values.get("location"));
		teamName = (String) values.get("teamname");
		autoRejoin = (boolean) values.get("autorejoin");
		activeTime = (int) values.get("activetime");
	}
	
	public Map<String, Object> serialize() {
		Map<String, Object> values = super.serialize();
		values.put("location", ConfigUtils.SerializeLocation(location));
		values.put("teamname", teamName);
		values.put("autorejoin", autoRejoin);
		values.put("activetime", activeTime);
		return values;
	}
	
	public String getTeam() {
		return teamName;
	}
	
	public Location getLocation() {
		return location;
	}
	
	public boolean getAutoRejoin() {
		return autoRejoin;
	}
	
	public int getActiveTime() {
		return activeTime;
	}
}


