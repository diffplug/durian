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

import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.annotation.Nullable;

/**
 * Helper functions for manipulating {@link Function}, copied from Guava.
 * <p>
 * The function signatures below are identical to Google's Guava 18.0, except that guava's functional
 * interfaces have been swapped with Java 8's. It is tested against the same test suite
 * as Google Guava to ensure functional compatibility.
 * <p>
 * Most of the implementation has been replaced with lambdas, which means that the following
 * functionality has been removed: equals(), hashCode(), toString(), GWT, and serialization.
 * <p>
 * Lambdas don't support these methods, and there isn't much reason why they should, so we
 * removed them in Durian.
 */
public final class Functions {
	private Functions() {}

	/**
	 * Returns the identity function.
	 */
	// implementation is "fully variant"; E has become a "pass-through" type
	public static <E> Function<E, E> identity() {
		return Function.identity();
	}

	/**
	 * Returns a function which performs a map lookup. The returned function throws an {@link
	 * IllegalArgumentException} if given a key that does not exist in the map. See also {@link
	 * #forMap(Map, Object)}, which returns a default value in this case.
	 */
	public static <K, V> Function<K, V> forMap(Map<K, V> map) {
		return key -> {
			if (map.containsKey(key)) {
				return map.get(key);
			} else {
				throw new IllegalArgumentException("Key '" + key + "' not present in map");
			}
		};
	}

	/**
	 * Returns a function which performs a map lookup with a default value. The function created by
	 * this method returns {@code defaultValue} for all inputs that do not belong to the map's key
	 * set. See also {@link #forMap(Map)}, which throws an exception in this case.
	 *
	 * @param map source map that determines the function behavior
	 * @param defaultValue the value to return for inputs that aren't map keys
	 * @return function that returns {@code map.get(a)} when {@code a} is a key, or {@code
	 *         defaultValue} otherwise
	 */
	public static <K, V> Function<K, V> forMap(Map<K, ? extends V> map, @Nullable V defaultValue) {
		return key -> map.containsKey(key) ? map.get(key) : defaultValue;
	}

	/**
	 * Returns the composition of two functions. For {@code f: A->B} and {@code g: B->C}, composition
	 * is defined as the function h such that {@code h(a) == g(f(a))} for each {@code a}.
	 *
	 * @param g the second function to apply
	 * @param f the first function to apply
	 * @return the composition of {@code f} and {@code g}
	 * @see <a href="//en.wikipedia.org/wiki/Function_composition">function composition</a>
	 */
	public static <A, B, C> Function<A, C> compose(Function<B, C> g, Function<A, ? extends B> f) {
		return g.compose(f);
	}

	/**
	 * Creates a function that returns the same boolean output as the given predicate for all inputs.
	 *
	 * <p>The returned function is <i>consistent with equals</i> (as documented at {@link
	 * Function#apply}) if and only if {@code predicate} is itself consistent with equals.
	 */
	public static <T> Function<T, Boolean> forPredicate(Predicate<T> predicate) {
		return predicate::test;
	}

	/**
	 * Creates a function that returns {@code value} for any input.
	 *
	 * @param value the constant value for the function to return
	 * @return a function that always returns {@code value}
	 */
	public static <E> Function<Object, E> constant(@Nullable E value) {
		return input -> value;
	}

	/**
	 * Returns a function that always returns the result of invoking {@link Supplier#get} on {@code
	 * supplier}, regardless of its input.
	 * 
	 * @since 10.0
	 */
	public static <T> Function<Object, T> forSupplier(Supplier<T> supplier) {
		return input -> supplier.get();
	}
}
