package com.thecsdev.commonmc.resources;

import com.thecsdev.common.util.annotations.Reflected;
import com.thecsdev.commonmc.TCDCommons;
import com.thecsdev.commonmc.api.client.gui.misc.TTextureElement;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;

import static com.thecsdev.commonmc.TCDCommons.MOD_ID;
import static net.minecraft.resources.Identifier.fromNamespaceAndPath;


/**
 * {@link TCDCommons}'s {@link Identifier}s for {@link TextureAtlasSprite}s.
 * @see AtlasIds
 * @see TextureAtlasSprite
 * @see TTextureElement.Mode#GUI_SPRITE
 */
public class TCDCSprites
{
	// ==================================================
	private TCDCSprites() {}
	// ==================================================
	public static final Identifier gui_widget_dropdownCollapsed() { return fromNamespaceAndPath(MOD_ID, "widget/dropdown_collapsed"); }
	public static final Identifier gui_widget_dropdownExpanded() { return fromNamespaceAndPath(MOD_ID, "widget/dropdown_expanded"); }
	// --------------------------------------------------
	public static final Identifier gui_popup_ctxmenu() { return fromNamespaceAndPath(MOD_ID, "popup/ctxmenu"); }
	public static final Identifier gui_popup_ctxmenuHighlighted() { return fromNamespaceAndPath(MOD_ID, "popup/ctxmenu_highlighted"); }
	public static final Identifier gui_popup_shadow() { return fromNamespaceAndPath(MOD_ID, "popup/shadow"); }
	// --------------------------------------------------
	public static final Identifier gui_icon_clipboard() { return fromNamespaceAndPath(MOD_ID, "icon/clipboard"); }
	// --------------------------------------------------
	public static final            Identifier gui_icon_fsFile() { return fromNamespaceAndPath(MOD_ID, "icon/fs_file"); }
	public static final            Identifier gui_icon_fsFileImage() { return fromNamespaceAndPath(MOD_ID, "icon/fs_file_image"); }
	public static final            Identifier gui_icon_fsFileTxt() { return fromNamespaceAndPath(MOD_ID, "icon/fs_file_txt"); }
	public static final @Reflected Identifier gui_icon_fsFolder() { return fromNamespaceAndPath(MOD_ID, "icon/fs_folder"); }
	public static final            Identifier gui_icon_fsFolderGray() { return fromNamespaceAndPath(MOD_ID, "icon/fs_folder_gray"); }
	// ==================================================
}
