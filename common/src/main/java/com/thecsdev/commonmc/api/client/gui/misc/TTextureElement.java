package com.thecsdev.commonmc.api.client.gui.misc;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.thecsdev.common.properties.IntegerProperty;
import com.thecsdev.common.properties.NotNullProperty;
import com.thecsdev.common.util.annotations.Virtual;
import com.thecsdev.commonmc.api.client.gui.TElement;
import com.thecsdev.commonmc.api.client.gui.render.TGuiGraphics;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED;
import static net.minecraft.client.renderer.texture.TextureManager.INTENTIONAL_MISSING_TEXTURE;

/**
 * A {@link TElement} whose sole purpose is to render a sprite/texture.
 */
public @Virtual class TTextureElement extends TElement
{
	// ================================================== ==================================================
	//                                    TTextureElement IMPLEMENTATION
	// ================================================== ==================================================
	private final NotNullProperty<RenderPipeline>   renderPipeline = new NotNullProperty<>(GUI_TEXTURED);
	private final NotNullProperty<Identifier>       texture        = new NotNullProperty<>(INTENTIONAL_MISSING_TEXTURE);
	private final NotNullProperty<Mode>             mode           = new NotNullProperty<>(Mode.TEXTURE);
	private final IntegerProperty                   color          = new IntegerProperty(0xFFFFFFFF);
	// ==================================================
	public TTextureElement() { this(null); }
	public TTextureElement(@Nullable Identifier texture)
	{
		//initialize the texture
		if(texture != null) {
			this.texture.getHandle().set(texture);
			this.mode.getHandle().set(texture.getPath().endsWith(".png") ? Mode.TEXTURE : Mode.GUI_SPRITE);
		}
		//this element is not supposed to be focusable or hoverable by default
		focusableProperty().set(false, TTextureElement.class);
		hoverableProperty().set(false, TTextureElement.class);
	}
	// ==================================================
	/**
	 * Returns the {@link NotNullProperty} holding the {@link RenderPipeline}
	 * used to render this {@link TTextureElement}.
	 */
	public final NotNullProperty<RenderPipeline> renderPipelineProperty() { return this.renderPipeline; }

	/**
	 * Returns the {@link NotNullProperty} holding the sprite/texture.
	 */
	public final NotNullProperty<Identifier> textureProperty() { return this.texture; }

	/**
	 * Returns the {@link NotNullProperty} holding the rendering {@link Mode}
	 * that determines how this {@link TTextureElement} is rendered.
	 */
	public final NotNullProperty<Mode> modeProperty() {return this.mode; }

	/**
	 * Returns the {@link IntegerProperty} holding the sprite's color.
	 */
	public final IntegerProperty colorProperty() { return this.color; }
	// ==================================================
	public @Virtual @Override void renderCallback(@NotNull TGuiGraphics pencil)
	{
		//get necessary property values
		@NotNull  var pipeline = this.renderPipeline.get();
		@Nullable var sprite   = this.texture.get();
		          int color    = this.color.getI();

		//get bounding box and draw
		final var bb = getBounds();
		switch(this.mode.get()) {
			case TEXTURE -> pencil.drawTexture(pipeline, sprite, bb.x, bb.y, bb.width, bb.height, color);
			case GUI_SPRITE -> pencil.drawGuiSprite(pipeline, sprite, bb.x, bb.y, bb.width, bb.height, color);
		}
	}
	// ================================================== ==================================================
	//                                               Mode IMPLEMENTATION
	// ================================================== ==================================================
	/**
	 * The method used to render a {@link TTextureElement}.
	 * @see TGuiGraphics#drawTexture(RenderPipeline, Identifier, int, int, int, int, int)
	 * @see TGuiGraphics#drawGuiSprite(RenderPipeline, Identifier, int, int, int, int, int)
	 */
	public static enum Mode
	{
		/**
		 * The {@link TTextureElement} is rendered using {@link TGuiGraphics#drawTexture(Identifier, int, int, int, int, int)}.
		 */
		TEXTURE,

		/**
		 * The {@link TTextureElement} is rendered using {@link TGuiGraphics#drawGuiSprite(Identifier, int, int, int, int, int)}.
		 */
		GUI_SPRITE
	}
	// ================================================== ==================================================
}
