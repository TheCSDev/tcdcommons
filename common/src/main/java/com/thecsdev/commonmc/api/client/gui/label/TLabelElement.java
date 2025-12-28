package com.thecsdev.commonmc.api.client.gui.label;

import com.thecsdev.common.math.Bounds2i;
import com.thecsdev.common.properties.*;
import com.thecsdev.common.util.annotations.Virtual;
import com.thecsdev.common.util.enumerations.CompassDirection;
import com.thecsdev.commonmc.api.client.gui.TElement;
import com.thecsdev.commonmc.api.client.gui.render.TGuiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A {@link TElement} that draws text on the screen.
 */
public final class TLabelElement extends TElement
{
	// ==================================================
	private final NotNullProperty<Component>        text          = new NotNullProperty<>(Component.empty());
	private final NotNullProperty<Font>             font          = new NotNullProperty<>(Minecraft.getInstance().font);
	private final IntegerProperty                   lineSpacing   = new IntegerProperty(2);
	private final IntegerProperty                   textColor     = new IntegerProperty(0xFFFFFFFF);
	private final BooleanProperty                   wrapText      = new BooleanProperty(false);
	private final NotNullProperty<CompassDirection> textAlignment = new NotNullProperty<>(CompassDirection.WEST);
	private final DoubleProperty                    textScale     = new DoubleProperty(1d);
	// --------------------------------------------------
	private boolean                     isDirty = true;
	private List<FormattedCharSequence> lines;
	private int                         textWidth, textHeight; //scaled
	// ==================================================
	public TLabelElement(@NotNull Component text) { this(); setText(text); }
	public @SuppressWarnings("unchecked") TLabelElement()
	{
		//this element is not supposed to be focusable or hoverable by default
		focusableProperty().set(false, TLabelElement.class);
		hoverableProperty().set(false, TLabelElement.class);

		//whenever a change to text and its visual properties take place, the
		//text elements need to be reconstructed, hence these change listeners
		final IChangeListener<?> cl_refresh = (p, o, n) -> this.isDirty = true;
		boundsProperty()  .addChangeListener((p, o, n) -> { if(!o.hasSameSize(n)) this.isDirty = true; });
		this.text         .addChangeListener((IChangeListener<Component>)        cl_refresh);
		this.lineSpacing  .addChangeListener((IChangeListener<Integer>)          cl_refresh);
		this.wrapText     .addChangeListener((IChangeListener<Boolean>)          cl_refresh);
		this.textAlignment.addChangeListener((IChangeListener<CompassDirection>) cl_refresh);
		this.textScale    .addChangeListener((IChangeListener<Double>)           cl_refresh);
	}
	// ==================================================
	/**
	 * An {@link NotNullProperty} for this {@link TLabelElement}'s text.
	 */
	public final NotNullProperty<Component> textProperty() { return this.text; }

	/**
	 * An {@link NotNullProperty} for this {@link TLabelElement}'s {@link Font}.
	 */
	public final NotNullProperty<Font> fontProperty() { return this.font; }

	/**
	 * An {@link IntegerProperty} for the spacing between lines of text.
	 */
	public final IntegerProperty lineSpacingProperty() { return this.lineSpacing; }

	/**
	 * An {@link IntegerProperty} for the color of the text that will be rendered.
	 */
	public final IntegerProperty textColorProperty() { return this.textColor; }

	/**
	 * A {@link BooleanProperty} for whether the text should be wrapped.
	 * @apiNote <a href="https://en.wikipedia.org/wiki/Wrapping_(text)">Wikipedia article</a>
	 */
	public final BooleanProperty wrapTextProperty() { return this.wrapText; }

	/**
	 * An {@link NotNullProperty} for the horizontal and vertical alignment of text.
	 */
	public final NotNullProperty<CompassDirection> textAlignmentProperty() { return this.textAlignment; }

	/**
	 * A {@link DoubleProperty} for the visual scale of the text.
	 */
	public final DoubleProperty textScaleProperty() { return this.textScale; }
	// ==================================================
	protected final @Override void initCallback() { /*if(this.isDirty) refresh();*/ }
	// --------------------------------------------------
	public final @Override void renderCallback(@NotNull TGuiGraphics pencil)
	{
		//if dirty, refresh this label
		if(this.isDirty) refresh();
		//do not waste resources rendering if there's no lines to render
		else if(this.lines.isEmpty()) return;

		//prepare
		final var bb          = getBounds();
		final var font        = this.font.get();
		final var textScale   = this.textScale.getF();
		final int lineSpacing = (lines.size() < 2 ? 0 : this.lineSpacing.getI());
		final int totalLineH  = font.lineHeight + lineSpacing;
		final int textHeight  = (int) ((float) getTextHeight() / textScale); //unscaled total text height
		final int color       = this.textColor.getI();
		final var align       = this.textAlignment.get();

		//the overall height of the text block after scaling is applied.
		final float textHeightScaled = (float)textHeight * textScale;

		//1. calculate the final y position where the top of the scaled text block should start
		final float totalTranslateY;
		if(align.isTop())
			totalTranslateY = bb.y;
		else if(align.isCenterY())
			//center the scaled text height within the bounds height
			totalTranslateY = bb.y + (bb.height - textHeightScaled) / 2.0f;
		else //isBottom() - place the bottom of the scaled text block at bb.endY
			totalTranslateY = bb.endY - textHeightScaled;


		//we also need to calculate the *maximum unscaled width* of the text block
		int maxUnscaledWidth = 0;
		for(final var line : lines) {
			final int lineWidth = font.width(line);
			if(lineWidth > maxUnscaledWidth) maxUnscaledWidth = lineWidth;
		}
		final float textWidthScaled = (float) maxUnscaledWidth * textScale;

		//2. calculate the final x position where the left of the scaled text block should start
		final float totalTranslateX;
		if(align.isLeft())
			totalTranslateX = bb.x;
		else if(align.isCenterX())
			//center the scaled text width within the bounds width
			totalTranslateX = bb.x + (bb.width - textWidthScaled) / 2.0f;
		else //isRight() - place the right of the scaled text block at bb.endX
			totalTranslateX = bb.endX - textWidthScaled;


		//3. apply transformations: translate to the final top-left of the scaled text block, then scale
		pencil.pushScissors(bb.x, bb.y, bb.width, bb.height);
		final var matrix = pencil.getNativeMatrices();
		matrix.pushMatrix();
		matrix.scale(textScale, textScale);
		matrix.translate(totalTranslateX / textScale, totalTranslateY / textScale);

		try
		{
			//vertical anchor is now always 0, as the initial translation handled it
			final int startY = 0;

			int offsetY = 0;
			for(final var line : lines)
			{
				final int lineWidth = font.width(line);

				//horizontal anchor (nearest-pixel centering). Calculations use unscaled width
				//NOTE: we now align within the *unscaled* text width (maxUnscaledWidth) and not bb.width
				final int drawX;
				if(align.isLeft())
					drawX = 0;
				else if(align.isCenterX())
					//center the line within the max text width
					drawX = Math.round((maxUnscaledWidth - lineWidth) / 2.0f);
				else //isRight() - right-align the line within the max text width
					drawX = maxUnscaledWidth - lineWidth;

				final int drawY = startY + offsetY;
				pencil.getNative().drawString(font, line, drawX, drawY, color);

				offsetY += totalLineH;
			}
		}
		finally {
			matrix.popMatrix();
			pencil.popScissors();
		}
	}

	public @Virtual @Override void postRenderCallback(@NotNull TGuiGraphics pencil)
	{
		//default focus outline for focused elements
		if(!isFocused()) return;
		final var bb = getBounds();
		pencil.drawOutlineIn(bb.x, bb.y, bb.width, bb.height, 0xDAFFFFFF);
	}
	// ==================================================
	/**
	 * Refreshes this {@link TLabelElement}. This may be called in the event
	 * a property was updated without triggering change listeners.
	 */
	public final void refresh()
	{
		//reset the dirtiness flag
		this.isDirty = false;

		//if the text is empty, clear the lines and return
		final var    text      = this.text.get();
		final double textScale = this.textScale.getD(); //can't divide by 0
		if(StringUtils.isEmpty(text.getString()) || textScale < 0.01) {
			this.lines      = List.of();
			this.textHeight = 0;
			return;
		}

		//wrap text
		final var font  = this.font.get();
		final var lines = this.lines = font.split(text, this.wrapText.getZ() ?
				(int) ((double) getBounds().width / textScale) :
				Integer.MAX_VALUE);

		//calculate text height
		final double lineSpacing = (lines.size() < 2 ? 0 : this.lineSpacing.getD());
		final double totalLineH  = ((double) font.lineHeight + lineSpacing) * textScale;
		this.textWidth           = (int) Math.ceil(((double) lines.stream().map(font::width).max(Integer::compare).orElse(0) * textScale));
		this.textHeight          = (int) Math.ceil((lines.size() * totalLineH) - (lineSpacing * textScale));
	}
	// --------------------------------------------------
	/**
	 * Convenience method for getting the value of {@link #textProperty()}.
	 * @return The current {@link #textProperty()} value.
	 */
	public final @NotNull Component getText() { return this.text.get(); }

	/**
	 * Convenience method for setting the value of {@link #textProperty()}.
	 * @param text The new {@link #textProperty()} value.
	 */
	public final void setText(@NotNull Component text) { this.text.set(text, TLabelElement.class); }
	// --------------------------------------------------
	/**
	 * Sets this {@link TLabelElement}'s bounds to fit its text, given a
	 * maximum width.
	 * @param maxWidth The maximum width the bounds can have.
	 */
	public final void setBoundsToFitText(int maxWidth) {
		final var bb = getBounds();
		setBoundsToFitText(bb.x, bb.y, maxWidth);
	}

	/**
	 * Sets this {@link TLabelElement}'s bounds to fit its text, given a
	 * maximum width.
	 * @param x The x position to set the bounds to.
	 * @param y The y position to set the bounds to.
	 * @param maxWidth The maximum width the bounds can have.
	 */
	public final void setBoundsToFitText(int x, int y, int maxWidth)
	{
		//obtain initial bounds for later calculation
		var bb = getBounds();
		//calculate new bounding box depending on value of word wrap property
		if(wrapTextProperty().getZ()) {
			//initial bounds are to be set - without invoking listeners at this moment.
			//we assign infinite height but limited width, to allow for text wrapping to take place
			boundsProperty().getHandle().set(bb = new Bounds2i(bb.x, bb.y, maxWidth, Integer.MAX_VALUE));
			//we then calculate and properly set the final height
			refresh();
			setBounds(x, y, bb.width, this.textHeight);
		} else {
			//initial bounds are to be set - without invoking listeners at this moment.
			//we assign infinite height but limited width, to allow for text wrapping to take place
			boundsProperty().getHandle().set(bb = new Bounds2i(bb.x, bb.y, Integer.MAX_VALUE, Integer.MAX_VALUE));
			//we then calculate and properly set the final height
			refresh();
			setBounds(x, y, this.textWidth, this.textHeight);
		}
		//bounds changed, so now it's dirty
		this.isDirty = true;
	}
	// --------------------------------------------------
	/**
	 * Returns the width this {@link TLabelElement} would need, to fit all
	 * lines of text it has. This accounts for {@link #textScaleProperty()}
	 * value as well.
	 * <p>
	 * Note: If you have {@link #wrapTextProperty()} enabled, this will likely
	 * return a value that's around the width of this {@link TLabelElement}.
	 * @apiNote Relies on this {@link TLabelElement}'s bounding box.
	 * @see TElement#getBounds()
	 */
	public final int getTextWidth() {
		if(this.isDirty) refresh(); //must be done to get the latest up-to-date answer
		return this.textWidth;
	}

	/**
	 * Returns the height this {@link TLabelElement} would need, to fit all
	 * lines of text it has. This accounts for {@link #textScaleProperty()}
	 * value as well.
	 * @apiNote Relies on this {@link TLabelElement}'s bounding box.
	 * @see TElement#getBounds()
	 */
	public final int getTextHeight() {
		if(this.isDirty) refresh(); //must be done to get the latest up-to-date answer
		return this.textHeight;
	}
	// ==================================================
}
