package com.thecsdev.common.resource.protocol;

import com.thecsdev.common.resource.ResourceRequest;
import com.thecsdev.common.resource.ResourceResolver;
import com.thecsdev.common.resource.ResourceResponse;
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
	 * Handles the given {@link ResourceRequest} and returns a {@link CompletableFuture}
	 * that will complete with a {@link ResourceResponse}.
	 * @param request The {@link ResourceRequest} to handle.
	 * @throws NullPointerException If the argument is {@code null}.
	 * @throws IllegalArgumentException If {@link URI#getScheme()} of the {@link ResourceRequest#getUri()} is unsupported.
	 */
	public @NotNull CompletableFuture<ResourceResponse> handle(@NotNull ResourceRequest request)
			throws NullPointerException, IllegalArgumentException;
	// ==================================================
}
