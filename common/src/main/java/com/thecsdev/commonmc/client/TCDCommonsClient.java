package com.thecsdev.commonmc.client;

import com.thecsdev.commonmc.TCDCommons;
import com.thecsdev.commonmc.client.mixin.hooks.AccessorLocalPlayer;
import dev.architectury.event.events.client.ClientPlayerEvent;
import net.minecraft.client.multiplayer.SessionSearchTrees;
import net.minecraft.world.item.CreativeModeTabs;

import java.util.List;

/**
 * The main "client" entry-point for this mod, that is executed
 * by all loaders (fabric/neoforge).
 */
public class TCDCommonsClient extends TCDCommons
{
	// ==================================================
	public TCDCommonsClient()
	{
		//regroup items into creative mode tabs when joining worlds
		ClientPlayerEvent.CLIENT_PLAYER_JOIN.register(localPlayer ->
		{
			// ----------
			//do not do this if this feature is disabled
			if(!getConfig().updateItemGroupsOnJoin()) return;
			// ----------
			//noinspection resource | huh? this warning makes 0 sense in this context
			final var level  = localPlayer.level();
			final var client = ((AccessorLocalPlayer)localPlayer).getMinecraft();
			// ----------
			//when the client joins a world, update the item group display context
			//right away, to avoid lag spikes when opening inventory later.
			//this is also here so mods can display properly grouped items right away
			// ----------
			//update display context for items
			CreativeModeTabs.tryRebuildTabContents(
					level.enabledFeatures(),
					client.options.operatorItemsTab().get() && localPlayer.canUseGameMasterBlocks(),
					level.registryAccess());

			//update the "Search" item group
			if(localPlayer.connection.searchTrees() instanceof SessionSearchTrees sm) {
				final var list = List.copyOf(CreativeModeTabs.searchTab().getDisplayItems());
				sm.updateCreativeTooltips(level.registryAccess(), list);
				sm.updateCreativeTags(list);
			}
			// ----------
		});

		//NOTE - Below is a test trigger for testing GUI code:
		/*BlockEvent.BREAK.register((level, pos, state, player, xp) -> {
			final var client = Minecraft.getInstance();
			client.execute(() -> client.setScreen(new TTestScreen().getAsScreen()));
			return EventResult.pass();
		});*/
	}
	// ==================================================
}
