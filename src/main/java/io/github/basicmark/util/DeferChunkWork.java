package io.github.basicmark.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

import org.bukkit.Chunk;
import org.bukkit.Location;

public class DeferChunkWork {
	HashMap<UnloadedChunk, DeferWorkList> pendingWork;

	public DeferChunkWork() {
		pendingWork = new HashMap<UnloadedChunk, DeferWorkList>();
	}
	
	public void addWork(Location location, Runnable task) {
		final UnloadedChunk chunk = new UnloadedChunk(location);
		DeferWorkList chunkWork;
		
		if (!pendingWork.containsKey(chunk)) {
			chunkWork = new DeferWorkList();
			pendingWork.put(chunk, chunkWork);
		} else {
			chunkWork = pendingWork.get(chunk);
		}
		
		chunkWork.add(location, task);
	}
	
	public void chunkLoad(Chunk chunk) {
		final UnloadedChunk loadedChunk = new UnloadedChunk(chunk);

		if (pendingWork.containsKey(loadedChunk)) {
			DeferWorkList work = pendingWork.get(loadedChunk);

			work.doWork();
			pendingWork.remove(loadedChunk);
		}
	}
	
	private class DeferWorkList {
		HashMap<ChunkOffset, Runnable> tasks;
		
		DeferWorkList() {
			tasks = new HashMap<ChunkOffset,Runnable>();
		}
		
		void add(Location location, Runnable task) {
			final ChunkOffset offset = new ChunkOffset(location);
			tasks.put(offset, task);
		}
		
		void doWork() {
			Iterator<Runnable> i = tasks.values().iterator();

			while (i.hasNext()) {
				Runnable task = i.next();

				task.run();
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

		private DeferChunkWork getOuterType() {
			return DeferChunkWork.this;
		}
	}
	
	private class ChunkOffset {
		int XOffset;
		int YOffset;
		int ZOffset;
		
		ChunkOffset(Location location) {
			this.XOffset = location.getBlockX() & 0xf;
			this.YOffset = location.getBlockY();
			this.ZOffset = location.getBlockZ() & 0xf;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + XOffset;
			result = prime * result + YOffset;
			result = prime * result + ZOffset;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ChunkOffset other = (ChunkOffset) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (XOffset != other.XOffset)
				return false;
			if (YOffset != other.YOffset)
				return false;
			if (ZOffset != other.ZOffset)
				return false;
			return true;
		}

		private DeferChunkWork getOuterType() {
			return DeferChunkWork.this;
		}
	}
}
