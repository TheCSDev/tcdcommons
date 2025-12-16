package com.thecsdev.common.properties;

/**
 * A change listener is notified when the value of an {@link ObjectProperty} changes.
 */
@FunctionalInterface
public interface IChangeListener<T>
{
	/**
	 * Called when the value of a given {@link ObjectProperty} changes.
	 * @param property The {@link ObjectProperty} that is responsible for the change.
	 *                 Can be one of multiple different {@link ObjectProperty}s if
	 *                 they share the same {@link ValueHandle}. <u><b>SO BE CAREFUL!</b></u>
	 * @param oldValue The old value of the {@link ObjectProperty}.
	 * @param newValue The new value of the {@link ObjectProperty}.
	 * @apiNote Do NOT update the value the {@link ObjectProperty} from within
	 * an {@link IChangeListener}. Doing so will result in a {@link StackOverflowError}.
	 */
	public void apply(ObjectProperty<T> property, T oldValue, T newValue);
}
