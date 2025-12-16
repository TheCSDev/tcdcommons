package com.thecsdev.commonmc.api.client.gui.misc;

import com.thecsdev.common.properties.NotNullProperty;
import com.thecsdev.common.properties.ObjectProperty;
import com.thecsdev.common.util.annotations.Virtual;
import com.thecsdev.common.util.enumerations.CompassDirection;
import com.thecsdev.commonmc.api.client.gui.TElement;
import com.thecsdev.commonmc.api.client.gui.panel.TPanelElement;
import com.thecsdev.commonmc.api.client.gui.util.TInputContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * {@link TElement} that when hovered, sends simulated mouse-scroll input
 * to its target {@link TElement}.
 */
public @Virtual class THoverScrollElement<E extends TElement> extends TElement
{
	// ================================================== ==================================================
	//                                THoverScrollElement IMPLEMENTATION
	// ================================================== ==================================================
	private final ObjectProperty<E>                 target    = new ObjectProperty<>();
	private final NotNullProperty<CompassDirection> direction = new NotNullProperty<>(CompassDirection.SOUTH);
	// ==================================================
	public THoverScrollElement() {
		hoverableProperty().set(true, THoverScrollElement.class);
		focusableProperty().set(false, THoverScrollElement.class);
	}
	public THoverScrollElement(@NotNull E target) {
		this();
		this.target.set(target, THoverScrollElement.class);
	}
	// ==================================================
	/**
	 * {@link ObjectProperty} that holds the target {@link TElement}.
	 */
	public final @NotNull ObjectProperty<E> targetProperty() { return this.target; }

	/**
	 * {@link NotNullProperty} that holds the scroll direction.
	 */
	public final @NotNull NotNullProperty<CompassDirection> directionProperty() { return this.direction; }
	// ==================================================
	protected @Virtual @Override void tickCallback()
	{
		//if this element is hovered, send scroll input to the target
		if(isHovered())
		{
			//obtain target element (check if it's there too)
			final @Nullable var target = this.target.get();
			if(target == null) return;
			//obtain mouse X and Y from the client
			final var client = getClient();
			assert client != null;
			final var window = client.getWindow();
			final double mouseX = client.mouseHandler.getScaledXPos(window);
			final double mouseY = client.mouseHandler.getScaledYPos(window);
			//obtain the scroll amount
			final double dX = switch(this.direction.get()) {
				case EAST, NORTH_EAST, SOUTH_EAST -> 0.3;
				case WEST, NORTH_WEST, SOUTH_WEST -> -0.3;
				default -> 0;
			};
			final double dY = switch(this.direction.get()) {
				case NORTH, NORTH_EAST, NORTH_WEST -> 0.3;
				case SOUTH, SOUTH_EAST, SOUTH_WEST -> -0.3;
				default -> 0;
			};
			//send scroll input
			if(dX != 0 || dY != 0)
				target.inputCallback(
						TInputContext.InputDiscoveryPhase.MAIN,
						TInputContext.ofMouseScroll(mouseX, mouseY, dX, dY));
		}
		//set visibility based on whether it should be visible
		if(visibleProperty().getZ() != shouldBeVisible())
			visibleProperty().set(!visibleProperty().getZ(), THoverScrollElement.class);
	}
	// ==================================================
	/**
	 * Each tick, {@link #tickCallback()} sets {@link #visibleProperty()} value
	 * to be the return value of this method.
	 */
	public @Virtual boolean shouldBeVisible() { return true; }
	// ================================================== ==================================================
	//                                              Panel IMPLEMENTATION
	// ================================================== ==================================================
	public static @Virtual class Panel extends THoverScrollElement<TPanelElement>
	{
		// ==================================================
		public Panel() {}
		public Panel(@NotNull TPanelElement target) { super(target); }
		// ==================================================
		public @Virtual @Override boolean shouldBeVisible()
		{
			//a panel must be present
			final var panel = targetProperty().get();
			if(panel == null) return false;
			//obtain scroll amount
			final var psa = panel.scrollAmountProperty().get();
			//return based on the direction and scroll amount value
			return switch(directionProperty().get()) {
				case NORTH -> psa.y > 0;
				case SOUTH -> psa.y < 1;
				case EAST  -> psa.x < 1;
				case WEST  -> psa.x > 0;
				case NORTH_EAST -> psa.y > 0 || psa.x < 1;
				case NORTH_WEST -> psa.y > 0 || psa.x > 0;
				case SOUTH_EAST -> psa.y < 1 || psa.x < 1;
				case SOUTH_WEST -> psa.y < 1 || psa.x > 0;
				default -> false;
			};
		}
		// ==================================================
	}
	// ================================================== ==================================================
}
