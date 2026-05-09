package com.thecsdev.commonmc.api.client.gui.misc;

import com.thecsdev.common.properties.IntegerProperty;
import com.thecsdev.common.properties.NotNullProperty;
import com.thecsdev.common.util.annotations.Virtual;
import com.thecsdev.commonmc.api.client.gui.TElement;
import com.thecsdev.commonmc.api.client.gui.panel.TPanelElement;
import com.thecsdev.commonmc.api.client.gui.render.TGuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Simple {@link TElement} that draws a solid-colored line either
 * horizontally or vertically.
 */
public @Virtual class TSeparatorElement extends TElement
{
	// ==================================================
	/**
	 * The direction in which {@link TSeparatorElement} will draw its line.
	 */
	public static enum Direction { HORIZONTAL, VERTICAL }
	// --------------------------------------------------
	private final IntegerProperty            thickness = new IntegerProperty(1);
	private final NotNullProperty<Direction> direction = new NotNullProperty<>(Direction.HORIZONTAL);
	private final IntegerProperty            color     = new IntegerProperty(0x2eFFFFFF);
	// ==================================================
	public TSeparatorElement() {
		focusableProperty().set(false, TSeparatorElement.class);
		hoverableProperty().set(false, TSeparatorElement.class);
	}
	// ==================================================
	/**
	 * The thickness of the separator line, in {@link Screen}-space units.
	 */
	public final IntegerProperty thicknessProperty() { return this.thickness; }

	/**
	 * The direction in which {@link TSeparatorElement} will draw its line.
	 */
	public final NotNullProperty<Direction> directionProperty() { return this.direction; }

	/**
	 * The color of the separator line, in ARGB format.
	 */
	public final IntegerProperty colorProperty() { return this.color; }
	// ==================================================
	public @Virtual @Override void renderCallback(@NotNull TGuiGraphics pencil)
	{
		//preparation
		final var bb  = getBounds();
		final int thi = this.thickness.getI();
		final int col = this.color.getI();

		//drawing based on direction
		if(this.direction.get() == Direction.HORIZONTAL)
			pencil.fillColor(bb.x, bb.y + (bb.height / 2) - (thi / 2), bb.width, thi, col);
		else pencil.fillColor(bb.x + (bb.width / 2) - (thi / 2), bb.y, thi, bb.height, col);
	}
	// ==================================================
	/**
	 * Creates and initializes a {@link TSeparatorElement} element and then adds
	 * it to a given {@link TPanelElement} and returns it.
	 * @param target The {@link TPanelElement} to which the new {@link TSeparatorElement} will be added.
	 * @param size The width or height of the separator, depending on direction.
	 * @param direction The direction in which the separator will be drawn.
	 * @param thickness The thickness of the separator line, in {@link Screen}-space units.
	 * @param color The color of the separator line, in ARGB format.
	 * @return The created and initialized {@link TSeparatorElement}.
	 * @throws NullPointerException If a {@link NotNull} argument is {@code null}.
	 */
	public static final @NotNull TSeparatorElement init(
			@NotNull TPanelElement target, int size,
			@NotNull Direction direction, int thickness, int color) throws NullPointerException
	{
		//argument not-null checks
		Objects.requireNonNull(target);
		Objects.requireNonNull(direction);

		//create and configure separator
		final var el = new TSeparatorElement();
		el.setBounds(switch (direction) {
			case HORIZONTAL -> target.computeNextYBounds(size, 0);
			case VERTICAL -> target.computeNextXBounds(size, 0);
		});
		el.directionProperty().set(direction, TSeparatorElement.class);
		el.thicknessProperty().set(thickness, TSeparatorElement.class);
		el.colorProperty().set(color, TSeparatorElement.class);

		//add and return
		target.add(el);
		return el;
	}

	/**
	 * Same as {@code #init}, but with {@link Direction#HORIZONTAL}.
	 * @param target The {@link TPanelElement} to which the new {@link TSeparatorElement} will be added.
	 * @param size The width or height of the separator, depending on direction.
	 * @param thickness The thickness of the separator line, in {@link Screen}-space units.
	 * @param color The color of the separator line, in ARGB format.
	 * @return The created and initialized {@link TSeparatorElement}.
	 * @throws NullPointerException If a {@link NotNull} argument is {@code null}.
	 * @see #init(TPanelElement, int, Direction, int, int)
	 */
	public static final @NotNull TSeparatorElement initH(
			@NotNull TPanelElement target, int size, int thickness, int color)
			throws NullPointerException {
		return init(target, size, Direction.HORIZONTAL, thickness, color);
	}

	/**
	 * Same as {@code #init}, but with {@link Direction#VERTICAL}.
	 * @param target The {@link TPanelElement} to which the new {@link TSeparatorElement} will be added.
	 * @param size The width or height of the separator, depending on direction.
	 * @param thickness The thickness of the separator line, in {@link Screen}-space units.
	 * @param color The color of the separator line, in ARGB format.
	 * @return The created and initialized {@link TSeparatorElement}.
	 * @throws NullPointerException If a {@link NotNull} argument is {@code null}.
	 * @see #init(TPanelElement, int, Direction, int, int)
	 */
	public static final @NotNull TSeparatorElement initV(
			@NotNull TPanelElement target, int size, int thickness, int color)
			throws NullPointerException {
		return init(target, size, Direction.VERTICAL, thickness, color);
	}
	// ==================================================
}
