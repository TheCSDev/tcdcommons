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
import java.time.ZoneId;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import static java.time.ZonedDateTime.now;
import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;
import static java.util.Objects.requireNonNull;

/**
 * A {@link ProtocolHandler} implementation for handling "http" and "https" protocol
 * {@link URI}s. This handler processes HTTP and HTTPS requests and returns their
 * responses as {@link ResourceResponse} objects.
 *
 * @see URI#getScheme()
 */
public class HttpProtocolHandler implements ProtocolHandler
{
	// ====================================================================================================
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
	private final HttpResourceCache cache      = HttpResourceCache.DEFAULT;
	private final HttpClient        httpClient = HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(10))
			.followRedirects(HttpClient.Redirect.NORMAL)
			.build();
	// ==================================================
	private HttpProtocolHandler() {}
	// ==================================================
	public final @Override boolean matches(@NotNull URI uri) {
		final @Nullable var scheme = uri.getScheme();
		return (scheme != null && (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https")));
	}
	// --------------------------------------------------
	public final @Override @NotNull CompletableFuture<ResourceResponse> handle(@NotNull ResourceRequest rssReq)
			throws NullPointerException, IllegalArgumentException
	{
		//---------- argument validity checks
		//require not null for the argument
		requireNonNull(rssReq);
		//ensure this handler can process the request URI
		if(!matches(rssReq.getUri()))
			throw new IllegalArgumentException("Cannot handle URI with scheme: " + rssReq.getUri().getScheme());

		//---------- build the client request (this also validates argument)
		//obtain the http method to use
		final var httpMethod = rssReq.getFirstOrThrow(HEADER_HTTP_METHOD).toUpperCase(Locale.ENGLISH);
		if((httpMethod.equals("GET") || httpMethod.equals("HEAD")) && rssReq.getData().length > 0)
			throw new IllegalArgumentException("'HTTP " + httpMethod + "' requests cannot have a body/data.");

		//start building the http request
		final var httpReq = HttpRequest.newBuilder(rssReq.getUri())
			.method(httpMethod, httpMethod.equals("GET") ?
					HttpRequest.BodyPublishers.noBody() :
					HttpRequest.BodyPublishers.ofByteArray(rssReq.getData()));

		//add header values to the http request
		for(final var headerEntry : rssReq.getMetadata().entrySet())
			for(final var headerValue : headerEntry.getValue())
				httpReq.header(headerEntry.getKey(), headerValue);
		//      ^ intentionally allow illegal argument exception. end user needs to know when they mess up

		//---------- consult the cache
		return this.cache
				.fetchAsync(rssReq)
				.thenCompose((@Nullable ResourceResponse cachedRssRes) ->
		{
			//cache hit - return the cached value
			if(cachedRssRes != null)
				return CompletableFuture.completedFuture(cachedRssRes);

			//cache miss - send the http request and return its response
			return this.httpClient
					.sendAsync(httpReq.build(), HttpResponse.BodyHandlers.ofByteArray())
					.thenApply(httpRes ->
			{
				//---------- construct resource response
				//start building the resource response
				final var rssRes = new ResourceResponse.Builder(rssReq.getUri())
						.setStatus(httpRes.statusCode())
						.setData(httpRes.body());

				//add header values to the resource respone
				for(final var headerEntry : httpRes.headers().map().entrySet())
					for(final var headerValue : headerEntry.getValue())
						rssRes.add(headerEntry.getKey(), headerValue);

				//set the "Date" header value in the resource response
				//(HTTP spec. always requires it, and it must be GMT - written in 'RFC 1123')
				if(httpRes.headers().firstValue("date").isEmpty())
					rssRes.set("date", now(ZoneId.of("GMT")).format(RFC_1123_DATE_TIME));

				//set the HTTP request method header value
				rssRes.set(HEADER_HTTP_METHOD, httpMethod);

				//---------- build and return once done
				final var result = rssRes.build();
				this.cache.storeAsync(rssReq, result);
				return result;
			});
		});
	}
	// ==================================================
}
