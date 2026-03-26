package com.thecsdev.common.scene;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * An interface representing a visual {@link Node} that is renderable
 * in a graphical framework.
 */
@ApiStatus.NonExtendable //future removal may happen
public interface INodeRenderable<N extends Node<N>, Context> extends INode<N>
{
	/**
	 * Callback method that is invoked when this {@link Node} is
	 * being rendered.
	 * @param context The rendering context.
	 */
	default void renderCallback(@NotNull Context context) {}

	/**
	 * Callback method that is invoked after this {@link Node} and all
	 * of their children have been rendered.
	 * @param context The rendering context.
	 */
	default void postRenderCallback(@NotNull Context context) {}
}
