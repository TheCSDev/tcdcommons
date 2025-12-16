package com.thecsdev.commonmc.client.mixin.hooks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Screen.class)
public interface AccessorScreen
{
	public @Accessor("minecraft") Minecraft getMinecraft();
	public @Mutable @Accessor("title") void setTitle(Component title);
}
