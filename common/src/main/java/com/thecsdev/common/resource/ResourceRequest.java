package com.thecsdev.common.resource;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a request to fetch a resource from a specified {@link URI}.
 * @see ResourceResolver#fetchAsync(URI)
 */
public final class ResourceRequest extends ResourceMessage
{
	// ================================================== ==================================================
	//                                    ResourceRequest IMPLEMENTATION
	// ================================================== ==================================================
	private ResourceRequest(
			@NotNull URI resourceUri,
			@NotNull Map<String, List<String>> metadata,
			byte @NotNull [] data)
			throws NullPointerException
	{
		super(resourceUri, metadata, data);
	}
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
		// --------------------------------------------------
		private @Nullable ResourceRequest build; //for if already built
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
		 * @throws IllegalStateException If this {@link Builder} already built a {@link ResourceRequest}.
		 */
		public final @NotNull Builder setMetadata(@NotNull Map<String, List<String>> metadata)
				throws NullPointerException, IllegalStateException
		{
			//not null and not build assertions
			Objects.requireNonNull(metadata);
			assertNotBuilt();

			//clear and put all new metadata properties
			this.metadata.clear();
			this.metadata.putAll(metadata);
			return this;
		}

		/**
		 * Adds a metadata entry to this {@link ResourceRequest}.
		 * @param metadataName  The name of the metadata entry.
		 * @param metadataValue The value of the metadata entry.
		 * @return The current {@link Builder} instance for method chaining.
		 * @throws NullPointerException If any argument is {@code null}.
		 * @throws IllegalStateException If this {@link Builder} already built a {@link ResourceRequest}.
		 * @see #setMetadata(Map)
		 */
		public final @NotNull Builder add(@NotNull String metadataName, @NotNull String metadataValue)
				throws NullPointerException, IllegalStateException
		{
			//not null and not build assertions
			Objects.requireNonNull(metadataName);
			Objects.requireNonNull(metadataValue);
			assertNotBuilt();

			//add the metadata entry value
			this.metadata.computeIfAbsent(metadataName, __ -> new java.util.ArrayList<>()).add(metadataValue);
			return this;
		}

		/**
		 * Sets a metadata entry for this {@link ResourceRequest}, replacing any existing
		 * values associated with the specified metadata name.
		 * @param metadataName  The name of the metadata entry.
		 * @param metadataValue The value of the metadata entry.
		 * @return The current {@link Builder} instance for method chaining.
		 * @throws NullPointerException If any argument is {@code null}.
		 * @throws IllegalStateException If this {@link Builder} already built a {@link ResourceRequest}.
		 * @see #setMetadata(Map)
		 */
		public final @NotNull Builder set(@NotNull String metadataName, @NotNull String metadataValue)
				throws NullPointerException, IllegalStateException
		{
			//not null and not build assertions
			Objects.requireNonNull(metadataName);
			Objects.requireNonNull(metadataValue);
			assertNotBuilt();

			//set the metadata entry value
			final var list = new java.util.ArrayList<String>();
			list.add(metadataValue);
			this.metadata.put(metadataName, list);
			return this;
		}
		// --------------------------------------------------
		/**
		 * Sets the raw byte data for this {@link ResourceRequest}.
		 * @param data A byte array containing the resource request data.
		 * @return The current {@link Builder} instance for method chaining.
		 * @throws NullPointerException If the argument is {@code null}.
		 * @throws IllegalStateException If this {@link Builder} already built a {@link ResourceRequest}.
		 * @apiNote The underlying {@code byte[]}'s contents are <b>not to be modified!</b>
		 *          The {@code byte[]} must remain unchanged for the rest of its lifespan.
		 */
		public final @NotNull Builder setData(byte @NotNull [] data)
				throws NullPointerException, IllegalStateException
		{
			//not null and not build assertions
			Objects.requireNonNull(data);
			assertNotBuilt();
			//set the data byte array
			this.data = data;
			return this;
		}
		// ==================================================
		private final @ApiStatus.Internal void assertNotBuilt() {
			if(this.build != null)
				throw new IllegalStateException("Already built.");
		}
		// --------------------------------------------------
		/**
		 * Builds and returns a new {@link ResourceRequest} instance based on the
		 * current state of the builder.
		 * @return A new {@link ResourceRequest} instance.
		 */
		public final @NotNull ResourceRequest build() {
			if(this.build == null)
				this.build = new ResourceRequest(this.uri, this.metadata, this.data);
			return this.build;
		}
		// ==================================================
	}
	// ================================================== ==================================================
}