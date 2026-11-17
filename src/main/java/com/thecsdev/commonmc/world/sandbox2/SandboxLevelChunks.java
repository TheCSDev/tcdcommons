package com.thecsdev.commonmc.world.sandbox2;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.Objects;
import java.util.function.BooleanSupplier;

/**
 * Barebones {@link ChunkSource} implementation for {@link SandboxLevel}.
 * <p>
 * Represents a fully generated "void" world. Empty chunks are created on demand
 * at the requested {@link ChunkPos}, and cached for subsequent lookups.
 */
final @ApiStatus.Internal class SandboxLevelChunks extends ChunkSource
{
	// ==================================================
	/**
	 * The maximum number of {@link EmptyLevelChunk}s to cache.
	 */
	private static final int MAX_CACHED_CHUNKS = 32;
	// --------------------------------------------------
	private final @NotNull SandboxLevel     level;
	private final @NotNull LevelLightEngine lightEngine;
	private final @NotNull Holder<Biome>    biome;
	// --------------------------------------------------
	private final @NotNull Long2ObjectMap<EmptyLevelChunk> chunks = new Long2ObjectOpenHashMap<>();
	// ==================================================
	SandboxLevelChunks(@NotNull SandboxLevel level) throws NullPointerException {
		this.level       = Objects.requireNonNull(level);
		this.lightEngine = new LevelLightEngine(this, true, level.dimensionType().hasSkyLight());
		this.biome       = level.getFallbackBiome();
	}
	// ==================================================
	public final @Override @NonNull ChunkAccess getChunk(int chunkX, int chunkZ, @NonNull ChunkStatus chunkStatus, boolean loadOrGenerate)
	{
		//obtain cached chunk
		final     long key   = ChunkPos.pack(chunkX, chunkZ);
		@Nullable var  chunk = this.chunks.get(key);

		//create and cache the chunk if not already present
		if(chunk == null) {
			//clear the cached chunks if filled up too much
			if(this.chunks.size() >= MAX_CACHED_CHUNKS)
				this.chunks.clear();
			//create and cache new empty chunk instance
			this.chunks.put(key, chunk = new EmptyLevelChunk(this.level, new ChunkPos(chunkX, chunkZ), this.biome));
		}

		//return the chunk
		return chunk;
	}
	public final @Override void tick(@NonNull BooleanSupplier booleanSupplier, boolean bl) {}
	public final @Override @NotNull String gatherStats() { return Integer.toString(this.chunks.size()); }
	public final @Override int getLoadedChunksCount() { return this.chunks.size(); }
	public final @Override @NotNull LevelLightEngine getLightEngine() { return this.lightEngine; }
	public final @Override @NotNull BlockGetter getLevel() { return this.level; }
	// ==================================================
}
