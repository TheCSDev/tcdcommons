package com.thecsdev.commonmc.neoforge;

import com.thecsdev.commonmc.TCDCommons;
import com.thecsdev.commonmc.api.util.modinfo.ModInfoProvider;
import com.thecsdev.commonmc.neoforge.client.TCDCommonsNeoClient;
import com.thecsdev.commonmc.neoforge.server.TCDCommonsNeoServer;
import com.thecsdev.commonmc.neoforge.util.modinfo.NeoForgeModInfoProvider;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(TCDCommons.MOD_ID)
public final class TCDCommonsNeoForge
{
	// ==================================================
	public TCDCommonsNeoForge()
	{
		//initialize common neoforge stuff
		onInitialize();
		//create an instance of the mod's main class, depending on the dist
		switch(FMLEnvironment.getDist())
		{
			case CLIENT           -> new TCDCommonsNeoClient();
			case DEDICATED_SERVER -> new TCDCommonsNeoServer();
		}
	}
	// --------------------------------------------------
	private final void onInitialize() {
		ModInfoProvider.setInstance(new NeoForgeModInfoProvider());
	}
	// ==================================================
}
