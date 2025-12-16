package com.thecsdev.commonmc.client.mixin.hooks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LocalPlayer.class)
public interface AccessorLocalPlayer
{
	@Accessor("minecraft") Minecraft getMinecraft();
}
