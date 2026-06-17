package com.thecsdev.commonmc.client.mixin.events;

import com.llamalad7.mixinextras.sugar.Local;
import com.thecsdev.commonmc.api.client.registry.TClientRegistries;
import com.thecsdev.commonmc.client.mixin.hooks.AccessorScreen;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * Mixins for the game's HUD (heads-up display).
 */
@Mixin(Gui.class)
public abstract class MixinGui
{
	// ==================================================
	private @Final @Shadow Minecraft minecraft;
	// ==================================================
	@Inject(method = "extractRenderState", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILHARD)
	private void onPostRender(
			DeltaTracker deltaTracker,
			boolean shouldRenderLevel,
			boolean resourcesLoaded,
			CallbackInfo ci,
			@Local(name = "graphics") GuiGraphicsExtractor graphics)
	{
		//render in-game-hud screens
		if(TClientRegistries.HUD_SCREEN.size() > 0)
		{
			//prepare variables for rendering
			final var currentScreen = minecraft.gui.screen();
			final var clientWindow  = minecraft.getWindow();
			final var mouse         = minecraft.mouseHandler;
			final int windowW       = clientWindow.getGuiScaledWidth();
			final int windowH       = clientWindow.getGuiScaledHeight();
			final int mouseX        = (int) mouse.getScaledXPos(clientWindow);
		    final int mouseY        = (int) mouse.getScaledYPos(clientWindow);

			//iterate hud screens and render them
			for(final var hudScreen : TClientRegistries.HUD_SCREEN)
				try {
					//do not render the current screen
					if(hudScreen == currentScreen) continue;
					//(re/)initialize screens whenever necessary
					if(((AccessorScreen)hudScreen).getMinecraft() != this.minecraft ||
							hudScreen.width != windowW || hudScreen.height != windowH) {
						hudScreen.init(windowW, windowH);
					}
					//render the screen onto the in-game-hud
					hudScreen.extractRenderState(graphics, mouseX, mouseY, deltaTracker.getGameTimeDeltaPartialTick(false));
					//note: ticking screens is not done here, to avoid weird visual bugs
				} catch(Exception exc) {
					//hold modded screens accountable for any exceptions they throw
					final var hudScreenId = TClientRegistries.HUD_SCREEN.getKey(hudScreen);
					final var report = CrashReport.forThrowable(exc, "Rendering HUD Screen ID " + hudScreenId);
					throw new ReportedException(report);
				}
		}
	}
	// ==================================================
}
