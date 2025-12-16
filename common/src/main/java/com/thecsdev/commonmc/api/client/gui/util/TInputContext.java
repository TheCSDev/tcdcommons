package com.thecsdev.commonmc.api.client.gui.util;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Holds information about a user's input.
 */
public final class TInputContext
{
	// ==================================================
	/**
	 * This refers to the current "phase" at which an input is being handled.<br/>
	 * Each input phase has its own unique behaviors.
	 */
	public enum InputDiscoveryPhase
	{
		/**
		 * During this input phase, the input is forwarded to all elements,
		 * sequentially, starting from the root parent.
		 * <p>
		 * This phase is solely for the purpose of elements being able to
		 * know an input took place. Any elements handling the input on
		 * this phase will have no effect on the input propagation, and
		 * will not be able to prevent the {@link InputDiscoveryPhase}s that follow.
		 */
		BROADCAST,

		/**
		 * This phase is similar to {@link #BROADCAST}, except elements are
		 * able to stop the input propagation by handling the input. Doing
		 * so will prevent {@link #MAIN} from being handled.
		 * @see #BROADCAST
		 * @see #MAIN
		 */
		PREEMPT,

		/**
		 * During this input phase, the input is forwarded to the
		 * currently focused or hovered element (depending on the input type),
		 * after which the input "bubbles" towards the root parent element,
		 * until the input gets handled by the target element or one of its parents.
		 */
		MAIN
	}
	// --------------------------------------------------
	/**
	 * Represents an input's type. This tells you whether an
	 * input is keyboard or mouse related, and what kind of input it is.
	 */
	public enum InputType
	{
		KEY_PRESS, KEY_RELEASE, CHAR_TYPE,
		MOUSE_PRESS, MOUSE_RELEASE, MOUSE_MOVE, MOUSE_SCROLL, MOUSE_DRAG;

		public final boolean isKey() { return (this == KEY_PRESS) || (this == KEY_RELEASE); }
		public final boolean isMouse() {
			return (this == MOUSE_PRESS) || (this == MOUSE_RELEASE) || (this == MOUSE_MOVE) ||
					(this == MOUSE_SCROLL) || (this == MOUSE_DRAG);
		}
	}
	// ==================================================
	private final InputType inputType;
	private final @Nullable Integer keyCode, scanCode, keyModifiers;
	private final @Nullable Character character;
	private final @Nullable Double mouseX, mouseY;
	private final @Nullable Integer mouseButton;
	private final @Nullable Double scrollX, scrollY;
	private final @Nullable Double mouseDeltaX, mouseDeltaY;
	// ==================================================
	private TInputContext(
			InputType inputType,
			@Nullable Integer keyCode, @Nullable Integer scanCode, @Nullable Integer keyModifiers,
			@Nullable Character character,
			@Nullable Double mouseX, @Nullable Double mouseY, @Nullable Integer mouseButton,
			@Nullable Double scrollX, @Nullable Double scrollY,
			@Nullable Double mouseDeltaX, @Nullable Double mouseDeltaY)
	{
		this.inputType    = Objects.requireNonNull(inputType);
		this.keyCode      = keyCode;
		this.scanCode     = scanCode;
		this.keyModifiers = keyModifiers;
		this.character    = character;
		this.mouseX       = mouseX;
		this.mouseY       = mouseY;
		this.mouseButton  = mouseButton;
		this.scrollX      = scrollX;
		this.scrollY      = scrollY;
		this.mouseDeltaX  = mouseDeltaX;
		this.mouseDeltaY  = mouseDeltaY;
	}
	// ==================================================
	public final @Override int hashCode()
	{
		return Objects.hash(this.inputType, this.keyCode, this.scanCode, this.keyModifiers,
				this.character, this.mouseX, this.mouseY, this.mouseButton, this.scrollX,
				this.scrollY, this.mouseDeltaX, this.mouseDeltaY);
	}
	public final @Override boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj instanceof TInputContext other)
			return Objects.equals(this.inputType, other.inputType) &&
				Objects.equals(this.keyCode, other.keyCode) &&
				Objects.equals(this.scanCode, other.scanCode) &&
				Objects.equals(this.keyModifiers, other.keyModifiers) &&
				Objects.equals(this.character, other.character) &&
				Objects.equals(this.mouseX, other.mouseX) &&
				Objects.equals(this.mouseY, other.mouseY) &&
				Objects.equals(this.mouseButton, other.mouseButton) &&
				Objects.equals(this.scrollX, other.scrollX) &&
				Objects.equals(this.scrollY, other.scrollY) &&
				Objects.equals(this.mouseDeltaX, other.mouseDeltaX) &&
				Objects.equals(this.mouseDeltaY, other.mouseDeltaY);
		else return false;
	}
	// --------------------------------------------------
	/**
	 * Creates and returns a {@link TInputContext} for {@link InputType#KEY_PRESS}.
	 * @param keyCode The key code.
	 * @param scanCode The scan code.
	 * @param modifiers The key modifiers.
	 */
	public static final TInputContext ofKeyPress(int keyCode, int scanCode, int modifiers) {
		return new TInputContext(InputType.KEY_PRESS, keyCode, scanCode, modifiers, null, null, null, null, null, null, null, null);
	}

	/**
	 * Creates and returns a {@link TInputContext} for {@link InputType#KEY_RELEASE}.
	 * @param keyCode The key code.
	 * @param scanCode The scan code.
	 * @param modifiers The key modifiers.
	 */
	public static final TInputContext ofKeyRelease(int keyCode, int scanCode, int modifiers) {
		return new TInputContext(InputType.KEY_RELEASE, keyCode, scanCode, modifiers, null, null, null, null, null, null, null, null);
	}

	/**
	 * Creates and returns a {@link TInputContext} for {@link InputType#CHAR_TYPE}.
	 * @param character The typed {@link Character}.
	 * @param modifiers The key modifiers.
	 */
	public static final TInputContext ofCharType(char character, int modifiers) {
		return new TInputContext(InputType.CHAR_TYPE, null, null, modifiers, character, null, null, null, null, null, null, null);
	}

	/**
	 * Creates and returns a {@link TInputContext} for {@link InputType#MOUSE_PRESS}.
	 * @param mouseX Mouse X position.
	 * @param mouseY Mouse Y position.
	 * @param button The pressed mouse button.
	 */
	public static final TInputContext ofMousePress(double mouseX, double mouseY, int button) {
		return new TInputContext(InputType.MOUSE_PRESS, null, null, null, null, mouseX, mouseY, button, null, null, null, null);
	}

	/**
	 * Creates and returns a {@link TInputContext} for {@link InputType#MOUSE_RELEASE}.
	 * @param mouseX Mouse X position.
	 * @param mouseY Mouse Y position.
	 * @param button The pressed mouse button.
	 */
	public static final TInputContext ofMouseRelease(double mouseX, double mouseY, int button) {
		return new TInputContext(InputType.MOUSE_RELEASE, null, null, null, null, mouseX, mouseY, button, null, null, null, null);
	}

	/**
	 * Creates and returns a {@link TInputContext} for {@link InputType#MOUSE_RELEASE}.
	 * @param mouseX Mouse X position.
	 * @param mouseY Mouse Y position.
	 */
	public static final TInputContext ofMouseMove(double mouseX, double mouseY) {
		return new TInputContext(InputType.MOUSE_MOVE, null, null, null, null, mouseX, mouseY, null, null, null, null, null);
	}

	/**
	 * Creates and returns a {@link TInputContext} for {@link InputType#MOUSE_SCROLL}.
	 * @param mouseX Mouse X position.
	 * @param mouseY Mouse Y position.
	 * @param scrollX Horizontal scroll amount.
	 * @param scrollY Vertical scroll amount.
	 */
	public static final TInputContext ofMouseScroll(double mouseX, double mouseY, double scrollX, double scrollY) {
		return new TInputContext(InputType.MOUSE_SCROLL, null, null, null, null, mouseX, mouseY, null, scrollX, scrollY, null, null);
	}

	/**
	 * Creates and returns a {@link TInputContext} for {@link InputType#MOUSE_DRAG}.
	 * @param mouseX Mouse X position.
	 * @param mouseY Mouse Y position.
	 * @param button The pressed mouse button.
	 * @param deltaX The mouse movement's delta X.
	 * @param deltaY The mouse movement's delta Y.
	 */
	public static final TInputContext ofMouseDrag(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		return new TInputContext(InputType.MOUSE_DRAG, null, null, null, null, mouseX, mouseY, button, null, null, deltaX, deltaY);
	}
	// ==================================================
	/**
	 * Returns the {@link InputType}.
	 */
	public final InputType getInputType() { return this.inputType; }

	/**
	 * @see InputType#KEY_PRESS
	 * @see InputType#KEY_RELEASE
	 */
	public final @Nullable Integer getKeyCode() { return this.keyCode; }

	/**
	 * @see InputType#KEY_PRESS
	 * @see InputType#KEY_RELEASE
	 */
	public final @Nullable Integer getScanCode() { return this.scanCode; }

	/**
	 * @see InputType#KEY_PRESS
	 * @see InputType#KEY_RELEASE
	 * @see InputType#CHAR_TYPE
	 */
	public final @Nullable Integer getKeyModifiers() { return this.keyModifiers; }

	/**
	 * @see InputType#CHAR_TYPE
	 */
	public final @Nullable Character getCharacter() { return this.character; }

	/**
	 * @see InputType#MOUSE_PRESS
	 * @see InputType#MOUSE_RELEASE
	 * @see InputType#MOUSE_MOVE
	 * @see InputType#MOUSE_SCROLL
	 */
	public final @Nullable Double getMouseX() { return this.mouseX; }

	/**
	 * @see InputType#MOUSE_PRESS
	 * @see InputType#MOUSE_RELEASE
	 * @see InputType#MOUSE_MOVE
	 * @see InputType#MOUSE_SCROLL
	 */
	public final @Nullable Double getMouseY() { return this.mouseY; }

	/**
	 * @see InputType#MOUSE_PRESS
	 * @see InputType#MOUSE_RELEASE
	 */
	public final @Nullable Integer getMouseButton() { return this.mouseButton; }

	/**
	 * @see InputType#MOUSE_SCROLL
	 */
	public final @Nullable Double getScrollX() { return this.scrollX; }

	/**
	 * @see InputType#MOUSE_SCROLL
	 */
	public final @Nullable Double getScrollY() { return this.scrollY; }

	/**
	 * @see InputType#MOUSE_DRAG
	 */
	public final @Nullable Double getMouseDeltaX() { return this.mouseDeltaX; }

	/**
	 * @see InputType#MOUSE_DRAG
	 */
	public final @Nullable Double getMouseDeltaY() { return this.mouseDeltaY; }
	// ==================================================
}