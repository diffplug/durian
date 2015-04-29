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

/**
 * Simple class for creating boxed variables. Useful when you
 * need to make a mutable variable available to a closure.
 */
public class Box<T> implements GetterSetter<T> {
	/** The (possibly-null) object being held. */
	private volatile T obj;

	protected Box(T init) {
		this.accept(init);
	}

	/** Creates a Holder of the given object. */
	public static <T> Box<T> of(T init) {
		return new Box<T>(init);
	}

	/** Creates an empty Holder object. */
	public static <T> Box<T> empty() {
		return new Box<T>(null);
	}

	@Override
	public T get() {
		return obj;
	}

	@Override
	public void accept(T obj) {
		this.obj = obj;
	}

	@Override
	public String toString() {
		return get() == null ? "(null)" : get().toString();
	}

	/** A Box<T> which guarantees to never be null (by disallowing null values in its setter). */
	public static class NonNull<T> extends Box<T> {
		public static <T> NonNull<T> of(T value) {
			return new NonNull<T>(value);
		}

		protected NonNull(T value) {
			super(Objects.requireNonNull(value));
		}

		@Override
		public void accept(T obj) {
			super.accept(Objects.requireNonNull(obj));
		}
	}
}
