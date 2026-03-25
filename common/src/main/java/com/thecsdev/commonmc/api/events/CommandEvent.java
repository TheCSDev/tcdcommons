package com.thecsdev.commonmc.api.events;

import com.mojang.brigadier.CommandDispatcher;
import com.thecsdev.common.event.Event;
import com.thecsdev.common.event.Events;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.jetbrains.annotations.NotNull;

/**
 * {@link Event}s related to commands.
 */
public interface CommandEvent
{
	// ==================================================
	/**
	 * <b>Trigger:</b> Initialization of {@link Commands}.<br>
	 * <b>Thread:</b> Unknown | Main (server) (likely)
	 * @see Commands#Commands(Commands.CommandSelection, CommandBuildContext)
	 */
	Event<InitCommands> INIT_COMMANDS = Events.createLoop();
	// ==================================================
	/**
	 * {@link Event} handler type for {@link #INIT_COMMANDS}.
	 */
	interface InitCommands {
		void invoke(@NotNull CommandDispatcher<CommandSourceStack> dispatcher,
					@NotNull CommandBuildContext buildContext,
		            @NotNull Commands.CommandSelection commandSelection);
	}
	// ==================================================
}
