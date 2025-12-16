package com.thecsdev.commonmc.api.client.gui.ctxmenu;

import com.thecsdev.common.math.Bounds2i;
import com.thecsdev.common.properties.NotNullProperty;
import com.thecsdev.common.properties.ObjectProperty;
import com.thecsdev.common.util.annotations.Virtual;
import com.thecsdev.common.util.enumerations.CompassDirection;
import com.thecsdev.commonmc.api.client.gui.TElement;
import com.thecsdev.commonmc.api.client.gui.label.TLabelElement;
import com.thecsdev.commonmc.api.client.gui.misc.THoverScrollElement;
import com.thecsdev.commonmc.api.client.gui.panel.TPanelElement;
import com.thecsdev.commonmc.api.client.gui.render.TGuiGraphics;
import com.thecsdev.commonmc.api.client.gui.screen.TScreen;
import com.thecsdev.commonmc.api.client.gui.util.TGuiUtils;
import com.thecsdev.commonmc.api.client.gui.util.TInputContext;
import com.thecsdev.commonmc.api.client.gui.widget.TButtonWidget;
import com.thecsdev.commonmc.api.client.gui.widget.TClickableWidget;
import com.thecsdev.commonmc.resources.TCDCSprites;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Consumer;

import static com.thecsdev.commonmc.api.client.gui.ctxmenu.TContextMenu.PropertyAccessor.setRootCtxMenuValue;
import static com.thecsdev.commonmc.api.client.gui.util.TGuiUtils.calcMaxWidth;
import static com.thecsdev.commonmc.api.client.gui.util.TGuiUtils.isAncestor;
import static com.thecsdev.commonmc.api.client.gui.util.TInputContext.InputDiscoveryPhase.MAIN;
import static com.thecsdev.commonmc.api.client.gui.util.TInputContext.InputDiscoveryPhase.PREEMPT;
import static com.thecsdev.commonmc.api.client.gui.util.TInputContext.InputType.MOUSE_PRESS;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;

/**
 * A {@link TContextMenu} is a specialized {@link TElement} that represents a context menu
 * GUI. It is designed to handle focus, input events, and screen management to ensure proper
 * behavior when interacting with other GUI elements.
 * <p>
 * This element's parent <b>must</b> be either a {@link TScreen} or another {@link TContextMenu}.
 * Attempting to add this element to any other type of parent <b>will {@code throw}</b>!
 */
public @Virtual class TContextMenu extends TElement
{
	// ================================================== ==================================================
	//                                       TContextMenu IMPLEMENTATION
	// ================================================== ==================================================
	private final NotNullProperty<TContextMenu> rootContextMenu = new NotNullProperty<>(this);
	// ==================================================
	public TContextMenu()
	{
		//this element is focusable
		hoverableProperty().set(true, TContextMenu.class);
		focusableProperty().set(true, TContextMenu.class);
		//in order to have child context menus be visible, we need not clip descendants
		clipsDescendantsProperty().set(false, TContextMenu.class);
		clipsDescendantsProperty().addFilter(__ -> false, TContextMenu.class);

		//handle being assigned to a new screen
		screenProperty().addChangeListener((p, o, n) -> {
			//ignore removals from screens (aka screen becoming null)
			if(n == null) return;
			//remove other dropdown elements "branches". there cannot be more than one
			n.forEach(el -> {
				//1. element must be a context menu
				//2. element must not be 'this'
				//3. element must not be a (grand/)child or a (grand/)parent
				if(el instanceof TContextMenu && el != this && !isAncestor(el, this) && !isAncestor(this, el))
					el.remove(); //remove elements matching such criterions
			}, true);
			//focus onto this element once added to a screen
			n.focusedElementProperty().set(this, TContextMenu.class);
		});

		//ensure the parent is always a screen or another context menu
		parentProperty().addChangeListener((p, o, n) -> {
			if(n == null) return;
			if(!(n instanceof TScreen) && !(n instanceof TContextMenu)) {
				n.remove(this); //restore to stable state - caution: re-invokes this change listener
				throw new IllegalStateException("Context menu assigned to an illegal parent - " + n);
			}
		});

		//tracking the root context menu element (this has to be placed last)
		this.rootContextMenu.setReadOnly(true, TContextMenu.class);
		this.rootContextMenu.setOwner(PropertyAccessor.class, TContextMenu.class);
		this.rootContextMenu.addChangeListener((p, o, n) -> {
			//propagate the new root context menu to all child context menus
			for(final var child : this)
				if(child instanceof TContextMenu childMenu)
					setRootCtxMenuValue(childMenu, n);
		});
		parentProperty().addChangeListener((p, o, n) -> {
			//find the root context menu and set it
			@NotNull  TContextMenu root = this;
			@Nullable TElement     next = this;
			while(next != null) { if((next = next.getParent()) instanceof TContextMenu pcm) root = pcm; }
			setRootCtxMenuValue(this, root);
		});
	}
	// ==================================================
	/**
	 * The top-most {@link TContextMenu} in the hierarchy of this {@link TElement}.
	 * If this element has no parent context menu, this property will reference
	 * this element itself.
	 * @apiNote Owned by {@link PropertyAccessor}.
	 */
	public final NotNullProperty<TContextMenu> rootContextMenuProperty() { return this.rootContextMenu; }
	// ==================================================
	@SuppressWarnings("DataFlowIssue")
	public final @Override boolean inputCallback(TInputContext.@NotNull InputDiscoveryPhase phase, @NotNull TInputContext context)
	{
		//handle preemptive input based on its type
		if(phase == PREEMPT)
			switch(context.getInputType())
			{
				//nillify out of bounds mouse scrolls
				//(only the "main/root/top" context menu element can this)
				case MOUSE_SCROLL: return !isHoverAncestor() && !(getParent() instanceof TContextMenu);
				//close this dropdown if mouse was pressed out of bounds,
				case MOUSE_PRESS: {
					//do not handle this if it is hover ancestor
					if(isHoverAncestor()) break;
					//remove this element if not hover ancestor
					final var result = !rootContextMenuProperty().get().isHoverAncestor();
					remove(); //removal comes AFTER result calculation
					//only return true if press took place outside a context menu
					return result;
				}
				//close this dropdown if a key press happens without focus
				//to this dropdown or one of its (grand/)children
				case KEY_PRESS: {
					if(!isFocusAncestor() || context.getKeyCode() == GLFW_KEY_ESCAPE)
						return remove();
					else break;
				}
			}

		//accept main input clicks
		else if(phase == MAIN && context.getInputType() == MOUSE_PRESS)
			return true;

		//return false if nothing happened
		return false;
	}
	// --------------------------------------------------
	/**
	 * Renders a shadow effect for this context menu.
	 * @param pencil The {@link TGuiGraphics} instance used for rendering.
	 */
	protected @Virtual void renderShadow(@NotNull TGuiGraphics pencil) {
		final var bb = getBounds();
		pencil.drawShadow(bb.x, bb.y, bb.width, bb.height, 0, 0, 5, 1, 0x10000000, false);
	}

	public @Virtual @Override void renderCallback(@NotNull TGuiGraphics pencil) {
		renderShadow(pencil);
		final var bb = getBounds();
		pencil.fillColor(bb.x, bb.y, bb.width, bb.height, 0xFF1f1f1f);
		pencil.drawOutlineOut(bb.x, bb.y, bb.width, bb.height, isFocused() ? 0xFF7e7e7e : 0xFF5e5e5e);
	}
	// ==================================================
	/**
	 * Returns {@code true} if {@link #isFocused()} or if one this {@link TElement}'s
	 * children or grandchildren {@link #isFocused()}.
	 */
	public final boolean isFocusAncestor()
	{
		final @Nullable var screen = screenProperty().get();
		if(screen == null) return false;
		final @Nullable var focus = screen.focusedElementProperty().get();
		return focus == this || isAncestor(focus, this);
	}

	/**
	 * Returns {@code true} if {@link #isHovered()} or if one this {@link TElement}'s
	 * children or grandchildren {@link #isHovered()}.
	 */
	public final boolean isHoverAncestor()
	{
		final @Nullable var screen = screenProperty().get();
		if(screen == null) return false;
		final @Nullable var hover = screen.hoveredElementProperty().get();
		return hover == this || isAncestor(hover, this);
	}
	// --------------------------------------------------
	/**
	 * Snaps this context menu to be fully visible within the bounds of its parent
	 * context menu or viewport.
	 * @see TGuiUtils#keepElementWithinBounds(TElement, Bounds2i)
	 */
	public final void snapToParent()
	{
		//obtain 'viewport' and parent context menu
		final @Nullable var viewport = rootContextMenuProperty().get().getParent();
		if(viewport == null) return; //viewport element is required
		final @Nullable var parentcm = (getParent() instanceof TContextMenu pcm) ? pcm : null;

		//calculate the detla x and delta y
		final var bb  = getBounds();
		final var vbb = viewport.getBounds();
		int dX = 0, dY = 0;
		if(bb.endY > vbb.endY) dY = vbb.endY - bb.endY;
		if(bb.y + dY < vbb.y)  dY += vbb.y - (bb.y + dY);
		if(bb.endX > vbb.endX) dX = (parentcm != null) ? parentcm.getBounds().x - bb.endX : vbb.endX - bb.endX;
		if(bb.x + dX < vbb.x)  dX += vbb.x - (bb.x + dX);

		//finally, move
		move(dX, dY);
	}
	// ================================================== ==================================================
	//                                           PROPERTY ACCESSOR
	// ================================================== ==================================================
	/**
	 * An internal {@link Class} whose sole purpose is to take ownership of certain
	 * {@link ObjectProperty}s defined in {@link TContextMenu}, so that they
	 * cannot be modified from outside this class.
	 */
	static final @ApiStatus.Internal class PropertyAccessor
	{
		private PropertyAccessor() {}
		static void setRootCtxMenuValue(TContextMenu self, @Nullable TContextMenu root) { self.rootContextMenu.set(root, PropertyAccessor.class); }
	}
	// ================================================== ==================================================
	//                                            Builder IMPLEMENTATION
	// ================================================== ==================================================
	/**
	 * A builder for creating a simple {@link TContextMenu} instance.
	 */
	public static final class Builder
	{
		// ==================================================
		private final Minecraft           client;
		private final ArrayList<TElement> entries = new ArrayList<>();
		// --------------------------------------------------
		private int maxW, maxH;
		// ==================================================
		public Builder(@NotNull Minecraft client) {
			this.client    = Objects.requireNonNull(client);
			final var wnd  = client.getWindow();
			this.maxW      = max(wnd.getGuiScaledWidth() / 3, 300);
			this.maxH      = max(wnd.getGuiScaledHeight() - 4, 20);
		}
		// ==================================================
		/**
		 * Sets the maximum width of the context menu.
		 * @param maxWidth The maximum width in game's GUI units.
		 */
		public final Builder setMaxWidth(int maxWidth) { this.maxW = max(maxWidth, 300); return this; }

		/**
		 * Sets the maximum height of the context menu.
		 * @param maxHeight The maximum height in game's GUI units.
		 */
		public final Builder setMaxHeight(int maxHeight) { this.maxH = max(maxHeight, 20); return this; }

		/**
		 * Sets the maximum size of the context menu.
		 * @param maxWidth The maximum width in game's GUI units.
		 * @param maxHeight The maximum height in game's GUI units.
		 */
		public final Builder setMaxSize(int maxWidth, int maxHeight) {
			return setMaxWidth(maxWidth).setMaxHeight(maxHeight);
		}
		// ==================================================
		/**
		 * Adds a custom {@link TElement} to the context menu.
		 * @param element The {@link TElement} to add.
		 * @throws NullPointerException If the argument is {@code null}.
		 * @apiNote This element will be stretched horizontally to fit the context menu's width.
		 */
		public final Builder addElement(@NotNull TElement element) throws NullPointerException {
			this.entries.add(Objects.requireNonNull(element));
			return this;
		}
		// --------------------------------------------------
		/**
		 * Adds a {@link TButtonWidget} with the specified text to the context menu.
		 * @param text The text to display on the button.
		 * @param onClick The callback to invoke when the button is clicked.
		 * @throws NullPointerException If the argument is {@code null}.
		 */
		public final Builder addButton(
				@NotNull Component text,
				@NotNull Consumer<TClickableWidget> onClick) throws NullPointerException
		{
			final var btn = new Button();
			btn.getLabel().setText(text);
			btn.getLabel().textAlignmentProperty().set(CompassDirection.WEST, Builder.class);
			btn.setBounds(0, 0, this.client.font.width(text) + (TButtonWidget.LBL_PAD_X * 2), 15);
			btn.eClicked.register(onClick); //on-click goes first, as is may rely on btn#getClient()
			btn.eClicked.register(          //then remove, which also clears the screen/client property value
					__ -> btn.getParentMenu().rootContextMenuProperty().get().remove());
			this.entries.add(btn);
			return this;
		}
		// --------------------------------------------------
		/**
		 * Adds a {@link TButtonWidget} that opens a sub-menu when clicked.
		 * @param text The text to display on the entry.
		 * @param menu The {@link TContextMenu} to open when the entry is clicked.
		 * @throws NullPointerException If an argument is {@code null}.
		 */
		public final Builder addContextMenu(
				@NotNull Component text,
				@NotNull TContextMenu menu) throws NullPointerException
		{
			//create the button and configure it
			final var btn = new Button();
			btn.getLabel().setText(text);
			btn.getLabel().textAlignmentProperty().set(CompassDirection.WEST, Builder.class);
			btn.setBounds(0, 0, this.client.font.width(text) + (TButtonWidget.LBL_PAD_X * 2), 15);
			btn.eClicked.register(__ -> {
				//add the context menu
				btn.getParentMenu().add(menu);
				//move the menu to the correct position
				final var pbb = btn.getParentMenu().getBounds();
				final var bbb = btn.getBounds();
				menu.moveTo(pbb.endX, bbb.y);
				menu.snapToParent();
			});
			//add the entry
			this.entries.add(btn);
			return this;
		}
		// --------------------------------------------------
		/**
		 * Adds a separator line to the context menu.
		 */
		public final Builder addSeparator()
		{
			final var sep = new TElement() {
				public final @Override void renderCallback(@NotNull TGuiGraphics pencil){
					final var bb = getBounds();
					pencil.fillColor(bb.x + 2, bb.y + (bb.height / 2), bb.width - 4, 1, 0x2eFFFFFF);
				}
			};
			sep.setBounds(0, 0, 0, 4);
			this.entries.add(sep);
			return this;
		}
		// ==================================================
		/**
		 * Builds and returns the {@link TContextMenu} instance.
		 */
		public final @NotNull TContextMenu build()
		{
			//construct the panel and add entries to it
			final var panel = new TPanelElement.Transparent();
			panel.setBounds(0, 0, calcMaxWidth(this.entries), 0);
			panel.addAllVertically(this.entries, 0);
			panel.setBounds(panel.getContentBounds());

			//constrain panel's size
			boolean panelTooBig = false;
			{
				final var pbb  = panel.getBounds();
				if(pbb.width > this.maxW || pbb.height > this.maxH) {
					panel.setBounds(pbb.x, pbb.y, min(pbb.width, maxW), min(pbb.height, maxH));
					panelTooBig = true;
				}
			}

			//build the context menu
			final var ctx = new TContextMenu() {
				public final @Override void renderCallback(@NotNull TGuiGraphics pencil) {
					renderShadow(pencil);
					final var bb = getBounds();
					pencil.drawGuiSprite(
							isFocused() ? TCDCSprites.gui_popup_ctxmenuHighlighted() : TCDCSprites.gui_popup_ctxmenu(),
							bb.x, bb.y, bb.width, bb.height, 0xFFFFFFFF);
				}
			};
			ctx.setBounds(panel.getBounds().add(-1, -2, 2, 4));
			ctx.add(panel);

			//if the panel ended up being too big, add hover scrolls
			if(panelTooBig)
			{
				//obtain context menu bounding box
				final var mbb = ctx.getBounds();
				//create hover-scrolls and add them to the context menu
				final var scroll_up = new HScroll(panel);
				scroll_up.directionProperty().set(CompassDirection.NORTH, Builder.class);
				scroll_up.setBounds(mbb.x + 2, mbb.y + 1, mbb.width - 4, 15);
				ctx.add(scroll_up);
				final var scroll_down = new HScroll(panel);
				scroll_down.directionProperty().set(CompassDirection.SOUTH, Builder.class);
				scroll_down.setBounds(mbb.x + 2, mbb.endY - 15 - 1, mbb.width - 4, 15);
				ctx.add(scroll_down);
				//create arrow labels and add them to the hover-scrolls
				final var lbl_up = new TLabelElement();
				lbl_up.setText(Component.literal("▲"));
				lbl_up.textAlignmentProperty().set(CompassDirection.CENTER, Builder.class);
				lbl_up.setBounds(scroll_up.getBounds());
				scroll_up.add(lbl_up);
				final var lbl_down = new TLabelElement();
				lbl_down.setText(Component.literal("▼"));
				lbl_down.textAlignmentProperty().set(CompassDirection.CENTER, Builder.class);
				lbl_down.setBounds(scroll_down.getBounds());
				scroll_down.add(lbl_down);
			}

			//initialize and return the context panel
			ctx.clearAndInit(); //highly important - executed last
			return ctx;
		}
		// ==================================================
		/**
		 * {@link TButtonWidget} implementation used by this type of context menu.
		 */
		private static final class Button extends TButtonWidget.Transparent {
			public final @Override void postRenderCallback(@NotNull TGuiGraphics pencil) {}
			public final @Override void renderCallback(@NotNull TGuiGraphics pencil) {
				if(!isHoveredOrFocused()) return;
				final var bb = getBounds();
				pencil.fillColor(bb.x, bb.y, bb.width, bb.height, 0xFF363635);
			}
			public final @NotNull TContextMenu getParentMenu() {
				final var parent = findParent(p -> p instanceof TContextMenu).orElse(null);
				assert parent != null; //must always be the case
				return (TContextMenu) parent;
			}
		}

		/**
		 * {@link THoverScrollElement} with a fitting texture.
		 */
		private static final class HScroll extends THoverScrollElement.Panel
		{
			public HScroll(@NotNull TPanelElement target) { super(target); }
			public @Virtual @Override void renderCallback(@NotNull TGuiGraphics pencil) {
				final var bb = getBounds();
				pencil.fillColor(bb.x, bb.y, bb.width, bb.height, 0xAA000000);
				//the following didn't work out. incompatible with custom resource packs:
				//pencil.drawGuiSprite(TCDCSprites.gui_ctxmenu(), bb.x, bb.y, bb.width, bb.height, 0xFFDDDDDD);
				//pencil.drawTexture(TCDCTex.gui_ctxmenu(), bb.x, bb.y, bb.width, bb.height, 4, 4, 4, 4, 12, 12, 0xFFDDDDDD);
			}
		}
		// ==================================================
	}
	// ================================================== ==================================================
}
