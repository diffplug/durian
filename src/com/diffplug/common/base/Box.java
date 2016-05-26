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

import java.util.Objects;
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
 * Provides get/set access to a mutable non-null value.
 */
public interface Box<T> extends Supplier<T>, Consumer<T> {
	/** Sets the value which will later be returned by get(). */
	void set(T value);

	/**
	 * Delegates to set().
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
	 * {@link #modify(Function)} guarantees of the underlying Box.
	 */
	default <R> Box<R> map(Converter<T, R> converter) {
		return new Mapped<>(this, converter);
	}

	public static final class Mapped<T, R> implements Box<R> {
		private final Box<T> delegate;
		private final Converter<T, R> converter;

		public Mapped(Box<T> delegate,
				Converter<T, R> converter) {
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

		/** Shortcut for doing a set() on the result of a get(). */
		@Override
		public R modify(Function<? super R, ? extends R> mutator) {
			Box.Nullable<R> result = Box.Nullable.ofNull();
			delegate.modify(input -> {
				R unmappedResult = mutator.apply(converter.convertNonNull(input));
				result.set(unmappedResult);
				return converter.revertNonNull(unmappedResult);
			});
			return result.get();
		}

		@Override
		public String toString() {
			return "[" + delegate + " mapped to " + get() + " by " + converter + "]";
		}
	}

	/**
	 * Creates a `Box` holding the given value in a `volatile` field.
	 *
	 * Every call to {@link #set(Object)} confirms that the argument
	 * is actually non-null, and the value is stored in a volatile variable.
	 */
	public static <T> Box<T> of(T value) {
		return new Default<>(value);
	}

	static final class Default<T> implements Box<T> {
		private volatile T obj;

		private Default(T init) {
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

	/**
	 * Creates a `Box` holding the given value in a non-`volatile` field.
	 *
	 * The value is stored in standard non-volatile
	 * field, and non-null-ness is not checked on
	 * every call to set.
	 */
	public static <T> Box<T> ofFast(T value) {
		return new DefaultFast<>(value);
	}

	static final class DefaultFast<T> implements Box<T> {
		private T obj;

		private DefaultFast(T init) {
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
			return "Box.ofFast[" + get() + "]";
		}
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

	/** Provides get/set access to a mutable nullable value. */
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
		 * Maps one {@code Box} to another {@code Box}, preserving any
		 * {@link #modify(Function)} guarantees of the underlying Box.
		 */
		default <R> Nullable<R> map(ConverterNullable<T, R> converter) {
			return new Nullable.Mapped<>(this, converter);
		}

		static final class Mapped<T, R> implements Nullable<R> {
			private final Nullable<T> delegate;
			private final ConverterNullable<T, R> converter;

			public Mapped(Nullable<T> delegate,
					ConverterNullable<T, R> converter) {
				this.delegate = delegate;
				this.converter = converter;
			}

			@Override
			public R get() {
				return converter.convert(delegate.get());
			}

			@Override
			public void set(R value) {
				delegate.set(converter.revert(value));
			}

			/** Shortcut for doing a set() on the result of a get(). */
			@Override
			public R modify(Function<? super R, ? extends R> mutator) {
				Box.Nullable<R> result = Box.Nullable.ofNull();
				delegate.modify(input -> {
					R unmappedResult = mutator.apply(converter.convert(input));
					result.set(unmappedResult);
					return converter.revert(unmappedResult);
				});
				return result.get();
			}

			@Override
			public String toString() {
				return "[" + delegate + " mapped to " + get() + " by " + converter + "]";
			}
		}

		/** Creates a `Box.Nullable` holding the given possibly-null value in a `volatile` field. */
		public static <T> Nullable<T> of(@javax.annotation.Nullable T init) {
			return new Default<>(init);
		}

		/** Creates a `Box.Nullable` holding null in a `volatile` field. */
		public static <T> Nullable<T> ofNull() {
			return new Default<>(null);
		}

		static class Default<T> implements Box.Nullable<T> {
			private volatile T obj;

			private Default(T init) {
				this.obj = init;
			}

			@Override
			public T get() {
				return obj;
			}

			@Override
			public void set(@javax.annotation.Nullable T obj) {
				this.obj = obj;
			}

			@Override
			public String toString() {
				return "Box.Nullable.of[" + get() + "]";
			}
		}

		/** Creates a `Box.Nullable` holding the given possibly-null value in a non-`volatile` field. */
		public static <T> Nullable<T> ofFast(@javax.annotation.Nullable T init) {
			return new DefaultFast<>(init);
		}

		/** Creates a `Box.Nullable` holding null in a non-`volatile` field. */
		public static <T> Nullable<T> ofFastNull() {
			return new DefaultFast<>(null);
		}

		static class DefaultFast<T> implements Box.Nullable<T> {
			private T obj;

			private DefaultFast(T init) {
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
				return "Box.Nullable.ofFast[" + get() + "]";
			}
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

	/** A `Box` for primitive doubles. */
	public interface Dbl extends DoubleSupplier, DoubleConsumer {
		/** Sets the value which will later be returned by get(). */
		void set(double value);

		/** Returns the boxed value. */
		double get();

		/**
		 * Delegates to {@link #set}.
		 *
		 * @deprecated Provided to satisfy the {@link DoubleConsumer} interface; use {@link #set} instead.
		 */
		@Deprecated
		@Override
		default void accept(double value) {
			set(value);
		}

		/**
		 * Delegates to {@link #get}.
		 *
		 * @deprecated Provided to satisfy the {@link DoubleSupplier} interface; use {@link #get} instead.
		 */
		@Deprecated
		@Override
		default double getAsDouble() {
			return get();
		}

		/** Creates a `Box.Dbl` holding the given value in a `volatile` field. */
		public static Dbl of(double value) {
			return new Default(value);
		}

		static class Default implements Box.Dbl {
			private volatile double obj;

			private Default(double init) {
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
				return "Box.Dbl.of[" + get() + "]";
			}
		}

		/** Creates a `Box.Dbl` holding the given value in a non-`volatile` field. */
		public static Dbl ofFast(double value) {
			return new DefaultFast(value);
		}

		static class DefaultFast implements Box.Dbl {
			private double obj;

			private DefaultFast(double init) {
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
				return "Box.Dbl.ofFast[" + get() + "]";
			}
		}

		/** Creates a `Box.Dbl` from a `DoubleSupplier` and a `DoubleConsumer`. */
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

				@Override
				public String toString() {
					return "Box.Dbl.from[" + get() + "]";
				}
			};
		}
	}

	/** A `Box` for primitive ints. */
	public interface Int extends IntSupplier, IntConsumer {
		/** Sets the value which will later be returned by get(). */
		void set(int value);

		/** Returns the boxed value. */
		int get();

		/**
		 * Delegates to {@link #set}.
		 *
		 * @deprecated Provided to satisfy the {@link IntConsumer} interface; use {@link #set} instead.
		 */
		@Deprecated
		@Override
		default void accept(int value) {
			set(value);
		}

		/**
		 * Delegates to {@link #get}.
		 *
		 * @deprecated Provided to satisfy the {@link IntSupplier} interface; use {@link #get} instead.
		 */
		@Deprecated
		@Override
		default int getAsInt() {
			return get();
		}

		/** Creates a `Box.Int` holding the given value in a `volatile` field. */
		public static Int of(int value) {
			return new Default(value);
		}

		static class Default implements Box.Int {
			private volatile int obj;

			private Default(int init) {
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
				return "Box.Int.of[" + get() + "]";
			}
		}

		/** Creates a `Box.Int` holding the given value in a non-`volatile` field. */
		public static Int ofFast(int value) {
			return new DefaultFast(value);
		}

		static class DefaultFast implements Box.Int {
			private int obj;

			private DefaultFast(int init) {
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
				return "Box.Int.ofFast[" + get() + "]";
			}
		}

		/** Creates a `Box.Int` from a `IntSupplier` and a `IntConsumer`. */
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

				@Override
				public String toString() {
					return "Box.Int.from[" + get() + "]";
				}
			};
		}
	}

	/** A `Box` for primitive longs. */
	public interface Long extends LongSupplier, LongConsumer {
		/** Sets the value which will later be returned by get(). */
		void set(long value);

		/** Returns the boxed value. */
		long get();

		/**
		 * Delegates to {@link #set}.
		 *
		 * @deprecated Provided to satisfy the {@link LongConsumer} interface; use {@link #set} instead.
		 */
		@Deprecated
		@Override
		default void accept(long value) {
			set(value);
		}

		/**
		 * Delegates to {@link #get}.
		 *
		 * @deprecated Provided to satisfy the {@link LongSupplier} interface; use {@link #get} instead.
		 */
		@Deprecated
		@Override
		default long getAsLong() {
			return get();
		}

		/** Creates a `Box.Long` holding the given value in a `volatile` field. */
		public static Long of(long value) {
			return new Default(value);
		}

		static class Default implements Box.Long {
			private volatile long obj;

			private Default(long init) {
				this.obj = init;
			}

			@Override
			public long get() {
				return obj;
			}

			@Override
			public void set(long obj) {
				this.obj = obj;
			}

			@Override
			public String toString() {
				return "Box.Long.of[" + get() + "]";
			}
		}

		/** Creates a `Box.Long` holding the given value in a non-`volatile` field. */
		public static Long ofFast(long value) {
			return new DefaultFast(value);
		}

		static class DefaultFast implements Box.Long {
			private long obj;

			private DefaultFast(long init) {
				this.obj = init;
			}

			@Override
			public long get() {
				return obj;
			}

			@Override
			public void set(long obj) {
				this.obj = obj;
			}

			@Override
			public String toString() {
				return "Box.Long.ofFast[" + get() + "]";
			}
		}

		/** Creates a `Box.Long` from a `LongSupplier` and a `LongConsumer`. */
		public static Long from(LongSupplier getter, LongConsumer setter) {
			return new Long() {
				@Override
				public long get() {
					return getter.getAsLong();
				}

				@Override
				public void set(long value) {
					setter.accept(value);
				}

				@Override
				public String toString() {
					return "Box.Long.from[" + get() + "]";
				}
			};
		}
	}
}
