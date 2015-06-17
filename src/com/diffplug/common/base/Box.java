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
		return new Default<T>(value);
	}

	/** A simple implementation of Box. */
	public static class Default<T> implements Box<T> {
		/** The (possibly-null) object being held. */
		protected volatile T obj;

		protected Default(T init) {
			this.obj = Objects.requireNonNull(init);
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
			return "Box[" + get().toString() + "]";
		}
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
			return new Default<T>(init);
		}

		/** Creates an Nullable holding null. */
		public static <T> Nullable<T> ofNull() {
			return new Default<T>(null);
		}

		/** A simple implementation of Box.Nullable. */
		public static class Default<T> implements Box.Nullable<T> {
			/** The (possibly-null) object being held. */
			protected volatile T obj;

			protected Default(T init) {
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
				return "Box.Nullable[" + (get() == null ? "null" : get().toString()) + "]";
			}
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
	public interface Dbl extends DoubleSupplier, DoubleConsumer {
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

		/** Returns a Box wrapped around the given double. */
		public static Dbl of(double value) {
			return new Default(value);
		}

		/** A simple implementation of Box.Double. */
		public static class Default implements Box.Dbl {
			/** The (possibly-null) object being held. */
			protected volatile double obj;

			protected Default(double init) {
				this.obj = init;
			}

			@Override
			public double get() {
				return obj;
			}

			@Override
			public void set(double obj) {
				this.obj = obj;
			}

			@Override
			public String toString() {
				return "Box.Double[" + obj + "]";
			}
		}

		/** Creates a Box.Double from a Supplier and a Consumer. */
		public static Dbl from(DoubleSupplier getter, DoubleConsumer setter) {
			return new Dbl() {
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

		/** Returns a Box wrapped around the given double. */
		public static Int of(int value) {
			return new Default(value);
		}

		/** A simple implementation of Box.Int. */
		public static class Default implements Box.Int {
			/** The (possibly-null) object being held. */
			protected volatile int obj;

			protected Default(int init) {
				this.obj = init;
			}

			@Override
			public int get() {
				return obj;
			}

			@Override
			public void set(int obj) {
				this.obj = obj;
			}

			@Override
			public String toString() {
				return "Box.Int[" + obj + "]";
			}
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
