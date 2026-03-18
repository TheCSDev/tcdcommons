package com.thecsdev.commonmc.api.client.gui.screen.promise;

import com.thecsdev.commonmc.api.client.gui.screen.promise.TFileChooserScreen.Mode;
import com.thecsdev.commonmc.api.client.gui.screen.promise.TFileChooserScreen.PathFilter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Controller class for the {@link TFileChooserScreen}, responsible for managing
 * the state and behavior of the file chooser interface.
 * <p>
 * This class handles navigation, directory management, and interaction with the
 * file system, providing a structured way to control the file chooser's functionality.
 */
@Environment(EnvType.CLIENT)
final @ApiStatus.Internal class TFileChooserController
{
	// ==================================================
	private final @NotNull Mode             mode;
	private final @NotNull List<PathFilter> pathFilters;
	private       @NotNull PathFilter       pathFilter;
	// --------------------------------------------------
	private       @NotNull Path        dir;
	private final @NotNull Deque<Path> backStack;
	private final @NotNull Deque<Path> forwardStack;
	// --------------------------------------------------
	private long _editCount = Long.MIN_VALUE;
	// ==================================================
	public TFileChooserController(
			@NotNull Mode mode,
			@NotNull Path dir,
			@NotNull List<PathFilter> pathFilters) throws NullPointerException
	{
		this.mode         = Objects.requireNonNull(mode);
		this.pathFilters  = Objects.requireNonNull(pathFilters);
		this.pathFilter   = pathFilters.isEmpty() ? PathFilter.ALL : pathFilters.getFirst();
		this.dir          = Objects.requireNonNull(dir);
		this.backStack    = new LinkedList<>();
		this.forwardStack = new LinkedList<>();
	}
	// ==================================================
	/**
	 * Returns the total number of changes made to this {@link TFileChooserController}.
	 * <p>
	 * This can be used to track changes and determine if this controller's state
	 * has been modified since it was last checked.
	 */
	public final long getEditCount() { return this._editCount; }

	/**
	 * Increments the {@link #getEditCount()} value.
	 * <p>
	 * This method is automatically invoked whenever a chance occurs within
	 * this {@link TFileChooserController}.
	 */
	public final void addEditCount() { this._editCount++; }
	// --------------------------------------------------
	public final @NotNull Mode getMode() { return this.mode; }
	public final @NotNull List<PathFilter> getFilters() { return this.pathFilters; }
	public final @NotNull PathFilter getFilter() { return this.pathFilter; }
	public final void setPathFilter(@NotNull PathFilter pathFilter) throws NullPointerException {
		this.pathFilter = Objects.requireNonNull(pathFilter);
		addEditCount();
	}
	public final @NotNull Path getDirectory() { return this.dir; }
	// --------------------------------------------------
	/**
	 * Returns {@code true} if {@link #getDirectory()} is a root directory
	 * (i.e., it has no parent and is an absolute path).
	 */
	public final boolean isDirectoryRoot() {
		return this.dir.getParent() == null && this.dir.isAbsolute();
	}
	// ==================================================
	/**
	 * Navigates to the specified directory, updating the current directory
	 * and refreshing the screen to display its contents.
	 * @param directory The target directory {@link Path}.
	 * @throws NullPointerException If the argument is {@code null}.
	 */
	public final void navigateTo(@NotNull Path directory) throws NullPointerException
	{
		//push current dir to back history, then change current dir
		this.backStack.push(this.dir);
		this.dir = Objects.requireNonNull(directory);
		//enforce max size of 32 on the backward history
		while(this.backStack.size() > 32)
			this.backStack.removeLast();
		//clear forward history and refresh
		this.forwardStack.clear();
		addEditCount();
	}

	/**
	 * Navigates back to the previous directory in the navigation history.
	 */
	public final boolean navigateBack()
	{
		//if there's no back history, we cannot navigate
		if(this.backStack.isEmpty()) return false;
		//push current dir to forward history, then change current dir
		this.forwardStack.push(this.dir);
		this.dir = this.backStack.pop();
		//enforce max size of 32 on the forward history
		while(this.forwardStack.size() > 32)
			this.forwardStack.removeLast();
		//refresh and return
		addEditCount();
		return true;
	}

	/**
	 * Navigates forward to the next directory in the navigation history.
	 */
	public final boolean navigateForward()
	{
		//if there's no forward history, we cannot navigate
		if(this.forwardStack.isEmpty()) return false;
		//push current dir to back history, then change current dir
		this.backStack.push(this.dir);
		this.dir = this.forwardStack.pop();
		//enforce max size of 32 on the backward history
		while(this.backStack.size() > 32)
			this.backStack.removeLast();
		//refresh and return
		addEditCount();
		return true;
	}
	// ==================================================
}
