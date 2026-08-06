package com.thecsdev.commonmc.api.client.gui.tooltip;

import com.thecsdev.commonmc.api.stats.util.SubjectStats;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

import static com.thecsdev.commonmc.api.stats.IStatsProvider.getStatTypeName;
import static net.minecraft.ChatFormatting.*;
import static net.minecraft.network.chat.Component.literal;
import static net.minecraft.network.chat.Component.translatable;

/**
 * {@link TTooltip} that shows statistics related to a given {@link SubjectStats}.
 */
@Environment(EnvType.CLIENT)
final @ApiStatus.Internal class TTooltipSubjectStats extends TTooltipLabel
{
	// ==================================================
	private final @NotNull SubjectStats<?> stats;
	// ==================================================
	public TTooltipSubjectStats(@NotNull SubjectStats<?> stats, boolean showItemDescription)
			throws NullPointerException
	{
		//initialize fields
		this.stats = Objects.requireNonNull(stats);

		//construct the label tooltip text
		{
			//start constructing the tooltip text
			final var tt = literal("");
			tt.append(literal("").append(stats.getSubjectDisplayName()).withStyle(YELLOW)).append("\n");

			//add subject description
			if(showItemDescription && computeItemDescription(stats) instanceof MutableComponent desc)
				tt.append(desc.withStyle(GRAY)).append("\n");

			//add advanced tooltips
			if(Minecraft.getInstance().options.advancedItemTooltips)
				tt.append(literal(stats.getSubjectID().toString()).withStyle(DARK_GRAY)).append("\n");

			//add subject stats
			tt.append("\n");
			final var entries = stats.getValuesF().entrySet().iterator();
			while(entries.hasNext()) {
				final var entry  = entries.next();
				final var stName = getStatTypeName(entry.getKey().getType());
				tt.append(literal("- ").withStyle(YELLOW));
				tt.append(stName);
				tt.append(literal(": ").withStyle(YELLOW));
				tt.append(literal(entry.getValue()).withStyle(GOLD));
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
	/**
	 * Attempts to pull an "item description" for a given {@link SubjectStats#getSubject()}.
	 * @param stats The item to obtain the description for.
	 * @throws NullPointerException If the argument is {@code null}.
	 */
	@ApiStatus.Internal
	private static final @Nullable MutableComponent computeItemDescription(
			@NotNull SubjectStats<?> stats) throws NullPointerException
	{
		//general preparation for all cases
		final var lang = Language.getInstance();

		//handle items
		if(stats.getSubject() instanceof Item item)
			return (MutableComponent) Optional.empty()
					.or(() -> {
						final var sid  = stats.getSubjectID();
						final var key  = String.format("lore.%s.%s", sid.getNamespace(), sid.getPath());
						return Optional.ofNullable(lang.has(key) ? translatable(key) : null);
					})
					.or(() -> item.getDefaultInstance().tags()
							.map(TagKey::location)
							.map(id -> String.format("tag.%s.%s.description", id.getNamespace(), id.getPath()))
							.map(key -> lang.has(key) ? translatable(key) : null)
							.filter(Objects::nonNull)
							.findFirst()
					)
					.orElse(null);
		//handle blocks
		else if(stats.getSubject() instanceof Block block)
			return (MutableComponent) Optional.empty()
					.or(() -> {
						final var sid  = stats.getSubjectID();
						final var key  = String.format("lore.%s.%s", sid.getNamespace(), sid.getPath());
						return Optional.ofNullable(lang.has(key) ? translatable(key) : null);
					})
					.or(() -> block.defaultBlockState().tags()
							.map(TagKey::location)
							.map(id -> String.format("tag.%s.%s.description", id.getNamespace(), id.getPath()))
							.map(key -> lang.has(key) ? translatable(key) : null)
							.filter(Objects::nonNull)
							.findFirst()
					)
					.orElse(null);
		//handle entities
		else if(stats.getSubject() instanceof EntityType<?> entityType) {
			return (MutableComponent) Optional.empty()
					.or(() -> {
						final var sid  = stats.getSubjectID();
						final var key  = String.format("entity.%s.%s.description", sid.getNamespace(), sid.getPath());
						return Optional.ofNullable(lang.has(key) ? translatable(key) : null);
					})
					.or(() -> entityType.builtInRegistryHolder().tags()
							.map(TagKey::location)
							.map(id -> String.format("tag.%s.%s.description", id.getNamespace(), id.getPath()))
							.map(key -> lang.has(key) ? translatable(key) : null)
							.filter(Objects::nonNull)
							.findFirst()
					)
					.orElse(null);
		}
		//all other types return nothing
		else return null;
	}
	// ==================================================
}
