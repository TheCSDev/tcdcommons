package com.thecsdev.commonmc.api.client.gui.widget;

import com.thecsdev.common.math.Bounds2i;
import com.thecsdev.common.math.Point2d;
import com.thecsdev.common.properties.IChangeListener;
import com.thecsdev.common.properties.NotNullProperty;
import com.thecsdev.common.properties.ObjectProperty;
import com.thecsdev.common.util.annotations.Virtual;
import com.thecsdev.commonmc.api.client.gui.TElement;
import com.thecsdev.commonmc.api.client.gui.panel.TPanelElement;
import com.thecsdev.commonmc.api.client.gui.render.TGuiGraphics;
import com.thecsdev.commonmc.api.client.gui.util.TInputContext;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

import static com.thecsdev.common.math.UDim2.fromScale;
import static com.thecsdev.commonmc.api.client.gui.util.TInputContext.InputDiscoveryPhase.MAIN;
import static com.thecsdev.commonmc.api.client.gui.util.TInputContext.InputType.KEY_PRESS;
import static com.thecsdev.commonmc.api.client.gui.util.TInputContext.InputType.MOUSE_SCROLL;

/**
 * {@link TSliderWidget} acting as a scroll-bar for a given {@link TPanelElement}.
 */
//TODO - Resolve cyclic dependency with the scrollAmount<->value properties
public @Virtual class TScrollBarWidget extends TSliderWidget
{
	// ================================================== ==================================================
	//                                   TScrollBarWidget IMPLEMENTATION
	// ================================================== ==================================================
	private final ObjectProperty<TPanelElement>    panel     = new ObjectProperty<>(null);
	private final NotNullProperty<ScrollDirection> direction = new NotNullProperty<>(ScrollDirection.BIAXIAL);
	// ==================================================
	public TScrollBarWidget()
	{
		//knob size will be controlled manually. therefore, assume control over it
		knobSizeProperty().setReadOnly(true, TScrollBarWidget.class);
		knobSizeProperty().setOwner(TScrollBarWidget.class, TScrollBarWidget.class);

		//value should be filtered depending on scroll direction
		//(use value 0 for axes not targeted by the scroll direction)
		valueProperty().addFilter(in -> switch(this.direction.get()) {
			case HORIZONTAL -> new Point2d(in.x, 0);
			case VERTICAL -> new Point2d(0, in.y);
			default -> in;
		}, TScrollBarWidget.class);
		//changes to the slider value scrolls the panel
		valueProperty().addChangeListener((p, o, n) -> {
			//ensure the target panel is present
			final @Nullable var panel = this.panel.get();
			if(panel == null) return;
			//obtain the panel's current scroll amount, and then update
			//it based on the current scroll direction
			final var panelScrollAmount = panel.scrollAmountProperty().get();
			panel.scrollAmountProperty().set(switch(this.direction.get()) {
				case HORIZONTAL -> new Point2d(n.x, panelScrollAmount.y);
				case VERTICAL -> new Point2d(panelScrollAmount.x, n.y);
				default -> n;
			}, TScrollBarWidget.class);
		});

		//handle automatic refreshing
		final Consumer<TElement>       onPanelInit              = __        -> refresh();
		final IChangeListener<Point2d> onPanelScrollValueChange = (p, o, n) -> _refreshValue();
		this.panel.addChangeListener((p, o, n) -> {
			if(o != null) {
				o.eInitialized.unregister(onPanelInit);
				o.scrollAmountProperty().removeChangeListener(onPanelScrollValueChange);
			}
			if(n != null) {
				n.eInitialized.register(onPanelInit);
				n.scrollAmountProperty().addChangeListener(onPanelScrollValueChange);
			}
		});
	}

	public TScrollBarWidget(@NotNull TPanelElement panel) {
		this();
		this.panel.set(panel, TScrollBarWidget.class);
	}

	public TScrollBarWidget(@NotNull TPanelElement panel, @NotNull ScrollDirection direction) {
		this();
		this.direction.set(direction, TScrollBarWidget.class);
		this.panel.set(panel, TScrollBarWidget.class);
	}
	// ==================================================
	/**
	 * Manually refreshes the value and knob side of this {@link TScrollBarWidget}.
	 * @apiNote Automatically called whenever the target panel (re/)initializes.
	 * @see #clearAndInit()
	 */
	public final void refresh() { _refreshValue(); _refreshKnobSize(); }

	/**
	 * Updates this {@link TScrollBarWidget}'s slider value whenever the panel's
	 * scroll values update.
	 */
	private final @ApiStatus.Internal void _refreshValue() {
		//do not do this while the user is currently using the slider (mouse input)
		if(pressedProperty().getZ()) return;
		//do not do this if not assigned to a panel
		final @Nullable var panel = this.panel.get();
		if(panel == null) return;
		//refresh the value and the knob
		this.valueProperty().getHandle().set(
				this.valueProperty().applyFilters(panel.scrollAmountProperty().get()));
		refreshKnobQuietly();
	}

	/**
	 * Updates this {@link TScrollBarWidget}'s knob size to reflect the viewport
	 * of the target {@link TPanelElement}.
	 */
	private final @ApiStatus.Internal void _refreshKnobSize() {
		//do not do this if not assigned to a panel
		final @Nullable var panel = this.panel.get();
		if(panel == null) return;
		//obtain the bounding boxes of the panel and its contents
		final var pbb  = panel.getBounds();
		final var pcbb = panel.getContentBounds();
		knobSizeProperty().set(switch(this.direction.get()) {
			case HORIZONTAL -> fromScale((double) pbb.width / pcbb.width, 1);
			case VERTICAL   -> fromScale(1, (double) pbb.height / pcbb.height);
			case BIAXIAL    -> fromScale((double) pbb.width / pcbb.width, (double) pbb.height / pcbb.height);
		}, TScrollBarWidget.class);
	}
	// ==================================================
	/**
	 * The {@link ObjectProperty} holding the {@link TPanelElement} this
	 * {@link TSliderWidget} is bound to.
	 */
	public final ObjectProperty<@Nullable TPanelElement> panelProperty() { return this.panel; }

	/**
	 * The {@link NotNullProperty} holding the {@link ScrollDirection} of this
	 * {@link TScrollBarWidget}. The scroll direction dictates the scroll axis this
	 * scroll-bar widget controls.
	 */
	public final NotNullProperty<ScrollDirection> scrollDirectionProperty() { return this.direction; }
	// ==================================================
	public final @Override boolean inputCallback(TInputContext.@NotNull InputDiscoveryPhase phase, @NotNull TInputContext context)
	{
		//some inputs should be forwarded to the panel
		if(phase == MAIN && (context.getInputType() == MOUSE_SCROLL || context.getInputType() == KEY_PRESS)) {
			final @Nullable var panel = this.panel.get();
			if(panel != null) return panel.inputCallback(phase, context);
		}
		//all other input goes to the slider
		return super.inputCallback(phase, context);
	}
	// ================================================== ==================================================
	//                                               Flat IMPLEMENTATION
	// ================================================== ==================================================
	/**
	 * {@link TScrollBarWidget} whose visual appearance is more flat and does not
	 * use the vanilla button texture.
	 */
	public static @Virtual class Flat extends TScrollBarWidget
	{
		// ==================================================
		public Flat() { super(); }
		public Flat(@NotNull TPanelElement panel) { super(panel); }
		public Flat(@NotNull TPanelElement panel, @NotNull ScrollDirection direction) { super(panel, direction); }
		// ==================================================
		public @Virtual @Override void renderBackground(@NotNull TGuiGraphics pencil) {
			final var bb = getBounds();
			pencil.fillColor(bb.x, bb.y, bb.width, bb.height, 1342177280);
			pencil.drawOutlineIn(bb.x, bb.y, bb.width, bb.height, 0xFF000000);
		}
		public @Virtual @Override void renderKnob(@NotNull TGuiGraphics pencil, @NotNull Bounds2i kbb) {
			pencil.fillColor(kbb.x, kbb.y, kbb.width, kbb.height, isHoveredOrFocused() ? 0x6EFFFFFF : 0x42FFFFFF);
		}
		// ==================================================
	}
	// ================================================== ==================================================
	//                                    ScrollDirection IMPLEMENTATION
	// ================================================== ==================================================
	/**
	 * Represents an axis/scroll-direction a given {@link TScrollBarWidget} controls.
	 */
	public static enum ScrollDirection { HORIZONTAL, VERTICAL, BIAXIAL }
	// ================================================== ==================================================
}
