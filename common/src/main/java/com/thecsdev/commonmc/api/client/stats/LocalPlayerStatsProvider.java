package com.thecsdev.commonmc.api.client.stats;

import com.thecsdev.commonmc.api.stats.PlayerStatsProvider;
import com.thecsdev.commonmc.api.stats.StatsProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.stats.StatsCounter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A {@link StatsProvider} that provides statistics of a {@link LocalPlayer}.
 */
public final class LocalPlayerStatsProvider extends PlayerStatsProvider<LocalPlayer>
{
	// ==================================================
	private final LocalPlayer  player;
	// --------------------------------------------------
	private final StatsCounter statsCounter;
	// ==================================================
	private LocalPlayerStatsProvider(@NotNull LocalPlayer player) throws NullPointerException {
		this.player       = Objects.requireNonNull(player);
		this.statsCounter = player.getStats();
	}
	// ==================================================
	public final @Override LocalPlayer getPlayer() { return this.player; }
	// --------------------------------------------------
	public final @Override <T> int getValue(Stat<T> stat) { return this.statsCounter.getValue(stat); }
	public final @Override <T> int getValue(StatType<T> type, T subject) { return this.statsCounter.getValue(type, subject); }
	// ==================================================
	public final @Override int hashCode() { return this.player.hashCode(); }
	public final @Override boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		final var lpsp = (LocalPlayerStatsProvider) obj;
		return (this.player == lpsp.player);
	}
	// ==================================================
	/**
	 * Creates a {@link LocalPlayerStatsProvider} instance based on a {@link LocalPlayer}.
	 * @param player The {@link LocalPlayer}.
	 * @throws NullPointerException If the argument is {@code null}.
	 */
	public static final LocalPlayerStatsProvider of(@NotNull LocalPlayer player) throws NullPointerException {
		return new LocalPlayerStatsProvider(player);
	}

	/**
	 * Creates a {@link LocalPlayerStatsProvider} instance using {@link Minecraft#player}.
	 * @throws NullPointerException If {@link Minecraft#player} is {@code null}.
	 */
	public static final LocalPlayerStatsProvider ofCurrentLocalPlayer() throws NullPointerException {
		return new LocalPlayerStatsProvider(Objects.requireNonNull(Minecraft.getInstance().player));
	}
	// ==================================================
}
