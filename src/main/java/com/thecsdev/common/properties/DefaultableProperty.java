package com.thecsdev.common.properties;

import com.thecsdev.common.util.annotations.Virtual;
import org.jetbrains.annotations.Nullable;

/**
 * An {@link ObjectProperty} whose value defaults to {@link #getDefaultValue()}
 * when attempting to {@link #set(Object)} it to {@code null}.
 */
public @Virtual class DefaultableProperty<T> extends ObjectProperty<T>
{
	// ==================================================
	private final @Nullable T defaultValue;
	// ==================================================
	public DefaultableProperty(@Nullable T defaultValue) { this(defaultValue, defaultValue); }
	public DefaultableProperty(@Nullable T value, @Nullable T defaultValue)
	{
		super((value != null) ? value : defaultValue);
		this.defaultValue = defaultValue;
		addFilter(it -> (it != null) ? it : this.defaultValue, DefaultableProperty.class);
	}
	// ==================================================
	/**
	 * Returns the default value that is assigned when attempting to
	 * set the value to {@code null}.
	 */
	public final @Nullable T getDefaultValue() { return this.defaultValue; }
	// ==================================================
}
