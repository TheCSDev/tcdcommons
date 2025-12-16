package com.thecsdev.commonmc.api.client.gui.tooltip;

import com.thecsdev.commonmc.api.stats.util.EntityStats;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.thecsdev.commonmc.api.stats.StatsProvider.getStatTypeName;
import static net.minecraft.network.chat.Component.literal;

/**
 * {@link TTooltip} that shows statistics about a given {@link EntityType}.
 */
@ApiStatus.Internal
final class TTooltipEntityStats extends TTooltipLabel
{
	// ==================================================
	private final @NotNull EntityStats stats;
	// ==================================================
	TTooltipEntityStats(@NotNull EntityStats stats) throws NullPointerException
	{
		//not null assertions
		this.stats = Objects.requireNonNull(stats);

		//construct the label tooltip text
		{
			//start constructing the tooltip text
			final var tt = literal("");
			tt.append(literal("").append(stats.getSubjectDisplayName()).withStyle(ChatFormatting.YELLOW)).append("\n");
			tt.append(literal(stats.getSubjectID().toString()).withStyle(ChatFormatting.GRAY));
			tt.append("\n\n");

			//add item stats
			final var entries = stats.getValuesF().entrySet().iterator();
			while(entries.hasNext()) {
				final var entry  = entries.next();
				final var stName = getStatTypeName(entry.getKey().getType());
				tt.append(literal("- ").withStyle(ChatFormatting.YELLOW));
				tt.append(stName);
				tt.append(literal(" - ").withStyle(ChatFormatting.YELLOW));
				tt.append(literal(entry.getValue()).withStyle(ChatFormatting.GOLD));
				if(entries.hasNext()) tt.append("\n");
			}

			//set tooltip text
			textProperty().set(tt, TTooltipEntityStats.class);
		}
	}
	// ==================================================
	/**
	 * Returns the {@link EntityStats} this tooltip is about.
	 */
	public final @NotNull EntityStats getStats() { return stats; }
	// ==================================================
}
