package com.thecsdev.commonmc.client.mixin.events;

import com.thecsdev.commonmc.api.client.gui.screen.IStatsListener;
import com.thecsdev.commonmc.api.client.gui.screen.TScreenWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAwardStatsPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientboundAwardStatsPacket.class)
public abstract class MixinClientboundAwardStatsPacket
{
	// ==================================================
	@Inject(method = "handle*", at = @At("RETURN"), require = 1, remap = true)
	private void onHandle(ClientGamePacketListener clientPlayPacketListener, CallbackInfo callbackInfo)
	{
		//execute handlers for this packet, but on the client's thread
		final var client = Minecraft.getInstance();
		client.execute(() ->
		{
			//invoke callback method for screens
			if(client.screen instanceof IStatsListener isl)
				isl.statsReceivedCallback();
			//or invoke callback method for t-screens
			else if(client.screen instanceof TScreenWrapper<?> tsw &&
					tsw.getTargetTScreen() instanceof IStatsListener isl)
				isl.statsReceivedCallback();
		});
	}
	// ==================================================
}
