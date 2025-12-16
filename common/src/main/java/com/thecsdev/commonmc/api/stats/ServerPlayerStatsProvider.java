package com.thecsdev.commonmc.api.stats;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A {@link StatsProvider} that provides statistics of a {@link ServerPlayer}.
 */
public final class ServerPlayerStatsProvider extends PlayerStatsProvider<ServerPlayer>
{
	// ==================================================
	private final ServerPlayer       player;
	// --------------------------------------------------
	private final ServerStatsCounter statsCounter;
	// ==================================================
	private ServerPlayerStatsProvider(@NotNull ServerPlayer player) throws NullPointerException {
		this.player       = Objects.requireNonNull(player);
		this.statsCounter = player.getStats();
	}
	// ==================================================
	public final @Override ServerPlayer getPlayer() { return this.player; }
	// --------------------------------------------------
	public final @Override <T> int getValue(Stat<T> stat) { return this.statsCounter.getValue(stat); }
	public final @Override <T> int getValue(StatType<T> type, T subject) { return this.statsCounter.getValue(type, subject); }
	// ==================================================
	public final @Override int hashCode() { return this.player.hashCode(); }
	public final @Override boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		final var spsp = (ServerPlayerStatsProvider) obj;
		return (this.player == spsp.player);
	}
	// ==================================================
	/**
	 * Creates a {@link ServerPlayerStatsProvider} instance based on a {@link ServerPlayer}.
	 * @param player The {@link ServerPlayer}.
	 */
	public static final ServerPlayerStatsProvider of(@NotNull ServerPlayer player) throws NullPointerException {
		return new ServerPlayerStatsProvider(player);
	}
	// ==================================================
}
