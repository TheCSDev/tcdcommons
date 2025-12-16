package com.thecsdev.commonmc.resources;

import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.data.AtlasIds;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.objects.AtlasSprite;
import net.minecraft.network.chat.contents.objects.ObjectInfo;
import net.minecraft.network.chat.contents.objects.PlayerSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.component.ResolvableProfile;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static com.thecsdev.commonmc.TCDCommons.MOD_ID;
import static net.minecraft.network.chat.Component.literal;
import static net.minecraft.network.chat.Component.object;

/**
 * Utility methods for creating {@link Component} instances, primarily for
 * {@link Component#object(ObjectInfo)}.
 */
public final class TComponent
{
	// ==================================================
	private TComponent() {}
	// ==================================================
	public static final MutableComponent air() { return literal("").append(object(new AtlasSprite(AtlasIds.GUI, Identifier.fromNamespaceAndPath(MOD_ID, "air")))); }
	public static final MutableComponent missingNo() { return literal("").append(object(new AtlasSprite(AtlasSprite.DEFAULT_ATLAS, TextureManager.INTENTIONAL_MISSING_TEXTURE))); }
	// --------------------------------------------------
	public static final MutableComponent block(@NotNull String texId) { return literal("").append(object(new AtlasSprite(AtlasIds.BLOCKS, Identifier.parse(texId)))); }
	public static final MutableComponent block(@NotNull Identifier texId) { return literal("").append(object(new AtlasSprite(AtlasIds.BLOCKS, texId))); }
	// --------------------------------------------------
	public static final MutableComponent item(@NotNull String texId) { return literal("").append(object(new AtlasSprite(AtlasIds.ITEMS, Identifier.parse(texId)))); }
	public static final MutableComponent item(@NotNull Identifier texId) { return literal("").append(object(new AtlasSprite(AtlasIds.ITEMS, texId))); }
	// --------------------------------------------------
	public static final MutableComponent painting(@NotNull String texId) { return literal("").append(object(new AtlasSprite(AtlasIds.PAINTINGS, Identifier.parse(texId)))); }
	public static final MutableComponent painting(@NotNull Identifier texId) { return literal("").append(object(new AtlasSprite(AtlasIds.PAINTINGS, texId))); }
	// --------------------------------------------------
	public static final MutableComponent particle(@NotNull String texId) { return literal("").append(object(new AtlasSprite(AtlasIds.PARTICLES, Identifier.parse(texId)))); }
	public static final MutableComponent particle(@NotNull Identifier texId) { return literal("").append(object(new AtlasSprite(AtlasIds.PARTICLES, texId))); }
	// --------------------------------------------------
	public static final MutableComponent gui(@NotNull String texId) { return literal("").append(object(new AtlasSprite(AtlasIds.GUI, Identifier.parse(texId)))); }
	public static final MutableComponent gui(@NotNull Identifier texId) { return literal("").append(object(new AtlasSprite(AtlasIds.GUI, texId))); }
	// --------------------------------------------------
	public static final MutableComponent sign(@NotNull String texId) { return literal("").append(object(new AtlasSprite(AtlasIds.SIGNS, Identifier.parse(texId)))); }
	public static final MutableComponent sign(@NotNull Identifier texId) { return literal("").append(object(new AtlasSprite(AtlasIds.SIGNS, texId))); }
	// --------------------------------------------------
	public static final MutableComponent head(@NotNull UUID uuid) { return literal("").append(object(new PlayerSprite(ResolvableProfile.createUnresolved(uuid), true))); }
	public static final MutableComponent head(@NotNull String username) { return literal("").append(object(new PlayerSprite(ResolvableProfile.createUnresolved(username), true))); }
	// ==================================================
}
