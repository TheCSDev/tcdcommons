package com.thecsdev.commonmc.api.client.gui.misc;

import com.thecsdev.common.util.annotations.Virtual;
import com.thecsdev.commonmc.api.client.gui.TElement;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * {@link TElement} that renders a given {@link ItemStack} on the screen,
 * like how the game does it in the inventory screen.
 */
@Environment(EnvType.CLIENT)
public @Virtual class TItemStackElement extends TItemElement
{
	public TItemStackElement() { super(); }
	public TItemStackElement(@Nullable ItemStack itemStack) { super(itemStack); }
}
