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
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.LongConsumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

/**
 * Provides get/set access to a mutable non-null value.  See {@link Box.Nullable} if
 * you need the ability to handle null values, or {@link Box.Dbl}, {@link Box.Int},
 * or {@link Box.Lng} if you'd like better performance for primitive values.
 *
 * Some implementations may provide atomicity or concurrency guarantees around the
 * {@link #modify(Function)} method, but this is not required of `Box` in general.
 *
 * All implementations have a {@link #map(Converter)} method for transforming
 * the underlying storage location and making it appear to hold a different kind
 * of value.  Any atomicity guarantees of `modify` are maintained by `map`.
 *
 * The primary use cases for `Box` are:
 *
 * - giving "member variables" to lambda expressions
 * - reifying and mapping fields
 *
 * ## Member variables for lambdas
 *
 * It's often helpful for a lambda to hold some state, but lambdas can only reference
 * variables which are effectively final.  Boxes make it easy to circumvent this
 * limitation.
 *
 *```java
 * public static Consumer<String> stringsToLines(Consumer<String> perLine) {
 *     Box<String> leftover = Box.of("");
 *     return rawString -> {
 *         int lastIdx = 0;
 *         int idx = 0;
 *         while ((idx = rawString.indexOf('\n', lastIdx)) > -1) {
 *             perLine.accept(rawString.substring(lastIdx, idx));
 *             lastIdx = idx + 1;
 *         }
 *         leftover.set(rawString.substring(lastIdx));
 *     };
 * }
 * ```
 * 
 * ## Reifying fields
 *
 * Let's say you've got various game entities that you're drawing on a screen:
 *
 * ```java
 * class SpaceShip { Point2D p; }
 * class Bullet {
 *    int getX(); void setX(int x);
 *    int getY(); void setY(int y);
 * }
 * ```
 *
 * You'd like to present their position to some animation mechanism in a general
 * way.  With boxes, you could do it like this:
 *
 * ```java
 * SpaceShip ship = ...
 * Box<Point2D> shipPos = Box.from(
 *     () -> ship.p,
 *     value -> { ship.p = value; });
 *
 * Bullet bullet = ...
 * Box<Point2D> bulletPos = Box.from(
 *     () -> new Point2D(bullet.getX(), bullet.getY()),
 *     value -> {
 *         bullet.setX(value.x);
 *         bullet.setY(value.y);
 *     });
 * // and for testing
 * Box<Point2D> testPos = Box.of(new Point2D(0, 0));
 *
 * animationApi.animate(shipPos  ).from(0, 0).to(10, 10);
 * animationApi.animate(bulletPos).from(0, 0).to(10, 10);
 * animationApi.animate(testPos  ).from(0, 0).to(10, 10);
 * ```
 */
public interface Box<T> extends Supplier<T>, Consumer<T> {
	/** Sets the value which will later be returned by get(). */
	void set(T value);

	/**
	 * Delegates to {@link #set}.
	 *
	 * @deprecated Provided to satisfy the {@link Consumer} interface; use {@link #set} instead.
	 */
	@Deprecated
	default void accept(T value) {
		set(value);
	}

	/**
	 * Performs a set() on the result of a get().
	 *
	 * Some implementations may provide atomic semantics,
	 * but it's not required.
	 */
	default T modify(Function<? super T, ? extends T> mutator) {
		T modified = mutator.apply(get());
		set(modified);
		return modified;
	}

	/**
	 * Maps one {@code Box} to another {@code Box}, preserving any
	 * {@link #modify(Function)} concurrency guarantees of the
	 * underlying Box.
	 */
	default <R> Box<R> map(Converter<T, R> converter) {
		return new BoxImp.Mapped<>(this, converter);
	}

	/**
	 * Creates a `Box` holding the given value in a non-`volatile` field.
	 *
	 * `modify()` is just a shortcut for `set(function.apply(get()))`.
	 */
	public static <T> Box<T> of(T value) {
		return new BoxImp.Default<>(value);
	}

	/**
	 * Creates a `Box` holding the given value in a `volatile` field.
	 *
	 * `modify()` is just a shortcut for `set(function.apply(get()))`,
	 * use {@link AtomicReference} if you require atomic semantics.
	 */
	public static <T> Box<T> ofVolatile(T value) {
		return new BoxImp.Volatile<>(value);
	}

	/** Creates a `Box` from a `Supplier` and a `Consumer`. */
	public static <T> Box<T> from(Supplier<T> getter, Consumer<T> setter) {
		Objects.requireNonNull(getter);
		Objects.requireNonNull(setter);
		return new Box<T>() {
			@Override
			public T get() {
				return Objects.requireNonNull(getter.get());
			}

			@Override
			public void set(T value) {
				setter.accept(Objects.requireNonNull(value));
			}

			@Override
			public String toString() {
				return "Box.from[" + get() + "]";
			}
		};
	}

	/** A {@link Box} which allows nulls. */
	public interface Nullable<T> extends Supplier<T>, Consumer<T> {
		/** Sets the value which will later be returned by get(). */
		void set(@javax.annotation.Nullable T value);

		/**
		 * Delegates to set().
		 *
		 * @deprecated Provided to satisfy the {@code Function} interface; use {@link #set} instead.
		 */
		@Deprecated
		default void accept(@javax.annotation.Nullable T value) {
			set(value);
		}

		/** Shortcut for doing a set() on the result of a get(). */
		default T modify(Function<? super T, ? extends T> mutator) {
			T modified = mutator.apply(get());
			set(modified);
			return modified;
		}

		/**
		 * Maps one {@code Box.Nullable} to another {@code Box.Nullable}, preserving any
		 * {@link #modify(Function)} concurrency guarantees of the
		 * underlying Box.
		 */
		default <R> Nullable<R> map(ConverterNullable<T, R> converter) {
			return new BoxImp.Nullable.Mapped<>(this, converter);
		}

		/**
		 * Creates a `Box` holding the given value in a `volatile` field.
		 *
		 * `modify()` is just a shortcut for `set(function.apply(get()))`,
		 * use {@link AtomicReference} if you require atomic semantics.
		 */
		public static <T> Nullable<T> ofVolatile(@javax.annotation.Nullable T init) {
			return new BoxImp.Nullable.Volatile<>(init);
		}

		/** Creates a `Box.Nullable` holding the null value in a `volatile` field. */
		public static <T> Nullable<T> ofVolatileNull() {
			return ofVolatile(null);
		}

		/** Creates a `Box.Nullable` holding the given possibly-null value in a non-`volatile` field. */
		public static <T> Nullable<T> of(@javax.annotation.Nullable T init) {
			return new BoxImp.Nullable.Default<>(init);
		}

		/** Creates a `Box.Nullable` holding null value in a non-`volatile` field. */
		public static <T> Nullable<T> ofNull() {
			return of(null);
		}

		/** Creates a `Box.Nullable` from a `Supplier` and a `Consumer`. */
		public static <T> Nullable<T> from(Supplier<T> getter, Consumer<T> setter) {
			Objects.requireNonNull(getter);
			Objects.requireNonNull(setter);
			return new Nullable<T>() {
				@Override
				public T get() {
					return getter.get();
				}

				@Override
				public void set(T value) {
					setter.accept(value);
				}

				@Override
				public String toString() {
					return "Box.Nullable.from[" + get() + "]";
				}
			};
		}
	}

	/** A {@link Box} for primitive doubles. */
	public interface Dbl extends DoubleSupplier, DoubleConsumer, Box<Double> {
		/** Sets the value which will later be returned by get(). */
		void set(double value);

		@Override
		double getAsDouble();

		/**
		 * Delegates to {@link #getAsDouble()}.
		 *
		 * @deprecated Provided to satisfy {@code Box<Double>}; use {@link #getAsDouble()} instead.
		 * */
		@Override
		@Deprecated
		default Double get() {
			return getAsDouble();
		}

		/**
		 * Delegates to {@link #set(double)}.
		 *
		 * @deprecated Provided to satisfy {@code Box<Double>}; use {@link #set(double)} instead.
		 */
		@Override
		@Deprecated
		default void set(Double value) {
			set(value.doubleValue());
		}

		/**
		 * Delegates to {@link #set(double)}.
		 *
		 * @deprecated Provided to satisfy the {@link DoubleConsumer}; use {@link #set(double)} instead.
		 */
		@Deprecated
		@Override
		default void accept(double value) {
			set(value);
		}

		/** Creates a `Box.Dbl` holding the given value in a non-`volatile` field. */
		public static Dbl of(double value) {
			return new Box.Dbl() {
				private double obj = value;

				@Override
				public double getAsDouble() {
					return obj;
				}

				@Override
				public void set(double obj) {
					this.obj = obj;
				}

				@Override
				public String toString() {
					return "Box.Dbl.of[" + getAsDouble() + "]";
				}
			};
		}

		/** Creates a `Box.Dbl` from a `DoubleSupplier` and a `DoubleConsumer`. */
		public static Dbl from(DoubleSupplier getter, DoubleConsumer setter) {
			return new Dbl() {
				@Override
				public double getAsDouble() {
					return getter.getAsDouble();
				}

				@Override
				public void set(double value) {
					setter.accept(value);
				}

				@Override
				public String toString() {
					return "Box.Dbl.from[" + getAsDouble() + "]";
				}
			};
		}
	}

	/** A {@link Box} for primitive ints. */
	public interface Int extends IntSupplier, IntConsumer, Box<Integer> {
		/** Sets the value which will later be returned by {@link #getAsInt()}. */
		void set(int value);

		@Override
		int getAsInt();

		/**
		 * Delegates to {@link #getAsInt()}.
		 *
		 * @deprecated Provided to satisfy {@code Box<Integer>}; use {@link #getAsInt()} instead.
		 * */
		@Override
		@Deprecated
		default Integer get() {
			return getAsInt();
		}

		/**
		 * Delegates to {@link #set(int)}.
		 *
		 * @deprecated Provided to satisfy {@code Box<Integer>}; use {@link #set(int)} instead.
		 */
		@Override
		@Deprecated
		default void set(Integer value) {
			set(value.intValue());
		}

		/**
		 * Delegates to {@link #set}.
		 *
		 * @deprecated Provided to satisfy the {@link IntConsumer} interface; use {@link #set(int)} instead.
		 */
		@Deprecated
		@Override
		default void accept(int value) {
			set(value);
		}

		/** Creates a `Box.Int` holding the given value in a non-`volatile` field. */
		public static Int of(int value) {
			return new Int() {
				private int obj = value;

				@Override
				public int getAsInt() {
					return obj;
				}

				@Override
				public void set(int obj) {
					this.obj = obj;
				}

				@Override
				public String toString() {
					return "Box.Int.of[" + getAsInt() + "]";
				}
			};
		}

		/** Creates a `Box.Int` from a `IntSupplier` and a `IntConsumer`. */
		public static Int from(IntSupplier getter, IntConsumer setter) {
			return new Int() {
				@Override
				public int getAsInt() {
					return getter.getAsInt();
				}

				@Override
				public void set(int value) {
					setter.accept(value);
				}

				@Override
				public String toString() {
					return "Box.Int.from[" + getAsInt() + "]";
				}
			};
		}
	}

	/** A {@link Box} for primitive longs. */
	public interface Lng extends LongSupplier, LongConsumer, Box<Long> {
		/** Sets the value which will later be returned by {@link #getAsLong()}. */
		void set(long value);

		@Override
		long getAsLong();

		/**
		 * Auto-boxed getter.
		 *
		 * @deprecated Provided to satisfy {@code Box<Long>} interface; use {@link #getAsLong()} instead.
		 * */
		@Override
		@Deprecated
		default Long get() {
			return getAsLong();
		}

		/**
		 * Delegates to {@link #set(long)}.
		 *
		 * @deprecated Provided to satisfy {@code Box<Long>} interface; use {@link #set(long)} instead.
		 */
		@Override
		@Deprecated
		default void set(Long value) {
			set(value.longValue());
		}

		/**
		 * Delegates to {@link #set(long)}.
		 *
		 * @deprecated Provided to satisfy {@link LongConsumer} interface; use {@link #set(long)} instead.
		 */
		@Deprecated
		@Override
		default void accept(long value) {
			set(value);
		}

		/** Creates a `Box.Long` holding the given value in a non-`volatile` field. */
		public static Lng of(long value) {
			return new Lng() {
				private long obj = value;

				@Override
				public long getAsLong() {
					return obj;
				}

				@Override
				public void set(long obj) {
					this.obj = obj;
				}

				@Override
				public String toString() {
					return "Box.Lng.of[" + getAsLong() + "]";
				}
			};
		}

		/** Creates a `Box.Long` from a `LongSupplier` and a `LongConsumer`. */
		public static Lng from(LongSupplier getter, LongConsumer setter) {
			return new Lng() {
				@Override
				public long getAsLong() {
					return getter.getAsLong();
				}

				@Override
				public void set(long value) {
					setter.accept(value);
				}

				@Override
				public String toString() {
					return "Box.Lng.from[" + getAsLong() + "]";
				}
			};
		}
	}
}
