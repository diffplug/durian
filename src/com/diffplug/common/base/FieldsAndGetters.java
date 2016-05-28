/*
 * Copyright 2016 DiffPlug
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.diffplug.common.base;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Utilities for obtaining the fields and getter methods of an object using reflection.
 * Useful for first-pass debugging of runtime objects.
 */
public class FieldsAndGetters {
	/**
	 * Returns a {@code Stream} of all public fields and their values for the given object.
	 *
	 * @see #fields(Object, Predicate)
	 */
	public static Stream<Map.Entry<Field, Object>> fields(Object obj) {
		return fields(obj, Predicates.alwaysTrue());
	}

	/**
	 * Returns a {@code Stream} of all public fields which match {@code predicate} and their values for the given object.
	 * <p>
	 * This method uses reflection to find all of the public instance fields of the given object,
	 * and if they pass the given predicate, it includes them in a stream of {@code Map.Entry<Field, Object>}
	 * where the entry's value is the value of the field for this object.
	 */
	public static Stream<Map.Entry<Field, Object>> fields(Object obj, Predicate<Field> predicate) {
		Class<?> clazz = obj == null ? ObjectIsNull.class : obj.getClass();
		return Arrays.asList(clazz.getFields()).stream()
				// gotta be public
				.filter(field -> Modifier.isPublic(field.getModifiers()))
				// gotta be an instance field
				.filter(field -> !Modifier.isStatic(field.getModifiers()))
				// gotta pass the predicate 
				.filter(predicate)
				// get its value
				.map(field -> createEntry(field, tryCall(field.getName(), () -> field.get(obj))));
	}

	/**
	 * Returns a {@code Stream} of all public getter methods and their return values for the given object.
	 *
	 * @see #getters(Object, Predicate)
	 */
	public static Stream<Map.Entry<Method, Object>> getters(Object obj) {
		return getters(obj, Predicates.alwaysTrue());
	}

	/**
	 * Returns a {@code Stream} of all public getter methods which match {@code predicate} and their return values for the given object.
	 * <p>
	 * This method uses reflection to find all of the public instance methods which don't take any arguments
	 * and return a value.  If they pass the given predicate, then they are called, and the return value is
	 * included in a stream of {@code Map.Entry<Method, Object>}.
	 * <p>
	 * Note that there are some methods which have the signature of a getter, but actually mutate the object
	 * being inspected, e.g. {@link java.io.InputStream#read()}.  These will be called unless you manually
	 * exclude them using the predicate.
	 */
	public static Stream<Map.Entry<Method, Object>> getters(Object obj, Predicate<Method> predicate) {
		Class<?> clazz = obj == null ? ObjectIsNull.class : obj.getClass();
		return Arrays.asList(clazz.getMethods()).stream()
				// we only want methods that don't take parameters
				.filter(method -> method.getParameterTypes().length == 0)
				// we only want public methods
				.filter(method -> Modifier.isPublic(method.getModifiers()))
				// we only want instance methods
				.filter(method -> !Modifier.isStatic(method.getModifiers()))
				// we only want methods that don't return void
				.filter(method -> !method.getReturnType().equals(Void.TYPE))
				// we only want methods that pass our predicate
				.filter(predicate)
				// turn it into Map<Method, Result>
				.map(method -> createEntry(method, tryCall(method.getName(), () -> method.invoke(obj))));
	}

	/** Sentinel class for null objects. */
	public static class ObjectIsNull {}

	/** Executes the given function, return any exceptions it might throw as wrapped values. */
	private static Object tryCall(String methodName, Throwing.Supplier<Object> supplier) {
		try {
			return supplier.get();
		} catch (Throwable error) {
			return new CallException(methodName, error);
		}
	}

	/** Exception which wraps up a thrown exception - ensures that users don't think an exception was returned. */
	private static class CallException extends Exception {
		private static final long serialVersionUID = 1206955156719866328L;

		private final String methodName;

		private CallException(String methodName, Throwable cause) {
			super(cause);
			this.methodName = methodName;
		}

		@Override
		public String toString() {
			return "When calling " + methodName + ": " + getCause().getMessage();
		}
	}

	/**
	 * Returns a {@code Stream} of all public fields and getter methods and their values for the given object.
	 *
	 * @see #getters(Object, Predicate)
	 */
	public static Stream<Map.Entry<String, Object>> fieldsAndGetters(Object obj) {
		return fieldsAndGetters(obj, Predicates.alwaysTrue());
	}

	/**
	 * Returns a {@code Stream} of all public fields and getter methods which match {@code predicate} and their values for the given object.
	 * <p>
	 * This method combines the results of {@link #fields(Object, Predicate)} and {@link #getters(Object, Predicate)}. The {@code Predicate<String>}
	 * will be passed the field names and the getter names (which are postfixed by {@code ()} to mark them as methods).
	 * 
	 * @see #fields(Object, Predicate)
	 * @see #getters(Object, Predicate)
	 */
	public static Stream<Map.Entry<String, Object>> fieldsAndGetters(Object obj, Predicate<String> predicate) {
		Stream<Map.Entry<String, Object>> fields = fields(obj, field -> predicate.test(field.getName()))
				.map(entry -> createEntry(entry.getKey().getName(), entry.getValue()));
		Function<Method, String> methodName = method -> method.getName() + "()";
		Stream<Map.Entry<String, Object>> getters = getters(obj, field -> predicate.test(methodName.apply(field)))
				.map(entry -> createEntry(methodName.apply(entry.getKey()), entry.getValue()));
		return Stream.concat(fields, getters);
	}

	/**
	 * Passes each field and getter of {@code obj} to {@code evalPredicate}, grabs its value if it passes, and if the value passes {@code dumpPredicate} then it is dumped to {@code printer}.
	 * @see #fieldsAndGetters(Object, Predicate)
	 */
	public static void dumpIf(String name, Object obj, Predicate<String> evalPredicate, Predicate<Map.Entry<String, Object>> dumpPredicate, StringPrinter printer) {
		printer.println(name + ": " + obj.getClass().getName());
		fieldsAndGetters(obj, evalPredicate).filter(dumpPredicate).forEach(entry -> {
			printer.println("\t" + entry.getKey() + " = " + entry.getValue());
		});
	}

	/**
	 * Dumps all fields and getters of {@code obj} to {@code System.out}.
	 * @see #dumpIf
	 */
	public static void dumpAll(String name, Object obj) {
		dumpAll(name, obj, StringPrinter.systemOut());
	}

	/**
	 * Dumps all non-null fields and getters of {@code obj} to {@code System.out}.
	 * @see #dumpIf
	 */
	public static void dumpNonNull(String name, Object obj) {
		dumpNonNull(name, obj, StringPrinter.systemOut());
	}

	/**
	 * Dumps all fields and getters of {@code obj} to {@code printer}.
	 * @see #dumpIf
	 */
	public static void dumpAll(String name, Object obj, StringPrinter printer) {
		dumpIf(name, obj, Predicates.alwaysTrue(), Predicates.alwaysTrue(), printer);
	}

	/**
	 * Dumps all non-null fields and getters of {@code obj} to {@code printer}.
	 * @see #dumpIf
	 */
	public static void dumpNonNull(String name, Object obj, StringPrinter printer) {
		dumpIf(name, obj, Predicates.alwaysTrue(), entry -> entry.getValue() != null, printer);
	}

	/** Creates an immutable Map.Entry. */
	private static <K, V> Map.Entry<K, V> createEntry(K key, V value) {
		return new Map.Entry<K, V>() {
			@Override
			public K getKey() {
				return key;
			}

			@Override
			public V getValue() {
				return value;
			}

			@Override
			public V setValue(V value) {
				throw new UnsupportedOperationException();
			}
		};
	}
}
