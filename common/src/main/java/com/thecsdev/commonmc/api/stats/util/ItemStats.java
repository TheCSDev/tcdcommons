package com.thecsdev.commonmc.api.stats.util;

import com.thecsdev.commonmc.api.stats.StatsProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

import static net.minecraft.core.registries.BuiltInRegistries.ITEM;

/**
 * {@link SubjectStats} utility implementation for reading statistics
 * about a given {@link Item}.
 */
public final class ItemStats extends SubjectStats<Item>
{
	// ==================================================
	private BlockStats blockStats;
	// ==================================================
	/**
	 * @throws NullPointerException  If an argument is {@code null}.
	 * @throws IllegalStateException If the subject is not properly registered in the game's registries.
	 */
	public ItemStats(
			@NotNull Item subject,
			@NotNull StatsProvider statsProvider) throws NullPointerException, IllegalStateException {
		super(BuiltInRegistries.ITEM, subject, statsProvider);
	}
	// ==================================================
	public final @Override @NotNull Component getSubjectDisplayName() { return getSubject().getName(); }
	public final @Override @NotNull LinkedHashMap<Stat<Item>, Integer> getValues() {
		final var subject = getSubject();
		final var map     = new LinkedHashMap<Stat<Item>, Integer>();
		for(final var st : StatsProvider.getItemStatTypes())
			map.put(st.get(subject), getStatsProvider().getValue(st, subject));
		return map;
	}
	// ==================================================
	/**
	 * Returns the {@link BlockStats} of the {@link #getSubject()}'s corresponding {@link Block}.
	 */
	public final @NotNull BlockStats getItemBlockStats() {
		if(this.blockStats == null)
			this.blockStats = new BlockStats(Block.byItem(getSubject()), getStatsProvider());
		return this.blockStats;
	}
	// ==================================================
	/**
	 * Returns the value of {@link Stats#ITEM_BROKEN}.
	 */
	public final int getTimesBroken() { return getStatsProvider().getValue(Stats.ITEM_BROKEN, getSubject()); }

	/**
	 * Returns the value of {@link Stats#ITEM_CRAFTED}.
	 */
	public final int getTimesCrafted() { return getStatsProvider().getValue(Stats.ITEM_CRAFTED, getSubject()); }

	/**
	 * Returns the value of {@link Stats#ITEM_DROPPED}.
	 */
	public final int getTimesDropped() { return getStatsProvider().getValue(Stats.ITEM_DROPPED, getSubject()); }

	/**
	 * Returns the value of {@link Stats#ITEM_USED}.
	 */
	public final int getTimesUsed() { return getStatsProvider().getValue(Stats.ITEM_USED, getSubject()); }

	/**
	 * Returns the value of {@link Stats#ITEM_PICKED_UP}.
	 */
	public final int getTimesPickedUp() { return getStatsProvider().getValue(Stats.ITEM_PICKED_UP, getSubject()); }
	// ==================================================
	/**
	 * Obtains a list of all {@link ItemStats}.
	 * @param statsProvider The {@link StatsProvider} instance.
	 * @param predicate An optional {@link Predicate} for filtering the stats.
	 * @param comparator An optional {@link Comparator} for sorting the list.
	 * @throws NullPointerException If a {@link NotNull} argument is {@code null}.
	 */
	public static final Collection<ItemStats> getItemStats(
			@NotNull StatsProvider statsProvider,
			@Nullable Predicate<ItemStats> predicate,
			@Nullable Comparator<ItemStats> comparator) throws NullPointerException
	{
		//not null requirements
		Objects.requireNonNull(statsProvider);
		//initialize and populate the list
		final var result = new ArrayList<ItemStats>((predicate == null) ? ITEM.size() : 0);
		for(final var statSubject : ITEM) {
			final var stat = new ItemStats(statSubject, statsProvider);
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
