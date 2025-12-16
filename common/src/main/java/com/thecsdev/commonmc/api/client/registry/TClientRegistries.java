package com.thecsdev.commonmc.api.client.registry;

import com.thecsdev.commonmc.TCDCommons;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@link TCDCommons}'s client-sided registries for adding features to the mod.
 * <p>
 * Note that this does not use the game's native {@link Registry} system, instead
 * relying on {@link Map}s for simplicity.
 */
public final class TClientRegistries
{
	// ==================================================
	private TClientRegistries() {}
	// ==================================================
	/**
	 * This {@link Map} contains registered {@link Screen}s that are to be rendered
	 * in the game's HUD (heads-up display).
	 */
	public static final Map<@NotNull Identifier, @NotNull Screen> HUD_SCREEN = new LinkedHashMap<>();
	// ==================================================
}
