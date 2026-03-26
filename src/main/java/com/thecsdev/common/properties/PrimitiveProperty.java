package com.thecsdev.common.properties;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * A {@link NotNullProperty} implementation representing primitive types whose
 * default values can not be {@code null}.
 */
public sealed abstract class PrimitiveProperty<T> extends NotNullProperty<T>
		permits BooleanProperty, ByteProperty, CharacterProperty, DoubleProperty, FloatProperty,
		IntegerProperty, LongProperty, ShortProperty
{
	// ==================================================
	public PrimitiveProperty(@Nullable T value, @NotNull T defaultValue) throws NullPointerException {
		super(value, Objects.requireNonNull(defaultValue));
	}
	// ==================================================
}
