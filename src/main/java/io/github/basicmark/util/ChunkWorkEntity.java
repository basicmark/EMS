package io.github.basicmark.util;

import java.util.HashMap;
import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

public class ChunkWorkEntity implements ChunkWork<Entity> {
	HashMap<Entity, Runnable> tasks;

	ChunkWorkEntity() {
		tasks = new HashMap<Entity,Runnable>();
	}

	public void add(Entity entity, Runnable task) {
		Bukkit.getLogger().info("Added " + entity.toString());
		tasks.put(entity, task);
	}

	public void doWork() {
		Iterator<Runnable> i = tasks.values().iterator();

		while (i.hasNext()) {
			Runnable task = i.next();
			Bukkit.getLogger().info("Running work");
			task.run();
		}
	}
}
