package com.thecsdev.common.math;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.thecsdev.common.math.TMath.clamp01d;

/**
 * Immutable two-dimensional integer-based point.
 */
public final class Point2d implements Cloneable
{
	// ==================================================
	/**
	 * The default "zero" value whose (x, y) values are all {@code 0}.
	 */
	public static final Point2d ZERO = new Point2d(0, 0);
	// --------------------------------------------------
	public final double x, y;
	// ==================================================
	public Point2d() { this.x = 0; this.y = 0; }
	public Point2d(double x, double y) { this.x = x; this.y = y; }
	// ==================================================
	public final @Override int hashCode() { return Objects.hash(this.x, this.y); }
	public final @Override boolean equals(Object obj) {
		if(obj == this) return true;
		else if(obj instanceof Point2d tbb)
			return (this.x == tbb.x) && (this.y == tbb.y);
		else return false;
	}
	// --------------------------------------------------
	public final @Override Point2d clone() {
		try { return (Point2d) super.clone(); }
		catch(CloneNotSupportedException e) { throw new RuntimeException(e); }
	}
	public final @Override String toString() {
		return super.toString() + "[x=" + this.x + ",y=" + this.y + "]";
	}

	// ==================================================
	/**
	 * Returns a new {@link Point2d} with the {@link #x} and {@link #y} values added.
	 */
	public final @NotNull Point2d add(double dX, double dY) { return new Point2d(this.x + dX, this.y + dY); }

	/**
	 * Returns a new {@link Point2d} with the {@link #x} and {@link #y} values subtracted.
	 */
	public final @NotNull Point2d sub(double dX, double dY) { return new Point2d(this.x - dX, this.y - dY); }
	// --------------------------------------------------
	/**
	 * Returns a new {@link Point2d} with the {@link #x} and {@link #y} values clamped
	 * to {@code 0 to 1} range.
	 */
	public final @NotNull Point2d clamp01() { return new Point2d(clamp01d(this.x), clamp01d(this.y)); }
	// ==================================================
}
