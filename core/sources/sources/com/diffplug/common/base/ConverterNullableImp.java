/*
 * Original Guava code is copyright (C) 2015 The Guava Authors.
 * Modifications from Guava are copyright (C) 2016 DiffPlug.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.diffplug.common.base;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.util.function.Function;

import javax.annotation.Nullable;

final class ConverterNullableImp {
	private ConverterNullableImp() {}

	static final class FunctionBasedConverter<A, B> implements ConverterNullable<A, B>, Serializable {
		private static final long serialVersionUID = 1L;

		final Function<? super A, ? extends B> forwardFunction;
		final Function<? super B, ? extends A> backwardFunction;
		final String name;

		FunctionBasedConverter(
				Function<? super A, ? extends B> forwardFunction,
				Function<? super B, ? extends A> backwardFunction,
				String name) {
			this.forwardFunction = requireNonNull(forwardFunction);
			this.backwardFunction = requireNonNull(backwardFunction);
			this.name = requireNonNull(name);
		}

		@Override
		public B convert(A a) {
			return forwardFunction.apply(a);
		}

		@Override
		public A revert(B b) {
			return backwardFunction.apply(b);
		}

		@Override
		public boolean equals(@Nullable Object object) {
			if (object instanceof FunctionBasedConverter) {
				FunctionBasedConverter<?, ?> that = (FunctionBasedConverter<?, ?>) object;
				return this.forwardFunction.equals(that.forwardFunction)
						&& this.backwardFunction.equals(that.backwardFunction);
			} else {
				return false;
			}
		}

		@Override
		public int hashCode() {
			return forwardFunction.hashCode() * 31 + backwardFunction.hashCode();
		}

		@Override
		public String toString() {
			return name;
		}
	}

	static final class ConverterComposition<A, B, C> implements ConverterNullable<A, C>, Serializable {
		private static final long serialVersionUID = 1L;

		final ConverterNullable<A, B> first;
		final ConverterNullable<B, C> second;

		ConverterComposition(ConverterNullable<A, B> first, ConverterNullable<B, C> second) {
			this.first = requireNonNull(first);
			this.second = requireNonNull(second);
		}

		@Override
		public C convert(A a) {
			return second.convert(first.convert(a));
		}

		@Override
		public A revert(C c) {
			return first.revert(second.revert(c));
		}

		@Override
		public boolean equals(@Nullable Object object) {
			if (object instanceof ConverterComposition) {
				ConverterComposition<?, ?, ?> that = (ConverterComposition<?, ?, ?>) object;
				return this.first.equals(that.first) && this.second.equals(that.second);
			} else {
				return false;
			}
		}

		@Override
		public int hashCode() {
			return 31 * first.hashCode() + second.hashCode();
		}

		@Override
		public String toString() {
			return first + ".andThen(" + second + ")";
		}
	}

	static class ReverseConverter<A, B> implements ConverterNullable<A, B>, Serializable {
		private static final long serialVersionUID = 1L;

		final ConverterNullable<B, A> original;

		ReverseConverter(ConverterNullable<B, A> original) {
			this.original = original;
		}

		@Override
		public B convert(A a) {
			return original.revert(a);
		}

		@Override
		public A revert(B b) {
			return original.convert(b);
		}

		@Override
		public ConverterNullable<B, A> reverse() {
			return original;
		}

		@Override
		public boolean equals(@Nullable Object object) {
			if (object instanceof ReverseConverter) {
				ReverseConverter<?, ?> that = (ReverseConverter<?, ?>) object;
				return this.original.equals(that.original);
			} else {
				return false;
			}
		}

		@Override
		public int hashCode() {
			return ~original.hashCode();
		}

		@Override
		public String toString() {
			return original.toString() + ".reverse()";
		}
	}
}
