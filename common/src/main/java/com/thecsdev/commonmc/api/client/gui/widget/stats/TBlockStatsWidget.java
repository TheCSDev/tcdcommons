package com.thecsdev.commonmc.api.client.gui.widget.stats;

import com.thecsdev.common.math.Bounds2i;
import com.thecsdev.common.properties.ObjectProperty;
import com.thecsdev.commonmc.api.client.gui.TElement;
import com.thecsdev.commonmc.api.client.gui.render.TGuiGraphics;
import com.thecsdev.commonmc.api.client.gui.tooltip.TTooltip;
import com.thecsdev.commonmc.api.stats.IStatsProvider;
import com.thecsdev.commonmc.api.stats.util.BlockStats;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;

/**
 * A GUI widget that shows the statistics for a given {@link Block}.
 */
@Environment(EnvType.CLIENT)
public final class TBlockStatsWidget extends TStatsWidget
{
	// ==================================================
	//tooltip supplier function. defined only once to save on memory usage
	private static final Function<TElement, TElement> TOOLTIP = el -> {
		final var isw   = (TBlockStatsWidget) el;
		final var stats = isw.stats.get();
		Objects.requireNonNull(stats, "Stats value is missing while constructing tooltip.");
		return TTooltip.of(stats);
	};
	// ==================================================
	private final ObjectProperty<BlockStats> stats = new ObjectProperty<>();
	// --------------------------------------------------
	private @Nullable Bounds2i           itemDrawBounds = null;                           //for caching maths, for optimization
	private @NotNull  ItemStack          itemStack      = Items.AIR.getDefaultInstance(); //for rendering
	private @Nullable TextureAtlasSprite sprite         = null;                           //for rendering
	private           boolean            renderSprite   = false;
	// ==================================================
	public TBlockStatsWidget() { this(null); }
	public TBlockStatsWidget(@NotNull Block subject, @NotNull IStatsProvider provider) { this(new BlockStats(subject, provider)); }
	public TBlockStatsWidget(@Nullable BlockStats stats)
	{
		//rendering maths optimization - invalidating old numbers
		boundsProperty().addChangeListener((p, o, n) -> this.itemDrawBounds = null);

		//handle automatic updating and set initial value
		this.stats.addChangeListener((p, o, n) ->
		{
			if(n == null) {
				this.itemStack    = Items.AIR.getDefaultInstance();
				this.sprite       = null;
				this.renderSprite = false;
				tooltipProperty().set(null, TBlockStatsWidget.class);
				invalidateTooltipCache();
				return;
			}

			//obtain the item stack for the given block, if any
			this.itemStack = n.getSubject().asItem().getDefaultInstance();

			//obtain the block model and texture
			this.sprite = Minecraft.getInstance()
					.getBlockRenderer()
					.getBlockModel(n.getSubject().defaultBlockState())
					.particleIcon();

			//noinspection ConstantValue - render sprite when no corresponding item exists
			this.renderSprite = (this.sprite != null) &&      //if there's a sprite
					(n.getSubject().asItem() == Items.AIR) && //and there's no corresponding item
					(n.getSubject() != Blocks.AIR) &&         //and block itself isn't an AIR
					(n.getSubject() != Blocks.CAVE_AIR) &&    //and block itself isn't an AIR
					(n.getSubject() != Blocks.VOID_AIR);      //and block itself isn't an AIR

			//refresh the tooltip text
			tooltipProperty().set(TOOLTIP, TBlockStatsWidget.class);
			invalidateTooltipCache();
		});
		this.stats.set(stats, TBlockStatsWidget.class);
	}
	// ==================================================
	/**
	 * The {@link ObjectProperty} holding the {@link BlockStats}
	 * whose statistics are to be shown.
	 */
	public final ObjectProperty<BlockStats> statsProperty() { return this.stats; }
	// ==================================================
	public final @Override void renderCallback(@NotNull TGuiGraphics pencil)
	{
		//render super
		super.renderCallback(pencil);

		//calculate bounding boxes
		final var bb = getBounds();
		final var idb = (this.itemDrawBounds != null) ?
				this.itemDrawBounds :
				(this.itemDrawBounds = new Bounds2i(bb.x + 3, bb.y + 3, bb.width - 6, bb.height - 6));
		if(idb.isEmpty) return;

		//draw the background and the item
		if(!this.renderSprite || this.sprite == null)
			pencil.renderItem(this.itemStack, idb.x, idb.y, idb.width, idb.height);
		else pencil.getNative().blitSprite(RenderPipelines.GUI_TEXTURED, this.sprite, idb.x, idb.y, idb.width, idb.height);
	}
	// ==================================================
}
