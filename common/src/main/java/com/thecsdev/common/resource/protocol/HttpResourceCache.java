package com.thecsdev.common.resource.protocol;

import com.thecsdev.common.resource.ResourceRequest;
import com.thecsdev.common.resource.ResourceResponse;
import com.thecsdev.common.util.TUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static java.time.ZonedDateTime.now;

//TODO - Saving/loading logic is necessary. Will implement later
//TODO - Also implement in-memory cache size limits
//TODO - Cached responses are meant to be independent of ResourceRequest objects
//TODO - Regular interval for clean-up may be done a better way? We'll see

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
	static {
		//schedule periodic memory cache cleanup task
		TUtils.getScheduledExecutor().scheduleAtFixedRate(INSTANCE::cleanUp, 3, 10, TimeUnit.MINUTES);
	}
	// ==================================================
	/**
	 * Asynchronously retrieves a cached {@link ResourceResponse} for the given
	 * {@link ResourceRequest}, or loads it from disk if not present in in-memory cache.
	 *
	 * @param request The {@link ResourceRequest} for which to retrieve or load the response.
	 * @return A {@link CompletableFuture} that will complete with the cached or loaded
	 *         {@link ResourceResponse}, or {@code null} if not found.
	 * @throws NullPointerException If the argument is {@code null}.
	 */
	public final CompletableFuture<@Nullable ResourceResponse> getAsync(
			@NotNull ResourceRequest request) throws NullPointerException
	{
		//argument requirements
		Objects.requireNonNull(request);

		//clean up expired entries
		cleanUp();

		//obtain cached value and return it if present
		final @Nullable var entry = this.loadedCache.get(request);
		return CompletableFuture.completedFuture(entry != null ? entry.response : null);
	}
	// --------------------------------------------------
	/**
	 * Puts the given {@link ResourceResponse} into the cache associated with the
	 * @param request The {@link ResourceRequest} key.
	 * @param response The {@link ResourceResponse} to cache.
	 * @throws NullPointerException If an argument is {@code null}.
	 */
	public final void put(@NotNull ResourceRequest request, @NotNull ResourceResponse response)
			throws NullPointerException
	{
		//not null argument requirements
		Objects.requireNonNull(request);
		Objects.requireNonNull(response);

		//create entry instance and store it if it can be cached
		final var entry = new Entry(response);
		if(entry.canBeCached())
			this.loadedCache.put(request, entry);
	}
	// ==================================================
	/**
	 * Cleans up the in-memory cache by removing entries that are no longer valid
	 * for caching based on their caching metadata.
	 */
	public final void cleanUp() {
		this.loadedCache.entrySet().removeIf(entry -> !entry.getValue().canBeCached());
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
		private static final ZoneId GMT = Objects.requireNonNull(ZoneId.of("GMT"), "Unknown ZoneId: GMT");
		// ==================================================
		private final ResourceResponse response;
		// --------------------------------------------------
		private final ZonedDateTime date;
		private final boolean       noCache, noStore, mustRevalidate, mustUnderstand;
		private final ZonedDateTime expires;
		// ==================================================
		private Entry(@NotNull ResourceResponse response) throws NullPointerException
		{
			//initialize the main field
			this.response = Objects.requireNonNull(response);

			//parse the response date and expiration time
			ZonedDateTime date, expires;
			try {
				date = ZonedDateTime.parse(
						Objects.requireNonNull(response.getFirst("date")),
						DateTimeFormatter.RFC_1123_DATE_TIME);
			} catch(Exception e) { date = Instant.EPOCH.atZone(GMT); }

			//parse cache-control header
			final var cacheControl = response.getFirst("cache-control", "").toLowerCase(Locale.ENGLISH);
			this.noCache           = cacheControl.contains("no-cache");
			this.noStore           = cacheControl.contains("no-store");
			this.mustRevalidate    = cacheControl.contains("must-revalidate");
			this.mustUnderstand    = cacheControl.contains("must-understand");

			//parse expiration date
			if(cacheControl.contains("max-age")) {
				final var maxAgeMatch = Pattern.compile("max-age=(\\d+)").matcher(cacheControl);
				if(maxAgeMatch.find()) {
					try { expires = date.plusSeconds(Long.parseLong(maxAgeMatch.group(1))); }
					catch(Exception e) { expires = date; }
				}
				else expires = date;
			}
			else if(response.has("expires")) {
				try {
					expires = ZonedDateTime.parse(
							Objects.requireNonNull(response.getFirst("expires")),
							DateTimeFormatter.RFC_1123_DATE_TIME);
				} catch(Exception e) { expires = date; }
			}
			else expires = date;

			//assign date and expires
			this.date    = date;
			this.expires = expires;
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
			return !(this.noCache || this.noStore || this.mustRevalidate
					|| this.mustUnderstand) && now(GMT).isBefore(this.expires);
		}
		// ==================================================
	}
	// ================================================== ==================================================
}
