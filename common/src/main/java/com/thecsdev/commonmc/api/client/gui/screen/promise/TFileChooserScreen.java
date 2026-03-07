package com.thecsdev.commonmc.api.client.gui.screen.promise;

import com.thecsdev.commonmc.api.client.gui.render.TGuiGraphics;
import com.thecsdev.commonmc.api.client.gui.screen.ILastScreenProvider;
import com.thecsdev.commonmc.api.client.gui.screen.TScreen;
import com.thecsdev.commonmc.api.client.gui.widget.TDropdownWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

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
	// ==================================================
	public TFileChooserScreen(
			@Nullable Screen lastScreen,
			@NotNull Mode mode,
			@NotNull List<PathFilter> pathFilters) throws NullPointerException
	{
		super(lastScreen);
		this.mode        = Objects.requireNonNull(mode);
		this.pathFilters = List.copyOf(Objects.requireNonNull(pathFilters));
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
		throw new NotImplementedException("This file-chooser is not implemented yet.");
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
}
