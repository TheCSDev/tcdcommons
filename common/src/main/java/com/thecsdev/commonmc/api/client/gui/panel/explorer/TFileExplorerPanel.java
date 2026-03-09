package com.thecsdev.commonmc.api.client.gui.panel.explorer;

import com.thecsdev.common.util.annotations.Virtual;
import com.thecsdev.commonmc.api.client.gui.panel.TPanelElement;
import com.thecsdev.commonmc.api.client.gui.screen.promise.TFileChooserScreen;
import com.thecsdev.commonmc.api.client.gui.widget.TDropdownWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Deque;
import java.util.Objects;
import java.util.function.Predicate;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public @Virtual class TFileExplorerPanel extends TPanelElement.Paintable
{
	// ================================================== ==================================================
	//                                 TFileExplorerPanel IMPLEMENTATION
	// ================================================== ==================================================
	private final @NotNull View view;
	// ==================================================
	public TFileExplorerPanel(@NotNull View view) {
		this.view = Objects.requireNonNull(view);
		outlineColorProperty().set(0, TFileExplorerPanel.class);
		focusedOutlineColorProperty().set(0x33FFFFFF, TFileExplorerPanel.class);
	}
	// ==================================================
	/**
	 * Returns the {@link View} instance associated with this {@link TFileExplorerPanel}.
	 */
	public final @NotNull View getView() { return this.view; }
	// ==================================================
	protected @Virtual @Override void initCallback()
	{
		//FIXME - IMPLEMENT
	}
	// ================================================== ==================================================
	//                                               View IMPLEMENTATION
	// ================================================== ==================================================
	/**
	 * This interface defines the necessary methods for managing the state of a
	 * {@link TFileExplorerPanel}, including the current directory and navigation
	 * history.
	 */
	public interface View
	{
		// ==================================================
		/**
		 * Returns the directory currently displayed in the panel.
		 *
		 * @return The active {@link Path}.
		 */
		public @NotNull Path currentDirectory();
		// --------------------------------------------------
		/**
		 * Returns the stack of previously visited directories.
		 *
		 * @apiNote The returned {@link Deque} must be mutable and unique to
		 * this {@link View} instance.
		 * @return The backward navigation history.
		 */
		public @NotNull Deque<Path> backStack();

		/**
		 * Returns the stack of directories available for forward navigation.
		 *
		 * @apiNote The returned {@link Deque} must be mutable and unique to
		 * this {@link View} instance.
		 * @return The forward navigation history.
		 */
		public @NotNull Deque<Path> forwardStack();
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
