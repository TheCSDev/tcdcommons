package com.thecsdev.commonmc.api.util.modinfo;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Provides information about currently installed mods.
 */
public abstract class ModInfoProvider
{
	// ==================================================
	private static @Nullable ModInfoProvider INSTANCE = null;
	// ==================================================
	/**
	 * Returns the current {@link ModInfoProvider} instance,
	 * or {@code null} if none is present at the moment.
	 */
	public static final ModInfoProvider getInstance() { return INSTANCE; }

	/**
	 * Sets the current {@link ModInfoProvider} instance.
	 * @param provider The new provider to set.
	 * @throws NullPointerException When the argument is {@code null}.
	 */
	public static final void setInstance(@NotNull ModInfoProvider provider) throws NullPointerException {
		INSTANCE = Objects.requireNonNull(provider);
	}
	// ==================================================
	/**
	 * Returns the {@link ModInfo} object for a given currently
	 * installed mod, or {@code null} if no such mod is installed.
	 * @param modid The unique namespace/ID of the mod.
	 * @throws NullPointerException When the argument is {@code null}.
	 * @throws NoSuchElementException When no mod with the given ID/namespace is installed.
	 */
	public abstract @NotNull ModInfo getModInfo(@NotNull String modid) throws NullPointerException, NoSuchElementException;

	/**
	 * Returns an array of all currently installed mod IDs/namespaces.
	 */
	public abstract @NotNull String[] getLoadedModIDs();

	/**
	 * Returns whether a mod with the given ID/namespace is currently installed.
	 * @param modid The unique namespace/ID of the mod.
	 * @throws NullPointerException When the argument is {@code null}.
	 */
	public abstract boolean isModLoaded(@NotNull String modid) throws NullPointerException;
	// ==================================================
}
