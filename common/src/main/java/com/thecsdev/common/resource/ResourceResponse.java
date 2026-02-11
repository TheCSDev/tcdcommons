package com.thecsdev.common.resource;

import com.google.gson.JsonArray;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Represents the response received after fetching a resource from a specified {@link URI}.
 * @see ResourceResolver#fetchAsync(URI)
 */
public final class ResourceResponse extends ResourceMessage
{
	// ================================================== ==================================================
	//                                   ResourceResponse IMPLEMENTATION
	// ================================================== ==================================================
	private final int status; //https://en.wikipedia.org/wiki/Exit_status
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
	public static final class Builder extends ResourceMessage.AbstractBuilder
	{
		// ==================================================
		private int status = 0;
		// --------------------------------------------------
		private @Nullable ResourceResponse build; //for if already built
		// ==================================================
		public Builder(@NotNull URI uri) throws NullPointerException { super(uri); }
		public Builder(@NotNull URI uri, int status) throws NullPointerException {
			super(uri);
			this.status = status;
		}
		// ==================================================
		/**
		 * Sets the status code for this {@link ResourceResponse}.
		 * @param status The status code to set.
		 * @return The current {@link Builder} instance for method chaining.
		 * @throws IllegalStateException If this {@link Builder} already built a {@link ResourceResponse}.
		 */
		public final Builder setStatus(int status) throws IllegalStateException {
			assertNotBuilt();
			this.status = status; return this;
		}
		// --------------------------------------------------
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
				this.build = new ResourceResponse(super.uri, this.status, super.metadata, super.data);
			return this.build;
		}
		// ==================================================
	}
	// ================================================== ==================================================
}
