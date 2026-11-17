package com.thecsdev.commonmc.world.sandbox2;

import com.thecsdev.common.util.TUtils;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagLoader;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Stream;

/**
 * Utilities for creating {@link SandboxLevel}s from data pack resources.
 * @apiNote If a {@link RegistryAccess} is already available, use
 * {@link SandboxLevel#SandboxLevel(RegistryAccess)} instead of loading the registries again.
 */
public final @ApiStatus.Internal class SandboxLevels
{
	// ==================================================
	private SandboxLevels() {}
	// ==================================================
	/**
	 * Loads resources and creates a new {@link SandboxLevel} asynchronously.
	 * @param resources The {@link ResourceManager} to load resources from.
	 * @throws NullPointerException If the argument is {@code null}.
	 * @see #createWorldAsync(ResourceManager, Executor)
	 */
	public static final @NotNull CompletableFuture<SandboxLevel> createWorldAsync(
			@NotNull ResourceManager resources) throws NullPointerException {
		return createWorldAsync(resources, TUtils.getVirtualThreadPerTaskExecutor());
	}

	/**
	 * Loads data pack registries and creates a new {@link SandboxLevel} asynchronously.
	 * @param resources The {@link ResourceManager} to load resources from.
	 * @param executor The {@link Executor} used for resource loading and level creation.
	 * @throws NullPointerException If an argument is {@code null}.
	 */
	public static final @NotNull CompletableFuture<SandboxLevel> createWorldAsync(
			@NotNull ResourceManager resources, @NotNull Executor executor) throws NullPointerException
	{
		//not null requirements
		Objects.requireNonNull(resources);
		Objects.requireNonNull(executor);

		//obtain static registries, and prepare their data pack tags off-thread
		return CompletableFuture.supplyAsync(() ->
		{
			//prerequisites
			final var staticAccess = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
			final var pendingTags  = TagLoader.loadTagsForExistingRegistries(resources, staticAccess);
			final var context      = TagLoader.buildUpdatedLookups(staticAccess, pendingTags);

			//load dynamic registries using the prepared context
			return RegistryDataLoader
					.load(resources, context, RegistryDataLoader.WORLD_REGISTRIES, executor)
					.thenApplyAsync(loaded ->
					{
						//merge static and dynamic registries
						final var registries = new RegistryAccess.ImmutableRegistryAccess(
								Stream.concat(staticAccess.registries(), loaded.registries())
						).freeze();
						//create and return the resulting level
						return new SandboxLevel(registries);
					}, executor);
		}, executor)
		.thenCompose(future -> future);
	}
	// ==================================================
}
