package com.thecsdev.commonmc.client.mixin.events;

import com.thecsdev.commonmc.api.client.events.ClientEvent;
import com.thecsdev.commonmc.client.mixin.hooks.AccessorScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class MixinScreen
{
	// ==================================================
	protected @Final @Shadow Minecraft minecraft;
	// ==================================================
	@Inject(method = "init(II)V", at = @At("RETURN"))
	private void onPostInit(int width, int height, CallbackInfo ci) {
		ClientEvent.SCREEN_INIT.invoker().invoke((Screen)(Object) this, (AccessorScreen) this);
	}
	// ==================================================
}
