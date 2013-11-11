package io.github.basicmark.util;

public interface ChunkWork<D> {
	void add(D data, Runnable task);
	
	void doWork();
}
