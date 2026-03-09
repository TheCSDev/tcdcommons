package com.thecsdev.commonmc.api.util.modinfo;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.Objects;

import static com.thecsdev.common.util.TUtils.str2sha256base36;

/**
 * Provides information about currently installed mods.
 */
public abstract class ModInfoProvider
{
	// ==================================================
	private static @Nullable ModInfoProvider INSTANCE = null;
	// --------------------------------------------------
	private @Nullable String modpackId;
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
	/**
	 * Returns a unique ID for the current modpack installation, which is generated
	 * by hashing the list of currently installed mods and their versions.
	 */
	public final @NotNull String getModpackID()
	{
		//return the value if already computed
		if(this.modpackId != null) return this.modpackId;

		//otherwise compute value
		final var plaintext = new StringBuilder();
		Arrays.stream(getLoadedModIDs())
				.map(this::getModInfo)
				.sorted(Comparator.comparing(ModInfo::getID))
				.forEach(modInfo -> plaintext
						.append(modInfo.getID()).append("+")
						.append(modInfo.getVersion()).append("\n"));
		return (this.modpackId = str2sha256base36(plaintext.toString()));
	}
	// ==================================================
}
