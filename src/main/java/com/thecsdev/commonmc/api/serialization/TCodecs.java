package com.thecsdev.commonmc.api.serialization;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Utility {@link Codec} implementations and functions.
 */
public final class TCodecs
{
	// ==================================================
	private TCodecs() {}
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
	 */
	public static <A> @NotNull Codec<List<A>> lenientListOf(@NotNull Codec<A> codec) {
		return new LenientListCodec<>(codec).orElse(List.of());
	}
	// ==================================================
}
