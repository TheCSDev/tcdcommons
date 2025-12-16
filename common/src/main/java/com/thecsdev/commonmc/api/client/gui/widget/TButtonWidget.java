package com.thecsdev.commonmc.api.client.gui.widget;

import com.thecsdev.common.properties.IntegerProperty;
import com.thecsdev.common.util.annotations.Virtual;
import com.thecsdev.common.util.enumerations.CompassDirection;
import com.thecsdev.commonmc.api.client.gui.label.TLabelElement;
import com.thecsdev.commonmc.api.client.gui.panel.TPanelElement;
import com.thecsdev.commonmc.api.client.gui.render.TGuiGraphics;
import org.jetbrains.annotations.NotNull;

import static com.thecsdev.commonmc.api.client.gui.panel.TPanelElement.*;

/**
 * A {@link TClickableWidget} implementation that looks and behaves
 * like a regular button you'd see in-game's GUI.
 */
public @Virtual class TButtonWidget extends TClickableWidget
{
	// ================================================== ==================================================
	//                                      TButtonWidget IMPLEMENTATION
	// ================================================== ==================================================
	/**
	 * Default horizontal padding that is applied to the button's {@link #getLabel()}.
	 */
	public static final int LBL_PAD_X = 5;

	/**
	 * Default vertical padding that is applied to the button's {@link #getLabel()}.
	 */
	public static final int LBL_PAD_Y = 3;
	// ==================================================
	private final TLabelElement label = new TLabelElement();
	// ==================================================
	public TButtonWidget()
	{
		//reinitialize the children in the event of a resize
		boundsProperty().addChangeListener((p, o, n) -> {
			if(!o.hasSameSize(n) && getParent() != null) clearAndInit();
		});

		//default label properties
		this.label.textAlignmentProperty().set(CompassDirection.CENTER, TButtonWidget.class);
		this.label.wrapTextProperty().set(false, TButtonWidget.class);
	}
	// ==================================================
	/**
	 * Returns the {@link TLabelElement} used by this {@link TButtonWidget}.
	 * Use this to customize the button overlay text.
	 */
	public final TLabelElement getLabel() { return this.label; }
	// ==================================================
	protected @Virtual @Override void initCallback() {
		final var bb = this.getBounds();
		this.label.setBounds(bb.add(LBL_PAD_X, LBL_PAD_Y, -LBL_PAD_X * 2, -LBL_PAD_Y * 2));
		add(this.label);
	}
	// --------------------------------------------------
	public @Virtual @Override void renderCallback(@NotNull TGuiGraphics pencil) {
		//draw the button texture, based on the button's state
		final var bb = getBounds();
		pencil.drawButton(
				bb.x, bb.y, bb.width, bb.height, -1,
				isFocusable(), isHoveredOrFocused());
	}
	// ================================================== ==================================================
	//                         Flat/Transparent/Paintable IMPLEMENTATION
	// ================================================== ==================================================
	/**
	 * {@link TButtonWidget} whose visual appearance is more flat and does not
	 * use the vanilla button texture. Looks more like a {@link TPanelElement}.
	 */
	public static @Virtual class Flat extends TButtonWidget
	{
		public @Virtual @Override void renderCallback(@NotNull TGuiGraphics pencil)
		{
			//draw the background fill and then the outline
			final var bb = getBounds();
			pencil.fillColor(bb.x, bb.y, bb.width, bb.height, COLOR_BACKGROUND);
			pencil.drawOutlineIn(
					bb.x, bb.y, bb.width, bb.height,
					isHoveredOrFocused() ? COLOR_OUTLINE_FOCUSED : COLOR_OUTLINE);
		}
	}

	/**
	 * {@link TButtonWidget} that does not have a visual appearance aside from its
	 * label and focus outline.
	 */
	public static @Virtual class Transparent extends TButtonWidget
	{
		public @Virtual @Override void renderCallback(@NotNull TGuiGraphics pencil) {
			if(!isHoveredOrFocused()) return;
			final var bb = getBounds();
			pencil.drawOutlineIn(bb.x, bb.y, bb.width, bb.height, isHovered() ? COLOR_OUTLINE : COLOR_OUTLINE_FOCUSED);
		}
	}

	/**
	 * {@link TButtonWidget} implementation that allows you to customize its background
	 * and outline colors.
	 */
	public static @Virtual class Paintable extends TButtonWidget
	{
		// ==================================================
		private final IntegerProperty background     = new IntegerProperty(COLOR_BACKGROUND);
		private final IntegerProperty outline        = new IntegerProperty(COLOR_OUTLINE);
		private final IntegerProperty outlineFocused = new IntegerProperty(COLOR_OUTLINE_FOCUSED);
		// ==================================================
		public Paintable() {}
		public Paintable(int backgroundColor) { this.background.getHandle().set(backgroundColor); }
		public Paintable(int backgroundColor, int outlineColor) {
			this.background.getHandle().set(backgroundColor);
			this.outline.getHandle().set(outlineColor);
		}
		public Paintable(int backgroundColor, int outlineColor, int focusOutlineColor) {
			this.background.getHandle().set(backgroundColor);
			this.outline.getHandle().set(outlineColor);
			this.outlineFocused.getHandle().set(focusOutlineColor);
		}
		// ==================================================
		/**
		 * {@link IntegerProperty} for the background color of this {@link TButtonWidget.Paintable} button.
		 */
		public final IntegerProperty backgroundColorProperty() { return this.background; }

		/**
		 * {@link IntegerProperty} for the outline color of this {@link TButtonWidget.Paintable} button.
		 */
		public final IntegerProperty outlineColorProperty() { return this.outline; }

		/**
		 * {@link IntegerProperty} for the outline color of this {@link TButtonWidget.Paintable} button
		 * when this button {@link #isFocused()}.
		 */
		public final IntegerProperty focusedOutlineColorProperty() { return this.outlineFocused; }
		// ==================================================
		public final @Override void renderCallback(@NotNull TGuiGraphics pencil) {
			final var bb = getBounds();
			pencil.fillColor(bb.x, bb.y, bb.width, bb.height, this.background.getI());
		}

		public final @Override void postRenderCallback(@NotNull TGuiGraphics pencil) {
			final var bb = getBounds();
			pencil.drawOutlineIn(
					bb.x, bb.y, bb.width, bb.height,
					isHoveredOrFocused() ? this.outlineFocused.getI() : this.outline.getI());
			super.postRenderCallback(pencil);
		}
		// ==================================================
	}
	// ================================================== ==================================================
}
