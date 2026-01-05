package com.thecsdev.common.resource;

import com.thecsdev.common.resource.protocol.FileProtocolHandler;
import com.thecsdev.common.resource.protocol.HttpProtocolHandler;
import com.thecsdev.common.resource.protocol.ProtocolHandler;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A utility class for resolving and fetching resources from various {@link URI}s.
 * <p>
 * This class provides methods to fetch resources asynchronously based on their {@link URI}s.
 * Different protocols can be handled by their corresponding {@link ProtocolHandler}s.
 * </p>
 * @see ResourceResponse
 * @see ProtocolHandler
 */
@ApiStatus.Experimental
public final class ResourceResolver
{
	// ==================================================
	/**
	 * A mapping of protocol schemes to their corresponding {@link ProtocolHandler}s.
	 * @see URI#getScheme()
	 */
	@ApiStatus.Internal
	private static final HashMap<String, ProtocolHandler> PROTOCOL_HANDLERS = new HashMap<>();
	// --------------------------------------------------
	/**
	 * A mapping of currently outgoing {@link ResourceRequest}s to their corresponding
	 * {@link CompletableFuture}s that will complete with {@link ResourceResponse}s.
	 * <p>
	 * This is used to track {@link ResourceRequest}s that are in progress to avoid duplicate fetches.
	 */
	@ApiStatus.Internal
	private static final ConcurrentHashMap<ResourceRequest, CompletableFuture<ResourceResponse>> OUTGOING_REQUESTS = new ConcurrentHashMap<>();
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

		//clean-up for if any dangling requests remain - shouldn't happen
		OUTGOING_REQUESTS.entrySet().removeIf(entry -> entry.getValue().isDone());

		//if the request isn't already being performed, execute it
		return OUTGOING_REQUESTS.computeIfAbsent(request, req ->
		{
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
					.thenApply(res ->
					{
						//ensure returned response has matching URI
						if(!Objects.equals(res.getUri(), req.getUri())) {
							final var message = String.format(
									"%s for scheme '%s' returned %s with mismatched %s! Expected: \"%s\", Got: \"%s\"",
									ProtocolHandler.class.getSimpleName(),
									scheme,
									ResourceResponse.class.getSimpleName(),
									URI.class.getSimpleName(),
									request.getUri(), res.getUri());
							throw new IllegalStateException(message);
						}
						//return the response
						return res;
					});
		})
		.whenComplete((res, throwable) -> {
			//request done - remove it from outgoing requests
			OUTGOING_REQUESTS.remove(request);
		});
	}
	// ==================================================
	/**
	 * Internal method for bootstrapping the {@link ResourceResolver} and
	 * registers default protocol handlers.
	 * @apiNote Called automatically. Do not call this yourself.
	 */
	public static final @ApiStatus.Internal void bootstrap() {
		registerProtocolHandler("file",  FileProtocolHandler.INSTANCE);
		registerProtocolHandler("http",  HttpProtocolHandler.INSTANCE);
		registerProtocolHandler("https", HttpProtocolHandler.INSTANCE);
	}
	// ==================================================
}
