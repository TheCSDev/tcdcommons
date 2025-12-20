package com.thecsdev.commonmc.api.client.stats;

import com.thecsdev.commonmc.api.stats.IStatsProvider;
import com.thecsdev.commonmc.api.stats.PlayerStatsProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.stats.StatsCounter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A {@link IStatsProvider} that provides statistics of a {@link LocalPlayer}.
 */
public final class LocalPlayerStatsProvider extends PlayerStatsProvider<@NotNull LocalPlayer>
{
	// ==================================================
	private final LocalPlayer  player;
	// ==================================================
	private final StatsCounter _statsCounter;
	// ==================================================
	private LocalPlayerStatsProvider(@NotNull LocalPlayer player) throws NullPointerException {
		this.player        = Objects.requireNonNull(player);
		this._statsCounter = player.getStats();
	}
	// ==================================================
	public final @Override LocalPlayer getPlayer() { return this.player; }
	// --------------------------------------------------
	public final @Override <T> int getIntValue(Stat<T> stat) { return this._statsCounter.getValue(stat); }
	public final @Override <T> int getIntValue(@NotNull StatType<T> type, @NotNull T subject) { return this._statsCounter.getValue(type, subject); }
	// ==================================================
	public final @Override int hashCode() { return this.player.hashCode(); }
	public final @Override boolean equals(Object obj) {
		if(obj == this) return true;
		if(obj == null || obj.getClass() != getClass()) return false;
		return (((LocalPlayerStatsProvider)obj).player == this.player);
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
