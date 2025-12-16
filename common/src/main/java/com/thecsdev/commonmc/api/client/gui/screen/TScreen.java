package com.thecsdev.commonmc.api.client.gui.screen;

import com.thecsdev.common.math.Bounds2i;
import com.thecsdev.common.properties.NotNullProperty;
import com.thecsdev.common.properties.ObjectProperty;
import com.thecsdev.common.util.annotations.Virtual;
import com.thecsdev.commonmc.api.client.gui.TElement;
import com.thecsdev.commonmc.api.client.gui.util.TGuiUtils;
import com.thecsdev.commonmc.api.client.gui.util.TInputContext;
import com.thecsdev.commonmc.client.mixin.hooks.AccessorTElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static com.thecsdev.commonmc.api.client.gui.screen.ILastScreenProvider.getLastScreen;

/**
 * An abstraction layer that represents a game's {@link Screen}.
 * @see TScreenWrapper
 */
public abstract class TScreen extends TElement
{
	// ==================================================
	@Internal final TScreenWrapper<?>          screen  = Objects.requireNonNull(createWrapperScreen());
	// --------------------------------------------------
	@Internal final NotNullProperty<Component> title   = new NotNullProperty<>(Component.empty());
	@Internal final ObjectProperty<TElement>   hovered = new ObjectProperty<>(null);
	@Internal final ObjectProperty<TElement>   focused = new ObjectProperty<>(null);
	@Internal final ObjectProperty<TElement>   dragged = new ObjectProperty<>(null);
	// ==================================================
	public TScreen(@NotNull Component title) {
		this();
		titleProperty().set(title, TScreen.class);
	}
	public TScreen()
	{
		//filter out invalid elements from hover/focus/drag
		this.hovered.addFilter(el -> (el != null && el.screenProperty().get() != this) ? null : el, TScreen.class);
		this.focused.addFilter(el -> (el != null && el.screenProperty().get() != this) ? null : el, TScreen.class);
		this.dragged.addFilter(el -> (el != null && el.screenProperty().get() != this) ? null : el, TScreen.class);

		//invalidate tooltip caches for hovered/focused elements as they change
		this.hovered.addChangeListener((p, o, n) -> {
			if(o != null) ((AccessorTElement)(Object)o)._hoverLostCallback();
			if(n != null) {
				n.invalidateTooltipCache();
				((AccessorTElement)(Object)n)._hoverGainedCallback();
			}
		});
		this.focused.addChangeListener((p, o, n) -> {
			if(o != null) ((AccessorTElement)(Object)o)._focusLostCallback();
			if(n != null) {
				n.invalidateTooltipCache();
				((AccessorTElement)(Object)n)._focusGainedCallback();
				TGuiUtils.scrollToElement(n); //scroll to element AFTER notifying it
			}
		});
		this.dragged.addChangeListener((p, o, n) -> {
			if(o != null) ((AccessorTElement)(Object)o)._dragEndCallback();
			if(n != null) ((AccessorTElement)(Object)n)._dragStartCallback();
		});

		//the bounds' X and Y must both be 0
		boundsProperty().addFilter(
				bb -> (bb.x != 0 || bb.y != 0) ? new Bounds2i(0, 0, bb.width, bb.height) : bb,
				TScreen.class);
		//handle stuff on resize
		boundsProperty().addChangeListener((p, o, n) -> {
			//clear hovered and focused elements on resize, as they are no longer valid
			this.hovered.set(null, TScreen.class); //clear and init is what makes it invalid
			this.focused.set(null, TScreen.class); //clear and init is what makes it invalid
			this.dragged.set(null, TScreen.class); //clear and init is what makes it invalid
			//reinitialize this screen
			clearAndInit();
		});
	}
	// ==================================================
	/**
	 * Creates a {@link TScreenWrapper} instance for this {@link TScreen}.
	 * @see #getAsScreen()
	 */
	protected @Virtual @NotNull TScreenWrapper<?> createWrapperScreen() { return new TScreenWrapper<>(this); }

	/**
	 * Returns the {@link Screen} representation of this {@link TScreen}.
	 */
	public final @NotNull TScreenWrapper<?> getAsScreen() { return this.screen; }

	/**
	 * Returns the {@link Minecraft} client instance that last opened
	 * this {@link TScreen}, if any.
	 */
	public final @Nullable @Override Minecraft getClient() { return this.screen.getClient(); }
	// --------------------------------------------------
	/**
	 * Returns the {@link NotNullProperty} for this {@link TScreen}'s title.
	 */
	public final NotNullProperty<Component> titleProperty() { return this.title; }

	/**
	 * Returns the {@link ObjectProperty} for the currently
	 * mouse-hovered {@link TElement}.
	 * @apiNote Is read only! Set calls will {@code throw}!
	 */
	public final ObjectProperty<TElement> hoveredElementProperty() { return this.hovered; }

	/**
	 * Returns the {@link ObjectProperty} for the currently
	 * focused {@link TElement}.
	 * @apiNote Cannot be set to a {@link TElement} that is not a child or
	 * grandchild of this {@link TScreen}.
	 */
	public final ObjectProperty<TElement> focusedElementProperty() { return this.focused; }
	// ==================================================
	/**
	 * Returns {@code true} if this {@link TScreen} is currently opened by
	 * a {@link Minecraft} client instance.
	 * @see Minecraft#screen
	 */
	@Contract(pure = true)
	public final boolean isOpen() {
		final           var screen = getAsScreen();
		final @Nullable var client = screen.getClient();
		return (client != null && client.screen == screen);
	}

	/**
	 * Returns a {@code boolean} indicating whether the game
	 * should be paused while this {@link TScreen} is open.
	 * @apiNote Game pausing might not take place in multiplayer.
	 */
	@Contract(pure = true)
	public @Virtual boolean isPauseScreen() { return true; }

	/**
	 * Returns {@code true} if the in-game HUD (heads-up display) GUI
	 * should be rendered while this {@link TScreen} is open.
	 * If {@code false}, the HUD will not be rendered while this
	 * {@link TScreen} is open.
	 */
	@Contract(pure = true)
	public @Virtual boolean isAllowingInGameHud() { return true; }
	// --------------------------------------------------
	/**
	 * Emulates a user input event for this {@link TScreen}.
	 * @param context Information about the user's input.
	 * @return {@code true} if the input event was handled by a GUI element.
	 * @apiNote Does not affect the native GUI.
	 */
	public final boolean sendInput(@NotNull TInputContext context) { return this.screen.sendInput(context); }
	// ==================================================
	protected abstract @Override void initCallback(); //forced to be overridden

	/**
	 * Callback method that is invoked whenever this {@link TScreen} is opened
	 * via {@link Minecraft#setScreen(Screen)}.<br>
	 * This takes place before {@link #initCallback()} is invoked.
	 * @see Screen#added()
	 */
	protected @Virtual void openCallback() {}

	/**
	 * Callback method that is invoked whenever this {@link TScreen} is closed
	 * via {@link Minecraft#setScreen(Screen)}.<br>
	 * This takes place after {@link #close()} is invoked.
	 * @see Screen#removed()
	 */
	protected @Virtual void closeCallback() {}

	/**
	 * Callback method that is invoked whenever this {@link TScreen} is closing.
	 * By default, this sets {@link Minecraft#screen} to {@code null}, but you
	 * may override this to set another {@link Screen} instance if necessary.
	 * @throws IllegalStateException Whenever {@link #getClient()} returns {@code null}.
	 * @apiNote This method <b>must</b> use {@link Minecraft#setScreen(Screen)}.
	 */
	public @Virtual void close() throws IllegalStateException {
		//obtain client instance and ensure its presence
		final @Nullable var client = getClient();
		if(client == null) throw new IllegalStateException("Missing 'client' instance");
		//set screen to last screen if available, else to null
		if(isOpen()) client.setScreen(getLastScreen(this));
	}
	// ==================================================
}
