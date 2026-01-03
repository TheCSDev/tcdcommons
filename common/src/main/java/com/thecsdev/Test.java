package com.thecsdev;

import com.thecsdev.common.resource.ResourceResolver;
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
				.fetchAsync(URI.create("file:///C:/Users/User/Desktop/hello.txt"))
				.get();
		System.out.println("Status code: " + res.getStatus());
		System.out.println("Body:\n");
		System.out.println(new String(res.getData()));
	}
}
