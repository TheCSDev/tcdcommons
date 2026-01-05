package com.thecsdev.common.resource;

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
	// ==================================================
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
		metadataName = metadataName.toLowerCase(Locale.ENGLISH);
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
		metadataName = metadataName.toLowerCase(Locale.ENGLISH);

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
		//create new map and populare it
		final var immutableMap = new HashMap<String, List<String>>(metadata.size());
		for(final var entry : metadata.entrySet())
			immutableMap.put(entry.getKey(), Collections.unmodifiableList(entry.getValue()));
		//return the new unmodifiable map
		return Collections.unmodifiableMap(immutableMap);
	}
	// ==================================================
}
