package com.thecsdev.commonmc.api.client.gui.widget.stats;

import com.thecsdev.common.properties.IChangeListener;
import com.thecsdev.common.properties.ObjectProperty;
import com.thecsdev.commonmc.api.client.gui.TElement;
import com.thecsdev.commonmc.api.client.gui.tooltip.TTooltip;
import com.thecsdev.commonmc.api.stats.StatsProvider;
import com.thecsdev.commonmc.api.stats.util.CustomStat;
import com.thecsdev.commonmc.api.stats.util.StatFormatterOverride;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;

import static net.minecraft.network.chat.Component.literal;

/**
 * Textual stat widget that shows the name and value of a "custom stat",
 * also known as "general stat" in the game's statistics GUI.
 */
public final class TCustomStatWidget extends TTextualStatWidget
{
	// ==================================================
	//tooltip supplier function. defined only once to save on memory usage
	private static final Function<TElement, @NotNull TElement> TOOLTIP = el -> {
		final var csw  = (TCustomStatWidget) el;
		final var stat = csw.stat.get();
		Objects.requireNonNull(stat, "Stat value is missing while constructing tooltip.");
		return TTooltip.of(stat);
	};
	// --------------------------------------------------
	private final ObjectProperty<CustomStat>            stat              = new ObjectProperty<>();
	private final ObjectProperty<StatFormatterOverride> formatterOverride = new ObjectProperty<>();
	// ==================================================
	public TCustomStatWidget() { this(null); }
	public TCustomStatWidget(@NotNull Identifier subject, @NotNull StatsProvider provider) {
		this(new CustomStat(subject, provider));
	}
	public @SuppressWarnings("unchecked") TCustomStatWidget(@Nullable CustomStat stat) {
		//refresh this widget when property values change
		final IChangeListener<?> cl_update = (p, o, n) -> { if(getParent() != null) onUpdate(); };
		this.stat.addChangeListener((IChangeListener<CustomStat>) cl_update);
		this.formatterOverride.addChangeListener((IChangeListener<StatFormatterOverride>) cl_update);
		//initial stat value
		this.stat.getHandle().set(stat); //now is no time to trigger change listeners
	}
	// --------------------------------------------------
	private final @ApiStatus.Internal void onUpdate()
	{
		final @Nullable var stat = this.stat.get();
		if(stat != null) {
			getKeyLabel().textProperty().set(stat.getSubjectDisplayName(), TCustomStatWidget.class);
			getValueLabel().textProperty().set(literal(stat.getValueF(this.formatterOverride.get())), TCustomStatWidget.class);
			tooltipProperty().set(TOOLTIP, TCustomStatWidget.class);
		} else {
			getKeyLabel().textProperty().set(literal("-"), TCustomStatWidget.class);
			getValueLabel().textProperty().set(literal("-"), TCustomStatWidget.class);
			tooltipProperty().set(null, TCustomStatWidget.class);
		}
		invalidateTooltipCache();
	}
	protected @Override void initCallback() { super.initCallback(); onUpdate(); }
	// ==================================================
	/**
	 * The {@link ObjectProperty} holding the {@link CustomStat}
	 * whose statistics are to be shown.
	 */
	public final ObjectProperty<CustomStat> statProperty() { return this.stat; }

	/**
	 * The {@link ObjectProperty} holding the {@link StatFormatterOverride}
	 * used to format the stat's value for display.
	 * If the value is {@code null}, the default formatter will be used.
	 */
	public final ObjectProperty<StatFormatterOverride> formatterOverrideProperty() { return this.formatterOverride; }
	// ==================================================
}
