package com.thecsdev.common.resource;

import com.google.gson.JsonArray;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.*;

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
	public static final class Builder extends ResourceMessage.AbstractBuilder
	{
		// ==================================================
		private @Nullable ResourceRequest build; //for if already built
		// ==================================================
		public Builder(@NotNull URI uri) throws NullPointerException { super(uri); }
		// ==================================================
		public final @NotNull @Override Builder add(
				@NotNull String metadataName, @NotNull String metadataValue)
				throws NullPointerException, IllegalStateException
		{
			assertNotBuilt();
			super.add(metadataName, metadataValue);
			return this;
		}
		// --------------------------------------------------
		public final @Override Builder addAll(
				@NotNull String metadataName, @NotNull Collection<String> metadataValues)
				throws NullPointerException, IllegalStateException {
			assertNotBuilt();
			super.addAll(metadataName, metadataValues);
			return this;
		}
		public final @Override Builder addAll(
				@NotNull String metadataName, @NotNull JsonArray metadataValues)
				throws NullPointerException, IllegalStateException {
			assertNotBuilt();
			super.addAll(metadataName, metadataValues);
			return this;
		}
		// --------------------------------------------------
		public final @NotNull @Override Builder set(
				@NotNull String metadataName, @NotNull String metadataValue)
				throws NullPointerException, IllegalStateException
		{
			assertNotBuilt();
			super.set(metadataName, metadataValue);
			return this;
		}
		// --------------------------------------------------
		public final Builder setData(byte @NotNull [] data)
				throws NullPointerException, IllegalStateException {
			assertNotBuilt();
			super.setData(data);
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
				this.build = new ResourceRequest(super.uri, super.metadata, super.data);
			return this.build;
		}
		// ==================================================
	}
	// ================================================== ==================================================
}