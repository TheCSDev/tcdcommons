package com.thecsdev.commonmc.api.client.gui.widget.stats;

import com.thecsdev.common.properties.IntegerProperty;
import com.thecsdev.common.util.annotations.Virtual;
import com.thecsdev.commonmc.api.client.gui.render.TGuiGraphics;
import com.thecsdev.commonmc.api.client.gui.widget.TButtonWidget;
import org.jetbrains.annotations.NotNull;

import static com.thecsdev.commonmc.api.client.gui.panel.TPanelElement.*;

/**
 * The base {@link Class} for stats widgets.
 */
abstract sealed class TStatsWidget extends TButtonWidget permits TEntityStatsWidget, TItemStatsWidget, TTextualStatWidget
{
	// ==================================================
	private final IntegerProperty backgroundColor   = new IntegerProperty(COLOR_BACKGROUND);
	private final IntegerProperty outlineColor      = new IntegerProperty(0x35000000);
	private final IntegerProperty focusOutlineColor = new IntegerProperty(COLOR_OUTLINE_FOCUSED);
	// ==================================================
	/**
	 * The {@link IntegerProperty} for this stat widget's background color.
	 * This color is drawn in the background of this element.
	 */
	public final IntegerProperty backgroundColorProperty() { return this.backgroundColor; }

	/**
	 * The {@link IntegerProperty} for this stat widget's outline color.
	 * This color is drawn as the outline.
	 */
	public final IntegerProperty outlineColorProperty() { return this.outlineColor; }

	/**
	 * The {@link IntegerProperty} for this stat widget's focus outline color.
	 * This color is drawn as the outline when this element is focused.
	 */
	public final IntegerProperty focusOutlineColorProperty() { return this.focusOutlineColor; }
	// ==================================================
	public @Virtual @Override void renderCallback(@NotNull TGuiGraphics pencil) {
		//draw background color
		final var bb = getBounds();
		pencil.fillColor(bb.x, bb.y, bb.width, bb.height, this.backgroundColor.getI());
	}

	public @Virtual @Override void postRenderCallback(@NotNull TGuiGraphics pencil) {
		//draw outline
		final var bb = getBounds();
		pencil.drawOutlineIn(
				bb.x, bb.y, bb.width, bb.height,
				isHoveredOrFocused() ? this.focusOutlineColor.getI() : this.outlineColor.getI());
	}
	// ==================================================
}
