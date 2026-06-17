package com.thecsdev.commonmc.api.client.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This {@code interface} is implemented by {@link TScreen}s that
 * can provide information about the last {@link Screen} instance
 * that was open before them.
 * <p>
 * This is generally used to open the previous screen when closing
 * the current one.
 *
 * @see Gui#setScreen(Screen)
 * @see TScreen#close()
 */
@Environment(EnvType.CLIENT)
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
	/**
	 * A utility method that retrieves the current {@link ILastScreenProvider} instance
	 * for a {@link Minecraft} client.
	 * <p>
	 * This method checks if the current screen is an instance of {@link ILastScreenProvider},
	 * and if not, it checks if it's an instance of {@link TScreenWrapper} that wraps an
	 * {@link ILastScreenProvider}.
	 *
	 * @param client The {@link Minecraft} client.
	 * @return Any {@link ILastScreenProvider} associated with the current {@link Gui#screen()}, if any.
	 * @throws NullPointerException If the argument is {@code null}.
	 */
	public static @Nullable ILastScreenProvider getCurrent(
			@NotNull Minecraft client) throws NullPointerException {
		if(client.gui.screen() instanceof ILastScreenProvider lsp)
			return lsp;
		else if(client.gui.screen() instanceof TScreenWrapper<?> tsw && tsw.getTargetTScreen() instanceof ILastScreenProvider lsp)
			return lsp;
		return null;
	}
	// ==================================================
}
