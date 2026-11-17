package com.thecsdev.commonmc.world.sandbox2;

import com.thecsdev.common.util.annotations.Virtual;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ExplosionParticleInfo;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.random.WeightedList;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.attribute.EnvironmentAttributeSystem;
import net.minecraft.world.clock.ClockInstance;
import net.minecraft.world.clock.ClockManager;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragonPart;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.crafting.RecipeAccess;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.LevelCallback;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.entity.TransientEntitySectionManager;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.ticks.BlackholeTickAccess;
import net.minecraft.world.ticks.LevelTickAccess;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.*;

import static com.thecsdev.commonmc.TCDCommons.MOD_ID;

/**
 * Barebones {@link Level} implementation for rendering {@link Entity}s outside
 * of a "real" world, such as on GUI screens.
 * @see SandboxLevels#createWorldAsync(ResourceManager)
 */
public @ApiStatus.Internal @Virtual class SandboxLevel extends Level
{
	// ==================================================
	/**
	 * The {@link ResourceKey} used to identify this sandbox {@link Level}.
	 * @implNote This is distinct from {@link Level#OVERWORLD}, as the sandbox
	 * does not represent the actual overworld.
	 */
	public static final ResourceKey<Level> DIMENSION = ResourceKey.create(
			Registries.DIMENSION, Identifier.fromNamespaceAndPath(MOD_ID, "sandbox"));
	// --------------------------------------------------
	/**
	 * The {@link FeatureFlagSet} used by this {@link SandboxLevel}.
	 */
	private static final FeatureFlagSet FEATURES = FeatureFlagSet.of(FeatureFlags.VANILLA);

	/**
	 * The {@link EnvironmentAttributeSystem} used by this {@link SandboxLevel}.
	 * @implNote No layers are defined, so environment attributes use their default values.
	 */
	private static final EnvironmentAttributeSystem ENV_ATTR_SYS = EnvironmentAttributeSystem.builder().build();

	/**
	 * The {@link #getSeaLevel()} used by this {@link SandboxLevel}.
	 */
	private static final int SEA_LEVEL = 63;

	/**
	 * Fallback {@link DimensionType} used when the dimension type registry
	 * does not contain the overworld.
	 */
	private static final DimensionType FALLBACK_DIMENSION_TYPE = new DimensionType(
			false,									//has fixed time
			true,									//has skylight
			false,									//has ceiling
			false,									//has ender dragon fight
			1.0D,									//coordinate scale
			-64,									//minimum Y
			384,									//height
			384,									//logical height
			HolderSet.empty(),						//infiniburn
			0.0F,									//ambient light
			new DimensionType.MonsterSettings(UniformInt.of(0, 7), 0),
			DimensionType.Skybox.OVERWORLD,
			CardinalLighting.Type.DEFAULT,
			EnvironmentAttributeMap.EMPTY,			//attributes
			HolderSet.direct(),						//timelines
			Optional.empty()						//default clock
	);
	// --------------------------------------------------
	protected @NotNull Holder<Biome>                         fallbackBiome   = resolveFallbackBiome();
	protected @NotNull TransientEntitySectionManager<Entity> entityManager   = new TransientEntitySectionManager<>(Entity.class, new LevelCallback<>() {
		public final @Override void onCreated(@NonNull Entity object) {}
		public final @Override void onDestroyed(@NonNull Entity object) {}
		public final @Override void onTickingStart(@NonNull Entity object) {}
		public final @Override void onTickingEnd(@NonNull Entity object) {}
		public final @Override void onTrackingStart(@NonNull Entity object) {}
		public final @Override void onTrackingEnd(@NonNull Entity object) {}
		public final @Override void onSectionChange(@NonNull Entity object) {}
	});
	protected @NotNull ClockInstance                         clockInstance   = new ClockInstance() {
		public final @Override long totalTicks() { return 0; }
		public final @Override float partialTick() { return 0; }
		public final @Override float rate() { return 0; }
		public final @Override boolean isPaused() { return true; }
	};
	protected @NotNull ClockManager                          clockManager    = _ -> this.clockInstance;
	protected @NotNull List<EnderDragonPart>                 dragonParts     = new ArrayList<>();
	protected @NotNull TickRateManager                       tickRateManager = new TickRateManager();
	protected @NotNull Scoreboard                            scoreboard      = new Scoreboard();
	protected @NotNull RecipeAccess                          recipeAccess    = new SandboxLevelRecipes();
	protected @NotNull ChunkSource                           chunkSource     = new SandboxLevelChunks(this);
	protected @NotNull List<Player>                          players         = new ArrayList<>();
	protected @NotNull WorldBorder                           worldBorder     = new WorldBorder();
	// ==================================================
	public SandboxLevel(@NotNull RegistryAccess registryAccess) throws NullPointerException {
		this(
				new SandboxLevelData(),
				DIMENSION,
				Objects.requireNonNull(registryAccess),
				resolveDimensionType(registryAccess),
				true,   //client-sided
				false,  //debug world
				0L,     //biome zoom seed
				1);     //maximum chained neighbor updates
	}
	// --------------------------------------------------
	public SandboxLevel(
			WritableLevelData levelData,
			ResourceKey<Level> dimension,
			RegistryAccess registryAccess,
			Holder<DimensionType> dimensionType,
			boolean isClientSide,
			boolean isDebug,
			long biomeZoomSeed,
			int maxChainedNeighborUpdates)
	{
		super(levelData, dimension, registryAccess, dimensionType, isClientSide, isDebug, biomeZoomSeed, maxChainedNeighborUpdates);
	}
	// ==================================================
	public @Virtual @Override void sendBlockUpdated(@NonNull BlockPos position, @NonNull BlockState oldState, @NonNull BlockState newState, int flags) {}
	public @Virtual @Override void playSeededSound(@Nullable Entity source, double x, double y, double z, @NonNull Holder<SoundEvent> sound, @NonNull SoundSource category, float volume, float pitch, long speed) {}
	public @Virtual @Override void playSeededSound(@Nullable Entity source, @NonNull Entity entity, @NonNull Holder<SoundEvent> sound, @NonNull SoundSource category, float volume, float pitch, long speed) {}
	public @Virtual @Override void explode(@Nullable Entity entity, @Nullable DamageSource damageSource, @Nullable ExplosionDamageCalculator behavior, double x, double y, double z, float power, boolean createFire, @NonNull ExplosionInteraction explosionSourceType, @NonNull ParticleOptions smallParticle, @NonNull ParticleOptions largeParticle, @NonNull WeightedList<ExplosionParticleInfo> blockParticles, @NonNull Holder<SoundEvent> soundEvent) {}
	public @Virtual @Override @NonNull String gatherChunkSourceStats() {
		return "Chunks[C] W: " + getChunkSource().gatherStats() + " E: " + this.entityManager.gatherStats();
	}
	public @Virtual @Override void setRespawnData(LevelData.@NonNull RespawnData spawnPoint) {
		((WritableLevelData) getLevelData()).setSpawn(spawnPoint);
	}
	public @Virtual @Override LevelData.@NonNull RespawnData getRespawnData() { return getLevelData().getRespawnData(); }
	public @Virtual @Override @Nullable Entity getEntity(int entityId) { return getEntities().get(entityId); }
	public @Virtual @Override @NonNull Collection<EnderDragonPart> dragonParts() { return this.dragonParts; }
	public @Virtual @Override @NonNull TickRateManager tickRateManager() { return this.tickRateManager; }
	public @Virtual @Override @Nullable MapItemSavedData getMapData(@NonNull MapId mapId) { return null; }
	public @Virtual @Override void destroyBlockProgress(int entityId, @NonNull BlockPos position, int progress) {}
	public @Virtual @Override @NonNull Scoreboard getScoreboard() { return this.scoreboard; }
	public @Virtual @Override @NonNull RecipeAccess recipeAccess() { return this.recipeAccess; }
	protected @Virtual @Override @NonNull LevelEntityGetter<Entity> getEntities() { return this.entityManager.getEntityGetter(); }
	public @Virtual @Override @NonNull ClockManager clockManager() { return this.clockManager; }
	public @Virtual @Override @NonNull EnvironmentAttributeSystem environmentAttributes() { return ENV_ATTR_SYS; }
	public @Virtual @Override @NonNull ChunkSource getChunkSource() { return this.chunkSource; }
	public @Virtual @Override void levelEvent(@Nullable Entity source, int eventId, @NonNull BlockPos position, int data) {}
	public @Virtual @Override void gameEvent(@NonNull Holder<GameEvent> event, @NonNull Vec3 emitterPosition, GameEvent.@NonNull Context emitter) {}
	public @Virtual @Override @NonNull List<? extends Player> players() { return this.players; }
	public @Virtual @Override @NonNull Holder<Biome> getUncachedNoiseBiome(int biomeX, int biomeY, int biomeZ) { return this.fallbackBiome; }
	public @Virtual @Override int getSeaLevel() { return SEA_LEVEL; }
	public @Virtual @Override @NonNull FeatureFlagSet enabledFeatures() { return FEATURES; }
	public @Virtual @Override @NonNull WorldBorder getWorldBorder() { return this.worldBorder; }
	public @Virtual @Override @NonNull LevelTickAccess<Block> getBlockTicks() { return BlackholeTickAccess.emptyLevelList(); }
	public @Virtual @Override @NonNull LevelTickAccess<Fluid> getFluidTicks() { return BlackholeTickAccess.emptyLevelList(); }
	// ==================================================
	/**
	 * Returns the "fallback" {@link Biome} used by this {@link SandboxLevel}'s chunks.
	 */
	final @NotNull Holder<Biome> getFallbackBiome() { return this.fallbackBiome; }
	// ==================================================
	/**
	 * Resolves the fallback {@link Biome} used by this {@link SandboxLevel}.
	 */
	private final @NotNull Holder<Biome> resolveFallbackBiome() {
		final var biomes = registryAccess().lookupOrThrow(Registries.BIOME);
		return biomes.get(Biomes.THE_VOID).or(biomes::getAny).orElseThrow();
	}
	// --------------------------------------------------
	/**
	 * Resolves the {@link DimensionType} used by this {@link SandboxLevel}.
	 * @param registryAccess The {@link RegistryAccess} to obtain the {@link DimensionType} from.
	 */
	private static final @NotNull Holder<DimensionType> resolveDimensionType(
			@NotNull RegistryAccess registryAccess)
	{
		final @Nullable Holder<DimensionType> overworld = registryAccess
				.lookupOrThrow(Registries.DIMENSION_TYPE)
				.get(BuiltinDimensionTypes.OVERWORLD)
				.orElse(null);
		return (overworld != null) ? overworld : Holder.direct(FALLBACK_DIMENSION_TYPE);
	}
	// ==================================================
}
