package com.thecsdev.commonmc.client.gui.screen;

import com.thecsdev.common.math.Point2d;
import com.thecsdev.common.math.UDim2;
import com.thecsdev.common.util.TUtils;
import com.thecsdev.common.util.enumerations.CompassDirection;
import com.thecsdev.commonmc.TCDCommons;
import com.thecsdev.commonmc.api.client.gui.TElement;
import com.thecsdev.commonmc.api.client.gui.ctxmenu.TContextMenu;
import com.thecsdev.commonmc.api.client.gui.label.TLabelElement;
import com.thecsdev.commonmc.api.client.gui.misc.TFillColorElement;
import com.thecsdev.commonmc.api.client.gui.panel.TPanelElement;
import com.thecsdev.commonmc.api.client.gui.screen.TFileChooserScreen;
import com.thecsdev.commonmc.api.client.gui.screen.TScreen;
import com.thecsdev.commonmc.api.client.gui.screen.TScreenPlus;
import com.thecsdev.commonmc.api.client.gui.screen.TTextDialogScreen;
import com.thecsdev.commonmc.api.client.gui.widget.TButtonWidget;
import com.thecsdev.commonmc.api.client.gui.widget.TScrollBarWidget;
import com.thecsdev.commonmc.api.client.gui.widget.TSliderWidget;
import com.thecsdev.commonmc.api.client.gui.widget.stats.TEntityStatsWidget;
import com.thecsdev.commonmc.api.client.gui.widget.stats.TItemStatsWidget;
import com.thecsdev.commonmc.api.stats.RandomStatsProvider;
import com.thecsdev.commonmc.resources.TComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.thecsdev.commonmc.api.client.gui.panel.TPanelElement.COLOR_BACKGROUND;
import static com.thecsdev.commonmc.api.client.gui.panel.TPanelElement.COLOR_OUTLINE;
import static com.thecsdev.commonmc.resources.TComponent.block;
import static net.minecraft.network.chat.Component.literal;

/**
 * Internal {@link TScreen} implementation whose purpose is debugging and preloading
 * {@link Class}es necessary for the GUI system. The main reason for preloading
 * is avoiding performance hiccups later on from Java's lazy loading mechanism.
 * @apiNote This conveniently also serves as an internal debug screen.
 */
public final @ApiStatus.Internal class TTestScreen extends TScreenPlus
{
	// ==================================================
	public static @ApiStatus.Internal @NotNull Point2d slider_value = new Point2d(1, 1);
	// ==================================================
	protected final @Override void initCallback()
	{
		final var bb = getBounds();

		final var panel = new TPanelElement.Transparent();
		panel.scrollPaddingProperty().set(10, TTestScreen.class);
		add(panel);
		panel.setBounds(new UDim2(0, 0, 0, 0), new UDim2(1, -10, 1, -10));

		final var pscroll_y = new TScrollBarWidget.Flat(panel, TScrollBarWidget.ScrollDirection.VERTICAL);
		pscroll_y.setBounds(panel.getBounds().endX, 0, 10, panel.getBounds().height);
		add(pscroll_y);
		final var pscroll_x = new TScrollBarWidget.Flat(panel, TScrollBarWidget.ScrollDirection.HORIZONTAL);
		pscroll_x.setBounds(0, panel.getBounds().endY, panel.getBounds().width, 10);
		add(pscroll_x);

		final var el1 = new TButtonWidget();
		el1.setBounds(10, 10, 1000, 20);
		el1.getLabel().setText(literal("Popup context menu"));
		el1.eClicked.register(TElement::showContextMenu);
		el1.contextMenuProperty().set(__ -> new TContextMenu.Builder(Objects.requireNonNull(getClient()))
				.addButton(literal("Option 1"), ___ -> {})
				.addButton(literal("Option 2"), ___ -> {})
				.addButton(literal("Option 3"), ___ -> {})
				.addSeparator()
				.addButton(literal("Option 4"), ___ -> {})
				.addButton(literal("Option 5"), ___ -> {})
				.addButton(literal("Option 6"), ___ -> {})
				.addSeparator()
				.addButton(block("block/fire_coral").append(" I like corals"), ___ -> {})
				.addButton(block("block/gold_block").append(" Pfft, corals? Gold is better"), ___ -> {})
				.addContextMenu(literal("See more..."), new TContextMenu.Builder(getClient())
						.addButton(block("block/fire_coral").append(" I like corals"), ___ -> {})
						.addButton(block("block/fire_coral").append(" I like corals"), ___ -> {})
						.addButton(block("block/fire_coral").append(" I like corals"), ___ -> {})
						.addButton(block("block/fire_coral").append(" I like corals"), ___ -> {})
						.addButton(block("block/fire_coral").append(" I like corals"), ___ -> {})
						.addButton(block("block/fire_coral").append(" I like corals"), ___ -> {})
						.addButton(block("block/gold_block").append(" SHUT UP!   "), ___ -> {})
						.build())
				.addSeparator()
				.addButton(TComponent.head("Steve").append(" I... am Steve!"), ___ -> {})
				.addButton(TComponent.head("Alex").append(" and I'm Aleeex! xo"), ___ -> {})
				.addButton(TComponent.head("Herobrine").append(" and im a furry- (Herobrine)"), ___ -> {})
				.addButton(TComponent.air().append(" ..."), ___ -> {})
				.addSeparator()
				.addButton(TComponent.painting("dennis").append(" Hi, my name is Dennis!"), ___ -> {})
				.addButton(TComponent.painting("donkey_kong").append(" donkey konged in yo- *cuts off*"), ___ -> {})
				.addButton(TComponent.painting("pigscene").append(" this painting has a lot of *pig*ment"), ___ -> {})
				.addButton(TComponent.painting("burning_skull").append(" SKULL EMOJIIII 💀💀💀💀💀💀💀💀💀"), ___ -> {})
				.addButton(TComponent.painting("skeleton").append(" spooky scary skeletons, or however that songo goes"), ___ -> {})
				.addButton(TComponent.painting("changing").append(" nice, very picasso. very coolieo. everyone will be amazed at how cool this is"), ___ -> {})
				.addSeparator()
				.addButton(TComponent.particle("big_smoke_1").append(" Respect has to be earned, Sweet - just like money."), ___ -> {})
				.addButton(TComponent.particle("explosion_8").append(" 👀 i see..."), ___ -> {})
				.addButton(TComponent.particle("angry").append(" villager when i offer reasonable trade"), ___ -> {})
				.addButton(TComponent.particle("glint").append(" villager when they rip me off"), ___ -> {})
				.addSeparator()
				.addButton(literal("if i see another separator im gonna separate yo-"), ___ -> {})
				.addSeparator()
				.addButton(literal("oh well."), ___ -> {})
				.addButton(literal("mama"), ___ -> {})
				.addButton(literal("mia"), ___ -> {})
				.addButton(literal("pasta"), ___ -> {})
				.addButton(literal("italia"), ___ -> {})
				.build(), TTestScreen.class);
		panel.addRel(el1);

		final var el2 = new TButtonWidget();
		el2.setBounds(10, 40, 300, 20);
		el2.getLabel().setText(literal("Popup file chooser"));
		el2.eClicked.register(__ -> {
			assert getClient() != null;
			getClient().setScreen(new TFileChooserScreen.Builder(TFileChooserScreen.Mode.CREATE_FILE)
					.setLastScreen(getAsScreen())
					.addFileFilter(TFileChooserScreen.FileFilter.ALL)
					.addFileFilter(TFileChooserScreen.FileFilter.extname("txt"))
					.addFileFilter(TFileChooserScreen.FileFilter.extname("png"))
					.addFileFilter(TFileChooserScreen.FileFilter.extname("gif"))
					.build((result, file) -> {
						TCDCommons.LOGGER.info("File chooser result: {}, file: {}", result, file);
						if(file != null && !file.exists())
							TUtils.uncheckedCall(file::createNewFile);
					})
					.getAsScreen());
		});
		panel.addRel(el2);

		final var el3 = new TFillColorElement.Flat(0xFFFF00FF, 0xFF000000);
		el3.setBounds(10, 70, 400, 200);
		panel.addRel(el3);
		final var el4 = new TLabelElement(literal(
				"""
				Hello world, how are you doing? Because I am a label. Speaking of labels, \
				my sole purpose is testing. I exist to ensure labels work properly. \
				Speaking of labels working properly, WHY IS THIS BUG STILL HERE???? \
				(it probably got fixed by the time you read this). Yea that text height \
				calculation needs fixing because I have no idea why it isn't calculating \
				it properly. Anyway this is it for the testing label and that's it. Okay, \
				bye.
				
				Edit: OHHHHH, GOT IT! I should have used 'double' instead of 'int' \
				in label's refresh function."""
		));
		el4.setBounds(el3.getBounds());
		el4.wrapTextProperty().set(true, TTestScreen.class);
		el3.add(el4);

		final var el5 = new TSliderWidget(new Point2d(0, 0.5));
		el5.setBounds(10, 280, 50, 50);
		el5.valueProperty().addChangeListener((p, o, n) -> {
			final var dir = CompassDirection.of01((int) Math.round(n.x * 2 - 1), (int) Math.round(n.y * 2 - 1));
			el4.textAlignmentProperty().set(dir, TTestScreen.class);
		});
		panel.addRel(el5);

		final var el6 = new TSliderWidget(new Point2d(0.1d, 0));
		el6.setBounds(70, 280, 200, 50);
		el6.knobSizeProperty().set(new UDim2(0, 10, 1, 0), TTestScreen.class);
		el6.valueProperty().addChangeListener((p, o, n) ->
				el4.textScaleProperty().set(n.x * 10, TTestScreen.class));
		panel.addRel(el6);

		final var el7 = new TButtonWidget();
		el7.setBounds(10, 340, 200, 20);
		el7.getLabel().setText(literal("Popup dialog"));
		el7.eClicked.register(__ -> {
			final var client = Objects.requireNonNull(__.getClient());
			final var dialog = new TTextDialogScreen(client.screen,
					literal("""
							This is a test message because I would like to test how these \
							dialogs work so I can make sure that everything is working fine \
							before I run out of words to put into this dialog and oh no I am \
							already running out of words oh my what will I do now other than \
							repeat myself a bagillion times until I have no idea what else to \
							say. Oh hold on, Christmas is coming as of writing this, so how \
							about a fun Christmas fact: On the day of Christmas, people give \
							gifts. It's a fun fact, isn't it? Too boring? Okay here's another \
							one: Shoppers spend a lot shopping around holidays, which attracts \
							advertisers who then spend a lot more on advertising, leading to \
							larger ad-revenues around holidays. Okay surely that's a fun fact!"""));
			client.setScreen(dialog.getAsScreen());
		});
		panel.addRel(el7);

		final var el8 = new TButtonWidget();
		el8.setBounds(10, 370, 200, 20);
		el8.getLabel().setText(literal("Popup dialog 2"));
		el8.eClicked.register(__ -> {
			final var client = Objects.requireNonNull(__.getClient());
			final var dialog = new TTextDialogScreen(client.screen,
					literal("""
							This message is intentionally not using a word-wrap label so we can ensure that those are accounted for as well.
							
							And so by not using word wrap thse should appear as two long lines that stretch on without wrapping, so yea... Let's see if that works."""));
			dialog.getMessageLabel().wrapTextProperty().set(false, TTestScreen.class);
			client.setScreen(dialog.getAsScreen());
		});
		panel.addRel(el8);

		initEnityStats(panel);
		initItemStats(panel);
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
