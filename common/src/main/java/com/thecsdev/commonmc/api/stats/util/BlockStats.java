package com.thecsdev.commonmc.api.stats.util;

import com.thecsdev.commonmc.api.stats.StatsProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

import static net.minecraft.core.registries.BuiltInRegistries.BLOCK;

/**
 * {@link SubjectStats} utility implementation for reading statistics
 * about a given {@link Block}.
 */
public final class BlockStats extends SubjectStats<Block>
{
	// ==================================================
	/**
	 * @throws NullPointerException  If an argument is {@code null}.
	 * @throws IllegalStateException If the subject is not properly registered in the game's registries.
	 */
	public BlockStats(
			@NotNull Block subject,
			@NotNull StatsProvider statsProvider) throws NullPointerException, IllegalStateException {
		super(BuiltInRegistries.BLOCK, subject, statsProvider);
	}
	// ==================================================
	public final @Override @NotNull Component getSubjectDisplayName() { return getSubject().getName(); }
	public final @Override @NotNull LinkedHashMap<Stat<Block>, Integer> getValues() {
		final var subject = getSubject();
		final var map     = new LinkedHashMap<Stat<Block>, Integer>();
		for(final var st : StatsProvider.getBlockStatTypes())
			map.put(st.get(subject), getStatsProvider().getValue(st, subject));
		return map;
	}
	// ==================================================
	/**
	 * Returns the value of {@link Stats#BLOCK_MINED}.
	 */
	public final int getTimesMined() { return getStatsProvider().getValue(Stats.BLOCK_MINED, getSubject()); }
	// ==================================================
	/**
	 * Obtains a list of all {@link BlockStats}.
	 * @param statsProvider The {@link StatsProvider} instance.
	 * @param predicate An optional {@link Predicate} for filtering the stats.
	 * @param comparator An optional {@link Comparator} for sorting the list.
	 * @throws NullPointerException If a {@link NotNull} argument is {@code null}.
	 */
	public static final Collection<BlockStats> getBlockStats(
			@NotNull StatsProvider statsProvider,
			@Nullable Predicate<BlockStats> predicate,
			@Nullable Comparator<BlockStats> comparator) throws NullPointerException
	{
		//not null requirements
		Objects.requireNonNull(statsProvider);
		//initialize and populate the list
		final var result = new ArrayList<BlockStats>((predicate == null) ? BLOCK.size() : 0);
		for(final var statSubject : BLOCK) {
			final var stat = new BlockStats(statSubject, statsProvider);
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
