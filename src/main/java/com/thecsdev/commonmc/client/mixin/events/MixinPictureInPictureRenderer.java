package com.thecsdev.commonmc.client.mixin.events;

import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.SubmitNodeStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin for {@link PictureInPictureRenderer}, which is what the game's GUIs
 * use to render things like entities onto their own dedicated textures.
 * <p>
 * Primarily exists to work around a crash caused by the "Improved Transparency"
 * video setting, where "water mask" submissions (aka boat water patches)
 * cannot be rendered inside picture-in-picture (GUI) render passes.
 */
@Mixin(PictureInPictureRenderer.class)
public abstract class MixinPictureInPictureRenderer
{
	// ==================================================
	private @Final @Shadow SubmitNodeStorage submitNodeStorage;
	// ==================================================
	@Inject(
			method = "prepare",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher;prepareFrame(Lnet/minecraft/client/renderer/SubmitNodeStorage;)Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher$PreparedFrame;",
					shift = At.Shift.BEFORE
			),
			require = 0
	)
	private void onBeforePrepareFrame(CallbackInfo ci)
	{
		//water masks are meant to be rendered inside the game's dedicated depth-only
		//"Water mask" render pass, and cannot be rendered inside picture-in-picture
		//(GUI) render passes, which are color-based. when the "Improved Transparency"
		//video setting is enabled, they use a render pipeline that has no color
		//targets at all, resulting in a crash. they produce no color output and are
		//meaningless inside a GUI, where there is no water to be masked
		for(final var collection : this.submitNodeStorage.getSubmitsPerOrder().values())
			collection.waterMask.clear();
	}
	// ==================================================
}