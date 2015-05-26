/**
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

/** Provides get/set access to some value. */
public interface Box<T> extends Supplier<T>, Consumer<T> {
	/** Sets the value which will later be returned by get(). */
	void set(T value);

	/** Delegates to set(). */
	default void accept(T value) {
		set(value);
	}

	/** Creates a Box holding the given value. */
	public static <T> Box<T> of(T value) {
		return new Box.BoxImp<T>(value);
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

	/** Creates a Nullable from an argument and two functions which operate on that argument. */
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

	/** Creates a Box box holding the given value. */
	public static <T> Box<T> wrap(Nullable<T> wrapped) {
		Objects.requireNonNull(wrapped.get());
		return new Box<T>() {
			@Override
			public void set(T value) {
				wrapped.set(Objects.requireNonNull(value));
			}

			@Override
			public T get() {
				return Objects.requireNonNull(wrapped.get());
			}
		};
	}

	/** A simple implementation of Nullable. */
	static class BoxImp<T> implements Box<T> {
		/** The (possibly-null) object being held. */
		private volatile T obj;

		protected BoxImp(T init) {
			this.set(init);
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
			return "Box[" + get().toString() + "]";
		}
	}

	/** Provides get/set access to some field. */
	public interface Nullable<T> extends Supplier<T>, Consumer<T> {
		/** Sets the value which will later be returned by get(). */
		void set(T value);

		/** Delegates to set(). */
		default void accept(T value) {
			set(value);
		}

		/** Creates a Holder of the given object. */
		public static <T> Nullable<T> of(T init) {
			return new NullableImp<T>(init);
		}

		/** Creates an empty Holder object. */
		public static <T> Nullable<T> ofNull() {
			return new NullableImp<T>(null);
		}

		/** A simple implementation of Nullable. */
		static class NullableImp<T> implements Nullable<T> {
			/** The (possibly-null) object being held. */
			private volatile T obj;

			protected NullableImp(T init) {
				this.set(init);
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

	/** A Nullable for primitive doubles. */
	public interface Double extends DoubleSupplier, DoubleConsumer {
		/** Sets the value which will later be returned by get(). */
		void set(double value);

		/** Implement the DoubleConsumer interface. */
		default void accept(double value) {
			set(value);
		}

		/** Creates a Nullable.Double from a Supplier and a Consumer. */
		public static Double from(DoubleSupplier getter, DoubleConsumer setter) {
			return new Double() {
				@Override
				public double getAsDouble() {
					return getter.getAsDouble();
				}

				@Override
				public void set(double value) {
					setter.accept(value);
				}
			};
		}
	}

	/** A Nullable for primitive ints. */
	public interface Int extends IntSupplier, IntConsumer {
		/** Sets the value which will later be returned by get(). */
		void set(int value);

		/** Implement the IntConsumer interface. */
		default void accept(int value) {
			set(value);
		}

		/** Creates a Nullable.Int from a Supplier and a Consumer. */
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
			};
		}
	}
}
