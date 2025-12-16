package com.thecsdev.common.properties;

/**
 * An {@link ObjectProperty} whose {@code T} type is {@link Boolean}.
 */
public final class BooleanProperty extends PrimitiveProperty<Boolean>
{
	// ==================================================
	public BooleanProperty() { super(false, false); }
	public BooleanProperty(Boolean value) { super(value, false); }
	// ==================================================
	/**
	 * Same as {@link #get()}, but returns a {@code boolean} instead of a {@link Boolean}.
	 */
	public final boolean getZ() { return this.get(); }
	// ==================================================
	/**
	 * Inverts the value of this {@link BooleanProperty}.
	 * @return The new (now inverted) value.
	 */
	public final boolean toggle() {
		final boolean result = !getZ();
		set(result, BooleanProperty.class);
		return result;
	}
	// ==================================================
}
