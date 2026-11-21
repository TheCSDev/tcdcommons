package com.thecsdev.commonmc.client.mixin.events;

import com.thecsdev.commonmc.api.client.events.ClientEvent;
import com.thecsdev.commonmc.client.mixin.hooks.AccessorScreen;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class MixinScreen
{
	// ==================================================
	private @Unique boolean tcdcommons$isInitializing;
	// ==================================================
	@Inject(method = "init(II)V", at = @At("HEAD"))
	private void onInitPre(int width, int height, CallbackInfo ci) {
		//this tracker prevents invoking the event more than once
		this.tcdcommons$isInitializing = true;
	}
	// --------------------------------------------------
	@Inject(method = "init(II)V", at = @At("RETURN"))
	private void onInitPost(int width, int height, CallbackInfo ci) {
		//this tracker prevents invoking the event more than once
		this.tcdcommons$isInitializing = false;
		//invoke the screen initialization event
		ClientEvent.SCREEN_INIT.invoker().invoke((Screen)(Object) this, (AccessorScreen) this);
	}
	// ==================================================
	@Inject(method = "rebuildWidgets()V", at = @At("RETURN"))
	private void onRebuildWidgetsPost(CallbackInfo ci) {
		//if called by #init(II), return and do nothing.
		//#init(II) itself already invokes the event, so we don't want to invoke it a 2nd time
		if(this.tcdcommons$isInitializing) return;
		//invoke the event if this method is called by anything else
		ClientEvent.SCREEN_INIT.invoker().invoke((Screen)(Object) this, (AccessorScreen) this);
	}
	// ==================================================
}
