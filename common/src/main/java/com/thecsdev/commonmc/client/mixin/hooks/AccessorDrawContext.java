package com.thecsdev.commonmc.client.mixin.hooks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuiGraphics.class)
public interface AccessorDrawContext
{
	public @Accessor("minecraft") Minecraft getClient();
	public @Mutable @Accessor("minecraft") void setClient(Minecraft client);
	
	public @Accessor("pose") Matrix3x2fStack getMatrices();
	public @Mutable @Accessor("pose") void setMatrices(Matrix3x2fStack matrices);
}