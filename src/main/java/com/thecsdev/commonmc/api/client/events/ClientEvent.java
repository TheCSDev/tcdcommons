package com.thecsdev.commonmc.api.client.events;

import com.thecsdev.common.event.Event;
import com.thecsdev.common.event.Events;
import com.thecsdev.commonmc.client.mixin.hooks.AccessorScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.NotNull;

/**
 * {@link Event}s related to the {@link Minecraft} client and {@link LocalPlayer}.
 */
@Environment(EnvType.CLIENT)
public interface ClientEvent
{
	// ==================================================
	/**
	 * <b>Trigger:</b> Whenever a {@link LocalPlayer} joins a level.<br>
	 * <b>Thread:</b> Main (client) &lt;- Scheduled from network thread
	 * @see ClientPacketListener#handleLogin(ClientboundLoginPacket)
	 */
	Event<LocalPlayerJoin> PLAYER_JOIN = Events.createLoop();

	/**
	 * <b>Trigger:</b> Whenever a {@link LocalPlayer} leaves a level.<br>
	 * <b>Thread:</b> Main (client)
	 * @see Minecraft#clearClientLevel(Screen)
	 */
	Event<LocalPlayerQuit> PLAYER_QUIT = Events.createLoop();
	// --------------------------------------------------
	/**
	 * <b>Trigger:</b> Whenever a {@link ClientLevel} is initialized.<br>
	 * <b>Thread:</b> Main (client)
	 * @see ClientLevel#ClientLevel(ClientPacketListener, ClientLevel.ClientLevelData, ResourceKey, Holder, int, int, LevelRenderer, boolean, long, int)
	 */
	Event<LevelInit> LEVEL_INIT = Events.createLoop();
	// --------------------------------------------------
	/**
	 * <b>Trigger:</b> Whenever a {@link Screen} is initialized.<br>
	 * <b>Thread:</b> Unknown
	 * @see Screen#init(int, int)
	 */
	Event<ScreenInit> SCREEN_INIT = Events.createLoop();
	// ==================================================
	/**
	 * {@link Event} handler type for {@link #PLAYER_JOIN}.
	 */
	interface LocalPlayerJoin { void invoke(@NotNull LocalPlayer localPlayer); }

	/**
	 * {@link Event} handler type for {@link #PLAYER_QUIT}.
	 */
	interface LocalPlayerQuit { void invoke(@NotNull LocalPlayer localPlayer); }
	// --------------------------------------------------
	/**
	 * {@link Event} handler type for {@link #LEVEL_INIT}.
	 */
	interface LevelInit { void invoke(@NotNull ClientLevel clientLevel); }
	// --------------------------------------------------
	/**
	 * {@link Event} handler type for {@link #SCREEN_INIT}.
	 */
	interface ScreenInit { void invoke(@NotNull Screen screen, @NotNull AccessorScreen accessor); }
	// ==================================================
}
