package com.thecsdev.commonmc.fabric.util.modinfo;

import com.thecsdev.commonmc.api.util.modinfo.ModInfo;
import com.thecsdev.commonmc.api.util.modinfo.ModInfoProvider;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;

import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Provides information about currently installed mods
 * on the Fabric mod loader platform.
 */
public final class FabricModInfoProvider extends ModInfoProvider
{
	// ==================================================
	public final @Override @NotNull ModInfo getModInfo(@NotNull String modid) throws NullPointerException, NoSuchElementException {
		return new FabricModInfo(modid);
	}
	public final @Override @NotNull String[] getLoadedModIDs() {
		return FabricLoader.getInstance().getAllMods().stream()
				.map(mod -> mod.getMetadata().getId())
				.toArray(String[]::new);
	}
	public final @Override boolean isModLoaded(@NotNull String modid) throws NullPointerException {
		return FabricLoader.getInstance().isModLoaded(Objects.requireNonNull(modid));
	}
	// ==================================================
}
