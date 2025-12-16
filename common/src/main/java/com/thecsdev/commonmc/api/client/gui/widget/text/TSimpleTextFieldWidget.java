package com.thecsdev.commonmc.api.client.gui.widget.text;

import com.thecsdev.common.math.Bounds2i;
import com.thecsdev.common.properties.NotNullProperty;
import com.thecsdev.common.util.annotations.Virtual;
import com.thecsdev.common.util.enumerations.CompassDirection;
import com.thecsdev.commonmc.api.client.gui.label.TLabelElement;
import com.thecsdev.commonmc.api.client.gui.render.TGuiGraphics;
import com.thecsdev.commonmc.api.client.gui.util.CursorType;
import com.thecsdev.commonmc.api.client.gui.util.TGuiUtils;
import com.thecsdev.commonmc.api.client.gui.util.TInputContext;
import com.thecsdev.commonmc.api.client.gui.widget.TClickableWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.thecsdev.commonmc.api.client.gui.panel.TPanelElement.COLOR_OUTLINE;
import static com.thecsdev.commonmc.api.client.gui.panel.TPanelElement.COLOR_OUTLINE_FOCUSED;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE;

/**
 * Text input widget where the user may type in text. Very minimal and
 * does not feature the concept of cursors and multiline support.
 * @apiNote Uses unoptimal practices that cost extra memory and performance.
 */
public final class TSimpleTextFieldWidget extends TClickableWidget
{
	// ==================================================
	private final NotNullProperty<Font>      font        = new NotNullProperty<>(Minecraft.getInstance().font);
	private final NotNullProperty<Component> placeholder = new NotNullProperty<>(Component.empty());
	private final NotNullProperty<String>    text        = new NotNullProperty<>("");
	// --------------------------------------------------
	private final TLabelElement lbl_placeholder = new TLabelElement();
	private final TLabelElement lbl_text        = new TLabelElement();
	// ==================================================
	public TSimpleTextFieldWidget()
	{
		//click sounds are not appropriate here
		super.eClicked.unregister(ONCLICK_SOUND);

		//initialize labels
		this.lbl_text.hoverableProperty().set(false, TSimpleTextFieldWidget.class);
		this.lbl_text.textAlignmentProperty().set(CompassDirection.WEST, TSimpleTextFieldWidget.class);
		this.lbl_placeholder.hoverableProperty().set(false, TSimpleTextFieldWidget.class);
		this.lbl_placeholder.textAlignmentProperty().set(CompassDirection.WEST, TSimpleTextFieldWidget.class);
		this.lbl_placeholder.textColorProperty().set(0x55FFFFFF, TSimpleTextFieldWidget.class);

		//change listeners
		final Runnable refresh_alignment = () -> {
			final int textW = this.font.get().width(this.text.get());
			this.lbl_text.textAlignmentProperty().set(
				(this.lbl_text.getBounds().width > textW) ? CompassDirection.WEST : CompassDirection.EAST,
				TSimpleTextFieldWidget.class
			);
		};
		boundsProperty().addChangeListener((p, o, n) -> refresh_alignment.run());
		this.font.addChangeListener((p, o, n) -> {
			this.lbl_placeholder.fontProperty().set(n, TSimpleTextFieldWidget.class);
			this.lbl_text.fontProperty().set(n, TSimpleTextFieldWidget.class);
			refresh_alignment.run();
		});
		this.placeholder.addChangeListener((p, o, n) -> this.lbl_placeholder.setText(n));
		this.text.addChangeListener((p, o, n) -> {
			this.lbl_text.setText(Component.literal(n));
			this.lbl_placeholder.visibleProperty().set(n.isEmpty(), TSimpleTextFieldWidget.class);
			refresh_alignment.run();
		});
	}
	// ==================================================
	/**
	 * The {@link NotNullProperty} holding this {@link TSimpleTextFieldWidget}'s text
	 * the user typed in.
	 */
	public final NotNullProperty<String> textProperty() { return this.text; }

	/**
	 * The {@link NotNullProperty} for this {@link TSimpleTextFieldWidget}'s {@link Font}
	 * used for text rendering.
	 */
	public final NotNullProperty<Font> fontProperty() { return this.font; }

	/**
	 * The {@link NotNullProperty} for this {@link TSimpleTextFieldWidget}'s placeholder
	 * text that is rendered when the user has yet to input (type) something.
	 */
	public final NotNullProperty<Component> placeholderProperty() { return this.placeholder; }
	// --------------------------------------------------
	/**
	 * Returns the {@link TLabelElement} that displays the user-typed text.<br>
	 * Your access to this label is intended for label property changing only!
	 */
	public final TLabelElement getTextLabel() { return this.lbl_text; }

	/**
	 * Returns the {@link TLabelElement} that displays the placeholder text.<br>
	 * Your access to this label is intended for label property changing only!
	 */
	public final TLabelElement getPlaceholderLabel() { return this.lbl_placeholder; }
	// ==================================================
	public @Virtual @Override @NotNull CursorType getCursor() {
		return isFocusable() ? CursorType.IBEAM : CursorType.NOT_ALLOWED;
	}
	// ==================================================
	protected final @Override void initCallback() {
		final var bb  = getBounds();
		final var lbb = new Bounds2i(bb.x + 4, bb.y + 2, bb.width - 8, bb.height - 4);
		this.lbl_placeholder.setBounds(lbb);
		add(this.lbl_placeholder);
		this.lbl_text.setBounds(lbb);
		add(this.lbl_text);
	}
	// --------------------------------------------------
	public @Virtual @Override void renderCallback(@NotNull TGuiGraphics pencil) {
		final var bb = getBounds();
		pencil.fillColor(bb.x, bb.y, bb.width, bb.height, 0xFF000000);
	}

	public @Virtual @Override void postRenderCallback(@NotNull TGuiGraphics pencil) {
		final var bb = getBounds();
		if(isHoveredOrFocused())
			pencil.drawOutlineIn(bb.x, bb.y, bb.width, bb.height, COLOR_OUTLINE_FOCUSED);
		else pencil.drawOutlineIn(bb.x, bb.y, bb.width, bb.height, COLOR_OUTLINE);
	}
	// --------------------------------------------------
	@SuppressWarnings("DataFlowIssue")
	public final @Override boolean inputCallback(TInputContext.@NotNull InputDiscoveryPhase phase, @NotNull TInputContext context)
	{
		//forward to super first
		if(super.inputCallback(phase, context)) return true;
		//handle only the main phase and if focusable
		else if(phase != TInputContext.InputDiscoveryPhase.MAIN || !isFocused())
			return false;

		//handle based on input type
		final boolean typed = switch(context.getInputType()) {
			//on click, handle to cath focus
			case CHAR_TYPE -> inputText(context.getCharacter().toString());
			case KEY_PRESS -> context.getKeyCode() == GLFW_KEY_BACKSPACE && inputBackspace(1);
			default -> false;
		};
		if(typed) TGuiUtils.playGuiTypingSound();
		return typed;
	}
	// ==================================================
	/**
	 * Inputs a textual {@link String}.
	 * @param text The text to insert.
	 * @return {@code true} if the text changed as a result of this operation.
	 */
	public final boolean inputText(@NotNull String text) {
		Objects.requireNonNull(text);
		final var old_text = this.text.get();
		this.text.set(old_text + text, TSimpleTextFieldWidget.class);
		return !Objects.equals(old_text, this.text.get());
	}

	/**
	 * Inputs backspace.
	 * @param amount The amount of times to input backspace.
	 * @return {@code true} if the text changed as a result of this operation.
	 */
	public final boolean inputBackspace(int amount) {
		final var old_text = this.text.get();
		final var old_text_len = old_text.length();
		amount = Math.clamp(amount, 0, old_text_len);
		this.text.set(old_text.substring(0, old_text_len - amount), TSimpleTextFieldWidget.class);
		return !Objects.equals(old_text, this.text.get());
	}
	// ==================================================
}
