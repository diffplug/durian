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
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/** Provides get/set access to a mutable non-null value. */
public interface Box<T> extends Supplier<T>, Consumer<T> {
	/** Sets the value which will later be returned by get(). */
	void set(T value);

	/** Delegates to set(). */
	default void accept(T value) {
		set(value);
	}

	/** Maps one {@code Box} to another {@code Box}. */
	default <R> Box<R> map(Function<? super T, ? extends R> getMapper, Function<? super R, ? extends T> setMapper) {
		return Box.from(() -> getMapper.apply(get()), toSet -> set(setMapper.apply(toSet)));
	}

	/** Creates a Box holding the given value. */
	public static <T> Box<T> of(T value) {
		return new BoxImplementations.BoxImp<T>(value);
	}

	/** Creates a Box from a Supplier and a Consumer. */
	public static <T> Box<T> from(Supplier<T> getter, Consumer<T> setter) {
		return new Box<T>() {
			@Override
			public T get() {
				return Objects.requireNonNull(getter.get());
			}

			@Override
			public void set(T value) {
				setter.accept(Objects.requireNonNull(value));
			}
		};
	}

	/** Creates a Box from an argument and two functions which operate on that argument. */
	public static <T, V> Box<T> from(V target, Function<V, T> getter, BiConsumer<V, T> setter) {
		return new Box<T>() {
			@Override
			public T get() {
				return Objects.requireNonNull(getter.apply(target));
			}

			@Override
			public void set(T value) {
				setter.accept(target, Objects.requireNonNull(value));
			}
		};
	}

	/** Provides get/set access to a mutable nullable value. */
	public interface Nullable<T> extends Supplier<T>, Consumer<T> {
		/** Sets the value which will later be returned by get(). */
		void set(T value);

		/** Delegates to set(). */
		default void accept(T value) {
			set(value);
		}

		/** Creates a Nullable of the given object. */
		public static <T> Nullable<T> of(T init) {
			return new BoxImplementations.NullableImp<T>(init);
		}

		/** Creates an Nullable holding null. */
		public static <T> Nullable<T> ofNull() {
			return new BoxImplementations.NullableImp<T>(null);
		}

		/** Creates a Nullable from a Supplier and a Consumer. */
		public static <T> Nullable<T> from(Supplier<T> getter, Consumer<T> setter) {
			return new Nullable<T>() {
				@Override
				public T get() {
					return getter.get();
				}

				@Override
				public void set(T value) {
					setter.accept(value);
				}
			};
		}

		/** Creates a Nullable from an argument and two functions which operate on that argument. */
		public static <T, V> Nullable<T> from(V target, Function<V, T> getter, BiConsumer<V, T> setter) {
			return new Nullable<T>() {
				@Override
				public T get() {
					return getter.apply(target);
				}

				@Override
				public void set(T value) {
					setter.accept(target, value);
				}
			};
		}
	}

	/** A Box for primitive doubles. */
	public interface Double extends DoubleSupplier, DoubleConsumer {
		/** Returns a Box wrapped around the given double. */
		public static Double of(double value) {
			return new BoxImplementations.DoubleImp(value);
		}

		/** Sets the value which will later be returned by get(). */
		void set(double value);

		/** Returns the boxed value. */
		double get();

		/** Delegates to set(). */
		@Override
		default void accept(double value) {
			set(value);
		}

		/** Delegates to get(). */
		@Override
		default double getAsDouble() {
			return get();
		}

		/** Creates a Box.Double from a Supplier and a Consumer. */
		public static Double from(DoubleSupplier getter, DoubleConsumer setter) {
			return new Double() {
				@Override
				public double get() {
					return getter.getAsDouble();
				}

				@Override
				public void set(double value) {
					setter.accept(value);
				}
			};
		}
	}

	/** A Box for primitive ints. */
	public interface Int extends IntSupplier, IntConsumer {
		/** Returns a Box wrapped around the given double. */
		public static Double of(double value) {
			return new BoxImplementations.DoubleImp(value);
		}

		/** Sets the value which will later be returned by get(). */
		void set(int value);

		/** Returns the boxed value. */
		int get();

		/** Delegates to set(). */
		@Override
		default void accept(int value) {
			set(value);
		}

		/** Delegates to get(). */
		@Override
		default int getAsInt() {
			return get();
		}

		/** Creates a Box.Double from a Supplier and a Consumer. */
		public static Int from(IntSupplier getter, IntConsumer setter) {
			return new Int() {
				@Override
				public int get() {
					return getter.getAsInt();
				}

				@Override
				public void set(int value) {
					setter.accept(value);
				}
			};
		}
	}
}
