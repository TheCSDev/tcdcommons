package com.thecsdev.commonmc.api.client.gui.screen;

import com.thecsdev.common.math.UDim2;
import com.thecsdev.common.util.annotations.Virtual;
import com.thecsdev.common.util.enumerations.CompassDirection;
import com.thecsdev.commonmc.api.client.gui.TElement;
import com.thecsdev.commonmc.api.client.gui.ctxmenu.TContextMenu;
import com.thecsdev.commonmc.api.client.gui.label.TLabelElement;
import com.thecsdev.commonmc.api.client.gui.misc.TFillColorElement;
import com.thecsdev.commonmc.api.client.gui.panel.TPanelElement;
import com.thecsdev.commonmc.api.client.gui.panel.window.TWindowElement;
import com.thecsdev.commonmc.api.client.gui.render.TGuiGraphics;
import com.thecsdev.commonmc.api.client.gui.tooltip.TTooltip;
import com.thecsdev.commonmc.api.client.gui.widget.TButtonWidget;
import com.thecsdev.commonmc.api.client.gui.widget.TScrollBarWidget;
import com.thecsdev.commonmc.resources.TCDCLang;
import com.thecsdev.commonmc.resources.TCDCSprites;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static com.thecsdev.commonmc.resources.TComponent.gui;
import static net.minecraft.network.chat.Component.translatable;

/**
 * {@link TScreen} implementation that shows a textual dialog to the user and
 * presents the user with some dialog options (clickable buttons).
 */
public final class TTextDialogScreen extends TScreenPlus implements ILastScreenProvider
{
	// ================================================== ==================================================
	//                                      TDialogScreen IMPLEMENTATION
	// ================================================== ==================================================
	private final @Nullable Screen        lastScreen;
	// --------------------------------------------------
	private final           TLabelElement lbl_message = new TLabelElement();
	// ==================================================
	public TTextDialogScreen(@Nullable Screen lastScreen, @NotNull Component text) {
		this(lastScreen, gui("icon/info").append(" ").append(TCDCLang.gui_screen_textDialog_defaultTitle()), text);
	}
	public TTextDialogScreen(@Nullable Screen lastScreen, @NotNull Component title, @NotNull Component text) {
		this.lastScreen = lastScreen;
		titleProperty().set(title, TTextDialogScreen.class);
		this.lbl_message.setText(text);
		this.lbl_message.wrapTextProperty().set(true, TTextDialogScreen.class);
		this.lbl_message.textScaleProperty().set(0.8, TTextDialogScreen.class);
		this.lbl_message.textColorProperty().set(0xBBFFFFFF, TTextDialogScreen.class);
		this.lbl_message.textAlignmentProperty().set(CompassDirection.NORTH_WEST, TTextDialogScreen.class);
	}
	// ==================================================
	/**
	 * Returns the {@link TLabelElement} that will display the dialog message.
	 */
	public final TLabelElement getMessageLabel() { return this.lbl_message; }
	// --------------------------------------------------
	/**
	 * Utility method for setting the dialog message. Sets the text value
	 * of {@link #getMessageLabel()}.
	 * @param message The message to display.
	 * @see #getMessageLabel()
	 */
	public final void setMessage(@NotNull Component message) { this.lbl_message.setText(message); }
	// ==================================================
	public final @Override @Nullable Screen getLastScreen() { return this.lastScreen; }
	// --------------------------------------------------
	public @Virtual @Override void renderCallback(@NotNull TGuiGraphics pencil) {
		//the following is the background that is to be rendered:
		if(this.lastScreen != null) {
			//the last screen is to be visually rendered below this screen
			this.lastScreen.render(pencil.getNative(), pencil.getMouseX(), pencil.getMouseY(), pencil.getDeltaTicks());
			//followed by a white plane background so it's easier to tell screens apart
			final var bb = getBounds();
			pencil.fillColor(bb.x, bb.y, bb.width, bb.height, 0x22FFFFFF);
		}
	}
	// --------------------------------------------------
	protected final @Override void initCallback() {
		//create and add the window element
		final var wnd = new WindowElement();
		add(wnd);
		wnd.setBounds(new UDim2(0.25, 0, 0.25, 0), new UDim2(0.5, 0, 0.5, 0));
	}
	// ================================================== ==================================================
	//                                      WindowElement IMPLEMENTATION
	// ================================================== ==================================================
	/**
	 * The {@link TWindowElement} whose GUI features the dialog message and
	 * action buttons.
	 */
	final class WindowElement extends TWindowElement
	{
		// ==================================================
		public WindowElement() {
			titleProperty().set(TTextDialogScreen.this.title.get(), WindowElement.class);
			backgroundColorProperty().set(0xFF2b2b2b, WindowElement.class);
			closeOperationProperty().set(TWindowElement.CloseOperation.CLOSE_SCREEN, WindowElement.class);
		}
		// ==================================================
		protected final @Override void initBodyCallback(@NotNull TElement body)
		{
			//background and panel for the label
			final var el_background = new TFillColorElement.Flat(0x22000000, 0x55000000);
			el_background.setBounds(body.getBounds().add(10, 10, -28, -20 - 17));
			body.add(el_background);

			final var el_panel = new TPanelElement.Paintable(0, 0, 0x77FFFFFF);
			el_panel.setBounds(el_background.getBounds());
			el_panel.scrollPaddingProperty().set(10, WindowElement.class);
			el_background.add(el_panel);

			final var bb_panel     = el_panel.getBounds();
			final var scroll_panel = new TScrollBarWidget.Flat(el_panel);
			scroll_panel.setBounds(bb_panel.endX, bb_panel.y, 8, bb_panel.height);
			body.add(scroll_panel);

			//the label itself
			final var el_label = TTextDialogScreen.this.lbl_message;
			el_label.setBoundsToFitText(
					el_panel.getBounds().x + 10, el_panel.getBounds().y + 10,
					el_panel.getBounds().width - 20);
			el_panel.add(el_label);

			//the 'Done' button
			final var btn_done = new TButtonWidget.Paintable(0x22FFFFFF, 0x55FFFFFF);
			btn_done.setBounds(
					bb_panel.x + (bb_panel.width / 4) + 20, bb_panel.endY + 5,
					(bb_panel.width / 2) - 20, 15);
			btn_done.getLabel().setText(translatable("gui.done"));
			btn_done.getLabel().textScaleProperty().set(0.8, WindowElement.class);
			btn_done.eClicked.register(__ -> TTextDialogScreen.this.close());
			body.add(btn_done);

			//the 'Copy to clipboard' button
			final var bb_done  = btn_done.getBounds();
			final var btn_copy = new TButtonWidget.Paintable(0x22FFFFFF, 0x55FFFFFF);
			btn_copy.setBounds(bb_done.x - 20, bb_done.y, 15, 15);
			btn_copy.getLabel().setText(gui(TCDCSprites.gui_icon_clipboard()));
			btn_copy.eClicked.register(btn -> Objects.requireNonNull(btn.getClient(), "Missing 'client' instance")
					.keyboardHandler.setClipboard(el_label.getText().getString()));
			btn_copy.tooltipProperty().set(__ -> TTooltip.of(translatable("chat.copy")), WindowElement.class);
			body.add(btn_copy);

			//context menu
			el_panel.contextMenuProperty().set(__ -> {
				final var client = Objects.requireNonNull(__.getClient(), "Missing 'client' instance");
				return new TContextMenu.Builder(client)
						.addButton(
								gui(TCDCSprites.gui_icon_clipboard()).append(" ").append(translatable("chat.copy")),
								___ -> client.keyboardHandler.setClipboard(el_label.getText().getString()))
						.build();
			}, WindowElement.class);
		}
		// ==================================================
	}
	// ================================================== ==================================================
}
