package com.thecsdev.commonmc.api.client.gui.screen.promise;

import com.thecsdev.common.math.UDim2;
import com.thecsdev.common.util.TUtils;
import com.thecsdev.common.util.enumerations.CompassDirection;
import com.thecsdev.commonmc.api.client.gui.TElement;
import com.thecsdev.commonmc.api.client.gui.ctxmenu.TContextMenu;
import com.thecsdev.commonmc.api.client.gui.label.TLabelElement;
import com.thecsdev.commonmc.api.client.gui.misc.TFillColorElement;
import com.thecsdev.commonmc.api.client.gui.panel.TPanelElement;
import com.thecsdev.commonmc.api.client.gui.panel.window.TWindowElement;
import com.thecsdev.commonmc.api.client.gui.render.TGuiGraphics;
import com.thecsdev.commonmc.api.client.gui.screen.ILastScreenProvider;
import com.thecsdev.commonmc.api.client.gui.screen.TScreen;
import com.thecsdev.commonmc.api.client.gui.widget.TButtonWidget;
import com.thecsdev.commonmc.api.client.gui.widget.TDropdownWidget;
import com.thecsdev.commonmc.api.client.gui.widget.TScrollBarWidget;
import com.thecsdev.commonmc.resource.TLanguage;
import com.thecsdev.commonmc.resource.TSprites;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Util;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.thecsdev.commonmc.resource.TComponent.air;
import static com.thecsdev.commonmc.resource.TComponent.gui;
import static com.thecsdev.commonmc.resource.TLanguage.*;
import static com.thecsdev.commonmc.resource.TSprites.gui_icon_fsFolder;
import static java.nio.file.Files.readAttributes;

/**
 * {@link TScreen} implementation that provides a user-friendly interface for selecting
 * files from the device's file-system. This screen is particularly useful for cases that
 * require file opening and saving functionalities, allowing users to easily navigate
 * through their directories and choose the desired files.
 */
@ApiStatus.Internal
@ApiStatus.Experimental
@Environment(EnvType.CLIENT)
public final class TFileChooserScreen extends TCompletableScreen<Collection<Path>> implements ILastScreenProvider
{
	// ================================================== ==================================================
	//                                 TFileChooserScreen IMPLEMENTATION
	// ================================================== ==================================================
	private final TFileChooserController controller;
	// --------------------------------------------------
	private long lastSeenEditCount = Long.MIN_VALUE; //for keeping up to date with controller's changes
	// ==================================================
	private TFileChooserScreen(
			@Nullable Screen lastScreen,
			@NotNull  Mode mode,
			@NotNull  Path currentDir,
			@NotNull  List<PathFilter> pathFilters) throws NullPointerException
	{
		super(lastScreen);
		titleProperty().set(Objects.requireNonNull(mode).getWindowTitle(), TFileChooserScreen.class);
		this.controller = new TFileChooserController(getResult(), mode, currentDir, pathFilters);
	}
	// ==================================================
	/**
	 * Returns the operating {@link Mode} of this {@link TFileChooserScreen}.
	 */
	public final @NotNull Mode getMode() { return this.controller.getMode(); }
	// ==================================================
	protected final @Override void tickCallback() {
		//if last seen edit count is out of date, we need to reinitialize
		if(this.lastSeenEditCount != this.controller.getEditCount())
			refresh();
	}
	// --------------------------------------------------
	/**
	 * Refreshes the window element, re-initializing its contents to reflect any
	 * changes in the file-system or current directory.
	 */
	public final void refresh() {
		if(!isOpen()) return;
		findChild(c -> c instanceof WindowElement, false).ifPresent(el -> {
			this.lastSeenEditCount = this.controller.getEditCount();
			el.clearAndInit();
		});
	}

	protected final @Override void initCallback()
	{
		//do not initialize a gui or even use this screen if this file chooser was used before
		if(getResult().isDone()) { close(); return; }

		//when reinitializing, we're up-to-date, so clear any "dirtiness" flags
		this.lastSeenEditCount = this.controller.getEditCount();

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
	 * @see Mode#EXPLORE
	 * @see Mode#CHOOSE_FILE
	 * @see Mode#CREATE_FILE
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
		 * {@link TFileChooserScreen} has the user to select a file
		 * from existing files on their device.
		 */
		CHOOSE_FILE(gui(gui_icon_fsFolder()).append(" ").append(gui_fileChooser_mode_chooseFile())),

		/**
		 * {@link TFileChooserScreen} has the user choose the path for a
		 * new file that is to be created on their device.
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
	//                                         PathFilter IMPLEMENTATION
	// ================================================== ==================================================
	/**
	 * A filter interface for filtering {@link Path}s displayed on the
	 * {@link TFileChooserScreen} interface.
	 */
	@FunctionalInterface
	public static interface PathFilter extends Predicate<Path>, TDropdownWidget.Entry
	{
		// ==================================================
		/**
		 * {@link PathFilter} instance that accepts all {@link Path}s.
		 */
		public static final PathFilter ALL = new PathFilter() {
			public final @Override @NotNull Component getDisplayName() { return Component.literal("*.*"); }
			public final @Override boolean test(Path path) { return true; }
		};
		// ==================================================
		default @Override @NotNull Component getDisplayName() { return Component.literal("?.?"); }
		// ==================================================
		/**
		 * Creates a simple {@link PathFilter} that filters {@link Path}s based on their
		 * extension name.
		 * @param extname The extension name (<b>case-sensitive</b>).
		 * @throws NullPointerException If the argument is {@code null}.
		 * @throws IllegalArgumentException If the extension name contains a known illegal character.
		 */
		public static PathFilter extname(@NotNull String extname)
				throws NullPointerException, IllegalArgumentException
		{
			Objects.requireNonNull(extname);
			if(!extname.startsWith(".")) extname = "." + extname;
			return extnames(Component.literal(extname), extname);
		}

		/**
		 * Creates a simple {@link PathFilter} that filters {@link Path}s based on their
		 * extension names.
		 * @param filterName The display name for the {@link PathFilter}.
		 * @param extnames The extension names (<b>case-sensitive</b>).
		 * @throws NullPointerException If an argument is {@code null}.
		 * @throws IllegalArgumentException If an extension name contains a known illegal character.
		 */
		public static PathFilter extnames(@NotNull Component filterName, @NotNull String... extnames)
				throws NullPointerException, IllegalArgumentException
		{
			//not null requirements
			Objects.requireNonNull(filterName);
			Objects.requireNonNull(extnames);

			//check for illegal characters
			final var illegalChars = new char[] { '/', '\\', '?', '%', '*', ':', '|', '"', '<', '>' };
			for(final var illegalChar : illegalChars)
				for(final var extname : extnames)
					if(extname.indexOf(illegalChar) >= 0)
						throw new IllegalArgumentException("Extension name cannot contain character: " + illegalChar);

			//for simplicity, the extension names are prefixed with a period
			for(int i = 0; i < extnames.length; i++)
				if(!extnames[i].startsWith("."))
					extnames[i] = "." + extnames[i];

			//construct and return the path filter
			return new PathFilter() {
				public final @Override @NotNull Component getDisplayName() { return filterName; }
				public final @Override boolean test(Path path) {
					final var fileName = path.getFileName().toString();
					for(final var extname : extnames)
						if(fileName.endsWith(extname))
							return true;
					return false;
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
		 * @throws IllegalArgumentException If the given {@link Path} is not absolute.
		 * @see Path#isAbsolute()
		 */
		public final Builder setCurrentDirectory(@NotNull Path currDir)
				throws NullPointerException, IllegalArgumentException
		{
			if(!Objects.requireNonNull(currDir).isAbsolute())
				throw new IllegalArgumentException("Path must be absolute");
			this.currDir = currDir;
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
			return new TFileChooserScreen(this.lastScreen, this.mode, this.currDir, this.pathFilters);
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
	private final @ApiStatus.Internal class WindowElement extends TWindowElement
	{
		// ==================================================
		private final TFileChooserController controller = TFileChooserScreen.this.controller;
		// ==================================================
		WindowElement() {
			titleProperty().set(TFileChooserScreen.this.titleProperty().get(), WindowElement.class);
			backgroundColorProperty().set(0xFF2b2b2b, WindowElement.class);
			closeOperationProperty().set(TWindowElement.CloseOperation.CLOSE_SCREEN, WindowElement.class);
		}
		// ==================================================
		protected final @Override void initBodyCallback(@NotNull TElement body)
		{
			//the panel where all gui elements will reside
			final var panel_main = new TFillColorElement.Flat(0xFF202020, 0);
			panel_main.setBounds((this.controller.getMode() == Mode.EXPLORE) ?
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

			//FIXME - IMPLEMENT
		}
		// ==================================================
	}
	// ================================================== ==================================================
	//                                    NavigationPanel IMPLEMENTATION
	// ================================================== ==================================================
	/**
	 * The top navigation panel that shows the current directory path.
	 */
	private final @ApiStatus.Internal class NavigationPanel extends TElement
	{
		// ==================================================
		private final TFileChooserController controller = TFileChooserScreen.this.controller;
		// ==================================================
		protected final @Override void initCallback()
		{
			//forward/backward/refresh navigation buttons
			final var btn_back = new TButtonWidget.Paintable(0x66888888, 0, 0xFFFFFFFF);
			btn_back.setBounds(0, 0, 20, 15);
			btn_back.getLabel().setText(Component.literal("<"));
			btn_back.getLabel().textScaleProperty().set(0.8, NavigationPanel.class);
			btn_back.eClicked.register(__ -> this.controller.navigateBack());
			addRel(btn_back);

			final var btn_fwd = new TButtonWidget.Paintable(0x66888888, 0, 0xFFFFFFFF);
			btn_fwd.setBounds(20, 0, 20, 15);
			btn_fwd.getLabel().setText(Component.literal(">"));
			btn_fwd.getLabel().textScaleProperty().set(0.8, NavigationPanel.class);
			btn_fwd.eClicked.register(__ -> this.controller.navigateForward());
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
					this.controller.getDirectory().toString()
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
	private final @ApiStatus.Internal class QuickAccessPanel extends TPanelElement.Transparent
	{
		// ==================================================
		private static record FileCategory(Component label, Collection<Map.Entry<Path, BasicFileAttributes>> entries) {}
		private static final CompletableFuture<List<FileCategory>> quickAccess = CompletableFuture.supplyAsync(() ->
		{
			//quick-access
			final var home = Path.of(System.getProperty("user.home"));
			final var quickAccessEntries = Stream.of(
							home,
							home.resolve("Desktop"),
							home.resolve("Documents"),
							home.resolve("Downloads"),
							home.resolve("Music"),
							home.resolve("Pictures"),
							home.resolve("Videos"),
							Path.of(System.getProperty("user.dir"))
					)
					.distinct()
					.map(path -> {
						try { return Map.entry(path, readAttributes(path, BasicFileAttributes.class)); }
						catch (Exception e) { return null; }
					})
					.filter(Objects::nonNull)
					.toList();

			//root directories
			final var deviceEntries = StreamSupport.stream(FileSystems.getDefault().getRootDirectories().spliterator(), false)
					.map(path -> {
						try { return Map.entry(path, readAttributes(path, BasicFileAttributes.class)); }
						catch (Exception e) { return null;}
					})
					.filter(Objects::nonNull)
					.toList();

			//return result
			return List.of(
					new FileCategory(gui("icon/accessibility").append(" ").append(TLanguage.gui_fileChooser_quickAccess()), quickAccessEntries),
					new FileCategory(gui("statistics/item_crafted").append(" ").append(TLanguage.gui_fileChooser_quickAccess_mountPoints()), deviceEntries)
			);
		}, TUtils.getVirtualThreadPerTaskExecutor());
		// ==================================================
		QuickAccessPanel() {
			scrollPaddingProperty().set(7, QuickAccessPanel.class);
		}
		// ==================================================
		protected final @Override void initCallback() {
			quickAccess.join().forEach(category -> {
				initCategory(category.label());
				for(final var entry : category.entries())
					initFileEntry(entry.getKey(), entry.getValue());
			});
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
		 * @param path The file or directory represented by the entry.
		 * @param attributes {@link Path}'s corresponding file attributes.
		 */
		private final void initFileEntry(@NotNull Path path, @NotNull BasicFileAttributes attributes) {
			final var el = new FileEntryElement(path, attributes);
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
	private final @ApiStatus.Internal class ExplorerPanel extends TPanelElement.Transparent
	{
		// ==================================================
		private final TFileChooserController controller = TFileChooserScreen.this.controller;
		// ==================================================
		ExplorerPanel() {
			scrollPaddingProperty().set(7, ExplorerPanel.class);
		}
		// ==================================================
		protected final @Override void initCallback()
		{
			//obtain current directory
			final var dir = this.controller.getDirectory();

			//initialize the "../" (parent directory) entry if applicable
			final @Nullable var parent = dir.getParent();
			if(parent != null) {
				final var el_up = new FileEntryElement(parent, new BasicFileAttributes() {
					public final @Override FileTime lastModifiedTime() { return FileTime.fromMillis(0); }
					public final @Override FileTime lastAccessTime() { return FileTime.fromMillis(0); }
					public final @Override FileTime creationTime() { return FileTime.fromMillis(0); }
					public final @Override boolean isRegularFile() { return false; }
					public final @Override boolean isDirectory() { return true; }
					public final @Override boolean isSymbolicLink() { return false; }
					public final @Override boolean isOther() { return false; }
					public final @Override long size() { return 0; }
					public final @Override Object fileKey() { return parent; }
				});
				el_up.setBounds(computeNextYBounds(15, 0));
				el_up.getLabel().setText(gui(TSprites.gui_icon_fsFolderGray()).append(" ../"));
				add(el_up);
			}

			//obtain file list for the current directory
			@NotNull  List<Map.Entry<Path, BasicFileAttributes>> dir_files    = List.of();
			@Nullable Exception                                  dir_filesErr = null;
			try { dir_files = listFiles(); } catch (Exception e) { dir_filesErr = e; }

			//initialize error label if applicable
			if(dir_filesErr != null)
			{
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

			//initialize entries for all directories and then files in the current directory
			else for(final var file : dir_files) {
				final var el = new FileEntryElement(file.getKey(), file.getValue());
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
		/**
		 * Lists the files in the current directory, applying the necessary filters and sorting.
		 * @throws Exception If an {@link Exception} occurs while accessing the file system.
		 */
		private final List<Map.Entry<Path, BasicFileAttributes>> listFiles() throws Exception
		{
			try (var stream = Files.list(this.controller.getDirectory()))
			{
				//read metadata
				final var futures = stream.limit(512)
						.map(path -> CompletableFuture.supplyAsync(() -> {
							try { return Map.entry(path, readAttributes(path, BasicFileAttributes.class)); }
							catch (Exception ignored) { return null; }
						}, TUtils.getVirtualThreadPerTaskExecutor()))
						.toList();

				//collect and sort
				return futures.stream()
						.map(CompletableFuture::join)
						.filter(Objects::nonNull)
						.sorted(Comparator
								.comparing((Map.Entry<Path, BasicFileAttributes> e) -> !e.getValue().isDirectory())
								.thenComparing(e -> e.getKey().getFileName().toString().toLowerCase()))
						.toList();
			}
		}
		// ==================================================
	}
	// ================================================== ==================================================
	//                                   FileEntryElement IMPLEMENTATION
	// ================================================== ==================================================
	/**
	 * {@link TButtonWidget} implementation that represents a single {@link Path}
	 * entry on a {@link TFileChooserScreen}.
	 */
	private final @ApiStatus.Internal class FileEntryElement extends TButtonWidget.Paintable
	{
		// ==================================================
		private final TFileChooserController controller = TFileChooserScreen.this.controller;
		// --------------------------------------------------
		private final Path                path;
		private final BasicFileAttributes attributes;
		// --------------------------------------------------
		private long lastClickMs = 0; //time of the last click
		// ==================================================
		public FileEntryElement(@NotNull Path path, @NotNull BasicFileAttributes attributes)
		{
			//initialize fields and properties
			this.path = Objects.requireNonNull(path);
			this.attributes = Objects.requireNonNull(attributes);

			//configure label
			getLabel().setText(computeFileLabelText());
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
		// --------------------------------------------------
		/**
		 * Creates a text label for this object, including an appropriate
		 * icon based on the file type.
		 */
		public final @NotNull MutableComponent computeFileLabelText() {
			final var name = Optional.ofNullable(this.path.getFileName()).map(Object::toString).orElse("");
			return computeFileIcon().append(" " + (!name.isBlank() ? name : path.toString()));
		}

		/**
		 * Creates an icon {@link MutableComponent} for this object.
		 */
		public final @NotNull MutableComponent computeFileIcon() {
			//TODO - Implement more icons for different file types
			return gui(this.attributes.isDirectory() ? TSprites.gui_icon_fsFolder() : TSprites.gui_icon_fsFile());
		}
		// ==================================================
		/**
		 * Returns the {@link Path} represented by this {@link FileEntryElement}.
		 */
		public final @NotNull Path getPath() { return this.path; }

		/**
		 * Returns the associated {@link TFileChooserController}.
		 */
		public final @NotNull TFileChooserController getController() { return this.controller; }
		// ==================================================
		/**
		 * Completes {@link #getResult()} with {@link #getPath()}
		 */
		private void select() {
			TFileChooserScreen.this.getResult().complete(List.of(this.path));
			TFileChooserScreen.this.close();
		}

		/**
		 * Opens the {@link #getPath()} with the default external application.
		 */
		private void open() { Util.getPlatform().openUri(this.path.toUri()); }
		// ==================================================
		protected final @Override void clickCallback()
		{
			//keep track of the last click, for double-clicking purposes
			final long lastClickMs = this.lastClickMs;
			final long thisClickMs = this.lastClickMs = System.currentTimeMillis();
			//enforce double-clicking
			if(thisClickMs - lastClickMs > 500 && !(getParent() instanceof QuickAccessPanel))
				return; //not a double click

			//directory navigation is independent and needs no double-clicking
			if(this.attributes.isDirectory()) {
				this.controller.navigateTo(this.path);
				return;
			}

			//handle double click based on file chooser mode
			switch(this.controller.getMode()) {
				//when choosing/creating, approve the selection of this file
				case CHOOSE_FILE, CREATE_FILE: { select(); break; }
				//when exploring, open the file with the associated application
				case EXPLORE:
				default: { open(); break; }
			}
		}
		// ==================================================
		/**
		 * Construct a {@link TContextMenu} instance for this {@link FileEntryElement}.
		 */
		private static final Function<FileEntryElement, TContextMenu> CONTEXT_MENU = (fee) ->
		{
			//create the builder instance
			final var builder = new TContextMenu.Builder(Objects.requireNonNull(fee.getClient()));

			//file "Select" / "Open"
			switch(fee.getController().getMode()) {
				case CREATE_FILE:
				case CHOOSE_FILE:
					builder.addButton(
							air().append(" ").append(TLanguage.gui_fileChooser_ctxmenu_open()),
							__ -> fee.select());
					builder.addSeparator();
					break;
				case EXPLORE:
					builder.addButton(
							air().append(" ").append(TLanguage.gui_fileChooser_ctxmenu_open()),
							__ -> fee.open());
					break;
				default: break;
			}

			//file "Open with"
			builder.addContextMenu(
					gui(TSprites.gui_icon_fsFolder()).append(" ").append(TLanguage.gui_fileChooser_ctxmenu_openWith()),
					__ -> new TContextMenu.Builder(fee.getClient())
							.addButton(
									gui(TSprites.gui_icon_fsFile()).append(" ").append(TLanguage.gui_fileChooser_ctxmenu_openWith_assocApp()),
									btn -> Util.getPlatform().openUri(fee.getPath().toUri()))
							.build());

			//build and return
			return builder.build();
		};
		// ==================================================
	}
	// ================================================== ==================================================
}
