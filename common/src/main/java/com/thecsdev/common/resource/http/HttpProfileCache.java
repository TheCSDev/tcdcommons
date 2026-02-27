package com.thecsdev.common.resource.http;

import com.google.gson.*;
import com.thecsdev.common.config.JsonConfig;
import com.thecsdev.common.resource.ResourceRequest;
import com.thecsdev.common.resource.ResourceResponse;
import com.thecsdev.common.util.TUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

import static com.thecsdev.common.resource.protocol.HttpProtocolHandler.HEADER_HTTP_METHOD;

/**
 * Represents a "cache" for storing cached HTTP resources.
 */
public final @ApiStatus.Experimental class HttpProfileCache
{
	// ================================================== ==================================================
	//                                   HttpProfileCache IMPLEMENTATION
	// ================================================== ==================================================
	@ApiStatus.Internal
	public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
	// ==================================================
	private final @NotNull Path dirname;
	// --------------------------------------------------
	private final int _hashCode;
	// ==================================================
	public HttpProfileCache(@NotNull Path dirname) throws NullPointerException {
		this.dirname   = Objects.requireNonNull(dirname);
		this._hashCode = Objects.hash(this.dirname);
	}
	// ==================================================
	public final @Override int hashCode() { return this._hashCode; }
	public final @Override boolean equals(@Nullable Object obj) {
		if(this == obj) return true;
		else if(obj == null || obj.getClass() != this.getClass()) return false;
		final var other = (HttpProfileCache) obj;
		return Objects.equals(this.dirname, other.dirname);
	}
	// ==================================================
	/**
	 * Returns the {@link Path} to the directory where the {@link HttpProfileCache}
	 * data and cached HTTP resources are stored.
	 */
	public final @NotNull Path getDirname() { return this.dirname; }
	// ==================================================
	/**
	 * Fetches the cached {@link ResourceResponse} for the given {@link ResourceRequest},
	 * if it exists and is valid. Returns a {@link CompletableFuture} that completes
	 * with the cached response, or {@code null} the cache is a "miss".
	 * @param request The {@link ResourceRequest} for which to fetch the cached response.
	 * @throws NullPointerException If the argument is {@code null}.
	 */
	public final CompletableFuture<@Nullable ResourceResponse> fetchAsync(
			@NotNull ResourceRequest request) throws NullPointerException {
		return CacheBucket.of(this, request).fetchAsync(request);
	}

	/**
	 * Stores the given {@link ResourceResponse} in the cache for the given
	 * {@link ResourceRequest}. Returns a {@link CompletableFuture} that completes
	 * when the response has been stored.
	 * @param request The {@link ResourceRequest} associated with the response to store.
	 * @param response The {@link ResourceResponse} to store in the cache.
	 * @throws NullPointerException If an argument is {@code null}.
	 */
	public CompletableFuture<@Nullable Void> storeAsync(
			@NotNull ResourceRequest request,
			@NotNull ResourceResponse response) throws NullPointerException {
		return CacheBucket.of(this, request).storeAsync(request, response);
	}
	// ==================================================
	/**
	 * Generates <b>relative</b> a cache path for the given {@link ResourceRequest},
	 * based on the given hash.
	 * The path is structured to provide directory nesting based on the hash.
	 */
	public static final @NotNull Path hash2path(@NotNull String hash)
			throws NullPointerException
	{
		//generate the hash
		final int len  = Objects.requireNonNull(hash).length();

		//ensure we have enough characters for nesting
		final var dir1 = hash.substring(0, Math.min(len, 2));
		final var dir2 = (len > 2) ? hash.substring(2, Math.min(len, 4)) : "00";

		//keep a reasonable filename length (e.g., 16-32 chars), then return
		final var filename = hash.substring(0, Math.min(len, 32));
		return Path.of(dir1, dir2, filename);
	}
	// ================================================== ==================================================
	//                                         CacheEntry IMPLEMENTATION
	// ================================================== ==================================================
	/**
	 * Represents a directory where cache data for a given {@link URI} is stored.
	 */
	public static final @ApiStatus.Experimental class CacheBucket
	{
		// ==================================================
		/**
		 * GMT {@link ZoneId} used for cache date formatting.
		 */
		@ApiStatus.Internal
		private static final ZoneId GMT = Objects.requireNonNull(ZoneId.of("GMT"), "Unknown ZoneId: GMT");
		// ==================================================
		private final @NotNull Path dirname;
		// ==================================================
		private CacheBucket(@NotNull Path dirname) throws NullPointerException {
			this.dirname = Objects.requireNonNull(dirname);
		}
		// ==================================================
		public final @Override int hashCode() { return this.dirname.hashCode(); }
		public final @Override boolean equals(Object obj) {
			if(this == obj) return true;
			else if(obj == null || obj.getClass() != this.getClass()) return false;
			final var other = (CacheBucket) obj;
			return Objects.equals(this.dirname, other.dirname);
		}
		// ==================================================
		/**
		 * Returns the {@link Path} to the directory where cache data for this
		 * {@link CacheBucket} is stored.
		 */
		public final @NotNull Path getDirname() { return this.dirname; }
		// ==================================================
		/**
		 * Fetches the cached {@link ResourceResponse} for the given {@link ResourceRequest},
		 * if it exists and is valid. Returns a {@link CompletableFuture} that completes
		 * with the cached response, or {@code null} the cache is a "miss".
		 * @param request The {@link ResourceRequest} for which to fetch the cached response.
		 * @throws NullPointerException If the argument is {@code null}.
		 */
		public final CompletableFuture<@Nullable ResourceResponse> fetchAsync(
				@NotNull ResourceRequest request) throws NullPointerException
		{
			//not null requirement
			Objects.requireNonNull(request);

			//the first future reads and parses the metadata file
			return CompletableFuture.supplyAsync(() -> {
				final var meta = new CacheBucketMeta(this);
				try { meta.loadFromFile(); } catch (Exception ignored) {}
				return meta;
			}, TUtils.getVirtualThreadPerTaskExecutor())
			//then obtain the cache file path
			.thenApply((@NotNull CacheBucketMeta meta) -> TUtils.uncheckedSupply(() ->
			{
				try {
					//obtain cache file attributes, and if cache file too old, return null
					final var cacheFile     = meta.resolveCacheFile(request);
					final var cacheFileAttr = Files.readAttributes(cacheFile, BasicFileAttributes.class);
					final var ageThreshold  = Instant.now().minusSeconds(meta.getMaxAge());
					if(cacheFileAttr.lastModifiedTime().toInstant().isBefore(ageThreshold))
						return null;
					//else return the path to the cache file
					return cacheFile;
				} catch (NoSuchFileException ignored) {
					//we try-catch instead of "exists"-checks, to avoid race conditions
					return null;
				}
			}))
			//read cached file data and construct resource response
			.thenApply((@Nullable Path cacheFile) -> TUtils.uncheckedSupply(() ->
			{
				try {
					//if the cache file does not exist, return null
					if(cacheFile == null) return null;
					//read and parse the cache file
					final var cacheJson = GSON.fromJson(Files.readString(cacheFile), JsonObject.class);
					final var response  = new ResourceResponse.Builder(request.getUri())
							.setStatus(cacheJson.get("status").getAsInt())
							.setData(Base64.getDecoder().decode(cacheJson.get("data").getAsString()))
							.set(HEADER_HTTP_METHOD, request.getFirst(HEADER_HTTP_METHOD, "GET"));
					cacheJson.get("metadata").getAsJsonObject().asMap()
							.forEach((key, value) -> response.addAll(key, value.getAsJsonArray()));
					return response.build();
				} catch (NoSuchFileException ignored) {
					return null;
				}
			}));
		}

		/**
		 * Stores the given {@link ResourceResponse} in the cache for this {@link CacheBucket}.
		 * Returns a {@link CompletableFuture} that completes when the response has been stored.
		 * @param response The {@link ResourceResponse} to store in the cache.
		 * @throws NullPointerException If the argument is {@code null}.
		 */
		public final CompletableFuture<@Nullable Void> storeAsync(
				@NotNull ResourceRequest request,
				@NotNull ResourceResponse response) throws NullPointerException
		{
			//not null requirements
			Objects.requireNonNull(request);
			Objects.requireNonNull(response);

			//storing operation
			return CompletableFuture.supplyAsync(() -> TUtils.uncheckedSupply(() ->
			{
				// ----- calculate whether caching can be done and max-age
				//parse cache-control header
				final var cacheControl = response.getFirst("cache-control", "").toLowerCase(Locale.ROOT);
				final var noCache = //HTTP standards generally do not allow caching of other HTTP methods
						!"GET,HEAD".contains(response.getFirstOrThrow(HEADER_HTTP_METHOD).toUpperCase(Locale.ROOT)) ||
								Objects.equals(response.getFirst("vary"), "*") || //"Vary: *" is uncacheable
								cacheControl.contains("no-cache") ||
								cacheControl.contains("no-store") ||
								cacheControl.contains("must-revalidate") ||
								cacheControl.contains("must-understand");
				if(noCache) return null;

				//parse the response date and expiration time
				long maxAge;
				ZonedDateTime date, expires;
				try {
					date = ZonedDateTime.parse(
							response.getFirstOrThrow("date"),
							DateTimeFormatter.RFC_1123_DATE_TIME);
				} catch(Exception e) { date = Instant.EPOCH.atZone(GMT); }

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

				//calculate and return the "max-age" in seconds
				maxAge = Math.max(ChronoUnit.SECONDS.between(date, expires), 0);
				if(maxAge < 1) return null;

				// ----- update bucket metadata, and obtain cache file
				//create meta instance
				final var meta = new CacheBucketMeta(this);
				//update metadata properties
				meta.setMaxAge(maxAge);
				if(response.has("vary")) {
					final var vary = response.getFirstOrThrow("vary").toLowerCase(Locale.ROOT);
					meta.setVary(Arrays.asList(vary.split(",")));
				}
				//save updated metadata to disk
				meta.saveToFile();
				//resolve and return cache file path
				final var cacheFile = meta.resolveCacheFile(request);

				// ----- store cache file data
				//serialize to json
				final var cacheJson = new JsonObject();
				cacheJson.addProperty("status", response.getStatus());
				cacheJson.addProperty("data", Base64.getEncoder().encodeToString(response.getData()));
				final var metadataJson = new JsonObject();
				response.getMetadata().forEach((key, value) -> metadataJson.add(key, GSON.toJsonTree(value).getAsJsonArray()));
				cacheJson.add("metadata", metadataJson);
				//save json to file - and finally, return
				Files.writeString(cacheFile, GSON.toJson(cacheJson));
				return null;
			}),
			TUtils.getVirtualThreadPerTaskExecutor());
		}
		// ==================================================
		/**
		 * Creates a new {@link CacheBucket} instance for the given {@link ResourceRequest},
		 * using the given {@link HttpProfileCache} to determine the base directory.
		 * @param cache The {@link HttpProfileCache} to use as the base directory for this cache entry.
		 * @param request The {@link ResourceRequest} for which to create the cache entry.
		 * @throws NullPointerException If an argument is {@code null}.
		 */
		public static final @NotNull CacheBucket of(
				@NotNull HttpProfileCache cache, @NotNull ResourceRequest request)
				throws NullPointerException
		{
			Objects.requireNonNull(cache);
			Objects.requireNonNull(request);
			return of(cache, request.getFirst(HEADER_HTTP_METHOD, "GET"), request.getUri());
		}

		/**
		 * Creates a new {@link CacheBucket} instance for the given {@link URI} and HTTP method,
		 * using the given {@link HttpProfileCache} to determine the base directory.
		 * @param cache The {@link HttpProfileCache} to use as the base directory for this cache entry.
		 * @param httpMethod The HTTP method (e.g., "GET", "HEAD") associated with this cache entry.
		 * @param uri The {@link URI} associated with this cache entry.
		 * @throws NullPointerException If an argument is {@code null}.
		 */
		public static final @NotNull CacheBucket of(
				@NotNull HttpProfileCache cache,
				@NotNull String httpMethod,
				@NotNull URI uri) throws NullPointerException
		{
			//not null requirements
			Objects.requireNonNull(cache);
			Objects.requireNonNull(httpMethod);
			Objects.requireNonNull(uri);

			//http method must be uppercase
			httpMethod = httpMethod.toUpperCase(Locale.ROOT);

			//remove URI fragments
			if(uri.getFragment() == null || !uri.getFragment().isEmpty()) try {
				uri = new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), uri.getQuery(), null);
			} catch(URISyntaxException e) {
				throw new IllegalArgumentException("Failed to remove URI fragment", e);
			}

			//hashing and bucket dir path resolving
			final var uriHash = TUtils.str2sha256base36("HTTP " + httpMethod + " " + uri);
			final var relDir  = hash2path(uriHash);
			return new CacheBucket(cache.getDirname().resolve(relDir));
		}
		// ==================================================
	}
	// ================================================== ==================================================
	//                                     CacheEntryMeta IMPLEMENTATION
	// ================================================== ==================================================
	/**
	 * Represents metadata for a {@link CacheBucket}, such as expiration time.
	 * This metadata is stored in a "meta.json" file within the {@link CacheBucket}
	 * directory.
	 */
	public static final @ApiStatus.Experimental class CacheBucketMeta
	{
		// ==================================================
		private final @NotNull Path   filename;
		// --------------------------------------------------
		private          long         maxAge;
		private @NotNull List<String> vary; //immutable, sorted
		// ==================================================
		public CacheBucketMeta(@NotNull CacheBucket cacheBucket) throws NullPointerException {
			this.filename = cacheBucket.getDirname().resolve("meta.json");
			this.vary     = List.of();
		}
		// ==================================================
		/**
		 * The {@link Path} to the metadata file of this {@link CacheBucketMeta}.
		 */
		public final @NotNull Path getFilename() { return this.filename; }

		/**
		 * The maximum age (in seconds) a cached resource may exist before being
		 * considered as "expired".
		 */
		public final long getMaxAge() { return this.maxAge; }

		/**
		 * The "Vary" header values associated with a {@link CacheBucket}.
		 * The returned {@link Collection} is sorted and immutable.
		 */
		public final @NonNull List<String> getVary() { return this.vary; }
		// --------------------------------------------------
		/**
		 * Sets the maximum age (in seconds) for this {@link CacheBucketMeta}. The value
		 * is clamped to a minimum of 0.
		 * @param maxAge The maximum age in seconds.
		 */
		public final void setMaxAge(long maxAge) { this.maxAge = Math.max(maxAge, 0); }

		/**
		 * Sets the "Vary" header values for this {@link CacheBucketMeta}. The provided
		 * collection is sorted and stored as an immutable list.
		 * @param varyHeaders The collection of "Vary" header values to set.
		 * @throws NullPointerException If the argument is {@code null}.
		 */
		public final void setVary(@NotNull Collection<String> varyHeaders) throws NullPointerException {
			Objects.requireNonNull(varyHeaders);
			this.vary = varyHeaders.stream()
					.filter(Objects::nonNull)
					.map(el -> el.toLowerCase(Locale.ROOT).trim())
					.sorted()
					.toList();
		}
		// ==================================================
		/**
		 * Saves this {@link CacheBucketMeta} instance to a new {@link JsonObject}
		 * instance and returns the {@link JsonObject}.
		 */
		public final @NotNull JsonObject saveToJson() {
			final var json = new JsonObject();
			saveToJson(json);
			return json;
		}

		/**
		 * Saves this {@link CacheBucketMeta} to an existing {@link JsonObject} instance.
		 * @param to The {@link JsonObject} to save to.
		 * @throws NullPointerException If the argument is {@code null}.
		 */
		public final void saveToJson(@NotNull JsonObject to) throws NullPointerException
		{
			//not null requirement
			Objects.requireNonNull(to);

			//save stuff to the json object
			to.addProperty("max-age", this.maxAge);
			to.add("vary", GSON.toJsonTree(this.vary));
		}
		// --------------------------------------------------
		/**
		 * Loads this {@link CacheBucketMeta} from a {@link JsonObject}, overriding
		 * any properties of this {@link JsonConfig} that were stored in the
		 * {@link JsonObject}.
		 * @param from The {@link JsonObject} to load from.
		 * @throws NullPointerException If the argument is {@code null}.
		 */
		public final void loadFromJson(@NotNull JsonObject from) throws NullPointerException
		{
			//not null requirement
			Objects.requireNonNull(from);

			//attempt to parse expiration date
			try { this.maxAge = from.get("max-age").getAsLong(); }
			catch(RuntimeException ignored) { this.maxAge = 0; }

			//attempt to parse vary headers
			try {
				setVary(from.getAsJsonArray("vary").asList().stream()
						.map(el -> (el == null || el.isJsonNull()) ? null : el.getAsString())
						.toList());
			} catch(RuntimeException ignored) { this.vary = List.of(); }
		}
		// ==================================================
		/**
		 * Saves this {@link CacheBucketMeta} to the file specified by {@link #filename}.
		 * @throws IOException If an I/O error occurs while writing to the file.
		 */
		public final void saveToFile() throws IOException {
			Files.createDirectories(this.filename.getParent());
			Files.writeString(this.filename, GSON.toJson(saveToJson()));
		}

		/**
		 * Loads this {@link CacheBucketMeta} from the file specified by {@link #filename}.
		 * If the file does not exist, this method does nothing.
		 * @throws IOException If an I/O error occurs while reading from the file or parsing its contents.
		 */
		public final void loadFromFile() throws IOException
		{
			try {
				loadFromJson(GSON.fromJson(Files.readString(this.filename), JsonObject.class));
			} catch (NoSuchFileException | JsonParseException ignored) {
				//we intentionally ignore file not existing.
				//we also ignore json syntax, as anything from the outside could affect it
			}
		}
		// ==================================================
		/**
		 * Resolves the cache file path for the given {@link ResourceRequest}, based on the
		 * "Vary" header values stored in this {@link CacheBucketMeta}. The returned path is
		 * relative to the {@link CacheBucket} directory.
		 * @param request The {@link ResourceRequest} for which to resolve the cache file path.
		 * @throws NullPointerException If the argument is {@code null}.
		 */
		public final @NotNull Path resolveCacheFile(@NotNull ResourceRequest request)
				throws NullPointerException
		{
			//not null requirements
			Objects.requireNonNull(request);

			//hash input construction - consisting of 'Vary' header values
			final var input = new StringBuilder();
			for(final var headerName : getVary())
				input.append(headerName).append(":")
						.append(request.getFirst(headerName, "null"))
						.append("\n");

			//hashing and file-name resolving
			final var hash = TUtils.str2sha256base36(input.toString());
			final var name = hash + ".json";
			return this.filename.getParent().resolve(name);
		}
		// ==================================================
	}
	// ================================================== ==================================================
}
