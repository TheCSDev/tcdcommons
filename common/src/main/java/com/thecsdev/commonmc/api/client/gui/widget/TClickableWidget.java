package com.thecsdev.commonmc.api.client.gui.widget;

import com.thecsdev.common.properties.BooleanProperty;
import com.thecsdev.common.util.annotations.Virtual;
import com.thecsdev.commonmc.api.client.gui.TElement;
import com.thecsdev.commonmc.api.client.gui.render.TGuiGraphics;
import com.thecsdev.commonmc.api.client.gui.util.CursorType;
import com.thecsdev.commonmc.api.client.gui.util.TGuiUtils;
import com.thecsdev.commonmc.api.client.gui.util.TInputContext;
import com.thecsdev.commonmc.api.client.gui.util.TInputContext.InputDiscoveryPhase;
import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

import static com.mojang.blaze3d.platform.InputConstants.KEY_NUMPADENTER;
import static com.mojang.blaze3d.platform.InputConstants.KEY_RETURN;
import static com.thecsdev.commonmc.api.client.gui.util.TGuiUtils.playGuiButtonClickSound;

/**
 * A {@link TElement} that can be clicked via mouse or keyboard inputs.
 */
public @Virtual class TClickableWidget extends TElement
{
	// ==================================================
	private final BooleanProperty enabled = new BooleanProperty(true);
	private final BooleanProperty pressed = new BooleanProperty(false);
	// ==================================================
	/**
	 * An {@link Event} that is invoked whenever this {@link TClickableWidget}
	 * is clicked. Usually via {@code LMB} or the {@code Enter} key.
	 * <p>
	 * The {@link Consumer} provides an instance of {@code this} for convenience.
	 */
	public final Event<Consumer<TClickableWidget>> eClicked = EventFactory.createLoop();

	/**
	 * Default event handler that plays a GUI click sound whenever this
	 * {@link TClickableWidget} is clicked. If you do not prefer this, you may
	 * remove this handler via {@link Event#unregister(Object)}.
	 * @see #eClicked
	 */
	protected static final Consumer<TClickableWidget> ONCLICK_SOUND = (__) -> playGuiButtonClickSound();
	// ==================================================
	public TClickableWidget() {
		super();
		super.focusableProperty().set(true, TClickableWidget.class);
		this.eClicked.register(ONCLICK_SOUND);
	}
	// ==================================================
	/**
	 * Returns a {@link BooleanProperty} that holds a {@link Boolean} value representing
	 * the "enabled" state of this {@link TClickableWidget}. When "disabled", this
	 * {@link TClickableWidget} will not handle click-related inputs.
	 */
	public final BooleanProperty enabledProperty() { return this.enabled; }
	
	/**
	 * Returns a {@link BooleanProperty} that holds a {@link Boolean} value representing
	 * whether this {@link TClickableWidget} is currently being pressed.
	 */
	public final BooleanProperty pressedProperty() { return this.pressed; }
	// ==================================================
	/**
	 * Calls {@link #clickCallback()} and invokes the {@link #eClicked} event.
	 */
	public final void click() { clickCallback(); this.eClicked.invoker().accept(this); }

	/**
	 * Callback method that is invoked whenever this {@link TClickableWidget} is clicked.
	 * @apiNote Remember to call {@code super} when overriding this method!
	 */
	protected @Virtual void clickCallback() {}
	// ==================================================
	public @Virtual @Override @NotNull CursorType getCursor() {
		return isFocusable() ? CursorType.POINTING_HAND : CursorType.NOT_ALLOWED;
	}
	// ==================================================
	public @Virtual @Override boolean isFocusable() { return super.isFocusable() && this.enabled.getZ(); }
	// --------------------------------------------------
	//play a gui sound on hover for aesthetics
	protected @Virtual @Override void hoverGainedCallback() { if(isFocusable()) TGuiUtils.playGuiHoverSound(); }
	//'pressed' state control
	protected @Virtual @Override void dragStartCallback() { this.pressed.set(true, TClickableWidget.class); }
	protected @Virtual @Override void dragEndCallback() { this.pressed.set(false, TClickableWidget.class); }
	protected @Virtual @Override void focusLostCallback() { this.pressed.set(false, TClickableWidget.class); }
	// --------------------------------------------------
	@SuppressWarnings("DataFlowIssue")
	public @Virtual @Override boolean inputCallback(@NotNull InputDiscoveryPhase phase, @NotNull TInputContext context)
	{
		//this element will capture inputs only on the main phase
		//and will not handle clicks when disabled
		if(phase != InputDiscoveryPhase.MAIN || !isFocusable())
			return false;

		//handle inputs
		switch(context.getInputType())
		{
			//mouse-based inputs
			case MOUSE_PRESS:
				if(context.getMouseButton() != 0) break; //only accept LMB
				return true;
			case MOUSE_RELEASE:
				if(context.getMouseButton() != 0) break; //only accept LMB
				if(isHovered()) click(); //just like in standardized GUI frameworks
				return true;

			//key-based inputs
			case KEY_PRESS: {
				final int kc = context.getKeyCode();
				if(!(kc == KEY_RETURN || kc == KEY_NUMPADENTER)) break;
				this.pressed.set(true, TClickableWidget.class);
				click();
				return true;
			}
			case KEY_RELEASE: {
				final int kc = context.getKeyCode();
				if(!(kc == KEY_RETURN || kc == KEY_NUMPADENTER)) break;
				this.pressed.set(false, TClickableWidget.class);
				return true;
			}

			//ignore everything else
			default: break;
		}
		return false;
	}
	// --------------------------------------------------
	public @Virtual @Override void postRenderCallback(@NotNull TGuiGraphics pencil) {
		//draw the press-highlight, based on the pressed state
		final var bb = getBounds();
		if(pressedProperty().get())
			pencil.fillColor(bb.x, bb.y, bb.width, bb.height, 0x33ffffff);
	}
	// ==================================================
}
