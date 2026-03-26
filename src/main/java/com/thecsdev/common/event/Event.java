package com.thecsdev.common.event;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * Represents an event that can be fired to invoke registered listeners.
 * @param <L> Event listener type.
 */
public interface Event<L>
{
	// ================================================== ==================================================
	//                                              Event IMPLEMENTATION
	// ================================================== ==================================================
	/**
	 * Returns the invoker of this {@link Event}, which is used to fire this {@link Event}
	 * and invoke all registered listeners.
	 */
	public @NotNull L invoker();
	// ==================================================
	/**
	 * Adds a listener to this {@link Event}, that is to be invoked when this {@link Event}
	 * is fired.
	 * @param listener The listener that will be invoked when this {@link Event} is fired.
	 * @return {@code false} if the listener was already registered, {@code true} otherwise.
	 * @throws NullPointerException If the argument is {@code null}.
	 */
	public boolean addListener(@NotNull L listener) throws NullPointerException;

	/**
	 * Removes a listener from this {@link Event}, so that it will no longer be invoked when
	 * this {@link Event} is fired.
	 * @param listener The listener to be removed.
	 * @return {@code true} if the listener was registered before removal, {@code false} otherwise.
	 * @throws NullPointerException If the argument is {@code null}.
	 */
	public boolean removeListener(@NotNull L listener) throws NullPointerException;

	/**
	 * Removes all listeners from this {@link Event}, so that no listeners will be invoked when
	 * this {@link Event} is fired.
	 */
	public void clearListeners();
	// --------------------------------------------------
	/**
	 * Checks if a listener is added to this {@link Event}.
	 * @param listener The listener to check for.
	 * @return {@code true} if the listener is added, {@code false} otherwise.
	 * @throws NullPointerException If the argument is {@code null}.
	 */
	public boolean containsListener(@NotNull L listener) throws NullPointerException;
	// ================================================== ==================================================
	//                                               Impl IMPLEMENTATION
	// ================================================== ==================================================
	/**
	 * Main {@link Event} implementation.
	 * @param <L> Event listener type.
	 */
	static final class Impl<L> implements Event<L>
	{
		// ==================================================
		private final @NotNull  ArrayList<L>         listeners  = new ArrayList<>();
		// --------------------------------------------------
		private       @Nullable L                    invoker    = null; //null = "dirty"
		private final @NotNull  Function<List<L>, L> mkInvoker;         //invoker factory
		// ==================================================
		public Impl(@NotNull Function<List<L>, L> mkInvoker) throws NullPointerException {
			this.mkInvoker = requireNonNull(mkInvoker);
		}
		// ==================================================
		public final @Override @NonNull L invoker() {
			if(this.invoker == null) refreshInvoker();
			return requireNonNull(this.invoker, "Missing 'Event' invoker");
		}
		// --------------------------------------------------
		public final @Override boolean addListener(@NonNull L listener) throws NullPointerException {
			//cannot add listener if already added
			if(this.listeners.contains(requireNonNull(listener))) return false;
			//clear the invoker and add the listener
			final var result = this.listeners.add(listener);
			if(result) this.invoker = null;
			return result;
		}

		public final @Override boolean removeListener(@NonNull L listener) throws NullPointerException {
			//cannot remove listener if not already added
			if(!this.listeners.contains(requireNonNull(listener))) return false;
			//clear the invoker and remove the listener
			final var result = this.listeners.remove(listener);
			if(result) {
				this.listeners.trimToSize();
				this.invoker = null;
			}
			return result;
		}

		public final @Override void clearListeners() {
			this.listeners.clear();
			this.listeners.trimToSize();
			this.invoker = null;
		}
		// --------------------------------------------------
		public final @Override boolean containsListener(@NonNull L listener) throws NullPointerException {
			return this.listeners.contains(requireNonNull(listener));
		}
		// ==================================================
		/**
		 * Refreshes the invoker of this {@link Event} based on the current listeners. If
		 * there is only one listener, the invoker is set to that listener. Otherwise,
		 * the invoker is created using the provided {@code mkInvoker} function, which
		 * takes the list of listeners as input and returns an invoker that can invoke
		 * all listeners when the event is fired.
		 */
		private void refreshInvoker() {
			this.invoker = (this.listeners.size() == 1) ?
					this.listeners.getFirst() :
					requireNonNull(this.mkInvoker.apply(this.listeners), "'Event' invoker factory returned 'null'");
		}
		// ==================================================
	}
	// ================================================== ==================================================
}
