package com.thecsdev.common.util.annotations;

import java.lang.annotation.*;

/**
 * This annotation indicates that a member is intended to be overridable.
 * <p>
 * It is used to explicitly declare that a method or type is designed to
 * be overridden by subclasses.
 * <p>
 * This can be particularly useful in large
 * codebases or libraries where the intent of the designer needs to be
 * clearly communicated. It can also be useful in debugging scenarios where
 * it's important to understand the intended use of a method or type.
 *
 * @see Override
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface Virtual {}
