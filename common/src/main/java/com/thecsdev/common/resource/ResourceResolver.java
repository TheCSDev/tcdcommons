package com.thecsdev.common.resource;

import com.thecsdev.common.resource.protocol.FileProtocolHandler;
import com.thecsdev.common.resource.protocol.ProtocolHandler;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.*;
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
	static { bootstrap(); }
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
	public static final @NotNull CompletableFuture<ResourceResponse> fetchAsync(@NotNull URI uri) throws NullPointerException {
		return fetchAsync(new ResourceRequest.Builder(uri).build());
	}

	/**
	 * Fetches a resource based on the provided {@link ResourceRequest} asynchronously.
	 * @param request The {@link ResourceRequest} containing details about the resource to fetch.
	 * @return A {@link CompletableFuture} that will complete with a {@link ResourceResponse}
	 *         containing the fetched resource data.
	 * @throws NullPointerException If the argument is {@code null}.
	 */
	public static final @NotNull CompletableFuture<ResourceResponse> fetchAsync(
			@NotNull ResourceRequest request) throws NullPointerException
	{
		//not null requirements
		Objects.requireNonNull(request);

		//obtain scheme name, and handle 'null' schemes
		final @Nullable var scheme = request.getUri().getScheme();
		if(scheme == null)
			return CompletableFuture.failedFuture(
					new UnsupportedOperationException("Missing scheme for URI: " + request));

		//obtain protocol handler for scheme
		final @Nullable var protocolHandler = PROTOCOL_HANDLERS.get(scheme.toLowerCase(Locale.ENGLISH));
		if(protocolHandler == null)
			return CompletableFuture.failedFuture(
					new UnsupportedOperationException("No protocol handler is registered for scheme: " + scheme));

		//delegate handling to protocol handler
		return protocolHandler.handle(request)
				.thenApply(rss ->
				{
					//ensure returned response has matching URI
					if(rss.getUri() != request.getUri()) {
						final var message = String.format(
								"%s for scheme '%s' returned %s with mismatched %s! Expected: \"%s\", Got: \"%s\"",
								ProtocolHandler.class.getSimpleName(),
								scheme,
								ResourceResponse.class.getSimpleName(),
								URI.class.getSimpleName(),
								request, rss.getUri());
						throw new IllegalStateException(message);
					}
					//return the response
					return rss;
				});
	}
	// ==================================================
	/**
	 * Internal method for bootstrapping the {@link ResourceResolver} and
	 * registers default protocol handlers.
	 * @apiNote Called automatically. Do not call this yourself.
	 */
	public static final @ApiStatus.Internal void bootstrap() {
		registerProtocolHandler("file", FileProtocolHandler.INSTANCE);
	}
	// ==================================================
}
