package com.dynxsty.dih4jda.util;

import com.dynxsty.dih4jda.DIH4JDALogger;

import java.lang.reflect.Constructor;

/**
 * Utility class for checking certain conditions.
 *
 * @since v1.4
 */
public class Checks {

	private Checks() {
	}

	/**
	 * Checks if the given base class implements a certain class.
	 *
	 * @param base           The base class.
	 * @param implementation The implementation that should be checked.
	 * @return Whether the base class is implementing the given class.
	 * @since v1.4
	 */
	public static boolean checkImplementation(Class<?> base, Class<?> implementation) {
		return ClassUtils.doesImplement(base, implementation);
	}

	/**
	 * Checks if the given class has an empty constructor.
	 *
	 * @param base The class to check.
	 * @return Whether the class has an empty constructor.
	 * @since v1.5.1
	 */
	public static boolean checkEmptyConstructor(Class<?> base) {
		for (Constructor<?> c : base.getConstructors()) {
			if (c.getParameterCount() == 0) return true;
		}
		DIH4JDALogger.warn(String.format("Class %s contains unknown constructor parameters!", base.getSimpleName()));
		return false;
	}
}
