package com.thecsdev.commonmc.fabric.util.modinfo;

import com.thecsdev.commonmc.api.util.modinfo.ModInfo;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.NoSuchElementException;

import static net.minecraft.network.chat.Component.literal;
import static net.minecraft.network.chat.Component.translatable;

/**
 * Represents basic information about a currently installed mod
 * on the Fabric mod loader platform.
 */
public final class FabricModInfo extends ModInfo
{
	// ==================================================
	private final @NotNull Component name;
	// ==================================================
	public FabricModInfo(@NotNull String modid) throws NullPointerException, NoSuchElementException {
		super(modid);
		final var info = FabricLoader.getInstance().getModContainer(modid).orElseThrow();
		final var meta = info.getMetadata();
		final var lang = Language.getInstance();
		this.name      = lang.has(modid) ? translatable(modid) : literal(meta.getName());
	}
	// ==================================================
	public final @Override @NotNull Component getName() { return this.name; }
	// ==================================================
}
