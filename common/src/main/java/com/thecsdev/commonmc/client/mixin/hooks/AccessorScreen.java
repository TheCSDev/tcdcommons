package com.thecsdev.commonmc.client.mixin.hooks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(Screen.class)
public interface AccessorScreen
{
	public @Accessor("minecraft") Minecraft getMinecraft();
	public @Mutable @Accessor("title") void setTitle(Component title);
	public @Accessor("renderables") List<Renderable> getRenderables();
	public @Accessor("children") List<GuiEventListener> getChildren();
	public @Accessor("narratables") List<NarratableEntry> getNarratables();
}
