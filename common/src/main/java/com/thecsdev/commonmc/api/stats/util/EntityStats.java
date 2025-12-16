package com.thecsdev.commonmc.api.stats.util;

import com.thecsdev.commonmc.api.stats.StatsProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

import static net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE;

/**
 * {@link SubjectStats} utility implementation for reading statistics
 * about a given {@link EntityType}.
 */
public final class EntityStats extends SubjectStats<EntityType<?>>
{
	// ==================================================
	/**
	 * @throws NullPointerException  If an argument is {@code null}.
	 * @throws IllegalStateException If the subject is not properly registered in the game's registries.
	 */
	public EntityStats(
			@NotNull EntityType<?> subject,
			@NotNull StatsProvider statsProvider) throws NullPointerException, IllegalStateException {
		super(ENTITY_TYPE, subject, statsProvider);
	}
	// ==================================================
	public final @Override @NotNull Component getSubjectDisplayName() { return getSubject().getDescription(); }
	public final @Override @NotNull LinkedHashMap<Stat<EntityType<?>>, Integer> getValues() {
		final var subject = getSubject();
		final var map     = new LinkedHashMap<Stat<EntityType<?>>, Integer>();
		for(final var st : StatsProvider.getEntityStatTypes())
			map.put(st.get(subject), getStatsProvider().getValue(st, subject));
		return map;
	}
	// ==================================================
	/**
	 * Returns the value of {@link Stats#ENTITY_KILLED}.
	 */
	public final int getKills() { return getStatsProvider().getValue(Stats.ENTITY_KILLED, getSubject()); }

	/**
	 * Returns the value of {@link Stats#ENTITY_KILLED_BY}.
	 */
	public final int getDeaths() { return getStatsProvider().getValue(Stats.ENTITY_KILLED_BY, getSubject()); }
	// ==================================================
	/**
	 * Obtains a list of all {@link EntityStats}.
	 * @param statsProvider The {@link StatsProvider} instance.
	 * @param predicate An optional {@link Predicate} for filtering the stats.
	 * @param comparator An optional {@link Comparator} for sorting the list.
	 * @throws NullPointerException If a {@link NotNull} argument is {@code null}.
	 */
	public static final Collection<EntityStats> getEntityStats(
			@NotNull StatsProvider statsProvider,
			@Nullable Predicate<EntityStats> predicate,
			@Nullable Comparator<EntityStats> comparator) throws NullPointerException
	{
		//not null requirements
		Objects.requireNonNull(statsProvider);
		//initialize and populate the list
		final var result = new ArrayList<EntityStats>((predicate == null) ? ENTITY_TYPE.size() : 0);
		for(final var statSubject : ENTITY_TYPE) {
			final var stat = new EntityStats(statSubject, statsProvider);
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
