package com.thecsdev.commonmc.api.client.gui.misc;

import com.thecsdev.common.properties.NotNullProperty;
import com.thecsdev.common.util.annotations.Virtual;
import com.thecsdev.commonmc.api.client.gui.TElement;
import com.thecsdev.commonmc.api.client.gui.render.TGuiGraphics;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * {@link TElement} that renders a given {@link ItemStack} on the screen,
 * like how the game does it in the inventory screen.
 * <p>
 * {@link Deprecated}. Please use {@link TItemStackElement} instead.
 */
@Environment(EnvType.CLIENT)
@Deprecated(since = "5.3.0", forRemoval = true)
public @Virtual class TItemElement extends TElement
{
	// ==================================================
	private final NotNullProperty<ItemStack> itemStack = new NotNullProperty<>(Items.AIR.getDefaultInstance());
	// ==================================================
	public TItemElement() { this(Items.AIR.getDefaultInstance()); }
	public TItemElement(@Nullable ItemStack itemStack)
	{
		//this element should not be focusable or hoverable
		focusableProperty().set(false, TItemElement.class);
		hoverableProperty().set(false, TItemElement.class);
		//initialize properties
		this.itemStack.set(itemStack, TItemElement.class);
	}
	// ==================================================
	/**
	 * Returns the {@link NotNullProperty} holding the {@link ItemStack}
	 * that is to be rendered by this {@link TItemElement}.
	 */
	public final NotNullProperty<ItemStack> itemStackProperty() { return this.itemStack; }
	// ==================================================
	public @Virtual @Override void renderCallback(@NotNull TGuiGraphics pencil) {
		final var bb = getBounds();
		pencil.renderItem(this.itemStack.get(), bb.x, bb.y, bb.width, bb.height);
	}
	// ==================================================
}
