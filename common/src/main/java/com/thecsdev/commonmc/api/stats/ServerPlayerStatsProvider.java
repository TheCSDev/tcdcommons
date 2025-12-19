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
public final class ServerPlayerStatsProvider extends PlayerStatsProvider<@NotNull ServerPlayer>
{
	// ==================================================
	private final ServerPlayer       player;
	// ==================================================
	private final ServerStatsCounter _statsCounter;
	// ==================================================
	private ServerPlayerStatsProvider(@NotNull ServerPlayer player) throws NullPointerException {
		this.player        = Objects.requireNonNull(player);
		this._statsCounter = player.getStats();
	}
	// ==================================================
	public final @Override ServerPlayer getPlayer() { return this.player; }
	// --------------------------------------------------
	public final @Override <T> int getIntValue(Stat<T> stat) { return this._statsCounter.getValue(stat); }
	public final @Override <T> int getIntValue(@NotNull StatType<T> type, @NotNull T subject) { return this._statsCounter.getValue(type, subject); }
	// ==================================================
	public final @Override int hashCode() { return this.player.hashCode(); }
	public final @Override boolean equals(Object obj) {
		if(obj == this) return true;
		if(obj == null || obj.getClass() != getClass()) return false;
		return (((ServerPlayerStatsProvider)obj).player == this.player);
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
