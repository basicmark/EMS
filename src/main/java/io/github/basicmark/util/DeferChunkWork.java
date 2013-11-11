package io.github.basicmark.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

public class DeferChunkWork<D, T extends ChunkWork<D>> {
	Plugin plugin;
	HashMap<UnloadedChunk, T> pendingWork;
	Set<UnloadedChunk> forceLoaded;

	public DeferChunkWork(Plugin plugin) {
		this.plugin = plugin;
		pendingWork = new HashMap<UnloadedChunk, T>();
		forceLoaded = new HashSet<UnloadedChunk>();
	}
	
	public void addWork(Location location, D data, Runnable task, Class<T> clazz) {
		final UnloadedChunk chunk = new UnloadedChunk(location);
		T chunkWork;
		
		if (!pendingWork.containsKey(chunk)) {
			try {
				chunkWork = clazz.newInstance();
			} catch (Exception e) {
				Bukkit.getLogger().throwing("DeferChunkWork", "addWork", e);
				return;
			}
			pendingWork.put(chunk, chunkWork);
		} else {
			chunkWork = pendingWork.get(chunk);
		}
		
		chunkWork.add(data, task);
	}
	
	public void chunkLoad(Chunk chunk) {
		final UnloadedChunk loadedChunk = new UnloadedChunk(chunk);

		if (pendingWork.containsKey(loadedChunk)) {
			T work = pendingWork.get(loadedChunk);
			boolean forced = forceLoaded.contains(loadedChunk);

			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new DeferChunkLoadWork<D,T>(work, chunk, forced), 10);
			pendingWork.remove(loadedChunk);
			if (forced) {
				forceLoaded.remove(forceLoaded);
			}
		}
	}
	
	public void forceWork() {
		Set<UnloadedChunk> keys = new HashSet<UnloadedChunk>(pendingWork.keySet());
		Iterator <UnloadedChunk> i = keys.iterator();
		
		while (i.hasNext()) {
			UnloadedChunk key = i.next();
			World world = Bukkit.getWorld(key.world);
			if (world.isChunkLoaded(key.chunkX, key.chunkZ)) {
				// The chunk is already loaded so do the work
				pendingWork.get(key).doWork();
				pendingWork.remove(key);
				
			} else {
				Chunk chunk;
				chunk = world.getChunkAt(key.chunkX, key.chunkZ);
				chunk.load(false);
				forceLoaded.add(key);
			}
		}
	}

	private class UnloadedChunk {
		UUID world;
		int chunkX;
		int chunkZ;
		
		UnloadedChunk(Location location) {
			this.world = location.getWorld().getUID();
			this.chunkX = location.getBlockX() >> 4;
			this.chunkZ = location.getBlockZ() >> 4;
		}

		UnloadedChunk(Chunk chunk) {
			this.world = chunk.getWorld().getUID();
			this.chunkX = chunk.getX();
			this.chunkZ = chunk.getZ();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + chunkX;
			result = prime * result + chunkZ;
			result = prime * result + ((world == null) ? 0 : world.hashCode());
			return result;
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			UnloadedChunk other = (UnloadedChunk) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (chunkX != other.chunkX)
				return false;
			if (chunkZ != other.chunkZ)
				return false;
			if (world == null) {
				if (other.world != null)
					return false;
			} else if (!world.equals(other.world))
				return false;
			return true;
		}

		private DeferChunkWork<D, T> getOuterType() {
			return DeferChunkWork.this;
		}
	}

	@SuppressWarnings("hiding")
	private class DeferChunkLoadWork<D, T extends ChunkWork<D>> implements Runnable {
		T work;
		boolean forced;
		Chunk chunk;

		public DeferChunkLoadWork(T work, Chunk chunk, boolean forced) {
			this.work = work;
			this.forced = forced;
			this.chunk = chunk;
		}

		public void run() {
			work.doWork();
			if (forced) {
				chunk.unload();
			}
		}		
	}
}
