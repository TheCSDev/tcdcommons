package com.thecsdev.commonmc.mixin.events;

import com.thecsdev.commonmc.api.events.CreativeModeTabEvent;
import com.thecsdev.commonmc.api.world.item.TItemUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.CreativeModeTabs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CreativeModeTabs.class, priority = 9001)
public abstract class MixinCreativeModeTabs
{
	@Inject(method = "tryRebuildTabContents", at = @At("RETURN"))
	private static void onRebuildTabContents(
			FeatureFlagSet enabledFeatures,
			boolean operatorEnabled,
			HolderLookup.Provider lookup,
			CallbackInfoReturnable<Boolean> callback)
	{
		//do nothing if the update does not take place
		if(!callback.getReturnValueZ()) return;

		//rebuild the internal map and invoke the event
		TItemUtils.rebuildI2TMap();
		CreativeModeTabEvent.REBUILD_CONTENTS_POST.invoker().invoke(enabledFeatures, operatorEnabled, lookup);
	}
}
