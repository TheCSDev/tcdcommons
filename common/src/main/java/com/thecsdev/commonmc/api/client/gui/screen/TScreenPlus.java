package com.thecsdev.commonmc.api.client.gui.screen;

import com.thecsdev.common.util.annotations.Virtual;
import com.thecsdev.common.util.enumerations.CompassDirection;
import com.thecsdev.commonmc.api.client.gui.TElement;
import com.thecsdev.commonmc.api.client.gui.util.TInputContext;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Same as {@link TScreen}, but with some extra features such as arrow-key
 * navigation. May cost extra performance.
 */
//TODO - Poor implementation. Needs something less clunky.
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
	//FIXME - What on earth is going on here? So weird and broken...
	@Contract("null, _, _ -> null; _, null, _ -> null; _, _, _ -> _")
	private @Nullable TElement findClosestSideElement(
			final @Nullable TElement target,
			final @Nullable CompassDirection direction,
			final @NotNull Predicate<TElement> predicate)
	{
		//esure the necessary arguments aren't null
		if(target == null || direction == null || target.getParent() == null)
			return null;
		Objects.requireNonNull(predicate);

		//obtain target's coordinates
		final var tbb = target.getBounds();
		final int x = tbb.x + (tbb.width / 2);
		final int y = tbb.y + (tbb.height / 2);

		//define the closest element
		final var closest = new AtomicReference<TElement>(null);
		final AtomicInteger dX = new AtomicInteger(0), dY = new AtomicInteger(0);

		//iterate elements
		final Predicate<TElement> finalPredicate = child ->
		{
			//skip these:
			if(child == target || !predicate.test(child))
				return false;
			final var cbb = child.getBounds();
			final int cX = cbb.x + (cbb.width / 2);
			final int cY = cbb.y + (cbb.height / 2);

			//direction check
			switch(direction) {
				case NORTH: if(cY > y - 1) return false; break;
				case SOUTH: if(cY < y + 1) return false; break;
				case WEST:  if(cX > x - 1) return false; break;
				case EAST:  if(cX < x + 1) return false; break;
				default: return false;
			}

			//if no closest is found yet, assign the current one
			if(closest.get() == null)
			{
				closest.set(child);
				dX.set(Math.abs(cX - x));
				dY.set(Math.abs(cY - y));
				return false;
			}

			//distance check
			else if(Math.abs(cX - x) < dX.get() || Math.abs(cY - y) < dY.get())
			{
				closest.set(child);
				dX.set(Math.abs(cX - x));
				dY.set(Math.abs(cY - y));
				return false;
			}

			//continue the loop
			return false;
		};

		//find child
		target.getParent().findChild(finalPredicate, true);
		if(closest.get() == null) findChild(finalPredicate, true);
		//FIXME ^ what is going on here??????

		//return
		return closest.get();
	}
	// ==================================================
}
