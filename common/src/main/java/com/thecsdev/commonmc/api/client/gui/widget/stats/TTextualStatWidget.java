package com.thecsdev.commonmc.api.client.gui.widget.stats;

import com.thecsdev.common.math.Bounds2i;
import com.thecsdev.common.util.annotations.Virtual;
import com.thecsdev.common.util.enumerations.CompassDirection;
import com.thecsdev.commonmc.api.client.gui.label.TLabelElement;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

/**
 * Simple textual stat widget featuring two {@link TLabelElement}s showing
 * only the name and value of a given stat. Can be anything, you set the label texts.
 */
public sealed class TTextualStatWidget extends TStatsWidget permits TCustomStatWidget
{
	// ==================================================
	private final TLabelElement lbl_key   = new TLabelElement();
	private final TLabelElement lbl_value = new TLabelElement();
	// ==================================================
	public TTextualStatWidget() {
		this.lbl_key.textAlignmentProperty().set(CompassDirection.WEST, TTextualStatWidget.class);
		this.lbl_value.textAlignmentProperty().set(CompassDirection.EAST, TTextualStatWidget.class);
	}
	public TTextualStatWidget(@NotNull Component left, @NotNull Component right) {
		this();
		this.lbl_key.setText(left);
		this.lbl_value.setText(right);
	}
	// ==================================================
	/**
	 * Returns the {@link TLabelElement} holding the stat name.
	 * Use this to customize the label and set its text.
	 * @apiNote Do not reassign this label to another parent!
	 */
	public final TLabelElement getKeyLabel() { return this.lbl_key; }

	/**
	 * Returns the {@link TLabelElement} holding the stat value.
	 * Use this to customize the label and set its text.
	 * @apiNote Do not reassign this label to another parent!
	 */
	public final TLabelElement getValueLabel() { return this.lbl_value; }
	// ==================================================
	protected @Virtual @Override void initCallback() {
		super.initCallback();
		final var bb  = getBounds();
		final var nbb = new Bounds2i(bb.x + 5, bb.y, bb.width - 10, bb.height);
		this.lbl_key.setBounds(nbb);   add(this.lbl_key);
		this.lbl_value.setBounds(nbb); add(this.lbl_value);
	}
	// ==================================================
}
