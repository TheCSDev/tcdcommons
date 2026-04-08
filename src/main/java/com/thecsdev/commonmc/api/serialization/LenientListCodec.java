package com.thecsdev.commonmc.api.serialization;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.ListCodec;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

//TODO - Implement 'List<E>' size constraints.

/**
 * Similar to Mojang's {@link ListCodec}, but instead of returning an error when an
 * element fails to encode/decode, it simply ignores that element and continues
 * encoding/decoding the rest of the list.
 * @param elementCodec The {@link Codec} for the list's element type.
 * @param <E> The list's element type.
 * @see ListCodec
 */
@ApiStatus.Internal
record LenientListCodec<E>(@NotNull Codec<E> elementCodec) implements Codec<List<E>>
{
	// ==================================================
	public LenientListCodec {
		Objects.requireNonNull(elementCodec, "'Codec' for 'List' elements must not be 'null'");
	}
	// ==================================================
	public final @Override @NonNull String toString() {
		return getClass().getSimpleName() + "[" + elementCodec + "]";
	}
	// ==================================================
	public final @Override <T> DataResult<T> encode(List<E> input, DynamicOps<T> ops, T prefix)
	{
		//encoding
		final ListBuilder<T> builder = ops.listBuilder();
		for(final E element : input)
			elementCodec.encodeStart(ops, element).result().ifPresent(builder::add);
		return builder.build(prefix);
	}
	// --------------------------------------------------
	public final @Override <T> DataResult<Pair<List<E>, T>> decode(DynamicOps<T> ops, T input)
	{
		return ops.getList(input).setLifecycle(Lifecycle.stable()).map(stream ->
		{
			//decoding
			final List<E> output = new ArrayList<>();
			stream.accept(value -> elementCodec.decode(ops, value)
					.result()
					.ifPresent(pair -> output.add(pair.getFirst())));

			//constructing the final map
			return Pair.of(Collections.unmodifiableList(output), ops.empty());
		});
	}
	// ==================================================
}
