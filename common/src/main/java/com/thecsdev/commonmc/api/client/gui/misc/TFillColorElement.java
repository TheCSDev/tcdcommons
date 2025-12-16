package com.thecsdev.commonmc.api.client.gui.misc;

import com.thecsdev.common.properties.IntegerProperty;
import com.thecsdev.common.util.annotations.Virtual;
import com.thecsdev.commonmc.api.client.gui.TElement;
import com.thecsdev.commonmc.api.client.gui.render.TGuiGraphics;
import org.jetbrains.annotations.NotNull;

/**
 * A blank {@link TElement} whose sole purpose is to render
 * a solid-colored rectangle.
 */
public @Virtual class TFillColorElement extends TElement
{
	// ================================================== ==================================================
	//                                  TFillColorElement IMPLEMENTATION
	// ================================================== ==================================================
	private final IntegerProperty fillColor    = new IntegerProperty();
	private final IntegerProperty outlineColor = new IntegerProperty();
	// ==================================================
	public TFillColorElement() { this(0, 0); }
	public TFillColorElement(int fillColor) { this(fillColor, 0); }
	public TFillColorElement(int fillColor, int outlineColor) {
		this.fillColor.set(fillColor, TFillColorElement.class);
		this.outlineColor.set(outlineColor, TFillColorElement.class);
	}
	// ==================================================
	/**
	 * Returns the {@link IntegerProperty} that represents the color
	 * of the rectangle that is being drawn.
	 */
	public final IntegerProperty fillColorProperty() { return this.fillColor; }

	/**
	 * Returns the {@link IntegerProperty} that represents the color
	 * of the rectangle border that is being drawn as an "outline".
	 */
	public final IntegerProperty outlineColorProperty() { return this.outlineColor; }
	// ==================================================
	public @Virtual @Override void renderCallback(@NotNull TGuiGraphics pencil) {
		final var bb    = getBounds();
		final int color = this.fillColor.getI();
		if(color != 0) pencil.fillColor(bb.x, bb.y, bb.width, bb.height, color);
	}

	public @Virtual @Override void postRenderCallback(@NotNull TGuiGraphics pencil) {
		final var bb = getBounds();
		final int color = this.outlineColor.getI();
		if(color != 0) pencil.drawOutlineIn(bb.x, bb.y, bb.width, bb.height, color);
	}
	// ================================================== ==================================================
	//                                               Flat IMPLEMENTATION
	// ================================================== ==================================================
	/**
	 * {@link TFillColorElement} implementation that renders its outline in
	 * {@link #renderCallback(TGuiGraphics)} instead of {@link #postRenderCallback(TGuiGraphics)},
	 * allowing you to have an outline that doesn't overlay over children.
	 */
	public static @Virtual class Flat extends TFillColorElement
	{
		// ==================================================
		public Flat() {}
		public Flat(int fillColor) { super(fillColor); }
		public Flat(int fillColor, int outlineColor) { super(fillColor, outlineColor); }
		// ==================================================
		public @Virtual @Override void postRenderCallback(@NotNull TGuiGraphics pencil) {}
		public @Virtual @Override void renderCallback(@NotNull TGuiGraphics pencil) {
			final var bb     = getBounds();
			final int fcolor = fillColorProperty().getI();
			final int ocolor = outlineColorProperty().getI();
			if(fcolor != 0) pencil.fillColor(bb.x, bb.y, bb.width, bb.height, fcolor);
			if(ocolor != 0) pencil.drawOutlineIn(bb.x, bb.y, bb.width, bb.height, ocolor);
		}
		// ==================================================
	}
	// ================================================== ==================================================
}
