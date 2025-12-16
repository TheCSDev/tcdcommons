package com.thecsdev.commonmc.api.client.gui.widget.stats;

import com.thecsdev.common.properties.BooleanProperty;
import com.thecsdev.common.properties.ObjectProperty;
import com.thecsdev.common.util.enumerations.CompassDirection;
import com.thecsdev.commonmc.api.client.gui.TElement;
import com.thecsdev.commonmc.api.client.gui.misc.TEntityElement;
import com.thecsdev.commonmc.api.client.gui.tooltip.TTooltip;
import com.thecsdev.commonmc.api.stats.StatsProvider;
import com.thecsdev.commonmc.api.stats.util.EntityStats;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;

/**
 * Stats widget that shows statistics about a given {@link EntityType}.
 */
public final class TEntityStatsWidget extends TStatsWidget
{
	// ==================================================
	//tooltip supplier function. defined only once to save on memory usage
	private static final Function<TElement, TElement> TOOLTIP = el -> {
		final var mstw  = (TEntityStatsWidget) el;
		final var stats = mstw.stats.get();
		Objects.requireNonNull(stats, "Stats value is missing while constructing tooltip.");
		return TTooltip.of(stats);
	};
	// --------------------------------------------------
	private final ObjectProperty<EntityStats> stats = new ObjectProperty<>();
	// --------------------------------------------------
	private final TEntityElement el_entity = new TEntityElement(EntityType.MARKER);
	// ==================================================
	public TEntityStatsWidget() { this(null); }
	public TEntityStatsWidget(@NotNull EntityType<?> subject, @NotNull StatsProvider provider) { this(new EntityStats(subject, provider)); }
	public TEntityStatsWidget(@Nullable EntityStats stats) {
		//initialize properties
		this.stats.addChangeListener((p, o, n) -> refresh());
		this.stats.getHandle().set(stats);
		//initialize elements
		this.el_entity.entityScaleProperty().set(0.7d, TEntityStatsWidget.class);
		getLabel().textAlignmentProperty().set(CompassDirection.CENTER, TEntityStatsWidget.class);
		getLabel().wrapTextProperty().set(true, TEntityStatsWidget.class);
		getLabel().visibleProperty().set(false, TEntityStatsWidget.class);
		//initial bootstrap refresh
		refresh();
	}
	// --------------------------------------------------
	/**
	 * Refreshes this {@link TEntityStatsWidget}'s tooltip and any other
	 * elements that are part of it.<br>
	 * Called automatically whenever {@link #statsProperty()} value changes.
	 * @apiNote {@link Override}s must call {@code super}!
	 */
	private final @ApiStatus.Internal void refresh() {
		//set tooltip for this element
		tooltipProperty().set(this.stats.get() != null ? TOOLTIP : null, TEntityStatsWidget.class);
		final @Nullable var stats = this.stats.get();
		//set display entity
		this.el_entity.entityTypeProperty().set(
				(stats != null) ? stats.getSubject() : null, //null turns into default value
				TEntityStatsWidget.class);
		//set display text to be the display name of the entity
		getLabel().setText((stats != null) ? stats.getSubjectDisplayName() : Component.literal("-"));
	}
	// ==================================================
	/**
	 * The {@link ObjectProperty} holding the {@link EntityStats}
	 * whose statistics are to be shown.
	 */
	public final ObjectProperty<EntityStats> statsProperty() { return this.stats; }

	/**
	 * Returns the {@link BooleanProperty} that determines whether
	 * this the rendered {@link Entity}'s on-screen rotation follows
	 * the cursor location.
	 * @see TEntityElement#followsCursorProperty()
	 */
	public final BooleanProperty followsCursorProperty() { return this.el_entity.followsCursorProperty(); }
	// ==================================================
	protected final @Override void initCallback() {
		//the entity element goes below the label
		this.el_entity.setBounds(getBounds());
		add(this.el_entity);
		//and the label goes above the entity element
		getLabel().setBounds(getBounds());
		add(getLabel());
	}
	protected final @Override void tickCallback() {
		super.tickCallback();
		//in the event something goes wrong when attempting to render the entity,
		//we will reveal the label as it will act as the placeholder replacement
		getLabel().visibleProperty().set(this.el_entity.getDisplayEntity() == null, TEntityStatsWidget.class);
	}
	// ==================================================
}
