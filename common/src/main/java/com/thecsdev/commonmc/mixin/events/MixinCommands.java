package com.thecsdev.commonmc.mixin.events;

import com.mojang.brigadier.CommandDispatcher;
import com.thecsdev.commonmc.api.events.CommandEvent;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Commands.class)
public abstract class MixinCommands
{
	// ==================================================
	private @Final @Shadow CommandDispatcher<CommandSourceStack> dispatcher;
	// ==================================================
	@Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/CommandDispatcher;setConsumer(Lcom/mojang/brigadier/ResultConsumer;)V"))
	private void onInit(Commands.CommandSelection sel, CommandBuildContext ctx, CallbackInfo ci) {
		CommandEvent.INIT_COMMANDS.invoker().invoke(dispatcher, ctx, sel);
	}
	// ==================================================
}
