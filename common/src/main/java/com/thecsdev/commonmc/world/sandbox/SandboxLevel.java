package com.thecsdev.commonmc.world.sandbox;

import com.thecsdev.common.util.annotations.Virtual;
import net.minecraft.core.*;
import net.minecraft.core.particles.ExplosionParticleInfo;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.random.WeightedList;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.attribute.EnvironmentAttributeSystem;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragonPart;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.RecipeAccess;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.FuelValues;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkSource;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Barebones minimal {@link Level} implementation.
 */
@ApiStatus.Internal
@SuppressWarnings("NullableProblems")
public @Virtual class SandboxLevel extends Level
{
	// ==================================================
	private static final FeatureFlagSet             FEATURES       = FeatureFlagSet.of(FeatureFlags.VANILLA);
	private static final EnvironmentAttributeSystem ENV_ATTR_SYS   = EnvironmentAttributeSystem.builder().build();
	private static final DimensionType              DIMENSION_TYPE = new DimensionType(
			true, true, false, 1D, 0, 32, 32, BlockTags.INFINIBURN_OVERWORLD, 1f,
			new DimensionType.MonsterSettings(UniformInt.of(0, 7), 0),
			DimensionType.Skybox.OVERWORLD, DimensionType.CardinalLightType.DEFAULT,
			EnvironmentAttributeMap.EMPTY, HolderSet.empty()
	);
	// --------------------------------------------------
	protected @NotNull TransientEntitySectionManager<Entity> entityManager   = new TransientEntitySectionManager<>(Entity.class, new LevelCallback<>() {
		public void onCreated(Entity object) {}
		public void onDestroyed(Entity object) {}
		public void onTickingStart(Entity object) {}
		public void onTickingEnd(Entity object) {}
		public void onTrackingStart(Entity object) {}
		public void onTrackingEnd(Entity object) {}
		public void onSectionChange(Entity object) {}
	});
	protected @NotNull List<EnderDragonPart>                 dragonParts     = new ArrayList<>();
	protected @NotNull TickRateManager                       tickRateManager = new TickRateManager();
	protected @NotNull Scoreboard                            scoreboard      = new Scoreboard();
	protected @NotNull RecipeAccess                          recipeAccess    = new SandboxLevelRecipes();
	protected @NotNull PotionBrewing                         potionBrewing   = new PotionBrewing.Builder(FEATURES).build();
	protected @NotNull FuelValues                            fuelValues      = new FuelValues.Builder(registryAccess(), FEATURES).build();
	protected @NotNull ChunkSource                           chunkSource     = new SandboxLevelChunks(this);
	protected @NotNull List<Player>                          players         = new ArrayList<>();
	protected @NotNull WorldBorder                           worldBorder     = new WorldBorder();
	// ==================================================
	/**
	 * Primary {@link SandboxLevel} instance that is used {@link ApiStatus.Internal}ly.
	 */
	public static final SandboxLevel INSTANCE = new SandboxLevel();
	// ==================================================
	public SandboxLevel() {
		this(new SandboxLevelData(), Level.OVERWORLD, DynamicRegistryAccess.LEVEL, new Holder.Direct<>(DIMENSION_TYPE), true, true, 0, 1);
	}
	public SandboxLevel(WritableLevelData properties, ResourceKey<Level> registryRef, RegistryAccess registryManager, Holder<DimensionType> dimensionEntry, boolean isClient, boolean debugWorld, long seed, int maxChainedNeighborUpdates) {
		super(properties, registryRef, registryManager, dimensionEntry, isClient, debugWorld, seed, maxChainedNeighborUpdates);
	}
	// ==================================================
	public @Virtual @Override void sendBlockUpdated(BlockPos position, BlockState oldState, BlockState newState, int flags) {}
	public @Virtual @Override void playSeededSound(@Nullable Entity source, double x, double y, double z, Holder<SoundEvent> sound, SoundSource category, float volume, float pitch, long speed) {}
	public @Virtual @Override void playSeededSound(@Nullable Entity source, Entity entity, Holder<SoundEvent> sound, SoundSource category, float volume, float pitch, long speed) {}
	public @Virtual @Override void explode(@Nullable Entity entity, @Nullable DamageSource damageSource, @Nullable ExplosionDamageCalculator behavior, double x, double y, double z, float power, boolean createFire, ExplosionInteraction explosionSourceType, ParticleOptions smallParticle, ParticleOptions largeParticle, WeightedList<ExplosionParticleInfo> blockParticles, Holder<SoundEvent> soundEvent) {}
	public @Virtual @Override String gatherChunkSourceStats() {
        return "Chunks[C] W: " + getChunkSource().gatherStats() + " E: " + this.entityManager.gatherStats();
	}
	public @Virtual @Override void setRespawnData(LevelData.RespawnData spawnPoint) {}
	public @Virtual @Override LevelData.RespawnData getRespawnData() { return getLevelData().getRespawnData(); }
	public @Virtual @Override @Nullable Entity getEntity(int entityId) { return getEntities().get(entityId); }
	public @Virtual @Override Collection<EnderDragonPart> dragonParts() { return this.dragonParts; }
	public @Virtual @Override TickRateManager tickRateManager() { return this.tickRateManager; }
	public @Virtual @Override @Nullable MapItemSavedData getMapData(MapId mapId) { return null; }
	public @Virtual @Override void destroyBlockProgress(int entityId, BlockPos position, int progress) {}
	public @Virtual @Override Scoreboard getScoreboard() { return this.scoreboard; }
	public @Virtual @Override RecipeAccess recipeAccess() { return this.recipeAccess; }
	protected @Virtual @Override LevelEntityGetter<Entity> getEntities() { return this.entityManager.getEntityGetter(); }
	public @Virtual @Override EnvironmentAttributeSystem environmentAttributes() { return ENV_ATTR_SYS; }
	public @Virtual @Override PotionBrewing potionBrewing() { return this.potionBrewing; }
	public @Virtual @Override FuelValues fuelValues() { return this.fuelValues; }
	public @Virtual @Override ChunkSource getChunkSource() { return this.chunkSource; }
	public @Virtual @Override void levelEvent(@Nullable Entity source, int eventId, BlockPos position, int data) {}
	public @Virtual @Override void gameEvent(Holder<GameEvent> event, Vec3 emitterPosition, GameEvent.Context emitter) {}
	public @Virtual @Override List<? extends Player> players() { return this.players; }
	public @Virtual @Override Holder<Biome> getUncachedNoiseBiome(int biomeX, int biomeY, int biomeZ) { return registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.PLAINS); }
	public @Virtual @Override int getSeaLevel() { return 0; }
	public @Virtual @Override FeatureFlagSet enabledFeatures() { return FEATURES; }
	public @Virtual @Override float getShade(Direction direction, boolean shaded) { return 0; }
	public @Virtual @Override WorldBorder getWorldBorder() { return this.worldBorder; }
	public @Virtual @Override LevelTickAccess<Block> getBlockTicks() { return BlackholeTickAccess.emptyLevelList(); }
	public @Virtual @Override LevelTickAccess<Fluid> getFluidTicks() { return BlackholeTickAccess.emptyLevelList(); }
	// ==================================================
}
