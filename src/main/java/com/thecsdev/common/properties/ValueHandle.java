package com.thecsdev.common.properties;

import java.util.concurrent.atomic.AtomicReference;

/**
 * {@link AtomicReference} implementation that holds an {@link ObjectProperty}'s
 * value reference.
 */
public final class ValueHandle<V> extends AtomicReference<V>
{
	// ==================================================
	public ValueHandle() { super(); }
	public ValueHandle(V initialValue) { super(initialValue); }
	// ==================================================
}
