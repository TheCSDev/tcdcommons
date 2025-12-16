package com.thecsdev.commonmc.api.client.gui.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.protocol.game.ClientboundAwardStatsPacket;

/**
 * This {@code interface} is implemented by {@link Screen}s and {@link TScreen}s
 * that wish to receive callback method calls for whenever the server sends a
 * {@link ClientboundAwardStatsPacket} to the client.
 */
public interface IStatsListener
{
	// ==================================================
	/**
	 * Invoked automatically on {@link Minecraft}'s {@link Thread} whenever
	 * the server sends a {@link ClientboundAwardStatsPacket} to the client.
	 * @see Minecraft#execute(Runnable)
	 */
	public void statsReceivedCallback();
	// ==================================================
}
