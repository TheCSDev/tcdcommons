package com.thecsdev.commonmc.api.client.gui.render;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.cursor.CursorType;
import com.thecsdev.common.util.annotations.Virtual;
import com.thecsdev.commonmc.api.client.gui.TElement;
import com.thecsdev.commonmc.api.client.gui.screen.TScreen;
import com.thecsdev.commonmc.api.client.gui.screen.TScreenWrapper;
import com.thecsdev.commonmc.client.mixin.hooks.AccessorDrawContext;
import com.thecsdev.commonmc.client.mixin.hooks.AccessorGuiRenderer;
import com.thecsdev.commonmc.resources.TCDCSprites;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Objects;

import static com.thecsdev.commonmc.api.client.gui.util.TGuiUtils.getGuiRenderer;

/**
 * An extension of the game's {@link GuiGraphics}, featuring additional
 * rendering-related tools and utilities.<br>
 * Serves as an abstraction layer over the game's native {@link GuiGraphics}.
 *
 * @apiNote Screen coordinates use in-game units and not native LWJGL/GLFW raw pixels.
 */
public abstract class TGuiGraphics
{
	// ==================================================
	/**
	 * Creates a {@link TGuiGraphics} instance that will be used for GUI
	 * rendering and drawing.
	 * @param drawContext The native {@link GuiGraphics} object this {@link TGuiGraphics} is based on.
	 * @param mouseX The mouse X position. In-game coordinates.
	 * @param mouseY The mouse Y position. In-game coordinates.
	 * @param deltaTicks The rendering delta-time in game ticks.
	 */
	@ApiStatus.Internal
	public static final TGuiGraphics createInstance(@NotNull GuiGraphics drawContext, int mouseX, int mouseY, float deltaTicks) {
		return new TGuiGraphicsDefault(drawContext, mouseX, mouseY, deltaTicks);
	}
	// ==================================================
	private final GuiGraphics                    drawContext;
	private final Minecraft                      client;
	private final Matrix3x2fStack                matrices;
	private final MultiBufferSource.BufferSource bufferSource;
	// --------------------------------------------------
	private final int   mouseX, mouseY;
	private final float deltaTicks;
	private final int   screenW, screenH;
	// --------------------------------------------------
	private @Nullable TElement currentElement;
	// ==================================================
	protected TGuiGraphics(GuiGraphics drawContext, int mouseX, int mouseY, float deltaTicks)
	{
		final var aDrawContext = (AccessorDrawContext) Objects.requireNonNull(drawContext);
		this.drawContext       = drawContext;
		this.client            = aDrawContext.getClient();
		this.matrices          = aDrawContext.getMatrices();
		this.bufferSource      = ((AccessorGuiRenderer)(Object)getGuiRenderer(aDrawContext.getClient())).getVertexConsumers();
		this.mouseX            = mouseX;
		this.mouseY            = mouseY;
		this.deltaTicks        = deltaTicks;
		final var window       = this.client.getWindow();
		this.screenW           = window.getGuiScaledWidth();
		this.screenH           = window.getGuiScaledHeight();
	}
	// ==================================================
	/**
	 * Returns the game's native {@link GuiGraphics}.
	 * @apiNote May be unsafe and unstable to make draw calls directly to the game.
	 */
	@ApiStatus.Experimental
	public final GuiGraphics getNative() { return this.drawContext; }

	/**
	 * Returns the game's {@link Minecraft} instance.
	 */
	@ApiStatus.Experimental
	public final Minecraft getNativeClient() { return this.client; }

	/**
	 * Returns the game-{@link GuiRenderer}'s {@link MultiBufferSource.BufferSource}.
	 */
	@ApiStatus.Experimental
	public final MultiBufferSource.BufferSource getNativeBufferSource() { return this.bufferSource; }

	/**
	 * Returns the native {@link GuiRenderer}'s {@link Matrix3x2fStack}.
	 */
	@ApiStatus.Experimental
	public final Matrix3x2fStack getNativeMatrices() { return this.matrices; }
	// ==================================================
	/**
	 * Returns the mouse's X position on the screen, in the game's
	 * scaled screen-space coordinates.
	 */
	public final int getMouseX() { return this.mouseX; }

	/**
	 * Returns the mouse's Y position on the screen, in the game's
	 * scaled screen-space coordinates.
	 */
	public final int getMouseY() { return this.mouseY; }

	/**
	 * Returns the "delta-time" for this frame, but in ticks.
	 */
	public final float getDeltaTicks() { return this.deltaTicks; }

	/**
	 * Returns the (gui-scaled) width of the game window.
	 */
	public final int getScreenWidth() { return this.screenW; }

	/**
	 * Returns the (gui-scaled) height of the game window.
	 * @see Window#getGuiScaledWidth()
	 */
	public final int getScreenHeight() { return this.screenH; }
	// --------------------------------------------------
	/**
	 * Returns the {@link TElement} that is currently being rendered.
	 * @see Window#getGuiScaledHeight()
	 */
	public final @Nullable TElement getCurrentElement() { return this.currentElement; }
	// ==================================================
	/**
	 * Pushes a "scissor" onto the game's "scissor stack". Scissors allow you
	 * to define regions on the screen where rendering will take place. Any
	 * draw calls that take place outside the scissor bounds get cut off.
	 * @see #popScissors()
	 * @apiNote You <b>must</b> call {@link #popScissors()} once you're done!
	 */
	public final void pushScissors(int x, int y, int width, int height) { this.drawContext.enableScissor(x, y, x + width, y + height); }

	/**
	 * Pops a "scissor" from the game's "scissor stack", effectively disabling
	 * the previously pushed scissors.
	 * @throws IllegalStateException If the scissor stack is already empty.
	 * @see #pushScissors(int, int, int, int)
	 */
	public final void popScissors() throws IllegalStateException { this.drawContext.disableScissor(); }
	// ==================================================
	/**
	 * Draws the "missing texture" sprite that looks like a magenta-&amp;-black grid.
	 * @param x The sprite's X coordinate.
	 * @param y The sprite's Y coordinate.
	 * @param width The sprite's width.
	 * @param height The sprite's height.
	 * @param color The sprite's ARGB color.
	 */
	public @Virtual void drawMissingNo(int x, int y, int width, int height, int color) {
		drawGuiSprite(TextureManager.INTENTIONAL_MISSING_TEXTURE, x, y, width, height, color);
	}

	/**
	 * Draws a simple shadow effect around the given rectangle.
	 * @param x Rectangle X coordinate.
	 * @param y Rectangle Y coordinate.
	 * @param width Rectangle width.
	 * @param height Rectangle height.
	 * @param color Shadow ARGB color.
	 */
	public @Virtual void drawShadow(
			int x, int y, int width, int height,
			int offsetX, int offsetY,
			int blurRadius, int spreadRadius, int color,
			boolean inset)
	{
		if(inset) {
			//the blur radius determines how many layers to stack
			for(int i = 0; i < blurRadius; i++)
			{
				//calculate the effective spread. positive spread radius grows the shadow inside
				//the layers shrink based on 'i' and expand based on spread radius
				final int offset = i + 1 + (Math.max(spreadRadius, 0));
				//check if the current layer is still valid (not collapsing on itself)
				if(width - (offset * 2) > 0 && height - (offset * 2) > 0)
					drawGuiSprite(
							TCDCSprites.gui_popup_shadow(),
							x + offset + offsetX, y + offset + offsetY,
							width - (offset * 2), height - (offset * 2),
							color);
			}
		} else {
			//the blur radius determines how many layers to stack
			for(int i = 0; i < blurRadius; i++)
			{
				//used for blur simulation
				final int offset = i + 1;
				//effective spread includes both blur offset and the spreadRadius
				//positive spread radius expands the shadow outward
				final int currentSpread = offset + (Math.max(spreadRadius, 0));
				//draw the shadow sprite
				drawGuiSprite(
						TCDCSprites.gui_popup_shadow(),
						x - currentSpread + offsetX, y - currentSpread + offsetY,
						width + (currentSpread * 2), height + (currentSpread * 2),
						color);
			}
		}
	}
	// ==================================================
	/**
	 * Fills the given region with a solid rectangular color.
	 * @param x The rectangle's start X coordinate.
	 * @param y The rectangle's start Y coordinate.
	 * @param width The rectangle's width.
	 * @param height The rectangle's height.
	 * @param color The rectangle's ARGB color.
	 */
	public abstract void fillColor(int x, int y, int width, int height, int color);

	/**
	 * Draws a simple 1-unit thick outline (inwards facing).
	 * @param x Outline X coordinate.
	 * @param y Outline Y coordinate.
	 * @param width Outline width.
	 * @param height Outline height.
	 * @param color Outline ARGB color.
	 */
	public @Virtual void drawOutlineIn(int x, int y, int width, int height, int color) {
		fillColor(x, y, width, 1, color);                      //top side
		fillColor(x, y + 1, 1, height - 2, color);             //left side
		fillColor(x + width - 1, y + 1, 1, height - 2, color); //right side
		fillColor(x, y + height - 1, width, 1, color);         //bottom side
	}

	/**
	 * Draws a simple 1-unit thick outline (outwards facing).
	 * @param x Outline X coordinate.
	 * @param y Outline Y coordinate.
	 * @param width Outline width.
	 * @param height Outline height.
	 * @param color Outline ARGB color.
	 */
	public @Virtual void drawOutlineOut(int x, int y, int width, int height, int color) {
		fillColor(x - 1, y - 1, width + 2, 1, color);      //top side
		fillColor(x - 1, y, 1, height, color);             //left side
		fillColor(x + width, y, 1, height, color);         //right side
		fillColor(x - 1, y + height, width + 2, 1, color); //bottom side
	}
	// --------------------------------------------------
	/**
	 * Draws a rectangular texture from the loaded resource-packs.
	 * @param id The texture's {@link Identifier}.
	 * @param x X coordinate on the screen.
	 * @param y Y coordinate on the screen.
	 * @param width Width on the screen.
	 * @param height Height on the screen.
	 * @param color Texture ARGB color. It is recommended to use white (aka -1).
	 */
	public @Virtual void drawTexture(
			@NotNull Identifier id,
			int x, int y, int width, int height,
			int color) {
		this.drawTexture(id, x, y, width, height, 0, 0, 1, 1, 1, 1, color);
	}

	/**
	 * Draws a rectangular texture from the loaded resource-packs.
	 * @param id The texture's {@link Identifier}.
	 * @param x X coordinate on the screen.
	 * @param y Y coordinate on the screen.
	 * @param width Width on the screen.
	 * @param height Height on the screen.
	 * @param uvX X coordinate on the texture UV coordinates.
	 * @param uvY Y coordinate on the texture UV coordinates.
	 * @param uvWidth Width on the UV coordinates.
	 * @param uvHeight Height on the UV coordinates.
	 * @param textureWidth Texture image width.
	 * @param textureHeight Texture image height.
	 * @param color Texture ARGB color. It is recommended to use white (aka -1).
	 */
	public abstract void drawTexture(
			@NotNull Identifier id,
			int x, int y, int width, int height,
			float uvX, float uvY, int uvWidth, int uvHeight,
			int textureWidth, int textureHeight,
			int color);

	/**
	 * Draws a rectangular sprite using the game's GUI sprite system.
	 * @param id The sprite's {@link Identifier}.
	 * @param x Sprite X coordinate.
	 * @param y Sprite Y coordinate.
	 * @param width Sprite width.
	 * @param height Sprite height.
	 * @param color Sprite ARGB color. It is recommended to use white (aka -1).
	 * @see AtlasIds#GUI
	 */
	public abstract void drawGuiSprite(
			@NotNull Identifier id,
			int x, int y, int width, int height,
			int color);
	// --------------------------------------------------
	/**
	 * Draws a rectangular 9-slided texture from the loaded resource-packs.
	 * @param id The texture's {@link Identifier}.
	 * @param x X coordinate on the screen.
	 * @param y Y coordinate on the screen.
	 * @param width Width on the screen.
	 * @param height Height on the screen.
	 * @param u X coordinate on the texture UV coordinates.
	 * @param v Y coordinate on the texture UV coordinates.
	 * @param uvWidth Width on the UV coordinates.
	 * @param uvHeight Height on the UV coordinates.
	 * @param textureWidth Texture image width.
	 * @param textureHeight Texture image height.
	 * @param color Texture ARGB color. It is recommended to use white (aka -1).
	 * @param sliceSize The size of the sliced pieces around the centerpiece.
	 */
	public @Virtual void draw9SlicedTexture(
			@NotNull Identifier id,
			int x, int y, int width, int height,
			float u, float v, int uvWidth, int uvHeight,
			int textureWidth, int textureHeight,
			int color, int sliceSize)
	{
		//calculations
		final int s2 = sliceSize * 2;

		//draw 9-slice if possible...
		if(s2 < width || s2 < height)
		{
			//the four corners
			drawTexture(id, x, y, sliceSize, sliceSize, u, v, sliceSize, sliceSize, textureWidth, textureHeight, color);
			drawTexture(id, x + width - sliceSize, y, sliceSize, sliceSize, u + uvWidth - sliceSize, v, sliceSize, sliceSize, textureWidth, textureHeight, color);
			drawTexture(id, x, y + height - sliceSize, sliceSize, sliceSize, u, v + uvHeight - sliceSize, sliceSize, sliceSize, textureWidth, textureHeight, color);
			drawTexture(id, x + width - sliceSize, y + height - sliceSize, sliceSize, sliceSize, u + uvWidth - sliceSize, v + uvHeight - sliceSize, sliceSize, sliceSize, textureWidth, textureHeight, color);

			//the four sides
			drawTexture(id, x + sliceSize, y, width - s2, sliceSize, u + sliceSize, v, uvWidth - s2, sliceSize, textureWidth, textureHeight, color);
			drawTexture(id, x, y + sliceSize, sliceSize, height - s2, u, v + sliceSize, sliceSize, uvHeight - s2, textureWidth, textureHeight, color);
			drawTexture(id, x + width - sliceSize, y + sliceSize, sliceSize, height - s2, u + uvWidth - sliceSize, v + sliceSize, sliceSize, uvHeight - s2, textureWidth, textureHeight, color);
			drawTexture(id, x + sliceSize, y + height - sliceSize, width - s2, sliceSize, u + sliceSize, v + uvHeight - sliceSize, uvWidth - s2, sliceSize, textureWidth, textureHeight, color);

			//the middle
			drawRepeatingTexture(id, x + sliceSize, y + sliceSize, width - s2, height - s2, u + sliceSize, v + sliceSize, uvWidth - s2, uvHeight - s2, textureWidth, textureHeight, color);
		}
		//...else the slicing is larger than the element itself - draw the full texture
		else drawTexture(id, x, y, width, height, u, v, uvWidth, uvHeight, textureWidth, textureHeight, color);
	}

	/**
	 * Draws a repeating texture in a given rectangular area.
	 * @param id The texture's {@link Identifier}.
	 * @param x X coordinate on the screen.
	 * @param y Y coordinate on the screen.
	 * @param width Width on the screen.
	 * @param height Height on the screen.
	 * @param u X coordinate on the texture UV coordinates.
	 * @param v Y coordinate on the texture UV coordinates.
	 * @param uvWidth Width on the UV coordinates.
	 * @param uvHeight Height on the UV coordinates.
	 * @param textureWidth Texture image width.
	 * @param textureHeight Texture image height.
	 * @param color Texture ARGB color. It is recommended to use white (aka -1).
	 */
	public @Virtual void drawRepeatingTexture(
			@NotNull Identifier id,
			int x, int y, int width, int height,
			float u, float v, int uvWidth, int uvHeight,
			int textureWidth, int textureHeight,
			int color)
	{
		int endX = x + width, endY = y + height;
		for(int y1 = y; y1 < endY; y1 += uvHeight)
			for(int x1 = x; x1 < endX; x1 += uvWidth)
			{
				int nextW = uvWidth, nextH = uvHeight;
				if(x1 + nextW > endX) nextW -= (x1 + nextW) - endX;
				if(y1 + nextH > endY) nextH -= (y1 + nextH) - endY;
				if(nextW < 1 || nextH < 1) continue;
				drawTexture(id, x1, y1, nextW, nextH, u, v, nextW, nextH, textureWidth, textureHeight, color);
			}
	}
	// --------------------------------------------------
	/**
	 * Draws a button.
	 * @param x The X coordinate.
	 * @param y The Y coordinate.
	 * @param width The width.
	 * @param height The height.
	 * @param color The texture color. It is recommended to use white (aka -1).
	 * @param enabled Is the button enabled and clickable?
	 * @param highlighted Is the button hovered or selected?
	 */
	public abstract void drawButton(int x, int y, int width, int height, int color, boolean enabled, boolean highlighted);

	/**
	 * Draws a checkbox.
	 * @param x The X coordinate.
	 * @param y The Y coordinate.
	 * @param width The width.
	 * @param height The height.
	 * @param color The texture color. It is recommended to use white (aka -1).
	 * @param enabled Is the checkbox enabled and clickable?
	 * @param highlighted Is the checkbox hovered or selected?
	 * @param checked Is the checkbox checked?
	 */
	public abstract void drawCheckbox(int x, int y, int width, int height, int color, boolean enabled, boolean highlighted, boolean checked);
	// ==================================================
	/**
	 * Renders an item.
	 * @param item The {@link ItemStack} to render.
	 * @param x The X coordinate.
	 * @param y The Y coordinate.
	 * @param width The width.
	 * @param height The height.
	 */
	public @Virtual void renderItem(@NotNull ItemStack item, int x, int y, int width, int height) {
		final var mat = getNativeMatrices();
		mat.pushMatrix();
		mat.translate(x, y);
		mat.scale((float) width / 16, (float) height / 16);
		getNative().renderFakeItem(item, 0, 0);
		mat.popMatrix();
	}
	// ==================================================
	/**
	 * Computes the size the on-screen size an {@link Entity} should have given
	 * the viewport width and height.<br>
	 * This size is used for {@link InventoryScreen#renderEntityInInventoryFollowsMouse(GuiGraphics, int, int, int, int, int, float, float, float, LivingEntity)}.
	 */
	@ApiStatus.Internal
	static final int computeEntitySize(@NotNull Entity entity, int viewportW, int viewportH) {
		//calculations
		final int maxSize = Math.min(viewportW, viewportH);
		if(maxSize == 0) return 5; //can't have zeros or too small outputs
		//calculate max gui size
		int result; {
			float  modelWidth  = entity.getType().getWidth();
			float  modelHeight = entity.getType().getHeight();
			double hypotenuse  = Math.sqrt((modelWidth * modelWidth) + (modelHeight * modelHeight));
			if(hypotenuse == 0) hypotenuse = 0.1;
			result = (int) (maxSize / hypotenuse);
		}
		//return the result
		return Math.max(result, 5); //cant have too small outputs
	}

	/**
	 * Renders an entity.
	 * @param entity The {@link Entity} to render.
	 * @param x The X coordinate.
	 * @param y The Y coordinate.
	 * @param width The width.
	 * @param height The height.
	 * @param scale The on-screen scale of the {@link Entity}.
	 * @param followsCursor Whether the {@link Entity}'s on-screen rotation follows the mouse cursor.
	 * @throws Exception Some entities are incompatible with the idea of being rendered on a GUI
	 * screen. Such entities may {@code throw} when attempting to render them. Should this happen,
	 * you are to <b>CEASE</b> attempting to render said {@link Entity}. The type of {@link Throwable}
	 * such entities can {@code throw} can be literally anything.
	 */
	@SuppressWarnings("RedundantThrows") //not redundant - incompatible entities can and do throw
	public @Virtual void renderEntity(
			@NotNull Entity entity,
			int x, int y, int width, int height,
			double scale, boolean followsCursor) throws Exception
	{
		Objects.requireNonNull(entity);
		if(width == 0 || height == 0) return;
		renderEntityInInventoryFollowsMouse(
				x, y, x + width, y + height,
				(int) (computeEntitySize(entity, width, height) * scale),
				followsCursor ? getMouseX() : (x + width),
				followsCursor ? getMouseY() : (y + height),
				entity);
	}

	/**
	 * Modified implementation of {@link InventoryScreen#renderEntityInInventoryFollowsMouse(GuiGraphics, int, int, int, int, int, float, float, float, LivingEntity)}
	 * that supports rendering entities of any type.
	 */
	final @ApiStatus.Internal void renderEntityInInventoryFollowsMouse(
			int x1, int y1, int x2, int y2, int size,
			float mouseX, float mouseY,
			@NotNull Entity entity)
	{
		final @Nullable var le = (entity instanceof LivingEntity livingEntity) ? livingEntity : null;

		float n = (float)(x1 + x2) / 2.0F;
		float o = (float)(y1 + y2) / 2.0F;
		float p = (float)Math.atan((double)((n - mouseX) / 40.0F));
		float q = (float)Math.atan((double)((o - mouseY) / 40.0F));
		Quaternionf quaternionf = (new Quaternionf()).rotateZ((float)Math.PI);
		Quaternionf quaternionf2 = (new Quaternionf()).rotateX(q * 20.0F * ((float)Math.PI / 180F));
		quaternionf.mul(quaternionf2);

		float old_yRot      = entity.getYRot();
		float old_xRot      = entity.getXRot();
		float old_yBodyRot  = 0;
		float old_yHeadRot0 = 0;
		float old_yHeadRot  = 0;
		if(le != null) {
			old_yBodyRot = le.yBodyRot;
			old_yHeadRot0 = le.yHeadRotO;
			old_yHeadRot = le.yHeadRot;
		}

		entity.setYRot(180.0F + p * 40.0F);
		entity.setXRot(-q * 20.0F);
		if(le != null) {
			le.yBodyRot = 180.0F + p * 20.0F;
			le.yHeadRot = entity.getYRot();
			le.yHeadRotO = entity.getYRot();
		}

		float w = (le != null) ? le.getScale() : 1;
		Vector3f vector3f = new Vector3f(0.0F, entity.getBbHeight() / 2.0F + (float) 0.03125 * w, 0.0F);
		float nextScale = (float)size / w;

		renderEntityInInventory(x1, y1, x2, y2, nextScale, vector3f, quaternionf, quaternionf2, entity);

		entity.setYRot(old_yRot);
		entity.setXRot(old_xRot);
		if(le != null) {
			le.yBodyRot = old_yBodyRot;
			le.yHeadRotO = old_yHeadRot0;
			le.yHeadRot = old_yHeadRot;
		}
	}

	/**
	 * Modified implementation of {@link InventoryScreen#renderEntityInInventoryFollowsMouse(GuiGraphics, int, int, int, int, int, float, float, float, LivingEntity)}
	 * that supports rendering entities of any type.
	 */
	@SuppressWarnings("ConstantValue") //incorrect, they are not constant and can be null
	final @ApiStatus.Internal void renderEntityInInventory(
			int x1, int y1, int x2, int y2,
			float scale,
			@NotNull Vector3f translation, @NotNull Quaternionf rotation,
			@Nullable Quaternionf overrideCameraAngle,
			@NotNull Entity entity)
	{
		final @NotNull  var entityRenderDispatcher = getNativeClient().getEntityRenderDispatcher();
		final @Nullable var entityRenderer         = entityRenderDispatcher.getRenderer(entity);
		if(entityRenderer == null) return;
		final @NotNull  var entityRenderState = entityRenderer.createRenderState(entity, 1.0F);
		entityRenderState.lightCoords = 15728880;
		entityRenderState.shadowPieces.clear();
		entityRenderState.outlineColor = 0;
		getNative().submitEntityRenderState(entityRenderState, scale, translation, rotation, overrideCameraAngle, x1, y1, x2, y2);
	}
	// ==================================================
	/**
	 * Renders a {@link TScreen} and all of its children recursively,
	 * as well as some additional screen-related stuff like tooltips.
	 * @param screen The {@link TScreen} to draw.
	 * @apiNote Not necessary for you to use this. Primarily used by
	 * {@link TScreenWrapper} when rendering {@link TScreen}s.
	 */
	@ApiStatus.Internal
	public final void renderTScreen(@NotNull TScreen screen)
	{
		// ---------- SCREEN RENDERING
		renderTElement(screen, screen);

		// ---------- TOOLTIP RENDERING
		final @Nullable var focus = screen.focusedElementProperty().get();
		final @Nullable var hover = screen.hoveredElementProperty().get();
		renderTooltip(focus);
		if(focus != hover) renderTooltip(hover);

		// ---------- CURSOR RENDERING
		getNative().requestCursor(hover != null ? hover.getCursor().getNative() : CursorType.DEFAULT);
	}

	/**
	 * Renders a {@link TElement} and all of its children recursively.
	 * @param element The {@link TElement} to draw.
	 */
	@ApiStatus.Internal
	private final void renderTElement(@NotNull TElement element, TScreen expectedScreen)
	{
		//code stability assertions
		assert element.screenProperty().get() == expectedScreen || //assert proper screen tracking
				element == expectedScreen ||                       //ignore screens (root elements)
				expectedScreen == null                             //ignore tooltips
			: (
				"Illegal " + TScreen.class.getSimpleName() + " value for " + element + ". " +
				"Expected " + expectedScreen + ", got " + element.screenProperty().get() + "."
			);

		//optimization - skip invisible and off-screen elements
		if(!element.visibleProperty().get())
			return; //draw child only if *IT* is visible (ignore (grand)parents)
		final var elBB = element.getBounds(); //element's bounding box
		if(elBB.isEmpty || elBB.x > this.screenW || elBB.y > this.screenH || elBB.endX < 0 || elBB.endY < 0)
			return; //draw child only if it has a valid size and is on-screen

		//keep track of last current element (important)
		final var lastCurrentElement = this.currentElement;

		//render the element
		this.currentElement = element;
		element.renderCallback(this);

		//draw the element's children
		{
			//push parent scissors
			final boolean clips = element.clipsDescendantsProperty().get();
			if(clips) pushScissors(elBB.x, elBB.y, elBB.width, elBB.height);

			//iterate children and make draw calls
			for(final var child : element)
				renderTElement(child, expectedScreen);

			//pop parent scissors once done
			if(clips) popScissors();
		}

		//post-render callback for the element, and then pop the matrix stack
		this.currentElement = element; //IMPORTANT: Reassign. Keep track of the element being rendered.
		element.postRenderCallback(this);

		//revert current-element once all is done
		this.currentElement = lastCurrentElement;
	}
	// --------------------------------------------------
	/**
	 * Renders a {@link TElement}'s tooltip.
	 * @param element The element whose tooltip is to be rendered
	 * @apiNote Do not call this yourself. Use {@link TElement#tooltipProperty()} instead.
	 */
	@ApiStatus.Internal
	private final void renderTooltip(@Nullable TElement element)
	{
		//obtain the tooltip element
		if(element == null || !element.isVisible()) return;
		final var root    = element.rootProperty().get(); //for tooltip positioning
		final var tooltip = element.getTooltip();
		if(tooltip == null) return;

		//obtain the tooltip bounding box and transform the matrix
		final var screenBounds  = root.getBounds();
		final var elementBounds = element.getBounds();
		final var tooltipBounds = tooltip.getBounds();

		final var matrices      = getNativeMatrices();
		matrices.pushMatrix();
		matrices.translate(-tooltipBounds.x, -tooltipBounds.y);
		if(element.isFocused()) //tooltip positioner for focused elements
		{
			//initial matrix offset, placing the tooltip below the element
			int offsetX = elementBounds.x, offsetY = elementBounds.endY + 1;
			//move the tooltip to the left if it doesn't fit horizontally
			if(offsetX + tooltipBounds.width > screenBounds.endX)
				offsetX = elementBounds.endX - tooltipBounds.width;
			//move the tooltip above the element if it doesn't fit vertically
			if(offsetY + tooltipBounds.height > screenBounds.endY)
				offsetY = elementBounds.y - tooltipBounds.height - 1;
			//finally, apply the matrix translation
			matrices.translate(offsetX, offsetY);
		}
		else //tooltip positioner for hovered elements
		{
			//initial matrix offset, placing the tooltip to the right of the cursor
			int offsetX = this.mouseX + 8, offsetY = this.mouseY;
			//move the tooltip to the left of the cursor if it doesn't fit horizontally
			if(offsetX + tooltipBounds.width > screenBounds.endX)
				offsetX = this.mouseX - tooltipBounds.width - 1;
			//move the tooltip above the cursor if it doesn't fit vertically
			if(offsetY + tooltipBounds.height > screenBounds.endY)
				offsetY = this.mouseY - tooltipBounds.height - 1;
			//finally, apply the matrix translation
			matrices.translate(offsetX, offsetY);
		}

		//render the tooltip and pop the matrix
		renderTElement(tooltip, null);
		matrices.popMatrix();
	}
	// ==================================================
}
