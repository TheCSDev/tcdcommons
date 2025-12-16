package com.thecsdev.commonmc.api.stats;

import com.thecsdev.common.util.annotations.Virtual;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.stats.StatsCounter;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.Objects;

/**
 * An abstraction layer over the game's native {@link StatsCounter} mechanism,
 * for reading statistics data, primarily about players.
 */
public abstract class StatsProvider
{
	// ==================================================
	/**
	 * Returns the {@link Integer} value of a given {@link Stat}.
	 * @param stat The {@link Stat} whose value is to be obtained.
	 * @see StatsCounter
	 */
	public abstract <T> int getValue(Stat<T> stat);

	/**
	 * Returns the {@link Integer} value of a given {@link StatType} and its corresponding {@link Stat}.
	 * @param type The {@link StatType}.
	 * @param subject The subject about whom stat value is to be obtained.
	 * @see StatsCounter
	 * @apiNote You should not override this, as it calls {@link #getValue(Stat)} by default.
	 */
	public @Virtual <T> int getValue(StatType<T> type, T subject) { return getValue(type.get(subject)); }
	// ==================================================
	/**
	 * Returns an unmodifiable {@link List} of all registered {@link StatType}s
	 * related to {@link Item}s.
	 */
	@SuppressWarnings("unchecked")
	public static final List<StatType<Item>> getItemStatTypes() {
		return BuiltInRegistries.STAT_TYPE.stream()
				.filter(st -> st.getRegistry() == BuiltInRegistries.ITEM)
				.map(st -> (StatType<Item>) st)
				.toList();
	}

	/**
	 * Returns an unmodifiable {@link List} of all registered {@link StatType}s
	 * related to {@link Block}s.
	 */
	@SuppressWarnings("unchecked")
	public static final List<StatType<Block>> getBlockStatTypes() {
		return BuiltInRegistries.STAT_TYPE.stream()
				.filter(st -> st.getRegistry() == BuiltInRegistries.BLOCK)
				.map(st -> (StatType<Block>) st)
				.toList();
	}

	/**
	 * Returns an unmodifiable {@link List} of all registered {@link StatType}s
	 * related to {@link EntityType}s.
	 */
	@SuppressWarnings("unchecked")
	public static final List<StatType<EntityType<?>>> getEntityStatTypes() {
		return BuiltInRegistries.STAT_TYPE.stream()
				.filter(st -> st.getRegistry() == BuiltInRegistries.ENTITY_TYPE)
				.map(st -> (StatType<EntityType<?>>) st)
				.toList();
	}
	// --------------------------------------------------
	/**
	 * Returns the display name of a given {@link StatType}.
	 * @throws NullPointerException If the {@link StatType} is not registered.
	 */
	public static final Component getStatTypeName(StatType<?> statType) throws NullPointerException
	{
		//obtain the vanilla translation key
		final var stId            = Objects.requireNonNull(BuiltInRegistries.STAT_TYPE.getKey(statType));
		final var stTextKey       = String.format("stat_type.%s.%s", stId.getNamespace(), stId.getPath());
		final var stTextKeyModded = "tcdcommons." + stTextKey;

		//prioritize using modded translation key when available
		final var lang = Language.getInstance();
		return lang.has(stTextKeyModded) ?
				Component.translatable(stTextKeyModded) : //use modded when available
				(lang.has(stTextKey) ?                    //else use vanilla only if it exists
						Component.translatable(stTextKey) :
						Component.literal(stTextKeyModded)); //guide user to define modded translation
	}

	/**
	 * Returns the display name of a given "custom stat", also known as
	 * "general stat" in the GUI statistics menu.
	 * @param customStat The custom/general stat.
	 * @throws NullPointerException If the argument is {@code null}.
	 */
	public static MutableComponent getCustomStatName(Identifier customStat) throws NullPointerException {
		return Component.translatable("stat." + customStat.toString().replace(':', '.'));
	}
	// ==================================================
}
