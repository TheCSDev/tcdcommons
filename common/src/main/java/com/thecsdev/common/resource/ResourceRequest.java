package com.thecsdev.common.resource;

import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a request to fetch a resource from a specified {@link URI}.
 * @see ResourceResolver#fetchAsync(URI)
 */
public final class ResourceRequest
{
	// ================================================== ==================================================
	//                                    ResourceRequest IMPLEMENTATION
	// ================================================== ==================================================
	private final URI                       uri;      //where the resource is to be fetched from
	private final Map<String, List<String>> metadata; //stuff like http headers and system metadata
	private final byte[]                    data;     //the resource request data
	// ==================================================
	private ResourceRequest(
			@NotNull URI resourceUri,
			@NotNull Map<String, List<String>> metadata,
			byte @NotNull [] data) throws NullPointerException
	{
		this.uri      = Objects.requireNonNull(resourceUri);
		this.metadata = Objects.requireNonNull(metadata);
		this.data     = Objects.requireNonNull(data);
	}
	// ==================================================
	/**
	 * Returns the {@link URI} where the resource is to be fetched from.
	 */
	public final @NotNull URI getUri() { return this.uri; }

	/**
	 * Returns an unmodifiable {@link Map} containing metadata associated with the
	 * resource request. This may include HTTP headers, system metadata, or other relevant
	 * information. Meanings vary based on {@link URI#getScheme()}.
	 */
	public final @NotNull Map<String, List<String>> getMetadata() { return this.metadata; }

	/**
	 * Returns the {@code byte[]} containing the raw byte data of the {@link ResourceRequest}.
	 */
	public final byte @NotNull [] getData() { return this.data; }
	// ================================================== ==================================================
	//                                            Builder IMPLEMENTATION
	// ================================================== ==================================================
	/**
	 * A builder for creating instances of {@link ResourceRequest}.
	 */
	public static final class Builder
	{
		// ==================================================
		private final URI                       uri;
		private final Map<String, List<String>> metadata = new HashMap<>();
		private       byte[]                    data     = new byte[0];
		// ==================================================
		public Builder(@NotNull URI uri) throws NullPointerException {
			this.uri = Objects.requireNonNull(uri);
		}
		// ==================================================
		/**
		 * Sets the metadata for this {@link ResourceRequest}.
		 * @param metadata A {@link Map} containing metadata key-value pairs.
		 * @return The current {@link Builder} instance for method chaining.
		 * @throws NullPointerException If the argument is {@code null}.
		 */
		public final @NotNull Builder setMetadata(@NotNull Map<String, List<String>> metadata) throws NullPointerException {
			this.metadata.clear();
			this.metadata.putAll(Objects.requireNonNull(metadata));
			return this;
		}

		/**
		 * Sets the raw byte data for this {@link ResourceRequest}.
		 * @param data A byte array containing the resource request data.
		 * @return The current {@link Builder} instance for method chaining.
		 * @throws NullPointerException If the argument is {@code null}.
		 * @apiNote The underlying {@code byte[]}'s contents are <b>not to be modified!</b>
		 *          The {@code byte[]} must remain unchanged for the rest of its lifespan.
		 */
		public final @NotNull Builder setData(byte @NotNull [] data) throws NullPointerException {
			this.data = Objects.requireNonNull(data);
			return this;
		}
		// ==================================================
		/**
		 * Builds and returns a new {@link ResourceRequest} instance based on the
		 * current state of the builder.
		 * @return A new {@link ResourceRequest} instance.
		 */
		public final @NotNull ResourceRequest build() {
			return new ResourceRequest(this.uri, this.metadata, this.data);
		}
		// ==================================================
	}
	// ================================================== ==================================================
}