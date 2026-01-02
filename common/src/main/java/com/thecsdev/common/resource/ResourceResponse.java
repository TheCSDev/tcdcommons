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
	// ==================================================
	private final URI                           uri;      //where the resource was fetched from
	private final int                           status;   //https://en.wikipedia.org/wiki/Exit_status
	private final HashMap<String, List<String>> metadata; //stuff like http headers and system metadata
	private final byte[]                        data;     //the resource data
	// ==================================================
	public ResourceResponse(@NotNull URI resourceUri, int status) throws NullPointerException {
		this(resourceUri, status, new byte[0]);
	}
	public ResourceResponse(@NotNull URI resourceUri, int status, byte @NotNull [] data) throws NullPointerException {
		this(resourceUri, status, new HashMap<>(), data);
	}
	public ResourceResponse(@NotNull URI resourceUri, int status, @NotNull HashMap<String, List<String>> metadata, byte @NotNull [] data) throws NullPointerException {
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
	 * Returns a map containing metadata associated with the resource. This may
	 * include HTTP headers, system metadata, or other relevant information.
	 */
	public final @NotNull Map<String, List<String>> getMetadata() { return this.metadata; }

	/**
	 * Returns the raw byte data of the fetched resource.
	 */
	public final byte @NotNull [] getData() { return this.data; }
	// ==================================================
}
