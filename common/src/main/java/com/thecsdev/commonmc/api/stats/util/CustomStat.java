package com.thecsdev.commonmc.api.stats.util;

import com.thecsdev.commonmc.api.stats.StatsProvider;
import com.thecsdev.commonmc.mixin.hooks.AccessorStat;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatFormatter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

import static net.minecraft.core.registries.BuiltInRegistries.CUSTOM_STAT;
import static net.minecraft.stats.Stats.CUSTOM;

/**
 * {@link SubjectStats} utility implementation for reading statistics
 * about a given "General" (also known as "Custom") stat.
 */
public final class CustomStat extends SubjectStats<Identifier>
{
	// ==================================================
	private final @NotNull Component              subjectName;
	private final @NotNull Stat<Identifier> stat;
	private final @NotNull StatFormatter          statFormatter;
	// ==================================================
	/**
	 * @throws NullPointerException If an argument is {@code null}.
	 * @throws IllegalStateException If the subject is not properly registered in the game's registries.
	 */
	public CustomStat(
			@NotNull Identifier subject,
			@NotNull StatsProvider statsProvider) throws NullPointerException, IllegalStateException {
		super(CUSTOM_STAT, subject, statsProvider);
		this.subjectName   = StatsProvider.getCustomStatName(subject);
		this.stat          = CUSTOM.get(subject);
		final var f        = ((AccessorStat)(Object)this.stat).getFormatter();
		this.statFormatter = (f != null) ? f : StatFormatter.DEFAULT;
	}
	// ==================================================
	public final @Override @NotNull Component getSubjectDisplayName() { return this.subjectName; }
	public final @Override @NotNull LinkedHashMap<Stat<Identifier>, Integer> getValues() {
		final var map = new LinkedHashMap<Stat<Identifier>, Integer>();
		map.put(this.stat, getValue());
		return map;
	}
	public final @Override boolean isEmpty() { return getValue() == 0; }
	// ==================================================
	/**
	 * Returns the raw (unformatted) numeric value of the "General" stat.
	 */
	public final int getValue() { return getStatsProvider().getValue(this.stat); }

	/**
	 * Returns {@link #getValue()} formatted using {@link Stat#format(int)}.
	 */
	public final @NotNull String getValueF() { return this.statFormatter.format(getValue()); }

	/**
	 * Returns {@link #getValue()} formatted using a custom formatter.
	 * @param formatter The {@link StatFormatterOverride} to use. If {@code null}, the default one is used.
	 */
	public final @NotNull String getValueF(@Nullable StatFormatterOverride formatter) {
		if(formatter == null) formatter = StatFormatterOverride.DEFAULT;
		return formatter.format(this.statFormatter, getValue());
	}
	// --------------------------------------------------
	/**
	 * Returns true if the {@link StatFormatter} of this {@link CustomStat}
	 * is {@link StatFormatter#DISTANCE}.
	 */
	public final boolean isDistance() { return this.statFormatter == StatFormatter.DISTANCE; }

	/**
	 * Returns true if the {@link StatFormatter} of this {@link CustomStat}
	 * is {@link StatFormatter#TIME}.
	 */
	public final boolean isTime() { return this.statFormatter == StatFormatter.TIME; }

	/**
	 * Returns true if the {@link StatFormatter} of this {@link CustomStat}
	 * is {@link StatFormatter#DIVIDE_BY_TEN}.
	 */
	public final boolean isDivideBy10() { return this.statFormatter == StatFormatter.DIVIDE_BY_TEN; }
	// ==================================================
	/**
	 * Obtains a list of all {@link CustomStat}.
	 * @param statsProvider The {@link StatsProvider} instance.
	 * @param predicate An optional {@link Predicate} for filtering the stats.
	 * @param comparator An optional {@link Comparator} for sorting the list.
	 * @throws NullPointerException If a {@link NotNull} argument is {@code null}.
	 */
	public static final Collection<CustomStat> getCustomStats(
			@NotNull StatsProvider statsProvider,
			@Nullable Predicate<CustomStat> predicate,
			@Nullable Comparator<CustomStat> comparator) throws NullPointerException
	{
		//not null requirements
		Objects.requireNonNull(statsProvider);
		//initialize and populate the list
		final var result = new ArrayList<CustomStat>((predicate == null) ? CUSTOM_STAT.size() : 0);
		for(final var statSubject : CUSTOM_STAT) {
			final var stat = new CustomStat(statSubject, statsProvider);
			if(predicate != null && !predicate.test(stat)) continue;
			result.add(stat);
		}
		//sort the list based on display names
		if(comparator != null) result.sort(comparator);
		//return the result
		return result;
	}
	// ==================================================
}
