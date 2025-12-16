package com.thecsdev.commonmc.api.client.gui.tooltip;

import com.thecsdev.commonmc.api.stats.util.CustomStat;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.thecsdev.commonmc.TCDCommonsConfig.FLAG_DEV_ENV;
import static net.minecraft.ChatFormatting.*;
import static net.minecraft.network.chat.Component.literal;

/**
 * {@link TTooltip} that shows statistics about a given "custom stat",
 * also known as "general stat", aka {@link Identifier}.
 */
final class TTooltipCustomStat extends TTooltipLabel
{
	// ==================================================
	private final @NotNull CustomStat stat;
	// ==================================================
	TTooltipCustomStat(@NotNull CustomStat stat) throws NullPointerException
	{
		//not null assertions
		this.stat = Objects.requireNonNull(stat);

		//construct the label tooltip text
		{
			//start constructing the tooltip text
			final var tt = literal("");
			tt.append(literal("").append(stat.getSubjectDisplayName()).withStyle(YELLOW)).append("\n");
			tt.append(literal("K: " + stat.getSubjectID()).withStyle(GRAY)).append("\n");
			tt.append(literal("V: " + stat.getSubject()).withStyle(GRAY));
			tt.append("\n\n");

			tt.append(literal(stat.getValueF()).withStyle(GOLD));
			if(FLAG_DEV_ENV) tt.append(literal(" (" + stat.getValue() + ")").withStyle(GRAY));

			//set tooltip text
			textProperty().set(tt, TTooltipCustomStat.class);
		}
	}
	// ==================================================
	/**
	 * Returns the {@link CustomStat} this tooltip is about.
	 */
	public final @NotNull CustomStat getStat() { return stat; }
	// ==================================================
}
