package com.thecsdev.commonmc.api.client.gui.panel.window;

import com.thecsdev.common.math.Bounds2i;
import com.thecsdev.common.properties.BooleanProperty;
import com.thecsdev.common.properties.IntegerProperty;
import com.thecsdev.common.properties.NotNullProperty;
import com.thecsdev.common.scene.Node;
import com.thecsdev.common.util.annotations.Virtual;
import com.thecsdev.commonmc.api.client.gui.TElement;
import com.thecsdev.commonmc.api.client.gui.label.TLabelElement;
import com.thecsdev.commonmc.api.client.gui.misc.TFillColorElement;
import com.thecsdev.commonmc.api.client.gui.render.TGuiGraphics;
import com.thecsdev.commonmc.api.client.gui.screen.TScreen;
import com.thecsdev.commonmc.api.client.gui.util.CursorType;
import com.thecsdev.commonmc.api.client.gui.util.TGuiUtils;
import com.thecsdev.commonmc.api.client.gui.util.TInputContext;
import com.thecsdev.commonmc.api.client.gui.widget.TButtonWidget;
import com.thecsdev.commonmc.resources.TComponent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * {@link TElement} implementation that has the visual appearance to that of a "window",
 * featuring a title-bar that has a title label and control buttons like [X].
 */
public abstract class TWindowElement extends TElement
{
	// ================================================== ==================================================
	//                                     TWindowElement IMPLEMENTATION
	// ================================================== ==================================================
	private final NotNullProperty<Component>      title           = new NotNullProperty<>(TComponent.missingNo().append(" -"));
	private final NotNullProperty<CloseOperation> closeOperation  = new NotNullProperty<>(CloseOperation.DO_NOTHING);
	private final BooleanProperty                 maximized       = new BooleanProperty(false);
	private final NotNullProperty<Bounds2i>       restoredBounds  = new NotNullProperty<>(new Bounds2i(0, 0, 400, 300));
	private final IntegerProperty                 backgroundColor = new IntegerProperty(0xFFFFFFFF);
	// ==================================================
	public TWindowElement()
	{
		//initialize properties
		focusableProperty().set(true, TWindowElement.class);

		//change listeners
		this.title.addChangeListener((p, o, n) -> {
			//update the title-bar's title label text
			findChild(c -> c instanceof TitlebarElement, false)
					.ifPresent(c -> ((TitlebarElement)c).lbl_title.setText(n));
		});
		this.maximized.addChangeListener((p, o, n) -> {
			//when maximizing, store current bounds as restored-bounds, so we can return to it later
			if(n) this.restoredBounds.set(getBounds(), TWindowElement.class);
			//set bounds based on maximized/restored state
			final @Nullable var parent = getParent();
			setBounds((n && parent != null) ? getParent().getBounds() : this.restoredBounds.get());
			//must reinitialize the gui to fit the new bounds
			clearAndInit();
			//TODO ^ Again, the issue of focused element clearing, thus hindering accessibility, reoccurs.
		});
	}
	// ==================================================
	/**
	 * {@link NotNullProperty} holding the title of this window, that is shown in the
	 * title-bar.
	 */
	public final NotNullProperty<Component> titleProperty() { return this.title; }

	/**
	 * {@link NotNullProperty} that controls what happens when {@link #close()} is called.
	 */
	public final NotNullProperty<CloseOperation> closeOperationProperty() { return this.closeOperation; }

	/**
	 * {@link BooleanProperty} that controls whether this window is maximized or not.
	 * <p>
	 * When setting this to {@code true}, it is highly advised to set the value of
	 * {@link #restoredBoundsProperty()} if you haven't already - specifically if
	 * this {@link TWindowElement} is maximized from the beginning of its lifespan
	 * (as in, you maximized it the moment you created it).
	 */
	public final BooleanProperty maximizedProperty() { return this.maximized; }

	/**
	 * {@link NotNullProperty} that holds the preferred bounds of this window, that
	 * are used when not maximized. Specifically, when restoring-down - the window will
	 * return to these bounds.
	 * <p>
	 * The value of this property is automatically updated whenever the value of
	 * {@link #maximizedProperty()} is set to {@code true}.
	 * <p>
	 * This value will be overriden when maximizing this {@link TWindowElement}.
	 * @see #maximizedProperty()
	 */
	public final NotNullProperty<Bounds2i> restoredBoundsProperty() { return this.restoredBounds; }

	/**
	 * {@link IntegerProperty} that holds the background color of this window.
	 */
	public final IntegerProperty backgroundColorProperty() { return this.backgroundColor; }
	// ==================================================
	protected final @Override void initCallback()
	{
		//obtain bounding box for child bounds calculation
		final var bb = getBounds();

		//initialize title-bar
		final var el_title = new TitlebarElement();
		el_title.setBounds(bb.height(15));
		add(el_title);

		//initialize body
		final var el_body = new TFillColorElement.Flat(0, 0x33888888);
		el_body.setBounds(bb.x, bb.y + 15, bb.width, bb.height - 15);
		add(el_body);
		initBodyCallback(el_body);
	}
	// --------------------------------------------------
	public @Virtual @Override void renderCallback(@NotNull TGuiGraphics pencil) {
		final var bb = getBounds();
		pencil.drawShadow(bb.x, bb.y, bb.width, bb.height, 0, 0, 5, 1, 0x10000000, false);
		pencil.fillColor(bb.x, bb.y, bb.width, bb.height, this.backgroundColor.getI());
	}
	public final @Override void postRenderCallback(@NotNull TGuiGraphics pencil) {
		final var bb = getBounds();
		pencil.drawOutlineOut(bb.x, bb.y, bb.width, bb.height, 0xFF000000);
	}
	// --------------------------------------------------
	private double dragDeltaX = 0, dragDeltaY = 0;
	@SuppressWarnings("DataFlowIssue")
	public final @Override boolean inputCallback(
			@NotNull TInputContext.InputDiscoveryPhase phase,
			@NotNull TInputContext context) throws NullPointerException
	{
		//only handle inputs on the main phase
		if(phase != TInputContext.InputDiscoveryPhase.MAIN)
			return false;

		//handle input based on type
		return switch(context.getInputType())
		{
			//LMB mouse press returns true, so drag input can be handled
			case MOUSE_PRESS -> context.getMouseButton() == 0;
			//mouse release stops the drag, so clear the drag values
			case MOUSE_RELEASE -> {
				//FIXME - Releasing another button causes this to yield false. This is an issue in the input system.
				if(context.getMouseButton() != 0) yield false;
				this.dragDeltaX = this.dragDeltaY = 0;
				TGuiUtils.keepElementWithinBounds(this, getParent().getBounds());
				yield true;
			}
			//handle mouse drag here
			case MOUSE_DRAG ->
			{
				//keep track of mouse delta (in decimal)
				this.dragDeltaX += context.getMouseDeltaX();
				this.dragDeltaY += context.getMouseDeltaY();

				//convert decimal to whole, and see if there's movement to be handled
				final int dX = (int) this.dragDeltaX, dY = (int) this.dragDeltaY;
				if(dX == 0 && dY == 0) yield true;

				//there's some movement to be made, move this window and yield
				this.dragDeltaX -= dX; this.dragDeltaY -= dY;
				move(dX, dY);
				yield true;
			}
			//all other input types are ignored
			default -> false;
		};
	}
	// ==================================================
	/**
	 * Similar to {@link #initCallback()}, except elements are intialized onto the
	 * provided "body" {@link TElement}.
	 * @param body The parent element where the GUI is to be initialized.
	 */
	protected abstract void initBodyCallback(@NotNull TElement body);

	/**
	 * Invokes the {@link CloseOperation} that is assigned to this {@link TWindowElement}.
	 * @see #closeOperationProperty()
	 */
	public final void close()
	{
		switch(this.closeOperation.get())
		{
			case DISPOSE: { remove(); break; }
			case HIDE: { visibleProperty().set(false, TWindowElement.class); break; }
			case CLOSE_SCREEN: { screenProperty().getOptional().ifPresent(TScreen::close); break; }
			case DO_NOTHING:
			default: break;
		}
	}
	// ================================================== ==================================================
	//                                     CloseOperation IMPLEMENTATION
	// ================================================== ==================================================
	/**
	 * Constants used to define what happens when {@link #close()} is called.
	 * @apiNote Inspired by {@link javax.swing.WindowConstants}.
	 */
	public enum CloseOperation
	{
		/**
		 * Nothing happens. The [X] button has no functionality.
		 */
		DO_NOTHING,

		/**
		 * {@link TWindowElement}'s visibility is set to {@code false}. It is still
		 * present, but no longer visible.
		 * @see TElement#visibleProperty()
		 */
		HIDE,

		/**
		 * {@link TWindowElement} is removed from its parent.
		 * @see Node#remove()
		 */
		DISPOSE,

		/**
		 * The current screen instance the {@link TWindowElement} belongs to is closed.
		 * @see TScreen#close()
		 */
		CLOSE_SCREEN;
	}
	// ================================================== ==================================================
	//                                    TitlebarElement IMPLEMENTATION
	// ================================================== ==================================================
	/**
	 * A private inner {@link Class} that represents the title-bar of the window,
	 * featuring a "title" label and control buttons such as [X].
	 */
	private final class TitlebarElement extends TElement
	{
		// ==================================================
		final TLabelElement           lbl_title    = new TLabelElement();
		final TButtonWidget.Paintable btn_close    = new TButtonWidget.Paintable(0xFFFF0000, 0, 0xFFFFFFFF);
		final TButtonWidget.Paintable btn_maximize = new TButtonWidget.Paintable(0x66888888, 0, 0xFFFFFFFF);
		// ==================================================
		TitlebarElement()
		{
			this.lbl_title.textScaleProperty().set(0.8, TitlebarElement.class);
			this.btn_close.getLabel().setText(Component.literal("X"));
			this.btn_close.getLabel().textScaleProperty().set(0.8, TitlebarElement.class);
			this.btn_maximize.getLabel().setText(Component.literal("■"));
			this.btn_maximize.getLabel().textScaleProperty().set(0.8, TitlebarElement.class);
		}
		// ==================================================
		public @Virtual @Override @NotNull CursorType getCursor() { return CursorType.RESIZE_ALL; }
		// ==================================================
		protected final @Override void initCallback()
		{
			//obtain bounding box for child bounds calculation
			final var bb = getBounds();

			//initialize title label
			this.lbl_title.setBounds(bb.add(4, 0, -8, 0));
			this.lbl_title.setText(TWindowElement.this.title.get());
			add(this.lbl_title);

			//initalize maximize button
			this.btn_maximize.setBounds(bb.width - 40, 0, 20, 15);
			this.btn_maximize.eClicked.register(__ -> TWindowElement.this.maximized.toggle());
			addRel(btn_maximize);

			//initialize close button
			this.btn_close.setBounds(bb.width - 20, 0, 20, 15);
			this.btn_close.eClicked.register(__ -> TWindowElement.this.close());
			addRel(this.btn_close);
		}
		// ==================================================
	}
	// ================================================== ==================================================
}
