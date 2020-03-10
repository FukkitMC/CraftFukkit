package com.github.fukkitmc.fukkit.access;

/**
 * for nms objects that hold a CraftBukkit object with them
 *
 * @param <T>
 */
public interface CraftHandled<T> {
	T getBukkit();

	void setBukkit(T obj);
}
