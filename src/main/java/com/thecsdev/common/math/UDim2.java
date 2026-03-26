package com.thecsdev.common.math;

import java.util.Objects;

/**
 * Represents a two-dimensional scalable value with independent scaling for each axis.
 * <p>
 * This structure extends {@link UDim} to two dimensions, allowing values to be defined using:
 * <ul>
 *   <li>{@code x} - The horizontal component.</li>
 *   <li>{@code y} - The vertical component.</li>
 * </ul>
 * Each axis is defined using a {@link UDim}, meaning it consists of both a relative scale
 * and an absolute offset. This is useful for 2D UI layouts, positioning, or procedural
 * generation where values need to be both scalable and adjustable.
 */
public final class UDim2
{
	// ==================================================
	public static final UDim2 ZERO = new UDim2();
	// ==================================================
	/**
	 * The horizontal component of the scalable value.
	 * <p>
	 * Defines how the value changes relative to a reference width,
	 * combining a proportional {@code scale} and a fixed {@code offset}.
	 */
	public final UDim x;
	
	/**
	 * The vertical component of the scalable value.
	 * <p>
	 * Defines how the value changes relative to a reference height,
	 * combining a proportional {@code scale} and a fixed {@code offset}.
	 */
	public final UDim y;
	// ==================================================
	public UDim2() { this(UDim.ZERO, UDim.ZERO); }
	public UDim2(double xScale, double xOffset, double yScale, double yOffset) { this(new UDim(xScale, xOffset), new UDim(yScale, yOffset)); }
	public UDim2(UDim x, UDim y) throws NullPointerException
	{
		this.x = Objects.requireNonNull(x);
		this.y = Objects.requireNonNull(y);
	}
	public static final UDim2 fromScale(double xScale, double yScale) { return new UDim2(xScale, 0, yScale, 0); }
	public static final UDim2 fromOffset(double xOffset, double yOffset) { return new UDim2(0, xOffset, 0, yOffset); }
	// ==================================================
	public final @Override int hashCode() { return Objects.hash(this.x, this.y); }
	public final @Override boolean equals(Object obj)
	{
		if(obj == this) return true;
		else if(obj instanceof UDim2 other)
			return Objects.equals(this.x, other.x) && Objects.equals(this.y, other.y);
		return false;
	}
	// --------------------------------------------------
	public final @Override UDim2 clone() { return new UDim2(this.x, this.y); }
	public final @Override String toString()
	{
		return getClass().getName() +
				"[xScale=" + this.x.scale + ",xOffset=" + this.x.offset +
				",yScale=" + this.y.scale + ",yOffset=" + this.y.offset + "]";
	}
	// ==================================================
	/**
	 * Computes the final values for both components using integer reference values.
	 * <p>
	 * Each axis is calculated independently using the formula:
	 * <pre>{@code finalValue = (referenceValue * scale) + offset;}</pre>
	 * The results are rounded down to the nearest integers.
	 *
	 * @param referenceValueX The reference value for the horizontal component.
	 * @param referenceValueY The reference value for the vertical component.
	 * @return An array containing the computed integer values for {@code x} and {@code y}.
	 */
	public final int[] computeI(int referenceValueX, int referenceValueY)
	{
		return new int[] {
				this.x.computeI(referenceValueX),
				this.y.computeI(referenceValueY)
		};
	}
	
	/**
	 * Computes the final values for both components using floating-point reference values.
	 * <p>
	 * Each axis is calculated independently using the formula:
	 * <pre>{@code finalValue = (referenceValue * scale) + offset;}</pre>
	 * The results retain floating-point precision.
	 *
	 * @param referenceValueX The reference value for the horizontal component.
	 * @param referenceValueY The reference value for the vertical component.
	 * @return An array containing the computed float values for {@code x} and {@code y}.
	 */
	public final float[] computeF(float referenceValueX, float referenceValueY)
	{
		return new float[] {
				this.x.computeF(referenceValueX),
				this.y.computeF(referenceValueY)
		};
	}
	
	/**
	 * Computes the final values for both components using double reference values.
	 * <p>
	 * Each axis is calculated independently using the formula:
	 * <pre>{@code finalValue = (referenceValue * scale) + offset;}</pre>
	 * The results retain double precision.
	 *
	 * @param referenceValueX The reference value for the horizontal component.
	 * @param referenceValueY The reference value for the vertical component.
	 * @return An array containing the computed double values for {@code x} and {@code y}.
	 */
	public final double[] computeD(double referenceValueX, double referenceValueY)
	{
		return new double[] {
				this.x.computeD(referenceValueX),
				this.y.computeD(referenceValueY)
		};
	}
	// ==================================================
}