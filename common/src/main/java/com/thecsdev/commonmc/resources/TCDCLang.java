package com.thecsdev.commonmc.resources;

import com.thecsdev.common.util.annotations.Reflected;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.ApiStatus;

import static net.minecraft.network.chat.Component.translatable;

/**
 * Language texts for {@link com.thecsdev.commonmc.TCDCommons}.
 */
@ApiStatus.Internal
public final class TCDCLang
{
	// ==================================================
	private TCDCLang() {}
	// ==================================================
	public static final @Reflected MutableComponent config_propertyValue() { return translatable("tcdcommons.config.property_value"); }
	public static final @Reflected MutableComponent config_common() { return translatable("tcdcommons.config.common"); }
	public static final @Reflected MutableComponent config_client() { return translatable("tcdcommons.config.client"); }
	public static final @Reflected MutableComponent config_client_updateItemGroupsOnJoin() { return translatable("tcdcommons.config.client.update_item_groups_on_join"); }
	public static final @Reflected MutableComponent config_client_updateItemGroupsOnJoin_tooltip() { return translatable("tcdcommons.config.client.update_item_groups_on_join.tooltip"); }
	public static final @Reflected MutableComponent config_server() { return translatable("tcdcommons.config.server"); }
	// --------------------------------------------------
	public static final MutableComponent gui_screen_textDialog_defaultTitle() { return translatable("tcdcommons.gui.screen.text_dialog.default_title"); }
	public static final MutableComponent gui_screen_textDialog_errorTitle() { return translatable("tcdcommons.gui.screen.text_dialog.error_title"); }
	public static final MutableComponent gui_dropdown_defaultLabel() { return translatable("tcdcommons.gui.dropdown.default_label"); }
	public static final MutableComponent gui_fileChooser_mode_explore() { return translatable("tcdcommons.gui.filechooser.mode.explore"); }
	public static final MutableComponent gui_fileChooser_mode_chooseFile() { return translatable("tcdcommons.gui.filechooser.mode.choose_file"); }
	public static final MutableComponent gui_fileChooser_mode_createFile() { return translatable("tcdcommons.gui.filechooser.mode.create_file"); }
	public static final MutableComponent gui_fileChooser_quickAccess() { return translatable("tcdcommons.gui.filechooser.quick_access"); }
	public static final MutableComponent gui_fileChooser_quickAccess_mountPoints() { return translatable("tcdcommons.gui.filechooser.quick_access.mount_points"); }
	public static final MutableComponent gui_fileChooser_ctxmenu_openIn() { return translatable("tcdcommons.gui.filechooser.ctxmenu.open_in"); }
	public static final MutableComponent gui_fileChooser_ctxmenu_openIn_assocApp() { return translatable("tcdcommons.gui.filechooser.ctxmenu.open_in.assoc_app"); }
	public static final MutableComponent gui_fileChooser_action_inputFilename_placeholder() { return translatable("tcdcommons.gui.filechooser.action.input_filename.placeholder"); }
	// ==================================================
}
