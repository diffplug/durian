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

import java.util.function.Function;

/**
 * A {@link Converter} which can neither receive nor return null.
 */
public interface ConverterNonNull<A, B> {
	/**
	 * Creates a converter using the given functions,
	 * with the given name shown in "toString()".
	 */
	public static <A, B> ConverterNonNull<A, B> from(
			Function<? super A, ? extends B> forwardFunction,
			Function<? super B, ? extends A> backwardFunction,
			String name) {
		return new ConverterNonNull<A, B>() {
			@Override
			public B convert(A a) {
				return forwardFunction.apply(a);
			}

			@Override
			public A revert(B b) {
				return backwardFunction.apply(b);
			}

			@Override
			public String toString() {
				return name;
			}
		};
	}

	/**
	 * Returns a representation of {@code a} as an instance of type {@code B}. If {@code a} cannot be
	 * converted, an unchecked exception (such as {@link IllegalArgumentException}) should be thrown.
	 *
	 * @param a the instance to convert; never null
	 * @return the converted instance; never null
	 */
	B convert(A a);

	/**
	 * Returns a representation of {@code b} as an instance of type {@code A}. If {@code b} cannot be
	 * converted, an unchecked exception (such as {@link IllegalArgumentException}) should be thrown.
	 *
	 * @param b the instance to convert; never null
	 * @return the converted instance; never null
	 * @throws UnsupportedOperationException if backward conversion is not implemented; this should be
	 *     very rare. Note that if backward conversion is not only unimplemented but
	 *     unimplement<i>able</i> (for example, consider a {@code Converter<Chicken, ChickenNugget>}),
	 *     then this is not logically a {@code Converter} at all, and should just implement {@link
	 *     Function}.
	 */
	A revert(B b);

	/**
	 * Returns a converter whose {@code convert} method applies {@code secondConverter} to the result
	 * of this converter. Its {@code reverse} method applies the converters in reverse order.
	 *
	 * <p>The returned converter is serializable if {@code this} converter and {@code secondConverter} are.
	 */
	default <C> ConverterNonNull<A, C> andThen(ConverterNonNull<B, C> andThen) {
		return from(
				a -> andThen.convert(convert(a)),
				c -> revert(andThen.revert(c)),
				toString() + " andThen " + andThen.toString());
	}

	/**
	 * Returns the reversed view of this converter, where the {@link #convert(Object)}
	 * and {@link #revert(Object)} methods are swapped.
	 */
	default ConverterNonNull<B, A> reverse() {
		return new ReverseConverter<B, A>(this);
	}

	static class ReverseConverter<A, B> implements ConverterNonNull<A, B> {
		final ConverterNonNull<B, A> original;

		ReverseConverter(ConverterNonNull<B, A> original) {
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
		public ConverterNonNull<B, A> reverse() {
			return original;
		}

		@Override
		public String toString() {
			return original.toString() + ".reverse()";
		}
	}
}
