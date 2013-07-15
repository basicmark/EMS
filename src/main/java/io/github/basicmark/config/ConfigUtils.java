package io.github.basicmark.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class ConfigUtils {
	public static Map<String, Object> SerializeLocation(Location loc) {
		Map<String, Object> LocData = new LinkedHashMap<String, Object>();
		
		LocData.put("world", loc.getWorld().getName());
		LocData.put("x", loc.getX());
		LocData.put("y", loc.getY());
		LocData.put("z", loc.getZ());
		LocData.put("yaw", Float.toString(loc.getYaw()));
		LocData.put("pitch", Float.toString(loc.getPitch()));
		return LocData;
	}
	
	public static Location DeserializeLocation(Map<String, Object> LocData) {
		String name;
		double x,y,z;
		float yaw, pitch;
	
		if (LocData == null) {
			return null;
		}
		name = (String) LocData.get("world");
		x = (double) LocData.get("x");
		y = (double) LocData.get("y");
		z = (double) LocData.get("z");
		yaw = (float) Float.parseFloat((String) LocData.get("yaw"));
		pitch = (float) Float.parseFloat((String) LocData.get("pitch"));

		return new Location(Bukkit.getServer().getWorld(name), x , y, z, yaw, pitch);
	}
	
	public static String SerializeIntArray(int[] array) {
		String result = new String();
		int i;
		for (i=0;i<array.length;i++) {
			if (i==0) {
				result += array[i];
			} else {
				result += "," + array[i];
			}
		}
		return result;
	}
	
	public static int[] DeserializeIntArray(String array) {
		String[] tmp = array.split(",");
		int[] result = new int[tmp.length];
		int i;
		for (i=0;i<tmp.length;i++) {
			result[i] = Integer.parseInt(tmp[i]);
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public static List<String> castToStringList(Object value) {
		List<String> result;
		try {
			result = (List<String>) value;
		} catch(Exception e) {
			result = null;
		}
		if(result == null) {
			return new ArrayList<String>();
		}
		return result;
	}
}
