package com.thecsdev.common.math;

import java.util.Objects;

/**
 * Represents a three-dimensional scalable value with independent scaling for each axis.
 * <p>
 * This structure extends {@link UDim} to three dimensions, allowing values to be defined using:
 * <ul>
 *   <li>{@code x} - The horizontal component.</li>
 *   <li>{@code y} - The vertical component.</li>
 *   <li>{@code z} - The depth or forward component.</li>
 * </ul>
 * Each axis is defined using a {@link UDim}, meaning it consists of both a relative scale
 * and an absolute offset. This is useful for 3D UI layouts, positioning, or procedural
 * generation where values need to be both scalable and adjustable.
 */
public final class UDim3
{
	// ==================================================
	public static final UDim3 ZERO = new UDim3();
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
	
	/**
	 * The depth or forward component of the scalable value.
	 * <p>
	 * Defines how the value changes relative to a reference depth,
	 * combining a proportional {@code scale} and a fixed {@code offset}.
	 */
	public final UDim z;
	// ==================================================
	public UDim3() { this(UDim.ZERO, UDim.ZERO, UDim.ZERO); }
	public UDim3(double xScale, double xOffset, double yScale, double yOffset, double zScale, double zOffset) { this(new UDim(xScale, xOffset), new UDim(yScale, yOffset), new UDim(zScale, zOffset)); }
	public UDim3(UDim x, UDim y, UDim z) throws NullPointerException
	{
		this.x = Objects.requireNonNull(x);
		this.y = Objects.requireNonNull(y);
		this.z = Objects.requireNonNull(z);
	}
	public static final UDim3 fromScale(double xScale, double yScale, double zScale) { return new UDim3(xScale, 0, yScale, 0, zScale, 0); }
	public static final UDim3 fromOffset(double xOffset, double yOffset, double zOffset) { return new UDim3(0, xOffset, 0, yOffset, 0, zOffset); }
	// ==================================================
	public final @Override int hashCode() { return Objects.hash(this.x, this.y, this.z); }
	public final @Override boolean equals(Object obj)
	{
		if(obj == this) return true;
		else if(obj instanceof UDim3 other)
			return Objects.equals(this.x, other.x) &&
					Objects.equals(this.y, other.y) &&
					Objects.equals(this.z, other.z);
		return false;
	}
	// --------------------------------------------------
	public final @Override UDim3 clone() { return new UDim3(this.x, this.y, this.z); }
	public final @Override String toString()
	{
		return getClass().getName() +
				"[xScale=" + this.x.scale + ",xOffset=" + this.x.offset +
				",yScale=" + this.y.scale + ",yOffset=" + this.y.offset +
				",zScale=" + this.z.scale + ",zOffset=" + this.z.offset + "]";
	}
	// ==================================================
	/**
	 * Computes the final values for all three components using integer reference values.
	 * <p>
	 * Each axis is calculated independently using the formula:
	 * <pre>{@code finalValue = (referenceValue * scale) + offset;}</pre>
	 * The results are rounded down to the nearest integers.
	 *
	 * @param referenceValueX The reference value for the horizontal component.
	 * @param referenceValueY The reference value for the vertical component.
	 * @param referenceValueZ The reference value for the depth component.
	 * @return An array containing the computed integer values for {@code x}, {@code y}, and {@code z}.
	 */
	public final int[] computeI(int referenceValueX, int referenceValueY, int referenceValueZ)
	{
		return new int[] {
				this.x.computeI(referenceValueX),
				this.y.computeI(referenceValueY),
				this.z.computeI(referenceValueZ)
		};
	}
	
	/**
	 * Computes the final values for all three components using floating-point reference values.
	 * <p>
	 * Each axis is calculated independently using the formula:
	 * <pre>{@code finalValue = (referenceValue * scale) + offset;}</pre>
	 * The results retain floating-point precision.
	 *
	 * @param referenceValueX The reference value for the horizontal component.
	 * @param referenceValueY The reference value for the vertical component.
	 * @param referenceValueZ The reference value for the depth component.
	 * @return An array containing the computed float values for {@code x}, {@code y}, and {@code z}.
	 */
	public final float[] computeF(float referenceValueX, float referenceValueY, float referenceValueZ)
	{
		return new float[] {
				this.x.computeF(referenceValueX),
				this.y.computeF(referenceValueY),
				this.z.computeF(referenceValueZ)
		};
	}
	
	/**
	 * Computes the final values for all three components using double reference values.
	 * <p>
	 * Each axis is calculated independently using the formula:
	 * <pre>{@code finalValue = (referenceValue * scale) + offset;}</pre>
	 * The results retain double precision.
	 *
	 * @param referenceValueX The reference value for the horizontal component.
	 * @param referenceValueY The reference value for the vertical component.
	 * @param referenceValueZ The reference value for the depth component.
	 * @return An array containing the computed double values for {@code x}, {@code y}, and {@code z}.
	 */
	public final double[] computeD(double referenceValueX, double referenceValueY, double referenceValueZ)
	{
		return new double[] {
				this.x.computeD(referenceValueX),
				this.y.computeD(referenceValueY),
				this.z.computeD(referenceValueZ)
		};
	}
	// ==================================================
}