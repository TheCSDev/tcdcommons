package com.thecsdev.common.properties;

/**
 * An {@link ObjectProperty} whose {@code T} type is {@link Float}.
 */
public final class FloatProperty extends PrimitiveProperty<Float>
{
	// ==================================================
	public FloatProperty() { super(0f, 0f); }
	public FloatProperty(Float value) { super((value != null) ? value : 0, 0f); }
	// ==================================================
	/**
	 * Same as {@link #get()}, but returns a {@code byte} instead of an {@link Integer}.
	 */
	public final byte getB() { return this.get().byteValue(); }

	/**
	 * Same as {@link #get()}, but returns a {@code short} instead of an {@link Integer}.
	 */
	public final short getS() { return this.get().shortValue(); }

	/**
	 * Same as {@link #get()}, but returns an {@code int} instead of an {@link Integer}.
	 */
	public final int getI() { return this.get().intValue(); }

	/**
	 * Same as {@link #get()}, but returns a {@code long} instead of an {@link Integer}.
	 */
	public final long getL() { return this.get().longValue(); }
	// --------------------------------------------------
	/**
	 * Same as {@link #get()}, but returns a {@code float} instead of an {@link Integer}.
	 */
	public final float getF() { return this.get(); }

	/**
	 * Same as {@link #get()}, but returns a {@code double} instead of an {@link Integer}.
	 */
	public final double getD() { return this.get(); }
	// ==================================================
}