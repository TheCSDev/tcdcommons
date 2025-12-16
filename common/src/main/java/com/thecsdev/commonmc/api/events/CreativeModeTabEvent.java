package com.thecsdev.commonmc.api.events;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

/**
 * {@link Event}s related to {@link CreativeModeTab} / {@link CreativeModeTabs}.
 */
public final class CreativeModeTabEvent
{
	// ==================================================
	private CreativeModeTabEvent() {}
	// ==================================================
	/**
	 * This {@link Event} is invoked whenever {@link Item}s are reorganized into {@link CreativeModeTab}s.
	 * <p>
	 * In other words, whenever the following method is invoked:
	 * {@link CreativeModeTabs#tryRebuildTabContents(FeatureFlagSet, boolean, HolderLookup.Provider)}.
	 *
	 * @see RebuildContents#invoke(FeatureFlagSet, boolean, HolderLookup.Provider)
	 */
	public static final Event<RebuildContents> REBUILD_CONTENTS_POST = EventFactory.createLoop();
	// ==================================================
	public static interface RebuildContents
	{
		/**
		 * See {@link CreativeModeTabEvent#REBUILD_CONTENTS_POST}.
		 * @param enabledFeatures The <a href="https://www.minecraft.net/en-us/article/testing-new-minecraft-features/feature-toggles-java-edition">currently enabled feature flags</a>.
		 * @param operatorEnabled Whether the "Operator items" {@link CreativeModeTab} is enabled.
		 * @param lookup <i>Dev note: I have no idea what this is. May be useful though.</i>
		 */
		void invoke(FeatureFlagSet enabledFeatures, boolean operatorEnabled, HolderLookup.Provider lookup);
	}
	// ==================================================
}
