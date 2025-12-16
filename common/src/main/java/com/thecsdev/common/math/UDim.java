package com.thecsdev.common.math;

import java.util.Objects;

/**
 * Represents a scalable dimension with both a proportional and absolute component.
 * <p>
 * This structure allows UI elements to be sized or positioned using a combination of:
 * <ul>
 *   <li>A relative {@code scale} factor (as a fraction of a reference size).</li>
 *   <li>An absolute {@code offset} value (fixed pixel units).</li>
 * </ul>
 * The final computed value is determined by:
 * <pre>{@code finalValue = (referenceValue * scale) + offset;}</pre>
 * This is useful for UI layouts that need to be responsive while maintaining fixed adjustments.
 */
public final class UDim
{
	// ==================================================
	public static final UDim ZERO = new UDim();
	// ==================================================
	/**
	 * The proportional scale factor (relative to a reference size).
	 * <p>
	 * A value of {@code 1.0} means the result is equal to the reference size,
	 * while {@code 0.5} makes it half of the reference size.
	 * Negative values are valid and will scale in the opposite direction.
	 */
	public final double scale;
	
	/**
	 * The absolute offset (in pixels) added to the scaled value.
	 * <p>
	 * This provides a fixed adjustment regardless of the reference size.
	 * A positive value shifts the result forward, while a negative value shifts it backward.
	 */
	public final double offset;
	// ==================================================
	public UDim() { this(0, 0); }
	public UDim(double scale, double offset)
	{
		this.scale  = scale;
		this.offset = offset;
	}
	// ==================================================
	public final @Override int hashCode() { return Objects.hash(this.scale, this.offset); }
	public final @Override boolean equals(Object obj)
	{
		if(obj == this) return true;
		else if(obj instanceof UDim other)
			return (this.scale == other.scale) && (this.offset == other.offset);
		return false;
	}
	// --------------------------------------------------
	public final @Override UDim clone() { return new UDim(this.scale, this.offset); }
	public final @Override String toString() {
		return getClass().getName() + "[scale=" + this.scale + ",offset=" + this.offset + "]";
	}
	// ==================================================
	/**
	 * Computes the final value using an integer reference value.
	 * <p>
	 * The formula used is:
	 * <pre>{@code finalValue = (referenceValue * scale) + offset;}</pre>
	 * The result is rounded down to the nearest integer.
	 *
	 * @param referenceValue The reference size to apply scaling to.
	 * @return The computed final value as an integer.
	 */
	public final int computeI(int referenceValue) { return (int) ((referenceValue * this.scale) + this.offset); }
	
	/**
	 * Computes the final value using a floating-point reference value.
	 * <p>
	 * The formula used is:
	 * <pre>{@code finalValue = (referenceValue * scale) + offset;}</pre>
	 * The result retains floating-point precision.
	 *
	 * @param referenceValue The reference size to apply scaling to.
	 * @return The computed final value as a float.
	 */
	public final float computeF(float referenceValue) { return (float) ((referenceValue * this.scale) + this.offset); }
	
	/**
	 * Computes the final value using a double reference value.
	 * <p>
	 * The formula used is:
	 * <pre>{@code finalValue = (referenceValue * scale) + offset;}</pre>
	 * The result retains double precision.
	 *
	 * @param referenceValue The reference size to apply scaling to.
	 * @return The computed final value as a float.
	 */
	public final double computeD(double referenceValue) { return (referenceValue * this.scale) + this.offset; }
	// ==================================================
}