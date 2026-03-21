package com.thecsdev.common.util;

import com.thecsdev.common.util.interfaces.CheckedRunnable;
import com.thecsdev.common.util.interfaces.CheckedSupplier;
import org.jetbrains.annotations.NotNull;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.lang.StackWalker.Option;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.HexFormat;
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

	/**
	 * Hashes a {@link String} using PBKDF2 with HMAC-SHA256.
	 * @param input The string to hash.
	 * @param salt The salt to use for hashing.
	 * @param iterations The number of iterations to use.
	 * @return The hex-encoded hash.
	 * @throws NullPointerException If an argument is {@code null}.
	 * @throws IllegalArgumentException If iterations is less than 1.
	 * @throws RuntimeException If the hashing algorithm is unavailable.
	 */
	public static final @NotNull String str2pbkdf2(
			@NotNull String input, byte @NotNull [] salt, int iterations)
			throws NullPointerException, IllegalArgumentException, RuntimeException
	{
		Objects.requireNonNull(input);
		Objects.requireNonNull(salt);
		if(iterations < 1) throw new IllegalArgumentException("Iterations must be at least 1.");
		try {
			final int    keyLength  = 256;
			final char[] inChars    = input.toCharArray();
			final var    spec       = new PBEKeySpec(inChars, salt, iterations, keyLength);
			try {
				final var skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
				return HexFormat.of().formatHex(skf.generateSecret(spec).getEncoded());
			} finally { spec.clearPassword(); }
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new RuntimeException("Failed to generate safe hash using PBKDF2");
		}
	}
	// ==================================================
}
