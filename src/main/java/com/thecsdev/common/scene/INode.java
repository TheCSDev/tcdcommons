package com.thecsdev.common.scene;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * An interface representing a {@link Node}.
 * @param <N> The {@link Node#getBaseType()}, in generic form.
 */
public interface INode<N extends Node<N>> extends Collection<N>
{
	/**
	 * Returns the {@link Class} object that represents the base type of this {@link INode}.
	 * @apiNote The returned value must <b>NOT</b> be {@code null}.
	 * @see Node#getBaseType()
	 */
	@NotNull Class<N> getBaseType();
}
