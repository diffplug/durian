/*
 * Copyright 2015 DiffPlug
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

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A minimal implementation of Either.
 */
public interface Either<L, R> {
	/** True if it's left. */
	boolean isLeft();

	/** True if it's right. */
	default boolean isRight() {
		return !isLeft();
	}

	/** Returns the left side. Throws an exception if it's really a Right. */
	L getLeft();

	/** Returns the right side. Throws an exception if it's really a Left. */
	R getRight();

	/** Performs the given action if this is a Left. */
	default void ifLeft(Consumer<? super L> consumer) {
		if (isLeft()) {
			consumer.accept(getLeft());
		}
	}

	/** Performs the given action if this is a Right. */
	default void ifRight(Consumer<? super R> consumer) {
		if (isRight()) {
			consumer.accept(getRight());
		}
	}

	/** Returns the left side as an Optional. */
	default Optional<L> asOptionalLeft() {
		return fold(Optional::of, val -> Optional.<L> empty());
	}

	/** Returns the right side as an Optional. */
	default Optional<R> asOptionalRight() {
		return fold(val -> Optional.<R> empty(), Optional::of);
	}

	/** Sets both the left and right consumers, using the default values to set the empty side. */
	default void setBoth(Consumer<L> left, Consumer<R> right, L defaultLeft, R defaultRight) {
		left.accept(isLeft() ? getLeft() : defaultLeft);
		right.accept(isRight() ? getRight() : defaultRight);
	}

	/** Applies either the left or the right function as appropriate. */
	default <T> T fold(Function<? super L, ? extends T> left, Function<? super R, ? extends T> right) {
		if (isLeft()) {
			return left.apply(getLeft());
		} else {
			return right.apply(getRight());
		}
	}

	/** Creates an instance of Left. */
	public static <L, R> Either<L, R> createLeft(L l) {
		return new Left<L, R>(l);
	}

	/** Creates an instance of Right. */
	public static <L, R> Either<L, R> createRight(R r) {
		return new Right<L, R>(r);
	}

	/** Implementation of left. */
	static final class Left<L, R> implements Either<L, R> {
		private final L value;

		public Left(L value) {
			this.value = Objects.requireNonNull(value);
		}

		@Override
		public boolean isLeft() {
			return true;
		}

		@Override
		public L getLeft() {
			return value;
		}

		@Override
		public R getRight() {
			throw Unhandled.operationException();
		}

		@Override
		public boolean equals(Object otherObj) {
			if (otherObj instanceof Left) {
				return Objects.equals(value, ((Left<?, ?>) otherObj).value);
			} else {
				return false;
			}
		}

		@Override
		public int hashCode() {
			return Objects.hash(Left.class, value);
		}

		@Override
		public String toString() {
			return "Left[" + value.toString() + "]";
		}
	}

	/** Implementation of right. */
	static final class Right<L, R> implements Either<L, R> {
		private final R value;

		public Right(R value) {
			this.value = Objects.requireNonNull(value);
		}

		@Override
		public boolean isLeft() {
			return false;
		}

		@Override
		public L getLeft() {
			throw Unhandled.operationException();
		}

		@Override
		public R getRight() {
			return value;
		}

		@Override
		public boolean equals(Object otherObj) {
			if (otherObj instanceof Right) {
				return Objects.equals(value, ((Right<?, ?>) otherObj).value);
			} else {
				return false;
			}
		}

		@Override
		public int hashCode() {
			return Objects.hash(Right.class, value);
		}

		@Override
		public String toString() {
			return "Right[" + value.toString() + "]";
		}
	}
}
