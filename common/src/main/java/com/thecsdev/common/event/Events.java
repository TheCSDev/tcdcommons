package com.thecsdev.common.event;

import com.google.common.reflect.AbstractInvocationHandler;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Objects;

import static java.lang.reflect.Proxy.newProxyInstance;

/**
 * Factory for creating {@link Event} instances.
 */
public final class Events
{
	// ==================================================
	private Events() {}
	// ==================================================
	/**
	 * Invokes the specified method on the specified listener with the specified arguments.
	 * @param listener The listener to invoke the method on.
	 * @param method The method to invoke.
	 * @param args The arguments to pass to the method.
	 * @param <L> Listener type.
	 * @param <R> Method return type.
	 * @return The result of the method invocation, or {@code null} if the method has a void return type.
	 * @throws NullPointerException If an argument is {@code null}.
	 * @throws Throwable If the method invocation throws an exception.
	 */
	@SuppressWarnings("unchecked")
	static <L, R> R invokeMethod(@NotNull L listener, @NotNull Method method, @NotNull Object[] args) throws Throwable {
		return (R) MethodHandles.lookup().unreflect(method).bindTo(listener).invokeWithArguments(args);
	}
	// ==================================================
	/**
	 * Creates a new {@link Event} instance with the specified listener type.
	 * @param typeGetter An empty array of the listener type, used to infer the listener type.
	 * @param <L> Listener type.
	 * @return A new {@link Event} instance with the specified listener type.
	 * @throws NullPointerException If the argument is {@code null}.
	 * @throws IllegalArgumentException If the argument is not an empty array.
	 */
	@SafeVarargs
	@SuppressWarnings("unchecked")
	public static final @NotNull <L> Event<L> createLoop(@NotNull L... typeGetter) {
		if(Objects.requireNonNull(typeGetter).length != 0)
			throw new IllegalArgumentException("The argument must be an empty array.");
		return createLoop((Class<L>) typeGetter.getClass().getComponentType());
	}

	/**
	 * Creates a new {@link Event} instance with the specified listener type.
	 * @param listenerType The class of the listener type.
	 * @param <L> Listener type.
	 * @return A new {@link Event} instance with the specified listener type.
	 * @throws NullPointerException If the argument is {@code null}.
	 */
	@SuppressWarnings("unchecked")
	public static final @NotNull <L> Event<L> createLoop(@NotNull Class<L> listenerType) {
		return new Event.Impl<>(listeners -> (L) newProxyInstance(
				Events.class.getClassLoader(),
				new Class[] { listenerType },
				new AbstractInvocationHandler() {
					protected final @Override @Nullable Object handleInvocation(
							@NonNull Object proxy, @NonNull Method method, Object @NonNull [] args)
							throws Throwable
					{
						for(final var listener : listeners)
							invokeMethod(listener, method, args);
						return null;
					}
				}));
	}
	// ==================================================
}
