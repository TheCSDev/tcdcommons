package com.thecsdev.common.resource.protocol;

import com.thecsdev.common.resource.ResourceRequest;
import com.thecsdev.common.resource.ResourceResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * A {@link ProtocolHandler} implementation for handling "file" protocol {@link URI}s.
 * This handler reads files from the local filesystem and returns their contents
 * as {@link ResourceResponse} objects.
 *
 * @see URI#getScheme()
 */
public final class FileProtocolHandler implements ProtocolHandler
{
	// ==================================================
	/**
	 * The main singleton instance of {@link FileProtocolHandler}.
	 */
	public static final FileProtocolHandler INSTANCE = new FileProtocolHandler();
	// --------------------------------------------------
	/**
	 * Resource found and read successfully from the filesystem.
	 */
	public static final int STATUS_OK = 200;

	/**
	 * Access denied; the system refused to open the file due to insufficient permissions.
	 */
	public static final int STATUS_FORBIDDEN = 403;

	/**
	 * The specified path does not exist or the file is missing.
	 */
	public static final int STATUS_NOT_FOUND = 404;

	/**
	 * A general I/O failure or error occurred while accessing the file.
	 */
	public static final int STATUS_ERROR = 500;
	// ==================================================
	private FileProtocolHandler() {}
	// ==================================================
	public final @Override boolean matches(@NotNull URI uri) {
		final @Nullable var scheme = uri.getScheme();
		return (scheme != null) && scheme.equalsIgnoreCase("file");
	}
	// --------------------------------------------------
	public final @Override @NotNull CompletableFuture<ResourceResponse> handle(@NotNull ResourceRequest request)
	{
		//require not null for the argument
		Objects.requireNonNull(request);
		//ensure this handler can process the request URI
		if(!matches(request.getUri()))
			throw new IllegalArgumentException("Cannot handle URI with scheme: " + request.getUri().getScheme());

		//handle the file reading operation asynchronously
		return CompletableFuture.supplyAsync(() ->
		{
			try {
				return fetchSync(request);
			}
			catch(FileNotFoundException fnf) {
				return new ResourceResponse.Builder(request.getUri(), STATUS_NOT_FOUND)
						.setData(getStackTrace(fnf))
						.build();
			} catch(SecurityException se) {
				return new ResourceResponse.Builder(request.getUri(), STATUS_FORBIDDEN)
						.setData(getStackTrace(se))
						.build();
			} catch(Exception e) {
				return new ResourceResponse.Builder(request.getUri(), STATUS_ERROR)
						.setData(getStackTrace(e))
						.build();
			}
		});
	}
	// ==================================================
	/**
	 * Synchronously fetches the resource specified in the {@link ResourceRequest}.
	 * @param request The {@link ResourceRequest} containing the URI of the resource to fetch.
	 * @return A {@link ResourceResponse} containing the resource data and status.
	 * @throws IOException If an I/O error occurs while reading the file.
	 */
	private static final ResourceResponse fetchSync(@NotNull ResourceRequest request) throws IOException
	{
		//convert request the URI to a Path
		final var uri  = request.getUri();
		final var path = Paths.get(uri);

		//return "not found" status in the event the file does not exist
		if(!Files.exists(path))
			throw new FileNotFoundException(path.toString());

		//read file bytes and respond
		final var bytes = Files.readAllBytes(path);
		return new ResourceResponse.Builder(uri, STATUS_OK).setData(bytes).build();
	}
	// --------------------------------------------------
	/**
	 * Utility method to convert a {@link Throwable}'s stack trace to a byte array.
	 * @param throwable The {@link Throwable} whose stack trace is to be converted.
	 * @return A byte array representing the stack trace of the throwable.
	 */
	private static final byte[] getStackTrace(@NotNull Throwable throwable) {
		final var sw = new StringWriter();
		throwable.printStackTrace(new PrintWriter(sw, true));
		return sw.toString().getBytes();
	}
	// ==================================================
}
