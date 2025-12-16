package com.thecsdev.common.util.annotations;

import java.lang.annotation.*;

/**
 * Indicates that a method is "caller-sensitive".<br/>
 * A caller-sensitive method varies its behavior according to the {@link Class} of its immediate caller.
 * @apiNote The internal "CallerSensitive" {@link Annotation} is not accessible in the public JVM API,
 * and as such, this {@link Annotation} serves as a replacement.
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface CallerSensitive {}
