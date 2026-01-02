package com.thecsdev.common.resource;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * A utility class for resolving and fetching resources from various {@link URI}s.
 * <p>
 * This class provides methods to fetch resources asynchronously based on their {@link URI}s.
 * Different protocols can be handled by their corresponding {@link ProtocolHandler}s.
 * </p>
 * @see ResourceResponse
 * @see ProtocolHandler
 */
public final class ResourceResolver
{
	// ==================================================
	/**
	 * A mapping of protocol schemes to their corresponding {@link ProtocolHandler}s.
	 * @see URI#getScheme()
	 */
	private static final HashMap<String, ProtocolHandler> PROTOCOL_HANDLERS = new HashMap<>();
	// ==================================================
	private ResourceResolver() {}
	// ==================================================
	/**
	 * Registers a {@link ProtocolHandler} for a specific protocol scheme.
	 * @param scheme The protocol scheme (e.g., "http", "https", "file").
	 * @param handler The {@link ProtocolHandler} responsible for handling the specified scheme.
	 * @throws NullPointerException If an argument is {@code null}.
	 */
	public static final void registerProtocolHandler(@NotNull String scheme, @NotNull ProtocolHandler handler) throws NullPointerException
	{
		//not null requirements
		Objects.requireNonNull(scheme);
		Objects.requireNonNull(handler);

		//register protocol handler
		PROTOCOL_HANDLERS.put(scheme.toLowerCase(Locale.ENGLISH), handler);
	}
	// --------------------------------------------------
	/**
	 * Fetches a resource from the specified {@link URI} asynchronously.
	 * @param uri The {@link URI} of the resource to fetch.
	 * @return A {@link CompletableFuture} that will complete with a {@link ResourceResponse}
	 *         containing the fetched resource data.
	 * @throws NullPointerException If the argument is {@code null}.
	 */
	public static final @NotNull CompletableFuture<ResourceResponse> fetchAsync(@NotNull URI uri) throws NullPointerException
	{
		//not null requirements
		Objects.requireNonNull(uri);

		//obtain scheme name, and handle 'null' schemes
		final @Nullable var scheme = uri.getScheme();
		if(scheme == null)
			return CompletableFuture.failedFuture(
					new UnsupportedOperationException("Missing scheme for URI: " + uri));

		//obtain protocol handler for scheme
		final @Nullable var protocolHandler = PROTOCOL_HANDLERS.get(scheme.toLowerCase(Locale.ENGLISH));
		if(protocolHandler == null)
			return CompletableFuture.failedFuture(
					new UnsupportedOperationException("No protocol handler is registered for scheme: " + scheme));

		//delegate handling to protocol handler
		return protocolHandler.handle(uri)
				.thenApply(rss ->
				{
					//ensure returned response has matching URI
					if(rss.getUri() != uri) {
						final var message = String.format(
								"%s for scheme '%s' returned %s with mismatched %s! Expected: \"%s\", Got: \"%s\"",
								ProtocolHandler.class.getSimpleName(),
								scheme,
								ResourceResponse.class.getSimpleName(),
								URI.class.getSimpleName(),
								uri, rss.getUri());
						throw new IllegalStateException(message);
					}
					//return the response
					return rss;
				});
	}
	// ==================================================
}
