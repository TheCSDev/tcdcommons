package com.thecsdev.commonmc;

import com.thecsdev.common.util.TUtils;
import com.thecsdev.commonmc.client.TCDCommonsClient;
import com.thecsdev.commonmc.server.TCDCommonsServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Properties;

/**
 * The main {@link Class} representing this mod.
 * This is the main "common" entry-point executed by all sides
 * (client/server) and all loaders (fabric/neoforge).
 */
public class TCDCommons
{
	// ==================================================
	/**
	 * The value of this variable MUST accurately reflect the same
	 * value as 'mod.id' from 'gradle.properties'.
	 */
	public static final String MOD_ID = "tcdcommons";
	// ==================================================
	/**
	 * The primary {@link Logger} instance used by this mod.
	 * Intended for this mod's internal/personal use only.
	 */
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	/**
	 * Holds the properties of this mod, inherited from 'gradle.properties'.
	 * Automatically loaded during the initialization phase.
	 */
	private static final Properties PROPERTIES = new Properties();

	/**
	 * Holds the configuration of this mod.
	 * Automatically loaded during the initialization phase.
	 */
	private static final TCDCommonsConfig CONFIG = new TCDCommonsConfig();
	// --------------------------------------------------
	/**
	 * THE ONE and ONLY instance of this object representing this mod.
	 * Automatically assigned post-initialization.
	 */
	private static TCDCommons INSTANCE;
	// --------------------------------------------------
	private final String           modName;
	private final String           modVersion;
	// ==================================================
	protected TCDCommons()
	{
		//since sealed classes and modules are incompatible with Minecraft modding
		//environments, we use runtime instanceof checks instead
		if(!(this instanceof TCDCommonsClient) && !(this instanceof TCDCommonsServer))
			throw new IllegalStateException("Unexpected subclass " + getClass());

		//there can only ever be ONE instance of this object
		else if(INSTANCE != null)
			throw new IllegalStateException("Mod already initialized - " + MOD_ID);
		INSTANCE = this; //keep track of the instance

		//log instance initialization
		LOGGER.info("Initializing '" + MOD_ID + "' as '" + getClass().getSimpleName() + "'.");

		//load the mod properties
		try {
			PROPERTIES.load(TCDCommons.class.getResourceAsStream("/" + MOD_ID + ".properties"));
		} catch(Exception e) {
			throw new RuntimeException("Failed to load '" + MOD_ID + ".properties'", e);
		}
		this.modName    = Objects.requireNonNull(PROPERTIES.getProperty("mod.name"));
		this.modVersion = Objects.requireNonNull(PROPERTIES.getProperty("mod.version"));

		//load the mod config
		TUtils.uncheckedCall(CONFIG::loadFromFile);
	}
	// ==================================================
	/**
	 * Returns the instance of this {@link TCDCommons}.
	 */
	public static final TCDCommons getInstance() { return INSTANCE; }
	/**
	 * Returns the primary {@link TCDCommonsConfig} instance used by this mod.
	 */
	public static final TCDCommonsConfig getConfig() { return CONFIG; }
	// --------------------------------------------------
	/**
	 * Returns the name of this mod.
	 */
	public final String getModName() { return this.modName; }

	/**
	 * Returns the version of this mod, in {@link String} form.
	 */
	public final String getModVersion() { return this.modVersion; }
	// ==================================================
}
