package com.thecsdev.commonmc.client.mixin.events;

import com.thecsdev.commonmc.api.client.gui.screen.TScreenWrapper;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public abstract class MixinHud
{
	// ==================================================
	private @Final @Shadow Minecraft minecraft;
	// ==================================================
	@Inject(method = "extractHotbarAndDecorations", at = @At("HEAD"), cancellable = true)
	private final void onExtractHotbarAndDecorations(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci)
	{
		//cancel hotbar rendering if a currently opened t-screen does not allow this
		if(this.minecraft.gui.screen() instanceof TScreenWrapper<?> tsw && !tsw.isAllowingInGameHud())
			ci.cancel();
	}
	// ==================================================
}
