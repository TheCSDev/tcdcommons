package com.thecsdev.commonmc.world.sandbox;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.RecipeAccess;
import net.minecraft.world.item.crafting.RecipePropertySet;
import net.minecraft.world.item.crafting.SelectableRecipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Barebones {@link RecipeAccess} implementation for a {@link SandboxLevel}.
 */
@ApiStatus.Internal
final class SandboxLevelRecipes implements RecipeAccess
{
	// ==================================================
    public final Map<ResourceKey<RecipePropertySet>, RecipePropertySet> itemSets           = new HashMap<>();
    public final SelectableRecipe.SingleInputSet<StonecutterRecipe>     stonecutterRecipes = new SelectableRecipe.SingleInputSet<>(new LinkedList<>());
	// ==================================================
    public @NotNull RecipePropertySet propertySet(ResourceKey<RecipePropertySet> resourceKey) {
        return (RecipePropertySet)this.itemSets.getOrDefault(resourceKey, RecipePropertySet.EMPTY);
    }
    public SelectableRecipe.@NotNull SingleInputSet<StonecutterRecipe> stonecutterRecipes() {
        return this.stonecutterRecipes;
    }
	// ==================================================
}
