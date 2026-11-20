package com.thecsdev.commonmc.world.sandbox2;

import net.minecraft.world.Difficulty;
import net.minecraft.world.level.storage.WritableLevelData;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

/**
 * {@link WritableLevelData} implementation for {@link SandboxLevel}.
 */
final @ApiStatus.Internal class SandboxLevelData implements WritableLevelData
{
	// ==================================================
	private @NotNull RespawnData spawnPoint = RespawnData.DEFAULT;
	// ==================================================
	public final @Override @NotNull RespawnData getRespawnData() { return this.spawnPoint; }
	public final @Override void setSpawn(@NonNull RespawnData respawnData) {
		this.spawnPoint = Objects.requireNonNull(respawnData);
	}
	// --------------------------------------------------
	public final @Override long getGameTime() { return 0; }
	public final @Override boolean isHardcore() { return false; }
	// --------------------------------------------------
	public final @Override @NotNull Difficulty getDifficulty() { return Difficulty.HARD; }
	public final @Override boolean isDifficultyLocked() { return true; }
	// ==================================================
}
