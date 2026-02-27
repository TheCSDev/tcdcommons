package com.thecsdev.common.util;

import com.thecsdev.common.util.interfaces.CheckedRunnable;
import com.thecsdev.common.util.interfaces.CheckedSupplier;
import org.jetbrains.annotations.NotNull;

import java.lang.StackWalker.Option;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * TheCSDev's common utilities.
 */
public final class TUtils
{
	// ==================================================
	private static final StackWalker              SW_RCR = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE);
	private static final ScheduledExecutorService STSE   = Executors.newSingleThreadScheduledExecutor(task -> {
		final var thread = new Thread(task);
		thread.setName("com.thecsdev.common:scheduled_executor");
		thread.setDaemon(true);
		return thread;
	});
	private static final ExecutorService          VTPTE  = Executors.newVirtualThreadPerTaskExecutor();
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
	 * Primary {@link Executors#newSingleThreadScheduledExecutor()} instance commonly used
	 * by this mod.
	 * <p>
	 * The scheduled executor's {@link Thread} is a daemon thread.
	 * @see Thread#isDaemon()
	 */
	public static final ScheduledExecutorService getSingleThreadScheduledExecutor() { return STSE; }

	/**
	 * Primary {@link Executors#newVirtualThreadPerTaskExecutor()} instance commonly used
	 * by this mod.
	 */
	public static final ExecutorService getVirtualThreadPerTaskExecutor() { return VTPTE; }
	// ==================================================
	/**
	 * Runs a {@link CheckedRunnable}, throwing a {@link RuntimeException}
	 * if said {@link CheckedRunnable} throws an {@link Exception}.
	 * @param action The action to run.
	 * @throws NullPointerException If the argument is null.
	 * @throws RuntimeException If the {@link CheckedRunnable} throws.
	 */
	public static final void uncheckedCall(@NotNull CheckedRunnable action)
			throws NullPointerException, RuntimeException
	{
		try {
			action.run();
		} catch(Exception e) {
			throw new RuntimeException("Exception raised during unchecked call.", e);
		}
	}

	/**
	 * Gets a value from a {@link CheckedSupplier}, throwing a {@link RuntimeException}
	 * if said {@link CheckedSupplier} throws an {@link Exception}.
	 * @param supplier The supplier to get the value from.
	 * @return The value supplied by the supplier.
	 * @throws NullPointerException If the argument is null.
	 * @throws RuntimeException If the {@link CheckedSupplier} throws.
	 */
	public static final <T> T uncheckedSupply(@NotNull CheckedSupplier<T> supplier)
			throws NullPointerException, RuntimeException
	{
		try {
			return supplier.get();
		} catch(Exception e) {
			throw new RuntimeException("Exception raised during unchecked supply.", e);
		}
	}
	// ==================================================
	/**
	 * Hashes the given input string using SHA-256 and encodes the hash as a
	 * base36 {@link String}.
	 * @param input The input string to hash.
	 * @throws NullPointerException If the argument is {@code null}.
	 * @throws RuntimeException If the SHA-256 algorithm is not available.
	 */
	public static final @NotNull String str2sha256base36(@NotNull String input)
			throws NullPointerException, RuntimeException
	{
		Objects.requireNonNull(input);
		try {
			final var digest = MessageDigest.getInstance("SHA-256");
			final var hash   = digest.digest(input.getBytes(StandardCharsets.UTF_8));
			final var result = new BigInteger(1, hash).toString(36);
			return "0".repeat(50 - result.length()) + result; //account for leading zeros in the number
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Failed to hash a String using SHA-256");
		}
	}
	// ==================================================
}
