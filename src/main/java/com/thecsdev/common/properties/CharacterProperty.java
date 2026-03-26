package com.thecsdev.common.properties;

/**
 * An {@link ObjectProperty} whose {@code T} type is {@link Character}.
 */
public final class CharacterProperty extends PrimitiveProperty<Character>
{
	// ==================================================
	public CharacterProperty() { super('\u0000', '\u0000'); }
	public CharacterProperty(Character value) { super((value != null) ? value : 0, '\u0000'); }
	// ==================================================
	/**
	 * Same as {@link #get()}, but returns a {@code char} instead of a {@link Character}.
	 */
	public final char getC() { return this.get(); }
	// ==================================================
}
