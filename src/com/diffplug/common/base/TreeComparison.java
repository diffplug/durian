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

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;

/** A mechanism for comparing trees. */
public final class TreeComparison<E, A> {
	/** The tree we expected to get. */
	private final TreeDef<E> expectedDef;
	private final E expectedRoot;
	/** The tree we actually got. */
	private final TreeDef<A> actualDef;
	private final A actualRoot;
	/** Functions for decorating the two sides of the tree when generating ComparisonFailures. */
	private Function<? super E, String> expectedToString = Object::toString;
	private Function<? super A, String> actualToString = Object::toString;

	private TreeComparison(TreeDef<E> expectedDef, E expectedRoot, TreeDef<A> actualDef, A actualRoot) {
		this.expectedDef = expectedDef;
		this.expectedRoot = expectedRoot;
		this.actualDef = actualDef;
		this.actualRoot = actualRoot;
	}

	/** Returns true if the two trees are equal, by calling {@link Objects#equals(Object, Object)} on the results of both mappers. */
	public boolean isEqualMappedBy(Function<? super E, ?> expectedMapper, Function<? super A, ?> actualMapper) {
		return isEqualBasedOn((expected, actual) -> {
			return Objects.equals(expectedMapper.apply(expected), actualMapper.apply(actual));
		});
	}

	/** Returns true if the two trees are equal, based on the given {@link BiPredicate}. */
	public boolean isEqualBasedOn(BiPredicate<? super E, ? super A> compareFunc) {
		return equals(expectedDef, expectedRoot, actualDef, actualRoot, compareFunc);
	}

	/** Recursively determines equality between two trees. */
	private static <E, A> boolean equals(TreeDef<E> expectedDef, E expectedRoot, TreeDef<A> actualDef, A actualRoot, BiPredicate<? super E, ? super A> compareFunc) {
		// compare the roots
		if (!compareFunc.test(expectedRoot, actualRoot)) {
			return false;
		}
		// compare the children lists
		List<E> expectedChildren = expectedDef.childrenOf(expectedRoot);
		List<A> actualChildren = actualDef.childrenOf(actualRoot);
		if (expectedChildren.size() != actualChildren.size()) {
			return false;
		}
		// recurse on each pair of children
		for (int i = 0; i < expectedChildren.size(); ++i) {
			E expectedChild = expectedChildren.get(i);
			A actualChild = actualChildren.get(i);
			if (!equals(expectedDef, expectedChild, actualDef, actualChild, compareFunc)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Asserts that the trees are equal, by calling {@link Objects#equals(Object, Object)} on the results of both mappers.
	 * 
	 * @see #isEqualMappedBy(Function, Function)
	 */
	public void assertEqualMappedBy(Function<? super E, ?> expectedMapper, Function<? super A, ?> actualMapper) {
		if (!isEqualMappedBy(expectedMapper, actualMapper)) {
			throwAssertionError();
		}
	}

	/**
	 * Asserts that the trees are equal, based on the given {@link BiPredicate}.
	 * 
	 * @see #isEqualBasedOn(BiPredicate)
	 */
	public void assertEqualBasedOn(BiPredicate<E, A> compareFunc) {
		if (!isEqualBasedOn(compareFunc)) {
			throwAssertionError();
		}
	}

	/** Decorates errors thrown by any assertions with the given functions. */
	public TreeComparison<E, A> decorateErrorsWith(Function<? super E, String> expectedToString, Function<? super A, String> actualToString) {
		this.expectedToString = expectedToString;
		this.actualToString = actualToString;
		return this;
	}

	/** Throws an assetion error. */
	private void throwAssertionError() {
		throw createAssertionError();
	}

	/**
	 * Returns an {@link AssertionError} containing the contents of the
	 * two trees. Attempts to throw a JUnit ComparisonFailure if JUnit
	 * is on the class path, but it fails to a plain old {@code java.lang.AssertionError}
	 * if the reflection calls fail. 
	 */
	private AssertionError createAssertionError() {
		// convert both sides to strings
		String expected = TreeQuery.toString(expectedDef, expectedRoot, expectedToString);
		String actual = TreeQuery.toString(actualDef, actualRoot, actualToString);
		// try to create a junit ComparisonFailure
		for (String exceptionType : Arrays.asList(
				"org.junit.ComparisonFailure",
				"junit.framework.ComparisonFailure")) {
			try {
				return createComparisonFailure(exceptionType, expected, actual);
			} catch (Exception e) {}
		}
		// we'll have to settle for a plain-jane AssertionError
		return new AssertionError("Expected:\n" + expected + "\n\nActual:\n" + actual);
	}

	/** Attempts to create an instance of junit's ComparisonFailure exception using reflection. */
	private AssertionError createComparisonFailure(String className, String expected, String actual) throws Exception {
		Class<?> clazz = Class.forName(className);
		Constructor<?> constructor = clazz.getConstructor(String.class, String.class, String.class);
		return (AssertionError) constructor.newInstance("", expected, actual);
	}

	/** Maps both sides of the comparison to the same type, for easier comparison and assertions. */
	public <T> SameType<T> mapToSame(Function<? super E, ? extends T> mapExpected, Function<? super A, ? extends T> mapActual) {
		return new SameTypeImp<>(this, mapExpected, mapActual);
	}

	/** An API for comparing trees which have been mapped to the same type. */
	public interface SameType<T> {
		/** Returns true if the trees are equal. */
		boolean isEqual();

		/** Asserts that the trees are equal. */
		void assertEqual();

		/** Decorates errors thrown by any assertions with the given functions. */
		SameType<T> decorateErrorsWith(Function<? super T, String> toString);

		/** Maps this SameType to some other type. */
		<R> SameType<R> map(Function<? super T, ? extends R> mapper);
	}

	/** A TreeComparison with convenience methods for creating a new  */
	private static class SameTypeImp<E, A, T> implements SameType<T> {
		private final TreeComparison<E, A> comparison;
		private final Function<? super E, ? extends T> mapExpected;
		private final Function<? super A, ? extends T> mapActual;

		public SameTypeImp(TreeComparison<E, A> comparison, Function<? super E, ? extends T> mapExpected, Function<? super A, ? extends T> mapActual) {
			this.comparison = comparison;
			this.mapExpected = mapExpected;
			this.mapActual = mapActual;
			comparison.decorateErrorsWith(mapExpected.andThen(Objects::toString), mapActual.andThen(Objects::toString));
		}

		/** Returns true if the two trees are equal. */
		@Override
		public boolean isEqual() {
			return comparison.isEqualMappedBy(mapExpected, mapActual);
		}

		/** Asserts that the two trees are equal. */
		@Override
		public void assertEqual() {
			comparison.assertEqualMappedBy(mapExpected, mapActual);
		}

		/** Decorates errors thrown by assertions with the given function. */
		@Override
		public SameType<T> decorateErrorsWith(Function<? super T, String> toString) {
			comparison.decorateErrorsWith(toString.compose(mapExpected), toString.compose(mapActual));
			return this;
		}

		/** Maps the variable on which the comparisons will take place. */
		@Override
		public <R> SameType<R> map(Function<? super T, ? extends R> mapper) {
			return new SameTypeImp<>(comparison, mapExpected.andThen(mapper), mapActual.andThen(mapper));
		}
	}

	/** Creates a {@link TreeComparison} for comparing the two trees. */
	public static <E, A> TreeComparison<E, A> of(TreeDef<E> expectedDef, E expectedRoot, TreeDef<A> actualDef, A actualRoot) {
		return new TreeComparison<>(expectedDef, expectedRoot, actualDef, actualRoot);
	}

	/** Creates a {@link SameType} for comparing two trees of the same type. */
	public static <T> SameType<T> of(TreeDef<T> treeDef, T expected, T actual) {
		return of(treeDef, expected, treeDef, actual).mapToSame(Function.identity(), Function.identity());
	}

	/** Creates a {@link SameType} for comparing a {@link TreeNode} against a generic tree. */
	public static <T> SameType<T> of(TreeNode<T> expected, TreeDef<T> treeDef, T actual) {
		return of(expected, treeDef, actual, Function.identity());
	}

	/** Creates a {@link SameType} for comparing a {@link TreeNode} against a generic tree which been mapped. */
	public static <T, U> SameType<T> of(TreeNode<T> expected, TreeDef<U> treeDef, U actual, Function<? super U, ? extends T> mapper) {
		return of(TreeNode.treeDef(), expected, treeDef, actual).mapToSame(TreeNode::getContent, mapper);
	}

	/** Creates a {@link SameType} from the given two {@link TreeNode}s of the same type. */
	public static <T> SameType<T> of(TreeNode<T> expected, TreeNode<T> actual) {
		return of(TreeNode.treeDef(), expected, TreeNode.treeDef(), actual).mapToSame(TreeNode::getContent, TreeNode::getContent);
	}
}
