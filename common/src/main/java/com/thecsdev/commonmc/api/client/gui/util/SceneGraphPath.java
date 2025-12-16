package com.thecsdev.commonmc.api.client.gui.util;

import com.thecsdev.commonmc.api.client.gui.TElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Represents a path in the scene graph from a parent {@link TElement}
 * down to a child {@link TElement}, via all intermediate elements.
 * <p>
 * Each segment in the path specifies the expected {@link Class} type
 * of the child {@link TElement} at that segment, as well as the expected
 * index of that child within its parent segment.
 */
public final class SceneGraphPath
{
	// ==================================================
	private final @NotNull  Class<? extends TElement> child;      //expected child type
	private final           int                       childIndex; //expected child index within parent
	private final @Nullable SceneGraphPath            next;       //next path segment
	// ==================================================

	/**
	 * Creates a new {@link SceneGraphPath} segment instance.
	 *
	 * @param child      The expected {@link Class} type of the child {@link TElement} at this segment.
	 * @param childIndex The expected index of the child {@link TElement} within its parent at this segment.
	 * @param path       The next segment in the scene graph path, or {@code null} if this is the last segment.
	 * @throws NullPointerException If {@code child} is {@code null}.
	 */
	public SceneGraphPath(
			@NotNull Class<? extends TElement> child,
			int childIndex, @Nullable SceneGraphPath path)
			throws NullPointerException
	{
		this.child = Objects.requireNonNull(child);
		this.childIndex = childIndex;
		this.next = path;
	}
	// ==================================================

	/**
	 * Returns the expected {@link Class} of the child {@link TElement}
	 * at this path segment.
	 */
	public @NotNull Class<? extends TElement> getChild()
	{
		return this.child;
	}

	/**
	 * Returns the expected index of the child {@link TElement}
	 * within its parent at this path segment.
	 */
	public int getChildIndex()
	{
		return this.childIndex;
	}

	/**
	 * Returns the next segment in this scene graph path,
	 * or {@code null} if this is the last segment.
	 */
	public @Nullable SceneGraphPath getNext()
	{
		return this.next;
	}
	// ==================================================
	/**
	 * Resolves this {@link SceneGraphPath} starting from a given {@link TElement},
	 * returning the final {@link TElement} at the end of the path, if found.
	 * @param from The {@link TElement} from which to start resolving the path.
	 * @return The resolved {@link TElement}, or {@code null} if the path
	 * could not be fully resolved.
	 * @throws NullPointerException If the argument is {@code null}.
	 */
	public final @Nullable TElement resolve(@NotNull TElement from) throws NullPointerException {
		return SceneGraphPath.resolve(from, this);
	}
	// ==================================================

	/**
	 * Creates a {@link SceneGraphPath} that navigates from a given
	 * parent {@link TElement} down to a specified child {@link TElement}.
	 *
	 * @param from The parent {@link TElement} from which to navigate.
	 * @param to   The child {@link TElement} to which to navigate.
	 * @return The {@link SceneGraphPath} from {@code from} to {@code to},
	 * or {@code null} if {@code from} is the same as {@code to}, or if
	 * {@code to} is not a descendant of {@code from}.
	 * @throws NullPointerException If an argument is {@code null}.
	 */
	public static final @Nullable SceneGraphPath of(@NotNull TElement from, @Nullable TElement to)
	{
		//not null requirements
		Objects.requireNonNull(from);
		//the destination must not be null and must be a descendant of the source
		if(to == null || !TGuiUtils.isAncestor(to, from))
			return null;

		//navigate from "to" up to "from"
		TElement current = to;
		SceneGraphPath path = null;
		while(current != from)
		{
			final var parent = current.getParent();
			if(parent == null) break;
			final int index = parent.indexOf(current);
			path = new SceneGraphPath(current.getClass(), index, path);
			current = parent;
		}

		//if we didn't reach "from", return null
		if(current != from) return null;
		else return path;
	}

	/**
	 * Resolves a {@link SceneGraphPath} starting from a given {@link TElement},
	 * returning the final {@link TElement} at the end of the path, if found.
	 *
	 * @param from The {@link TElement} from which to start resolving the path.
	 * @param path The {@link SceneGraphPath} to resolve.
	 * @return The resolved {@link TElement}, or {@code null} if the path
	 * could not be fully resolved.
	 * @throws NullPointerException If an argument is {@code null}.
	 */
	public static final @Nullable TElement resolve(
			@NotNull TElement from, @NotNull SceneGraphPath path) throws NullPointerException
	{
		//not null requirements
		Objects.requireNonNull(from);
		Objects.requireNonNull(path);

		//start from the "from" element
		TElement current = from;

		//traverse the path
		SceneGraphPath segment = path;
		while(segment != null)
		{
			//obtain the expected child type and index
			final var expectedType = segment.getChild();
			final int expectedIndex = segment.getChildIndex();

			//obtain the actual child at the expected index
			if(expectedIndex < 0 || expectedIndex >= current.size())
				return null; //index out of bounds
			final var child = current.get(expectedIndex);
			if(child == null) return null; //no child found

			//check if the child is of the expected type
			if(!expectedType.isInstance(child))
				return null; //type mismatch

			//advance to the next segment
			current = child;
			segment = segment.getNext();
		}

		//once done, return the resolved element
		return current;
	}
	// ==================================================
}
