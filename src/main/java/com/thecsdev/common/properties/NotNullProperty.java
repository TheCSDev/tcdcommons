package com.thecsdev.common.properties;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

/**
 * Similar to {@link DefaultableProperty}, except the default value is never
 * allowed to be {@code null}.
 */
public sealed class NotNullProperty<T> extends ObjectProperty<T> permits PrimitiveProperty
{
	// ==================================================
	private final @NotNull T defaultValue;
	// ==================================================
	public NotNullProperty(@NotNull T defaultValue) { this(defaultValue, defaultValue); }
	public NotNullProperty(@Nullable T value, @NotNull T defaultValue)
	{
		super((value != null) ? value : Objects.requireNonNull(defaultValue));
		this.defaultValue = Objects.requireNonNull(defaultValue);
		addFilter(it -> (it != null) ? it : this.defaultValue, NotNullProperty.class);
	}
	// ==================================================
	//note: only concern is someone setting the pointer's value to null, bypassing null checks.
	//      so technically unsafe in that sense, but null checks in get() could eat performance
	@SuppressWarnings("DataFlowIssue")
	public final @Override @NotNull T get() { return super.get(); }
	public final @Override @NotNull Optional<T> getOptional() { return Optional.of(get()); }
	// --------------------------------------------------
	/**
	 * Returns the default value that is assigned when attempting to
	 * set the value to {@code null}.
	 */
	public final @NotNull T getDefaultValue() { return this.defaultValue; }
	// ==================================================
}
