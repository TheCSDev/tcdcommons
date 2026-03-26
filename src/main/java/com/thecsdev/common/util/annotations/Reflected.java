package com.thecsdev.common.util.annotations;

import java.lang.annotation.*;

/**
 * Indicates that the annotated member is accessed via reflection or some other
 * mechanism that references the annotated member by name.
 * <p>
 * Care should be taken when refactoring or renaming such elements, as doing
 * so may break functionality.
 * <p>
 * Optionally, {@link #value()} may be used to document {@link Class}es where
 * such references are located. Although note that references are not strictly
 * present only in {@link Class}es and may be practically anywhere.
 *
 * @see java.lang.reflect
 */
@Documented
@Retention(RetentionPolicy.RUNTIME) //runtime is intentional
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface Reflected
{
	/**
	 * An optional array of {@link Class}es that reference the annotated member
	 * via reflection. This can be used for documentation purposes or to assist
	 * any automated tools that analyze code for reflection usage.
	 */
	Class<?>[] value() default {};
}
