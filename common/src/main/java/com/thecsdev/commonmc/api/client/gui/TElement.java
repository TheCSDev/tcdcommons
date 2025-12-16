package com.thecsdev.commonmc.api.client.gui;

import com.thecsdev.common.math.Bounds2i;
import com.thecsdev.common.math.UDim;
import com.thecsdev.common.math.UDim2;
import com.thecsdev.common.properties.BooleanProperty;
import com.thecsdev.common.properties.NotNullProperty;
import com.thecsdev.common.properties.ObjectProperty;
import com.thecsdev.common.scene.INodeBounded;
import com.thecsdev.common.scene.INodeRenderable;
import com.thecsdev.common.scene.Node;
import com.thecsdev.common.util.annotations.Reflected;
import com.thecsdev.common.util.annotations.Virtual;
import com.thecsdev.commonmc.TCDCommons;
import com.thecsdev.commonmc.api.client.gui.ctxmenu.TContextMenu;
import com.thecsdev.commonmc.api.client.gui.render.TGuiGraphics;
import com.thecsdev.commonmc.api.client.gui.screen.TScreen;
import com.thecsdev.commonmc.api.client.gui.util.CursorType;
import com.thecsdev.commonmc.api.client.gui.util.SceneGraphPath;
import com.thecsdev.commonmc.api.client.gui.util.TGuiUtils;
import com.thecsdev.commonmc.api.client.gui.util.TInputContext;
import com.thecsdev.commonmc.client.mixin.hooks.AccessorTElement;
import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.thecsdev.common.util.ReflectionUtils.isMethodOverridden;
import static com.thecsdev.commonmc.api.client.gui.TElement.TElementPropertyAccessor.setScreenValue;

/**
 * Represents a GUI element in {@link TCDCommons}'s GUI system.
 */
@Reflected(AccessorTElement.class)
public @Virtual class TElement extends Node<TElement> implements INodeBounded<TElement, Bounds2i>, INodeRenderable<TElement, TGuiGraphics>
{
	// ================================================== ==================================================
	//                                           TElement IMPLEMENTATION
	// ================================================== ==================================================
	private final NotNullProperty<Bounds2i> bounds           = new NotNullProperty<>(Bounds2i.ZERO);
	private final ObjectProperty<TScreen>   screen           = new ObjectProperty<>(null);
	// --------------------------------------------------
	private final BooleanProperty           visible          = new BooleanProperty(true);
	private final BooleanProperty           clipsDescendants = new BooleanProperty(true);
	private final BooleanProperty           focusable        = new BooleanProperty(false);
	private final BooleanProperty           hoverable        = new BooleanProperty(true);
	// --------------------------------------------------
	private final ObjectProperty<Function<TElement, TElement>>     tooltip     = new ObjectProperty<>(null);
	private final ObjectProperty<Function<TElement, TContextMenu>> contextMenu = new ObjectProperty<>(null);
	// --------------------------------------------------
	private @Nullable Bounds2i contentBounds  = null; //null = "dirty"
	private @Nullable TElement currentTooltip = null; //null = "dirty"
	// ==================================================
	/**
	 * An event that is invoked whenever {@link #clearAndInit()} finishes executing.
	 * <p>
	 * The {@link Consumer} provides an instance of {@code this} for convenience.
	 */
	public final Event<Consumer<TElement>> eInitialized = EventFactory.createLoop();
	// ==================================================
	public TElement()
	{
		//initialize the screen property
		this.screen.setReadOnly(true, TElement.class);
		this.screen.setOwner(TElementPropertyAccessor.class, TElement.class);

		//handle changes
		parentProperty().addChangeListener((p, o, n) -> {
			//update the screen property when the parent changes
			setScreenValue(this, (TScreen) findParent(ps -> ps instanceof TScreen).orElse(null));
			//invalidate content bounds of past and new parents
			if(o != null) o.contentBounds = null;
			if(n != null) n.contentBounds = null;
		});
		this.screen.addChangeListener((p, o, n) -> {
			//update children's screen values
			for(final var child : this) setScreenValue(child, n);
			//remove focus/hover from old screen if left over
			if(o != null) {
				//TODO - TElement has to lie here. Not ideal.
				if(o.hoveredElementProperty().get() == this)
					o.hoveredElementProperty().set(null, TScreen.class);
				if(o.focusedElementProperty().get() == this)
					o.focusedElementProperty().set(null, TScreen.class);
			}
		});
		boundsProperty().addChangeListener((p, o, n) -> {
			//invalidate this element's content bounds
			this.contentBounds = null; //important too
			//invalidate parent content bounds when this element's bounds change
			final @Nullable var pe = getParent();
			if(pe != null) pe.contentBounds = null;
		});
		this.tooltip.addChangeListener((p, o, n) -> invalidateTooltipCache());
	}
	// ==================================================
	public final @NotNull @Override TElement getSelf() { return this; }
	public final @NotNull @Override Class<TElement> getBaseType() { return TElement.class; }
	public @Virtual @Override void renderCallback(@NotNull TGuiGraphics pencil) {}
	public @Virtual @Override void postRenderCallback(@NotNull TGuiGraphics pencil) {}
	// --------------------------------------------------
	public @Virtual @Override String toString() {
		final var bb = getBounds();
		return String.format("%s[x=%d,y=%d,width=%d,height=%d]",
				super.toString(), bb.x, bb.y, bb.width, bb.height);
	}
	// --------------------------------------------------
	public final @NotNull @Override Bounds2i getBounds() { return this.bounds.get(); }

	/**
	 * Returns the {@link Bounds2i} that encapsulates all child bounding boxes
	 * as one. The returned bounding box shall represent the exact space all
	 * children's bounding boxes collectively occupy at the moment. This is
	 * done using each child's {@link #getBounds()}.
	 * <p>
	 * Does <b>NOT</b> return {@code null}. If there are no children, returns
	 * a {@link Bounds2i} instance that has this {@link TElement}'s position
	 * but zero size (aka width and height of 0).
	 */
	public final @NotNull Bounds2i getContentBounds()
	{
		//obtain the current content bounds value
		@Nullable var cb = this.contentBounds;
		//if "dirty", recalculate it
		if(cb == null)
		{
			//a bunch of bla bla bla math to calculate the content bounding box
			if(!isEmpty()) {
				//when there are children, calculate the bounding box that contains them all
				int sX = Integer.MAX_VALUE, sY = Integer.MAX_VALUE, eX = Integer.MIN_VALUE, eY = Integer.MIN_VALUE;
				for(final var child : this) {
					final @NotNull var childBounds = child.getBounds();
					if(childBounds.x < sX) sX = childBounds.x;
					if(childBounds.y < sY) sY = childBounds.y;
					if(childBounds.endX > eX) eX = childBounds.endX;
					if(childBounds.endY > eY) eY = childBounds.endY;
				}
				cb = new Bounds2i(sX, sY, eX - sX, eY - sY);
			} else {
				//otherwise when there are no children, just use this element's position
				//and no size. this is more practical because the size of 0 explicitly
				//communicates that "there are no children"
				final var bb = getBounds();
				cb = new Bounds2i(bb.x, bb.y, 0, 0);
			}
			//assign the newly calculated value to the property
			this.contentBounds = cb;
		}
		//return the calculated value
		return cb;
	}
	// --------------------------------------------------
	/**
	 * Returns the {@link Minecraft} client instance that last opened
	 * this {@link TElement}'s (grand/)parent {@link TScreen}, if any.
	 */
	public @ApiStatus.NonExtendable @Nullable Minecraft getClient() {
		final @Nullable var screen = screenProperty().get();
		return (screen != null) ? screen.getClient() : null;
	}
	// ==================================================
	/**
	 * The {@link NotNullProperty} that holds the bounding box of this {@link TElement}.
	 */
	public final NotNullProperty<Bounds2i> boundsProperty() { return this.bounds; }
	// --------------------------------------------------
	/**
	 * Returns the {@link ObjectProperty} for this {@link TElement}'s
	 * parent/grandparent {@link TScreen} element.
	 * @apiNote Read only. Owned by {@link TElementPropertyAccessor}.
	 */
	public final ObjectProperty<TScreen> screenProperty() { return this.screen; }
	// --------------------------------------------------
	/**
	 * Returns the {@link BooleanProperty} that controls the visibility
	 * of this {@link TElement}. Invisible elements do not render and
	 * cannot be interacted with.
	 */
	public final BooleanProperty visibleProperty() { return this.visible; }

	/**
	 * Returns the {@link BooleanProperty} that controls whether this {@link TElement}
	 * will clip (hide) any part of its descendant elements that would extend beyond
	 * its rectangular boundaries.
	 */
	public final BooleanProperty clipsDescendantsProperty() { return this.clipsDescendants; }

	/**
	 * Returns the {@link BooleanProperty} that controls whether this {@link TElement}
	 * should be receiving focus via user input such as mouse and keyboard.
	 *
	 * @apiNote Not to be confused with {@link #isFocusable()}. This property is about
	 * telling the {@link TElement} if it should be focusable, and {@link #isFocusable()}
	 * is about the {@link TElement} making the final decision.
	 */
	public final BooleanProperty focusableProperty() { return this.focusable; }

	/**
	 * Returns the {@link BooleanProperty} that controls whether this {@link TElement}
	 * should be detected as hovered by the mouse cursor.
	 *
	 * @apiNote Not to be confused with {@link #isHoverable()}. This property is about
	 * telling the {@link TElement} if it should be hoverable, and {@link #isHoverable()}
	 * is about the {@link TElement} making the final decision.
	 */
	public final BooleanProperty hoverableProperty() { return this.hoverable; }
	// --------------------------------------------------
	/**
	 * The {@link ObjectProperty} that holds a supplier {@link Function} for the tooltip
	 * {@link TElement} that is to be rendered as this {@link TElement}'s tooltip.
	 * <p>
	 * Reason {@link Function}s are used is for optimization, as creating a {@link TElement}
	 * instance is much more expensive than a supplier {@link Function}.
	 * <p>
	 * The first argument of the {@link Function} is {@code this} {@link TElement} instance,
	 * and the {@link Function} is to return a new {@link TElement} instance acting as the tooltip.
	 * <p>
	 * While the function itself can be set to {@code null}, the {@link TElement} it returns
	 * <b>must not</b> be {@code null}!
	 * @apiNote The tooltip element is only rendered visually, should serve no functional
	 * purpose, and should not be a child of another element.
	 */
	public final ObjectProperty<Function<@NotNull TElement, @NotNull TElement>> tooltipProperty() { return this.tooltip; }

	/**
	 * The {@link ObjectProperty} that holds a supplier {@link Function} for the context menu
	 * {@link TContextMenu} that is to be shown when this {@link TElement} is right-clicked.
	 * <p>
	 * Reason {@link Function}s are used is for optimization, as creating a {@link TContextMenu}
	 * instance is much more expensive than a supplier {@link Function}.
	 * <p>
	 * The first argument of the {@link Function} is {@code this} {@link TElement} instance,
	 * and the {@link Function} is to return a new {@link TContextMenu} instance acting as
	 * the context menu.
	 * <p>
	 * The function itself can be set to {@code null}, and the {@link TContextMenu} it returns
	 * may also be {@code null}.
	 */
	public final ObjectProperty<Function<@NotNull TElement, @Nullable TContextMenu>> contextMenuProperty() { return this.contextMenu; }
	// ==================================================
	/**
	 * Returns {@code true} if this {@link TElement} is fully visible and is not
	 * being hidden by a parent {@link TElement} either.
	 */
	public final boolean isVisible() {
		@Nullable var parent = getParent();
		if(parent != null)
			return (this.visible.getZ() && parent.isVisible());
		else return this.visible.getZ();
	}

	/**
	 * Returns {@code true} if this {@link TElement} is currently being
	 * hovered by the mouse cursor.
	 * @apiNote Returns {@code false} if {@link #screenProperty()}'s value is {@code null}.
	 */
	public final boolean isHovered() {
		final @Nullable var s = screenProperty().get();
		return s != null && s.hoveredElementProperty().get() == this;
	}

	/**
	 * Returns {@code true} if this {@link TElement} is currently the focused
	 * element of the {@link TScreen}.
	 * @apiNote Returns {@code false} if {@link #screenProperty()}'s value is {@code null}.
	 */
	public final boolean isFocused() {
		final @Nullable var s = screenProperty().get();
		return s != null && s.focusedElementProperty().get() == this;
	}

	/**
	 * Returns {@link #isHovered()} || {@link #isFocused()}.
	 */
	public final boolean isHoveredOrFocused() { return isHovered() || isFocused(); }
	// --------------------------------------------------
	/**
	 * Returns {@code true} if this {@link TElement} should be receiving focus via
	 * user input such as mouse and keyboard.
	 * @apiNote Overrides should take {@link #focusableProperty()} into account.
	 * @see #focusableProperty()
	 */
	public @Virtual boolean isFocusable() { return this.focusable.getZ(); }

	/**
	 * Returns {@code true} if this {@link TElement} should be detected as hovered
	 * by the mouse cursor. When {@code false}, mouse hovering over this element will
	 * "go though" this element, thus hovering an element below it instead.
	 * @apiNote Overrides should take {@link #hoverableProperty()} into account.
	 * @see #hoverableProperty()
	 */
	public @Virtual boolean isHoverable() { return this.hoverable.getZ(); }
	// ==================================================
	/**
	 * Returns the tooltip {@link TElement} that should be rendered whenever this
	 * {@link TElement} is hovered.<br>
	 * If {@link #tooltipProperty()}'s value is {@code null}, a tooltip element
	 * is sought in this element's (grand/)parents.
	 */
	public final @Nullable TElement getTooltip()
	{
		//if a tooltip was already computed earlier, return that
		if(this.currentTooltip != null) return this.currentTooltip;
		//otherwise try to compute the tooltip using the tooltip supplier
		@Nullable var tts = this.tooltip.get();
		if(tts == null) {
			//if there's no supplier, there's no tooltip here. try looking in parents
			final @Nullable var parent = getParent();
			return (parent != null) ? parent.getTooltip() : null;
		}
		this.currentTooltip = Objects.requireNonNull(tts.apply(this), "Tooltip supplier returned null");
		this.currentTooltip.initCallback(); //initialize newly created tooltips
		return this.currentTooltip;
	}

	/**
	 * Invalidates the cached {@link #currentTooltip} value. This cached value is supplied
	 * by the {@link #tooltipProperty()}, and is held for optimization purposes.
	 * <p>
	 * Call this in the event a property change requires updating the tooltip.
	 */
	public final void invalidateTooltipCache() { this.currentTooltip = null; }
	// ==================================================
	/**
	 * Moves this {@link TElement} and all of its (grand/)children.
	 * @param dX The amount to move in the X axis.
	 * @param dY The amount to move in the Y axis.
	 */
	public final void move(int dX, int dY) {
		if(dX == 0 && dY == 0) return;
		final var bb = getBounds();
		setBounds(bb.x + dX, bb.y + dY, bb.width, bb.height);
		moveChildren(dX, dY);
	}

	/**
	 * Moves all of this {@link TElement}'s (grand/)children.
	 * @param dX The amount to move in the X axis.
	 * @param dY The amount to move in the Y axis.
	 */
	public final void moveChildren(int dX, int dY) {
		if(dX == 0 && dY == 0) return; //can happen, so, optimization
		for(var child : this) child.move(dX, dY);
	}

	/**
	 * Moves this {@link TElement} to the given position. This affects
	 * children as well, by moving them based on the difference between
	 * the old and new position.
	 * @param x New X position.
	 * @param y New Y position.
	 */
	public final void moveTo(int x, int y) { final var bb = getBounds(); move(x - bb.x, y - bb.y); }

	/**
	 * Sets the value of {@link #boundsProperty()}.
	 * This does not move children or affect their bounding boxes.
	 * @param x The new X position.
	 * @param y The new Y position.
	 * @param width The new width.
	 * @param height The new height.
	 */
	public final void setBounds(int x, int y, int width, int height) { setBounds(new Bounds2i(x, y, width, height)); }

	/**
	 * Sets the value of {@link #boundsProperty()}.
	 * This does not move children or affect their bounding boxes.
	 * @param bounds The new {@link Bounds2i}.
	 */
	public final void setBounds(@NotNull Bounds2i bounds) { this.bounds.set(bounds, TElement.class); }

	/**
	 * Sets the value of {@link #boundsProperty()}, using {@link UDim}s to calculate
	 * the new bounds relative to parent bounds.
	 * This does not move children or affect their bounding boxes.
	 * @param position New position.
	 * @param size New size.
	 * @throws NullPointerException If an argument is {@code null}.
	 * @throws IllegalStateException If {@link #getParent()} is {@code null}.
	 */
	public final void setBounds(@NotNull UDim2 position, @NotNull UDim2 size)
			throws NullPointerException, IllegalStateException
	{
		//not null requirements and parent requirement
		Objects.requireNonNull(position);
		Objects.requireNonNull(size);
		final var parent = getParent();
		if(parent == null) throw new IllegalStateException("");

		//calculate and assign
		final var pbb = parent.getBounds();
		this.bounds.set(new Bounds2i(
				pbb.x + position.x.computeI(pbb.width),
				pbb.y + position.y.computeI(pbb.height),
				size.x.computeI(pbb.width),
				size.y.computeI(pbb.height)
		), TElement.class);
	}
	// ==================================================
	/**
	 * Attempts to find a {@link TElement} at the given screen-space position.
	 * @param screenX Screen-space X position.
	 * @param screenY Screen-space Y position.
	 * @apiNote Usually used internally for {@link TScreen#hoveredElementProperty()} value calculation.
	 *          This method assumes {@code this} {@link TElement} does <b>not</b> clip descendants.
	 */
	public final @Nullable TElement findElementAt(int screenX, int screenY)
	{
		//keep track of the best candidate so far
		@Nullable TElement bestCandidate = null;
		for(var child : this)
		{
			//skip invisible elements
			if(!child.visible.get()) continue;

			@Nullable TElement candidate = null;
			//1. if the child's bounds contain the point go into it.
			if(child.getBounds().contains(screenX, screenY))
			{
				//first check if a descendant is a candidate.
				@Nullable TElement descendantCandidate = child.findElementAt(screenX, screenY);
				//the candidate then becomes either a descendant or this child if hoverable
				candidate = (descendantCandidate != null) ?
						descendantCandidate : (child.isHoverable() ? child : null);
			}
			//2. otherwise, if the child does not clip descendants, check them even if the child’s bounds don't contain the point.
			else if(!child.clipsDescendants.get())
				candidate = child.findElementAt(screenX, screenY);

			//if we found a candidate, that's the best one we got
			if(candidate != null) bestCandidate = candidate;
		}
		return bestCandidate;
	}
	// --------------------------------------------------
	/**
	 * Calls {@link Consumer#accept(Object)} for each child {@link TElement} whose
	 * {@link TElement#isVisible()} returns {@code true}.
	 * @param action The action to perform.
	 * @param recursive Whether to call the {@link Consumer} recursively for (grand/)children.
	 * @throws NullPointerException If the argument is {@code null}.
	 */
	public final void forEachVisible(@NotNull Consumer<TElement> action, boolean recursive)
			throws NullPointerException
	{
		Objects.requireNonNull(action);
		//optimization - no child is visible in this case anyway
		if(!isVisible()) return;

		//iterate children
		for(final var child : this)
		{
			//skip invisible children, which will cut off their entire branch too
			if(!child.visibleProperty().get()) continue;
			//apply the action
			action.accept(child);
			//handle recursion
			if(recursive) child.forEachVisible(action, true);
		}
	}
	// ==================================================
	/**
	 * Returns {@link CursorType} that represents the visual look the user's mouse cursor
	 * should have when this {@link TElement} is hovered.
	 */
	@Contract(pure = true)
	public @Virtual @NotNull CursorType getCursor() { return CursorType.DEFAULT; }
	// ==================================================
	/**
	 * (Re/)initializes this {@link TElement} and its children.
	 * <p>
	 * If this {@link TElement} instance's {@link Class} overrides {@link #initCallback()};
	 * {@link #clear()} is called and then {@link #initCallback()} is called.
	 * <p>
	 * Once initialized, recursively calls this method for each child, and then
	 * invokes {@link #eInitialized}.
	 *
	 * @apiNote Automatically called by an initializing {@link TScreen}.
	 */
	public final void clearAndInit()
	{
		//obtain the screen instance, so we can keep track of focused element
		final @Nullable TScreen screen = (this instanceof TScreen s) ? s : screenProperty().get();
		//if there's no screen present, we only reinitialize and do nothing else
		if(screen == null) { _clearAndInit(); return; }

		//however if a screen is present, we keep track of its focused element's position
		//in the scene graph, and then refocus after reinitialization if possible.
		//the reinitialized elements will obviously not be the same objects by reference,
		//hence why we track based on "position in the scene graph". it's heuristics.

		//obtain the path to the currently focused element
		final @Nullable var path_focus = SceneGraphPath.of(this, screen.focusedElementProperty().get());
		//reinitialize
		_clearAndInit();
		//and if reinitialization ended up clearing the focused element...
		if(path_focus != null) {
			//...attempt to restore focus
			@Nullable TElement finding = path_focus.resolve(this);
			screen.focusedElementProperty().set(finding, TElement.class);
			if(finding != null) TGuiUtils.scrollToElement(finding);
		}
	}

	/**
	 * Internal re-initialization logic that focuses solely on calling
	 * {@link #clear()} and {@link #initCallback()} and then recursing.
	 * <p>
	 * {@link #clearAndInit()} differs in the sense that it also keeps
	 * tack of the focused element and attempts to restore focus.<br>
	 * That focus logic of-course, has to happen only once instead of
	 * for each recursion, hence this internal method.
	 */
	private final @ApiStatus.Internal void _clearAndInit()
	{
		//only clear and (re/)initialize if this method overrides the init callback method
		if(overridesInitCallback()) { clear(); initCallback(); }
		//after that, initialize children
		for(var c : this) c._clearAndInit();
		//and only after everything is done, invoke the event
		this.eInitialized.invoker().accept(this);
	}

	/**
	 * Callback method that is invoked when this {@link TElement} is initializing.
	 * You may override this method to create and add child {@link TElement}s.
	 * @apiNote Automatically called by an initializing {@link TScreen}.
	 * @see #clearAndInit()
	 */
	protected @Reflected @Virtual void initCallback() {}
	// --------------------------------------------------
	/**
	 * A callback method that is invoked when the user makes an input.
	 * @param phase The input discovery phase.
	 * @param context Information about the user's input.
	 * @return {@code true} if this {@link TElement} handled the input.
	 * @throws NullPointerException If an argument is {@code null}.
	 */
	public @Virtual boolean inputCallback(
			@NotNull TInputContext.InputDiscoveryPhase phase,
			@NotNull TInputContext context) throws NullPointerException {
		return false;
	}
	// --------------------------------------------------
	/**
	 * Ticks this {@link TElement} and its (grand/)children.
	 * @apiNote This is {@link ApiStatus.Internal}! <b>DO NOT CALL THIS YOURSELF!</b>
	 * @see #tickCallback()
	 */
	@ApiStatus.Internal
	private final @Reflected void tick() {
		//tick this element
		tickCallback();
		//tick only children that are in-bounds
		final var bb = getBounds();
		for(final var child : this)
			if(child.getBounds().intersects(bb)) child.tick();
	}

	/**
	 * Callback method that is invoked automatically every GUI tick,
	 * if and after {@link #getParent()} ticks.
	 * <p>
	 * For performance optimization reasons, this is invoked if this
	 * element is contained within its parent's {@link #getBounds()}.
	 * <p>
	 * Note that this is regardles of what {@link #isVisible()} returns.
	 * The only requirements are the element being "within parent bounds"
	 * and the parent having ticked.
	 */
	protected @Virtual void tickCallback() {}
	// --------------------------------------------------
	/**
	 * A callback method that is invoked whenever this {@link TElement}
	 * becomes hovered.
	 * @see #isHovered()
	 */
	protected @Reflected @Virtual void hoverGainedCallback() {}

	/**
	 * A callback method that is invoked whenever this {@link TElement}
	 * stops being hovered.
	 * @see #isHovered()
	 */
	protected @Reflected @Virtual void hoverLostCallback() {}

	/**
	 * A callback method that is invoked whenever this {@link TElement}
	 * gains focus.
	 * @see #isFocused()
	 */
	protected @Reflected @Virtual void focusGainedCallback() {}

	/**
	 * A callback method that is invoked whenever this {@link TElement}
	 * loses focus.
	 * @see #isFocused()
	 */
	protected @Reflected @Virtual void focusLostCallback() {}

	/**
	 * A callback method that is invoked whenever this {@link TElement}
	 * starts being dragged.
	 * @see #isFocused()
	 */
	protected @Reflected @Virtual void dragStartCallback() {}

	/**
	 * A callback method that is invoked whenever this {@link TElement}
	 * stops being dragged.
	 * @see #isFocused()
	 */
	protected @Reflected @Virtual void dragEndCallback() {}
	// ================================================== ==================================================
	//                                         COLLECTION UTILITIES
	// ================================================== ==================================================
	/**
	 * Adds the child via {@link Node#add(Node)} and repositions it by this
	 * element's bounds using {@link #move(int, int)}.
	 * @param child The child to add and reposition relative to this element.
	 */
	public final void addRel(TElement child) { add(child); final var bb = getBounds(); child.move(bb.x, bb.y); }

	/**
	 * Removes the child via {@link Node#remove(Node)} and repositions it by the
	 * inverse of this element's bounds using {@link #move(int, int)}.
	 * @param child The child to remove and reposition relative to this element.
	 */
	public final void removeRel(TElement child) { remove(child); final var bb = getBounds(); child.move(-bb.x, -bb.y); }
	// ================================================== ==================================================
	//                                           PROPERTY ACCESSOR
	// ================================================== ==================================================
	/**
	 * An internal {@link Class} whose sole purpose is to take ownership of
	 * certain {@link ObjectProperty}s defined in {@link TElement}.
	 */
	static final @ApiStatus.Internal class TElementPropertyAccessor
	{
		private TElementPropertyAccessor() {}
		static void setScreenValue(TElement element, @Nullable TScreen value) { element.screen.set(value, TElementPropertyAccessor.class); }
	}
	// ================================================== ==================================================
	//                                           TElement UTILS
	// ================================================== ==================================================
	/** @see #overridesInitCallback(Class) */
	private static final @ApiStatus.Internal Object2BooleanMap<Class<?>> OVERRIDES_INIT = new Object2BooleanOpenHashMap<>();
	// ==================================================
	/**
	 * Returns {@code true} if this {@link TElement} subclass overrides the
	 * {@link #initCallback()} function at least once.
	 */
	private final boolean overridesInitCallback() { return overridesInitCallback(getClass()); }

	/**
	 * Returns {@code true} if a given {@link TElement} subclass overrides the
	 * {@link #initCallback()} function at least once. This information is used
	 * to determine if a {@link TElement}'s {@link #clearAndInit()} should be used.
	 */
	private static final boolean overridesInitCallback(final Class<? extends TElement> clazz) {
		//for optimization, results are cached in a map
		return OVERRIDES_INIT.computeIfAbsent(clazz, __ -> isMethodOverridden(clazz, "initCallback", void.class));
	}
	// ==================================================
	/**
	 * Shows the context menu for this {@link TElement}, if available in
	 * {@link #contextMenuProperty()}.
	 * @return The shown {@link TContextMenu}, or {@code null} if none was shown.
	 */
	public final @Nullable TContextMenu showContextMenu()
	{
		//obtain the screen instance and ensure it exists
		final var screen = screenProperty().get();
		if(screen == null) return null;
		//obtain the function that supplies a context menu instance
		final var supplier = this.contextMenu.get();
		if(supplier == null) return null;

		//this boolean has to be obtained before focus state changes:
		final var moveToCursor = isHovered() && !isFocused();
		//create the context menu and add it to the screen
		final @Nullable var menu = supplier.apply(this);
		if(menu == null) return null;
		screen.add(menu); //<-- this changes the focus state

		//position the context menu based on hover status
		if(moveToCursor) {
			//when hovered, the context menu targets cursor position
			final var client = getClient();
			assert client != null;
			final var window = client.getWindow();
			menu.moveTo(
					(int) client.mouseHandler.getScaledXPos(window),
					(int) client.mouseHandler.getScaledYPos(window));
		}
		else {
			//when not hovered or when focused, the context menu targets element position
			final var bb  = getBounds();
			final var mbb = menu.getBounds();
			final var sbb = screen.getBounds();
			menu.moveTo(bb.x, (bb.endY + mbb.height > sbb.endY) ? (bb.y - mbb.height) : bb.endY);
		}
		menu.snapToParent();

		//return the created context menu
		return menu;
	}
	// ================================================== ==================================================
}
