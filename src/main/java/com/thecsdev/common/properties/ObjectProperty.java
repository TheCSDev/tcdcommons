package com.thecsdev.common.properties;

import com.thecsdev.common.util.TUtils;
import com.thecsdev.common.util.annotations.CallerSensitive;
import com.thecsdev.common.util.annotations.Virtual;
import io.netty.util.internal.UnstableApi;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * A property wrapper for an {@link Object} of type {@code T}, similar to
 * {@link AtomicReference}, but with additional utility features.<br>
 * Inspired by JavaFX's property system.
 *
 * @param <T> The type of the stored value.
 */
public @Virtual class ObjectProperty<T>
{
	// ==================================================
	private volatile Class<?>           owner;
	private volatile boolean            readOnly = false;
	private volatile ValueHandle<T>     handle   = new ValueHandle<>(null);
	// --------------------------------------------------
	private final     ConcurrentLinkedDeque<Function<T, T>> filters     = new ConcurrentLinkedDeque<>();
	private volatile  @Nullable IChangeListener<T>          interceptor = null;
	// --------------------------------------------------
	/**
	 * Holds {@link IChangeListener}s that are to be invoked whenever
	 * {@link ObjectProperty#set(Object, Class)} is called.<br>
	 * <br>
	 * Note that change listeners do not invoke on {@link ValueHandle#set(Object)},
	 * as the purpose of a {@link ValueHandle} is to grant raw access to the value
	 * of a given {@link ObjectProperty}.
	 */
	@ApiStatus.Internal
	private final ConcurrentLinkedDeque<IChangeListener<T>> changeListeners = new ConcurrentLinkedDeque<>();
	// ==================================================
	public ObjectProperty() { this(null); }
	public ObjectProperty(@Nullable T value) { this.handle.set(value); }
	// ==================================================
	/**
	 * Returns the {@link Class} that owns this {@link ObjectProperty}.
	 * @apiNote This is likely the {@link Class} that created this instance.
	 */
	public final Class<?> getOwner() { return this.owner; }

	/**
	 * Assigns a new owner {@link Class} for this {@link ObjectProperty}.
	 * @param owner The new owner.
	 * @param whoIsAsking The {@link Class} calling this method. It is <b>CRITICAL</b> not to lie here.
	 * @throws NullPointerException If a {@link NotNull} argument is {@code null}.
	 * @throws IllegalCallerException See {@link #assertCallerIsOwner(Class)}.
	 */
	@CallerSensitive
	public final synchronized void setOwner(@Nullable Class<?> owner, @NotNull Class<?> whoIsAsking) throws NullPointerException, IllegalCallerException {
		if(this.owner != null) assertCallerIsOwner(Objects.requireNonNull(whoIsAsking));
		this.owner = owner;
	}

	/**
	 * Returns {@code true} if a given {@link Class} is considered an owner of this
	 * {@link ObjectProperty}. Those are:
	 * <ul>
	 *     <li>{@link #getOwner()}</li>
	 *     <li>{@code #getOwner().isAssignableFrom(caller)}</li>
	 *     <li>{@code ObjectProperty.class.isAssignableFrom(caller)}</li>
	 * </ul>
	 * Any other {@link Class} will return {@code false}.
	 * @throws NullPointerException If the argument is {@code null}.
	 * @apiNote As a tip, remember that extending a {@link Class} that has declared
	 * {@link ObjectProperty}s means that you too (the subclass) now owns said properties as well.
	 */
	public final boolean isOwner(@NotNull Class<?> caller) throws NullPointerException
	{
		Objects.requireNonNull(caller);
		if(this.owner == null) return true;
		return (caller == this.owner ||
				this.owner.isAssignableFrom(caller) ||
				ObjectProperty.class.isAssignableFrom(caller));
	}

	/**
	 * Ensures that a given {@link Class} is an owner using {@link #isOwner(Class)}.
	 * If said class is not an owner, {@link IllegalCallerException} is thrown.
	 */
	protected final void assertCallerIsOwner(@NotNull Class<?> caller) throws IllegalCallerException {
		if(isOwner(caller)) return;
		throw new IllegalCallerException("Operation is only permitted from the owning class: " + owner.getName());
	}
	// --------------------------------------------------
	/**
	 * Returns the internal {@link AtomicReference} backing this property.
	 * <p>
	 * This gives direct access to the underlying value, bypassing all
	 * access control such as {@link #getOwner()} and {@link #getReadOnly()}.
	 *
	 * @return The atomic reference holding the actual value
	 * @apiNote Use with caution. This provides low-level access and can break
	 * property invariants if misused.
	 */
	public final ValueHandle<T> getHandle() { return this.handle; }

	/**
	 * Replaces the internal {@link AtomicReference} backing this property.
	 * <p>
	 * This completely overrides the current value holder, bypassing all
	 * checks including {@link #getOwner()} and {@link #getReadOnly()}.
	 *
	 * @param newHandle The new {@link ValueHandle} to use
	 * @throws NullPointerException if {@code handle} is {@code null}
	 * @apiNote Use with caution. This provides low-level access and can break
	 * property invariants if misused.<br>
	 * In addition, {@link IChangeListener}s are bound to {@link ValueHandle}s! (for now)
	 */
	@UnstableApi //discouraged. still not sure how change listeners should handle this, espetially when properties share the same handle
	public final synchronized void setHandle(@NotNull ValueHandle<T> newHandle) throws NullPointerException
	{
		//assert arguments not null
		Objects.requireNonNull(newHandle);

		//optimization by skipping equal handles
		if(this.handle == newHandle) return; //(why would anyone do this anyway...)

		//get the old value, replace the handle, and invoke the change listeners
		final @Nullable T oldValue = this.handle.get();
		final @Nullable T newValue = newHandle.get();

		this.handle = newHandle;

		if(!Objects.equals(oldValue, newValue)) //optimization
			invokeChangeListeners(oldValue, newValue);
	}
	// --------------------------------------------------
	/**
	 * Returns {@code true} if this {@link ObjectProperty} is read-only and cannot
	 * be {@link #set(Object)}.
	 * @apiNote The owner of this {@link ObjectProperty} bypasses {@link #readOnly}
	 * when calling {@link #set(Object)}.
	 * <p>
	 * In addition, read-only prevents {@link #getInterceptor(Class)} from working.
	 */
	public final boolean getReadOnly() { return this.readOnly; }

	/**
	 * Sets the {@link #getReadOnly()} state.
	 * @param readOnly The new readonly state.
	 * @param whoIsAsking The {@link Class} calling this method. It is <b>CRITICAL</b> not to lie here.
	 * @throws IllegalCallerException See {@link #assertCallerIsOwner(Class)}.
	 * @apiNote Will prevent {@link #getInterceptor(Class)} from working if set to {@code true}.
	 */
	@CallerSensitive
	public final synchronized void setReadOnly(boolean readOnly, @NotNull Class<?> whoIsAsking) throws IllegalCallerException {
		if(this.owner != null) assertCallerIsOwner(Objects.requireNonNull(whoIsAsking));
		this.readOnly = readOnly;
	}
	// --------------------------------------------------
	/**
	 * Returns the interceptor instance that intercepts {@link #set(Object)}
	 * calls by {@link Class}es that do not own this {@link ObjectProperty}.
	 * @param whoIsAsking The {@link Class} calling this method. It is <b>CRITICAL</b> not to lie here.
	 * @throws IllegalCallerException See {@link #assertCallerIsOwner(Class)}.
	 */
	@CallerSensitive
	public final @Nullable IChangeListener<T> getInterceptor(@NotNull Class<?> whoIsAsking) throws IllegalCallerException {
		if(this.owner != null) assertCallerIsOwner(Objects.requireNonNull(whoIsAsking));
		return this.interceptor;
	}

	/**
	 * Assigns an interceptor instance for this {@link ObjectProperty}.<br>
	 * Please see {@link #getInterceptor(Class)} for more info.
	 * @param interceptor The new interceptor instance.
	 * @param whoIsAsking The {@link Class} calling this method. It is <b>CRITICAL</b> not to lie here.
	 * @throws IllegalCallerException See {@link #assertCallerIsOwner(Class)}.
	 */
	@CallerSensitive
	public final void setInterceptor(@Nullable IChangeListener<T> interceptor, @NotNull Class<?> whoIsAsking) throws IllegalCallerException {
		if(this.owner != null) assertCallerIsOwner(Objects.requireNonNull(whoIsAsking));
		this.interceptor = interceptor;
	}
	// --------------------------------------------------
	/**
	 * Returns the current value of this {@link ObjectProperty}.
	 * @apiNote {@link Override}s should use the value in {@link #getHandle()}.
	 */
	public @Virtual @Nullable T get() { return this.handle.get(); }

	/**
	 * Returns the current value of this {@link ObjectProperty} wrapped in an {@link Optional}.
	 */
	public @Virtual @NotNull Optional<@Nullable T> getOptional() { return Optional.ofNullable(get()); }

	/**
	 * Sets the current value of this {@link ObjectProperty}.
	 * @deprecated Because this uses stack walking to enforce {@link CallerSensitive}, this
	 *             method causes performance bottlenecks. Avoid this one.
	 * @param value The new value.
	 * @throws IllegalCallerException If {@link #getReadOnly()}, and the caller
	 * is not an owner. See {@link #assertCallerIsOwner(Class)}.
	 * @see #set(Object, Class)
	 */
	@Deprecated
	@CallerSensitive
	//NOTE - JIT compiler forced me to duplicate #set(...) logic - Remove this method?
	public final synchronized void set(@Nullable T value) throws IllegalCallerException
	{
		//invoke the filters that override the value
		value = applyFilters(value);

		//get the old value, and compare it to the value being set
		final @Nullable T oldValue = this.handle.get();
		if(Objects.equals(oldValue, value))
			return; //cancel if match - OPTIMIZATION AND CLOSED-LOOP/STACK-OVERFLOW PREVENTION!

		//handle interceptors and read-only state
		final boolean callerNotOwner = (this.owner != null && !isOwner(TUtils.getStackWalkerRCR().getCallerClass()));
		if(callerNotOwner) //note: owners bypass read-only state and interceptors
		{
			//yes, interceptors are unreachable as well, when read-only
			if(this.readOnly)
				throw new IllegalCallerException(
					"Attempt to call 'set(...)' on a read-only object property. " +
					"Note that owner callers get to bypass this rule."
				);

			//if an interceptor is present
			else if(this.interceptor != null) {
				this.interceptor.apply(this, oldValue, value);
				return;
			}
		}

		//set the value and invoke the change listeners
		this.handle.set(value);
		invokeChangeListeners(oldValue, value);
	}

	/**
	 * Sets the current value of this {@link ObjectProperty}.
	 * @param value The new value.
	 * @param whoIsAsking The {@link Class} calling this method. It is <b>CRITICAL</b> not to lie here.
	 * @throws IllegalCallerException If {@link #getReadOnly()}, and the caller
	 * is not an owner. See {@link #assertCallerIsOwner(Class)}.
	 */
	@CallerSensitive
	public final synchronized void set(@Nullable T value, @NotNull Class<?> whoIsAsking) throws IllegalCallerException
	{
		//invoke the filters that override the value
		value = applyFilters(value);

		//get the old value, and compare it to the value being set
		final @Nullable T oldValue = this.handle.get();
		if(Objects.equals(oldValue, value))
			return; //cancel if match - OPTIMIZATION AND CLOSED-LOOP/STACK-OVERFLOW PREVENTION!

		//handle interceptors and read-only state
		final boolean callerNotOwner = (this.owner != null && !isOwner(whoIsAsking));
		if(callerNotOwner) //note: owners bypass read-only state and interceptors
		{
			//yes, interceptors are unreachable as well, when read-only
			if(this.readOnly)
				throw new IllegalCallerException(
					"Attempt to call 'set(...)' on a read-only object property. " +
					"Note that owner callers get to bypass this rule."
				);

			//if an interceptor is present
			else if(this.interceptor != null) {
				this.interceptor.apply(this, oldValue, value);
				return;
			}
		}

		//set the value and invoke the change listeners
		this.handle.set(value);
		invokeChangeListeners(oldValue, value);
	}
	// ==================================================
	/**
	 * Adds a filter {@link Function} to the list of filters.
	 * @param filter The filter to add.
	 * @param whoIsAsking The {@link Class} calling this method. It is <b>CRITICAL</b> not to lie here.
	 * @throws IllegalCallerException See {@link #assertCallerIsOwner(Class)}.
	 */
	@CallerSensitive
	public final boolean addFilter(Function<T, T> filter, @NotNull Class<?> whoIsAsking) throws IllegalCallerException {
		if(this.owner != null) assertCallerIsOwner(Objects.requireNonNull(whoIsAsking));
		synchronized(this.filters) {
			if(this.filters.contains(filter)) return false;
			else return this.filters.add(filter);
		}
	}

	/**
	 * Removes a filter {@link Function} from the list of filters.
	 * @param filter The filter to remove.
	 * @return A boolean indicating if the {@link Function} was present prior to its removal.
	 */
	public final boolean removeFilter(Object filter) {
		//intentionally not asserting ownership - if you have the reference to
		//a filter, then you likely were given permission to use and remove it
		synchronized(this.filters) { return this.filters.remove(filter); }
	}

	/**
	 * Applies filters to a given value. This is generally used in {@link #set(Object, Class)}
	 * @see #addFilter(Function, Class)
	 * @see #removeFilter(Object)
	 */
	public final T applyFilters(T newValue) {
		synchronized(this.filters) {
			if(this.filters.isEmpty()) return newValue;
			for(final var filter : this.filters)
				newValue = filter.apply(newValue);
		}
		return newValue;
	}
	// --------------------------------------------------
	/**
	 * Adds an {@link IChangeListener} to the list of change listeners.
	 * @param changeListener The {@link IChangeListener} to add.
	 * @throws NullPointerException If the argument is {@code null}.
	 * @apiNote <b>IMPORTANT:</b> {@link IChangeListener}s are bound to {@link ValueHandle}s!
	 * Using {@link #setHandle(ValueHandle)} changes the {@link IChangeListener}s as well!
	 */
	public final boolean addChangeListener(@NotNull IChangeListener<T> changeListener) throws NullPointerException {
		Objects.requireNonNull(changeListener);
		synchronized(this.changeListeners) {
			if(this.changeListeners.contains(changeListener)) return false;
			else return this.changeListeners.add(Objects.requireNonNull(changeListener));
		}
	}

	/**
	 * Removes an {@link IChangeListener} from the list of change listeners.
	 * @param changeListener The {@link IChangeListener} to remove.
	 * @return A boolean indicating if the {@link IChangeListener} was present prior to its removal.
	 */
	public final boolean removeChangeListener(Object changeListener) {
		synchronized(this.changeListeners) { return this.changeListeners.remove(changeListener); }
	}

	/**
	 * Invokes all {@link IChangeListener}s in the list of change listeners.
	 * @param oldValue The old value.
	 * @param newValue The new value.
	 */
	@ApiStatus.Internal
	private final void invokeChangeListeners(T oldValue, T newValue)
	{
		//synchronize the change listeners and prepare for iteration
		synchronized(this.changeListeners)
		{
			//optimization
			if(this.changeListeners.isEmpty()) return;

			//this is where any thrown exceptions will be stored before
			//being bundled in a single throw
			@Nullable List<Throwable> exceptions = null;

			//iterate change listeners and execute them sequentially
			for(final var changeListener : this.changeListeners)
				try { changeListener.apply(this, oldValue, newValue); }
				catch(Exception e) {
					if(exceptions == null) exceptions = new LinkedList<>();
					exceptions.add(e);
				}

			//throw if exceptions occurred during change listener executions
			if(exceptions != null) {
				final var re = new RuntimeException("Throwables were thrown during execution of change listeners");
				for(final var suppressed : exceptions)
					re.addSuppressed(suppressed);
				throw re;
			}
		}
	}
	// ==================================================
}
