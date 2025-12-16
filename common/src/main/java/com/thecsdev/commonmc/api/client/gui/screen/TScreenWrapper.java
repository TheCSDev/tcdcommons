package com.thecsdev.commonmc.api.client.gui.screen;

import com.thecsdev.common.math.Bounds2i;
import com.thecsdev.common.util.annotations.Virtual;
import com.thecsdev.commonmc.api.client.gui.TElement;
import com.thecsdev.commonmc.api.client.gui.render.TGuiGraphics;
import com.thecsdev.commonmc.api.client.gui.util.TGuiUtils;
import com.thecsdev.commonmc.api.client.gui.util.TInputContext;
import com.thecsdev.commonmc.client.mixin.hooks.AccessorTElement;
import io.netty.util.internal.UnstableApi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

import static com.thecsdev.commonmc.TCDCommons.LOGGER;
import static com.thecsdev.commonmc.TCDCommonsConfig.FLAG_DEV_ENV;
import static com.thecsdev.commonmc.api.client.gui.util.TGuiUtils.isShiftDown;
import static java.lang.System.nanoTime;
import static org.lwjgl.glfw.GLFW.*;

/**
 * The {@link TScreenWrapper} serves as an adapter for the {@link TScreen}
 * class. This class extends the game's {@link Screen} class and translates
 * calls from the game's engine into calls on the {@link TScreen} instances.
 *
 * <p>The purpose of this class is to isolate {@link TScreen} from Minecraft's
 * GUI code, making the mod more resilient to changes in the game's code. This
 * isolation makes {@link TScreen} act like a {@link Screen}, but in a more controlled
 * and independent way, similar to how custom rendering engines interact with rendering APIs.
 *
 * <p>This class should remain thin, serving only as a pass-through layer to
 * {@link TScreen}. All interactions with the {@link Screen} class that the mod
 * needs should be encapsulated within this class, keeping {@link TScreen}
 * unaware of the game's GUI code.
 *
 * @apiNote {@link UnstableApi}. Changes to the game's code, as well as the
 * default {@link TScreenWrapper} implementation, may cause disruptions.
 */
@UnstableApi
public @Virtual class TScreenWrapper<T extends TScreen> extends Screen
{
	// ==================================================
	private final @NotNull T target;
	// ==================================================
	protected TScreenWrapper(@NotNull T target) {
		super(Component.empty());
		this.target = Objects.requireNonNull(target);
	}
	// ==================================================
	/**
	 * Returns the {@link #target} {@link TScreen} for this {@link TScreenWrapper}.
	 */
	public final T getTargetTScreen() { return this.target; }

	/**
	 * Returns the {@link Minecraft} client instance that last opened
	 * this {@link Screen}, if any.
	 */
	public final @NotNull Minecraft getClient() { return this.minecraft; }
	// ==================================================
	/** @see TScreen#isAllowingInGameHud() */
	public final boolean isAllowingInGameHud() { return this.target.isAllowingInGameHud(); }
	// ==================================================
	//escape is handled independently in this framework
	public final @Override boolean shouldCloseOnEsc() { return false; }
	public final @Override boolean isPauseScreen() { return this.target.isPauseScreen(); }
	// --------------------------------------------------
	//TODO - These two should not invoke while the client is closing:
	public final @Override void added() { this.target.openCallback(); }
	public final @Override void removed() {
		this.target.closeCallback();
		//clean-up tasks for if closed - removing elements triggers their cleanup handlers
		if(!this.target.isOpen()) this.target.clear();
	}
	// --------------------------------------------------
	//note: the game should rename this method to #close(). current name "onClose" is confusing
	//      because it implies the screen had been closed by now when that's not the case.
	public final @Override void onClose() { //(intentionally overrides 'super')
		if(this.target.isOpen()) this.target.close();
	}
	// ==================================================
	protected final @Override void init()
	{
		//begin measuring initialization time
		final var ns = nanoTime();
		//trigger (re/)initialization by updating the bounds
		this.target.boundsProperty().getHandle().set(Bounds2i.ZERO); //so next call triggers change listeners
		this.target.setBounds(0, 0, this.width, this.height);        //<- this now triggers change listeners
		//initialize super
		super.init();
		//log initialization time
		if(FLAG_DEV_ENV)
			LOGGER.info("Initialized '" + this.target.getClass() + "' in " + (nanoTime() - ns) + "ns.");
	}
	public final @Override void tick() {
		((AccessorTElement)(Object)this.target)._tick();
		super.tick();
	}
	public final @Override void render(@NotNull GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
		final var tgg = TGuiGraphics.createInstance(context, mouseX, mouseY, deltaTicks);
		tgg.renderTScreen(this.target);
		super.render(context, mouseX, mouseY, deltaTicks);
	}
	// ==================================================
	public final @Override boolean keyPressed(@NotNull KeyEvent e) {
		if(super.keyPressed(e)) return true;
		else return sendInput(TInputContext.ofKeyPress(e.key(), e.scancode(), e.modifiers()));
	}
	public final @Override boolean keyReleased(@NotNull KeyEvent e) {
		if(super.keyReleased(e)) return true;
		else return sendInput(TInputContext.ofKeyRelease(e.key(), e.scancode(), e.modifiers()));
	}
	public final @Override boolean charTyped(@NotNull CharacterEvent e) {
		if(super.charTyped(e)) return true;
		else return sendInput(TInputContext.ofCharType((char) e.codepoint(), e.modifiers()));
	}
	// --------------------------------------------------
	public final @Override boolean mouseClicked(@NotNull MouseButtonEvent e, boolean doubled) {
		if(super.mouseClicked(e, doubled)) return true;
		else return sendInput(TInputContext.ofMousePress(e.x(), e.y(), e.button()));
	}
	public final @Override boolean mouseReleased(@NotNull MouseButtonEvent e) {
		if(super.mouseReleased(e)) return true;
		else return sendInput(TInputContext.ofMouseRelease(e.x(), e.y(), e.button()));
	}
	public final @Override void mouseMoved(double mouseX, double mouseY) {
		super.mouseMoved(mouseX, mouseY);
		sendInput(TInputContext.ofMouseMove(mouseX, mouseY));
	}
	public final @Override boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if(super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) return true;
		else return sendInput(TInputContext.ofMouseScroll(mouseX, mouseY, horizontalAmount, verticalAmount));
	}
	public final @Override boolean mouseDragged(@NotNull MouseButtonEvent e, double deltaX, double deltaY) {
		if(super.mouseDragged(e, deltaX, deltaY)) return true;
		else return sendInput(TInputContext.ofMouseDrag(e.x(), e.y(), e.button(), deltaX, deltaY));
	}
	// ==================================================
	/**
	 * Emulates a user input event for this {@link TScreen}.
	 * @param context Information about the user's input.
	 * @return {@code true} if the input event was handled by a GUI element.
	 * @apiNote Does not affect the native GUI. Is {@link ApiStatus.Internal} and may change.
	 */
	//FIXME - Extremely high priority. I urgently need a "set in stone" input handling contract.
	//        I keep finding myself needing to tweak input handling. I need standardization.
	@ApiStatus.Internal
	final boolean sendInput(TInputContext context)
	{
		//calculate hovered element for mouse-related inputs
		if(context.getInputType().isMouse()) {
			final @Nullable Double mouseX = context.getMouseX();
			final @Nullable Double mouseY = context.getMouseY();
			if(mouseX != null && mouseY != null)
				this.target.hovered.set(
						this.target.findElementAt(
								(int)mouseX.doubleValue(),
								(int)mouseY.doubleValue()),
						TScreenWrapper.class);
		}

		//broadcast phase
		this.target.inputCallback(TInputContext.InputDiscoveryPhase.BROADCAST, context);
		this.target.forEach(child -> child.inputCallback(TInputContext.InputDiscoveryPhase.BROADCAST, context), true);

		//preempt phase
		if(this.target.inputCallback(TInputContext.InputDiscoveryPhase.PREEMPT, context))
			return true;
		else if(this.target.findChild(child -> child.inputCallback(TInputContext.InputDiscoveryPhase.PREEMPT, context), true).isPresent())
			return true;

		//main phase
		switch(context.getInputType())
		{
			//mouse-press goes to the hovered element.
			case MOUSE_PRESS:
			{
				//TODO - Mouse click should bubble to hovered elements, not to parent elements!
				//mouse-press is sent to hovered element, with bubbling-to-parents logic
				final @Nullable var handled = sendInputBubbleMain(context, target.hovered.get());
				//element that handles the input becomes focused and dragged. no handling clears focus
				this.target.focused.set((handled instanceof TElement handledE) ?
						(handledE.isFocusable() ? handledE : null) : null,
						TScreenWrapper.class);
				this.target.dragged.set(handled, TScreenWrapper.class); //FIXME - Keep track of which mouse button is dragging an element. Right now all button releases can end the drag.
				//true is returned if an element did handle the input
				if(handled != null) return true; else break;
			}

			//mouse-drag goes to the dragged element,
			case MOUSE_DRAG: {
				final @Nullable var draggedEl = this.target.dragged.get();
				if(draggedEl != null && draggedEl.inputCallback(TInputContext.InputDiscoveryPhase.MAIN, context))
					return true;
				else break;
			}
			//mouse-release goes to the focused element
			case MOUSE_RELEASE: //INTENTIONAL! MOUSE RELEASE WILL FIRE ON FOCUSED ELEMENTS REGARDLESS OF HOVER!
			//key-press, key-release, and char-type go to focused element
			case KEY_PRESS:
			case KEY_RELEASE:
			case CHAR_TYPE:
			{
				//mouse release clears dragged element
				if(context.getInputType() == TInputContext.InputType.MOUSE_RELEASE) {
					//clear the dragged element, but do keep track of what element that was
					final @Nullable var dragged = this.target.dragged.get();
					this.target.dragged.set(null, TScreenWrapper.class);
					//then, if the dragged element does not have any focus, send mouse release
					//input to it (if focused, it will get that input anyway. here we just make
					//sure it gets the input in cases where it's not focused)
					if(dragged != null && this.target.focused.get() != dragged)
						dragged.inputCallback(TInputContext.InputDiscoveryPhase.MAIN, context);
				}
				//forward the input to the focused element or the target screen
				final var focusedEl = Optional.ofNullable(this.target.focused.get()).orElse(this.target);
				//if(focusedEl.inputCallback(TInputContext.InputDiscoveryPhase.MAIN, context))
				if(sendInputBubbleMain(context, focusedEl) != null)
					return true;
				else break;
			}

			//mouse-scroll goes to the hovered element
			case MOUSE_SCROLL:
			{
				//TODO - Mouse scroll should bubble to hovered elements, not to parent elements!
				//mouse-scroll is sent to hovered element, with bubbling-to-parents logic
				final @Nullable var handled = sendInputBubbleMain(context, target.hovered.get());
				//true is returned if an element did handle the input
				if(handled != null) return true; else break;
			}

			//not handled yet? this is the end. return false
			default: break;
		}

		//additional key-press logic
		if(context.getInputType() == TInputContext.InputType.KEY_PRESS)
		{
			//handle tab-navigation
			//noinspection DataFlowIssue
			if(context.getKeyCode() == GLFW_KEY_TAB)
			{
				//prepare for navigation
				final var          forward = !isShiftDown();
				@Nullable TElement current = this.target.focused.get();

				//determine next element that will gain focus
				if(current == null)
					current = forward ?
						TGuiUtils.nextFocusableTElement(this.target, true) :
						TGuiUtils.previousFocusableTElement(this.target, true);
				else
					current = forward ?
							TGuiUtils.nextFocusableTElement(current, false) :
							TGuiUtils.previousFocusableTElement(current, false);

				//assign focus, and return true
				this.target.focused.set(current, TScreenWrapper.class);
				return true;
			}
			//handle context menu key
			else if(context.getKeyCode() == GLFW_KEY_MENU) {
				//attept to show the context menu of the focused element
				final @Nullable var focused = this.target.focused.get();
				if(focused != null && focused.showContextMenu() != null)
					return true; //return ONLY IF successful, no returning false!
			}
			//handle closing on escape
			else if(context.getKeyCode() == GLFW_KEY_ESCAPE) { onClose(); return true; }
		}

		//additional mouse-press logic
		else if(context.getInputType() == TInputContext.InputType.MOUSE_PRESS) {
			//noinspection DataFlowIssue - handle right-clicking for opening context-menus
			if(context.getMouseButton() == 1) {
				//attept to show the context menu of the hovered element
				final @Nullable var hovered = this.target.hovered.get();
				if(hovered != null && hovered.showContextMenu() != null)
					return true; //return ONLY IF successful, no returning false!
			}
		}

		//default outcome; not handled
		return false;
	}

	/**
	 * {@link ApiStatus.Internal} method that sends an input to a {@link TElement}, bubbling
	 * the input to its parents if said element does not handle the input.
	 * @apiNote Uses {@link TInputContext.InputDiscoveryPhase#MAIN}.
	 */
	@ApiStatus.Internal
	private static final TElement sendInputBubbleMain(TInputContext context, @Nullable TElement element)
	{
		//null checks
		if(element == null) return null;

		//forward input to element, handling bubbling if element doesn't handle it
		while(element != null && !element.inputCallback(TInputContext.InputDiscoveryPhase.MAIN, context))
			element = element.getParent();

		//return element that handled the input, or null if none handled it
		return element;
	}
	// ==================================================
}
