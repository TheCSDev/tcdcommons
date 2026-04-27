package com.thecsdev.commonmc.api.client.gui.misc;

import com.thecsdev.common.properties.BooleanProperty;
import com.thecsdev.common.properties.NotNullProperty;
import com.thecsdev.common.util.annotations.Virtual;
import com.thecsdev.commonmc.api.client.gui.TElement;
import com.thecsdev.commonmc.api.client.gui.render.TGuiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * {@link TElement} that renders a given {@link BlockState} on the screen,
 * like how the game does it in the inventory screen.
 */
public @Virtual class TBlockStateElement extends TElement
{
	// ==================================================
	private final NotNullProperty<BlockState> blockState = new NotNullProperty<>(Blocks.AIR.defaultBlockState());
	private final BooleanProperty             renderItem = new BooleanProperty(true);
	// --------------------------------------------------
	private @NotNull ItemStack          _asStack; //for rendering
	private @NotNull TextureAtlasSprite _sprite;  //for rendering
	private          boolean            _isAir;   //for rendering
	// ==================================================
	public TBlockStateElement() { this(Blocks.AIR.defaultBlockState()); }
	public TBlockStateElement(@Nullable BlockState blockState)
	{
		//this element should not be focusable or hoverable
		focusableProperty().set(false, TBlockStateElement.class);
		hoverableProperty().set(false, TBlockStateElement.class);
		//initialize properties and fields
		this.blockState.set(blockState, TBlockStateElement.class);
		refresh();
		//change-listeners for refreshing internal field values
		this.blockState.addChangeListener((_, _, _) -> refresh());
		this.renderItem.addChangeListener((_, _, _) -> refresh());
	}
	// --------------------------------------------------
	/**
	 * Refreshes the internal field values used for rendering.
	 */
	private final void refresh()
	{
		final var bs  = this.blockState.get();
		final var bl  = bs.getBlock();
		this._asStack = bl.asItem().getDefaultInstance();
		this._sprite  = Minecraft.getInstance().getModelManager()
				.getBlockStateModelSet().get(bs).particleMaterial().sprite();
		this._isAir   = isAir(bl);
	}
	// ==================================================
	/**
	 * Returns the {@link NotNullProperty} holding the {@link BlockState}
	 * that is to be rendered by this {@link TBlockStateElement}.
	 */
	public final NotNullProperty<BlockState> blockStateProperty() { return this.blockState; }

	/**
	 * If set to {@code true}, the {@link BlockState}'s corresponding
	 * default {@link ItemStack} will be rendered where possible
	 * (similar to {@link TItemStackElement}). Otherwise, only the
	 * {@link BlockState}'s sprite will be rendered.
	 */
	public final BooleanProperty renderItemStackProperty() { return this.renderItem; }
	// ==================================================
	public final @Override void renderCallback(@NotNull TGuiGraphics pencil)
	{
		final var bb = getBounds();
		if(this.renderItem.getZ() || this._isAir) {
			pencil.renderItem(this._asStack, bb.x, bb.y, bb.width, bb.height);
		} else {
			pencil.getNative().blitSprite(RenderPipelines.GUI_TEXTURED, this._sprite, bb.x, bb.y, bb.width, bb.height);
		}
	}
	// ==================================================
	/**
	 * Returns {@code true} if a {@link Block} is an instance of
	 * {@link AirBlock}. This includes regular air, cave air, and void air.
	 * <p>
	 * <b>Note:</b> This does NOT include structure void, as that is not air.
	 */
	public static final boolean isAir(@NotNull Block block) { return (block instanceof AirBlock); }
	// ==================================================
}
