package com.thecsdev.commonmc.api.serialization;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Similar to Mojang's {@link Codec} for {@link Map}s, but instead of returning an
 * error when a key or value fails to encode/decode, it simply ignores that
 * key-value pair and continues encoding/decoding the rest of the {@link Map}.
 * @param keyCodec The {@link Codec} for the map's key type.
 * @param elementCodec The {@link Codec} for the map's value type.
 * @param <K> The {@link Map}'s key type.
 * @param <V> The {@link Map}'s value type.
 */
@ApiStatus.Internal
record LenientMapCodec<K, V>(
		@NotNull Codec<K> keyCodec,
		@NotNull Codec<V> elementCodec
) implements Codec<Map<K, V>>
{
	// ==================================================
	public LenientMapCodec {
		Objects.requireNonNull(keyCodec, "'Codec' for 'Map' keys must not be 'null'");
		Objects.requireNonNull(elementCodec, "'Codec' for 'Map' values must not be 'null'");
	}
	// ==================================================
	public final @Override <T> DataResult<Pair<Map<K, V>, T>> decode(DynamicOps<T> ops, T input)
	{
		return ops.getMap(input).flatMap(map ->
		{
			final var result = new LinkedHashMap<K, V>(Math.max((int) map.entries().count(), 16));
			map.entries().forEach(entry ->
			{
				final var k = keyCodec.parse(ops, entry.getFirst());
				final var v = elementCodec.parse(ops, entry.getSecond());
				k.resultOrPartial().ifPresent(key -> v.resultOrPartial().ifPresent(
						value -> result.put(key, value)));
			});
			return DataResult.success(result);
		})
		.map(r -> Pair.of(r, input));
	}
	// --------------------------------------------------
	public final @Override <T> DataResult<T> encode(Map<K, V> input, DynamicOps<T> ops, T prefix)
	{
		final var builder = ops.mapBuilder();
		for(Map.Entry<K, V> entry : input.entrySet())
		{
			final var key   = keyCodec.encodeStart(ops, entry.getKey());
			final var value = elementCodec.encodeStart(ops, entry.getValue());
			if(key.result().isPresent() && value.result().isPresent())
				builder.add(key.result().get(), value.result().get());
		}
		return builder.build(prefix);
	}
	// ==================================================
}
