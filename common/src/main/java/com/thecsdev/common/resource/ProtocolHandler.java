package com.thecsdev.common.resource;

import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

/**
 * An interface representing a handler for specific {@link URI} protocols.
 * Implementations of this interface are responsible for processing {@link URI}s
 * that match their supported protocol schemes.
 * @see ResourceResolver
 * @see ResourceResponse
 */
public interface ProtocolHandler
{
	// ==================================================
	/**
	 * Determines if this {@link ProtocolHandler} can handle the given {@link URI}.
	 * @param uri The URI to check.
	 * @return {@code true} if this handler can process the URI; {@code false} otherwise.
	 */
	public boolean matches(@NotNull URI uri);
	// --------------------------------------------------
	/**
	 * Fetches a resource from the specified {@link URI} asynchronously.
	 * @param uri The {@link URI} of the resource to fetch.
	 * @return A {@link CompletableFuture} that will complete with a {@link ResourceResponse}
	 *         containing the fetched resource data.
	 * @apiNote The resulting {@link ResourceResponse#getUri()} <b>must match</b>
	 *          the {@link URI} argument that's passed to this method.
	 */
	public @NotNull CompletableFuture<ResourceResponse> handle(@NotNull URI uri);
	// ==================================================
}
