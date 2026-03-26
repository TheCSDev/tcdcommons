package com.thecsdev.commonmc.api.stats.util;

import net.minecraft.stats.Stat;
import net.minecraft.stats.StatFormatter;
import org.jetbrains.annotations.NotNull;

/**
 * A functional interface for overriding the vanilla {@link StatFormatter} behavior.
 * @see StatFormatter
 */
@FunctionalInterface
public interface StatFormatterOverride
{
	// ==================================================
	public static final StatFormatterOverride DEFAULT = StatFormatter::format;
	// ==================================================
	/**
	 * Formats a {@link Stat}'s value using a custom formatter.
	 * @param vanillaFormatter The default vanilla formatter to use as a fallback or base.
	 * @param value            The statistic value to format.
	 * @return A formatted string representation of the statistic value.
	 */
	public @NotNull String format(@NotNull StatFormatter vanillaFormatter, int value);
	// ==================================================
}
