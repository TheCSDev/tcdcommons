package com.thecsdev.commonmc.client.mixin.events;

import com.thecsdev.commonmc.api.client.events.ClientEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public abstract class MixinClientLevel
{
	// ==================================================
	@SuppressWarnings("rawtypes")
	@Inject(method = "<init>", at = @At("RETURN"))
	private void onInit(
			ClientPacketListener connection,
			ClientLevel.ClientLevelData levelData,
			ResourceKey dimension,
			Holder dimensionType,
			int serverChunkRadius,
			int serverSimulationDistance,
			LevelExtractor levelExtractor,
			boolean isDebug,
			long biomeZoomSeed,
			int seaLevel,
			CallbackInfo ci)
	{
		Minecraft.getInstance().execute(() ->
				ClientEvent.LEVEL_INIT.invoker().invoke((ClientLevel)(Object)this));
	}
	// ==================================================
}
