package com.thecsdev.commonmc.client.mixin.events;

import com.thecsdev.commonmc.api.client.events.ClientEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft
{
	// ==================================================
	public @Shadow @Nullable LocalPlayer player;
	// ==================================================
	@Inject(method = "clearClientLevel", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/client/gui/Gui;onDisconnected()V"))
	private final void onLocalPlayerQuit(Screen screen, CallbackInfo ci) {
		final var lp = Objects.requireNonNull(player, "Missing 'LocalPlayer' instance");
		((Minecraft)(Object)this).execute(() -> ClientEvent.PLAYER_QUIT.invoker().invoke(lp));
	}
	// ==================================================
}
