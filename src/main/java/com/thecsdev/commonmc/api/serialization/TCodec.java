package com.thecsdev.commonmc.api.serialization;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * Utility {@link Codec} implementations and functions.
 */
public final class TCodec
{
	// ==================================================
	private TCodec() {}
	// ==================================================
	/**
	 * {@link Codec} for {@link java.net.URI}s.
	 */
	public static final Codec<java.net.URI> URI = Codec.STRING.flatXmap(
			uri -> {
				try { return DataResult.success(java.net.URI.create(uri)); }
				catch(Exception e) { return DataResult.error(() -> e.getClass() + ": " + e.getMessage()); }
			},
			uri -> {
				try { return DataResult.success(uri.toString()); }
				catch(Exception e) { return DataResult.error(() -> e.getClass() + ": " + e.getMessage()); }
			}
	);
	// ==================================================
	/**
	 * Returns a {@link Codec} for a {@link List} of elements, that is lenient
	 * when encoding/decoding, and simply ignores any elements that fail to
	 * encode/decode instead of returning an error for the entire {@link List}.
	 * @param codec The {@link Codec} for the list's element type.
	 * @param <A> The list's element type.
	 * @throws NullPointerException If the argument is {@code null}.
	 */
	public static <A> @NotNull Codec<List<A>> lenientListOf(
			@NotNull Codec<A> codec) throws NullPointerException {
		return new LenientListCodec<>(codec).orElse(List.of());
	}

	/**
	 * Returns a {@link Codec} for a {@link Map}, that is lenient when encoding/decoding,
	 * and simply ignores any key-value pairs that fail to encode/decode instead
	 * of returning an error for the entire {@link Map}.
	 * @param keyCodec The {@link Codec} for the map's key type.
	 * @param valueCodec The {@link Codec} for the map's value type.
	 * @param <K> The {@link Map}'s key type.
	 * @param <V> The {@link Map}'s value type.
	 * @throws NullPointerException If an argument is {@code null}.
	 */
	public static <K, V> @NotNull Codec<java.util.Map<K, V>> lenientMap(
			@NotNull Codec<K> keyCodec, @NotNull Codec<V> valueCodec) throws NullPointerException {
		return new LenientMapCodec<>(keyCodec, valueCodec).orElse(java.util.Map.of());
	}
	// ==================================================
}
