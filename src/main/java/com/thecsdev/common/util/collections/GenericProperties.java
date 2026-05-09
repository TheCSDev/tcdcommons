package com.thecsdev.common.util.collections;

import com.thecsdev.common.util.annotations.Virtual;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serial;
import java.util.HashMap;
import java.util.Objects;

/**
 * {@link HashMap} implementation that allows for storing and retrieving values using generics.
 */
public @Virtual class GenericProperties<K> extends HashMap<K, Object>
{
	// ==================================================
	private static final @Serial long serialVersionUID = 7086758792901773483L;
	// ==================================================
	/**
	 * Retrieves the property associated with the key, casting it to the specified type.
	 * Returns {@code null} if the key is not found or if the type from the map does not
	 * match the expected type.
	 * <p>
	 * <b>Note:</b><br>
	 * Avoid using primitive-type {@link Class}es like {@code int.class}. They aren't
	 * properly supported. Use non-primitives like {@code Integer.class}.
	 *
	 * @param <V>  The desired value type.
	 * @param type The Class object representing the desired return type (type token).
	 * @param key  The key to look up.
	 * @return The value of type V, or null on mismatch/absence.
	 * @throws NullPointerException if key or type is {@code null}.
	 */
	@Deprecated //users often forgot to specify default value
	public final @Nullable <V> V getProperty(@NotNull Class<V> type, @NotNull K key) throws NullPointerException {
		return getProperty(type, key, null);
	}

	/**
	 * Retrieves the property associated with the key, casting it to the specified type.
	 * If the value is present and the correct type, it is returned. If the value is
	 * {@code null} or a type mismatch occurs, the default value is returned, and placed
	 * into the map if non-{@code null}.
	 * <p>
	 * <b>Note:</b><br>
	 * Avoid using primitive-type {@link Class}es like {@code int.class}. They aren't
	 * properly supported. Use non-primitives like {@code Integer.class}.
	 *
	 * @param <V>          The desired value type.
	 * @param type         The {@link Class} object representing the desired return type (type token).
	 * @param key          The key to look up.
	 * @param defaultValue The value to return if the key is not found or types mismatch, and to insert if non-null.
	 * @return The value of type V, the default value, or {@code null}.
	 * @throws NullPointerException if key or type is {@code null}.
	 */
	@Contract("_, _, !null -> !null; _, _, _ -> _;")
	public final @Nullable <V> V getProperty(@NotNull Class<V> type, @NotNull K key, @Nullable V defaultValue)
			throws NullPointerException
	{
		//not null requirements
		Objects.requireNonNull(key);
		Objects.requireNonNull(type);
		type = wrap(type);

		//get the property and return the proper value
		@Nullable Object val = get(key);
		return type.isInstance(val) ? type.cast(val) : defaultValue;
	}
	// --------------------------------------------------
	/**
	 * Associates the specified value with the specified key in this map.
	 * The method checks if the value's type matches the expected type V.
	 * <p>
	 * <b>Note:</b><br>
	 * Avoid using primitive-type {@link Class}es like {@code int.class}. They aren't
	 * properly supported. Use non-primitives like {@code Integer.class}.
	 *
	 * @param <V>   The type of the value.
	 * @param type  The Class object representing the expected type of the value (type token).
	 * @param key   The key with which the specified value is to be associated.
	 * @param value The value to be associated with the specified key.
	 * @return The previous value associated with key, or {@code null} if there was no mapping
	 * for key, cast to V if the types match, otherwise {@code null}.
	 * @throws NullPointerException if key or type is {@code null}.
	 */
	public final @Nullable <V> V setProperty(@NotNull Class<V> type, @NotNull K key, @Nullable V value)
			throws NullPointerException
	{
		//not null requirements
		Objects.requireNonNull(key);
		Objects.requireNonNull(type);
		type = wrap(type);

		//put and return
		@Nullable Object prevVal = put(key, value);
		return type.isInstance(prevVal) ? type.cast(prevVal) : null;
	}
	// ==================================================
	/**
	 * Turns a primitive-type {@link Class} into its corresponding non-primitive
	 * {@link Class}. For example {@code int.class} to {@code Integer.class}.
	 * @param type The {@link Class} to wrap
	 */
	@SuppressWarnings("unchecked")
	private static final <V> Class<V> wrap(Class<V> type) {
		return (Class<V>) switch (type) {
			case Object o when o == int.class     -> Integer.class;
			case Object o when o == long.class    -> Long.class;
			case Object o when o == boolean.class -> Boolean.class;
			case Object o when o == double.class  -> Double.class;
			case Object o when o == float.class   -> Float.class;
			case Object o when o == byte.class    -> Byte.class;
			case Object o when o == char.class    -> Character.class;
			case Object o when o == short.class   -> Short.class;
			case Object o when o == void.class    -> Void.class;
			default -> type;
		};
	}
	// ==================================================
}
