package com.thecsdev.commonmc.api.world.item;

import com.thecsdev.commonmc.api.events.CreativeModeTabEvent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Objects;

/**
 * Utility methods related to {@link Item}s.
 */
public final class TItemUtils
{
	// ==================================================
	private static final HashMap<Item, CreativeModeTab> I2T = new HashMap<>();
	// ==================================================
	private TItemUtils() {}
	// ==================================================
	/**
	 * {@link ApiStatus.Internal} method that keeps track of {@link Item}s and their
	 * corresponding {@link CreativeModeTab}s.
	 * <p>
	 * Automatically called by {@link CreativeModeTabEvent#REBUILD_CONTENTS_POST}.
	 * <p>
	 * <b>Do not call this yourself!
	 */
	public static final @ApiStatus.Internal void rebuildI2TMap()
	{
		//start off by clearing the map
		I2T.clear();
		//then iterate all tabs and remap the items
		final var searchTab = CreativeModeTabs.searchTab();
		final var air       = Items.AIR;
		for(final var tab : CreativeModeTabs.allTabs())
		{
			//ignore the search group, as it is used for the
			//creative menu item search tab
			if(tab == searchTab) continue;

			//add group's items to the map
			tab.getDisplayItems().forEach(stack ->
			{
				//obtain the stack's item, and ensure an item is present
				//(in Minecraft's "language", AIR usually refers to "null")
				final var item = Objects.requireNonNull(stack.getItem());
				if(item == air) return;

				//put the item and its group to the map
				I2T.put(item, tab);
			});
		}
	}
	// ==================================================
	/**
	 * Returns the {@link CreativeModeTab} a given {@link Item} likely belongs to.
	 * @param item The target {@link Item}.
	 */
	public static final @Nullable CreativeModeTab getCreativeModeTab(@NotNull Item item) { return I2T.get(item); }
	// ==================================================
}
