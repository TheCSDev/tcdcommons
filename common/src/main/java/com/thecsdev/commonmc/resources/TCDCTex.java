package com.thecsdev.commonmc.resources;

import com.thecsdev.commonmc.TCDCommons;
import com.thecsdev.commonmc.api.client.gui.misc.TTextureElement;
import net.minecraft.resources.Identifier;

import static com.thecsdev.commonmc.TCDCommons.MOD_ID;
import static net.minecraft.resources.Identifier.fromNamespaceAndPath;

/**
 * {@link TCDCommons}'s {@link Identifier}s for textures.
 * @see TTextureElement.Mode#TEXTURE
 */
public final class TCDCTex
{
	// ==================================================
	private TCDCTex() {}
	// ==================================================
	public static final Identifier gui_ctxmenu() { return fromNamespaceAndPath(MOD_ID, "textures/gui/sprites/popup/ctxmenu.png"); }
	// ==================================================
}
