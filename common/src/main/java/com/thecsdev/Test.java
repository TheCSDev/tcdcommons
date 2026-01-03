package com.thecsdev;

import com.thecsdev.common.resource.ResourceRequest;
import com.thecsdev.common.resource.ResourceResolver;
import com.thecsdev.common.resource.protocol.HttpProtocolHandler;
import io.netty.util.internal.UnstableApi;
import org.jetbrains.annotations.ApiStatus;

import java.net.URI;
import java.util.concurrent.ExecutionException;

@UnstableApi
@ApiStatus.Internal
@Deprecated(forRemoval = true)
public final class Test
{
	public static void main(String[] args) throws ExecutionException, InterruptedException
	{
		final var res = ResourceResolver
				.fetchAsync(new ResourceRequest.Builder(URI.create("https://example.com/"))
						.add(HttpProtocolHandler.HEADER_HTTP_METHOD, "GET")
						.add("Accept", "text/html")
						.add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:146.0) Gecko/20100101 Firefox/146.0")
						.add("Sec-Fetch-Dest", "document")
						.add("Sec-Fetch-Mode", "navigate")
						.add("Sec-Fetch-Site", "none")
						.build())
				.get();

		System.out.println("HTTP " + res.getStatus());
		for(final var header : res.getMetadata().entrySet())
			for(final var headerValue : header.getValue())
				System.out.println(header.getKey() + ": " + headerValue);
		System.out.println();
		System.out.println(new String(res.getData()));
	}
}
