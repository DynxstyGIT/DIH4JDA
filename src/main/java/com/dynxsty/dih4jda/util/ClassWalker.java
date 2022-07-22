package com.dynxsty.dih4jda.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ClassWalker {

	private final String packageName;

	public ClassWalker(String packageName) {
		this.packageName = packageName;
	}

	/**
	 * Gets all classes inside the given package and sub packages.
	 *
	 * @return An unmodifiable {@link Set} of classes inside the given package.
	 */
	@NotNull
	public Set<Class<?>> getAllClasses() {
		InputStream is = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(packageName.replaceAll("[.]", "/"));
		if (is == null) return Set.of();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		Set<Class<?>> classes = new HashSet<>();
		for (String line : reader.lines().collect(Collectors.toSet())) {
			if (line.endsWith(".class")) classes.add(getClass(line));
			else classes.addAll(new ClassWalker(packageName + "." + line).getAllClasses());
		}
		return classes;
	}

	/**
	 * Attempts to get a single class, based on the given name.
	 *
	 * @param className The name of the class.
	 * @return The class with the given name.
	 */
	@Nullable
	private Class<?> getClass(@NotNull String className) {
		try {
			return Class.forName(packageName + "."
					+ className.substring(0, className.lastIndexOf('.')));
		} catch (ClassNotFoundException exception) {
			exception.printStackTrace();
		}
		return null;
	}

	/**
	 * Gets all classes in the given package and sub packages which extend the specified class.
	 *
	 * @param type The parent class to search for.
	 * @return An unmodifiable {@link Set} of classes which are assignable to the given type.
	 */
	@NotNull
	public <T> Set<Class<? extends T>> getSubTypesOf(@NotNull Class<T> type) {
		return getAllClasses()
				.stream()
				.filter(type::isAssignableFrom)
				.map(clazz -> (Class<? extends T>) clazz)
				.collect(Collectors.toSet());
	}
}
