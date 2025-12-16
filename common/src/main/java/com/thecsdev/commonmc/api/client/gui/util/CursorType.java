package com.thecsdev.commonmc.api.client.gui.util;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import com.thecsdev.commonmc.api.client.gui.TElement;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * An enumeration representing different types of mouse cursors that can be used in the
 * graphical user interface (GUI).
 * @see TElement#getCursor()
 */
public enum CursorType
{
	// ==================================================
	/**
	 * Default cursor icon. Same as {@link #ARROW}.
	 */
	DEFAULT(com.mojang.blaze3d.platform.cursor.CursorType.DEFAULT),

	/**
	 * Standard arrow. Default pointer.<br>
	 * General navigation, for non-interactive elements.
	 */
	ARROW(CursorTypes.ARROW),

	/**
	 * Vertical line with serifs.<br>
	 * Indicates an editable text field. For text selection and insertion.
	 */
	IBEAM(CursorTypes.IBEAM),

	/**
	 * Crosshair. Thin plus-shaped icon.<br>
	 * For precise selection, drawing, or targeting.
	 */
	CROSSHAIR(CursorTypes.CROSSHAIR),

	/**
	 * Hand Pointer with index finger pointing up.<br>
	 * Indicating a clickable link or interactive UI element.
	 */
	POINTING_HAND(CursorTypes.POINTING_HAND),

	/**
	 * Horizontal double arrow (↔).<br>
	 * For resizing a window or element horizontally.
	 */
	RESIZE_NS(CursorTypes.RESIZE_NS),

	/**
	 * Vertical double arrow (↕).<br>
	 * For resizing a window or element vertically.
	 */
	RESIZE_EW(CursorTypes.RESIZE_EW),

	/**
	 * Four-way arrow (↔ with ↕ overlaid).<br>
	 * For moving an element or resizing in any direction.
	 */
	RESIZE_ALL(CursorTypes.RESIZE_ALL),

	/**
	 * Slashed circle (circle with a diagonal line ⊘).<br>
	 * Indicating an action cannot be performed, like clicking or during drag-and-drop operations.
	 */
	NOT_ALLOWED(CursorTypes.NOT_ALLOWED);
	// ==================================================
	private final @NotNull com.mojang.blaze3d.platform.cursor.CursorType nativeType;
	// ==================================================
	CursorType(@NotNull com.mojang.blaze3d.platform.cursor.CursorType nativeType) {
		this.nativeType = nativeType;
	}
	// ==================================================
	/**
	 * Returns the game's corresponding {@link com.mojang.blaze3d.platform.cursor.CursorType}
	 * intance that is represented by this {@link CursorType}.
	 * <p>
	 * Used internally by this API mod to display said cursor on screen.
	 */
	@ApiStatus.Internal
	public final com.mojang.blaze3d.platform.cursor.CursorType getNative() {
		return this.nativeType;
	}
	// ==================================================
}
