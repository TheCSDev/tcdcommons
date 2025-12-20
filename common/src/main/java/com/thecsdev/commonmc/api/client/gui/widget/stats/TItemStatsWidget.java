package com.thecsdev.commonmc.api.client.gui.widget.stats;

import com.thecsdev.common.math.Bounds2i;
import com.thecsdev.common.properties.ObjectProperty;
import com.thecsdev.commonmc.api.client.gui.TElement;
import com.thecsdev.commonmc.api.client.gui.render.TGuiGraphics;
import com.thecsdev.commonmc.api.client.gui.tooltip.TTooltip;
import com.thecsdev.commonmc.api.stats.EmptyStatsProvider;
import com.thecsdev.commonmc.api.stats.IStatsProvider;
import com.thecsdev.commonmc.api.stats.util.ItemStats;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;

/**
 * A GUI widget that shows the statistics for a given {@link Item}.
 */
public final class TItemStatsWidget extends TStatsWidget
{
	// ==================================================
	//tooltip supplier function. defined only once to save on memory usage
	private static final Function<TElement, TElement> TOOLTIP = el -> {
		final var isw   = (TItemStatsWidget) el;
		final var stats = isw.stats.get();
		Objects.requireNonNull(stats, "Stats value is missing while constructing tooltip.");
		return TTooltip.of(stats);
	};
	// --------------------------------------------------
	private final ObjectProperty<ItemStats> stats = new ObjectProperty<>();
	// --------------------------------------------------
	private @Nullable Bounds2i  itemDrawBounds = null; //for caching maths, for optimization
	private @NotNull  ItemStack itemStack      = Items.AIR.getDefaultInstance(); //for item rendering
	// ==================================================
	public TItemStatsWidget() { this(Items.AIR, EmptyStatsProvider.INSTANCE); }
	public TItemStatsWidget(@NotNull Item item, @NotNull IStatsProvider statsProvider) {
		this(new ItemStats(item, statsProvider));
	}
	public TItemStatsWidget(@Nullable ItemStats stats)
	{
		//rendering maths optimization - invalidating old numbers
		boundsProperty().addChangeListener((p, o, n) -> this.itemDrawBounds = null);

		//handle automatic updating and set initial value
		this.stats.addChangeListener((p, o, n) -> {
			this.itemStack = (n != null) ? n.getSubject().getDefaultInstance() : Items.AIR.getDefaultInstance();
			tooltipProperty().set((n != null) ? TOOLTIP : null, TItemStatsWidget.class);
			invalidateTooltipCache();
		});
		this.stats.set(stats, TItemStatsWidget.class);
	}
	// ==================================================
	/**
	 * The {@link ObjectProperty} holding the {@link ItemStats}
	 * whose statistics are to be shown.
	 */
	public final ObjectProperty<ItemStats> statsProperty() { return this.stats; }
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

		//draw the background and the item
		if(!idb.isEmpty) pencil.renderItem(this.itemStack, idb.x, idb.y, idb.width, idb.height);
	}
	// ==================================================
}
