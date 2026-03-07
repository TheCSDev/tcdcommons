package com.thecsdev.commonmc.api.client.gui.screen.promise;

import com.thecsdev.common.properties.BooleanProperty;
import com.thecsdev.common.util.annotations.Virtual;
import com.thecsdev.commonmc.api.client.gui.screen.ILastScreenProvider;
import com.thecsdev.commonmc.api.client.gui.screen.TScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/**
 * A {@link TScreen} that provides a {@link CompletableFuture} that yields a result
 * when the screen is closed. This can be used for screens that require user input
 * or choices, allowing the caller to await the result asynchronously.
 * <p>
 * If the screen is closed prematurely without a result, the future is canceled.
 *
 * @see #getResult()
 * @param <R> The type of the result that will be returned when the screen is closed.
 */
@Environment(EnvType.CLIENT)
public abstract class TCompletableScreen<R> extends TScreen implements ILastScreenProvider
{
	// ================================================== ==================================================
	//                                 TCompletableScreen IMPLEMENTATION
	// ================================================== ==================================================
	private final Screen          lastScreen;
	private final Promise         result;
	private final BooleanProperty canCompleteAsync;
	// ==================================================
	protected TCompletableScreen() { this(null); }
	protected TCompletableScreen(@Nullable Screen lastScreen) {
		this.lastScreen       = lastScreen;
		this.result           = new Promise();
		this.canCompleteAsync = new BooleanProperty(false);
	}
	// ==================================================
	/**
	 * Returns a future that will be completed when this {@link TScreen} is closed.
	 * The future will contain the result of the screen, which can be used to perform
	 * actions based on the user's input or choices made within the screen.
	 */
	public final @NotNull CompletableFuture<R> getResult() { return result; }
	// --------------------------------------------------
	/**
	 * {@link BooleanProperty} that indicates whether the result of this screen can
	 * be completed asynchronously. If this property is set to {@code true}, the
	 * result can be completed from any thread without scheduling on the main thread.
	 * If set to {@code false}, completion actions will be scheduled on the client's
	 * main thread to ensure thread safety.
	 */
	protected final BooleanProperty canCompleteAsync() { return this.canCompleteAsync; }
	// ==================================================
	public final @Override @Nullable Screen getLastScreen() { return this.lastScreen; }
	// --------------------------------------------------
	/**
	 * {@inheritDoc}<p>
	 * This method automatically cancels {@link #getResult()} if not already completed.
	 * Overrides of this method should call {@code super} to preserve this behavior.
	 */
	protected @Virtual @Override void closeCallback() {
		if(!this.result.isDone()) result.cancel(true);
	}
	// ================================================== ==================================================
	//                                            Promise IMPLEMENTATION
	// ================================================== ==================================================
	/**
	 * A custom implementation of {@link CompletableFuture} that ensures that completion
	 * actions are executed on the client's main thread, and that the future can only be
	 * completed once.
	 * <p>
	 * This also ensures completion stages do not execute immediately after completion
	 * takes place.
	 */
	private final class Promise extends CompletableFuture<R>
	{
		// ==================================================
		private volatile boolean completed = false;
		// ==================================================
		public final @Override synchronized boolean complete(R value)
		{
			if(isDone() || this.completed) return false; else this.completed = true;
			if(TCompletableScreen.this.canCompleteAsync.getZ()) {
				return super.complete(value);
			} else {
				getClient().schedule(() -> super.complete(value));
				return true;
			}
		}
		public final @Override synchronized boolean completeExceptionally(Throwable ex)
		{
			if(isDone() || this.completed) return false; else this.completed = true;
			if(TCompletableScreen.this.canCompleteAsync.getZ()) {
				return super.completeExceptionally(ex);
			} else {
				getClient().schedule(() -> super.completeExceptionally(ex));
				return true;
			}
		}
		public final @Override synchronized boolean cancel(boolean mayInterruptIfRunning)
		{
			if(isDone() || this.completed) return false; else this.completed = true;
			if(TCompletableScreen.this.canCompleteAsync.getZ()) {
				return super.cancel(mayInterruptIfRunning);
			} else {
				getClient().schedule(() -> super.cancel(mayInterruptIfRunning));
				return true;
			}
		}
		// ==================================================
		public final @Override void obtrudeValue(R value)
		{
			if(TCompletableScreen.this.canCompleteAsync.getZ())
				super.obtrudeValue(value);
			else getClient().schedule(() -> super.obtrudeValue(value));
		}
		public final @Override void obtrudeException(Throwable ex)
		{
			if(TCompletableScreen.this.canCompleteAsync.getZ())
				super.obtrudeException(ex);
			else getClient().schedule(() -> super.obtrudeException(ex));
		}
		// ==================================================
	}
	// ================================================== ==================================================
}
