package com.thecsdev.commonmc.api.client.gui.screen.hud;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.thecsdev.common.math.Point2d;
import com.thecsdev.common.properties.IntegerProperty;
import com.thecsdev.common.properties.NotNullProperty;
import com.thecsdev.common.util.annotations.Virtual;
import com.thecsdev.commonmc.api.client.gui.TElement;
import com.thecsdev.commonmc.api.client.gui.ctxmenu.TContextMenu;
import com.thecsdev.commonmc.api.client.gui.render.TGuiGraphics;
import com.thecsdev.commonmc.api.client.gui.util.TGuiUtils;
import com.thecsdev.commonmc.api.client.gui.util.TInputContext;
import com.thecsdev.commonmc.api.client.registry.TClientRegistries;
import com.thecsdev.commonmc.resource.TLanguage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.thecsdev.commonmc.api.client.gui.panel.TPanelElement.COLOR_OUTLINE_FOCUSED;
import static org.lwjgl.sdl.SDLMouse.SDL_BUTTON_LEFT;

/**
 * A {@link TElement} that may be pinned onto a {@link THudCanvasScreen}.
 * <p>
 * Unlike an infinite "mind map" canvas, pinned elements live on a <b>finite</b>
 * "whiteboard" that matches the game's screen dimensions. For that reason, an
 * element's position is not described using absolute coordinates, but using
 * <b>anchor points</b> ranging from {@code 0} to {@code 1} on both axes.
 * <pre>{@code
 * (0, 0) ------------- (0.5, 0) ------------- (1, 0)
 *    |                    |                    |
 *    |                  CENTER                 |
 *    |                    |                    |
 * (0, 1) ------------- (0.5, 1) ------------- (1, 1)
 * }</pre>
 * Anchor points are what allow pinned elements to stay in roughly the same
 * on-screen spot when the game's screen is resized, or when the GUI scale changes.
 * <p>
 * Elements can be dragged around and configured only while their
 * {@link THudCanvasScreen} is <b>explicitly open</b>. When merely rendered on
 * the in-game HUD, they act as visual-only displays.
 *
 * @apiNote Implementations are expected to be registered via a
 *          corresponding {@link THudElementType}.
 * @see THudElementType
 * @see THudCanvasScreen
 */
@ApiStatus.Experimental
@Environment(EnvType.CLIENT)
public abstract @Virtual class THudElement extends TElement
{
	// ==================================================
	/**
	 * The default anchor point value, representing the center of the screen.
	 */
	public static final Point2d ANCHOR_CENTER = new Point2d(0.5, 0.5);

	/**
	 * The default grid size, in GUI units, that dragged elements snap to.
	 */
	public static final int GRID_SIZE_DEFAULT = 4;
	// --------------------------------------------------
	/**
	 * {@link Codec} for an {@link THudElement}'s anchor point.
	 * @see #anchorProperty()
	 */
	private static final Codec<Point2d> ANCHOR_CODEC = Codec.DOUBLE.listOf().flatXmap(
			list -> (list.size() == 2) ?
					DataResult.success(new Point2d(list.get(0), list.get(1))) :
					DataResult.error(() -> "Anchor must consist of exactly two numbers"),
			anchor -> DataResult.success(List.of(anchor.x, anchor.y)));

	/**
	 * {@link Codec} implementation for {@link THudElement}s.
	 * <p>
	 * This is a "dispatch" {@link Codec} that stores the {@link Identifier} of the
	 * {@link THudElementType} the element belongs to. That {@link Identifier} is
	 * then used to look up the correct codec for the element's own data.
	 * <p>
	 * Data points that cannot be parsed yield {@link DataResult#error(() -> ...)},
	 * which callers are expected to simply skip over.
	 */
	public static final Codec<THudElement> CODEC = new Codec<>()
	{
		public final @Override <T> DataResult<Pair<THudElement, T>> decode(DynamicOps<T> ops, T input)
		{
			return ops.getMap(input).flatMap(map ->
			{
				//the element's type identifier must always be present
				final @Nullable T typeValue = map.get("type");
				if(typeValue == null) return DataResult.error(() -> "Missing 'type' value");
				//as must the element's own data
				final @Nullable T dataValue = map.get("data");
				if(dataValue == null) return DataResult.error(() -> "Missing 'data' value");

				//decode the type identifier, and locate the corresponding type
				return Identifier.CODEC.decode(ops, typeValue).flatMap(typePair -> {
					final var             typeId = typePair.getFirst();
					final @Nullable var  type   = TClientRegistries.HUD_ELEMENT_TYPE.getValue(typeId);
					if(type == null) return DataResult.error(() -> "Unrecognized HUD element type - " + typeId);

					//decode the element's own data, and assign the located type to it
					return type.getCodec().codec().decode(ops, dataValue).flatMap(dataPair -> {
						final var element = dataPair.getFirst();
						element._assignType(type);
						return DataResult.success(Pair.of((THudElement) element, ops.empty()));
					});
				});
			});
		}

		public final @Override <T> DataResult<T> encode(THudElement input, DynamicOps<T> ops, T prefix)
		{
			//the element must have a type, and that type must be registered
			final @Nullable var type = input.getType();
			if(type == null) return DataResult.error(() -> "HUD element has no 'type' assigned - " + input);
			final @Nullable var typeId = type.getKey();
			if(typeId == null) return DataResult.error(() -> "HUD element 'type' is not registered - " + type);

			//obtain the codec for the element's own data (cast is safe - element IS of that type)
			@SuppressWarnings("unchecked")
			final Codec<THudElement> elementCodec = (Codec<THudElement>) (Codec<?>) type.getCodec().codec();

			//encode the type identifier
			return Identifier.CODEC.encodeStart(ops, typeId).flatMap(typeValue ->
					//encode the element's own data
					elementCodec.encodeStart(ops, input).flatMap(dataValue ->
							//encode the anchor point
							ANCHOR_CODEC.encodeStart(ops, input.anchor.get()).flatMap(anchorValue ->
									//and finally, combine everything into a single map
									ops.mapBuilder()
											.add("type", typeValue)
											.add("data", dataValue)
											.add("anchor", anchorValue)
											.build(prefix))));
		}
	};
	// ==================================================
	private       @Nullable THudElementType<?>       type;      //assigned by the corresponding type
	private final @NotNull  NotNullProperty<Point2d> anchor     = new NotNullProperty<>(ANCHOR_CENTER);
	private final @NotNull  IntegerProperty          gridSize  = new IntegerProperty(GRID_SIZE_DEFAULT);
	// --------------------------------------------------
	private @Nullable TElement handle; //the drag-handle. null = "not initialized"
	// ==================================================
	protected THudElement()
	{
		//anchor points must always remain in the 0 to 1 range
		this.anchor.addFilter(Point2d::clamp01, THudElement.class);

		//keep the drag-handle's size in sync with this element's size
		boundsProperty().addChangeListener((_, _, _) -> {
			if(this.handle == null) return;
			final var bb = getBounds();
			//only reposition when the size changed - moving already handles the position
			if(!this.handle.getBounds().hasSameSize(bb)) this.handle.setBounds(bb.x, bb.y, bb.width, bb.height);
		});

		//the default context menu delegates to the overridable builder method
		contextMenuProperty().set(el ->
		{
			//a client instance is required for building a context menu
			final @Nullable var client = el.getClient();
			if(client == null) return null;
			//build the context menu using the subclass-provided entries
			final var builder = new TContextMenu.Builder(client);
			buildContextMenu(builder);
			return builder.build();
		}, THudElement.class);
	}
	// ==================================================
	/**
	 * Returns the {@link THudElementType} of this {@link THudElement}, or {@code null}
	 * if this element does not belong to any type.
	 * @apiNote Elements constructed via {@link THudElementType#instantiate()} or
	 *          deserialized from data are always guaranteed to have a type.
	 */
	public final @Nullable THudElementType<?> getType() { return this.type; }

	/**
	 * The {@link NotNullProperty} holding this {@link THudElement}'s anchor point.
	 * @see #getAnchor()
	 */
	public final NotNullProperty<Point2d> anchorProperty() { return this.anchor; }

	/**
	 * Returns this {@link THudElement}'s current anchor point.
	 * @see #setAnchor(double, double)
	 */
	public final @NotNull Point2d getAnchor() { return this.anchor.get(); }

	/**
	 * Sets this {@link THudElement}'s anchor point, and updates its on-screen
	 * position to match the newly assigned anchor point.
	 * @param x The new X anchor point, ranging from {@code 0} to {@code 1}.
	 * @param y The new Y anchor point, ranging from {@code 0} to {@code 1}.
	 * @see #anchorProperty()
	 */
	public final void setAnchor(double x, double y) {
		this.anchor.set(new Point2d(x, y), THudElement.class);
		updatePositionFromAnchor();
	}

	/**
	 * The {@link IntegerProperty} holding the size of the grid, in GUI units, that
	 * this {@link THudElement} snaps to when dragged around and then released.
	 * <p>
	 * A value of {@code 0} or {@code 1} disables grid-snapping altogether.
	 */
	public final IntegerProperty gridSizeProperty() { return this.gridSize; }
	// ==================================================
	/**
	 * Recalculates this {@link THudElement}'s on-screen position so that it matches
	 * the current value of {@link #anchorProperty()}.
	 * <p>
	 * This is invoked automatically by the parent {@link THudCanvasScreen} whenever
	 * the game's screen is (re-)initialized or resized.
	 */
	public final void updatePositionFromAnchor()
	{
		//both this element and its parent are required for the maths
		final @Nullable var parent = getParent();
		if(parent == null) return;
		final var pbb = parent.getBounds();
		final var bb  = getBounds();

		//calculate the anchor point's position on both the parent and this element
		final var anchor = this.anchor.get();
		final int aX = (int) Math.round((pbb.width  * anchor.x) - (bb.width  * anchor.x));
		final int aY = (int) Math.round((pbb.height * anchor.y) - (bb.height * anchor.y));

		//reposition this element (which also moves its children along with it)
		moveTo(pbb.x + aX, pbb.y + aY);
	}

	/**
	 * Recalculates the value of {@link #anchorProperty()} so that it matches this
	 * {@link THudElement}'s current on-screen position.
	 */
	public final void updateAnchorFromPosition()
	{
		//both this element and its parent are required for the maths
		final @Nullable var parent = getParent();
		if(parent == null) return;
		final var pbb = parent.getBounds();
		final var bb  = getBounds();

		//calculate how much room there is to move around in on each axis
		final int dW = pbb.width  - bb.width;
		final int dH = pbb.height - bb.height;

		//and use that to figure out the new anchor point values
		final double aX = (dW > 0) ? Math.clamp((double) (bb.x - pbb.x) / dW, 0d, 1d) : 0d;
		final double aY = (dH > 0) ? Math.clamp((double) (bb.y - pbb.y) / dH, 0d, 1d) : 0d;
		this.anchor.set(new Point2d(aX, aY), THudElement.class);
	}
	// --------------------------------------------------
	/**
	 * Removes this {@link THudElement} from the {@link THudCanvasScreen} it
	 * is currently pinned onto, if any.
	 * @return {@code true} if this element was pinned onto a {@link THudCanvasScreen}.
	 */
	public final boolean unpin()
	{
		final @Nullable var screen = screenProperty().get();
		if(screen instanceof THudCanvasScreen canvas) { canvas.removeElement(this); return true; }
		else { remove(); return false; }
	}
	// --------------------------------------------------
	/**
	 * Returns {@code true} if this {@link THudElement} can be configured by the
	 * user while its {@link THudCanvasScreen} is open.
	 * @apiNote The return value of this method should be constant.
	 */
	@Contract(pure = true)
	public @Virtual boolean isConfigurable() { return false; }

	/**
	 * Invoked when the user requests to configure this {@link THudElement}.
	 * @apiNote Only ever invoked if {@link #isConfigurable()} returns {@code true}.
	 */
	public @Virtual void configure() {}
	// --------------------------------------------------
	/**
	 * Builds the context menu entries for this {@link THudElement}.
	 * <p>
	 * The default implementation offers configuration (when
	 * {@link #isConfigurable()}) and removal. Subclasses should override this
	 * method to add their own entries, and are expected to call {@code super}.
	 * @param builder The builder to add the entries to.
	 */
	protected @Virtual void buildContextMenu(@NotNull TContextMenu.Builder builder) {
		if(isConfigurable()) {
			builder.addButton(TLanguage.gui_hud_configure(), _ -> configure());
			builder.addSeparator();
		}
		builder.addButton(TLanguage.gui_hud_unpin(), _ -> unpin());
	}
	// ==================================================
	/**
	 * Assigns a {@link THudElementType} to this {@link THudElement}.
	 * @apiNote Only ever invoked by {@link THudElementType#instantiate()} and by
	 *          {@link THudElement#CODEC} during deserialization.
	 */
	@ApiStatus.Internal
	final void _assignType(@Nullable THudElementType<?> type) { this.type = type; }

	/**
	 * Moves this {@link THudElement} by the given amount, while updating its
	 * {@link #anchorProperty()} to match the new position.
	 */
	@ApiStatus.Internal
	final void _dragMove(int dX, int dY)
	{
		//move this element, and keep it within its parent's bounds
		move(dX, dY);
		final @Nullable var parent = getParent();
		if(parent != null) TGuiUtils.keepElementWithinBounds(this, parent.getBounds());
		//finally, update the anchor point
		updateAnchorFromPosition();
	}

	/**
	 * Snaps this {@link THudElement} to the {@link #gridSizeProperty()} grid, keeps
	 * it within its parent's bounds, and updates its {@link #anchorProperty()}.
	 */
	@ApiStatus.Internal
	final void _dragEnd()
	{
		//snap this element's position to the grid
		final int grid = this.gridSize.getI();
		if(grid > 1) {
			final var bb = getBounds();
			final int x  = Math.round((float) bb.x / grid) * grid;
			final int y  = Math.round((float) bb.y / grid) * grid;
			moveTo(x, y);
		}
		//keep this element within its parent's bounds
		final @Nullable var parent = getParent();
		if(parent != null) TGuiUtils.keepElementWithinBounds(this, parent.getBounds());
		//and finally, update the anchor point
		updateAnchorFromPosition();
	}
	// ==================================================
	protected final @Override void initCallback()
	{
		//initialize the element's own contents first, so that the drag-handle
		//can be added on top of them as the very last child
		initContentCallback();

		//initialize the drag-handle, and keep track of it
		final var bb = getBounds();
		final var handle = new DragHandleElement();
		handle.setBounds(0, 0, bb.width, bb.height);
		addRel(handle);
		this.handle = handle;
	}

	/**
	 * Callback method that is invoked when this {@link THudElement} is initializing.
	 * Override this method to create and add child {@link TElement}s.
	 * @apiNote Do <b>NOT</b> override {@link #initCallback()} directly, as that
	 *          would break this element's drag-handle initialization.
	 */
	protected @Virtual void initContentCallback() {}
	// --------------------------------------------------
	/**
	 * {@link TElement} whose sole purpose is capturing mouse input, so that
	 * {@link THudElement}s can be dragged around on their {@link THudCanvasScreen}.
	 */
	private final @ApiStatus.Internal class DragHandleElement extends TElement
	{
		// ==================================================
		private double deltaX = 0, deltaY = 0;
		// ==================================================
		public DragHandleElement() {
			focusableProperty().set(true, DragHandleElement.class);
			hoverableProperty().set(true, DragHandleElement.class);
			//forward the parent element's context menu
			final @NotNull var self = THudElement.this;
			contextMenuProperty().set(_ -> {
				final @Nullable var supplier = self.contextMenuProperty().get();
				return (supplier != null) ? supplier.apply(self) : null;
			}, DragHandleElement.class);
		}
		// ==================================================
		@SuppressWarnings("DataFlowIssue")
		public @Virtual @Override boolean inputCallback(
				@NotNull TInputContext.InputDiscoveryPhase phase,
				@NotNull TInputContext context)
		{
			//only handle inputs on the main discovery phase
			if(phase != TInputContext.InputDiscoveryPhase.MAIN) return false;

			//handle based on input type
			switch(context.getInputType())
			{
				//left-clicking accepts the input, so that dragging can take place.
				//right-clicking must be ignored here, so that the input falls
				//through to the context menu logic in the screen wrapper.
				case MOUSE_PRESS: {
					return context.getMouseButton() == SDL_BUTTON_LEFT;
				}
				//mouse dragging moves the parent element around
				case MOUSE_DRAG: {
					//keep track of the mouse delta (in decimal)
					this.deltaX += context.getMouseDeltaX();
					this.deltaY += context.getMouseDeltaY();

					//convert decimal to whole, and see if there's movement to be handled
					final int dX = (int) this.deltaX, dY = (int) this.deltaY;
					if(dX == 0 && dY == 0) return false;

					//there's some movement to be made, so move the element
					this.deltaX -= dX; this.deltaY -= dY;
					_dragMove(dX, dY);
					return true;
				}
				//all other input types are ignored
				default: return false;
			}
		}
		// --------------------------------------------------
		protected @Virtual @Override void dragEndCallback() { _dragEnd(); }
		// --------------------------------------------------
		public @Virtual @Override void postRenderCallback(@NotNull TGuiGraphics pencil) {
			//draw an outline while editing, so it is clear that the element is draggable
			if(!isHoveredOrFocused()) return;
			final var bb = getBounds();
			pencil.drawOutlineOut(bb.x, bb.y, bb.width, bb.height, COLOR_OUTLINE_FOCUSED);
		}
		// ==================================================
	}
	// ==================================================
}
