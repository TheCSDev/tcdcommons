package com.thecsdev.common.resource.http;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Represents a "profile" for storing HTTP resources such as cache and cookies.
 */
public final @ApiStatus.Experimental class HttpProfile
{
	// ==================================================
	/***
	 * Default {@link HttpProfile} instance that stores 
	 */
	public static final HttpProfile DEFAULT = new HttpProfile(Path.of(
			System.getProperty("user.home"), ".cache", "thecsdev", "20260202"));
	// ==================================================
	private final @NotNull Path             dirname;
	private final @NotNull HttpProfileCache cache;
	// --------------------------------------------------
	private final int _hashCode;
	// ==================================================
	public HttpProfile(@NotNull Path dirname) throws NullPointerException {
		this.dirname   = Objects.requireNonNull(dirname);
		this.cache     = new HttpProfileCache(dirname.resolve("cache"));
		this._hashCode = Objects.hash(this.dirname, this.cache);
	}
	// ==================================================
	public final @Override int hashCode() { return this._hashCode; }
	public final @Override boolean equals(@Nullable Object obj) {
		if(this == obj) return true;
		else if(obj == null || obj.getClass() != this.getClass()) return false;
		final var other = (HttpProfile) obj;
		return Objects.equals(this.dirname, other.dirname) && Objects.equals(this.cache, other.cache);
	}
	// ==================================================
	/**
	 * Returns the {@link Path} to the directory where the {@link HttpProfile}
	 * data is stored.
	 */
	public final @NotNull Path getDirname() { return this.dirname; }

	/**
	 * Returns the {@link HttpProfileCache} associated with this {@link HttpProfile}.
	 */
	public final @NotNull HttpProfileCache getCache() { return this.cache; }
	// ==================================================
}
