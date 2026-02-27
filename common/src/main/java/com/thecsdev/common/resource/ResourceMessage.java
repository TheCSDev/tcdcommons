package com.thecsdev.common.resource;

import com.google.gson.JsonArray;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.*;

/**
 * An abstract base class representing a message related to a resource identified
 * by a {@link URI}.<br>
 * This class serves as a common foundation for both {@link ResourceRequest} and
 * {@link ResourceResponse}.
 */
public abstract sealed class ResourceMessage permits ResourceRequest, ResourceResponse
{
	// ================================================== ==================================================
	//                                    ResourceMessage IMPLEMENTATION
	// ================================================== ==================================================
	private final URI                       uri;      //URI associated with the message
	private final Map<String, List<String>> metadata; //stuff like http headers and system metadata
	private final byte[]                    data;     //the message data, like for example an HTTP message body
	// --------------------------------------------------
	private final int                       hashCode;
	// ==================================================
	protected ResourceMessage(
			@NotNull URI resourceUri,
			@NotNull Map<String, List<String>> metadata,
			byte @NotNull [] data)
			throws NullPointerException
	{
		//initialize fields
		this.uri      = Objects.requireNonNull(resourceUri);
		this.metadata = unmodifiableMetadata(Objects.requireNonNull(metadata));
		this.data     = Objects.requireNonNull(data);

		//calculate hash-code just once, for cases where data is very large
		int hashCode  = uri.hashCode();
		hashCode      = 31 * hashCode + metadata.hashCode();
		hashCode      = 31 * hashCode + Arrays.hashCode(data);
		this.hashCode = hashCode;
	}
	// ==================================================
	public @Override int hashCode() { return this.hashCode; }
	public @Override boolean equals(@Nullable Object obj)
	{
		if(this == obj) return true;
		else if(obj == null || getClass() != obj.getClass()) return false;
		final var other = (ResourceMessage) obj;
		return this.hashCode == other.hashCode &&
				this.uri.equals(other.uri) &&
				this.metadata.equals(other.metadata) &&
				Arrays.equals(data, other.data);
	}
	// ==================================================
	/**
	 * Returns the {@link URI} associated with this resource message.
	 */
	public final @NotNull URI getUri() { return this.uri; }
	// --------------------------------------------------
	/**
	 * Returns an <b>unmodifiable</b> {@link Map} containing metadata associated with
	 * the resource message. This may include HTTP headers, system metadata, or other
	 * relevant information. Meanings vary based on {@link URI#getScheme()}.
	 * <p>
	 * The returned {@link Map} and all of its {@link List} entries are all unmodifiable.
	 */
	public final @NotNull Map<String, List<String>> getMetadata() { return this.metadata; }

	/**
	 * Checks if the specified metadata entry exists.
	 * @param metadataName The name of the metadata entry.
	 * @throws NullPointerException If the argument is {@code null}.
	 */
	public final boolean has(@NotNull String metadataName) throws NullPointerException {
		Objects.requireNonNull(metadataName);
		metadataName = metadataName.toLowerCase(Locale.ROOT);
		return this.metadata.containsKey(metadataName);
	}

	/**
	 * Returns the first metadata value associated with the specified metadata name.
	 * @param metadataName The name of the metadata entry.
	 * @throws NullPointerException If a {@link NotNull} argument is {@code null}.
	 */
	public final @Nullable String getFirst(@NotNull String metadataName) throws NullPointerException {
		return getFirst(metadataName, null);
	}

	/**
	 * Returns the first metadata value associated with the specified metadata name,
	 * or throws {@link NoSuchElementException} if not found.
	 * @param metadataName The name of the metadata entry.
	 * @throws NullPointerException If a {@link NotNull} argument is {@code null}.
	 * @throws NoSuchElementException If no metadata entry is found for the specified name.
	 */
	public final @NotNull String getFirstOrThrow(@NotNull String metadataName)
			throws NullPointerException, NoSuchElementException
	{
		final @Nullable var value = getFirst(metadataName);
		if(value == null)
			throw new NoSuchElementException("No metadata entry found for name: " + metadataName);
		return value;
	}

	/**
	 * Returns the first metadata value associated with the specified metadata name.
	 * @param metadataName The name of the metadata entry.
	 * @param defaultValue The default value to return if the metadata entry is not found.
	 * @throws NullPointerException If a {@link NotNull} argument is {@code null}.
	 */
	@Contract("_, null -> _; _, !null -> !null")
	public final @Nullable String getFirst(
			@NotNull String metadataName, @Nullable String defaultValue)
			throws NullPointerException
	{
		//assert not null argument, and then normalize metadata name
		Objects.requireNonNull(metadataName);
		metadataName = metadataName.toLowerCase(Locale.ROOT);

		//obtain and return first metadata value
		final var list = getMetadata().get(metadataName);
		if(list != null && !list.isEmpty())
			return list.getFirst();

		//return default value if nothing found
		return defaultValue;
	}
	// --------------------------------------------------
	/**
	 * Returns the {@code byte[]} containing the raw byte data of this
	 * {@link ResourceMessage}.
	 */
	public final byte @NotNull [] getData() { return this.data; }
	// ==================================================
	/**
	 * Creates an immutable copy of the provided metadata {@link Map}.
	 * @param metadata The original metadata {@link Map} to be made immutable.
	 * @return An unmodifiable {@link Map} containing unmodifiable {@link List}s as values.
	 * @throws NullPointerException If the argument is {@code null}.
	 */
	static final Map<String, List<String>> unmodifiableMetadata(
			@NotNull Map<String, List<String>> metadata) throws NullPointerException
	{
		//require not null argument
		Objects.requireNonNull(metadata);
		//create new map and populate it
		final var immutableMap = new HashMap<String, List<String>>(metadata.size());
		for(final var entry : metadata.entrySet())
			immutableMap.put(entry.getKey(), Collections.unmodifiableList(entry.getValue()));
		//return the new unmodifiable map
		return Collections.unmodifiableMap(immutableMap);
	}
	// ================================================== ==================================================
	//                                    AbstractBuilder IMPLEMENTATION
	// ================================================== ==================================================
	/**
	 * {@code abstract} base for {@link ResourceRequest.Builder} and
	 * {@link ResourceResponse.Builder}.
	 */
	abstract static sealed class AbstractBuilder
			permits ResourceRequest.Builder, ResourceResponse.Builder
	{
		// ==================================================
		protected final URI                       uri;
		protected final Map<String, List<String>> metadata;
		protected       byte[]                    data;
		// ==================================================
		protected AbstractBuilder(@NotNull URI uri) throws NullPointerException {
			this.uri      = Objects.requireNonNull(uri);
			this.metadata = new HashMap<>();
			this.data     = new byte[0];
		}
		// ==================================================
		/**
		 * Adds a metadata entry to this {@link ResourceMessage}.
		 * @param metadataName  The name of the metadata entry.
		 * @param metadataValue The value of the metadata entry.
		 * @return The current {@link AbstractBuilder} instance for method chaining.
		 * @throws NullPointerException If any argument is {@code null}.
		 * @throws IllegalStateException If this {@link AbstractBuilder} already built a {@link ResourceMessage}.
		 */
		public @NotNull AbstractBuilder add(@NotNull String metadataName, @NotNull String metadataValue)
				throws NullPointerException, IllegalStateException
		{
			//not null and not build assertions
			Objects.requireNonNull(metadataName);
			Objects.requireNonNull(metadataValue);

			//add the metadata entry value
			metadataName = metadataName.toLowerCase(Locale.ROOT);
			this.metadata.computeIfAbsent(metadataName, __ -> new ArrayList<>()).add(metadataValue);
			return this;
		}
		// --------------------------------------------------
		/**
		 * Adds multiple metadata values for the specified metadata name.
		 * @param metadataName The name of the metadata entry.
		 * @param metadataValues A collection of metadata values to add.
		 * @return The current {@link AbstractBuilder} instance for method chaining.
		 * @throws NullPointerException If any argument is {@code null}.
		 * @throws IllegalStateException If this {@link AbstractBuilder} already built a {@link ResourceMessage}.
		 */
		public AbstractBuilder addAll(
				@NotNull String metadataName, @NotNull Collection<String> metadataValues)
				throws NullPointerException, IllegalStateException
		{
			//not null assertions
			Objects.requireNonNull(metadataName);
			Objects.requireNonNull(metadataValues);

			//add all metadata values to the specified metadata name
			metadataName = metadataName.toLowerCase(Locale.ROOT);
			this.metadata.computeIfAbsent(metadataName, __ -> new ArrayList<>()).addAll(metadataValues);
			return this;
		}

		/**
		 * Adds multiple metadata values for the specified metadata name from a {@link JsonArray}.
		 * @param metadataName The name of the metadata entry.
		 * @param metadataValues A {@link JsonArray} containing metadata values to add.
		 * @return The current {@link AbstractBuilder} instance for method chaining.
		 * @throws NullPointerException If any argument is {@code null}.
		 * @throws IllegalStateException If this {@link AbstractBuilder} already built a {@link ResourceMessage}.
		 */
		public AbstractBuilder addAll(@NotNull String metadataName, @NotNull JsonArray metadataValues)
				throws NullPointerException, IllegalStateException
		{
			//not null assertions
			Objects.requireNonNull(metadataName);
			Objects.requireNonNull(metadataValues);

			//add all metadata values to the specified metadata name
			metadataName = metadataName.toLowerCase(Locale.ROOT);
			final var list = this.metadata.computeIfAbsent(metadataName, __ -> new ArrayList<>());
			for(final var value : metadataValues.asList()) {
				if(value == null || !value.isJsonPrimitive()) continue;
				list.add(value.getAsString());
			}
			return this;
		}
		// --------------------------------------------------
		/**
		 * Sets a metadata entry for this {@link ResourceMessage}, replacing any existing
		 * values associated with the specified metadata name.
		 * @param metadataName  The name of the metadata entry.
		 * @param metadataValue The value of the metadata entry.
		 * @return The current {@link AbstractBuilder} instance for method chaining.
		 * @throws NullPointerException If any argument is {@code null}.
		 * @throws IllegalStateException If this {@link AbstractBuilder} already built a {@link ResourceMessage}.
		 */
		public @NotNull AbstractBuilder set(@NotNull String metadataName, @NotNull String metadataValue)
				throws NullPointerException, IllegalStateException
		{
			//not null and not build assertions
			Objects.requireNonNull(metadataName);
			Objects.requireNonNull(metadataValue);

			//set the metadata entry value
			metadataName = metadataName.toLowerCase(Locale.ROOT);
			final var list = new ArrayList<String>();
			list.add(metadataValue);
			this.metadata.put(metadataName, list);
			return this;
		}
		// --------------------------------------------------
		/**
		 * Sets the raw byte data for the resource response.
		 * @param data A byte array containing the resource data.
		 * @return The current {@link AbstractBuilder} instance for method chaining.
		 * @throws NullPointerException If the argument is {@code null}.
		 * @throws IllegalStateException If this {@link AbstractBuilder} already built a
		 * {@link ResourceMessage}.
		 * @apiNote The underlying {@code byte[]}'s contents are <b>not to be modified!</b>
		 *          The {@code byte[]} must remain unchanged for the rest of its lifespan.
		 */
		public AbstractBuilder setData(byte @NotNull [] data)
				throws NullPointerException, IllegalStateException
		{
			//not null and not build assertions
			Objects.requireNonNull(data);
			//set the data byte array
			this.data = data;
			return this;
		}
		// ==================================================
	}
	// ================================================== ==================================================
}
