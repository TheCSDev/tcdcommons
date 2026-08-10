package com.thecsdev.commonmc.client.mixin.events;

import com.mojang.blaze3d.platform.NativeImage;
import org.spongepowered.asm.mixin.Mixin;

/**
 * This {@link Mixin}'s sole purpose is to put image byte data loading up to
 * LWJGL's digression, rather than having the game enforce PNG. And on another note,
 * the game even fails to validate PNG properly most the time anyway, so this
 * is kind of more of a bug fix in a way.
 */
@Mixin(value = NativeImage.class, priority = -1000)
public abstract class MixinNativeImage
{
	//come to think of it, this mixin is not needed anymore unlike before 5.X.
	//should external image loading ever be needed in the future, this mixin
	//will return. for now, it is best it not interfere with other mods.
	/*
	@Redirect(
		method = "read(Lcom/mojang/blaze3d/platform/NativeImage$Format;Ljava/nio/ByteBuffer;)Lcom/mojang/blaze3d/platform/NativeImage;",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/util/PngInfo;validateHeader(Ljava/nio/ByteBuffer;)V"
		),
		require = 0
	)
	private static void skipPngValidation(ByteBuffer buffer) {}
	*/
}
