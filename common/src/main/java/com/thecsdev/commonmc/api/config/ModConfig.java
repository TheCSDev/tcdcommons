package com.thecsdev.commonmc.api.config;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.thecsdev.common.config.JsonConfig;
import com.thecsdev.common.util.annotations.Virtual;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.Locale;

/**
 * {@link JsonConfig} implementation for Minecraft mods.
 * @see Expose
 * @see SerializedName
 */
public @Virtual class ModConfig extends JsonConfig
{
	// ==================================================
	public ModConfig(@NotNull String fileName) throws NullPointerException
	{
		//prepare the file-name argument
		fileName = fileName.toLowerCase(Locale.ENGLISH);
		if(!fileName.endsWith(".json")) fileName += ".json";

		//set config file
		setConfigFile(Path.of(System.getProperty("user.dir"), "config", fileName).toFile());
	}
	public ModConfig(@Nullable File configFile) { super(configFile); }
	// ==================================================
}
