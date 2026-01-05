package com.thecsdev.common.util;

import com.thecsdev.common.util.interfaces.CheckedRunnable;

import java.lang.StackWalker.Option;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * TheCSDev's common utilities.
 */
public final class TUtils
{
	// ==================================================
	private static final StackWalker              SW_RCR  = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE);
	private static final ScheduledExecutorService STSE    = Executors.newSingleThreadScheduledExecutor();
	// ==================================================
	private TUtils() {}
	// ==================================================
	/**
	 * Returns the {@link StackWalker} instance with the
	 * {@link StackWalker.Option#RETAIN_CLASS_REFERENCE} option.
	 * @apiNote Very expensive for performance. Avoid using this.
	 */
	@Deprecated
	public static final StackWalker getStackWalkerRCR() { return SW_RCR; }

	/**
	 * Returns a single-threaded scheduled {@link ExecutorService}
	 * commonly used by these APIs.
	 */
	public static final ScheduledExecutorService getScheduledExecutor() { return STSE; }
	// ==================================================
	/**
	 * Runs a {@link CheckedRunnable}, throwing a {@link RuntimeException}
	 * if said {@link CheckedRunnable} throws an {@link Exception}.
	 * @param action The action to run.
	 * @throws NullPointerException If the argument is null.
	 * @throws RuntimeException If the {@link CheckedRunnable} throws.
	 */
	public static final void uncheckedCall(CheckedRunnable action)
			throws NullPointerException, RuntimeException
	{
		try {
			action.run();
		} catch(Exception e) {
			throw new RuntimeException("Exception raised during unchecked call.", e);
		}
	}
	// ==================================================
}
