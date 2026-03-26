package com.thecsdev.commonmc.client.mixin.hooks;

import com.thecsdev.commonmc.api.client.gui.TElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = TElement.class, remap = false)
public interface AccessorTElement
{
	@Invoker("tick") void _tick();

	@Invoker("hoverGainedCallback") void _hoverGainedCallback();
	@Invoker("hoverLostCallback") void _hoverLostCallback();

	@Invoker("focusGainedCallback") void _focusGainedCallback();
	@Invoker("focusLostCallback") void _focusLostCallback();

	@Invoker("dragStartCallback") void _dragStartCallback();
	@Invoker("dragEndCallback") void _dragEndCallback();
}
