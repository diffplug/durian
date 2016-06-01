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

import java.util.Iterator;
import java.util.function.Function;

import javax.annotation.Nullable;

/**
 * A {@link Converter} which may receive null and may return null.
 */
public interface ConverterNullable<A, B> {
	/**
	 * Delegates to {@link #from(Function, Function, String)}, passing
	 * {@code forwardFunction.toString()} as the {@code name} parameter.
	 */
	public static <A, B> ConverterNullable<A, B> from(
			Function<? super A, ? extends B> forwardFunction,
			Function<? super B, ? extends A> backwardFunction) {
		return from(forwardFunction, backwardFunction, forwardFunction.toString());
	}

	/**
	 * Returns a converter based on <i>existing</i> forward and backward functions. Note that it is
	 * unnecessary to create <i>new</i> classes implementing {@code Function} just to pass them in
	 * here. Instead, simply subclass {@code ConverterNullable} and implement its {@link #convert}
	 * and {@link #revert} methods directly.
	 *
	 * <p>The {@code name} parameter will be returned as the {@code toString} of the created object.
	 *
	 * <p>The returned converter is serializable if both provided functions are.
	 */
	public static <A, B> ConverterNullable<A, B> from(
			Function<? super A, ? extends B> forwardFunction,
			Function<? super B, ? extends A> backwardFunction,
			String name) {
		return new ConverterNullableImp.FunctionBasedConverter<>(forwardFunction, backwardFunction, name);
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
		return new ConverterNullableImp.ConverterComposition<>(this, andThen);
	}

	/**
	 * Returns the reversed view of this converter, where the {@link #convert(Object)}
	 * and {@link #revert(Object)} methods are swapped.
	 */
	default ConverterNullable<B, A> reverse() {
		return new ConverterNullableImp.ReverseConverter<B, A>(this);
	}

	/**
	 * Returns an iterable that applies {@code convert} to each element of {@code fromIterable}. The
	 * conversion is done lazily.
	 *
	 * <p>The returned iterable's iterator supports {@code remove()} if the input iterator does. After
	 * a successful {@code remove()} call, {@code fromIterable} no longer contains the corresponding
	 * element.
	 */
	default Iterable<B> convertAll(Iterable<? extends A> fromIterable) {
		requireNonNull(fromIterable);
		return new Iterable<B>() {
			@Override
			public Iterator<B> iterator() {
				return new Iterator<B>() {
					final Iterator<? extends A> fromIterator = fromIterable.iterator();

					@Override
					public boolean hasNext() {
						return fromIterator.hasNext();
					}

					@Override
					public B next() {
						return convert(fromIterator.next());
					}

					@Override
					public void remove() {
						fromIterator.remove();
					}
				};
			}
		};
	}
}
