package com.thecsdev.commonmc.api.client.gui.widget;

import com.thecsdev.common.math.Bounds2i;
import com.thecsdev.common.properties.IChangeListener;
import com.thecsdev.common.properties.ObjectProperty;
import com.thecsdev.common.util.annotations.Virtual;
import com.thecsdev.common.util.enumerations.CompassDirection;
import com.thecsdev.commonmc.api.client.gui.TElement;
import com.thecsdev.commonmc.api.client.gui.ctxmenu.TContextMenu;
import com.thecsdev.commonmc.api.client.gui.misc.TTextureElement;
import com.thecsdev.commonmc.api.client.gui.panel.TPanelElement;
import com.thecsdev.commonmc.api.client.gui.render.TGuiGraphics;
import com.thecsdev.commonmc.resources.TCDCLang;
import com.thecsdev.commonmc.resources.TCDCSprites;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;

import static java.lang.Math.clamp;
import static java.lang.Math.min;

/**
 * A {@link TButtonWidget} that shows a dropdown menu featuring clickable items
 * when clicked.
 */
public @Virtual class TDropdownWidget<E extends TDropdownWidget.Entry> extends TButtonWidget
{
	// ================================================== ==================================================
	//                                    TDropdownWidget IMPLEMENTATION
	// ================================================== ==================================================
	private static final Identifier[] ICONS = new Identifier[] {
		TCDCSprites.gui_widget_dropdownCollapsed(), //CLOSED
		TCDCSprites.gui_widget_dropdownExpanded()   //OPENED
	};
	// ==================================================
	private final ObjectProperty<E> selectedEntry = new ObjectProperty<>();
	private final Collection<E>     entries       = new LinkedHashSet<>();
	// --------------------------------------------------
	private final TTextureElement   icon          = new TTextureElement();
	// ==================================================
	public TDropdownWidget() { this(null); }
	public TDropdownWidget(@Nullable E selectedEntry)
	{
		//initialize selected entry variable
		this.selectedEntry.getHandle().set(selectedEntry); //no invoking change listeners here
		this.selectedEntry.addChangeListener((p, o, n) -> {
			//if no entry is selected, use the default label
			if(n == null) getLabel().setText(TCDCLang.gui_dropdown_defaultLabel());
			//else use the entry's label
			else getLabel().setText(n.getDisplayName());
		});

		//configure the label and the icon
		getLabel().textAlignmentProperty().set(CompassDirection.WEST, TDropdownWidget.class);
		getLabel().setText(selectedEntry != null ?
				selectedEntry.getDisplayName() :
				TCDCLang.gui_dropdown_defaultLabel());
		this.icon.modeProperty().set(TTextureElement.Mode.GUI_SPRITE, TDropdownWidget.class);
		this.icon.textureProperty().set(ICONS[0], TDropdownWidget.class);
	}
	// ==================================================
	/**
	 * The {@link ObjectProperty} holding the currently selected {@link Entry}.
	 * @apiNote The selected {@link Entry} value can be {@code null}.
	 */
	public final ObjectProperty<E> selectedEntryProperty() { return this.selectedEntry; }
	// --------------------------------------------------
	/**
	 * Returns the {@link Collection} featuring {@link Entry}s that were
	 * added to this {@link TDropdownWidget}.
	 * @apiNote Do <b>NOT</b> add {@code null} entries to the {@link Collection}!
	 */
	public final Collection<E> getEntries() { return this.entries; }
	// ==================================================
	protected @Virtual @Override void initCallback()
	{
		//initialize the button label
		super.initCallback();
		//initialize the dropdown icon
		final var bb   = getBounds();
		final int size = (int) min((double) min(bb.width, bb.height) * 0.6, 20);
		this.icon.setBounds(bb.endX - size - 5, bb.y + (bb.height / 2) - (size /2 ), size, size);
		add(this.icon);
	}

	protected final @Override void clickCallback()
	{
		//super handler
		super.clickCallback();
		//ensure a screen is present, as it is required
		final @Nullable var screen = screenProperty().get();
		if(screen == null) return;
		//create a dropdown element instance and add it to the screen
		final var dd = new TDropdownElement();
		dd.boundsProperty().addFilter((n)-> {
			//move element back on-screen if it goes off-screen
			final var sbb = screen.getBounds();
			final var dX  = n.endX > sbb.endX ? sbb.endX - n.endX : 0;
			final var dY  = n.endY > sbb.endY ? sbb.endY - n.endY : 0;
			return (dX == 0 && dY == 0) ? n : new Bounds2i(n.x + dX, n.y + dY, n.width, n.height);
		}, TDropdownWidget.class);
		screen.add(dd);
		dd.clearAndInit();
	}
	// ================================================== ==================================================
	//                                   TDropdownElement IMPLEMENTATION
	// ================================================== ==================================================
	private final class TDropdownElement extends TContextMenu
	{
		// ==================================================
		public TDropdownElement()
		{
			//handle focus loss by refocusing onto the dropdown widget
			final IChangeListener<TElement> cl_focus = (p, o, n) -> {
				//if still focused, do nothing
				if(isFocusAncestor()) return;
				//close this dropdown if focus is lost and a (grand/)child is not focused now
				remove();
				//set the focus back to the dropdown widget
				p.set(TDropdownWidget.this, TDropdownElement.class);
			};
			screenProperty().addChangeListener((p, o, n) -> {
				if(o != null) o.focusedElementProperty().removeChangeListener(cl_focus);
				if(n != null) n.focusedElementProperty().addChangeListener(cl_focus);
			});

			//handle dropdown icon changing
			screenProperty().addChangeListener((p, o, n) -> TDropdownWidget.this.icon.textureProperty().set(
				(n == null) ? TDropdownWidget.ICONS[0] : TDropdownWidget.ICONS[1],
				TDropdownElement.class
			));
		}
		// ==================================================
		protected final @Override void initCallback()
		{
			//first create the panel element which will be used for entry scrolling
			final var panel = new TPanelElement.Transparent();
			panel.scrollPaddingProperty().set(0, TDropdownElement.class);

			//iterate entries and add their buttons to the panel
			int entryMaxW = 0;
			for(final @NotNull var entry : TDropdownWidget.this.entries)
			{
				//ensure entry is not null
				Objects.requireNonNull(entry, "Found a 'null' entry in a dropdown widget");

				//create and configure the entry button
				final var btn = new TButtonWidget.Transparent() {
					public final @Override void postRenderCallback(@NotNull TGuiGraphics pencil) {}
					public final @Override void renderCallback(@NotNull TGuiGraphics pencil) {
						if(!isHoveredOrFocused()) return;
						final var bb = getBounds();
						pencil.fillColor(bb.x, bb.y, bb.width, bb.height, 0xFF363635);
					}
				};
				final var lbl = btn.getLabel();
				lbl.textAlignmentProperty().set(CompassDirection.WEST, TDropdownElement.class);
				lbl.setText(entry.getDisplayName());
				entryMaxW = Math.max(lbl.fontProperty().get().width(lbl.textProperty().get()), entryMaxW);

				//set entry button on-click logic
				btn.eClicked.register(__ -> {
					//first close this dropdown by removing it
					TDropdownElement.this.remove();
					//then set new selected entry value
					TDropdownWidget.this.selectedEntry.set(entry, TDropdownElement.class);
				});

				//add the entry button
				panel.add(btn);
			}
			entryMaxW += 10; //small offset to allow for text padding to fit

			//iterate all added button and set their bounding boxes
			int nextY = 0, entryH = 15;
			for(final var entry : panel) {
				entry.setBounds(0, nextY, entryMaxW, entryH);
				nextY += entryH;
			}

			final int SCROLL_W = 8;
			/*make the panel fit its child entries*/ {
				final var dbb  = TDropdownWidget.this.getBounds(); //dropdown-widget bounding box
				final var pcbb = panel.getContentBounds();         //panel content bounding box
				panel.setBounds(0, 0,
						clamp(dbb.width - SCROLL_W, 100, 250),
						clamp(pcbb.height, entryH, 100));
			}

			/*make this dropdown fit the panel, and add the panel to it*/ {
				final var dbb = TDropdownWidget.this.getBounds(); //dropdown-widget bounding box
				final var pbb = panel.getBounds();                //panel bounding box
				setBounds(dbb.x, dbb.endY, pbb.width + SCROLL_W, pbb.height + SCROLL_W);
				addRel(panel);
			}

			/*add scroll-bars for the panel*/ {
				final var bb = getBounds();
				final var scroll_v = new TScrollBarWidget.Flat(panel, TScrollBarWidget.ScrollDirection.VERTICAL);
				scroll_v.setBounds(bb.endX - SCROLL_W, bb.y + 1, SCROLL_W - 1, bb.height - SCROLL_W - 1);
				add(scroll_v);
				final var scroll_h = new TScrollBarWidget.Flat(panel, TScrollBarWidget.ScrollDirection.HORIZONTAL);
				scroll_h.setBounds(bb.x + 1, bb.endY - SCROLL_W, bb.width - SCROLL_W - 1, SCROLL_W - 1);
				add(scroll_h);
			}

			/*ensure the buttons aren't too small and are at least of panel's width*/
			do {
				//check if the buttons are wide enough. if yes, break
				final var pbb = panel.getBounds();
				if(entryMaxW >= pbb.width) break;
				//iterate all buttons and enforce minimum size
				final int diff = pbb.width - entryMaxW;
				for(final var btn : panel) btn.setBounds(btn.getBounds().add(0, 0, diff, 0));
			} while(false);
		}
		// ==================================================
	}
	// ================================================== ==================================================
	//                                              Entry IMPLEMENTATION(S)
	// ================================================== ==================================================
	/**
	 * An entry in a given {@link TDropdownWidget}. Contains information on the
	 * entry's display name and what should happen when clicked.
	 */
	public static interface Entry
	{
		// ==================================================
		/**
		 * The display name of the {@link Entry} that is to be displayed on
		 * a given {@link TDropdownWidget}.
		 */
		@NotNull Component getDisplayName();
		// ==================================================
	}

	/**
	 * Barebones {@link TDropdownWidget.Entry} implementation.
	 */
	public static final class SimpleEntry implements TDropdownWidget.Entry
	{
		// ==================================================
		private final Component displayName;
		// ==================================================
		public SimpleEntry(@NotNull Component displayName) throws NullPointerException {
			this.displayName = Objects.requireNonNull(displayName);
		}
		// ==================================================
		public final @Override @NotNull Component getDisplayName() { return this.displayName; }
		// ==================================================
		public final @Override int hashCode() { return Objects.hash(this.displayName); }
		public final @Override boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null || getClass() != obj.getClass()) return false;
			final var other = (SimpleEntry) obj;
			return Objects.equals(this.displayName, other.displayName);
		}
		// ==================================================
	}
	// ================================================== ==================================================
}
