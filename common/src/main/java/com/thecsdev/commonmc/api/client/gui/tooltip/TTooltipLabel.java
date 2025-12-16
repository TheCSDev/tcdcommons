package com.thecsdev.commonmc.api.client.gui.tooltip;

import com.thecsdev.common.properties.NotNullProperty;
import com.thecsdev.common.properties.ObjectProperty;
import com.thecsdev.common.util.enumerations.CompassDirection;
import com.thecsdev.commonmc.api.client.gui.label.TLabelElement;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;

import static com.thecsdev.commonmc.api.client.gui.util.TGuiUtils.calcMaxLineWidth;
import static com.thecsdev.commonmc.api.client.gui.util.TGuiUtils.getScreenBounds;

/**
 * A {@link TTooltip} with a simple textual label.
 */
@ApiStatus.Internal
sealed class TTooltipLabel extends TTooltip
		permits TTooltipCustomStat, TTooltipEntityStats, TTooltipItemStats
{
	// ==================================================
	/**
	 * The {@link TLabelElement} that is responsible for rendering
	 * this {@link TTooltipLabel}'s text.
	 */
	private final TLabelElement label = new TLabelElement();
	// ==================================================
	TTooltipLabel() { this(null); }
	TTooltipLabel(Component text)
	{
		//reinit whenever text property changes
		textProperty().addChangeListener((p, o, n) -> clearAndInit());
		//initialize properties
		this.label.wrapTextProperty().set(true, TTooltipLabel.class);
		this.label.parentProperty()  .set(this, TTooltipLabel.class);
		this.label.textAlignmentProperty().set(CompassDirection.NORTH_WEST, TTooltipLabel.class);
		textProperty().getHandle().set(text); //dodge the change listener
	}
	// ==================================================
	/**
	 * An {@link ObjectProperty} for this {@link TTooltipLabel}'s text.
	 */
	public final NotNullProperty<Component> textProperty() { return this.label.textProperty(); }
	// ==================================================
	protected final @Override void initCallback()
	{
		//calculate text width, and cap tooltip width
		final int padd  = 3;
		final var textW = Math.min(
				Math.max(getScreenBounds().width / 3, 200),
				calcMaxLineWidth(textProperty().get(), this.label.fontProperty().get())
		);

		//calculate and set label bounds
		this.label.setBounds(0, 0, textW, 0);
		this.label.clearAndInit(); //wrap lines and calculate height
		this.label.setBounds(padd, padd, textW, this.label.getTextHeight());

		//add the label and set tooltip bounds
		setBounds(0, 0, this.label.getBounds().width + (padd * 2), this.label.getBounds().height + (padd * 2));
		add(this.label);
	}
	// ==================================================
}
