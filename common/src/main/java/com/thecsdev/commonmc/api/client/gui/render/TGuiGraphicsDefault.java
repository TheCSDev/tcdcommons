package com.thecsdev.commonmc.api.client.gui.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import static net.minecraft.resources.Identifier.withDefaultNamespace;

/**
 * The default implementation of {@link TGuiGraphics}, using vanilla game's
 * mechanisms to render and draw GUI elements.
 *
 * @apiNote In the event of a mod-breaking update, this abstraction layer
 * allows for an easier rewrite and portability.
 */
@ApiStatus.Internal
final class TGuiGraphicsDefault extends TGuiGraphics
{
	// ==================================================
	public TGuiGraphicsDefault(GuiGraphics drawContext, int mouseX, int mouseY, float deltaTicks) {
		super(drawContext, mouseX, mouseY, deltaTicks);
	}
	// ==================================================
	public final @Override void fillColor(int x, int y, int width, int height, int color) {
		getNative().fill(x, y, x + width, y + height, color);
	}
	// --------------------------------------------------
	public final @Override void drawTexture(
			@NotNull RenderPipeline renderPipeline, @NotNull Identifier id,
			int x, int y, int width, int height,
			float uvX, float uvY, int uvWidth, int uvHeight,
			int textureWidth, int textureHeight, int color)
	{
		getNative().blit(renderPipeline, id, x, y, uvX, uvY, width, height, uvWidth, uvHeight, textureWidth, textureHeight, color);
	}

	public final @Override void drawGuiSprite(
			@NotNull RenderPipeline renderPipeline, @NotNull Identifier id,
			int x, int y, int width, int height, int color)
	{
		getNative().blitSprite(renderPipeline, id, x, y, width, height, color);
	}
	// --------------------------------------------------
	private static final @ApiStatus.Internal Identifier[] SPRITE_BUTTONS = new Identifier[] {
		withDefaultNamespace("widget/button"),
		withDefaultNamespace("widget/button_highlighted"),
		withDefaultNamespace("widget/button_disabled")
	};

	public final @Override void drawButton(int x, int y, int width, int height, int color, boolean enabled, boolean highlighted) {
		if(!enabled)         drawGuiSprite(SPRITE_BUTTONS[2], x, y, width, height, color);
		else if(highlighted) drawGuiSprite(SPRITE_BUTTONS[1], x, y, width, height, color);
		else                 drawGuiSprite(SPRITE_BUTTONS[0], x, y, width, height, color);
	}

	private static final @ApiStatus.Internal Identifier[] SPRITE_CHECKBOXES = new Identifier[] {
		withDefaultNamespace("widget/checkbox"),
		withDefaultNamespace("widget/checkbox_highlighted"),
		withDefaultNamespace("widget/checkbox_selected"),
		withDefaultNamespace("widget/checkbox_selected_highlighted")
	};

	public final @Override void drawCheckbox(int x, int y, int width, int height, int color, boolean enabled, boolean highlighted, boolean checked) {
		if(!enabled) drawGuiSprite(SPRITE_CHECKBOXES[0], x, y, width, height, color);
		else         drawGuiSprite(SPRITE_CHECKBOXES[checked ? (highlighted ? 3 : 2) : (highlighted ? 1 : 0)], x, y, width, height, color);
	}
	// ==================================================
}
