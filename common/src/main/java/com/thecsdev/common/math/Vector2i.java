package com.thecsdev.common.math;

import java.util.Objects;

/**
 * An immutable {@link Integer}-based Vector2 record.
 */
public final class Vector2i
{
	// ==================================================
	public static final Vector2i ZERO = new Vector2i(0, 0);
	// --------------------------------------------------
	public final int x, y;
	// ==================================================
	public Vector2i(int x, int y) { this.x = x; this.y = y; }
	// ==================================================
	public final @Override int hashCode() { return Objects.hash(this.x, this.y); }
	public final @Override boolean equals(Object obj)
	{
		if(obj == this) return true;
		else if(obj instanceof Vector2i v2)
			return (this.x == v2.x && this.y == v2.y);
		else return false;
	}
	// --------------------------------------------------
	public final @Override Vector2i clone() { return new Vector2i(this.x, this.y); }
	public final @Override String toString() { return getClass().getName() + "[x=" + this.x + ",y=" + this.y + "]"; }
	// ==================================================
	public final Vector2i add(int x, int y) { return new Vector2i(this.x + x, this.y + y); }
	public final Vector2i sub(int x, int y) { return new Vector2i(this.x - x, this.y - y); }
	public final Vector2i mul(int x, int y) { return new Vector2i(this.x * x, this.y * y); }
	public final Vector2i div(int x, int y) { return new Vector2i(this.x / x, this.y / y); }
	// ==================================================
}
