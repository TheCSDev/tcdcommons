package com.thecsdev.common.scene;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * An interface representing a {@link Node} that has a bounding box.
 */
@ApiStatus.NonExtendable //future removal may happen
public interface INodeBounded<N extends Node<N>, Bounds> extends INode<N>
{
	/**
	 * Returns the bounding box of this {@link Node}.
	 */
	@NotNull Bounds getBounds();
}
