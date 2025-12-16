package com.thecsdev.commonmc.api.util.modinfo;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Represents basic information about a currently installed mod.
 */
public abstract class ModInfo
{
	// ==================================================
	private final @NotNull String modid;
	// ==================================================
	public ModInfo(@NotNull String modid) throws NullPointerException, NoSuchElementException {
		this.modid = Objects.requireNonNull(modid);
	}
	// ==================================================
	/**
	 * Returns the ID/namespace of this mod.
	 * @see Identifier#getNamespace()
	 */
	public @NotNull String getID() { return this.modid; }
	// ==================================================
	/**
	 * Returns the display name of this mod. If the mod does not have a display
	 * name, returns the mod's {@link #getID()} as a text {@link Component}.
	 */
	public abstract @NotNull Component getName();
	// ==================================================
}
