package com.thecsdev.commonmc.api.client.gui.util;

import com.mojang.blaze3d.platform.InputConstants;
import com.thecsdev.common.math.Bounds2i;
import com.thecsdev.commonmc.TCDCommons;
import com.thecsdev.commonmc.api.client.gui.TElement;
import com.thecsdev.commonmc.api.client.gui.panel.TPanelElement;
import com.thecsdev.commonmc.api.client.gui.widget.TClickableWidget;
import com.thecsdev.commonmc.api.client.gui.widget.text.TSimpleTextFieldWidget;
import com.thecsdev.commonmc.client.mixin.hooks.AccessorGameRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SHIFT;

/**
 * {@link TCDCommons} API's GUI-related utility methods.
 */
public final class TGuiUtils
{
	// ==================================================
	private static final Random RNG = new Random(); //isolated rng instance
	// --------------------------------------------------
	private TGuiUtils() {}
	// ==================================================
	/**
	 * Plays a GUI button click sound using the "master" volume.
	 */
	public static final void playGuiButtonClickSound() {
		Minecraft.getInstance().getSoundManager()
			.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1));
	}

	/**
	 * Plays a GUI sound for when a {@link TClickableWidget} is hovered.
	 */
	public static final void playGuiHoverSound() {
		Minecraft.getInstance().getSoundManager().play(
				SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_HAT.value(), 1.8f, 0.05f));
	}

	/**
	 * Plays a GUI sound for when the user types textual input into a
	 * {@link TSimpleTextFieldWidget}. Text input typing noises.
	 */
	public static final void playGuiTypingSound() {
		Minecraft.getInstance().getSoundManager().play(
				SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_HAT.value(), 1 + (RNG.nextFloat() * 0.3f), 0.05f));
	}
	// ==================================================
	/**
	 * Returns the {@link GuiRenderer} that belongs to a given {@link Minecraft}.
	 * @param client The {@link Minecraft}.
	 */
	public static final GuiRenderer getGuiRenderer(Minecraft client) {
		return ((AccessorGameRenderer)(Object)client.gameRenderer).getGuiRenderer();
	}

	/**
	 * Returns the bounding box of the game window in scaled (GUI) units.<br>
	 * This represents the screen dimensions in in-game units, not actual pixels.
	 * @apiNote The game screen can resize at any moment. Keep this in mind!
	 */
	public static final Bounds2i getScreenBounds() {
		final @NotNull var window = Minecraft.getInstance().getWindow();
		return new Bounds2i(0, 0, window.getGuiScaledWidth(), window.getGuiScaledHeight());
	}
	// ==================================================
	/**
	 * Returns {@code true} is a given key is currently held down
	 * for the current game window.
	 * @param keyCode The {@link GLFW} key code.
	 */
	public static final boolean isKeyDown(int keyCode) {
		return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), keyCode);
	}

	/**
	 * Returns {@code true} is the left or right shift key is currently held down
	 * for the current game window.
	 * @see #isKeyDown(int)
	 */
	public static final boolean isShiftDown() {
		return isKeyDown(GLFW_KEY_LEFT_SHIFT) || isKeyDown(GLFW_KEY_RIGHT_SHIFT);
	}
	// ==================================================
	/**
	 * Calculates the maximum width a {@link Component}'s line could take,
	 * assuming said {@link Component} were to be split by {@code \n} into lines.
	 * @param text The {@link Component} whose text is measured.
	 * @param font The {@link Font} used to measure the text.
	 * @throws NullPointerException If an argument is {@code null}.
	 */
	public static final int calcMaxLineWidth(@NotNull Component text, @NotNull Font font)
			throws NullPointerException
	{
		//prepare for iteration
		final var lines = text.getString().replace("\r\n", "\n").split("\n");
		//iterate, keep track of longest line, and return its width
		int maxWidth = 0;
		for(final var line : lines)
			maxWidth = Math.max(font.width(line), maxWidth);
		return maxWidth;
	}

	/**
	 * Calculates the largest width among a collection of {@link TElement}s.
	 * @param elements The collection of {@link TElement}s.
	 * @throws NullPointerException If the argument is {@code null}.
	 */
	public static final int calcMaxWidth(@NotNull Iterable<TElement> elements) throws NullPointerException {
		int maxW = 0;
		for(final var el : elements)
			maxW = Math.max(el.getBounds().width, maxW);
		return maxW;
	}
	// ==================================================
	/**
	 * Checks whether a given {@link TElement} is an ancestor of another specified {@link TElement}.
	 * @param child The element whose ancestry is tested. May be {@code null}.
	 * @param candidateAncestor The parent element to test as a possible ancestor. Must not be {@code null}.
	 * @return {@code true} if {@code candidateAncestor} is an ancestor of {@code child}, otherwise {@code false}.
	 * @throws NullPointerException if {@code candidateAncestor} is {@code null}.
	 */
	public static final boolean isAncestor(@Nullable TElement child, @NotNull TElement candidateAncestor) throws NullPointerException {
		Objects.requireNonNull(candidateAncestor);
		if(child == null) return false;
		else return (child.findParent(p -> p == candidateAncestor).isPresent());
	}
	// --------------------------------------------------
	/**
	 * Finds the {@link TElement} that is focusable and that comes before a
	 * given target {@link TElement}.
	 * @param target The target {@link TElement} after which to look for the previous one.
	 * @throws NullPointerException If an argument is {@code null}.
	 */
	public static final @Nullable TElement previousFocusableTElement(TElement target) throws NullPointerException {
		return previousFocusableTElement(target, false);
	}

	/**
	 * Finds the {@link TElement} that is focusable and that comes before a
	 * given target {@link TElement}.
	 * @param target The target {@link TElement} after which to look for the previous one.
	 * @param underflow When {@code true}, the search wraps around to the last focusable
	 * {@link TElement} if no elements are found before the target.
	 * @throws NullPointerException If an argument is {@code null}.
	 */
	public static final @Nullable TElement previousFocusableTElement(
			TElement target, boolean underflow) throws NullPointerException
	{
		//we start from the root element
		TElement root = Objects.requireNonNull(target).findParent(el -> el.getParent() == null).orElse(null);
		if(root == null) root = target; //we need a root

		//lay out the entire element tree into one single sequential array list
		final ArrayList<TElement> hierarchy = new ArrayList<>((int)(root.size() * 1.5f));
		root.forEachVisible(
				child -> { if(child.isFocusable() || child == target) hierarchy.add(child); },
				true);

		// ----- figure out the next element
		//empty list means there's nothing to choose from, so return null
		if(hierarchy.isEmpty()) return null;

		//obtain and use the target index to determine the previous
		int targetIndex = hierarchy.indexOf(target);
		if(targetIndex - 1 < 0) return underflow ? hierarchy.getLast() : null;
		else                    return hierarchy.get(targetIndex - 1);
	}

	/**
	 * Finds the {@link TElement} that is focusable and that comes after a
	 * given target {@link TElement}.
	 * @param target The target {@link TElement} after which to look for the next one.
	 * @throws NullPointerException If an argument is {@code null}.
	 */
	public static final @Nullable TElement nextFocusableTElement(TElement target) throws NullPointerException {
		return nextFocusableTElement(target, false);
	}

	/**
	 * Finds the {@link TElement} that is focusable and that comes after a
	 * given target {@link TElement}.
	 * @param target The target {@link TElement} after which to look for the next one.
	 * @param overflow When {@code true}, the search wraps around to the first focusable
	 * {@link TElement} if no elements are found after the target.
	 * @throws NullPointerException If an argument is {@code null}.
	 */
	public static final @Nullable TElement nextFocusableTElement(
			TElement target, boolean overflow) throws NullPointerException
	{
		//we start from the root element
		TElement root = Objects.requireNonNull(target).findParent(el -> el.getParent() == null).orElse(null);
		if(root == null) root = target; //we need a root

		//lay out the entire element tree into one single sequential array list
		final ArrayList<TElement> hierarchy = new ArrayList<>((int)(root.size() * 1.5f));
		root.forEachVisible(
				child -> { if(child.isFocusable() || child == target) hierarchy.add(child); },
				true);

		// ----- figure out the next element
		//empty list means there's nothing to choose from, so return null
		if(hierarchy.isEmpty()) return null;

		//obtain and use the target index to determine the next
		final int targetIndex = hierarchy.indexOf(target);
		if(targetIndex + 1 >= hierarchy.size()) return overflow ? hierarchy.getFirst() : null;
		else                                    return hierarchy.get(targetIndex + 1);
	}
	// ==================================================
	/**
	 * Scrolls the GUI so that a given {@link TElement} is fully visible on-screen.
	 * This involves scrolling {@link TPanelElement}s the {@link TElement} is part of.
	 * <br>
	 * If the element is already fully visible, no scrolling occurs.
	 * @param target The target {@link TElement} to scroll to.
	 * @return {@code true} if scrolling was possible and done, otherwise {@code false}.
	 */
	@Contract("null -> false; _ -> _")
	@SuppressWarnings("UnusedReturnValue")
	public static final boolean scrollToElement(@Nullable TElement target)
	{
		//if the target is null, we cannot scroll to it
		if(target == null) return false;
		//obtain the element's screen. we can't proceed without one
		final var screen = target.screenProperty().get();
		if(screen == null) return false;

		//obtain the element's parent panel. we can't proceed without it
		final var panel = (TPanelElement) target.findParent(p -> p instanceof TPanelElement).orElse(null);
		if(panel == null) //if there's no panel, can't scroll
			return screen.getBounds().contains(target.getBounds());

		//obtain bounding boxes. this will be used for maths
		final var pbb = panel.getBounds();  //panel
		final var tbb = target.getBounds(); //target
		//calculate deltaX and deltaY for how much the panel needs be scrolled
		//for the element to show up in panel's view
		int dX = 0, dY = 0;
		if(!pbb.contains(tbb))
		{
			//if the target is not too big, we just scroll to it normally
			if(!(tbb.width > pbb.width || tbb.height > pbb.height)) {
				if(tbb.x < pbb.x) dX = tbb.x - pbb.x;                  //target is left of panel
				else if(tbb.endX > pbb.endX) dX = tbb.endX - pbb.endX; //target is right of panel
				if(tbb.y < pbb.y) dY = tbb.y - pbb.y;                  //target is above panel
				else if(tbb.endY > pbb.endY) dY = tbb.endY - pbb.endY; //target is below panel
			}
			//otherwise we scroll to it only in the axis where it is not visible
			//for example if the target is wider than the panel but out of bounds vertically, we only scroll vertically
			else {
				if(tbb.width > pbb.width) {
					//target is wider than panel, so only scroll vertically
					if(tbb.y < pbb.y) dY = tbb.y - pbb.y;                  //target is above panel
					else if(tbb.endY > pbb.endY) dY = tbb.endY - pbb.endY; //target is below panel
				} else {
					//target is taller than panel, so only scroll horizontally
					if(tbb.x < pbb.x) dX = tbb.x - pbb.x;                  //target is left of panel
					else if(tbb.endX > pbb.endX) dX = tbb.endX - pbb.endX; //target is right of panel
				}
			}
		}
		//if the scroll delta is present, do the scroll
		if(dX != 0 || dY != 0)
			panel.scroll(-dX, -dY);

		//once the panel was scrolled, scroll the panel's parent panels
		return scrollToElement(panel);
	}
	// --------------------------------------------------
	/**
	 * Ensures that a given {@link TElement} remains within specified bounds.
	 * If the element is out of bounds, it is moved back within the bounds.
	 * @param target The target {@link TElement} to keep within bounds.
	 * @param bounds The bounding box within which the element must remain.
	 * @throws NullPointerException If an argument is {@code null}.
	 */
	public static final void keepElementWithinBounds(
			@NotNull TElement target, @NotNull Bounds2i bounds) throws NullPointerException
	{
		//not null requirements
		Objects.requireNonNull(target);
		Objects.requireNonNull(bounds);

		//calculate the detla x and delta y
		final var bb  = target.getBounds();
		int dX = 0, dY = 0;
		if(bb.endY > bounds.endY) dY = bounds.endY - bb.endY;
		if(bb.y + dY < bounds.y)  dY += bounds.y - (bb.y + dY);
		if(bb.endX > bounds.endX) dX = bounds.endX - bb.endX;
		if(bb.x + dX < bounds.x)  dX += bounds.x - (bb.x + dX);

		//finally, move
		if(dX != 0 || dY != 0) target.move(dX, dY);
	}
	// ==================================================
}
