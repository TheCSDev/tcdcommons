package com.thecsdev.commonmc.api.client.gui.widget;

import com.thecsdev.common.properties.BooleanProperty;
import com.thecsdev.common.util.annotations.Virtual;
import com.thecsdev.commonmc.api.client.gui.render.TGuiGraphics;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link TClickableWidget} implementation that looks and behaves
 * like a regular checkbox you'd see in-game.
 */
public @Virtual class TCheckboxWidget extends TClickableWidget
{
	// ==================================================
	private final BooleanProperty checked = new BooleanProperty(false);
	// ==================================================
	public TCheckboxWidget(boolean checked) { this(); this.checked.getHandle().set(checked); }
	public TCheckboxWidget()
	{
		//play click sound and toggle value on click
		super.eClicked.register(__ -> {
			this.checked.set(!this.checked.get(), TCheckboxWidget.class); //toggle checkbox value
		});
	}
	// ==================================================
	/**
	 * Returns the {@link BooleanProperty} that holds this
	 * {@link TCheckboxWidget}'s "checked" value.
	 */
	public final BooleanProperty checkedProperty() { return this.checked; }
	// ==================================================
	public @Virtual @Override void renderCallback(@NotNull TGuiGraphics pencil)
	{
		//draw the checkbox texture, based on the checkbox's state
		final var bb = getBounds();
		pencil.drawCheckbox(
				bb.x, bb.y, bb.width, bb.height, -1,
				enabledProperty().getZ(), isHoveredOrFocused(), checkedProperty().getZ());

		//draw the press-highlight, based on the pressed state
		if(pressedProperty().get())
			pencil.fillColor(bb.x, bb.y, bb.width, bb.height, 0x33ffffff);
	}
	// ==================================================
}
