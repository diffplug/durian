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

import javax.annotation.Nullable;

/**
 * A {@link Converter} which may receive null and may return null.
 */
public interface ConverterNullable<A, B> {
	/**
	 * Creates a converter using the given functions,
	 * with the given name shown in "toString()".
	 */
	public static <A, B> ConverterNullable<A, B> from(
			Function<? super A, ? extends B> forwardFunction,
			Function<? super B, ? extends A> backwardFunction,
			String name) {
		return new ConverterNullable<A, B>() {
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

	public static <A, B> ConverterNullable<A, B> from(
			Function<? super A, ? extends B> forwardFunction,
			Function<? super B, ? extends A> backwardFunction) {
		return from(forwardFunction, backwardFunction, forwardFunction.toString());
	}

	/**
	 * Returns a representation of {@code a} as an instance of type {@code B}. If {@code a} cannot be
	 * converted, an unchecked exception (such as {@link IllegalArgumentException}) should be thrown.
	 *
	 * @param a the instance to convert; possibly null
	 * @return the converted instance; possibly null
	 */
	@Nullable
	B convert(@Nullable A a);

	/**
	 * Returns a representation of {@code b} as an instance of type {@code A}. If {@code b} cannot be
	 * converted, an unchecked exception (such as {@link IllegalArgumentException}) should be thrown.
	 *
	 * @param b the instance to convert; possibly null
	 * @return the converted instance; possibly null
	 * @throws UnsupportedOperationException if backward conversion is not implemented; this should be
	 *     very rare. Note that if backward conversion is not only unimplemented but
	 *     unimplement<i>able</i> (for example, consider a {@code Converter<Chicken, ChickenNugget>}),
	 *     then this is not logically a {@code Converter} at all, and should just implement {@link
	 *     Function}.
	 */
	@Nullable
	A revert(@Nullable B b);

	/**
	 * Returns a converter whose {@code convert} method applies {@code secondConverter} to the result
	 * of this converter. Its {@code reverse} method applies the converters in reverse order.
	 *
	 * <p>The returned converter is serializable if {@code this} converter and {@code secondConverter} are.
	 */
	default <C> ConverterNullable<A, C> andThen(ConverterNullable<B, C> andThen) {
		return from(
				a -> andThen.convert(convert(a)),
				c -> revert(andThen.revert(c)),
				toString() + " andThen " + andThen.toString());
	}

	/**
	 * Returns the reversed view of this converter, where the {@link #convert(Object)}
	 * and {@link #revert(Object)} methods are swapped.
	 */
	default ConverterNullable<B, A> reverse() {
		return new ReverseConverter<B, A>(this);
	}

	static class ReverseConverter<A, B> implements ConverterNullable<A, B> {
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
		public String toString() {
			return original.toString() + ".reverse()";
		}
	}
}
