package com.thecsdev.commonmc.mixin.hooks;

import net.minecraft.stats.Stat;
import net.minecraft.stats.StatFormatter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Stat.class)
public interface AccessorStat
{
	public @Accessor("formatter") StatFormatter getFormatter();
}
