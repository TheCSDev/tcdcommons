package com.thecsdev.common.resource.protocol;

import com.thecsdev.common.resource.ResourceRequest;
import com.thecsdev.common.resource.ResourceResponse;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages caching of {@link ResourceResponse}s for HTTP {@link ResourceRequest}s.
 */
final @ApiStatus.Internal class HttpResourceCache
{
	// ================================================== ==================================================
	//                                  HttpResourceCache IMPLEMENTATION
	// ================================================== ==================================================
	public static final HttpResourceCache INSTANCE = new HttpResourceCache();
	// ==================================================
	/**
	 * An in-memory RAM cache for storing previously fetched {@link ResourceResponse}
	 * objects associated with their corresponding {@link ResourceRequest} keys.
	 */
	private final Map<ResourceRequest, Entry> loadedCache = new ConcurrentHashMap<>();
	// ==================================================
	private HttpResourceCache() {}
	// ==================================================
	/**
	 * Asynchronously retrieves a cached {@link ResourceResponse} for the given
	 * {@link ResourceRequest}, or loads it from disk if not present in in-memory cache.
	 *
	 * @param request The {@link ResourceRequest} for which to retrieve or load the response.
	 * @return A {@link CompletableFuture} that will complete with the cached or loaded
	 *         {@link ResourceResponse}, or {@code null} if not found.
	 */
	public final CompletableFuture<@Nullable ResourceResponse> getAsync(
			@NotNull ResourceRequest request) throws NullPointerException
	{
		//argument requirements
		Objects.requireNonNull(request);

		//obtain cached value and return it if present
		final @Nullable var entry = this.loadedCache.get(request);
		return CompletableFuture.completedFuture(entry != null ? entry.response : null);

		//TODO - Saving/loading logic is necessary. Will implement later.
		//       Also need to implement in-ram management and cleanup.
	}
	// --------------------------------------------------
	/**
	 * Puts the given {@link ResourceResponse} into the cache associated with the
	 * @param request The {@link ResourceRequest} key.
	 * @param response The {@link ResourceResponse} to cache.
	 */
	public final void put(@NotNull ResourceRequest request, @NotNull ResourceResponse response)
			throws NullPointerException
	{
		//FIXME - INVALIDATION MECHANISM IS MISSING - IMPLEMENT
		/*Objects.requireNonNull(request);
		Objects.requireNonNull(response);

		final var entry = new Entry(response);
		if(entry.canBeCached())
			this.loadedCache.put(request, entry);*/
	}
	// ================================================== ==================================================
	//                                              Entry IMPLEMENTATION
	// ================================================== ==================================================
	/**
	 * Represents a single cache entry storing a {@link ResourceResponse} along with
	 * its caching metadata.
	 */
	private static final class Entry
	{
		// ==================================================
		private final ResourceResponse response;
		// --------------------------------------------------
		private final ZonedDateTime date;
		private final boolean noCache, noStore, mustRevalidate, mustUnderstand;
		// ==================================================
		private Entry(@NotNull ResourceResponse response) throws NullPointerException
		{
			//initialize the main field
			this.response = Objects.requireNonNull(response);

			//parse the response date and expiration time
			ZonedDateTime date;
			try { date = ZonedDateTime.parse(Objects.requireNonNull(response.getFirst("date"))); }
			catch(Exception e) { date = Instant.EPOCH.atZone(ZoneId.of("GMT")); }
			this.date = date;

			//parse cache-control header
			final var cacheControl = response.getFirst("cache-control", "").toLowerCase(Locale.ENGLISH);
			this.noCache           = cacheControl.contains("no-cache");
			this.noStore           = cacheControl.contains("no-store");
			this.mustRevalidate    = cacheControl.contains("must-revalidate");
			this.mustUnderstand    = cacheControl.contains("must-understand");
		}
		// ==================================================
		public final @Override int hashCode() { return this.response.hashCode(); }
		public final @Override boolean equals(@Nullable Object obj) {
			if(this == obj) return true;
			else if(obj == null || getClass() != obj.getClass()) return false;
			else return this.response.equals(((Entry) obj).response);
		}
		// ==================================================
		/**
		 * Returns the cached {@link ResourceResponse}.
		 */
		public final @NotNull ResourceResponse getResponse() { return this.response; }

		/**
		 * Returns the date for when the HTTP {@link ResourceResponse} took place.
		 */
		public final @NotNull ZonedDateTime getDate() { return this.date; }

		/**
		 * Whether this cache entry can be cached, based on its cache-control directives.
		 */
		public final boolean canBeCached() {
			return !(this.noCache || this.noStore || this.mustRevalidate || this.mustUnderstand);
		}
		// ==================================================
	}
	// ================================================== ==================================================
}
