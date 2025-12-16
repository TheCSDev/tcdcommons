package com.thecsdev.commonmc.world.sandbox;

import com.mojang.serialization.Lifecycle;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.biome.BiomeData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.animal.chicken.ChickenVariants;
import net.minecraft.world.entity.animal.cow.CowVariants;
import net.minecraft.world.entity.animal.feline.CatVariants;
import net.minecraft.world.entity.animal.frog.FrogVariants;
import net.minecraft.world.entity.animal.nautilus.ZombieNautilusVariants;
import net.minecraft.world.entity.animal.pig.PigVariants;
import net.minecraft.world.entity.animal.wolf.WolfSoundVariants;
import net.minecraft.world.entity.animal.wolf.WolfVariants;
import net.minecraft.world.entity.decoration.painting.PaintingVariants;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.npc.villager.VillagerType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeSources;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * {@link RegistryAccess} implementation that dynamically resolves
 * {@link #lookup(ResourceKey)} and {@link #registries()}.
 */
@ApiStatus.Internal
public final class DynamicRegistryAccess implements RegistryAccess
{
	// ================================================== ==================================================
	//                              DynamicRegistryAccess IMPLEMENTATION
	// ================================================== ==================================================
	/**
	 * Static {@link DynamicRegistryAccess} instance that was bootstrapped using
	 * {@link #bootstrapLevel()}. Used for creating {@link Level} instances.
	 */
	public static final DynamicRegistryAccess LEVEL = new DynamicRegistryAccess().bootstrapLevel();
	// ==================================================
	private final WritableRegistry<WritableRegistry<?>> registries = new MappedRegistry<>(ResourceKey.createRegistryKey(Registries.ROOT_REGISTRY_NAME), Lifecycle.stable());
	// ==================================================
	public DynamicRegistryAccess() {
		//the root needs to be registered to itself
		Registry.register(this.registries, Registries.ROOT_REGISTRY_NAME, this.registries);
	}
	// ==================================================
	@SuppressWarnings("unchecked")
	public final @NotNull <E> Optional<WritableRegistry<E>> lookupM(@NotNull ResourceKey<? extends Registry<? extends E>> resourceKey) {
		if(!this.registries.containsKey(resourceKey.identifier()))
			Registry.register(this.registries, resourceKey.identifier(), new MappedRegistry<>((ResourceKey<? extends Registry<E>>) resourceKey, Lifecycle.stable()));
		return Optional.of((WritableRegistry<E>) this.registries.getValue(resourceKey.identifier()));
	}

	@SuppressWarnings("unchecked")
	public final @Override @NotNull <E> Optional<Registry<E>> lookup(ResourceKey<? extends Registry<? extends E>> resourceKey) {
		return (Optional<Registry<E>>) (Object) lookupM(resourceKey);
	}

	//@SuppressWarnings({"unchecked", "rawtypes"})
	public final @Override @NotNull Stream<RegistryEntry<?>> registries() {
		return Stream.empty(); //TODO - Implement
	}
	// ==================================================
	/**
	 * Bootstraps registries necessary for a <b>vanilla</b> {@link Level} instance.
	 */
	public DynamicRegistryAccess bootstrapLevel()
	{
		DamageTypes           .bootstrap(new GenericBootstrapContext<>(lookupM(Registries.DAMAGE_TYPE).orElseThrow()));
		BiomeSources          .bootstrap(lookupM(Registries.BIOME_SOURCE).orElseThrow());
		BiomeData             .bootstrap(new GenericBootstrapContext<>(lookupM(Registries.BIOME).orElseThrow()));
		CatVariants           .bootstrap(new GenericBootstrapContext<>(lookupM(Registries.CAT_VARIANT).orElseThrow()));
		PigVariants           .bootstrap(new GenericBootstrapContext<>(lookupM(Registries.PIG_VARIANT).orElseThrow()));
		ChickenVariants       .bootstrap(new GenericBootstrapContext<>(lookupM(Registries.CHICKEN_VARIANT).orElseThrow()));
		CowVariants           .bootstrap(new GenericBootstrapContext<>(lookupM(Registries.COW_VARIANT).orElseThrow()));
		FrogVariants          .bootstrap(new GenericBootstrapContext<>(lookupM(Registries.FROG_VARIANT).orElseThrow()));
		PaintingVariants      .bootstrap(new GenericBootstrapContext<>(lookupM(Registries.PAINTING_VARIANT).orElseThrow()));
		WolfVariants          .bootstrap(new GenericBootstrapContext<>(lookupM(Registries.WOLF_VARIANT).orElseThrow()));
		WolfSoundVariants     .bootstrap(new GenericBootstrapContext<>(lookupM(Registries.WOLF_SOUND_VARIANT).orElseThrow()));
		VillagerType          .bootstrap(lookupM(Registries.VILLAGER_TYPE).orElseThrow());
		VillagerProfession    .bootstrap(lookupM(Registries.VILLAGER_PROFESSION).orElseThrow());
		ZombieNautilusVariants.bootstrap(new GenericBootstrapContext<>(lookupM(Registries.ZOMBIE_NAUTILUS_VARIANT).orElseThrow()));
		return this;
	}
	// ================================================== ==================================================
	//                            GenericBootstrapContext IMPLEMENTATION
	// ================================================== ==================================================
	/**
	 * Generic {@link BootstrapContext} implementation that works with any {@link MappedRegistry}.
	 */
	@ApiStatus.Internal
	public static final class GenericBootstrapContext<T> implements BootstrapContext<T>
	{
		// ==================================================
		private final @NotNull WritableRegistry<T> registry;
		// ==================================================
		public GenericBootstrapContext(@NotNull WritableRegistry<T> registry) {
			this.registry = Objects.requireNonNull(registry);
		}
		// ==================================================
		@SuppressWarnings("unchecked")
		public @Override @NotNull <S> HolderGetter<S> lookup(ResourceKey<? extends Registry<? extends S>> resourceKey) {
			return (HolderGetter<S>) this.registry.createRegistrationLookup();
		}
		// --------------------------------------------------
		public @Override @NotNull Holder.Reference<T> register(ResourceKey<T> key, T value, Lifecycle lifecycle) {
			Registry.register(this.registry, key, value);
			return Holder.Reference.createStandAlone(this.registry, key);
		}
		// ==================================================
	}
	// ================================================== ==================================================
}
