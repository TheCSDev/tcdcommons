package com.thecsdev.common.properties;

import org.jetbrains.annotations.Nullable;

import com.thecsdev.common.util.annotations.Virtual;

/**
 * An {@link ObjectProperty} whose {@code T} type is {@link String}.
 */
public @Virtual class StringProperty extends ObjectProperty<String>
{
	// ==================================================
	public StringProperty() { super(); }
	public StringProperty(@Nullable String value) { super(value); }
	// ==================================================
}