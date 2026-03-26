package com.thecsdev.common.math;

import java.util.Objects;

/**
 * An immutable {@link Double}-based Vector2 record.
 */
public final class Vector2d
{
	// ==================================================
	public static final Vector2d ZERO = new Vector2d(0, 0);
	// --------------------------------------------------
	public final double x, y;
	// ==================================================
	public Vector2d(double x, double y) { this.x = x; this.y = y; }
	// ==================================================
	public final @Override int hashCode() { return Objects.hash(this.x, this.y); }
	public final @Override boolean equals(Object obj)
	{
		if(obj == this) return true;
		else if(obj instanceof Vector2d v2)
			return (this.x == v2.x && this.y == v2.y);
		else return false;
	}
	// --------------------------------------------------
	public final @Override Vector2d clone() { return new Vector2d(this.x, this.y); }
	public final @Override String toString() { return getClass().getName() + "[x=" + this.x + ",y=" + this.y + "]"; }
	// ==================================================
	public final Vector2d add(double x, double y) { return new Vector2d(this.x + x, this.y + y); }
	public final Vector2d sub(double x, double y) { return new Vector2d(this.x - x, this.y - y); }
	public final Vector2d mul(double x, double y) { return new Vector2d(this.x * x, this.y * y); }
	public final Vector2d div(double x, double y) { return new Vector2d(this.x / x, this.y / y); }
	// ==================================================
}
