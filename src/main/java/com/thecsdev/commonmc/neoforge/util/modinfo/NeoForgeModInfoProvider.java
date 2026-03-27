package com.thecsdev.commonmc.neoforge.util.modinfo;

import com.thecsdev.commonmc.api.util.modinfo.ModInfo;
import com.thecsdev.commonmc.api.util.modinfo.ModInfoProvider;
import net.neoforged.fml.loading.FMLLoader;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Provides information about currently installed mods
 * on the NeoForge mod loader platform.
 */
public final class NeoForgeModInfoProvider extends ModInfoProvider
{
	// ==================================================
	public final @Override @NotNull ModInfo getModInfo(@NotNull String modid) throws NullPointerException, NoSuchElementException {
		return new NeoForgeModInfo(modid);
	}
	public final @Override @NotNull String[] getLoadedModIDs() {
		return FMLLoader.getCurrent().getLoadingModList().getMods().stream()
			.map(net.neoforged.fml.loading.moddiscovery.ModInfo::getModId)
			.toArray(String[]::new);
	}
	public final @Override boolean isModLoaded(@NotNull String modid) throws NullPointerException {
		return Arrays.asList(getLoadedModIDs()).contains(Objects.requireNonNull(modid));
	}
	// ==================================================
}
