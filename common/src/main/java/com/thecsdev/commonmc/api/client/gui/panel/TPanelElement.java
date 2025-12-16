package com.thecsdev.commonmc.api.client.gui.panel;

import com.thecsdev.common.math.Bounds2i;
import com.thecsdev.common.math.Point2d;
import com.thecsdev.common.properties.IntegerProperty;
import com.thecsdev.common.properties.NotNullProperty;
import com.thecsdev.common.util.annotations.Virtual;
import com.thecsdev.commonmc.api.client.gui.TElement;
import com.thecsdev.commonmc.api.client.gui.render.TGuiGraphics;
import com.thecsdev.commonmc.api.client.gui.util.TInputContext;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

/**
 * A panel element primarily featuring functionality like scrolling.
 */
public @Virtual class TPanelElement extends TElement
{
	// ================================================== ==================================================
	//                                      TPanelElement IMPLEMENTATION
	// ================================================== ==================================================
	//default colors for background and outlines
	public static final int COLOR_BACKGROUND      = 0x50000000;
	public static final int COLOR_OUTLINE         = 0x50FFFFFF;
	public static final int COLOR_OUTLINE_FOCUSED = 0xFFAAFFFF;
	// --------------------------------------------------
	private final NotNullProperty<Point2d> scrollAmount      = new NotNullProperty<>(Point2d.ZERO);
	private final IntegerProperty          scrollPadding     = new IntegerProperty(0);
	private final IntegerProperty          scrollSensitivity = new IntegerProperty(30);
	// ==================================================
	public TPanelElement()
	{
		//this element should be focusable
		focusableProperty().set(true, TPanelElement.class);

		//clamp01 the scroll amount
		this.scrollAmount.addFilter(Point2d::clamp01, TPanelElement.class);
		this.scrollAmount.addChangeListener((p, o, n) -> {
			final var ccb = getContentBounds();
			final var ncb = computeContentBoundsFromScrollAmount();
			moveChildren(ncb.x - ccb.x, ncb.y - ccb.y);
		});
	}
	// ==================================================
	/**
	 * The {@link NotNullProperty} representing the two-dimensional
	 * scroll amount value, with each axis ranging {@code 0 to 1}.
	 */
	public NotNullProperty<Point2d> scrollAmountProperty() { return this.scrollAmount; }

	/**
	 * The {@link IntegerProperty} holding the "padding" of this {@link TPanelElement}.
	 */
	public final IntegerProperty scrollPaddingProperty() { return this.scrollPadding; }

	/**
	 * The {@link IntegerProperty} for this {@link TPanelElement}'s sensitivity
	 * of scrolling done by keyboard and mouse input.
	 */
	public final IntegerProperty scrollSensitivityProperty() { return this.scrollSensitivity; }
	// ==================================================
	public @Virtual @Override void renderCallback(@NotNull TGuiGraphics pencil) {
		final var bb = getBounds();
		pencil.fillColor(bb.x, bb.y, bb.width, bb.height, COLOR_BACKGROUND);
	}

	public @Virtual @Override void postRenderCallback(@NotNull TGuiGraphics pencil) {
		final var bb = getBounds();
		if(isFocused()) pencil.drawOutlineIn(bb.x, bb.y, bb.width, bb.height, COLOR_OUTLINE_FOCUSED);
		else            pencil.drawOutlineIn(bb.x, bb.y, bb.width, bb.height, COLOR_OUTLINE);
	}

	private double mouseDragX = 0, mouseDragY = 0;
	@SuppressWarnings("DataFlowIssue")
	public @Virtual @Override boolean inputCallback(TInputContext.@NotNull InputDiscoveryPhase phase, @NotNull TInputContext context)
	{
		//only handle the main discovery phase
		if(phase != TInputContext.InputDiscoveryPhase.MAIN) return false;

		//handle based on input type
		switch(context.getInputType())
		{
			//mouse press should result in focus, so we return true here
			case MOUSE_PRESS: return (context.getMouseButton() == 0 || context.getMouseButton() == 2) && isFocusable();
			//mouse scroll should result in scrolling
			case MOUSE_SCROLL: {
				final int s = this.scrollSensitivity.getI();
				//noinspection DataFlowIssue
				scroll((int) (-context.getScrollX() * s), (int) (context.getScrollY() * s));
				return true;
			}
			//handle mouse dragging
			case MOUSE_DRAG:
			{
				//keep track of mouse delta (in decimal)
				this.mouseDragX += context.getMouseDeltaX();
				this.mouseDragY += context.getMouseDeltaY();

				//convert decimal to whole, and see if there's movement to be handled
				int dX = (int) mouseDragX, dY = (int) mouseDragY; //int cast discards the decimal fragments
				if(dX == 0 && dY == 0) return false;

				//there's some movement to be made, scroll this panel and return
				this.mouseDragX -= dX; this.mouseDragY -= dY;
				scroll(dX, dY); return true;
			}
			case MOUSE_RELEASE: { this.mouseDragX = this.mouseDragY = 0; return true; }
			//and pressing arrow keys should also scroll
			case KEY_PRESS:
			{
				//only handle if explicily focused (aka focus isn't on a child)
				if(!isFocused()) break;
				final int s = this.scrollSensitivity.getI();
				int dX = 0, dY = 0;
				//noinspection DataFlowIssue
				switch(context.getKeyCode()) {
					case GLFW.GLFW_KEY_UP:    dY += s; break;
					case GLFW.GLFW_KEY_DOWN:  dY -= s; break;
					case GLFW.GLFW_KEY_LEFT:  dX += s; break;
					case GLFW.GLFW_KEY_RIGHT: dX -= s; break;
					default: break;
				}
				if(dX != 0 || dY != 0) { scroll(dX, dY); return true; }
				else break;
			}
			default: break;
		}

		//return false if nothing handles the input
		return false;
	}
	// ==================================================
	/**
	 * Calculates the value that the {@link #getContentBounds()} should have given
	 * the value of {@link #scrollAmountProperty()}.
	 * @author <a href="https://gemini.google.com/">Google Gemini</a>
	 */
	private final Bounds2i computeContentBoundsFromScrollAmount()
	{
		//Point2d - constructor: new Point2d(double x, double y) - fields: .x .y (doubles)
		//Bounds2i fields: .x .y .width .height .endX .endY (integers)
		final Bounds2i bb  = getBounds(); //current rectangle of this element (viewport)
		final Bounds2i cbb = getContentBounds(); //current rectangle encapsulating all children bounding boxes
		final Point2d  sa  = this.scrollAmount.get();
		final int      sp  = this.scrollPadding.getI(); // NEW: Scroll Padding

		// Calculate the effective maximum scrollable distance for each axis.
		// The scroll range is reduced by 2 * sp.
		// We clamp to 0 because if content is smaller than viewport, no scroll is possible,
		// and padding doesn't apply to the scroll range itself, only the resulting position.
		// The effective viewport size is (bb.width - 2*sp), but that's complex.
		// Simpler: The max scroll is Content_Size - Padded_Viewport_Size.
		final double maxScrollX = Math.max(0, cbb.width - (bb.width - 2 * sp));
		final double maxScrollY = Math.max(0, cbb.height - (bb.height - 2 * sp));

		// The current scroll offset is (sa * maxScroll).
		// The new content position is (Viewport Origin + Scroll Padding) - (Scroll Offset).
		//
		// At sa.x = 0.0: newX = (bb.x + sp) - 0. Content's left edge is at (bb.x + sp).
		// At sa.x = 1.0: newX = (bb.x + sp) - maxScrollX.
		//  If content is exactly scrollable (cbb.width = bb.width): maxScrollX=0.
		//   newX = bb.x + sp.
		//  If content is scrollable (cbb.width > bb.width - 2*sp): maxScrollX > 0.
		//   newX = bb.x + sp - (cbb.width - bb.width + 2*sp)
		//        = bb.x + sp - cbb.width + bb.width - 2*sp
		//        = bb.x + bb.width - cbb.width - sp
		//   Content's right edge (newX + cbb.width) = bb.x + bb.width - sp (end of viewport minus padding)
		final int newX = (int) (bb.x + sp - (sa.x * maxScrollX));
		final int newY = (int) (bb.y + sp - (sa.y * maxScrollY));

		// Create a new Bounds2i with the calculated position, keeping the original size.
		return new Bounds2i(newX, newY, cbb.width, cbb.height);
	}

	/**
	 * Calculates the value that {@link #scrollAmountProperty()} should have given
	 * the value of {@link #getContentBounds()}.
	 * @author <a href="https://gemini.google.com/">Google Gemini</a>
	 */
	private final Point2d computeScrollAmountFromContentBounds()
	{
		//Point2d - constructor: new Point2d(double x, double y) - fields: .x .y (doubles)
		//Bounds2i fields: .x .y .width .height .endX .endY (integers)
		final Bounds2i bb  = getBounds(); //rectangle of this element (viewport)
		final Bounds2i cbb = getContentBounds(); //rectangle encapsulating all children bounding boxes
		final int      sp  = this.scrollPadding.getI(); // NEW: Scroll Padding

		// 1. Calculate the effective maximum scrollable distance (delta)
		// The scrollable range is reduced by 2 * sp.
		final double maxScrollX = Math.max(0, cbb.width - (bb.width - 2 * sp));
		final double maxScrollY = Math.max(0, cbb.height - (bb.height - 2 * sp));

		// 2. Calculate the current effective scroll offset
		// Effective Offset = (Padded Viewport Origin) - (Content Origin)
		// Note: Padded Viewport Origin = bb.x + sp
		// Offset = (Viewport Origin + sp) - (Content Origin)
		final double offsetX = (bb.x + sp) - cbb.x;
		final double offsetY = (bb.y + sp) - cbb.y;

		double scrollAmountX;
		if (maxScrollX > 0) {
		   // sa = Offset / MaxScroll.
		   scrollAmountX = offsetX / maxScrollX;
		} else {
		   // Content fits or is smaller than viewport, so scroll is 0.
		   scrollAmountX = 0.0;
		}

		double scrollAmountY;
		if (maxScrollY > 0) {
		   scrollAmountY = offsetY / maxScrollY;
		} else {
		   scrollAmountY = 0.0;
		}

		// The scroll amount should be between 0 and 1.
		// We ensure this using Math.min/max, just in case of floating point inaccuracies or external manipulation.
		final double clampedX = Math.max(0.0, Math.min(1.0, scrollAmountX));
		final double clampedY = Math.max(0.0, Math.min(1.0, scrollAmountY));

		return new Point2d(clampedX, clampedY);
	}
	// ==================================================
	/**
	 * Scrolls this {@link TPanelElement} by moving the children via
	 * {@link #moveChildren(int, int)}.
	 * @param deltaX The X scroll amount, in in-game on-screen units.
	 * @param deltaY The Y scroll amount, in in-game on-screen units.
	 * @apiNote  Note that this method differs from {@link #moveChildren(int, int)}
	 * in that it also affects the value of {@link #scrollAmountProperty()}.
	 */
	public final void scroll(int deltaX, int deltaY) {
		//move children and then a super lazy unoptimized way to correct overshot
		//movements cuz i don't feel like doing actual maths here
		moveChildren(deltaX, deltaY);
		//TODO - A more optimized scroll(int, int) logic is needed
		this.scrollAmount.set(computeScrollAmountFromContentBounds(), TPanelElement.class);
		final var old_cbb = getContentBounds();
		final var new_cbb = computeContentBoundsFromScrollAmount();
		moveChildren(new_cbb.x - old_cbb.x, new_cbb.y - old_cbb.y);
	}
	// --------------------------------------------------
	/**
	 * Computes the bounds for the next element to be added vertically,
	 * given the specified height and gap.
	 * @param height The height of the next element.
	 * @param gap The vertical gap between the last element and the next one.
	 * @return A {@link Bounds2i} representing the position and size of the next element.
	 */
	public final @NotNull Bounds2i computeNextYBounds(int height, int gap) {
		final var bb  = getBounds();
		final var cbb = getContentBounds();
		final int pad = scrollPaddingProperty().getI();
		return new Bounds2i(
				bb.x + pad, isEmpty() ? (bb.y + pad) : (cbb.endY + gap),
				bb.width - (pad * 2), height);
	}

	/**
	 * Adds all specified elements vertically with the given gap between each element.
	 * Each added element is resized horizontally to fit the {@link TPanelElement}'s
	 * viewport width.
	 * @param elements The elements to add.
	 * @param gap The vertical gap between each element.
	 * @throws NullPointerException The argument is {@code null}.
	 */
	public final void addAllVertically(@NotNull Iterable<TElement> elements, int gap) throws NullPointerException {
		for(final var el : elements) {
			el.setBounds(computeNextYBounds(el.getBounds().height, gap));
			add(el);
		}
	}
	// ================================================== ==================================================
	//                                        Transparent IMPLEMENTATION
	// ================================================== ==================================================
	/**
	 * {@link TPanelElement} implementation that does not render any of its own visual,
	 * as in, has no background color or outlines.
	 */
	public static @Virtual class Transparent extends TPanelElement {
		public @Virtual @Override void postRenderCallback(@NotNull TGuiGraphics pencil) {}
		public @Virtual @Override void renderCallback(@NotNull TGuiGraphics pencil) {}
	}
	// ================================================== ==================================================
	//                                          Paintable IMPLEMENTATION
	// ================================================== ==================================================
	/**
	 * {@link TPanelElement} implementation that allows you to customize its background
	 * and outline colors.
	 */
	public static @Virtual class Paintable extends TPanelElement
	{
		// ==================================================
		private final IntegerProperty background     = new IntegerProperty(COLOR_BACKGROUND);
		private final IntegerProperty outline        = new IntegerProperty(COLOR_OUTLINE);
		private final IntegerProperty outlineFocused = new IntegerProperty(COLOR_OUTLINE_FOCUSED);
		// ==================================================
		public Paintable() {}
		public Paintable(int backgroundColor, int outlineColor, int focusOutlineColor) {
			this.background.set(backgroundColor, Paintable.class);
			this.outline.set(outlineColor, Paintable.class);
			this.outlineFocused.set(focusOutlineColor, Paintable.class);
		}
		// ==================================================
		/**
		 * {@link IntegerProperty} for the background color of this
		 * {@link Paintable} panel.
		 */
		public final IntegerProperty backgroundColorProperty() { return this.background; }

		/**
		 * {@link IntegerProperty} for the outline color of this {@link Paintable} panel.
		 */
		public final IntegerProperty outlineColorProperty() { return this.outline; }

		/**
		 * {@link IntegerProperty} for the outline color of this {@link Paintable} panel
		 * when this panel {@link #isFocused()}.
		 */
		public final IntegerProperty focusedOutlineColorProperty() { return this.outlineFocused; }
		// ==================================================
		public @Virtual @Override void renderCallback(@NotNull TGuiGraphics pencil) {
			final var bb = getBounds();
			pencil.fillColor(bb.x, bb.y, bb.width, bb.height, this.background.getI());
		}

		public @Virtual @Override void postRenderCallback(@NotNull TGuiGraphics pencil) {
			final var bb = getBounds();
			pencil.drawOutlineIn(
					bb.x, bb.y, bb.width, bb.height,
					isFocused() ? this.outlineFocused.getI() : this.outline.getI());
		}
		// ==================================================
	}
	// ================================================== ==================================================
}
