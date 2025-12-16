package com.thecsdev.commonmc.neoforge.util.modinfo;

import com.thecsdev.commonmc.api.util.modinfo.ModInfo;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.loading.FMLLoader;
import org.jetbrains.annotations.NotNull;

import java.util.NoSuchElementException;

import static net.minecraft.network.chat.Component.literal;
import static net.minecraft.network.chat.Component.translatable;

/**
 * Represents basic information about a currently installed mod
 * on the NeoForge mod loader platform.
 */
public final class NeoForgeModInfo extends ModInfo
{
	// ==================================================
	private final @NotNull Component name;
	// ==================================================
	public NeoForgeModInfo(@NotNull String modid) throws NullPointerException, NoSuchElementException {
		super(modid);
		final var info = FMLLoader.getCurrent().getLoadingModList().getMods()
				.stream().filter(it -> it.getModId().equals(modid))
				.findFirst().orElseThrow();
		final var lang = Language.getInstance();
		this.name      = lang.has(modid) ? translatable(modid) : literal(info.getDisplayName());
	}
	// ==================================================
	public final @Override @NotNull Component getName() { return this.name; }
	// ==================================================
}
