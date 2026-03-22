package com.thecsdev.commonmc.client.gui.screen;

import com.thecsdev.common.math.UDim2;
import com.thecsdev.commonmc.api.client.gui.misc.TFillColorElement;
import com.thecsdev.commonmc.api.client.gui.panel.TPanelElement;
import com.thecsdev.commonmc.api.client.gui.screen.TScreen;
import com.thecsdev.commonmc.api.client.gui.screen.TScreenPlus;
import com.thecsdev.commonmc.api.client.gui.screen.promise.TFileChooserScreen;
import com.thecsdev.commonmc.api.client.gui.widget.TButtonWidget;
import com.thecsdev.commonmc.api.client.gui.widget.TScrollBarWidget;
import com.thecsdev.commonmc.api.client.gui.widget.stats.TBlockStatsWidget;
import com.thecsdev.commonmc.api.client.gui.widget.stats.TEntityStatsWidget;
import com.thecsdev.commonmc.api.client.gui.widget.stats.TItemStatsWidget;
import com.thecsdev.commonmc.api.stats.RandomStatsProvider;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;

import static com.thecsdev.commonmc.TCDCommons.LOGGER;
import static com.thecsdev.commonmc.api.client.gui.panel.TPanelElement.COLOR_BACKGROUND;
import static com.thecsdev.commonmc.api.client.gui.panel.TPanelElement.COLOR_OUTLINE;

/**
 * Internal {@link TScreen} implementation whose purpose is debugging and preloading
 * {@link Class}es necessary for the GUI system. The main reason for preloading
 * is avoiding performance hiccups later on from Java's lazy loading mechanism.
 * @apiNote This conveniently also serves as an internal debug screen.
 */
@Environment(EnvType.CLIENT)
public final @ApiStatus.Internal class TTestScreen extends TScreenPlus
{
	// ==================================================
	protected final @Override void initCallback()
	{
		//the main panel
		final var panel = new TPanelElement.Transparent();
		panel.scrollPaddingProperty().set(10, TTestScreen.class);
		add(panel);
		panel.setBounds(new UDim2(0, 0, 0, 0), new UDim2(1, -10, 1, -10));

		//scroll-bars for the main panel
		final var psY = new TScrollBarWidget.Flat(panel, TScrollBarWidget.ScrollDirection.VERTICAL);
		psY.setBounds(panel.getBounds().endX, 0, 10, panel.getBounds().height);
		add(psY);
		final var psX = new TScrollBarWidget.Flat(panel, TScrollBarWidget.ScrollDirection.HORIZONTAL);
		psX.setBounds(0, panel.getBounds().endY, panel.getBounds().width, 10);
		add(psX);

		//test buttons
		final var btn1 = new TButtonWidget();
		btn1.getLabel().setText(Component.literal("Test file chooser"));
		btn1.setBounds(10, 10, 150, 20);
		btn1.eClicked.register(__ -> {
			final var screen = new TFileChooserScreen.Builder(TFileChooserScreen.Mode.CREATE_FILE)
					.setLastScreen(getAsScreen())
					.addPathFilter(TFileChooserScreen.PathFilter.ALL)
					.addPathFilter(TFileChooserScreen.PathFilter.extname(".txt"))
					.addPathFilter(TFileChooserScreen.PathFilter.extname("json"))
					.addPathFilter(TFileChooserScreen.PathFilter.extnames(Component.literal("Image files"), ".png", "jpg", "webp"))
					.build();
			screen.getResult().handle((paths, throwable) -> {
				if(paths != null)
					LOGGER.info("File chooser completed: {}",
							paths.stream().map(Object::toString).collect(Collectors.joining(", ")));
				else if(throwable != null)
					LOGGER.error("File chooser completed exceptionally: ", throwable);
				return null;
			});
			getClient().setScreen(screen.getAsScreen());
		});
		panel.add(btn1);

		//test statistics
		initEnityStats(panel);
		initItemStats(panel);
		initBlockStats(panel);
	}
	// ==================================================
	private final void initItemStats(@NotNull TPanelElement panel)
	{
		final var cbb = panel.getContentBounds();
		final var panel_items = new TFillColorElement(COLOR_BACKGROUND, COLOR_OUTLINE);
		panel_items.setBounds(cbb.x, cbb.endY + 10, panel.getBounds().width - 20, 0);
		panel.add(panel_items);

		final var stats = RandomStatsProvider.INSTANCE;
		final int padding = 5;
		int nextX = padding, nextY = padding;
		for(final var item : BuiltInRegistries.ITEM)
		{
			final var el_stat = new TItemStatsWidget(item, stats);
			el_stat.setBounds(nextX, nextY, 20, 20);
			panel_items.addRel(el_stat);
			nextX += 20 + padding;
			if(nextX > panel_items.getBounds().width - (20 + padding)) {
				nextX = padding; nextY += (20 + padding);
			}
		}
		final var cb = panel_items.getContentBounds();
		panel_items.setBounds(cb.x - padding, cb.y - padding, cb.width + (padding * 2), cb.height + (padding * 2));
	}
	// --------------------------------------------------
	private final void initBlockStats(@NotNull TPanelElement panel)
	{
		final var cbb = panel.getContentBounds();
		final var panel_blocks = new TFillColorElement(COLOR_BACKGROUND, COLOR_OUTLINE);
		panel_blocks.setBounds(cbb.x, cbb.endY + 10, panel.getBounds().width - 20, 0);
		panel.add(panel_blocks);

		final var stats = RandomStatsProvider.INSTANCE;
		final int padding = 5;
		int nextX = padding, nextY = padding;
		for(final var block : BuiltInRegistries.BLOCK)
		{
			final var el_stat = new TBlockStatsWidget(block, stats);
			el_stat.setBounds(nextX, nextY, 20, 20);
			panel_blocks.addRel(el_stat);
			nextX += 20 + padding;
			if(nextX > panel_blocks.getBounds().width - (20 + padding)) {
				nextX = padding; nextY += (20 + padding);
			}
		}
		final var cb = panel_blocks.getContentBounds();
		panel_blocks.setBounds(cb.x - padding, cb.y - padding, cb.width + (padding * 2), cb.height + (padding * 2));
	}
	// --------------------------------------------------
	private final void initEnityStats(@NotNull TPanelElement panel)
	{
		final var cbb = panel.getContentBounds();
		final var panel_entities = new TFillColorElement(COLOR_BACKGROUND, COLOR_OUTLINE);
		panel_entities.setBounds(cbb.x, cbb.endY + 10, panel.getBounds().width - 20, 0);
		panel.add(panel_entities);

		final var stats = RandomStatsProvider.INSTANCE;
		final int padding = 5, elSize = 20;
		int nextX = padding, nextY = padding;
		for(final var entity : BuiltInRegistries.ENTITY_TYPE)
		{
			final var el_stat = new TEntityStatsWidget(entity, stats);
			el_stat.setBounds(nextX, nextY, elSize, elSize);
			panel_entities.addRel(el_stat);
			nextX += elSize + padding;
			if(nextX > panel_entities.getBounds().width - (elSize + padding)) {
				nextX = padding; nextY += (elSize + padding);
			}
		}
		final var cb = panel_entities.getContentBounds();
		panel_entities.setBounds(cb.x - padding, cb.y - padding, cb.width + (padding * 2), cb.height + (padding * 2));
	}
	// ==================================================
}
