package com.thecsdev.commonmc.api.client.gui.label;

import com.thecsdev.common.math.Point2d;
import com.thecsdev.common.properties.IntegerProperty;
import com.thecsdev.common.properties.NotNullProperty;
import com.thecsdev.commonmc.api.client.gui.TElement;
import com.thecsdev.commonmc.api.client.gui.render.TGuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

/**
 * An element that renders a given text {@link Component} such that it is
 * stretched to fit this element's {@link #getBounds()}.
 */
public final class TStretchedTextElement extends TElement
{
	// ==================================================
	private final NotNullProperty<Component> text      = new NotNullProperty<>(Component.empty());
	private final IntegerProperty            textColor = new IntegerProperty(0xFFFFFFFF);
	private final NotNullProperty<Point2d>   scale     = new NotNullProperty<>(new Point2d(1, 1));
	// ==================================================
	public TStretchedTextElement() {}
	public TStretchedTextElement(@NotNull Component text) { this.text.getHandle().set(text); }
	// ==================================================
	/**
	 * Returns the {@link NotNullProperty} that stores the text {@link Component}
	 * to be rendered by this element.
	 */
	public final @NotNull NotNullProperty<Component> textProperty() { return this.text; }

	/**
	 * Returns the {@link IntegerProperty} that stores the color used to render
	 * the text {@link Component}.
	 */
	public final @NotNull IntegerProperty textColorProperty() { return this.textColor; }

	/**
	 * Returns the {@link NotNullProperty} that stores the scale factor used
	 * when rendering the text {@link Component}.
	 * @apiNote Neither axis should have its scale value set to {@code 0}!
	 */
	public final @NotNull NotNullProperty<Point2d> textScaleProperty() { return this.scale; }
	// ==================================================
	protected final @Override void initCallback() {}
	// --------------------------------------------------
	@SuppressWarnings({"DataFlowIssue", "ConstantValue"})
	public final @Override void renderCallback(@NotNull TGuiGraphics pencil)
	{
		//obtain text scale, and ensure it's not zero
		final var scale = this.scale.get();
		if(scale.x <= 0d || scale.y <= 0d) return;
		//calculate text width and height
		final var text  = this.text.get();
		final var font  = screenProperty().get().getClient().font;
		final int textW = font.width(text), textH = font.lineHeight;
		//avoid division by 0 by ignoring too small text
		if(textW < 1 || textH < 1) return;
		//obtain the bounding box and matrices, for later rendering
		final var bb    = getBounds();
		final var mat   = pencil.getNativeMatrices();

		//push the matrix stack and transform it to fill this element with the text
		mat.pushMatrix();
		mat.translate(bb.x, bb.y);
		mat.scale((float) (((double) bb.width / (double) textW) * scale.x),
				(float) (((double) bb.height / (double) textH) * scale.y));
		//then render the text
		pencil.getNative().drawString(font, text, 0, 0, this.textColor.getI());
		//and then pop the matrix stack
		mat.popMatrix();
	}
	// ==================================================
}
