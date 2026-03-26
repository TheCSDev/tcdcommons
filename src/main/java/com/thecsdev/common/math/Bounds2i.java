package com.thecsdev.common.math;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Immutable integer-based 2D rectangle representing a bounding-box.
 * A bounding box is what represents the spatial area a given thing occupies.
 * @apiNote Primarily for use in 2D graphics and GUI frameworks.
 */
public final class Bounds2i implements Cloneable
{
	// ==================================================
	/**
	 * The default "zero" value whose (x, y, width, height) values are all {@code 0}.
	 */
	public static final Bounds2i ZERO = new Bounds2i(0, 0, 0, 0);
	// --------------------------------------------------
	public final int x, y, width, height, endX, endY;

	/**
	 * {@code true} if {@link #width} or {@link #height} is {@code 0}.
	 */
	public final boolean isEmpty;
	// ==================================================
	public Bounds2i(int x, int y, int width, int height)
	{
		this.x       = x;
		this.y       = y;
		this.width   = width  = Math.max(width, 0);
		this.height  = height = Math.max(height, 0);
		this.endX    = x + width;
		this.endY    = y + height;
		this.isEmpty = (width == 0 || height == 0);
	}
	// ==================================================
	public final @Override int hashCode() { return Objects.hash(this.x, this.y, this.width, this.height); }
	public final @Override boolean equals(Object obj)
	{
		if(obj == this) return true;
		else if(obj instanceof Bounds2i tbb)
			return (this.x == tbb.x) && (this.y == tbb.y) &&
					(this.width == tbb.width) && (this.height == tbb.height);
		else return false;
	}
	// --------------------------------------------------
	public final @Override Bounds2i clone() {
		try { return (Bounds2i) super.clone(); }
		catch(CloneNotSupportedException e) { throw new RuntimeException(e); }
	}
	public final @Override String toString()
	{
		return super.toString() +
				"[x=" + this.x + ",y=" + this.y +
				",width=" + this.width + ",height=" + this.height + "]";
	}
	// ==================================================
	/**
	 * Returns {@code true} if this rectangle contains a 2D point.
	 * @param x The point's X position.
	 * @param y The point's Y position.
	 */
	public final boolean contains(int x, int y) { return !(x < this.x || y < this.y || x > this.endX || y > this.endY); }

	/**
	 * Returns {@code true} if this rectangle completely contains another
	 * {@link Bounds2i}.
	 * @param other The other {@link Bounds2i} to compare to.
	 * @throws NullPointerException If the argument is {@code null}.
	 * @apiNote This function is mathematically asymmetric.
	 */
	public final boolean contains(@NotNull Bounds2i other) throws NullPointerException {
		return !(other.x < this.x || other.y < this.y || other.endX > this.endX || other.endY > this.endY);
	}

	/**
	 * Returns {@code true} if this rectangle intersects with another
	 * {@link Bounds2i}.
	 * @param other The other {@link Bounds2i} to compare to.
	 * @throws NullPointerException If the argument is {@code null}.
	 * @apiNote This function is mathematically symmetric.
	 */
	public final boolean intersects(@NotNull Bounds2i other) throws NullPointerException {
		return !(other.x > this.endX || other.endX < this.x || other.y > this.endY || other.endY < this.y);
	}

	/**
	 * Returns {@code true} if only the {@link #x} and {@link #y} of this and the
	 * other {@link Bounds2i} match.
	 * @param other The other {@link Bounds2i} to compare to.
	 * @throws NullPointerException If the argument is {@code null}.
	 */
	public final boolean hasSamePosition(@NotNull Bounds2i other) {
		return this.x == other.x && this.y == other.y;
	}

	/**
	 * Returns {@code true} if only the {@link #width} and {@link #height} of this
	 * and the other {@link Bounds2i} match.
	 * @param other The other {@link Bounds2i} to compare to.
	 * @throws NullPointerException If the argument is {@code null}.
	 */
	public final boolean hasSameSize(@NotNull Bounds2i other) {
		return this.width == other.width && this.height == other.height;
	}
	// ==================================================
	/**
	 * Returns a new {@link Bounds2i} instance with the arguments
	 * added to it.
	 */
	public final Bounds2i add(int x, int y, int width, int height) {
		return new Bounds2i(this.x + x, this.y + y, this.width + width, this.height + height);
	}

	/**
	 * Returns a new {@link Bounds2i} instance with the arguments
	 * subtracted from it.
	 */
	public final Bounds2i sub(int x, int y, int width, int height) {
		return new Bounds2i(this.x - x, this.y - y, this.width - width, this.height + height);
	}
	// ==================================================
	/**
	 * Returns a new {@link Bounds2i} instance with the given X and Y
	 * position while retaining the current width and height.
	 */
	public final Bounds2i position(int x, int y) { return new Bounds2i(x, y, this.width, this.height); }

	/**
	 * Returns a new {@link Bounds2i} instance with the given X
	 * position while retaining the current Y, width and height.
	 */
	public final Bounds2i x(int x) { return new Bounds2i(x, this.y, this.width, this.height); }

	/**
	 * Returns a new {@link Bounds2i} instance with the given Y
	 * position while retaining the current X, width and height.
	 */
	public final Bounds2i y(int y) { return new Bounds2i(this.x, y, this.width, this.height); }
	// --------------------------------------------------
	/**
	 * Returns a new {@link Bounds2i} instance with the given width and height
	 * while retaining the current X and Y position.
	 */
	public final Bounds2i size(int width, int height) { return new Bounds2i(this.x, this.y, width, height); }

	/**
	 * Returns a new {@link Bounds2i} instance with the given width
	 * while retaining the current X, Y and height.
	 */
	public final Bounds2i width(int width) { return new Bounds2i(this.x, this.y, width, this.height); }

	/**
	 * Returns a new {@link Bounds2i} instance with the given height
	 * while retaining the current X, Y and width.
	 */
	public final Bounds2i height(int height) { return new Bounds2i(this.x, this.y, this.width, height); }
	// ==================================================
}
