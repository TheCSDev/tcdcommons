package com.thecsdev.commonmc.api.client.gui.screen.hud;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.thecsdev.common.util.annotations.Virtual;
import com.thecsdev.commonmc.TCDCommons;
import com.thecsdev.commonmc.api.client.gui.TElement;
import com.thecsdev.commonmc.api.client.gui.render.TGuiGraphics;
import com.thecsdev.commonmc.api.client.gui.screen.ILastScreenProvider;
import com.thecsdev.commonmc.api.client.gui.screen.TScreen;
import com.thecsdev.commonmc.api.client.registry.TClientRegistries;
import com.thecsdev.commonmc.api.serialization.TCodec;
import com.thecsdev.commonmc.resource.TLanguage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Registry;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static com.thecsdev.commonmc.TCDCommons.MOD_ID;
import static net.minecraft.resources.Identifier.fromNamespaceAndPath;

/**
 * A {@link TScreen} that acts as a finite "whiteboard" canvas, onto which
 * {@link THudElement}s of varying {@link THudElementType}s may be pinned.
 * <p>
 * Pinned elements are positioned using <b>anchor points</b> instead of absolute
 * coordinates, which allows them to stay in roughly the same on-screen spot
 * across different screen resolutions and GUI scales.
 * <p>
 * Elements may be dragged around and configured only while this screen is
 * <b>explicitly open</b>. While not open, the screen keeps rendering itself on
 * the game's in-game HUD (heads-up display), where its elements act as
 * visual-only displays.
 * <p>
 * The screen's contents may be saved to and loaded from disk using Mojang's
 * DataFixerUpper, in either {@code JSON} or {@code NBT} form. Data points that
 * cannot be parsed are simply ignored, and the rest are loaded as usual.
 *
 * @see TClientRegistries#HUD_SCREEN
 * @see TClientRegistries#HUD_ELEMENT_TYPE
 * @see THudElement
 * @see THudElementType
 */
@ApiStatus.Experimental
@Environment(EnvType.CLIENT)
public @Virtual class THudCanvasScreen extends TScreen implements ILastScreenProvider
{
	// ==================================================
	/**
	 * The {@link Identifier} under which {@link #INSTANCE} registers itself
	 * in the {@link TClientRegistries#HUD_SCREEN} registry.
	 */
	public static final Identifier HUD_SCREEN_ID = fromNamespaceAndPath(MOD_ID, "hud_canvas");

	/**
	 * The globally shared {@link THudCanvasScreen} instance, that is rendered on
	 * the game's HUD for everyone.
	 * @apiNote Isolated instances may be constructed using {@link #THudCanvasScreen()}.
	 */
	public static final THudCanvasScreen INSTANCE = new THudCanvasScreen();
	// --------------------------------------------------
	/**
	 * {@link Codec} implementation for this screen's contents.
	 * <p>
	 * Is "lenient" - {@link THudElement} entries that fail to encode or decode
	 * are skipped instead of failing the entire operation.
	 */
	public static final Codec<List<THudElement>> CONTENTS_CODEC = TCodec.lenientListOf(THudElement.CODEC);
	// --------------------------------------------------
	/**
	 * The default file this screen's contents are saved to and loaded from,
	 * relative to the game's runtime directory.
	 */
	public static final Path DEFAULT_CONTENTS_FILE = Path.of("config", MOD_ID, "hud_canvas.json");
	// --------------------------------------------------
	private static int nextScreenId = 1; //for assigning unique id-s to isolated instances
	// ==================================================
	private final @NotNull  List<THudElement> elements     = new ArrayList<>();
	private final @NotNull  Identifier        hudScreenId;
	private           @Nullable Screen        lastScreen;
	// ==================================================
	/**
	 * Constructs a new {@link THudCanvasScreen}.
	 */
	public THudCanvasScreen() { this(TLanguage.gui_hudCanvas_title()); }

	/**
	 * Constructs a new {@link THudCanvasScreen}.
	 * @param title The title of this screen.
	 * @throws NullPointerException If the argument is {@code null}.
	 */
	public THudCanvasScreen(@NotNull Component title) throws NullPointerException
	{
		super(Objects.requireNonNull(title));
		//the globally shared instance uses the well-known id, isolated ones get a unique one
		this.hudScreenId = (INSTANCE == null) ?
				HUD_SCREEN_ID :
				fromNamespaceAndPath(MOD_ID, "hud_canvas_" + nextScreenId++);
		//auto-register this screen in the HUD screens registry, so it renders in-game
		register();
	}
	// ==================================================
	public final @Override @Nullable Screen getLastScreen() { return this.lastScreen; }

	/**
	 * Sets the {@link Screen} that is to be returned to when this
	 * {@link THudCanvasScreen} is closed.
	 */
	public @Virtual void setLastScreen(@Nullable Screen lastScreen) { this.lastScreen = lastScreen; }

	/**
	 * Returns the {@link Identifier} under which this {@link THudCanvasScreen}
	 * is registered in the {@link TClientRegistries#HUD_SCREEN} registry.
	 */
	public final @NotNull Identifier getHudScreenID() { return this.hudScreenId; }
	// ==================================================
	/**
	 * Registers this {@link THudCanvasScreen} in the {@link TClientRegistries#HUD_SCREEN}
	 * registry, so that it renders on the game's HUD.
	 * @return {@code true} if this screen was not already registered.
	 */
	public final boolean register()
	{
		final var registry = TClientRegistries.HUD_SCREEN;
		if(registry.containsKey(this.hudScreenId)) return false;
		Registry.register(registry, this.hudScreenId, getAsScreen());
		return true;
	}
	// ==================================================
	/**
	 * Returns an unmodifiable {@link List} of all {@link THudElement}s currently
	 * pinned onto this {@link THudCanvasScreen}.
	 */
	public final @NotNull List<THudElement> getElements() { return List.copyOf(this.elements); }

	/**
	 * Returns the number of {@link THudElement}s pinned onto this {@link THudCanvasScreen}.
	 */
	public final int elementCount() { return this.elements.size(); }

	/**
	 * Returns {@code true} if a given {@link THudElement} is pinned onto
	 * this {@link THudCanvasScreen}.
	 * @throws NullPointerException If the argument is {@code null}.
	 */
	public final boolean containsElement(@NotNull THudElement element) throws NullPointerException {
		return this.elements.contains(Objects.requireNonNull(element));
	}
	// --------------------------------------------------
	/**
	 * Pins a given {@link THudElement} onto this {@link THudCanvasScreen}.
	 * @param element The {@link THudElement} to pin.
	 * @return {@code true} if the {@link THudElement} was pinned successfully.
	 * @throws NullPointerException If the argument is {@code null}.
	 */
	public final boolean addElement(@NotNull THudElement element) throws NullPointerException
	{
		Objects.requireNonNull(element, "'THudElement' must not be 'null'");
		//do not add duplicates, nor elements that already belong to another parent
		if(this.elements.contains(element)) return false;
		final @Nullable var parent = element.getParent();
		if(parent != null && parent != this) return false;

		//keep track of the element, and (re-)initialize this screen
		this.elements.add(element);
		refresh();
		return true;
	}

	/**
	 * Pins a {@link THudElement} of a given {@link THudElementType} onto this
	 * {@link THudCanvasScreen}.
	 * @param type The {@link THudElementType} whose element is to be pinned.
	 * @return The newly pinned {@link THudElement}, or {@code null} on failure.
	 * @throws NullPointerException If the argument is {@code null}.
	 */
	public final @Nullable <E extends THudElement> E addElement(@NotNull THudElementType<E> type)
			throws NullPointerException
	{
		final @NotNull var element = Objects
				.requireNonNull(type, "'THudElementType' must not be 'null'")
				.instantiate();
		return addElement(element) ? element : null;
	}

	/**
	 * Removes a given {@link THudElement} from this {@link THudCanvasScreen}.
	 * @return {@code true} if the {@link THudElement} was removed.
	 * @throws NullPointerException If the argument is {@code null}.
	 */
	public final boolean removeElement(@NotNull THudElement element) throws NullPointerException
	{
		Objects.requireNonNull(element, "'THudElement' must not be 'null'");
		final boolean removed = this.elements.remove(element);
		if(removed) element.remove();
		refresh();
		return removed;
	}

	/**
	 * Removes all {@link THudElement}s from this {@link THudCanvasScreen}.
	 */
	public final void clearElements() {
		this.elements.forEach(TElement::remove);
		this.elements.clear();
		refresh();
	}
	// --------------------------------------------------
	/**
	 * Refreshes this {@link THudCanvasScreen} by re-initializing it, which
	 * re-attaches all pinned {@link THudElement}s.
	 */
	public @Virtual void refresh() { clearAndInit(); }

	/**
	 * Opens this {@link THudCanvasScreen}, allowing the user to rearrange
	 * and configure its pinned {@link THudElement}s.
	 * @param client The {@link Minecraft} instance to open this screen on.
	 * @throws NullPointerException If the argument is {@code null}.
	 */
	public final void open(@NotNull Minecraft client) throws NullPointerException {
		Objects.requireNonNull(client);
		setLastScreen(client.gui.screen());
		client.gui.setScreen(getAsScreen());
	}
	// ==================================================
	/**
	 * Serializes this {@link THudCanvasScreen}'s contents using a given
	 * {@link DynamicOps}, and returns the resulting value.
	 * @param ops The {@link DynamicOps} to serialize with. E.g. {@link JsonOps} or {@link NbtOps}.
	 * @throws NullPointerException If the argument is {@code null}.
	 */
	public final @NotNull <T> T saveToOps(@NotNull DynamicOps<T> ops) throws NullPointerException
	{
		Objects.requireNonNull(ops);
		return CONTENTS_CODEC.encodeStart(ops, List.copyOf(this.elements)).getOrThrow();
	}

	/**
	 * Deserializes this {@link THudCanvasScreen}'s contents using a given
	 * {@link DynamicOps} and input value.
	 * <p>
	 * Data points that cannot be parsed are simply ignored, and the remaining
	 * valid ones are loaded as usual.
	 * @param ops The {@link DynamicOps} to deserialize with. E.g. {@link JsonOps} or {@link NbtOps}.
	 * @param input The value to deserialize from.
	 * @return {@code true} if any {@link THudElement}s were loaded.
	 * @throws NullPointerException If an argument is {@code null}.
	 */
	public final <T> boolean loadFromOps(@NotNull DynamicOps<T> ops, @NotNull T input)
			throws NullPointerException
	{
		Objects.requireNonNull(ops);
		Objects.requireNonNull(input);
		final var loaded = CONTENTS_CODEC.decode(ops, input).getOrThrow().getFirst();
		//replace the existing elements with the loaded ones
		clearElements();
		for(final var element : loaded) addElement(element);
		return !loaded.isEmpty();
	}
	// --------------------------------------------------
	/**
	 * Serializes this {@link THudCanvasScreen}'s contents into {@link JsonElement} form.
	 */
	public final @NotNull JsonElement saveToJson() { return (JsonElement) saveToOps(JsonOps.INSTANCE); }

	/**
	 * Deserializes this {@link THudCanvasScreen}'s contents from {@link JsonElement} form.
	 * @param json The {@link JsonElement} to load from.
	 * @return {@code true} if any {@link THudElement}s were loaded.
	 * @throws NullPointerException If the argument is {@code null}.
	 */
	public final boolean loadFromJson(@NotNull JsonElement json) throws NullPointerException {
		return loadFromOps(JsonOps.INSTANCE, Objects.requireNonNull(json));
	}
	// --------------------------------------------------
	/**
	 * Serializes this {@link THudCanvasScreen}'s contents into {@link Tag} form.
	 */
	public final @NotNull Tag saveToNbt() { return saveToOps(NbtOps.INSTANCE); }

	/**
	 * Deserializes this {@link THudCanvasScreen}'s contents from {@link Tag} form.
	 * @param nbt The {@link Tag} to load from.
	 * @return {@code true} if any {@link THudElement}s were loaded.
	 * @throws NullPointerException If the argument is {@code null}.
	 */
	public final boolean loadFromNbt(@NotNull Tag nbt) throws NullPointerException {
		return loadFromOps(NbtOps.INSTANCE, Objects.requireNonNull(nbt));
	}
	// ==================================================
	/**
	 * Saves this {@link THudCanvasScreen}'s contents to a given file.
	 * @param file The target file. Its extension-name decides the format.
	 * @throws NullPointerException If the argument is {@code null}.
	 * @throws IllegalArgumentException If the file's extension-name is unsupported.
	 * @throws IOException If an I/O error occurs.
	 * @apiNote Supports {@code .json} and {@code .nbt} files only.
	 */
	public final void saveToFile(@NotNull Path file)
			throws NullPointerException, IllegalArgumentException, IOException
	{
		Objects.requireNonNull(file);
		switch(getExtension(file.toString()).toLowerCase(Locale.ROOT)) {
			case "json": saveAsJson(file); break;
			case "nbt":  saveAsNbt(file);  break;
			default: throw new IllegalArgumentException("Unsupported extname - " + file);
		}
	}

	/**
	 * Loads this {@link THudCanvasScreen}'s contents from a given file.
	 * @param file The target file. Its extension-name decides the format.
	 * @return {@code true} if any {@link THudElement}s were loaded.
	 * @throws NullPointerException If the argument is {@code null}.
	 * @throws IllegalArgumentException If the file's extension-name is unsupported.
	 * @throws IOException If an I/O error occurs.
	 * @apiNote Supports {@code .json} and {@code .nbt} files only.
	 */
	public final boolean loadFromFile(@NotNull Path file)
			throws NullPointerException, IllegalArgumentException, IOException
	{
		Objects.requireNonNull(file);
		switch(getExtension(file.toString()).toLowerCase(Locale.ROOT)) {
			case "json": return loadFromJsonFile(file);
			case "nbt":  return loadFromNbtFile(file);
			default: throw new IllegalArgumentException("Unsupported extname - " + file);
		}
	}
	// --------------------------------------------------
	private final void saveAsJson(@NotNull Path file) throws IOException
	{
		final var parent = file.getParent();
		if(parent != null) Files.createDirectories(parent);
		try(final var writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
			writer.write(saveToJson().toString());
		}
	}

	private final void saveAsNbt(@NotNull Path file) throws IOException
	{
		final var parent = file.getParent();
		if(parent != null) Files.createDirectories(parent);
		try(final var out = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(file)))) {
			NbtIo.write(saveToNbt().asCompound().orElseThrow(), out);
		}
	}

	private final boolean loadFromJsonFile(@NotNull Path file) throws IOException
	{
		if(!Files.exists(file)) return false;
		try(final var reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
			return loadFromJson(JsonParser.parseReader(reader));
		}
	}

	private final boolean loadFromNbtFile(@NotNull Path file) throws IOException
	{
		if(!Files.exists(file)) return false;
		try(final var in = new DataInputStream(new BufferedInputStream(Files.newInputStream(file)))) {
			return loadFromNbt(Objects.requireNonNull(NbtIo.read(in)));
		}
	}
	// ==================================================
	protected final @Override void initCallback()
	{
		//(re-)attach all pinned elements, and reposition them based on their anchor points
		for(final var element : List.copyOf(this.elements))
		{
			//attach the element to this screen if it is not attached already
			if(element.getParent() != this) add(element);
			//reposition it based on its anchor point
			element.updatePositionFromAnchor();
			//elements must also be re-initialized, as they were cleared alongside this screen
			try { element.clearAndInit(); }
			//a single misbehaving element must not take the entire HUD down with it
			catch(Exception e) {
				TCDCommons.LOGGER.error("Failed to initialize a pinned HUD element of type '{}'", element.getType(), e);
			}
		}
	}
	// --------------------------------------------------
	public @Virtual @Override void renderCallback(@NotNull TGuiGraphics pencil) {
		//dim the screen slightly while editing, so it is clear the canvas is open
		if(isOpen()) {
			final var bb = getBounds();
			if(!bb.isEmpty) pencil.fillColor(bb.x, bb.y, bb.width, bb.height, 0x40000000);
		}
		//re-attach any elements that were detached from this screen. this happens
		//when this screen's wrapper clears its target upon being closed, and there
		//is no re-initialization to recover from it, as the size never changed
		for(final var element : this.elements)
			if(element.getParent() != this) {
				add(element);
				element.updatePositionFromAnchor();
			}
	}
	// ==================================================
	/**
	 * Returns the extension-name of a given file name.
	 */
	private static @NotNull String getExtension(@NotNull String fileName) {
		final int i = fileName.lastIndexOf('.');
		return (i >= 0) ? fileName.substring(i + 1) : "";
	}
	// ==================================================
}
