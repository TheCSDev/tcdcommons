package com.thecsdev.commonmc.api.client.gui.screen;

import com.thecsdev.common.math.UDim2;
import com.thecsdev.common.properties.ObjectProperty;
import com.thecsdev.common.util.enumerations.CompassDirection;
import com.thecsdev.commonmc.api.client.gui.TElement;
import com.thecsdev.commonmc.api.client.gui.ctxmenu.TContextMenu;
import com.thecsdev.commonmc.api.client.gui.label.TLabelElement;
import com.thecsdev.commonmc.api.client.gui.misc.TFillColorElement;
import com.thecsdev.commonmc.api.client.gui.panel.TPanelElement;
import com.thecsdev.commonmc.api.client.gui.panel.window.TWindowElement;
import com.thecsdev.commonmc.api.client.gui.render.TGuiGraphics;
import com.thecsdev.commonmc.api.client.gui.util.TInputContext;
import com.thecsdev.commonmc.api.client.gui.widget.TButtonWidget;
import com.thecsdev.commonmc.api.client.gui.widget.TDropdownWidget;
import com.thecsdev.commonmc.api.client.gui.widget.TScrollBarWidget;
import com.thecsdev.commonmc.api.client.gui.widget.text.TSimpleTextFieldWidget;
import com.thecsdev.commonmc.resources.TCDCLang;
import com.thecsdev.commonmc.resources.TCDCSprites;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Util;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.InvalidPathException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.thecsdev.commonmc.resources.TCDCLang.*;
import static com.thecsdev.commonmc.resources.TCDCSprites.gui_icon_fsFolder;
import static com.thecsdev.commonmc.resources.TComponent.gui;
import static net.minecraft.network.chat.Component.literal;

/**
 * {@link TScreen} implementation that provides a user-friendly interface for selecting
 * files from the device's file-system. This screen is particularly useful for cases that
 * require file opening and saving functionalities, allowing users to easily navigate
 * through their directories and choose the desired files.
 */
public final class TFileChooserScreen extends TScreenPlus implements ILastScreenProvider
{
	// ================================================== ==================================================
	//                                 TFileChooserScreen IMPLEMENTATION
	// ================================================== ==================================================
	private final @Nullable Screen                   lastScreen;
	private final @NotNull  Mode                     mode;
	private final @NotNull  List<FileFilter>         fileFilters;
	private final @NotNull  BiConsumer<Result, File> onResult; //@NotNull Result, @Nullable File
	// --------------------------------------------------
	private       @Nullable FileFilter               currFileFilter;
	private       @NotNull  File                     history_currDir;
	private final @NotNull  Deque<File>              history_back     = new LinkedList<>();
	private final @NotNull  Deque<File>              history_fwd      = new LinkedList<>();
	// --------------------------------------------------
	private                 boolean                  output_invoked   = false;
	private       @NotNull  Result                   output_result    = Result.ERROR;
	private final @NotNull  ObjectProperty<File>     output_file      = new ObjectProperty<>(null);
	// ==================================================
	private TFileChooserScreen(
			@Nullable Screen lastScreen,
			@NotNull Mode mode,
			@NotNull File currDir,
			@NotNull List<FileFilter> fileFilters,
			@NotNull BiConsumer<Result, File> onResult) throws NullPointerException
	{
		//initialize 'super'
		super(Objects.requireNonNull(mode).getWindowTitle());

		//initialize fields
		this.lastScreen      = lastScreen;
		this.mode            = mode; //already null-checked earlier
		this.history_currDir = Objects.requireNonNull(currDir);
		this.fileFilters     = Objects.requireNonNull(fileFilters);
		this.onResult        = Objects.requireNonNull(onResult);

		if(!this.fileFilters.isEmpty()) //select first file-filer if there is one present
			this.currFileFilter = fileFilters.getFirst();

		//post-processing the output file
		this.output_file.addFilter(file -> {
			//the file must not be null or a file-system's root file
			if(file == null || file.getParentFile() == null)
				return null;
			//if a file-filter is present, sanitize the file's name
			if(this.currFileFilter != null) {
				final var newName = this.currFileFilter.postProcessFilename(file.getName());
				if(newName.equals(file.getName()))
					file = new File(file.getParentFile(), newName);
			}
			//return the filtered file result
			return file;
		}, TFileChooserScreen.class);
		//changes to the output file should be reflected on the action panel
		this.output_file.addChangeListener((p, o, n) ->
				findChild(c -> c instanceof ActionPanel, true)
				.ifPresent(c -> ((ActionPanel)c).in_filename.textProperty().set(
						(n != null) ? n.getName() : "", TFileChooserScreen.class
				)));
		//required ownership claim so outsiders don't mess up the property's behavior
		this.output_file.setOwner(TFileChooserScreen.class, TFileChooserScreen.class);
	}
	// ==================================================
	public final @Override @Nullable Screen getLastScreen() { return this.lastScreen; }
	// --------------------------------------------------
	public final @Override void renderCallback(@NotNull TGuiGraphics pencil) {
		//the following is the background that is to be rendered:
		if(this.lastScreen != null) {
			//the last screen is to be visually rendered below this screen
			this.lastScreen.render(pencil.getNative(), pencil.getMouseX(), pencil.getMouseY(), pencil.getDeltaTicks());
			//followed by a white plane background so it's easier to tell screens apart
			final var bb = getBounds();
			pencil.fillColor(bb.x, bb.y, bb.width, bb.height, 0x22FFFFFF);
		}
	}
	// --------------------------------------------------
	protected final @Override void initCallback() {
		//do not initialize a gui or even use this screen if this file chooser was used before
		if(this.output_invoked) { close(); return; }
		//create and add the window element
		final var wnd = new WindowElement();
		add(wnd);
		wnd.setBounds(new UDim2(0.1, 0, 0.1, 0), new UDim2(0.8, 0, 0.8, 0));
	}
	// --------------------------------------------------
	@SuppressWarnings("DataFlowIssue")
	public final @Override boolean inputCallback(
			@NotNull TInputContext.InputDiscoveryPhase phase,
			@NotNull TInputContext context)
	{
		//forward input super first, and return true if it handled it
		if(super.inputCallback(phase, context)) return true;
		//only handle MAIN phase
		else if(phase != TInputContext.InputDiscoveryPhase.MAIN) return false;

		//user pressing 'Escape' to close this screen, results in CANCEL
		if(context.getInputType() == TInputContext.InputType.KEY_PRESS &&
				context.getKeyCode() == GLFW.GLFW_KEY_ESCAPE) {
			this.output_result = Result.CANCEL;
			this.output_file.set(null, TFileChooserScreen.class);
			close();
			return true;
		}

		//all other inputs are not handled
		return false;
	}
	// --------------------------------------------------
	protected final @Override void closeCallback()
	{
		//if the "on result" handler was already invoked before, do nothing
		if(this.output_invoked) return; else this.output_invoked = true;

		//sanitize output data
		if(this.output_result != Result.APPROVE)
			this.output_file.set(null, TFileChooserScreen.class); //do not output files if not approved
		else if(/*this.output_result == Result.APPROVE &&*/ this.output_file.get() == null)
			this.output_result = Result.ERROR; //if approving without an output file

		//lastly, the "on result" handler with the output data
		try {
			//FIXME ? Minecrafr#setScreen(Screen) calls from here fail because we're already inside a #setScreen call
			this.onResult.accept(this.output_result, this.output_file.get());
		} catch(Exception exc) {
			//prepare to construct an error popup textual dialog to later show the user
			final var client  = Objects.requireNonNull(getClient(), "Missing 'client' instance");
			final var message = ExceptionUtils.getStackTrace(exc)
					.replace("\r\n", "\n").replace("\t", "    ");
			//scheduling the opening of the text dialog screen because #setScreen no workie here
			client.schedule(() -> {
				final var screen  = new TTextDialogScreen(
						client.screen, //use 'client.screen' inside scheduled task and 'this.lastScreen' outside
						gui("icon/unseen_notification").append(" ").append(TCDCLang.gui_screen_textDialog_errorTitle()),
						literal(message));
				screen.getMessageLabel().wrapTextProperty().set(false, TFileChooserScreen.class);
				client.setScreen(screen.getAsScreen());
			});
		}
	}
	// ==================================================
	/**
	 * Gets the operational {@link Mode} of this {@link TFileChooserScreen}.
	 */
	public final @NotNull Mode getMode() { return this.mode; }

	/**
	 * Gets the current directory that this {@link TFileChooserScreen} is displaying.
	 */
	public final @NotNull File getCurrentDirectory() { return this.history_currDir; }
	// --------------------------------------------------
	/**
	 * Refreshes the window element, re-initializing its contents to reflect any
	 * changes in the file-system or current directory.
	 */
	public final void refresh() {
		if(!isOpen()) return;
		findChild(c -> c instanceof WindowElement, false).ifPresent(TElement::clearAndInit);
	}

	/**
	 * Navigates to the specified directory, updating the current directory
	 * and refreshing the screen to display its contents.
	 * @param directory The target directory {@link File}.
	 * @throws NullPointerException If the argument is {@code null}.
	 */
	public final void navigateTo(@NotNull File directory) throws NullPointerException
	{
		//clear selected output file, as it is no longer valid (it belonged to the now previous directory)
		TFileChooserScreen.this.output_file.set(null, TFileChooserScreen.class);

		//push current dir to back history, then change current dir
		this.history_back.push(this.history_currDir);
		this.history_currDir = Objects.requireNonNull(directory);
		//enforce max size of 32 on the backward history
		while(this.history_back.size() > 32)
			this.history_back.removeLast();
		//clear forward history and refresh
		this.history_fwd.clear();
		refresh();
	}

	/**
	 * Navigates back to the previous directory in the navigation history.
	 */
	public final boolean navigateBack()
	{
		//clear selected output file, as it is no longer valid (it belonged to the now previous directory)
		TFileChooserScreen.this.output_file.set(null, TFileChooserScreen.class);

		//if there's no back history, we cannot navigate
		if(this.history_back.isEmpty()) return false;
		//push current dir to forward history, then change current dir
		this.history_fwd.push(this.history_currDir);
		this.history_currDir = this.history_back.pop();
		//enforce max size of 32 on the forward history
		while(this.history_fwd.size() > 32)
			this.history_fwd.removeLast();
		//refresh and return
		refresh();
		return true;
	}

	/**
	 * Navigates forward to the next directory in the navigation history.
	 */
	public final boolean navigateForward()
	{
		//clear selected output file, as it is no longer valid (it belonged to the now previous directory)
		TFileChooserScreen.this.output_file.set(null, TFileChooserScreen.class);

		//if there's no forward history, we cannot navigate
		if(this.history_fwd.isEmpty()) return false;
		//push current dir to back history, then change current dir
		this.history_back.push(this.history_currDir);
		this.history_currDir = this.history_fwd.pop();
		//enforce max size of 32 on the backward history
		while(this.history_back.size() > 32)
			this.history_back.removeLast();
		//refresh and return
		refresh();
		return true;
	}
	// ================================================== ==================================================
	//                             Mode/Result/FileFilter IMPLEMENTATION
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

	/**
	 * Defines the possible results of a file selection operation within a
	 * {@link TFileChooserScreen}.
	 */
	public static enum Result
	{
		/**
		 * The user approved the file selection (e.g., clicked "Open" or "Save").
		 */
		APPROVE,

		/**
		 * The user canceled the file selection operation.
		 */
		CANCEL,

		/**
		 * An error occurred during the file selection process.
		 */
		//FIXME - Implement. For when the dialog is closed by an external non-user thing.
		ERROR
	}

	/**
	 * A filter interface for filtering {@link File}s displayed in the
	 * {@link TFileChooserScreen}.
	 */
	@FunctionalInterface
	public static interface FileFilter extends java.io.FileFilter, Predicate<File>, TDropdownWidget.Entry
	{
		// ==================================================
		/**
		 * A file filter that accepts all {@link File}s.
		 */
		public static final FileFilter ALL = new FileFilter() {
			private static final Component DISPLAY_NAME = Component.literal("*.*");
			public final @Override @NotNull Component getDisplayName() { return DISPLAY_NAME; }
			public final @Override boolean accept(@Nullable File pathname) { return true; }
		};
		// ==================================================
		default @Override @NotNull Component getDisplayName() { return Component.literal("*.?"); }
		// --------------------------------------------------
		//predicate tests are redirected to file-filter
		default @Override @ApiStatus.NonExtendable boolean test(@Nullable File file) { return accept(file); }
		//filtering logic goes here:
		boolean accept(File pathname);
		// --------------------------------------------------
		/**
		 * Ensures that the provided {@link File}'s name adheres to the criteria defined
		 * by this {@link FileFilter}. If the file name is already considered acceptable,
		 * it is returned as-is. Otherwise, a modified name that meets the acceptance
		 * criteria is returned.
		 * @param fileName The original name to be processed.
		 * @throws NullPointerException If the argument is {@code null}.
		 */
		default @NotNull String postProcessFilename(@NotNull String fileName) throws NullPointerException { return fileName; }
		// ==================================================
		/**
		 * Creates a simple {@link FileFilter} that filters {@link File}s based on their
		 * extension name.
		 * @param extnameCaseSensitive The extension name (with or without leading period).
		 * @throws NullPointerException If the argument is {@code null}.
		 * @throws IllegalArgumentException If the extension name contains an illegal character.
		 * Note that this method may miss some illegal characters, so it is ultimately your
		 * responsibility to ensure the provided extension name is valid.
		 */
		public static FileFilter extname(@NotNull String extnameCaseSensitive)
				throws NullPointerException, IllegalArgumentException
		{
			//check for illegal characters
			Objects.requireNonNull(extnameCaseSensitive);
			final var illegalChars = new char[] { '/', '\\', '?', '%', '*', ':', '|', '"', '<', '>' };
			for(final var c : illegalChars)
				if(extnameCaseSensitive.indexOf(c) >= 0)
					throw new IllegalArgumentException("Extension name cannot contain character: " + c);
			//for simplicity, the extension name has a period internally
			final var extnameF = (extnameCaseSensitive.startsWith(".")) ? extnameCaseSensitive : ("." + extnameCaseSensitive);
			final var extnameC = Component.literal(extnameF);
			//construct and return the file filter
			return new FileFilter()
			{
				public final @Override @NotNull Component getDisplayName() { return extnameC; }
				public final @Override boolean accept(@Nullable File pathname) {
					return pathname != null && pathname.getName().endsWith(extnameF);
				}
				public final @Override @NotNull String postProcessFilename(@NotNull String fileName) throws NullPointerException {
					fileName = fileName.replaceAll("[\n\r<>:\"/\\\\|?*\\x00-\\x1F]", "_").trim();
					return fileName.endsWith(extnameF) ? fileName : (fileName + extnameF);
				}
			};
		}
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
		private       @NotNull  File             currDir;
		private final @NotNull  List<FileFilter> fileFilters;
		// ==================================================
		public Builder(@NotNull Mode mode) throws NullPointerException {
			this.currDir     = new File(System.getProperty("user.home"));
			this.mode        = Objects.requireNonNull(mode);
			this.fileFilters = new ArrayList<>();
		}
		// ==================================================
		/**
		 * Gets the {@link Mode} this {@link Builder} was initialized with.
		 */
		public final @NotNull Mode getmode() { return this.mode; }
		// --------------------------------------------------
		/**
		 * Sets the {@link Screen} instance that will be assigned as the "last screen" for the
		 * {@link TFileChooserScreen} instance created by this builder.
		 * @param lastScreen The last {@link Screen} instance.
		 */
		public final Builder setLastScreen(@Nullable Screen lastScreen) {
			this.lastScreen = lastScreen;
			return this;
		}

		/**
		 * Sets the starting directory that the {@link TFileChooserScreen} instance created
		 * by this builder will display upon opening.
		 * @param currDir The current directory {@link File}.
		 * @throws NullPointerException If the argument is {@code null}.
		 */
		public final Builder setCurrentDirectory(@NotNull File currDir) throws NullPointerException {
			this.currDir = Objects.requireNonNull(currDir);
			return this;
		}

		/**
		 * Sets the {@link FileFilter} that will be used by the {@link TFileChooserScreen}
		 * instance created by this {@link Builder} to filter displayed {@link File}s.
		 * @param fileFilter The {@link FileFilter} instance.
		 * @throws NullPointerException If the argument is {@code null}.
		 */
		public final Builder setFileFilter(@NotNull FileFilter fileFilter) throws NullPointerException {
			Objects.requireNonNull(fileFilter);
			this.fileFilters.clear();
			this.fileFilters.add(fileFilter);
			return this;
		}

		/**
		 * Adds a {@link FileFilter} that will be used by the {@link TFileChooserScreen}
		 * instance created by this {@link Builder} to filter displayed {@link File}s.
		 * @param fileFilter The {@link FileFilter} instance.
		 * @throws NullPointerException If the argument is {@code null}.
		 * @throws IllegalStateException If the {@link Mode} of this {@link Builder} is {@link Mode#EXPLORE}.
		 * For now there is no interface to select a {@link FileFilter} in that mode. This may change in the
		 * future.
		 */
		public final Builder addFileFilter(@NotNull FileFilter fileFilter)
				throws NullPointerException, IllegalStateException
		{
			//not null requirement
			Objects.requireNonNull(fileFilter);
			//mode state requirement
			if(this.mode == Mode.EXPLORE && this.fileFilters.size() == 1)
				throw new IllegalStateException("Cannot have multiple file-filters in " + Mode.class.getName() + "#" + Mode.EXPLORE);
			//add file filter and return
			this.fileFilters.add(fileFilter);
			return this;
		}
		// --------------------------------------------------
		/**
		 * Builds a new {@link TFileChooserScreen} instance using the parameters
		 * previously set in this builder.
		 * @param resultHandler A {@link BiConsumer} that handles the result of the
		 * file selection operation, receiving a {@link NotNull} {@link Result} and
		 * the selected {@link Nullable} {@link File}.
		 * @return A new {@link TFileChooserScreen} instance.
		 * @throws NullPointerException If the argument is {@code null}.
		 */
		public final @NotNull TFileChooserScreen build(
				@NotNull BiConsumer<@NotNull Result, @Nullable File> resultHandler)
				throws NullPointerException
		{
			return new TFileChooserScreen(
					this.lastScreen, this.mode, this.currDir,
					this.fileFilters, resultHandler);
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
			titleProperty().set(TFileChooserScreen.this.title.get(), WindowElement.class);
			backgroundColorProperty().set(0xFF2b2b2b, WindowElement.class);
			closeOperationProperty().set(TWindowElement.CloseOperation.CLOSE_SCREEN, WindowElement.class);
		}
		// ==================================================
		protected final @Override void initBodyCallback(@NotNull TElement body)
		{
			//the panel where all gui elements will reside
			final var panel_main = new TFillColorElement.Flat(0xFF202020, 0);
			panel_main.setBounds((TFileChooserScreen.this.mode == Mode.EXPLORE) ?
					body.getBounds() : body.getBounds().add(0, 0, 0, -18));
			body.add(panel_main);

			//navigation panel
			final var panel_nav = new NavigationPanel();
			panel_main.add(panel_nav);
			panel_nav.setBounds(UDim2.ZERO, new UDim2(1, 0, 0, 15));

			//quick access panel
			final var panel_qa = new QuickAccessPanel();
			panel_main.add(panel_qa);
			panel_qa.setBounds(new UDim2(0, 0, 0, 15), new UDim2(0.25, -7, 1, -15));

			//explorer panel
			final var panel_ex = new ExplorerPanel();
			panel_main.add(panel_ex);
			panel_ex.setBounds(new UDim2(0.25, 0, 0, 15), new UDim2(0.75, -7, 1, -15));

			//scroll-bars
			final var scroll_qa = new TScrollBarWidget.Flat(panel_qa);
			panel_main.add(scroll_qa);
			scroll_qa.setBounds(new UDim2(0.25, -8, 0, 15), new UDim2(0, 8, 1, -15));

			final var scroll_ex = new TScrollBarWidget.Flat(panel_ex);
			panel_main.add(scroll_ex);
			scroll_ex.setBounds(new UDim2(1, -8, 0, 15), new UDim2(0, 8, 1, -15));

			//action panel
			if(TFileChooserScreen.this.mode != Mode.EXPLORE) {
				final var panel_action = new ActionPanel();
				body.add(panel_action);
				panel_action.setBounds(new UDim2(0, 0, 1, -18), new UDim2(1, 0, 0, 18));
			}
		}
		// ==================================================
	}
	// ================================================== ==================================================
	//                                    NavigationPanel IMPLEMENTATION
	// ================================================== ==================================================
	/**
	 * The top navigation panel that shows the current directory path.
	 */
	final class NavigationPanel extends TElement
	{
		// ==================================================
		protected final @Override void initCallback()
		{
			//forward/backward/refresh navigation buttons
			final var btn_back = new TButtonWidget.Paintable(0x66888888, 0, 0xFFFFFFFF);
			btn_back.setBounds(0, 0, 20, 15);
			btn_back.getLabel().setText(Component.literal("<"));
			btn_back.getLabel().textScaleProperty().set(0.8, NavigationPanel.class);
			btn_back.eClicked.register(__ -> TFileChooserScreen.this.navigateBack());
			addRel(btn_back);

			final var btn_fwd = new TButtonWidget.Paintable(0x66888888, 0, 0xFFFFFFFF);
			btn_fwd.setBounds(20, 0, 20, 15);
			btn_fwd.getLabel().setText(Component.literal(">"));
			btn_fwd.getLabel().textScaleProperty().set(0.8, NavigationPanel.class);
			btn_fwd.eClicked.register(__ -> TFileChooserScreen.this.navigateForward());
			addRel(btn_fwd);

			final var btn_refresh = new TButtonWidget.Paintable(0x66888888, 0, 0xFFFFFFFF);
			btn_refresh.setBounds(40, 0, 20, 15);
			btn_refresh.getLabel().setText(Component.literal("o"));
			btn_refresh.getLabel().textScaleProperty().set(0.85, NavigationPanel.class);
			btn_refresh.eClicked.register(__ -> TFileChooserScreen.this.refresh());
			addRel(btn_refresh);

			//the label that shows the current directory path
			final var lbl_path = new TLabelElement();
			lbl_path.setBounds(getBounds().add(65, 0, -65, 0));
			lbl_path.setText(Component.literal(
					TFileChooserScreen.this.history_currDir.getAbsolutePath()
							.replace("\\", "/").replace("/", " > ")
			));
			lbl_path.textScaleProperty().set(0.7, NavigationPanel.class);
			lbl_path.textColorProperty().set(0xCCFFFFFF, NavigationPanel.class);
			lbl_path.textAlignmentProperty().set(
					//depending on if the text width is too big to fit the label
					((double) lbl_path.fontProperty().get().width(lbl_path.getText()) * lbl_path.textScaleProperty().getD() < lbl_path.getBounds().width) ?
							//assign appropriate direction - west by default, east if too big
							CompassDirection.WEST : CompassDirection.EAST,
					NavigationPanel.class
			);
			add(lbl_path);
		}
		// ==================================================
		public final @Override void renderCallback(@NotNull TGuiGraphics pencil) {
			final var bb = getBounds();
			pencil.fillColor(bb.x, bb.y, bb.width, bb.height, 0xFF000000);
		}
		// ==================================================
	}
	// ================================================== ==================================================
	//                                   QuickAccessPanel IMPLEMENTATION
	// ================================================== ==================================================
	/**
	 * The "quick access" panel provides convenient access to frequently used directories
	 * and drives on the user's device.
	 */
	final class QuickAccessPanel extends TPanelElement.Transparent
	{
		// ==================================================
		QuickAccessPanel() {
			scrollPaddingProperty().set(7, QuickAccessPanel.class);
		}
		// ==================================================
		protected final @Override void initCallback()
		{
			//quick access category
			initCategory(gui("icon/accessibility").append(" ").append(TCDCLang.gui_fileChooser_quickAccess()));
			final var dir_home = new File(System.getProperty("user.home"));
			final var dir_desk = new File(dir_home, "Desktop");
			final var dir_down = new File(dir_home, "Downloads");
			final var dir_docs = new File(dir_home, "Documents");
			final var dir_udir = new File(System.getProperty("user.dir"));
			if(dir_home.exists()) initFileEntry(dir_home);
			if(dir_down.exists()) initFileEntry(dir_down);
			if(dir_desk.exists()) initFileEntry(dir_desk);
			if(dir_docs.exists()) initFileEntry(dir_docs);
			if(dir_udir.exists()) initFileEntry(dir_udir);

			//devices category, for drive letters / mount points
			initCategory(gui("statistics/item_crafted").append(" ").append(TCDCLang.gui_fileChooser_quickAccess_mountPoints()));
			final var roots = File.listRoots();
			if(roots != null)
				for(final var root : roots)
					if(root.exists()) initFileEntry(root);
			else initFileEntry(new File("/"));
		}
		// --------------------------------------------------
		public final @Override void renderCallback(@NotNull TGuiGraphics pencil) {
			//obtain bounding box
			final var bb = getBounds();
			//draw focus outline if focused
			if(isFocused())
				pencil.drawOutlineIn(bb.x, bb.y, bb.width - 1, bb.height, 0x22FFFFFF);
		}
		// ==================================================
		/**
		 * Initializes a new category title {@link TLabelElement}.
		 * @param name The name of the category.
		 */
		private final void initCategory(@NotNull Component name) {
			//init the label
			final var lbl = new TLabelElement(name);
			lbl.setBounds(computeNextYBounds(15, 10));
			lbl.textScaleProperty().set(0.8d, QuickAccessPanel.class);
			add(lbl);
			//init a small gap element below the label
			final var el_gap = new TElement();
			el_gap.setBounds(computeNextYBounds(3, 0));
			add(el_gap);
		}

		/**
		 * Initializes a new file entry {@link FileEntryElement}.
		 * @param file The file or directory represented by the entry.
		 */
		private final void initFileEntry(@NotNull File file) {
			final var el = new FileEntryElement(file);
			el.setBounds(computeNextYBounds(15, 0).add(5, 0, -5, 0));
			add(el);
		}
		// ==================================================
	}
	// ================================================== ==================================================
	//                                      ExplorerPanel IMPLEMENTATION
	// ================================================== ==================================================
	/**
	 * The main panel where file-system navigation and file entries are displayed.
	 */
	final class ExplorerPanel extends TPanelElement.Transparent
	{
		// ==================================================
		ExplorerPanel() {
			scrollPaddingProperty().set(7, ExplorerPanel.class);
		}
		// ==================================================
		protected final @Override void initCallback()
		{
			//obtain current directory
			final var dir = TFileChooserScreen.this.history_currDir;

			//initialize the "../" (parent directory) entry if applicable
			final @Nullable var parent = dir.getParentFile();
			if(parent != null && parent.exists()) {
				final var el_up = new FileEntryElement(
						parent, gui(TCDCSprites.gui_icon_fsFolderGray()).append(" ../"));
				el_up.setBounds(computeNextYBounds(15, 0));
				add(el_up);
			}

			//obtain file list for the current directory
			@NotNull  File[]    dir_files    = null;
			@Nullable Exception dir_filesErr = null;
			try {
				dir_files = dir.listFiles();
				if(dir_files == null)
					throw new FileNotFoundException(dir.getAbsolutePath());
			} catch(Exception e) { dir_filesErr = e; }

			//initialize error label if applicable
			if(dir_filesErr != null) {
				final var bb  = getBounds();
				final int pad = scrollPaddingProperty().getI();
				final var lbl = new TLabelElement(Component.literal(
						dir_filesErr.getClass().getName() + "\n" +
						dir_filesErr.getLocalizedMessage()
				));
				lbl.setBounds(pad, pad + 15, bb.width - (pad * 2), bb.height - (pad * 2) - 30);
				lbl.wrapTextProperty().set(true, ExplorerPanel.class);
				lbl.textAlignmentProperty().set(CompassDirection.CENTER, ExplorerPanel.class);
				lbl.textColorProperty().set(0x55FFFFFF, ExplorerPanel.class);
				lbl.textScaleProperty().set(0.8, ExplorerPanel.class);
				addRel(lbl);
				return;
			}

			//filter out invisible files
			dir_files = Arrays.stream(dir_files)
					.filter(File::exists)
					.filter(file -> !file.isHidden())
					.filter(file -> file.isDirectory() ||
							TFileChooserScreen.this.currFileFilter == null ||
							TFileChooserScreen.this.currFileFilter.test(file))
					.toArray(File[]::new);

			//initialize entries for all directories and then files in the current directory
			for(final var file : dir_files)
				if(file.isDirectory()) {
					final var el = new FileEntryElement(file);
					el.setBounds(computeNextYBounds(15, 0));
					add(el);
				}
			for(final var file : dir_files)
				if(file.isFile()) {
					final var el = new FileEntryElement(file);
					el.setBounds(computeNextYBounds(15, 0));
					add(el);
				}
		}
		// --------------------------------------------------
		public final @Override void renderCallback(@NotNull TGuiGraphics pencil) {
			//obtain bounding box
			final var bb = getBounds();
			//draw focus outline if focused
			if(isFocused())
				pencil.drawOutlineIn(bb.x, bb.y, bb.width, bb.height, 0x22FFFFFF);
		}
		// ==================================================
	}
	// ================================================== ==================================================
	//                                        ActionPanel IMPLEMENTATION
	// ================================================== ==================================================
	/**
	 * The bottom action panel that contains action buttons like "Open", "Save", "Cancel", etc.
	 */
	final class ActionPanel extends TElement
	{
		// ==================================================
		private final TSimpleTextFieldWidget      in_filename;
		private final TDropdownWidget<FileFilter> dd_filefilter;
		private final TButtonWidget.Paintable     btn_cancel;
		private final TButtonWidget.Paintable     btn_accept;
		// ==================================================
		public ActionPanel()
		{
			//initialize fields
			this.in_filename   = new TSimpleTextFieldWidget();
			this.dd_filefilter = new TDropdownWidget<>(TFileChooserScreen.this.currFileFilter);
			this.btn_cancel    = new TButtonWidget.Paintable(0x50440000, 0x50FFFFFF, 0xFFAAFFFF);
			this.btn_accept    = new TButtonWidget.Paintable(0x50004400, 0x50FFFFFF, 0xFFAAFFFF);

			//configure child elements
			this.in_filename.placeholderProperty().set(
					TCDCLang.gui_fileChooser_action_inputFilename_placeholder(),
					ActionPanel.class);
			this.in_filename.getTextLabel().textScaleProperty().set(0.8, ActionPanel.class);
			this.in_filename.getPlaceholderLabel().textScaleProperty().set(0.8, ActionPanel.class);
			this.dd_filefilter.getLabel().textScaleProperty().set(0.8, ActionPanel.class);
			this.dd_filefilter.getLabel().textAlignmentProperty().set(CompassDirection.WEST, ActionPanel.class);
			this.btn_cancel.getLabel().setText(Component.literal("x"));
			this.btn_cancel.getLabel().textAlignmentProperty().set(CompassDirection.CENTER, ActionPanel.class);
			this.btn_cancel.getLabel().textScaleProperty().set(0.8, ActionPanel.class);
			this.btn_accept.getLabel().setText(Component.literal("✓"));
			this.btn_accept.getLabel().textAlignmentProperty().set(CompassDirection.CENTER, ActionPanel.class);
			this.btn_accept.getLabel().textScaleProperty().set(0.8, ActionPanel.class);
			this.btn_accept.visibleProperty().set(TFileChooserScreen.this.mode != Mode.EXPLORE, ActionPanel.class);

			//barebones minimal filename input filtering
			this.in_filename.textProperty().addFilter(n -> //note: #trim()-ing is done in btn_accept
					n.replace("\\", "/").replace("/", ""), ActionPanel.class);
			//to avoid cyclic dependencies, the filename input field shall not have
			//any change listeners for its text property

			//initialize file filter dropdown entries, and its value change listener
			this.dd_filefilter.getEntries().addAll(TFileChooserScreen.this.fileFilters);
			this.dd_filefilter.selectedEntryProperty().addChangeListener((p, o, n) -> {
				TFileChooserScreen.this.currFileFilter = n;
				TFileChooserScreen.this.refresh();
			});

			//the cancel button closes the dialog with CANCEL result
			this.btn_cancel.eClicked.register(__ -> {
				TFileChooserScreen.this.output_result = Result.CANCEL;
				TFileChooserScreen.this.output_file.set(null, ActionPanel.class);
				TFileChooserScreen.this.close();
			});

			//the accept button closes the dialog with ACCEPT result.
			//behavior varies based on the dialog's Mode
			this.btn_accept.eClicked.register(__ ->
			{
				//post-process the input file-name
				String fileName = this.in_filename.textProperty().get().trim();
				if(TFileChooserScreen.this.currFileFilter != null)
					fileName = TFileChooserScreen.this.currFileFilter.postProcessFilename(fileName);
				//don't do anything if file-name is blank. such files can't exist on a file-system
				if(fileName.isBlank()) {
					//noinspection DataFlowIssue
					screenProperty().get().focusedElementProperty().set(this.in_filename, ActionPanel.class);
					return;
				}

				//try to construct file and set it as the output file
				try {
					TFileChooserScreen.this.output_file.set(
							new File(TFileChooserScreen.this.history_currDir, fileName),
							ActionPanel.class);
				} catch(InvalidPathException e) {
					TFileChooserScreen.this.output_file.set(null, ActionPanel.class);
				}

				//a file has to be chosen for accepting to take place
				final @Nullable var output_file = TFileChooserScreen.this.output_file.get();
				if(output_file == null) return;
				//handle accepting based on file chooser mode
				switch(TFileChooserScreen.this.mode) {
					//if choosing a file, said file has to exist
					//(if exists, fall to CREATE logic because selection is the same)
					case CHOOSE_FILE:
						if(!output_file.exists()) break;
					//if creating a file, approve and close
					case CREATE_FILE:
						TFileChooserScreen.this.output_result = Result.APPROVE;
						TFileChooserScreen.this.close();
						break;
					//if exploring files, do nothing
					case EXPLORE:
					default: break;
				}
			});
		}
		// ==================================================
		protected final @Override void initCallback()
		{
			//obtain the bounding box for math calculations
			final var bb = getBounds();
			//recalculate bounds for children and add them
			this.in_filename.setBounds(bb.x + 2, bb.y + 2, bb.width - 150, 15);
			add(this.in_filename);
			this.dd_filefilter.setBounds(bb.endX - 146, bb.y + 2, 90, 15);
			add(this.dd_filefilter);
			this.btn_cancel.setBounds(bb.endX - 54, bb.y + 2, 25, 15);
			add(this.btn_cancel);
			this.btn_accept.setBounds(bb.endX - 27, bb.y + 2, 25, 15);
			add(this.btn_accept);
		}
		// --------------------------------------------------
		public final @Override void renderCallback(@NotNull TGuiGraphics pencil) {
			final var bb = getBounds();
			pencil.fillColor(bb.x, bb.y, bb.width, bb.height, 0xFF2b2b2b);
			pencil.fillColor(bb.x, bb.y, bb.width, 1, 0xFF000000);
		}
		// ==================================================
	}
	// ================================================== ==================================================
	//                                   FileEntryElement IMPLEMENTATION
	// ================================================== ==================================================
	/**
	 * {@link TButtonWidget} implementation that represents a single {@link File}
	 * entry on a {@link TFileChooserScreen}.
	 */
	final class FileEntryElement extends TButtonWidget.Paintable
	{
		// ==================================================
		private final File file;
		private       long lastClickMs = 0; //time of the last click
		// ==================================================
		public FileEntryElement(@NotNull File file) { this(file, computeFileLabelText(file)); }
		public FileEntryElement(@NotNull File file, @NotNull Component text)
		{
			//initialize fields and properties
			this.file = Objects.requireNonNull(file);

			//configure label
			getLabel().setText(text);
			getLabel().textAlignmentProperty().set(CompassDirection.WEST, FileEntryElement.class);
			getLabel().textScaleProperty().set(0.8d, FileEntryElement.class);

			//configure appearance
			backgroundColorProperty().set(0, FileEntryElement.class);
			outlineColorProperty().set(0, FileEntryElement.class);

			//noinspection unchecked - tooltip
			contextMenuProperty().set(
					(Function<TElement, TContextMenu>)(Object) CONTEXT_MENU,
					FileEntryElement.class);
		}
		// ==================================================
		protected final @Override void clickCallback()
		{
			//directory navigation is independent and needs no double-clicking
			if(this.file.isDirectory()) {
				TFileChooserScreen.this.navigateTo(this.file);
				return;
			}

			//the file is selected, set it as the output file
			//(this also needs no double-click)
			TFileChooserScreen.this.output_file.set(this.file, FileEntryElement.class);

			//keep track of the last click, for double-clicking purposes
			final long lastClickMs = this.lastClickMs;
			final long thisClickMs = this.lastClickMs = System.currentTimeMillis();
			//enforce double-clicking
			if(thisClickMs - lastClickMs > 500) return; //not a double click
			//handle double click based on file chooser mode
			switch(TFileChooserScreen.this.mode)
			{
				//when choosing/creating, approve the selection of this file
				case CHOOSE_FILE, CREATE_FILE: {
					//approve selection of this file path for creation
					TFileChooserScreen.this.output_result = Result.APPROVE;
					TFileChooserScreen.this.close();
					break;
				}
				//when exploring, open the file with the associated application
				case EXPLORE:
				default: {
					Util.getPlatform().openUri(this.file.toURI());
					break;
				}
			}
		}
		// ==================================================
		/**
		 * Returns the {@link File} represented by this {@link FileEntryElement}.
		 */
		public final @NotNull File getFile() { return this.file; }
		// ==================================================
		/**
		 * Creates a text label for the given {@link File}, including an appropriate
		 * icon based on the file type.
		 * @param file The {@link File} for which to compute the label text.
		 */
		public static final @NotNull MutableComponent computeFileLabelText(@NotNull File file) {
			return computeFileIcon(file).append(" " + (!file.getName().isBlank() ? file.getName() : file.getPath()));
		}

		/**
		 * Creates an icon {@link MutableComponent} for the given {@link File}.
		 * @param file The {@link File} for which to compute the icon {@link Component}.
		 */
		public static final @NotNull MutableComponent computeFileIcon(@NotNull File file) {
			//TODO - Implement more icons for different file types
			return gui(file.isDirectory() ? TCDCSprites.gui_icon_fsFolder() : TCDCSprites.gui_icon_fsFile());
		}
		// ==================================================
		/**
		 * Construct a {@link TContextMenu} instance for this {@link FileEntryElement}.
		 */
		private static final Function<FileEntryElement, TContextMenu> CONTEXT_MENU = (fee) ->
			new TContextMenu.Builder(Objects.requireNonNull(fee.getClient()))
				.addContextMenu(
					gui(TCDCSprites.gui_icon_fsFolder()).append(" ").append(TCDCLang.gui_fileChooser_ctxmenu_openIn()),
					new TContextMenu.Builder(fee.getClient())
						.addButton(
							gui(TCDCSprites.gui_icon_fsFile()).append(" ").append(TCDCLang.gui_fileChooser_ctxmenu_openIn_assocApp()),
							btn -> Util.getPlatform().openUri(fee.getFile().toURI()))
						.build())
				.build();
		// ==================================================
	}
	// ================================================== ==================================================
}
