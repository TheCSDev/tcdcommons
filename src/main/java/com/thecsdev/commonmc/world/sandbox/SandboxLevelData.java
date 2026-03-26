package com.thecsdev.commonmc.world.sandbox;

import net.minecraft.world.Difficulty;
import net.minecraft.world.level.storage.WritableLevelData;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * {@link WritableLevelData} implementation for the {@link SandboxLevel}.
 */
@ApiStatus.Internal
final class SandboxLevelData implements WritableLevelData
{
	// ==================================================
	private @NotNull RespawnData spawnPoint = RespawnData.DEFAULT;
	// ==================================================
	public final @Override @NotNull RespawnData getRespawnData() { return this.spawnPoint; }
	public final @Override void setSpawn(RespawnData respawnData) {
		this.spawnPoint = (respawnData != null) ? respawnData : RespawnData.DEFAULT;
	}
	public final @Override long getGameTime() { return 0; }
	public final @Override boolean isHardcore() { return false; }
	public final @Override @NotNull Difficulty getDifficulty() { return Difficulty.HARD; }
	public final @Override boolean isDifficultyLocked() { return true; }
	// ==================================================
}
