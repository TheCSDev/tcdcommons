package com.thecsdev.common.util.interfaces;

import java.util.function.Supplier;

/**
 * A {@link Supplier}-like interface with a {@link #get()} method
 * that is able to throw {@link Exception}s.
 */
public @FunctionalInterface interface CheckedSupplier<T>
{
	/**
	 * Gets a result.
	 * @return The result.
	 * @throws Exception If an {@link Exception} is thrown while getting the result.
	 */
	T get() throws Exception;
}
