package com.thecsdev.commonmc.api.stats;

import net.minecraft.stats.Stat;

import java.util.Random;

/**
 * A {@link IStatsProvider} that always returns random statistics.
 */
public final class RandomStatsProvider implements IStatsProvider
{
	// ==================================================
	/**
	 * The main instance of {@link RandomStatsProvider}.
	 */
	public static final RandomStatsProvider INSTANCE = new RandomStatsProvider();
	// --------------------------------------------------
	private final Random random = new Random();
	// ==================================================
	private RandomStatsProvider() {}
	// ==================================================
	public final @Override <T> int getIntValue(Stat<T> stat)  { return this.random.nextInt(0, Integer.MAX_VALUE); }
	// ==================================================
}
