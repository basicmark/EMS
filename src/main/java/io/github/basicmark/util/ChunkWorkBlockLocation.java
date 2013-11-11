package io.github.basicmark.util;

import java.util.HashMap;
import java.util.Iterator;

import org.bukkit.Location;

public class ChunkWorkBlockLocation implements ChunkWork<Location> {
	HashMap<ChunkOffset, Runnable> tasks;

	ChunkWorkBlockLocation() {
		tasks = new HashMap<ChunkOffset,Runnable>();
	}

	public void add(Location location, Runnable task) {
		final ChunkOffset offset = new ChunkOffset(location);
		tasks.put(offset, task);
	}

	public void doWork() {
		Iterator<Runnable> i = tasks.values().iterator();

		while (i.hasNext()) {
			Runnable task = i.next();

			task.run();
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

		private ChunkWorkBlockLocation getOuterType() {
			return ChunkWorkBlockLocation.this;
		}

	}
}
