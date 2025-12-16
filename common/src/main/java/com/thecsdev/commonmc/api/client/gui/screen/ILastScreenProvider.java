package com.thecsdev.commonmc.api.client.gui.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

/**
 * This {@code interface} is implemented by {@link TScreen}s that
 * can provide information about the last {@link Screen} instance
 * that was open before them.
 * <p>
 * This is generally used to open the previous screen when closing
 * the current one.
 *
 * @see Minecraft#setScreen(Screen)
 * @see TScreen#close()
 */
public interface ILastScreenProvider
{
	// ==================================================
	/**
	 * Returns the last {@link Screen} instance that was open before this one.
	 */
	public @Nullable Screen getLastScreen();
	// ==================================================
	/**
	 * A utility method that retrieves the last {@link Screen}
	 * from a {@link TScreen} instance.
	 * @param screen The {@link TScreen} instance.
	 */
	@Contract("null -> null; _ -> _")
	public static @Nullable Screen getLastScreen(@Nullable TScreen screen) {
		if(screen == null) return null;
		return (screen instanceof ILastScreenProvider lsp) ? lsp.getLastScreen() : getLastScreen(screen.getAsScreen());
	}

	/**
	 * A utility method that retrieves the last {@link Screen}
	 * from a {@link Screen} instance.
	 * @param screen The {@link Screen} instance.
	 */
	@Contract("null -> null; _ -> _")
	public static @Nullable Screen getLastScreen(@Nullable Screen screen) {
		if(screen == null) return null;
		return (screen instanceof ILastScreenProvider lsp) ? lsp.getLastScreen() : null;
	}
	// ==================================================
}
