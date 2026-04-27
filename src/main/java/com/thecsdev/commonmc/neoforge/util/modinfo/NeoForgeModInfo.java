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
	private final @NotNull String    version;
	// ==================================================
	public NeoForgeModInfo(@NotNull String modid) throws NullPointerException, NoSuchElementException
	{
		//initialize super
		super(modid);
		//preparations
		final var info        = getFMLModInfo(modid);
		final var lang        = Language.getInstance();
		final var modMenuName = "modmenu.nameTranslation." + modid;
		//initialize field values
		this.name = lang.has(modMenuName) ?
				translatable(modMenuName) :
				lang.has(modid) ?
						translatable(modid) :
						literal(info.getDisplayName());
		this.version   = info.getVersion().toString();
	}
	// ==================================================
	public final @Override @NotNull Component getName() { return this.name; }
	public final @Override @NotNull String getVersion() { return this.version; }
	// ==================================================
	private static final @NotNull net.neoforged.fml.loading.moddiscovery.ModInfo getFMLModInfo(
			@NotNull String modid) throws NullPointerException, NoSuchElementException {
		return FMLLoader.getCurrent().getLoadingModList().getMods()
				.stream().filter(it -> it.getModId().equals(modid))
				.findFirst().orElseThrow();
	}
	// ==================================================
}
