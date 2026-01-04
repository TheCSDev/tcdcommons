package com.thecsdev.common.resource;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents the response received after fetching a resource from a specified {@link URI}.
 * @see ResourceResolver#fetchAsync(URI)
 */
public final class ResourceResponse extends ResourceMessage
{
	// ================================================== ==================================================
	//                                   ResourceResponse IMPLEMENTATION
	// ================================================== ==================================================
	private final int status;   //https://en.wikipedia.org/wiki/Exit_status
	// --------------------------------------------------
	private final int hashCode;
	// ==================================================
	private ResourceResponse(
			@NotNull URI resourceUri,
			int status,
			@NotNull Map<String, List<String>> metadata,
			byte @NotNull [] data)
			throws NullPointerException
	{
		super(resourceUri, metadata, data);
		this.status   = status;
		this.hashCode = 31 * super.hashCode() + Integer.hashCode(status);
	}
	// ==================================================
	public final @Override int hashCode() { return this.hashCode; }
	public final @Override boolean equals(@Nullable Object obj) {
		if(!super.equals(obj)) return false;
		return this.status == ((ResourceResponse) obj).status;
	}
	// ==================================================
	/**
	 * Returns the status code representing the outcome of the resource fetch
	 * operation. The interpretation of this status code may differ depending on the
	 * protocol specified in the {@link URI}.
	 */
	public final int getStatus() { return this.status; }
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
		// --------------------------------------------------
		private @Nullable ResourceResponse build; //for if already built
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
		 * @throws IllegalStateException If this {@link Builder} already built a {@link ResourceResponse}.
		 */
		public Builder setStatus(int status) throws IllegalStateException {
			assertNotBuilt();
			this.status = status; return this;
		}

		/**
		 * Sets the metadata for the resource response.
		 * @param metadata A map containing metadata key-value pairs.
		 * @return The current {@link Builder} instance for method chaining.
		 * @throws NullPointerException If the argument is {@code null}.
		 * @throws IllegalStateException If this {@link Builder} already built a {@link ResourceResponse}.
		 */
		public Builder setMetadata(@NotNull Map<String, List<String>> metadata)
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
		 * Adds a metadata entry to the resource response.
		 * @param metadataName  The name of the metadata entry.
		 * @param metadataValue The value of the metadata entry.
		 * @return The current {@link Builder} instance for method chaining.
		 * @throws NullPointerException If any argument is {@code null}.
		 * @throws IllegalStateException If this {@link Builder} already built a {@link ResourceResponse}.
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
		 * Sets a metadata entry for the resource response, replacing any existing
		 * values associated with the specified metadata name.
		 * @param metadataName  The name of the metadata entry.
		 * @param metadataValue The value of the metadata entry.
		 * @return The current {@link Builder} instance for method chaining.
		 * @throws NullPointerException If any argument is {@code null}.
		 * @throws IllegalStateException If this {@link Builder} already built a {@link ResourceResponse}.
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
		 * Sets the raw byte data for the resource response.
		 * @param data A byte array containing the resource data.
		 * @return The current {@link Builder} instance for method chaining.
		 * @throws NullPointerException If the argument is {@code null}.
		 * @throws IllegalStateException If this {@link Builder} already built a {@link ResourceResponse}.
		 * @apiNote The underlying {@code byte[]}'s contents are <b>not to be modified!</b>
		 *          The {@code byte[]} must remain unchanged for the rest of its lifespan.
		 */
		public Builder setData(byte @NotNull [] data) throws NullPointerException, IllegalStateException
		{
			//not null and not build assertions
			Objects.requireNonNull(data);
			assertNotBuilt();
			//set the data byte array
			this.data = data;
			return this;
		}
		// ==================================================
		private final void assertNotBuilt() throws IllegalStateException {
			if(this.build != null)
				throw new IllegalStateException("Already built.");
		}
		// --------------------------------------------------
		/**
		 * Builds and returns a new {@link ResourceResponse} instance based on the
		 * configured properties.
		 * @return A new {@link ResourceResponse} instance.
		 */
		public ResourceResponse build() {
			if(this.build == null)
				this.build = new ResourceResponse(this.uri, this.status, this.metadata, this.data);
			return this.build;
		}
		// ==================================================
	}
	// ================================================== ==================================================
}
