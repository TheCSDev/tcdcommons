package com.thecsdev.commonmc.api.events;

import com.thecsdev.common.event.Event;
import com.thecsdev.common.event.Events;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

/**
 * {@link Event}s related to {@link CreativeModeTab} / {@link CreativeModeTabs}.
 */
public interface CreativeModeTabEvent
{
	// ==================================================
	/**
	 * <b>Trigger:</b> Whenever {@link Item}s are reorganized into {@link CreativeModeTab}s.<br>
	 * <b>Thread:</b> Main (client) | Unknown if there are more
	 * @see CreativeModeTabs#tryRebuildTabContents(FeatureFlagSet, boolean, HolderLookup.Provider)
	 * @see RebuildContents#invoke(FeatureFlagSet, boolean, HolderLookup.Provider)
	 */
	Event<RebuildContents> REBUILD_CONTENTS = Events.createLoop();
	// ==================================================
	/**
	 * {@link Event} handler type for {@link #REBUILD_CONTENTS}.
	 */
	interface RebuildContents {
		/**
		 * See {@link CreativeModeTabEvent#REBUILD_CONTENTS}.
		 * @param enabledFeatures The <a href="https://www.minecraft.net/en-us/article/testing-new-minecraft-features/feature-toggles-java-edition">currently enabled feature flags</a>.
		 * @param operatorEnabled Whether the "Operator items" {@link CreativeModeTab} is enabled.
		 * @param lookup <i>Dev note: I have no idea what this is. May be useful though.</i>
		 */
		void invoke(@NotNull FeatureFlagSet enabledFeatures,
		            boolean operatorEnabled,
		            @NotNull HolderLookup.Provider lookup);
	}
	// ==================================================
}
