package com.thecsdev.common.math;

/**
 * TheCSDev's mathematics-related utilities.
 */

public final class TMath
{
	// ==================================================
	private TMath() {}
	// ==================================================
	/**
	 * Clamps a {@code int} value to the given {@code min/max} range.
	 * @param value the value to clamp
	 * @param min The minimum value.
	 * @param max The maximum value.
	 * @return {@code min} if {@code value < min}, {@code max} if {@code value > max}, otherwise {@code value}.
	 */
	@Deprecated(forRemoval = true)
	public static final int clampi(int value, int min, int max) { return Math.max(min, Math.min(max, value)); }

	/**
	 * Clamps a {@code float} value to the given {@code min/max} range.
	 * @param value the value to clamp
	 * @param min The minimum value.
	 * @param max The maximum value.
	 * @return {@code min} if {@code value < min}, {@code max} if {@code value > max}, otherwise {@code value}.
	 */
	@Deprecated(forRemoval = true)
	public static final float clampf(float value, float min, float max) { return Math.max(min, Math.min(max, value)); }

	/**
	 * Clamps a {@code double} value to the given {@code min/max} range.
	 * @param value the value to clamp
	 * @param min The minimum value.
	 * @param max The maximum value.
	 * @return {@code min} if {@code value < min}, {@code max} if {@code value > max}, otherwise {@code value}.
	 */
	@Deprecated(forRemoval = true)
	public static final double clampd(double value, double min, double max) { return Math.max(min, Math.min(max, value)); }
	// --------------------------------------------------
	/**
	 * Clamps a {@code float} value to the range [0.0f, 1.0f].
	 * @param value the value to clamp
	 * @return {@code 0.0f} if {@code value < 0.0f}, {@code 1.0f} if {@code value > 1.0f}, otherwise {@code value}.
	 */
	public static final double clamp01f(float value) { return Math.max(0.0f, Math.min(1.0f, value)); }

	/**
	 * Clamps a {@code double} value to the range [0.0, 1.0].
	 * @param value the value to clamp
	 * @return {@code 0.0} if {@code value < 0.0}, {@code 1.0} if {@code value > 1.0}, otherwise {@code value}.
	 */
	public static final double clamp01d(double value) { return Math.max(0.0d, Math.min(1.0d, value)); }
	// ==================================================
}
