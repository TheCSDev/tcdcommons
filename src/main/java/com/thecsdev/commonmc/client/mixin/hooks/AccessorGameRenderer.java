package com.thecsdev.commonmc.client.mixin.hooks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.GameRenderer;

@Mixin(GameRenderer.class)
public interface AccessorGameRenderer
{
	public @Accessor("guiRenderer") GuiRenderer getGuiRenderer();
}