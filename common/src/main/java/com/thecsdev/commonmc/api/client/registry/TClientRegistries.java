package com.thecsdev.commonmc.api.client.registry;

import com.thecsdev.commonmc.TCDCommons;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

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
	// ==================================================
	public static final void bootstrap() { /*invokes <clinit>*/ }
	static {
		//create registry instances
		HUD_SCREEN = new MappedRegistry<>(createRegistryKey(id("hud_screen")), stable());
	}
	// --------------------------------------------------
	private static final @ApiStatus.Internal Identifier id(@NotNull String id) {
		return fromNamespaceAndPath(MOD_ID, id);
	}
	// ==================================================
}
