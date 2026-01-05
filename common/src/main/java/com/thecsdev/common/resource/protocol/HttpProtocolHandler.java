package com.thecsdev.common.resource.protocol;

import com.thecsdev.common.resource.ResourceRequest;
import com.thecsdev.common.resource.ResourceResponse;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static java.time.ZonedDateTime.now;
import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;

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
	 * The main shared {@link HttpClient} instance used for sending HTTP requests.
	 */
	@ApiStatus.Internal
	private static final HttpClient CLIENT = HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(10))
			.followRedirects(HttpClient.Redirect.NORMAL)
			.build();

	private static final Map<ResourceRequest, ResourceResponse> RAM_CACHE = new HashMap<>();
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
	public final @Override @NotNull CompletableFuture<ResourceResponse> handle(@NotNull ResourceRequest request)
			throws NullPointerException, IllegalArgumentException
	{
		//require not null for the argument
		Objects.requireNonNull(request);
		//ensure this handler can process the request URI
		if(!matches(request.getUri()))
			throw new IllegalArgumentException("Cannot handle URI with scheme: " + request.getUri().getScheme());

		//---------- build the client request
		//obtain the http method to use
		final var httpMet = request.getFirst(HEADER_HTTP_METHOD, "GET").toUpperCase(Locale.ENGLISH);
		if((httpMet.equals("GET") || httpMet.equals("HEAD")) && request.getData().length > 0)
			throw new IllegalArgumentException("'HTTP " + httpMet + "' requests cannot have a body/data.");

		//start building the http request
		final var httpReq = HttpRequest.newBuilder(request.getUri())
			.method(httpMet, httpMet.equals("GET") ?
					HttpRequest.BodyPublishers.noBody() :
					HttpRequest.BodyPublishers.ofByteArray(request.getData()));

		//add header values to the http request
		for(final var headerEntry : request.getMetadata().entrySet())
			for(final var headerValue : headerEntry.getValue())
				httpReq.header(headerEntry.getKey(), headerValue);
		//      ^ intentionally allow illegal argument exception. end user needs to know when they mess up

		//---------- send the http request and return
		return CLIENT.sendAsync(httpReq.build(), HttpResponse.BodyHandlers.ofByteArray()).thenApply(httpRes ->
		{
			//---------- construct resource response
			//start building the resource response
			final var rssRes = new ResourceResponse.Builder(request.getUri())
					.setStatus(httpRes.statusCode())
					.setData(httpRes.body());

			//add header values to the resource respone
			for(final var headerEntry : httpRes.headers().map().entrySet())
				for(final var headerValue : headerEntry.getValue())
					rssRes.add(headerEntry.getKey(), headerValue);

			//set the "Date" header in value the resource response
			//(HTTP spec. always requires it, and it must be GMT - written in 'RFC 1123')
			if(httpRes.headers().firstValue("date").isEmpty())
				rssRes.set("date", now(ZoneId.of("GMT")).format(RFC_1123_DATE_TIME));

			//---------- build and return once done
			return rssRes.build();
		});
	}
	// ==================================================
}
