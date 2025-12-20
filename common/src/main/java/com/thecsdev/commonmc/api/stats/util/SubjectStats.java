package com.thecsdev.commonmc.api.stats.util;

import com.thecsdev.common.util.annotations.Virtual;
import com.thecsdev.commonmc.api.stats.IStatsProvider;
import com.thecsdev.commonmc.mixin.hooks.AccessorStat;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * Utility for reading statistics about a given thing (aka subject).
 * @param <S> The "thing" about which the statistics are.
 *            This could be something like a general-stat/item/mob.
 */
public sealed abstract class SubjectStats<S> permits CustomStat, ItemStats, BlockStats, EntityStats
{
	// ==================================================
	private final @NotNull IStatsProvider statsProvider;
	private final @NotNull  S                subject;
	private final @NotNull  Identifier subjectID;
	// ==================================================
	/**
	 * @throws NullPointerException If an argument is {@code null}.
	 * @throws IllegalStateException If the subject is not properly registered in the game's registries.
	 */
	protected SubjectStats(
			@NotNull Registry<S> subjectRegistry,
			@NotNull S subject,
			@NotNull IStatsProvider statsProvider) throws NullPointerException, IllegalStateException
	{
		//initialize stats provider and subject as normal
		this.statsProvider = requireNonNull(statsProvider);
		this.subject       = requireNonNull(subject);

		//the subject id is handled a bit differently
		@Nullable var subjectID = requireNonNull(subjectRegistry).getKey(subject);
		if(subjectID == null)
			throw new IllegalStateException("'" + subjectRegistry + "' does not contain '" + subject + "'");
		this.subjectID     = subjectID;
	}
	// ==================================================
	public final @Override int hashCode() { return Objects.hash(this.statsProvider, this.subject); }
	public final @Override boolean equals(@Nullable Object obj) {
		return (obj == this) ||
				(obj instanceof SubjectStats<?> st &&
						(this.statsProvider == st.statsProvider && this.subject == st.subject));
	}
	// ==================================================
	/**
	 * The {@link IStatsProvider} instance used by this {@link SubjectStats} instance.
	 */
	public final @NotNull IStatsProvider getStatsProvider() { return this.statsProvider; }

	/**
	 * The "thing" this {@link SubjectStats} instance is about.
     * This could be something like a custom-id/item/mob.
	 */
	public final @NotNull S getSubject() { return this.subject; }

	/**
	 * Returns the {@link Identifier} that is associated with the given
	 * {@link #getSubject()} in the game's registry system.
	 * @see BuiltInRegistries
	 */
	public final @NotNull Identifier getSubjectID() { return this.subjectID; }

	/**
	 * Returns the in-game display name of the {@link #getSubject()}.
	 */
	public abstract @NotNull Component getSubjectDisplayName();
	// ==================================================
	/**
	 * Returns all the statistics about the {@link #getSubject()}.
	 */
	public abstract @NotNull LinkedHashMap<Stat<S>, Integer> getValues();

	/**
	 * Returns all the statistics about the {@link #getSubject()},
	 * with their values formatted using {@link Stat#format(int)}.
	 */
	public final @NotNull LinkedHashMap<Stat<S>, String> getValuesF() {
		return getValuesF(StatFormatterOverride.DEFAULT);
	}

	/**
	 * Returns all the statistics about the {@link #getSubject()},
	 * with their values formatted using a custom formatter.
	 * @param formatter The custom formatter to use.
	 * @throws NullPointerException If the argument is {@code null}.
	 */
	public final @NotNull LinkedHashMap<Stat<S>, String> getValuesF(@NotNull StatFormatterOverride formatter) throws NullPointerException
	{
		Objects.requireNonNull(formatter);
		final var vals = getValues();
		final var map  = new LinkedHashMap<Stat<S>, String>(vals.size());
		for(final var entry : vals.entrySet())
		{
			final var stat          = entry.getKey();
			final var statFormatter = ((AccessorStat)(Object)stat).getFormatter();
			final var statValue     = entry.getValue();
			map.put(stat, formatter.format(statFormatter, statValue));
		}
		return map;
	}

	/**
	 * Returns {@code true} if all {@link StatType}s for the given
	 * {@link #getSubject()} have the value {@code 0}.
	 */
	public @Virtual boolean isEmpty() { return getValues().values().stream().noneMatch(val -> val != 0); }
	// --------------------------------------------------
	/**
	 * Returns {@code true} if this {@link #getSubject()} matches a given search query.
	 * @param query The search query.
	 */
	public @Virtual boolean isSearchMatch(@NotNull String query) throws NullPointerException
	{
		//not null requirement
		Objects.requireNonNull(query);
		//normalize strings to make it easier to compare
		query         = query.replaceAll("[^A-Za-z]", "").toLowerCase(Locale.ENGLISH);
		final var sid = this.subjectID.toString().replaceAll("[^A-Za-z]", "").toLowerCase(Locale.ENGLISH);
		final var sdn = getSubjectDisplayName().getString().replaceAll("[^A-Za-z]", "").toLowerCase(Locale.ENGLISH);
		//compare and return
		return sid.contains(query) || sdn.contains(query);
	}
	// ==================================================
}
