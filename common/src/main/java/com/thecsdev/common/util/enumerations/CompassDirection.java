package com.thecsdev.common.util.enumerations;


/**
 * Represents one of the 9 compass-like positions in a 3x3 grid:
 * the eight cardinal/intercardinal directions plus the {@link #CENTER}.
 * <p>
 * Useful for things like UI grids and spatial layouts where both axis
 * and directional grouping queries are needed.
 */
public enum CompassDirection
{
	// ==================================================
	NORTH_WEST, NORTH, NORTH_EAST,
	WEST, CENTER, EAST,
	SOUTH_WEST, SOUTH, SOUTH_EAST;
	// ==================================================
	/**
	 * @return true if this direction is on the left column
	 */
	public boolean isLeft() {
		return this == NORTH_WEST || this == WEST || this == SOUTH_WEST;
	}

	/**
	 * @return true if this direction is on the middle column
	 */
	public boolean isCenterX() {
		return this == NORTH || this == CENTER || this == SOUTH;
	}

	/**
	 * @return true if this direction is on the right column
	 */
	public boolean isRight() {
		return this == NORTH_EAST || this == EAST || this == SOUTH_EAST;
	}

	/**
	 * @return true if this direction is on the top row
	 */
	public boolean isTop() {
		return this == NORTH_WEST || this == NORTH || this == NORTH_EAST;
	}

	/**
	 * @return true if this direction is on the middle row
	 */
	public boolean isCenterY() {
		return this == WEST || this == CENTER || this == EAST;
	}

	/**
	 * @return true if this direction is on the bottom row
	 */
	public boolean isBottom() {
		return this == SOUTH_WEST || this == SOUTH || this == SOUTH_EAST;
	}
	// ==================================================
	/**
	 * Returns the {@link CompassDirection} that corresponds to a given XY coordinate
	 * that ranges from [-1, -1] to [1, 1], where [0, 0] is the {@link CompassDirection#CENTER}.
	 * <p>
	 * Negative X corresponds to western directions, whereas negative Y corresponds to northern
	 * directions.
	 */
	public static final CompassDirection of01(int x, int y)
	{
		if(y < 0) {
			if(x < 0) return NORTH_WEST;
			else if(x > 0) return NORTH_EAST;
			else return NORTH;
		}
		else if(y > 0) {
			if(x < 0) return SOUTH_WEST;
			else if(x > 0) return SOUTH_EAST;
			else return SOUTH;
		}
		else {
			if(x < 0) return WEST;
			else if(x > 0) return EAST;
			else return CENTER;
		}
	}
	// ==================================================
}
