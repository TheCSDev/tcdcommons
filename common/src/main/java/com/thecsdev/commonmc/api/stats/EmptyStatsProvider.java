package com.thecsdev.commonmc.api.stats;

import net.minecraft.stats.Stat;

/**
 * A {@link IStatsProvider} that always returns {@code 0} for every single stat.
 */
public final class EmptyStatsProvider implements IStatsProvider
{
	// ==================================================
	/**
	 * The main instance of {@link EmptyStatsProvider}.
	 */
	public static final IStatsProvider INSTANCE = new EmptyStatsProvider();
	// ==================================================
	private EmptyStatsProvider() {}
	// ==================================================
	public final @Override <T> int getIntValue(Stat<T> stat) { return 0; }
	// ==================================================
}
