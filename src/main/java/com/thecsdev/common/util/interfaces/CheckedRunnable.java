package com.thecsdev.common.util.interfaces;

/**
 * A {@link Runnable}-like interface with a {@link #run()} method
 * that is able to throw {@link Exception}s.
 */
public @FunctionalInterface interface CheckedRunnable
{
	/**
	 * Runs this operation.
	 */
	void run() throws Exception;
}
