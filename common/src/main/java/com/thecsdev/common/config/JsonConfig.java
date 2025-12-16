package com.thecsdev.common.config;

import com.google.common.collect.Streams;
import com.google.gson.*;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.thecsdev.common.util.ReflectionUtils;
import com.thecsdev.common.util.TUtils;
import com.thecsdev.common.util.annotations.Virtual;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * A container for various configurable properties that can be saved and to and
 * loaded from the user's disk drive.<br>
 * <br>
 * This configuration system uses reflection to save and load properties
 * found within a given container.<br>
 * <br>
 * The data is saved and loaded in JSON format.
 *
 * @apiNote Depends on <b>Guava</b> - {@code com.google.*}.
 *
 * @see Expose
 * @see SerializedName
 */
public abstract class JsonConfig
{
	// ==================================================
	/**
	 * The main {@link Gson} instance used by {@link JsonConfig}.
	 */
	protected static final Gson GSON = new GsonBuilder()
			.excludeFieldsWithoutExposeAnnotation()
			.setPrettyPrinting()
			.create();
	// --------------------------------------------------
	/**
	 * The main {@link File} where this {@link JsonConfig} is to be stored at.
	 */
	protected transient volatile @Nullable File configFile;
	// ==================================================
	public JsonConfig() { this(null); }
	public JsonConfig(@Nullable File configFile) { this.configFile = configFile; }
	// ==================================================
	/**
	 * Returns the {@link File} that is used by {@link #saveToFile()} and
	 * {@link #loadFromFile()} when saving and loading this {@link JsonConfig}.
	 * @see #saveToFile()
	 * @see #loadFromFile()
	 * @see #setConfigFile(File)
	 */
	protected @Virtual @Nullable File getConfigFile() { return this.configFile; }

	/**
	 * Sets the {@link File} that is used by {@link #saveToFile()} and
	 * {@link #loadFromFile()} when saving and loading this {@link JsonConfig}.
	 * @param file The new {@link File} value.
	 * @see #saveToFile()
	 * @see #loadFromFile()
	 * @see #getConfigFile()
	 */
	protected @Virtual void setConfigFile(@Nullable File file) { this.configFile = file; }
	// ==================================================
	/**
	 * Called after {@link #saveToJson(JsonObject)} finishes its execution.
	 * @param to The {@link JsonObject} the data was saved to.
	 */
	protected @Virtual void onSave(JsonObject to) {}

	/**
	 * Called after {@link #loadFromJson(JsonObject)} finishes its execution.
	 * @param from The {@link JsonObject} the data was loaded from.
	 */
	protected @Virtual void onLoad(JsonObject from) {}
	// ==================================================
	/**
	 * Saves this {@link JsonConfig} instance to a new {@link JsonObject}
	 * instance and returns the {@link JsonObject}.
	 */
	public final @NotNull JsonObject saveToJson() {
		final var json = new JsonObject();
		saveToJson(json);
		return json;
	}

	/**
	 * Saves this {@link JsonConfig} to an existing {@link JsonObject} instance.
	 * @param json The {@link JsonObject} to save to.
	 * @throws NullPointerException If the argument is {@code null}.
	 */
	public final void saveToJson(JsonObject json) throws NullPointerException
	{
		Objects.requireNonNull(json);
		getSerializableProperties(getClass()).forEach(property -> TUtils.uncheckedCall(() ->
		{
			property.setAccessible(true);
			//obtain property value, and skip it if it's null
			@Nullable Object pValue = property.get(this);
			if(pValue == null) return;

			//save the value to the json object
			final String pName = getPropertyNames(property)[0];
			json.add(pName, (pValue instanceof JsonConfig jc) ? jc.saveToJson() : GSON.toJsonTree(pValue));
		}));
	}

	/**
	 * Loads this {@link JsonConfig} from a {@link JsonObject}, overriding
	 * any properties of this {@link JsonConfig} that were stored in the {@link JsonObject}.
	 * Mismatched types and {@code null} values do not override existing values.
	 * @param json The {@link JsonObject} to load from.
	 * @throws NullPointerException If the argument is {@code null}.
	 */
	public final void loadFromJson(JsonObject json)
	{
		//validate input json is not null
		Objects.requireNonNull(json);

		//iterate over all deserializable properties for this class
		getDeserializableProperties(getClass()).forEach(property -> TUtils.uncheckedCall(() ->
		{
			property.setAccessible(true);
			//get all possible property names for json lookup
			final String[] pNames = getPropertyNames(property);

			//json element for the property value, if found
			@Nullable JsonElement pjElement = null;
			for(final var pName : pNames)
			{
				if(!json.has(pName)) continue;
				pjElement = json.get(pName);
				break; //found json element for property
			}

			//skip if no json element of the given name(s) is present or if json is null
			//(null must not override existing values)
			if(pjElement == null || pjElement.isJsonNull())
				return;

			//get property type to guide deserialization
			final Class<?> pType = property.getType();

			//nested JSON config properties get special treatment
			if(JsonConfig.class.isAssignableFrom(pType))
			{
				//get current property value instance
				final var pValue = property.get(this);

				//ensure json element is an object
				final JsonObject obj = pjElement.isJsonObject() ? pjElement.getAsJsonObject() : null;
				if(obj == null) return; //skip mismatched type

				//reuse current instance or create a new one
				final var cfg = (pValue instanceof JsonConfig jc) ?
						jc : (JsonConfig) ReflectionUtils.createClassInstance(pType);
				if(cfg == null) return; //skip if an instance couldn't be created

				//load nested JSON config from JSON object
				cfg.loadFromJson(obj);

				//update property with loaded config
				property.set(this, cfg);
				return; //all done, onto the next property
			}

			//all other property types get default deserialization behavior
			@Nullable Object pValue = null;
			try { pValue = GSON.fromJson(pjElement, pType); }
			catch(JsonSyntaxException ignored) { return; /*skip mismatched type */ }
			property.set(this, pValue); //must NOT be set to null
		}));
	}
	// --------------------------------------------------
	/**
	 * Saves this {@link JsonConfig} to the {@link #getConfigFile()}.<br>
	 * Overrides any existing {@link #getConfigFile()}s.
	 * @throws IOException If {@link #getConfigFile()} is {@code null}.
	 * @throws IOException If an {@link IOException} takes place.
	 */
	public final void saveToFile() throws NullPointerException, IOException
	{
		//get config file, and make its parent directories
		final File file   = Objects.requireNonNull(getConfigFile());
		Optional.ofNullable(file.getParentFile()).ifPresent(File::mkdirs);

		//save to json as string and then write the string to the file
		Files.writeString(file.toPath(), GSON.toJson(saveToJson()));
	}

	/**
	 * Loads this {@link JsonConfig} from the {@link #getConfigFile()}.<br>
	 * If the {@link File} does not exist, nothing happens.
	 * @throws IOException If {@link #getConfigFile()} is {@code null}.
	 * @throws IOException If an {@link IOException} takes place.
	 */
	public final void loadFromFile() throws NullPointerException, IOException
	{
		//get config file and check if it exists
		final File file = Objects.requireNonNull(getConfigFile());
		if(!file.exists()) return;

		//read the file as string and load it
		try {
			loadFromJson(GSON.fromJson(Files.readString(file.toPath()), JsonObject.class));
		} catch(Exception e) {
			throw new IOException("Failed to load JSON config file: " + file, e);
		}
	}
	// ==================================================
	/**
	 * Returns a {@link Stream} of all {@link Field}s (including inherited) that should be handled
	 * during serialization and deserialization of the given {@link JsonConfig} {@link Class}.
	 *
	 * <p>Fields are included only if they meet all the following conditions:</p>
	 * <ul>
	 *   <li>Are not static</li>
	 *   <li>Are not transient</li>
	 *   <li>Are not arrays</li>
	 *   <li>Are primitive types or {@link Serializable} or {@link JsonConfig}</li>
	 *   <li>Are annotated with Expose</li>
	 * </ul>
	 *
	 * @param clazz the {@link JsonConfig} class to inspect.
	 * @return a {@link Stream} of eligible {@link Field}s.
	 * @throws NullPointerException If the argument is {@code null}.
	 */
	public static final Stream<Field> getProperties(Class<? extends JsonConfig> clazz)
			throws NullPointerException {
		return Arrays.stream(ReflectionUtils.getAllDeclaredFields(clazz))
			.filter(f -> !Modifier.isStatic(f.getModifiers()))
			.filter(f -> !Modifier.isTransient(f.getModifiers()))
			.filter(f -> !f.getType().isArray())
			.filter(f -> f.getType().isPrimitive() ||
					Serializable.class.isAssignableFrom(f.getType()) ||
					JsonConfig.class.isAssignableFrom(f.getType()))
			.filter(f -> f.isAnnotationPresent(Expose.class));
	}

	/**
	 * From {@link #getProperties(Class)}, filters out only serializable properties.
	 * @param clazz the {@link JsonConfig} class to inspect.
	 * @throws NullPointerException If the argument is {@code null}.
	 */
	public static final Stream<Field> getSerializableProperties(Class<? extends  JsonConfig> clazz)
			throws NullPointerException {
		return getProperties(clazz).filter(f -> f.getAnnotation(Expose.class).serialize());
	}

	/**
	 * From {@link #getProperties(Class)}, filters out only deserializable properties.
	 * @param clazz The {@link JsonConfig} class to inspect.
	 * @throws NullPointerException If the argument is {@code null}.
	 */
	public static final Stream<Field> getDeserializableProperties(Class<? extends  JsonConfig> clazz)
			throws NullPointerException {
		return getProperties(clazz).filter(f -> f.getAnnotation(Expose.class).deserialize());
	}
	// --------------------------------------------------
	/**
	 * Cache of resolved property names per {@link Field} for fast reuse.
	 */
	private static final Map<Field, String[]> PROPERTY_NAME_CACHE = new ConcurrentHashMap<>();

	/**
	 * Resolves the serialized names for a {@link Field} using its {@link SerializedName}
	 * annotation (if present), returning the primary name followed by any alternates.
	 *
	 * @param property The {@link Field} to inspect.
	 * @return An array of names to use for serialization/deserialization (never null, non-empty).
	 * @throws NullPointerException If the argument is {@code null}.
	 */
	public static String[] getPropertyNames(Field property) throws NullPointerException
	{
		Objects.requireNonNull(property);
		return PROPERTY_NAME_CACHE.computeIfAbsent(property, f ->
		{
			final @Nullable var sn = f.getAnnotation(SerializedName.class);
			if(sn != null)
				return Streams.concat(Stream.of(sn.value()), Arrays.stream(sn.alternate()))
					.toArray(String[]::new);
			else return new String[] { f.getName() };
		});
	}
	// ==================================================
}
