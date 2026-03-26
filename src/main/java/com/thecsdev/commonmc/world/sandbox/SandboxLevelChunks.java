package com.thecsdev.commonmc.world.sandbox;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.BooleanSupplier;

/**
 * Barebones {@link ChunkSource} implementation for {@link SandboxLevel}.
 */
@ApiStatus.Internal
final class SandboxLevelChunks extends ChunkSource
{
	// ==================================================
	private final @NotNull SandboxLevel level;
	private final @NotNull LevelLightEngine lightEngine;
	// --------------------------------------------------
	private final ChunkAccess chunk_0_0;
	// ==================================================
	public SandboxLevelChunks(@NotNull SandboxLevel level) {
		this.level       = Objects.requireNonNull(level);
		this.lightEngine = new LevelLightEngine(this, true, level.dimensionType().hasSkyLight());
		this.chunk_0_0   = new EmptyLevelChunk(
				level, ChunkPos.ZERO, level.registryAccess()
						.getOrThrow(Registries.BIOME).value()
						.getOrThrow(Biomes.PLAINS));
	}
	// ==================================================
	public final @Override @Nullable ChunkAccess getChunk(int i, int j, ChunkStatus chunkStatus, boolean bl) { return this.chunk_0_0; }
	public final @Override void tick(BooleanSupplier booleanSupplier, boolean bl) {}
	public final @Override @NotNull String gatherStats() { return "0, 0"; }
	public final @Override int getLoadedChunksCount() { return 0; }
	public final @Override @NotNull LevelLightEngine getLightEngine() { return this.lightEngine; }
	public final @Override @NotNull BlockGetter getLevel() { return this.level; }
	// ==================================================
}
