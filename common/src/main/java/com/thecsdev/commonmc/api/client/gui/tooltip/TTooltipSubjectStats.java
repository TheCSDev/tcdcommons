package com.thecsdev.commonmc.api.client.gui.tooltip;

import com.thecsdev.commonmc.api.stats.util.SubjectStats;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.thecsdev.commonmc.api.stats.IStatsProvider.getStatTypeName;
import static net.minecraft.network.chat.Component.literal;

/**
 * {@link TTooltip} that shows statistics related to a given {@link SubjectStats}.
 */
@Environment(EnvType.CLIENT)
final @ApiStatus.Internal class TTooltipSubjectStats extends TTooltipLabel
{
	// ==================================================
	private final @NotNull SubjectStats<?> stats;
	// ==================================================
	public TTooltipSubjectStats(@NotNull SubjectStats<?> stats) throws NullPointerException
	{
		//initialize fields
		this.stats = Objects.requireNonNull(stats);

		//construct the label tooltip text
		{
			//start constructing the tooltip text
			final var tt = literal("");
			tt.append(literal("").append(stats.getSubjectDisplayName()).withStyle(ChatFormatting.YELLOW)).append("\n");
			tt.append(literal(stats.getSubjectID().toString()).withStyle(ChatFormatting.GRAY));
			tt.append("\n\n");

			//add block stats
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

			//set the tooltip text
			textProperty().set(tt, TTooltipSubjectStats.class);
		}
	}
	// ==================================================
	/**
	 * Returns the {@link SubjectStats} related to this {@link TTooltipLabel}.
	 */
	public final @NotNull SubjectStats<?> getStats() { return this.stats; }
	// ==================================================
}
