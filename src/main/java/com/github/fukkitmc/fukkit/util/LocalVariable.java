package com.github.fukkitmc.fukkit.util;

import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Logger;

public class LocalVariable<T> {
	private static final Logger LOGGER = Logger.getLogger("LocalVariable");
	private final ThreadLocal<Queue<T>> type = ThreadLocal.withInitial(LinkedList::new);
	private final int threshold;

	public LocalVariable() {this(20);}

	public LocalVariable(int threshold) {this.threshold = threshold;}

	public T get() {
		return this.type.get().peek();
	}

	public T pop() {
		return this.type.get().poll();
	}

	public void push(T val) {
		Queue<T> queue = this.type.get();
		if (queue.size() > this.threshold) {
			LOGGER.warning("Potential Memory Leak in " + new Throwable().getStackTrace()[1].getClassName());
		}
		queue.add(val);
	}
}
