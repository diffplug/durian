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

import java.util.Objects;
import java.util.function.Function;

/** Standard implementations for the various kinds of {@link Box}. */
final class BoxImp {
	private BoxImp() {}

	public static final class Mapped<T, R> implements Box<R> {
		private final Box<T> delegate;
		private final Converter<T, R> converter;

		public Mapped(Box<T> delegate, Converter<T, R> converter) {
			this.delegate = delegate;
			this.converter = converter;
		}

		@Override
		public R get() {
			return converter.convertNonNull(delegate.get());
		}

		@Override
		public void set(R value) {
			delegate.set(converter.revertNonNull(value));
		}

		@Override
		public R modify(Function<? super R, ? extends R> mutator) {
			Objects.requireNonNull(mutator);
			Box.Nullable<R> result = Box.Nullable.of(null);
			delegate.modify(input -> {
				R unmappedResult = mutator.apply(converter.convertNonNull(input));
				result.set(unmappedResult);
				return converter.revertNonNull(unmappedResult);
			});
			return result.get();
		}

		@Override
		public String toString() {
			return "[" + delegate + " mapped to [" + get() + "] by " + converter + "]";
		}
	}

	public static final class Default<T> implements Box<T> {
		private T obj;

		Default(T init) {
			set(init);
		}

		@Override
		public T get() {
			return obj;
		}

		@Override
		public void set(T obj) {
			this.obj = Objects.requireNonNull(obj);
		}

		@Override
		public String toString() {
			return "Box.of[" + get() + "]";
		}
	}

	public static final class Volatile<T> implements Box<T> {
		private volatile T obj;

		Volatile(T init) {
			set(init);
		}

		@Override
		public T get() {
			return obj;
		}

		@Override
		public void set(T obj) {
			this.obj = Objects.requireNonNull(obj);
		}

		@Override
		public String toString() {
			return "Box.ofVolatile[" + get() + "]";
		}
	}

	static final class Nullable {
		private Nullable() {}

		static final class Default<T> implements Box.Nullable<T> {
			private T obj;

			Default(T init) {
				this.obj = init;
			}

			@Override
			public T get() {
				return obj;
			}

			@Override
			public void set(T obj) {
				this.obj = obj;
			}

			@Override
			public String toString() {
				return "Box.Nullable.of[" + get() + "]";
			}
		}

		static final class Volatile<T> implements Box.Nullable<T> {
			private volatile T obj;

			Volatile(T init) {
				set(init);
			}

			@Override
			@javax.annotation.Nullable
			public T get() {
				return obj;
			}

			@Override
			public void set(@javax.annotation.Nullable T obj) {
				this.obj = obj;
			}

			@Override
			public String toString() {
				return "Box.Nullable.ofVolatile[" + get() + "]";
			}
		}

		static final class Mapped<T, R> implements Box.Nullable<R> {
			private final Box.Nullable<T> delegate;
			private final ConverterNullable<T, R> converter;

			public Mapped(Box.Nullable<T> delegate, ConverterNullable<T, R> converter) {
				this.delegate = delegate;
				this.converter = converter;
			}

			@Override
			@javax.annotation.Nullable
			public R get() {
				return converter.convert(delegate.get());
			}

			@Override
			public void set(@javax.annotation.Nullable R value) {
				delegate.set(converter.revert(value));
			}

			/** Shortcut for doing a set() on the result of a get(). */
			@Override
			public R modify(Function<? super R, ? extends R> mutator) {
				Objects.requireNonNull(mutator);
				Box.Nullable<R> result = Box.Nullable.of(null);
				delegate.modify(input -> {
					R unmappedResult = mutator.apply(converter.convert(input));
					result.set(unmappedResult);
					return converter.revert(unmappedResult);
				});
				return result.get();
			}

			@Override
			public String toString() {
				return "[" + delegate + " mapped to [" + get() + "] by " + converter + "]";
			}
		}
	}
}
