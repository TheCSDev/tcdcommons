package com.thecsdev.common.resource.protocol;

import com.thecsdev.common.resource.ResourceRequest;
import com.thecsdev.common.resource.ResourceResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * A {@link ProtocolHandler} implementation for handling "http" and "https" protocol
 * {@link URI}s. This handler processes HTTP and HTTPS requests and returns their
 * responses as {@link ResourceResponse} objects.
 *
 * @see URI#getScheme()
 */
public class HttpProtocolHandler implements ProtocolHandler
{
	// ==================================================
	/**
	 * The main singleton instance of {@link HttpProtocolHandler}.
	 */
	public static final HttpProtocolHandler INSTANCE = new HttpProtocolHandler();
	// --------------------------------------------------
	/**
	 * The HTTP header key used to specify the HTTP method (e.g., GET, POST, PUT, DELETE)
	 * for the request. If not provided, the default method is "GET".
	 */
	public static final String HEADER_HTTP_METHOD = "x-http-method";
	// ==================================================
	private HttpProtocolHandler() {}
	// ==================================================
	public final @Override boolean matches(@NotNull URI uri) {
		final @Nullable var scheme = uri.getScheme();
		return (scheme != null && (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https")));
	}
	// --------------------------------------------------
	public final @Override @NotNull CompletableFuture<ResourceResponse> handle(@NotNull ResourceRequest request) throws NullPointerException, IllegalArgumentException
	{
		//require not null for the argument
		Objects.requireNonNull(request);
		//ensure this handler can process the request URI
		if(!matches(request.getUri()))
			throw new IllegalArgumentException("Cannot handle URI with scheme: " + request.getUri().getScheme());

		//handle the HTTP request operation asynchronously
		return CompletableFuture.supplyAsync(() ->
		{
			//create client instance and try to send the request
			try(final var client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build())
			{
				//build the client request and send it
				final var clientRequest = HttpRequest.newBuilder(request.getUri())
					.method(request.get(HEADER_HTTP_METHOD, "GET"),
							HttpRequest.BodyPublishers.ofByteArray(request.getData()));

				for(final var headerEntry : request.getMetadata().entrySet())
					for(final var headerValue : headerEntry.getValue())
						clientRequest.header(headerEntry.getKey(), headerValue);

				//send the request and get the response
				final var clientResponse = client.send(
						clientRequest.build(),
						HttpResponse.BodyHandlers.ofByteArray());

				//complete the future
				final var rrBuilder = new ResourceResponse.Builder(request.getUri())
						.setStatus(clientResponse.statusCode())
						.setData(clientResponse.body());

				for(final var headerEntry : clientResponse.headers().map().entrySet())
					for(final var headerValue : headerEntry.getValue())
						rrBuilder.add(headerEntry.getKey(), headerValue);

				return rrBuilder.build();
			}
			//handle exceptions raised during the request sending process
			catch(Exception e) {
				throw new RuntimeException("Failed to send HTTP request to " + request.getUri(), e);
			}
		});
	}
	// ==================================================
}
