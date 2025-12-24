package com.thecsdev.commonmc.api.client.gui.tooltip;

import com.thecsdev.commonmc.api.stats.util.ItemStats;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.thecsdev.commonmc.api.stats.IStatsProvider.getStatTypeName;
import static net.minecraft.network.chat.Component.literal;

/**
 * {@link TTooltip} that shows statistics about a given {@link Item}.
 */
@ApiStatus.Internal
final class TTooltipItemStats extends TTooltipLabel
{
	// ==================================================
	private final @NotNull ItemStats stats;
	// ==================================================
	TTooltipItemStats(@NotNull ItemStats stats) throws NullPointerException
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

			//add block stats if appropriate
			if(stats.getSubject() == Items.AIR || Block.byItem(stats.getSubject()) != Blocks.AIR) {
				final var bEntries = stats.getItemBlockStats().getValuesF().entrySet().iterator();
				if(bEntries.hasNext()) tt.append("\n");
				while(bEntries.hasNext()) {
					final var entry  = bEntries.next();
					final var stName = getStatTypeName(entry.getKey().getType());
					tt.append(literal("- ").withStyle(ChatFormatting.YELLOW));
					tt.append(stName);
					tt.append(literal(" - ").withStyle(ChatFormatting.YELLOW));
					tt.append(literal(entry.getValue()).withStyle(ChatFormatting.GOLD));
					if(bEntries.hasNext()) tt.append("\n");
				}
			}

			//set tooltip text
			textProperty().set(tt, TTooltipItemStats.class);
		}
	}
	// ==================================================
	/**
	 * Returns the {@link ItemStats} this tooltip is about.
	 */
	public final @NotNull ItemStats getStats() { return stats; }
	// ==================================================
}
