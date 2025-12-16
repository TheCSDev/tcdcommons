package com.thecsdev.commonmc.fabric;

import com.thecsdev.commonmc.api.util.modinfo.ModInfoProvider;
import com.thecsdev.commonmc.fabric.client.TCDCommonsFabricClient;
import com.thecsdev.commonmc.fabric.server.TCDCommonsFabricServer;
import com.thecsdev.commonmc.fabric.util.modinfo.FabricModInfoProvider;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.ModInitializer;

public final class TCDCommonsFabric implements ModInitializer, ClientModInitializer, DedicatedServerModInitializer
{
	// ==================================================
	public @Override void onInitializeClient() { new TCDCommonsFabricClient(); }
	public @Override void onInitializeServer() { new TCDCommonsFabricServer(); }
	// --------------------------------------------------
	public @Override void onInitialize() {
		//initialize common fabric stuff here
		ModInfoProvider.setInstance(new FabricModInfoProvider());
	}
	// ==================================================
}
