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

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * Provides get/set access to some field.
 * 
 * It's tempting for this to implement Consumer<T>, but it turns
 * out to be very strange to use with the accept() rather than set(T value).
 */
public interface GetterSetter<T> extends Supplier<T> {
	/** Sets the value which will later be returned by get(). */
	void set(T value);

	/** Creates a GetterSetter from a Supplier and a Consumer. */
	public static <T> GetterSetter<T> from(Supplier<T> getter, Consumer<T> setter) {
		return new GetterSetter<T>() {
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

	/** Creates a GetterSetter from an argument and two functions which operate on that argument. */
	public static <T, V> GetterSetter<T> from(V target, Function<V, T> getter, BiConsumer<V, T> setter) {
		return new GetterSetter<T>() {
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

	/** A GetterSetter for primitive doubles. */
	public interface Double extends DoubleSupplier {
		/** Sets the value which will later be returned by get(). */
		void set(double value);

		/** Creates a GetterSetter.Double from a Supplier and a Consumer. */
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

	/** A GetterSetter for primitive ints. */
	public interface Int extends IntSupplier {
		/** Sets the value which will later be returned by get(). */
		void set(int value);

		/** Creates a GetterSetter.Int from a Supplier and a Consumer. */
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
