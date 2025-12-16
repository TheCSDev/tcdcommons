package com.thecsdev.commonmc;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.thecsdev.commonmc.api.config.ModConfig;

import java.util.Objects;

/**
 * {@link TCDCommons}'s primary {@link ModConfig}.
 */
public final class TCDCommonsConfig extends ModConfig
{
	// ==================================================
	public static boolean FLAG_DEV_ENV = false;
	// --------------------------------------------------
	private @Expose @SerializedName("client-updateItemGroupsOnJoin") boolean updateItemGroupsOnJoin = true;
	// ==================================================
	public TCDCommonsConfig() { super(TCDCommons.MOD_ID); }
	static {
		FLAG_DEV_ENV = Objects.equals(System.getProperty("fabric.development"), "true");
		if(FLAG_DEV_ENV) TCDCommons.LOGGER.info("Running in development environment...");
	}
	// ==================================================
	/**
	 * Whether to update item groups (creative mode tabs) when joining a world.
	 * This optimization feature aims to move the lag spike that occurs when
	 * opening an inventory screen - to the loading screen.
	 */
	public final boolean updateItemGroupsOnJoin() { return updateItemGroupsOnJoin; }

	/**
	 * Sets the value of {@link #updateItemGroupsOnJoin()}.
	 * @param value The new value.
	 */
	public final void setUpdateItemGroupsOnJoin(boolean value) { this.updateItemGroupsOnJoin = value; }
	// ==================================================
}
