package com.thecsdev.commonmc.api.client.hooks;

import com.thecsdev.commonmc.client.mixin.hooks.AccessorButton;
import dev.architectury.event.events.client.ClientGuiEvent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Objects;
import java.util.function.BiConsumer;

import static java.util.Optional.ofNullable;

/**
 * Utility methods for hooking into GUI-related activities.
 */
public final class GuiHooks
{
	// ==================================================
	private static final HashMap<Class<?>, HashMap<Component, BiConsumer<Button, Runnable>>> VANILLA_BUTTON_MODS = new HashMap<>();
	// ==================================================
	private GuiHooks() {}
	static
	{
		//vanilla button modder
		ClientGuiEvent.INIT_POST.register((screen, access) ->
		{
			//obtain mods for the given
			final @Nullable var mods = VANILLA_BUTTON_MODS.get(screen.getClass());
			if(mods == null) return;

			//iterate all renderable elements and find button instances,
			//then mod them if necessary
			access.getRenderables().stream()
				.filter(el -> el instanceof Button)
				.forEach(el -> {
					//obtain button and its info
					final var btn    = (Button)el;
					final var btnTxt = btn.getMessage();

					//check if the button is a mod's target
					if(!mods.containsKey(btnTxt)) return;

					//obtain onclick info
					final var      btnAccess       = (AccessorButton)(Object)btn;
					final var      btn_onPress_old = btnAccess.getOnPress();
					final Runnable btn_onPress_new = () -> { if(btn_onPress_old != null) btn_onPress_old.onPress(btn); };
					final var      btn_onPress_mod = ofNullable(mods.get(btnTxt)).orElse((__, ___) -> {});

					//mod the button
					btnAccess.setOnPress(__ -> btn_onPress_mod.accept(btn, btn_onPress_new));
				});
		});
	}
	// ==================================================
	/**
	 * Registers a modification of a {@link Button}'s on-click functionality on for given
	 * vanilla {@link Screen}.
	 * @param screen The {@link Screen} where the {@link Button} is located.
	 * @param btnText The text of the targeted button.
	 * @param btnOnClickSupplier The {@link BiConsumer} whose inputs are the {@link Button}
	 * instance and its current (now old) on-click {@link Runnable}.
	 * @throws NullPointerException If an argument is {@code null}.
	 * @throws IllegalArgumentException If the provided {@link Class} is {@code abstract}.
	 */
	public static final void registerVanillaButtonMod(
			final @NotNull Class<? extends Screen> screen,
			final @NotNull Component btnText,
			final @NotNull BiConsumer<Button, Runnable> btnOnClickSupplier)
			throws NullPointerException, IllegalArgumentException {
		//not null requirements
		Objects.requireNonNull(screen);
		Objects.requireNonNull(btnText);
		Objects.requireNonNull(btnOnClickSupplier);
		//ensure the class fits the job
		if(Modifier.isAbstract(screen.getModifiers()))
			throw new IllegalArgumentException("Cannot register abstract class " + screen);
		//register the modification
		//noinspection ExcessiveLambdaUsage
		VANILLA_BUTTON_MODS
			.computeIfAbsent(screen, __ -> new HashMap<>())
			.computeIfAbsent(btnText, __ -> btnOnClickSupplier);
	}
	// ==================================================
}
