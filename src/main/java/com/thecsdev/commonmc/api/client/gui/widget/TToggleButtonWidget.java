package com.thecsdev.commonmc.api.client.gui.widget;

import com.thecsdev.common.properties.BooleanProperty;
import com.thecsdev.common.util.annotations.Virtual;
import com.thecsdev.commonmc.api.client.gui.render.TGuiGraphics;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Similar to a {@link TCheckboxWidget} in that it holds a {@code boolean} value,
 * except this is a "togglable" {@link TButtonWidget}.
 */
public @Virtual class TToggleButtonWidget extends TButtonWidget
{
	// ==================================================
	private final BooleanProperty toggled = new BooleanProperty(false);
	// ==================================================
	/**
	 * Event handler for {@link #eClicked} that flips the value of
	 * {@link #toggledProperty()} when clicked.
	 */
	private static final Consumer<TClickableWidget> ONCLICK_TOGGLE = btn -> {
		if(btn instanceof TToggleButtonWidget btnToggle)
			btnToggle.toggled.toggle();
	};
	// ==================================================
	public TToggleButtonWidget() { super.eClicked.addListener(ONCLICK_TOGGLE); }
	// ==================================================
	/**
	 * Holds the value of this {@link TToggleButtonWidget}.
	 */
	public final BooleanProperty toggledProperty() { return toggled; }
	// ==================================================
	public @Virtual @Override void renderCallback(@NotNull TGuiGraphics pencil) {
		final var bb = getBounds();
		pencil.drawToggleButton(
				bb.x, bb.y, bb.width, bb.height, -1,
				isFocusable(), isHoveredOrFocused(),
				this.toggled.getZ());
	}
	// ==================================================
}
