package com.thecsdev.commonmc.api.stats;

import net.minecraft.world.entity.player.Player;

/**
 * A {@link StatsProvider} that is related to a {@link Player} entity.
 */
public abstract class PlayerStatsProvider<P extends Player> extends StatsProvider
{
	// ==================================================
	/**
	 * Returns the {@link Player} instance this {@link PlayerStatsProvider} is related to.
	 */
	public abstract P getPlayer();
	// ==================================================
}
