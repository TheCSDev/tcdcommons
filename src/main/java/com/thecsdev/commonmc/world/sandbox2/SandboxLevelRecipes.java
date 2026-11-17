package com.thecsdev.commonmc.world.sandbox2;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.RecipeAccess;
import net.minecraft.world.item.crafting.RecipePropertySet;
import net.minecraft.world.item.crafting.SelectableRecipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

/**
 * {@link RecipeAccess} implementation for {@link SandboxLevel}.
 */
final @ApiStatus.Internal class SandboxLevelRecipes implements RecipeAccess
{
	// ==================================================
	private static final SelectableRecipe.SingleInputSet<StonecutterRecipe> STONECUTTER_RECIPES =
			SelectableRecipe.SingleInputSet.empty();
	// ==================================================
	public final @Override @NotNull RecipePropertySet propertySet(@NonNull ResourceKey<RecipePropertySet> resourceKey) {
		return RecipePropertySet.EMPTY;
	}
	public final @Override SelectableRecipe.@NotNull SingleInputSet<StonecutterRecipe> stonecutterRecipes() {
		return STONECUTTER_RECIPES;
	}
	// ==================================================
}
