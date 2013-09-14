package io.github.basicmark.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public enum SpawnMethod {
	ROUNDROBIN, RANDOM, RANDOM_ONCE, INVALID;
	
	public static boolean spawnPlayers(SpawnMethod method, Set<Player> players, List<Location> locations, TeleportQueue queue) {
		switch (method) {
			case ROUNDROBIN:
			{
				Location[] locationArray = locations.toArray(new Location[]{});
				Iterator<Player> it = players.iterator();
				int i=0;

				while(it.hasNext()) {
					Player player = it.next();
					queue.addPlayer(player, locationArray[i++]);
					if (i >= locationArray.length) {
						i = 0;
					}
				}
				break;
			}
			case RANDOM:
			{
				Location[] locationArray = locations.toArray(new Location[]{});
				Random rand = new Random(System.currentTimeMillis());
				Iterator<Player> it = players.iterator();

				while(it.hasNext()) {
					//[rand.nextInt(set.size())
					int i = rand.nextInt(locationArray.length);
					Player player = it.next();
					queue.addPlayer(player, locationArray[i]);
				}
				break;
			}
			case RANDOM_ONCE:
			{
				Set<Location> randLocs = new HashSet<Location>();
				Random rand = new Random(System.currentTimeMillis());
				Iterator<Player> ip = players.iterator();

				if (players.size() > locations.size()) {
					// There are more players then locations!
					Bukkit.getLogger().info("There are more players then locations!");
					return false;
				}
				// Create a copy of the locations and for each player find a random location
				// before then removing that location for the next time around the loop
				randLocs.addAll(locations);

				while(ip.hasNext()) {
					Player player = ip.next();
					Iterator<Location> il = randLocs.iterator();
					Location loc = null;
					int r = rand.nextInt(randLocs.size());
					int k = 0;

					while(il.hasNext()) {
						loc = il.next();
						if (r == k) {
							queue.addPlayer(player, loc);
							break;
						}
						k++;
					}
					randLocs.remove(loc);
				}
				break;
			}
			case INVALID:
				return false;
		}
		return true;
	}
	
	public static SpawnMethod fromString(String string) {
		 if (string != null) {
			 for (SpawnMethod method : SpawnMethod.values()) {
		        if (string.equalsIgnoreCase(method.toString())) {
		        	return method;
		        }
			 }
		 }
		 return INVALID;
	}
}
