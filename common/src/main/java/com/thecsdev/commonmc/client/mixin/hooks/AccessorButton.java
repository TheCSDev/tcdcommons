package com.thecsdev.commonmc.client.mixin.hooks;

import net.minecraft.client.gui.components.Button;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Button.class)
public interface AccessorButton
{
	@Accessor("onPress") Button.OnPress getOnPress();
	@Mutable @Accessor("onPress") void setOnPress(@NotNull Button.OnPress onPress);
}
