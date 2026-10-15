package com.thecsdev.commonmc.api.client.registry;

import com.thecsdev.commonmc.TCDCommons;
import com.thecsdev.commonmc.api.client.gui.screen.hud.THudCanvasScreen;
import com.thecsdev.commonmc.api.client.gui.screen.hud.THudElement;
import com.thecsdev.commonmc.api.client.gui.screen.hud.THudElementType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import static com.mojang.serialization.Lifecycle.stable;
import static com.thecsdev.commonmc.TCDCommons.MOD_ID;
import static net.minecraft.resources.Identifier.fromNamespaceAndPath;
import static net.minecraft.resources.ResourceKey.createRegistryKey;

/**
 * {@link TCDCommons}'s client-sided registries for adding features to the mod.
 * <p>
 * <b>Important note:</b><br>
 * These {@link Registry}s are <b>NOT</b> registered in the game's <b>ROOT</b>
 * {@link BuiltInRegistries#REGISTRY}! Avoid any and all operations that involve
 * the game's <b>ROOT</b> registry!
 */
@Environment(EnvType.CLIENT)
public final class TClientRegistries
{
	// ==================================================
	private TClientRegistries() {}
	// ==================================================
	/**
	 * {@link Screen}s that are to be rendered on the game's HUD (heads-up display).
	 */
	public static final Registry<Screen> HUD_SCREEN;

	/**
	 * {@link Registry} for {@link THudElementType}s.<br>
	 * Registered entries are the types of {@link THudElement}s that may be
	 * pinned onto a {@link THudCanvasScreen}.
	 * <p>
	 * The {@link Identifier}s in this registry end up in the serialized data of
	 * every corresponding {@link THudElement}. For that reason, once published,
	 * an {@link Identifier} and the data it represents must never change.
	 */
	@ApiStatus.Experimental
	public static final Registry<THudElementType<?>> HUD_ELEMENT_TYPE;
	// ==================================================
	public static final void bootstrap() { /*invokes <clinit>*/ }
	static {
		//create registry instances
		HUD_SCREEN       = new MappedRegistry<>(createRegistryKey(id("hud_screen")), stable());
		HUD_ELEMENT_TYPE = new MappedRegistry<>(createRegistryKey(id("hud_element_type")), stable());
	}
	// --------------------------------------------------
	/**
	 * Creates an {@link Identifier} instance that uses this mod's
	 * ID as the namespace.
	 * @param path The {@link Identifier#getPath()} value.
	 */
	@Contract("_ -> new")
	private static final @ApiStatus.Internal @NonNull Identifier id(@NotNull String path) {
		return fromNamespaceAndPath(MOD_ID, path);
	}
	// ==================================================
}
