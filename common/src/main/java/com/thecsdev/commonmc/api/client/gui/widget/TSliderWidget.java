package com.thecsdev.commonmc.api.client.gui.widget;

import com.thecsdev.common.math.Bounds2i;
import com.thecsdev.common.math.Point2d;
import com.thecsdev.common.math.UDim2;
import com.thecsdev.common.properties.IChangeListener;
import com.thecsdev.common.properties.NotNullProperty;
import com.thecsdev.common.util.annotations.Virtual;
import com.thecsdev.commonmc.api.client.gui.render.TGuiGraphics;
import com.thecsdev.commonmc.api.client.gui.util.TInputContext;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import static com.thecsdev.commonmc.api.client.gui.util.TGuiUtils.playGuiButtonClickSound;
import static com.thecsdev.commonmc.api.client.gui.util.TInputContext.InputType.*;
import static java.lang.Math.*;
import static org.lwjgl.glfw.GLFW.*;

/**
 * A GUI slider that lets a user select a value by moving a handle along a track.
 * It's often used when a precise numerical input isn't required and a visual
 * representation of the value range is helpful.
 */
//TODO - Why are sliders so clunky? We need better sliders.
public @Virtual class TSliderWidget extends TButtonWidget
{
	// ================================================== ==================================================
	//                                      TSliderWidget IMPLEMENTATION
	// ================================================== ==================================================
	private final NotNullProperty<Point2d> value    = new NotNullProperty<>(Point2d.ZERO);
	private final NotNullProperty<UDim2>   knobSize = new NotNullProperty<>(UDim2.ZERO);
	// --------------------------------------------------
	@ApiStatus.Internal //for internally keeping track of the real knob size
	private final NotNullProperty<Bounds2i> knobBounds = new NotNullProperty<>(Bounds2i.ZERO);
	// ==================================================
	public TSliderWidget(@NotNull Point2d value) { this(); this.value.getHandle().set(value); }
	public @SuppressWarnings("unchecked") TSliderWidget()
	{
		//original click sound mechanism doesn't work properly here
		super.eClicked.unregister(ONCLICK_SOUND); //so we remove it
		pressedProperty().addChangeListener((p, o, n) -> {
			if(!n) playGuiButtonClickSound(); //this one works better here
		});

		//clamp value to [0 to 1] range
		this.value.addFilter(Point2d::clamp01, TSliderWidget.class);
		//updates to the value need to be reflected on the knob bounds
		//(set value to handle to avoid stack overflow from cyclic dependency)
		this.value.addChangeListener((p, o, n) -> this.knobBounds.getHandle().set(computeKnobFromValue()));

		//control the knob's bounds, such that it never leaves this slider
		this.knobBounds.addFilter(hbb -> {
			final var sbb  = TSliderWidget.this.getBounds();
			final int kbbW = clamp(hbb.width,  0, sbb.width);
			final int kbbH = clamp(hbb.height, 0, sbb.height);
			return new Bounds2i(clamp(hbb.x, sbb.x, sbb.endX - kbbW), clamp(hbb.y, sbb.y, sbb.endY - kbbH), kbbW, kbbH);
		}, TSliderWidget.class);
		//when the knob bounds update during click and drag, update the value of the slider
		this.knobBounds.addChangeListener((p, o, n) -> {
			//do not handle if not pressed or if resized
			if(!pressedProperty().getZ() || !o.hasSameSize(n)) return;
			//else set value
			this.value.set(computeValueFromKnob(), TSliderWidget.class);
		});

		//size changes also update the knob bounds size
		final IChangeListener<?> cl_rkbq = (p, o, n) -> refreshKnobQuietly();
		boundsProperty().addChangeListener((IChangeListener<Bounds2i>) cl_rkbq);
		this.knobSize   .addChangeListener((IChangeListener<UDim2>)    cl_rkbq);
		refreshKnobQuietly();
	}
	// --------------------------------------------------
	/**
	 * Recalculates the {@link #knobBounds} given the current
	 * {@link #knobSizeProperty()} and {@link #valueProperty()} values.
	 * <p>
	 * Does not invoke change listeners for {@link #knobBounds}. This is because
	 * invoking change listeners would affect the {@link #value}, and we do not
	 * wish to update the {@link #value} while resizing the knob.
	 */
	protected final @ApiStatus.Internal void refreshKnobQuietly() {
		final var sbb = getBounds();
		final var kbb = this.knobBounds.get();
		final var siz = this.knobSize.get();
		this.knobBounds.getHandle().set(new Bounds2i(
				kbb.x, kbb.y,
				min(max(siz.x.computeI(sbb.width), 10), sbb.width),
				min(max(siz.y.computeI(sbb.height), 10), sbb.height)
		));
		this.knobBounds.getHandle().set(computeKnobFromValue());
	}
	// ==================================================
	/**
	 * The {@link NotNullProperty} holding the value of this {@link TSliderWidget}.
	 */
	public final NotNullProperty<Point2d> valueProperty() { return this.value; }

	/**
	 * The {@link NotNullProperty} holding the XY size of the know, in {@code 0 to 1}
	 * number range. For example, a value of {@code [0.5, 0,5]} means the knob takes
	 * half the size of the {@link TSliderWidget} on both axis.
	 * @apiNote Set an axis to {@code 0} to use the minimum size, of around 10 units.
	 */
	public final NotNullProperty<UDim2> knobSizeProperty() { return this.knobSize; }
	// ==================================================
	/**
	 * Calculates the value the {@link TSliderWidget} should have given the
	 * bounding box of the slider's knob.
	 */
	@ApiStatus.Internal
	private final @NotNull Point2d computeValueFromKnob()
	{
		final var sbb = getBounds();
		final var kbb = this.knobBounds.get();
		final var val = this.value.get();
		final double cX = Math.max(kbb.x - sbb.x, 0),         cY = Math.max(kbb.y - sbb.y, 0);
		final double cW = Math.max(sbb.width - kbb.width, 1), cH = Math.max(sbb.height - kbb.height, 1);
		return new Point2d(
				//the width/height checks disable the sliding logic on a given axis if the
				//knob is too large to manipulate said axis.
				//for example, if the knob width matches slider width, there is no need
				//for the knob to control X scroll amount, hence that gets ignored
				(kbb.width  >= sbb.width)  ? val.x : (cX / cW),
				(kbb.height >= sbb.height) ? val.y : (cY / cH)
		);
	}

	/**
	 * Calculates the bounding box the slider's knob should have, given the
	 * value of the {@link #valueProperty()}.
	 */
	@ApiStatus.Internal
	private final @NotNull Bounds2i computeKnobFromValue()
	{
		final var val = this.value.get();
		final var sbb = getBounds();
		final var kbb = this.knobBounds.get();
		final int cW  = sbb.width - kbb.width, cH = sbb.height - kbb.height;
		final int cX  = sbb.x + (int)(cW * val.x),  cY = sbb.y + (int)(cH * val.y);
		return new Bounds2i(cX, cY, kbb.width, kbb.height);
	}
	// ==================================================
	public final @Override void renderCallback(@NotNull TGuiGraphics pencil) {
		renderBackground(pencil);
		renderKnob(pencil, this.knobBounds.get());
	}
	public @Virtual @Override void postRenderCallback(@NotNull TGuiGraphics pencil) {}

	/**
	 * Renders the background of this {@link TSliderWidget}.
	 */
	public @Virtual void renderBackground(@NotNull TGuiGraphics pencil) {
		final var sbb = getBounds();
		pencil.drawButton(sbb.x, sbb.y, sbb.width, sbb.height, 0xFFFFFFFF, false, false);
	}

	/**
	 * Renders the knob of this {@link TSliderWidget}.
	 */
	public @Virtual void renderKnob(@NotNull TGuiGraphics pencil, @NotNull Bounds2i kbb) {
		final var enabled     = enabledProperty().getZ();
		final var highlighted = isHoveredOrFocused();
		final var bb          = getBounds();
		pencil.drawButton(bb.x, bb.y, kbb.endX - bb.x, kbb.endY - bb.y, 0x75505050, enabled, highlighted);
		pencil.drawButton(kbb.x, kbb.y, kbb.width, kbb.height, 0xFFFFFFFF, enabled, highlighted);
	}
	// --------------------------------------------------
	/**
	 * {@inheritDoc}
	 * @apiNote <b>Do not override</b>, as this implementation uses internal variables.
	 */
	@ApiStatus.NonExtendable
	@SuppressWarnings("DataFlowIssue")
	public @Override boolean inputCallback(@NotNull TInputContext.InputDiscoveryPhase phase, @NotNull TInputContext context)
	{
		//handle super, so it can do input stuff
		if(super.inputCallback(phase, context)) return true;
		//only handle the main phase, and only when is focusable
		else if(phase != TInputContext.InputDiscoveryPhase.MAIN || !isFocusable())
			return false;

		//handle click and dragging
		//(must return true for both drag and mouse press)
		if(context.getInputType() == MOUSE_DRAG && pressedProperty().getZ() && context.getMouseButton() == 0) {
			//move knob to cursor position
			final var kbb = this.knobBounds.get();
			final int w2  = kbb.width / 2, h2 = kbb.height / 2;
			this.knobBounds.set(new Bounds2i(
					(int) (context.getMouseX() - w2), (int) (context.getMouseY() - h2),
					kbb.width, kbb.height),
				TSliderWidget.class);
			//return true to indicate handled input
			return true;
		}
		//handle value adjustments via keyboard input (arrow keys)
		else if(context.getInputType() == KEY_PRESS)
		{
			final double sensitivity = 0.05;
			double dX = 0, dY = 0;
			switch(context.getKeyCode()) {
				case GLFW_KEY_LEFT:  dX -= sensitivity; break;
				case GLFW_KEY_RIGHT: dX += sensitivity; break;
				case GLFW_KEY_UP:    dY -= sensitivity; break;
				case GLFW_KEY_DOWN:  dY += sensitivity; break;
				default: break;
			}
			if(dX != 0 || dY != 0) {
				this.value.set(this.value.get().add(dX, dY), TSliderWidget.class);
				return true;
			}
		}
		//handle value adjustments via scroll-wheel input
		else if(context.getInputType() == MOUSE_SCROLL) {
			//NOTE - Scrolling a slider widget needs further testing and refining
			final double sensitivity = 0.05;
			double dX = sensitivity * context.getScrollX(), dY = -sensitivity * context.getScrollY();
			if(dX != 0 || dY != 0) {
				this.value.set(this.value.get().add(dX, dY), TSliderWidget.class);
				return true;
			}
		}

		//return false if nothing happened
		return false;
	}
	// ================================================== ==================================================
	//                                               Flat IMPLEMENTATION
	// ================================================== ==================================================
	/**
	 * {@link TSliderWidget} whose visual appearance is more flat and does not
	 * use the vanilla button texture.
	 */
	public static @Virtual class Flat extends TSliderWidget
	{
		public final @Override void renderBackground(@NotNull TGuiGraphics pencil) {
			final var bb = getBounds();
			pencil.fillColor(bb.x, bb.y, bb.width, bb.height, 1342177280);
			pencil.drawOutlineIn(bb.x, bb.y, bb.width, bb.height, 0xFF000000);
		}
		public final @Override void renderKnob(@NotNull TGuiGraphics pencil, @NotNull Bounds2i kbb) {
			pencil.fillColor(kbb.x, kbb.y, kbb.width, kbb.height, isHoveredOrFocused() ? 1862270975 : 855638015);
		}
	}
	// ================================================== ==================================================
}
