package com.thecsdev.commonmc.api.client.gui.screen;

import com.thecsdev.common.util.annotations.Virtual;
import com.thecsdev.common.util.enumerations.CompassDirection;
import com.thecsdev.commonmc.api.client.gui.TElement;
import com.thecsdev.commonmc.api.client.gui.util.TInputContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Predicate;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Same as {@link TScreen}, but with some extra features such as arrow-key
 * navigation. May cost extra performance.
 */
@Environment(EnvType.CLIENT)
public abstract class TScreenPlus extends TScreen
{
	// ==================================================
	private static final Predicate<TElement> FCSE_DEFAULT = (e -> e.isFocusable() && e.isVisible());
	// ==================================================
	public TScreenPlus() {}
	public TScreenPlus(@NotNull Component title) { super(title); }
	// ==================================================
	public @Virtual @Override boolean inputCallback(
			@NotNull TInputContext.InputDiscoveryPhase phase,
			@NotNull TInputContext context)
	{
		//always respect super when dealing with screens
		if(super.inputCallback(phase, context)) return true;
		//only handle the main phase
		else if(phase != TInputContext.InputDiscoveryPhase.MAIN) return false;

		//handle key press for arrow key navigation
		if(context.getInputType() == TInputContext.InputType.KEY_PRESS)
		{
			//obtain the direction based on the pressed key-code
			final CompassDirection direction = switch(context.getKeyCode()) {
				case GLFW_KEY_UP    -> CompassDirection.NORTH;
				case GLFW_KEY_DOWN  -> CompassDirection.SOUTH;
				case GLFW_KEY_LEFT  -> CompassDirection.WEST;
				case GLFW_KEY_RIGHT -> CompassDirection.EAST;
				case null, default  -> null;
			};
			if(direction == null) return false;

			//find the "closest" element to the currently focused element
			final var closest = findClosestSideElement(super.focused.get(), direction);
			if(closest == null) return false; //return if not found

			//focus on the found "closest" element
			super.focused.set(closest, TScreenPlus.class);

			//return true to indicate the event was handled
			return true;
		}

		//return false by default
		return false;
	}
	// ==================================================
	/**
	 * Same as {@link #findClosestSideElement(TElement, CompassDirection, Predicate)},
	 * except this method uses the default {@link Predicate} that requires the returned
	 * {@link TElement} to be focusable and visible.
	 * @param target See below...
	 * @param direction See below...
	 * @see #findClosestSideElement(TElement, CompassDirection, Predicate)
	 */
	@Contract("null, _ -> null; _, null -> null; _, _ -> _")
	private @Nullable TElement findClosestSideElement(
			final @Nullable TElement target,
			final @Nullable CompassDirection direction) {
		return findClosestSideElement(target, direction, FCSE_DEFAULT);
	}

	/**
	 * Finds and returns the {@link TElement} nearest to the specified target
	 * {@link TElement}, in the given {@link CompassDirection}.<br/>
	 * The method will iterate through all children elements on the {@link TScreen}
	 * and locate the element closest to the target in the specified direction.
	 *
	 * <p>For example, if the direction is {@link CompassDirection#EAST}, the method
	 * will find the element nearest to the target that is to the right of it.</p>
	 *
	 * @param target The target {@link TElement} for which the closest side element needs to be found.
	 * @param direction The {@link CompassDirection} in which to search for the nearest element.
	 *                  This does not support diagonal directions.
	 * @param predicate The {@link Predicate} the returned {@link TElement} must match.
	 * @return The nearest {@link TElement} to the target in the specified direction,
	 * or {@code null} if no such element is found.
	 */
	@Contract("null, _, _ -> null; _, null, _ -> null; _, _, _ -> _")
	private @Nullable TElement findClosestSideElement(
			final @Nullable TElement target,
			final @Nullable CompassDirection direction,
			final @NotNull Predicate<TElement> predicate)
	{
		//ensure the necessary arguments aren't null
		if(target == null || direction == null) return null;
		Objects.requireNonNull(predicate);

		//obtain target's center coordinates
		final var tbb = target.getBounds();
		final int x = tbb.x + (tbb.width / 2);
		final int y = tbb.y + (tbb.height / 2);

		//lightweight arrays to bypass the "effectively final" lambda restriction
		final TElement[] closest = { null };
		final long[] minDistanceSq = { Long.MAX_VALUE };

		//define the search logic
		final Predicate<TElement> searchPredicate = child ->
		{
			//skip the target itself or anything that fails the given criteria
			if(child == target || !predicate.test(child)) return false;

			final var cbb = child.getBounds();
			final int cX = cbb.x + (cbb.width / 2);
			final int cY = cbb.y + (cbb.height / 2);

			//strict directional half-plane check based on centers
			boolean isValidDirection = switch(direction) {
				case NORTH -> cY < y;
				case SOUTH -> cY > y;
				case WEST  -> cX < x;
				case EAST  -> cX > x;
				default    -> false; //should not happen with CompassDirection
			};

			if(!isValidDirection) return false;

			//calculate Squared euclidean distance (a^2 + b^2 = c^2)
			long dX = cX - x;
			long dY = cY - y;

			//multiplying the cross-axis by a weight (e.g., 4) makes the cursor strongly
			//prefer jumping in a straight line rather than making wild diagonal jumps.
			long distSq = switch(direction) {
				case NORTH, SOUTH -> (dX * dX * 4) + (dY * dY); // Penalize X variance
				case EAST, WEST   -> (dX * dX) + (dY * dY * 4); // Penalize Y variance
				default           -> (dX * dX) + (dY * dY);
			};

			//if it's the closest one we've seen so far, record it
			if(distSq < minDistanceSq[0]) {
				minDistanceSq[0] = distSq;
				closest[0] = child;
			}

			//always return false to force 'findChild' to iterate through ALL elements
			return false;
		};

		//search globally from 'this' (the TScreen root element)
		findChild(searchPredicate, true);

		return closest[0];
	}
	// ==================================================
}
