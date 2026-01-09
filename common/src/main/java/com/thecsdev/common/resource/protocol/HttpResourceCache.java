package com.thecsdev.common.resource.protocol;

import com.thecsdev.common.resource.ResourceRequest;
import com.thecsdev.common.resource.ResourceResponse;
import com.thecsdev.common.util.TUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static com.thecsdev.common.resource.protocol.HttpProtocolHandler.HEADER_HTTP_METHOD;
import static java.time.ZonedDateTime.now;
import static java.util.Objects.requireNonNull;

//FIXME - Full implementation is necessary, including hashing algorithms on-disk storage.

/**
 * Manages caching of {@link ResourceResponse}s for HTTP {@link ResourceRequest}s.
*/
public final @ApiStatus.Internal class HttpResourceCache
{
	// ================================================== ==================================================
	//                                  HttpResourceCache IMPLEMENTATION
	// ================================================== ==================================================
	/**
	 * A set of all existing {@link HttpResourceCache} instances.<br>
	 * This is tracked for the purpose of periodic cleanup of in-memory caches.
	 */
	private static final Set<WeakReference<HttpResourceCache>> INSTANCES;

	/**
	 * The default {@link HttpResourceCache} that is used by {@link HttpProtocolHandler}s.
	 */
	public static final HttpResourceCache DEFAULT;

	/**
	 * The maximum size in bytes for data stored in the in-memory cache.
	 */
	private static final @ApiStatus.Internal long RAM_DATA_MAX_SIZE = 1024 * 1024 * 10;
	// ==================================================
	private final @NotNull Path                        cacheDir;
	private final @NotNull Map<ResourceRequest, Entry> ramCache;
	// ==================================================
	static {
		//initialize fields
		INSTANCES = ConcurrentHashMap.newKeySet();
		DEFAULT   = new HttpResourceCache(); //must be declared after the Set<>

		//schedule periodic memory cache cleanup task for all instances
		TUtils.getScheduledExecutor().scheduleAtFixedRate(() ->
		{
			//firstly, remove all weak references whose values are gone
			INSTANCES.removeIf(entry -> entry.refersTo(null));

			//next, clean up all existing instances
			for(final var weakRef : INSTANCES) {
				final @Nullable var httpCache = weakRef.get();
				if(httpCache != null) httpCache.cleanUp();
			}
		}, 5, 10, TimeUnit.MINUTES);
	}
	// --------------------------------------------------
	public HttpResourceCache() {
		this(Path.of(System.getProperty("user.home"), ".cache/thecsdev/common/http"));
	}
	public HttpResourceCache(@NotNull Path cacheDir) throws NullPointerException {
		//initialize face
		this.cacheDir  = requireNonNull(cacheDir);
		this.ramCache  = new ConcurrentHashMap<>();
		//keep track of instances
		INSTANCES.add(new WeakReference<>(this));
	}
	// ==================================================
	/**
	 * Returns the directory where cached HTTP resources are stored.
	 */
	public final Path getCacheDirectory() { return this.cacheDir; }
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
	public final CompletableFuture<@Nullable ResourceResponse> fetchAsync(
			@NotNull ResourceRequest request) throws NullPointerException
	{
		requireNonNull(request);
		final @Nullable var cValue = this.ramCache.get(request);
		if(cValue == null || !cValue.isCacheable())
			return CompletableFuture.completedFuture(null);
		return CompletableFuture.completedFuture(cValue.response);
	}
	// --------------------------------------------------
	/**
	 * Asynchronously stores the given {@link ResourceResponse} associated with the
	 * specified {@link ResourceRequest}, into the cache.
	 *
	 * @param request The {@link ResourceRequest} associated with the response to store.
	 * @param response The {@link ResourceResponse} to store in the cache.
	 * @return A {@link CompletableFuture} that will complete when the response has been stored.
	 * @throws NullPointerException If an argument is {@code null}.
	 */
	public final CompletableFuture<@NotNull Boolean> storeAsync(
			@NotNull ResourceRequest request, @NotNull ResourceResponse response)
			throws NullPointerException
	{
		//not null requirements
		requireNonNull(request);
		requireNonNull(response);

		//preemptive clean up - to save up on memory usage
		cleanUp();

		//create and cache the entry
		final var entry = new Entry(response);
		if(entry.isCacheable()) {
			this.ramCache.put(request, entry);
			return CompletableFuture.completedFuture(true);
		} else return CompletableFuture.completedFuture(false);
	}
	// ==================================================
	/**
	 * Returns the approximate size in bytes of all cached data currently stored
	 * in the in-memory cache.
	 */
	public final long getRamDataSize() {
		return this.ramCache.values().stream()
				.mapToLong(entry -> entry.getResponse().getData().length)
				.sum();
	}

	/**
	 * Cleans up the in-memory cache by removing entries that are no longer valid
	 * for caching based on their caching metadata.
	 */
	public final void cleanUp()
	{
		//clear expired entries
		this.ramCache.entrySet().removeIf(entry -> !entry.getValue().isCacheable());

		//if ram data size is above 20mb, clear largest entries until it's back below 20mb
		final long[] ramDataSize = { getRamDataSize() };
		if(ramDataSize[0] > RAM_DATA_MAX_SIZE)
		{
			this.ramCache.entrySet().stream()
					.sorted((e1, e2) -> Long.compare(
							e2.getValue().getResponse().getData().length,
							e1.getValue().getResponse().getData().length))
					.forEachOrdered(entry -> {
						if(ramDataSize[0] <= RAM_DATA_MAX_SIZE) return;
						ramDataSize[0] -= entry.getValue().getResponse().getData().length;
						this.ramCache.remove(entry.getKey());
					});
		}
	}
	// ================================================== ==================================================
	//                                              Value IMPLEMENTATION
	// ================================================== ==================================================
	/**
	 * Represents a single cache entry storing a {@link ResourceResponse} along with
	 * its caching metadata.
	 */
	private static final class Entry
	{
		// ==================================================
		private static final ZoneId GMT = requireNonNull(ZoneId.of("GMT"), "Unknown ZoneId: GMT");
		// ==================================================
		private final ResourceResponse response;
		// --------------------------------------------------
		private final ZonedDateTime    date;
		private final boolean          noCache;
		private final ZonedDateTime    expires;
		// ==================================================
		private Entry(@NotNull ResourceResponse response) throws NullPointerException
		{
			//initialize the main field
			this.response = requireNonNull(response);

			//parse the response date and expiration time
			ZonedDateTime date, expires;
			try {
				date = ZonedDateTime.parse(
						response.getFirstOrThrow("date"),
						DateTimeFormatter.RFC_1123_DATE_TIME);
			} catch(Exception e) { date = Instant.EPOCH.atZone(GMT); }

			//parse cache-control header
			final var cacheControl = response.getFirst("cache-control", "").toLowerCase(Locale.ENGLISH);
			this.noCache = //HTTP standards generally do not allow caching of other HTTP methods
					!"GET,HEAD".contains(response.getFirstOrThrow(HEADER_HTTP_METHOD).toUpperCase(Locale.ENGLISH)) ||
					(response.getData().length > RAM_DATA_MAX_SIZE / 5) || //do not cache too large data
					Objects.equals(response.getFirst("vary"), "*") ||      //"Vary: *" is uncacheable
					cacheControl.contains("no-cache") ||
					cacheControl.contains("no-store") ||
					cacheControl.contains("must-revalidate") ||
					cacheControl.contains("must-understand");

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
							response.getFirstOrThrow("expires"),
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
		// --------------------------------------------------
		/**
		 * Returns whether this cache entry has expired.
		 */
		public final boolean isExpired() { return now(GMT).isAfter(this.expires); }

		/**
		 * Whether this cache entry can be cached, based on its cache-control directives.
		 */
		public final boolean isCacheable() { return !this.noCache && !isExpired(); }
		// ==================================================
	}
	// ================================================== ==================================================
}
