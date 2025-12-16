package com.thecsdev.commonmc.api.client.gui.tooltip;

import com.thecsdev.common.util.annotations.Virtual;
import com.thecsdev.commonmc.api.client.gui.TElement;
import com.thecsdev.commonmc.api.client.gui.render.TGuiGraphics;
import com.thecsdev.commonmc.api.stats.util.CustomStat;
import com.thecsdev.commonmc.api.stats.util.EntityStats;
import com.thecsdev.commonmc.api.stats.util.ItemStats;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A {@link TElement} whose sole purpose is meant to serve as being
 * as tooltip element for other {@link TElement}s.
 */
public @Virtual class TTooltip extends TElement
{
	// ==================================================
	public @Virtual @Override void renderCallback(@NotNull TGuiGraphics pencil) {
		final var bb = getBounds();
		pencil.fillColor(bb.x, bb.y, bb.width, bb.height, 0xFF000000);
		pencil.drawOutlineIn(bb.x, bb.y, bb.width, bb.height, 0xFF29006b);
	}
	// ==================================================
	/**
	 * Creates and returns a new simple textual tooltip element.
	 * @param text The tooltip text.
	 * @throws NullPointerException If the argument is {@code null}.
	 */
	public static final TTooltip of(@NotNull Component text) throws NullPointerException {
		return new TTooltipLabel(Objects.requireNonNull(text));
	}
	// --------------------------------------------------
	/**
	 * A tooltip that shows the value of a given "custom stat" (also known
	 * as "general stat") in the statistics GUI.
	 * @param stat The custom/general stat.
	 * @throws NullPointerException If the argument is {@code null}.
	 */
	public static final TTooltip of(@NotNull CustomStat stat) throws NullPointerException {
		return new TTooltipCustomStat(stat);
	}

	/**
	 * A tooltip that shows the player statistics for a given {@link Item}.
	 * @param stats The {@link Item} whose statistics are to be shown.
	 * @throws NullPointerException If the argument is {@code null}.
	 */
	public static final TTooltip of(@NotNull ItemStats stats) throws NullPointerException {
		return new TTooltipItemStats(stats);
	}

	/**
	 * A tooltip that shows the player statistics for a given {@link EntityType}.
	 * @param stats The {@link EntityType} whose statistics are to be shown.
	 * @throws NullPointerException If the argument is {@code null}.
	 */
	public static final TTooltip of(@NotNull EntityStats stats) throws NullPointerException {
		return new TTooltipEntityStats(stats);
	}
	// ==================================================
}
