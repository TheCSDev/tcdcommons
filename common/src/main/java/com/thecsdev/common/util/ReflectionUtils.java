package com.thecsdev.common.util;

import com.google.common.primitives.Primitives;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Utility methods for {@code java.lang.reflect}.
 */
public final class ReflectionUtils
{
	// ==================================================
	private ReflectionUtils() {}
	// ==================================================
	/**
	 * Retrieves all declared {@link Field}s from the specified {@link Class} and its superclasses,
	 * up to (but not including) {@link Object}. This method ensures that no duplicate
	 * fields are included in the result.
	 *
	 * @param clazz The {@link Class} from which to retrieve {@link Field}s.
	 * @return An array of {@link Field} objects representing all declared fields in the
	 * {@link Class} hierarchy, without duplicates. The {@link Field}s also include private,
	 * protected, and package-private members of the classes.
	 */
	public static final @NotNull Field[] getAllDeclaredFields(@Nullable Class<?> clazz)
	{
		//once recursion reaches Object.class, return an empty array
		if(clazz == null || clazz == Object.class)
			return new Field[0];

		//create a stream that concatenates declared fields of this class and its superclass
		return Stream.concat(
			Arrays.stream(clazz.getDeclaredFields()),
			Arrays.stream(getAllDeclaredFields(clazz.getSuperclass())) //recurse into superclass
		)
		.distinct() //ensure no duplicates somehow emerge
		.toArray(Field[]::new);
	}

	/**
	 * Compares a {@link Constructor}'s parameter types against an {@link Object}
	 * array, checking if the arguments match the constructor.
	 * @param constructor The {@link Constructor} to check.
	 * @param args The arguments for said {@link Constructor}.
	 * @return The result of the comparison. {@code true} if it's a match.
	 * @apiNote Depends on <b>Guava</b> - {@code com.google.*}.
	 */
	public static boolean constructorMatchesArgs(Constructor<?> constructor, Object... args)
	{
		//get constructor parameter types
		final var paramTypes = constructor.getParameterTypes();

		//check for argument length mismatch
		if(paramTypes.length != args.length) return false;

		//iterate and compare individual parameter types
		for(int i = 0; i < paramTypes.length; i++)
		{
			final var paramType = paramTypes[i];
			final var arg       = args[i];

			//if an argument is null, it may pass only if the parameter is not a primitive type
			if(arg == null) {
				if(paramType.isPrimitive()) return false;
				continue;
			}

			//get actual argument type
			final var argType = arg.getClass();

			//if parameter is primitive, wrap it for assignability check
			final var effectiveParamType = paramType.isPrimitive()
				? Primitives.wrap(paramType)
				: paramType;

			//ensure the parameter type is assignable from the argument type
			if(!effectiveParamType.isAssignableFrom(argType))
				return false;
		}

		//return true if all checks pass
		return true;
	}

	/**
	 * Creates an instance of a {@link Class} using the first constructor that
	 * matches the given parameters. Pass no parameters to use a constructor that does
	 * not take any arguments. Non-primitive argument types may be assigned {@code null}.<br>
	 * <br>
	 * The constructor must also be accessible. If a matching and accessible constructor
	 * cannot be found, {@code null} is returned.
	 * @param <T> The {@link Class} type.
	 * @param type The {@link Class} type.
	 * @param params The {@link Class} constructor parameters.
	 * @throws NullPointerException If an argument is {@code null}.
	 * @apiNote Depends on <b>Guava</b> - {@code com.google.*}.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T createClassInstance(Class<T> type, Object... params)
			throws NullPointerException
	{
		//require not null on arguments
		Objects.requireNonNull(type);
		Objects.requireNonNull(params);

		//iterate constructors to find the matching one
		for(final var constructor : type.getConstructors())
		{
			//skip non-matching constructors
			if(!constructorMatchesArgs(constructor, params))
				continue;

			//try to create and return new class instance
			try {
				return (T) constructor.newInstance(params);
			} catch(ReflectiveOperationException e) {
				throw new RuntimeException("Failed to create instance of " + constructor, e);
			}
		}
		return null;
	}

	/**
	 * Checks if the given {@link Class} overrides a method with the specified signature.
	 * @param topClass Class to start checking from.
	 * @param methodName Name of the method.
	 * @param returnType Expected return type.
	 * @param paramTypes Expected parameter types.
	 * @return {@code true} if the method is overridden in the {@link Class} hierarchy,
	 *         {@code false} otherwise.
	 * @throws NullPointerException If an argument is {@code null}.
	 * @apiNote <b>This does not account for {@code interface}s yet!</b>
	 */
	@ApiStatus.Experimental
	public static boolean isMethodOverridden(
			Class<?> topClass, String methodName, Class<?> returnType, Class<?>... paramTypes)
			throws NullPointerException
	{
		//not null assertions
		Objects.requireNonNull(topClass);
		Objects.requireNonNull(methodName);
		Objects.requireNonNull(returnType);
		Objects.requireNonNull(paramTypes);

		//iterate up the class hierarchy, looking for the same method
		//appearing at least twice
		int concreteCount = 0;
		for(Class<?> cls = topClass; cls != null; cls = cls.getSuperclass()) {
			try {
				final var method = cls.getDeclaredMethod(methodName, paramTypes);
				if(returnType.isAssignableFrom(method.getReturnType())) {
					if(Modifier.isAbstract(method.getModifiers())) return concreteCount > 0;
					else                                           concreteCount++;
				}
				if(concreteCount >= 2) return true;
			}
			catch(NoSuchMethodException e) { /*ignore and move up*/ }
		}

		//no results found? return false
		return false;
	}
	// ==================================================
}
