package com.thecsdev.common.resource;

import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents the response received after fetching a resource from a specified {@link URI}.
 * @see ResourceResolver#fetchAsync(URI)
 */
public final class ResourceResponse
{
	// ================================================== ==================================================
	//                                   ResourceResponse IMPLEMENTATION
	// ================================================== ==================================================
	private final URI                       uri;      //where the resource was fetched from
	private final int                       status;   //https://en.wikipedia.org/wiki/Exit_status
	private final Map<String, List<String>> metadata; //stuff like http headers and system metadata
	private final byte[]                    data;     //the resource data
	// ==================================================
	private ResourceResponse(
			@NotNull URI resourceUri, int status,
			@NotNull Map<String, List<String>> metadata,
			byte @NotNull [] data) throws NullPointerException
	{
		this.uri      = Objects.requireNonNull(resourceUri);
		this.status   = status;
		this.metadata = Objects.requireNonNull(metadata);
		this.data     = Objects.requireNonNull(data);
	}
	// ==================================================
	/**
	 * Returns the {@link URI} where the resource was fetched from.
	 */
	public final @NotNull URI getUri() { return this.uri; }

	/**
	 * Returns the status code representing the outcome of the resource fetch
	 * operation. The interpretation of this status code may differ depending on the
	 * protocol specified in the {@link URI}.
	 */
	public final int getStatus() { return this.status; }

	/**
	 * Returns an unmodifiable {@link Map} containing metadata associated with the
	 * resource. This may include HTTP headers, system metadata, or other relevant
	 * information. Meanings vary based on {@link URI#getScheme()}.
	 */
	public final @NotNull Map<String, List<String>> getMetadata() { return this.metadata; }

	/**
	 * Returns the {@code byte[]} containing the raw byte data of the fetched resource.
	 */
	public final byte @NotNull [] getData() { return this.data; }
	// ================================================== ==================================================
	//                                            Builder IMPLEMENTATION
	// ================================================== ==================================================
	/**
	 * A builder class for constructing instances of {@link ResourceResponse}.
	 */
	public static final class Builder
	{
		// ==================================================
		private final URI                       uri;
		private       int                       status   = 0;
		private final Map<String, List<String>> metadata = new HashMap<>();
		private       byte[]                    data     = new byte[0];
		// ==================================================
		public Builder(@NotNull URI uri) throws NullPointerException {
			this.uri = Objects.requireNonNull(uri);
		}
		public Builder(@NotNull URI uri, int status) throws NullPointerException {
			this.uri    = Objects.requireNonNull(uri);
			this.status = status;
		}
		// ==================================================
		/**
		 * Sets the status code for this {@link ResourceResponse}.
		 * @param status The status code to set.
		 * @return The current {@link Builder} instance for method chaining.
		 */
		public Builder setStatus(int status) { this.status = status; return this; }

		/**
		 * Sets the metadata for the resource response.
		 * @param metadata A map containing metadata key-value pairs.
		 * @return The current {@link Builder} instance for method chaining.
		 * @throws NullPointerException If the argument is {@code null}.
		 */
		public Builder setMetadata(@NotNull Map<String, List<String>> metadata) throws NullPointerException {
			this.metadata.clear();
			this.metadata.putAll(Objects.requireNonNull(metadata));
			return this;
		}

		/**
		 * Sets the raw byte data for the resource response.
		 * @param data A byte array containing the resource data.
		 * @return The current {@link Builder} instance for method chaining.
		 * @throws NullPointerException If the argument is {@code null}.
		 * @apiNote The underlying {@code byte[]}'s contents are <b>not to be modified!</b>
		 *          The {@code byte[]} must remain unchanged for the rest of its lifespan.
		 */
		public Builder setData(byte @NotNull [] data) throws NullPointerException {
			this.data = Objects.requireNonNull(data);
			return this;
		}
		// --------------------------------------------------
		/**
		 * Builds and returns a new {@link ResourceResponse} instance based on the
		 * configured properties.
		 * @return A new {@link ResourceResponse} instance.
		 */
		public ResourceResponse build() {
			return new ResourceResponse(this.uri, this.status, this.metadata, this.data);
		}
		// ==================================================
	}
	// ================================================== ==================================================
}
