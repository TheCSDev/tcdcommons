package com.thecsdev.commonmc.api.client.gui.screen.promise;

import com.thecsdev.common.math.UDim2;
import com.thecsdev.commonmc.api.client.gui.TElement;
import com.thecsdev.commonmc.api.client.gui.panel.explorer.TFileExplorerPanel;
import com.thecsdev.commonmc.api.client.gui.panel.explorer.TFileExplorerPanel.PathFilter;
import com.thecsdev.commonmc.api.client.gui.panel.window.TWindowElement;
import com.thecsdev.commonmc.api.client.gui.render.TGuiGraphics;
import com.thecsdev.commonmc.api.client.gui.screen.ILastScreenProvider;
import com.thecsdev.commonmc.api.client.gui.screen.TScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

import static com.thecsdev.commonmc.resource.TComponent.gui;
import static com.thecsdev.commonmc.resource.TLanguage.*;
import static com.thecsdev.commonmc.resource.TSprites.gui_icon_fsFolder;

/**
 * {@link TScreen} implementation that provides a user-friendly interface for selecting
 * files from the device's file-system. This screen is particularly useful for cases that
 * require file opening and saving functionalities, allowing users to easily navigate
 * through their directories and choose the desired files.
 */
@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public final class TFileChooserScreen extends TCompletableScreen<Path> implements ILastScreenProvider
{
	// ================================================== ==================================================
	//                                 TFileChooserScreen IMPLEMENTATION
	// ================================================== ==================================================
	private final @NotNull Mode             mode;
	private final @NotNull List<PathFilter> pathFilters;
	// --------------------------------------------------
	private       @Nullable PathFilter  currFilter;
	private       @NotNull  Path        history_dir;
	private final @NotNull  Deque<Path> history_back;
	private final @NotNull  Deque<Path> history_fwd;
	// ==================================================
	private TFileChooserScreen(
			@Nullable Screen lastScreen,
			@NotNull Mode mode,
			@NotNull List<PathFilter> pathFilters,
			@NotNull Path currentDir) throws NullPointerException
	{
		super(lastScreen);
		titleProperty().set(Objects.requireNonNull(mode).getWindowTitle(), TFileChooserScreen.class);
		this.mode         = mode;
		this.pathFilters  = List.copyOf(Objects.requireNonNull(pathFilters));
		this.history_dir  = Objects.requireNonNull(currentDir);
		this.history_back = new LinkedList<>();
		this.history_fwd  = new LinkedList<>();
	}
	// ==================================================
	/**
	 * Returns the operational {@link Mode} of this {@link TFileChooserScreen}.
	 */
	public final @NotNull Mode getMode() { return this.mode; }
	// ==================================================
	public final @Override void renderCallback(@NotNull TGuiGraphics pencil)
	{
		if(getLastScreen() == null) return;
		//the last screen is to be visually rendered below this screen
		getLastScreen().render(pencil.getNative(), pencil.getMouseX(), pencil.getMouseY(), pencil.getDeltaTicks());
		//followed by a white plane background so it's easier to tell screens apart
		final var bb = getBounds();
		pencil.fillColor(bb.x, bb.y, bb.width, bb.height, 0x22FFFFFF);
	}
	// --------------------------------------------------
	protected final @Override void initCallback()
	{
		//do not initialize a gui or even use this screen if this file chooser was used before
		if(getResult().isDone()) { close(); return; }
		//create and add the window element
		final var wnd = new WindowElement();
		add(wnd);
		wnd.setBounds(new UDim2(0.1, 0, 0.1, 0), new UDim2(0.8, 0, 0.8, 0));
	}
	// ================================================== ==================================================
	//                                               Mode IMPLEMENTATION
	// ================================================== ==================================================
	/**
	 * Defines the operational modes of the {@link TFileChooserScreen}, determining its
	 * behavior and user interactions.
	 */
	public static enum Mode
	{
		// ==================================================
		/**
		 * {@link TFileChooserScreen} acts as a generic file browser. No
		 * opening/saving files logic.
		 */
		EXPLORE(gui(gui_icon_fsFolder()).append(" ").append(gui_fileChooser_mode_explore())),

		/**
		 * {@link TFileChooserScreen} has the user to select a {@link File}
		 * from existing files on their device.
		 */
		CHOOSE_FILE(gui(gui_icon_fsFolder()).append(" ").append(gui_fileChooser_mode_chooseFile())),

		/**
		 * {@link TFileChooserScreen} has the user choose the path for a
		 * new {@link File} that is to be created on their device.
		 */
		CREATE_FILE(gui(gui_icon_fsFolder()).append(" ").append(gui_fileChooser_mode_createFile()));
		// ==================================================
		private final Component windowTitle;
		// ==================================================
		Mode(@NotNull Component windowTitle) { this.windowTitle = windowTitle; }
		// ==================================================
		/**
		 * Returns the title that should be applied to a {@link TFileChooserScreen}'s
		 * window depending on its {@link Mode}.
		 */
		public final @NotNull Component getWindowTitle() { return this.windowTitle; }
		// ==================================================
	}
	// ================================================== ==================================================
	//                                            Builder IMPLEMENTATION
	// ================================================== ==================================================
	/**
	 * A builder class for creating {@link TFileChooserScreen} instances.
	 */
	public static final class Builder
	{
		// ==================================================
		private final @NotNull  Mode             mode;
		private       @Nullable Screen           lastScreen;
		private       @NotNull  Path             currDir;
		private final @NotNull  List<PathFilter> pathFilters;
		// ==================================================
		public Builder(@NotNull Mode mode) throws NullPointerException {
			this.mode        = Objects.requireNonNull(mode);
			this.currDir     = Path.of(System.getProperty("user.dir"));
			this.pathFilters = new ArrayList<>();
		}
		// ==================================================
		/**
		 * Gets the {@link Mode} this {@link Builder} was initialized with.
		 */
		public final @NotNull Mode getmode() { return this.mode; }
		// ==================================================
		/**
		 * Sets the {@link Screen} instance that will be assigned as the "last screen" for the
		 * {@link TFileChooserScreen} instance created by this builder.
		 * @param lastScreen The last {@link Screen} instance.
		 */
		public final Builder setLastScreen(@Nullable Screen lastScreen) {
			this.lastScreen = lastScreen;
			return this;
		}
		// --------------------------------------------------
		/**
		 * Sets the starting directory that the {@link TFileChooserScreen} instance created
		 * by this builder will display upon opening.
		 * @param currDir The current directory {@link Path}.
		 * @throws NullPointerException If the argument is {@code null}.
		 */
		public final Builder setCurrentDirectory(@NotNull Path currDir) throws NullPointerException {
			this.currDir = Objects.requireNonNull(currDir);
			return this;
		}
		// --------------------------------------------------
		/**
		 * Sets the {@link PathFilter} that will be used by the {@link TFileChooserScreen}
		 * instance created by this builder.
		 * @param pathFilter The {@link PathFilter} instance.
		 * @throws NullPointerException If the argument is {@code null}.
		 */
		public final Builder setPathFilter(@NotNull PathFilter pathFilter) throws NullPointerException {
			Objects.requireNonNull(pathFilter);
			this.pathFilters.clear();
			this.pathFilters.add(pathFilter);
			return this;
		}

		/**
		 * Adds a {@link PathFilter} that will be used by the {@link TFileChooserScreen}
		 * instance created by this builder.
		 * @param pathFilter The {@link PathFilter} instance.
		 * @throws NullPointerException If the argument is {@code null}.
		 * @throws IllegalStateException If the {@link Mode} of this {@link Builder} is {@link Mode#EXPLORE}.
		 * For now there is no interface to select a {@link PathFilter} in that mode. This may change in the
		 * future.
		 */
		public final Builder addPathFilter(@NotNull PathFilter pathFilter)
				throws NullPointerException, IllegalStateException
		{
			//not null requirement
			Objects.requireNonNull(pathFilter);
			//mode state requirement
			if(this.mode == Mode.EXPLORE && this.pathFilters.size() == 1)
				throw new IllegalStateException("Cannot have multiple file-filters in " + Mode.class.getName() + "#" + Mode.EXPLORE);
			//add file filter and return
			this.pathFilters.add(pathFilter);
			return this;
		}
		// ==================================================
		/**
		 * Builds a new {@link TFileChooserScreen} instance using the parameters
		 * previously set in this builder.
		 */
		public final @NotNull TFileChooserScreen build() {
			return new TFileChooserScreen(this.lastScreen, this.mode, this.pathFilters, this.currDir);
		}
		// ==================================================
	}
	// ================================================== ==================================================
	//                                      WindowElement IMPLEMENTATION
	// ================================================== ==================================================
	/**
	 * The {@link TWindowElement} whose GUI features file-system navigation and file
	 * selection.
	 */
	final class WindowElement extends TWindowElement
	{
		// ==================================================
		WindowElement() {
			titleProperty().set(TFileChooserScreen.this.titleProperty().get(), WindowElement.class);
			backgroundColorProperty().set(0xFF2b2b2b, WindowElement.class);
			closeOperationProperty().set(TWindowElement.CloseOperation.CLOSE_SCREEN, WindowElement.class);
		}
		// ==================================================
		protected final @Override void initBodyCallback(@NotNull TElement body)
		{
			final var explorer = new TFileExplorerPanel(new TFileExplorerPanel.View() {
				public final @Override @NotNull Path currentDirectory() { return TFileChooserScreen.this.history_dir; }
				public final @Override @NotNull Deque<Path> backStack() { return TFileChooserScreen.this.history_back; }
				public final @Override @NotNull Deque<Path> forwardStack() { return TFileChooserScreen.this.history_fwd; }
			});
			explorer.setBounds(body.getBounds());
			body.add(explorer);
		}
		// ==================================================
	}
	// ================================================== ==================================================
}
