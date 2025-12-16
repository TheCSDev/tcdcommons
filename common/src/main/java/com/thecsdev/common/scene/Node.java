package com.thecsdev.common.scene;

import com.thecsdev.common.properties.IChangeListener;
import com.thecsdev.common.properties.NotNullProperty;
import com.thecsdev.common.properties.ObjectProperty;
import com.thecsdev.common.util.annotations.Virtual;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.thecsdev.common.scene.Node.NodePropertyAccessor.setParentValue;
import static com.thecsdev.common.scene.Node.NodePropertyAccessor.setRootValue;

/**
 * Represents a unique node that is present in a given scene graph.
 * @author TheCSDev
 * @apiNote <a href="https://en.wikipedia.org/wiki/Scene_graph">https://en.wikipedia.org/wiki/Scene_graph</a>
 * @param <N> The {@link #getBaseType()}, in generic form.
 */
public abstract class Node<N extends Node<N>> implements INode<N>
{
	// ================================================== ==================================================
	//                                               Node IMPLEMENTATION
	// ================================================== ==================================================
	final NotNullProperty<N>       root        = new NotNullProperty<>(getSelf());
	final ObjectProperty<N>        parent      = new ObjectProperty<>(null);
	final ConcurrentLinkedDeque<N> children    = new ConcurrentLinkedDeque<>();
	final Set<N>                   childrenSet = new HashSet<>(); //for faster #contains(Object) checks
	// ==================================================
	public Node()
	{
		//behavior of the 'root' and 'parent' properties
		this.root.addChangeListener((p, o, n) -> {
			for(final var child : this) setRootValue(child, n); //root value propagates
		});
		this.root.setReadOnly(true, Node.class);
		this.root.setOwner(NodePropertyAccessor.class, Node.class); //subclasses cannot be owners

		this.parent.setInterceptor((p, o, n) -> {
			if(n != null)      n.add(getSelf());    //setting parent to non-null
			else if(o != null) o.remove(getSelf()); //setting parent to null
		}, Node.class);
		//this.parent.setReadOnly(true); -- prevents interceptor from working
		this.parent.setOwner(NodePropertyAccessor.class, Node.class); //subclasses cannot be owners
	}
	// --------------------------------------------------
	/**
	 * Returns this {@link Node}, cast to its concrete type.
	 * <p>
	 * Override this method in your subclasses and return {@code this}.
	 * For example:
	 * <pre>{@code
	 * public class MyNode extends Node<MyNode>
	 * {
	 *     protected MyNode getSelf() { return this; }
	 * }
	 * }</pre>
	 * @return {@code this} {@link Node} instance, cast to the type parameter {@code E}.
	 */
	public abstract @NotNull N getSelf();

	/**
	 * Returns the {@link Class} object that represents the base type of this node.
	 * <p>
	 * The <b>base type</b> is the "root" {@link Class} in an {@link Node} hierarchy, from
	 * which all other specific {@link Node} types in a scene graph are derived. For example,
	 * if {@code LightNode}, {@code MeshNode}, and {@code CameraNode} all extend a common
	 * {@code Node} class, then {@code Node} is the base type for that hierarchy.
	 * <p>
	 * Override this method in your subclasses and return the class literal
	 * of your subclass. For example:
	 * <pre>{@code
	 * public class MyNode extends Node<MyNode>
	 * {
	 *     public Class<MyNode> getBaseType() { return MyNode.class; }
	 * }
	 * }</pre>
	 */
	public abstract @NotNull Class<N> getBaseType();
	// ==================================================
	public final @Override boolean equals(Object obj) { return super.equals(obj); }
	public final @Override int hashCode() { return super.hashCode(); }
	// ==================================================
	/**
	 * The {@link NotNullProperty} that holds a reference to the
	 * "root" {@link Node} in the scene graph.
	 * If {@link #getParent()} is {@code null}, this {@link NotNullProperty}
	 * will treat this {@link Node} as the "root" node.
	 * @apiNote Owned by {@link NodePropertyAccessor}.
	 */
	public final NotNullProperty<N> rootProperty() { return this.root; }

	/**
	 * The {@link ObjectProperty} that holds a reference to this
	 * {@link Node}'s parent.
	 * @apiNote Owned by {@link NodePropertyAccessor}.
	 */
	public final ObjectProperty<N> parentProperty() { return this.parent; }
	// ==================================================
	/**
	 * Callback function that is invoked after an {@link Node} is added
	 * as a child to this {@link Node}.
	 * @param child The now new child.
	 */
	@SuppressWarnings("unused")
	protected @Virtual void childAddedCallback(@NotNull N child) {}

	/**
	 * Callback function that is invoked after an {@link Node} is removed
	 * from this {@link Node}.
	 * @param pastChild The now "ex" child.
	 */
	@SuppressWarnings("unused")
	protected @Virtual void childRemovedCallback(@NotNull N pastChild) {}
	// ==================================================
	/**
	 * Convenience function that returns the value {@link #parentProperty()}.
	 */
	public final @Nullable N getParent() { return this.parent.get(); }
	// --------------------------------------------------
	/**
	 * Attempts to find a parent or grandparent {@link Node} that matches a given {@link Predicate}.
	 * @param predicate The {@link Predicate} to test.
	 * @return The first parent/grandparent that matches the {@link Predicate}.
	 * @throws NullPointerException If an argument is {@code null}.
	 */
	public final @NotNull Optional<@Nullable N> findParent(
			@NotNull Predicate<N> predicate) throws NullPointerException
	{
		Objects.requireNonNull(predicate);
		@Nullable N sought = this.getParent();
		while(sought != null) {
			if(predicate.test(sought)) return Optional.of(sought);
			else                       sought = sought.getParent();
		}
		return Optional.empty();
	}

	/**
	 * Attempts to find a child (or even a grandchild) {@link Node} (depending on
	 * arguments) that matches a given {@link Predicate}.
	 * @param predicate The {@link Predicate} to test.
	 * @param nested Whether to check grandchildren in nested branches.
	 * @throws NullPointerException If an argument is {@code null}.
	 * @apiNote May run in nondeterministic polynomial time. May be expensive to call.
	 */
	public final @NotNull Optional<@Nullable N> findChild(
			@NotNull Predicate<N> predicate, boolean nested) throws NullPointerException
	{
		Objects.requireNonNull(predicate);
		for(final var child : this)
		{
			if(predicate.test(child))
				return Optional.ofNullable(child);
			if(nested) { //depth-first if nested
				final var found = child.findChild(predicate, true);
				if(found.isPresent()) return found;
			}
		}
		return Optional.empty();
	}

	/**
	 * Attempts to find a sibling {@link Node} that matches a given {@link Predicate}.
	 * @param predicate The {@link Predicate} to test.
	 * @return The first sibling that matches the {@link Predicate}.
	 * @throws NullPointerException If the argument is {@code null}.
	 */
	public final @NotNull Optional<@Nullable N> findSibling(
			@NotNull Predicate<N> predicate) throws NullPointerException
	{
		//not null requirement
		Objects.requireNonNull(predicate);
		//there has to be a parent present
		final @Nullable var parent = getParent();
		if(parent == null) return Optional.empty();
		//iterate sibling and search
		for(final var sibling : parent) {
			if(sibling != this && predicate.test(sibling))
				return Optional.ofNullable(sibling);
		}
		return Optional.empty();
	}
	// ================================================== ==================================================
	//                                        (TODO) List IMPLEMENTATION
	// ================================================== ==================================================
	/**
	 * Returns the child {@link Node} at the specified index.
	 * @param index The index of the child to return.
	 * @return The child {@link Node} at the specified index.
	 * @throws IndexOutOfBoundsException If the index is out of range
	 *         ({@code index < 0 || index >= size()}).
	 */
	public final @Nullable N get(int index) throws IndexOutOfBoundsException {
		synchronized(this.children) {
			//check the index bounds
			if(index < 0 || index >= size())
				throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size());
			//iterate to the index
			int i = 0;
			for(final var child : this) { if(i == index) return child; else i++; }
			//should never reach here
			throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size());
		}
	}

	/**
	 * Returns the index of the first occurrence of the specified element
	 * in this list, or -1 if this list does not contain the element.
	 * @param o The element to search for.
	 * @return The index of the first occurrence of the specified element
	 *         in this list, or -1 if this list does not contain the element.
	 */
	public final int indexOf(Object o) {
		int index = 0;
		for(final var child : this) { if(child == o) return index; else index++; }
		return -1;
	}
	// ================================================== ==================================================
	//                                         Collection IMPLEMENTATION
	// ================================================== ==================================================
	public final @Override int size() { return this.children.size(); }
	public final @Override boolean isEmpty() { return this.children.isEmpty(); }
	public final @Override boolean contains(Object o) { return this.childrenSet.contains(o); }
	public final @Override @NotNull Object @NotNull [] toArray() { return this.children.toArray(); }
	public final @Override @NotNull <T> T @NotNull [] toArray(@NotNull T @NotNull [] a) { return this.children.toArray(a); }
	public final @Override boolean containsAll(@NotNull Collection<?> c) { return this.children.containsAll(c); }
	// --------------------------------------------------
	public final @Override void clear() { for(final var child : this) remove(child); }
	public final @Override boolean addAll(@NotNull Collection<? extends N> c) { boolean out = false; for(final var el : c) { out |= add(el); } return out; }
	public final @Override boolean removeAll(@NotNull Collection<?> c) { boolean out = false; for(final var el : c) { out |= remove(el); } return out; }
	public final @Override boolean retainAll(@NotNull Collection<?> c) { boolean out = false; for(final var el : this) { if(!c.contains(el)) { out |= remove(el); } } return out; }
	// --------------------------------------------------
	public final @Override @NotNull Iterator<N> iterator()
	{
		final var iterator = this.children.iterator();
		return new Iterator<>()
		{
			private volatile N current;
			public final @Override synchronized N next() { return (this.current = iterator.next()); }
			public final @Override synchronized boolean hasNext() { return iterator.hasNext(); }
			public final @Override synchronized void remove()
			{
				@Nullable var current = this.current;
				if(current == null)
					throw new IllegalStateException("next() must be called before remove()");
				Node.this.remove(current);
				this.current = null;
			}
		};
	}

	/**
	 * Similar to {@link #forEach(Consumer)}, but with an additional ability to recursively
	 * apply the {@link Consumer} to all children and grandchildren.
	 * @param action The action to be performed for each element.
	 * @param recursive Whether to recursively call this same method for children as well.
	 * @throws NullPointerException If the specified action is {@code null}.
	 */
	public final void forEach(Consumer<N> action, boolean recursive) throws NullPointerException {
		if(!recursive) { forEach(action); return; } //optimization
		for(final var child: this) { action.accept(child); child.forEach(action, true); }
	}
	// ==================================================
	/**
	 * {@inheritDoc}
	 * @throws NullPointerException {@inheritDoc}
	 * @throws ClassCastException {@inheritDoc}
	 * @throws IllegalArgumentException {@inheritDoc}
	 */
	public final @Override boolean add(@NotNull N child)
			throws NullPointerException, ClassCastException, IllegalArgumentException
	{
		//null nodes are not allowed
		Objects.requireNonNull(child);
		//duplicate nodes are not allowed
		if(contains(child)) return false;
		//check child class type, ensure it matches
		else if(!getBaseType().isAssignableFrom(child.getClass()))
			throw new ClassCastException(
					"Cannot add child node of type " + child.getClass() +
					" because it is not an instance of " + getBaseType());
		//check for scene graph relation violations
		//FIXME - Implement anti-closed-loop of parents
		else if(child == this /*|| child is a grandChild*/)
			throw new IllegalArgumentException("Scene graph violation. Child cannot be 'this' or a grandchild.");

		//remove the child from the last parent, if one is present
		//a node can only have one parent at a time
		final @Nullable var lastParent = child.parent.get();
		if(lastParent != null && !lastParent.remove(child))
			throw new IllegalArgumentException(
					"Cannot add child node (" + child.getClass() +
					") because its current parent (" + lastParent.getClass() +
					") refused to remove it.");

		//add the child, and handle successful addition
		this.children.add(child);
		this.childrenSet.add(child);
		setRootValue(child, this.root.get());
		setParentValue(child, getSelf());
		childAddedCallback(child);
		return true;
	}
	// --------------------------------------------------
	/**
	 * Removes this {@link Node} instance from its parent. If this {@link Node} does
	 * not have a parent, nothing happens.
	 * @return {@code true} if {@link #getParent()} is not {@code null} and parent's
	 *         {@link Node#remove(Node)} operation returned {@code true}.
	 */
	public final boolean remove() {
		final @Nullable var parent = getParent();
		return (parent != null && parent.remove(this));
	}

	@SuppressWarnings("unchecked") //i did in-fact, check it
	public final @Override boolean remove(Object child) {
		return (child == null || getBaseType().isAssignableFrom(child.getClass())) && remove((N) child);
	}

	/**
	 * Same as {@link #remove(Object)}.<br>
	 * Removes a child {@link Node} instance from this {@link Node}.
	 * @param child The {@link Node} to be removed.
	 * @return {@code true} if a node was removed as a result of this call.
	 */
	public final boolean remove(N child)
	{
		//remove the child, return and do nothing else if nothing changed
		if(!this.children.remove(child) || child == null) //null check is 2nd - intentional
			return false;
		this.childrenSet.remove(child);

		//handle successful removal
		setRootValue(child, child.getSelf()); //cannot be null
		setParentValue(child, null);
		childRemovedCallback(child);
		return true;
	}
	// ================================================== ==================================================
	//                                           PROPERTY ACCESSOR
	// ================================================== ==================================================
	/**
	 * An internal {@link Class} whose sole purpose is to take ownership of certain
	 * {@link ObjectProperty}s defined in {@link Node}.
	 * <p>
	 * The reason for this is that as per {@link ObjectProperty} specification,
	 * subclasses are also considered "owners" of properties defined in superclasses.
	 * <p>
	 * In this case, that is not desired, as that would prevent "interceptors" from
	 * functioning in subclasses. In addition, setting the values of properties in
	 * {@link Node} is meant to only be done by internal logic.
	 * <p>
	 * To resolve this issue, ownership is transferred to this internal {@link Class}.
	 * @see ObjectProperty#getOwner()
	 * @see ObjectProperty#isOwner(Class)
	 * @see ObjectProperty#setOwner(Class, Class)
	 * @see ObjectProperty#setInterceptor(IChangeListener, Class)
	 */
	static final @ApiStatus.Internal class NodePropertyAccessor
	{
		private NodePropertyAccessor() {}
		static <E extends Node<E>> void setRootValue(Node<E> self, @Nullable E parent) { self.root.set(parent, NodePropertyAccessor.class); }
		static <E extends Node<E>> void setParentValue(Node<E> self, @Nullable E parent) { self.parent.set(parent, NodePropertyAccessor.class); }
	}
	// ================================================== ==================================================
}
