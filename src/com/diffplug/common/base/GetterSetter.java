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
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Provides get/set access to some field.
 * 
 * It's tempting for this to implement Consumer<T>, but it turns
 * out to be very strange to use with the accept() rather than get().
 * 
 * And in our whole codebase, it turns out there's no where that a GetterSetter
 * is used directly as either a Consumer, 
 */
public interface GetterSetter<T> extends Supplier<T> {
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
}
