package com.thecsdev.commonmc.client.mixin.events;

import com.thecsdev.commonmc.api.client.events.ClientEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.LevelRenderer;
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
			ClientPacketListener clientPacketListener,
			ClientLevel.ClientLevelData clientLevelData,
			ResourceKey resourceKey,
			Holder holder,
			int i, int j,
			LevelRenderer levelRenderer,
			boolean bl, long l, int k,
			CallbackInfo ci)
	{
		Minecraft.getInstance().execute(() ->
				ClientEvent.LEVEL_INIT.invoker().invoke((ClientLevel)(Object)this));
	}
	// ==================================================
}
