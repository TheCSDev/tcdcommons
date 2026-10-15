package com.thecsdev.commonmc.api.client.gui.screen.hud;

import com.mojang.serialization.MapCodec;
import com.thecsdev.commonmc.api.client.registry.TClientRegistries;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Represents a type of {@link THudElement} that may be pinned onto a
 * {@link THudCanvasScreen}.
 * <p>
 * Each {@link THudElementType} is expected to be registered in the
 * {@link TClientRegistries#HUD_ELEMENT_TYPE} registry under a unique {@link Identifier}.
 * That {@link Identifier} ends up in the serialized data of every corresponding
 * {@link THudElement}, which is what allows the data to be read back later on,
 * even if it was written by a different game session.
 *
 * @param <E> The {@link THudElement} type.
 * @see THudElement
 * @see THudCanvasScreen
 */
@ApiStatus.Experimental
@Environment(EnvType.CLIENT)
public abstract class THudElementType<E extends THudElement>
{
	// ==================================================
	/**
	 * Returns the {@link Identifier} key for this {@link THudElementType} in the
	 * {@link TClientRegistries#HUD_ELEMENT_TYPE} registry, or {@code null} if this
	 * {@link THudElementType} is not registered.
	 */
	public final @Nullable Identifier getKey() { return TClientRegistries.HUD_ELEMENT_TYPE.getKey(this); }
	// ==================================================
	/**
	 * Returns the {@link MapCodec} used for serializing and deserializing the data
	 * of {@link THudElement}s of this {@link THudElementType}.
	 * <p>
	 * The returned {@link MapCodec} is responsible for the element's <b>own</b> data only.
	 * Things like the element's anchor point are handled by the framework itself.
	 */
	public abstract @NotNull MapCodec<E> getCodec();
	// --------------------------------------------------
	/**
	 * Creates a new {@link THudElement} instance for this {@link THudElementType},
	 * with default configurations (where any apply).
	 * @throws UnsupportedOperationException If this {@link THudElementType} cannot create an instance from scratch.
	 */
	@Contract("-> new")
	public abstract @NotNull E create() throws UnsupportedOperationException;
	// --------------------------------------------------
	/**
	 * Creates a new {@link THudElement} for this {@link THudElementType}, and assigns
	 * this {@link THudElementType} to the created {@link THudElement}.
	 * @apiNote Unlike {@link #create()}, the returned {@link THudElement} is guaranteed
	 *          to be serializable. Prefer this method over {@link #create()}.
	 */
	@Contract("-> new")
	public final @NotNull E instantiate() throws NullPointerException {
		final @NotNull var element = Objects.requireNonNull(create(), "'create()' returned 'null'");
		element._assignType(this);
		return element;
	}

	/**
	 * Assigns this {@link THudElementType} to a given, already constructed
	 * {@link THudElement}, making it serializable.
	 * @param element The {@link THudElement} to adopt.
	 * @return The same {@link THudElement} instance, for convenience.
	 * @throws NullPointerException If the argument is {@code null}.
	 * @apiNote Use this when an {@link THudElement} must be constructed from
	 *          data that {@link #create()} has no way of knowing about.
	 */
	public final @NotNull E adopt(@NotNull E element) throws NullPointerException {
		Objects.requireNonNull(element, "'THudElement' must not be 'null'")._assignType(this);
		return element;
	}	// ==================================================
}
