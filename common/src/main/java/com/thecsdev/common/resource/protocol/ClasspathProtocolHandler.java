package com.thecsdev.common.resource.protocol;

import com.thecsdev.common.resource.ResourceRequest;
import com.thecsdev.common.resource.ResourceResponse;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static com.thecsdev.common.resource.protocol.FileProtocolHandler.stackTraceToBytes;
import static com.thecsdev.common.resource.protocol.HttpProtocolHandler.*;

public final class ClasspathProtocolHandler implements ProtocolHandler
{
	// ==================================================
	public static final ClasspathProtocolHandler INSTANCE = new ClasspathProtocolHandler();
	// ==================================================
	private ClasspathProtocolHandler() {}
	// ==================================================
	public final @Override boolean matches(@NotNull URI uri) {
		return "classpath".equalsIgnoreCase(uri.getScheme());
	}
	// --------------------------------------------------
	public final @Override @NotNull CompletableFuture<ResourceResponse> handle(@NotNull ResourceRequest request)
	{
		//require not null for the argument
		Objects.requireNonNull(request);
		//ensure this handler can process the request URI
		if(!matches(request.getUri()))
			throw new IllegalArgumentException("Cannot handle URI with scheme: " + request.getUri().getScheme());

		//handle the resource reading operation asynchronously
		return CompletableFuture.supplyAsync(() ->
		{
			//obtain uri and is path
			final var uri     = request.getUri();
			String    uriPath = uri.getPath();

			//remove leading slashes to make path valid resource path for ClassLoader
			while(uriPath.startsWith("/"))
				uriPath = uriPath.substring(1);

			//attempt to read resource from classpath
			try(var inputStream = getClass().getClassLoader().getResourceAsStream(uriPath))
			{
				//if resource not found, return appropriate response
				if(inputStream == null)
					return new ResourceResponse.Builder(uri).setStatus(STATUS_NOT_FOUND).build();

				//read bytes and return response
				byte[] data = inputStream.readAllBytes();
				return new ResourceResponse.Builder(uri).setStatus(STATUS_OK).setData(data).build();
			}
			catch(SecurityException se) {
				return new ResourceResponse.Builder(request.getUri(), STATUS_FORBIDDEN)
						.setData(stackTraceToBytes(se))
						.build();
			} catch(Exception e) {
				return new ResourceResponse.Builder(request.getUri(), STATUS_ERROR)
						.setData(stackTraceToBytes(e))
						.build();
			}
		});
	}
	// ==================================================
}
