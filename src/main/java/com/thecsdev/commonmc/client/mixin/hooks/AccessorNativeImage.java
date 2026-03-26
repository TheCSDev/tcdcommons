package com.thecsdev.commonmc.client.mixin.hooks;

import com.mojang.blaze3d.platform.NativeImage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(NativeImage.class)
public interface AccessorNativeImage
{
	public @Accessor("pixels") long getPointer();
	public @Accessor("width")  int  getWidth();
	public @Accessor("height") int  getHeight();
}