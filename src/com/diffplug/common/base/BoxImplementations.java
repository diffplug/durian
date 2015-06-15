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

/**
 * Trivial implementations of the various {@link Box} classes. Useful for overriding
 * them to create custom behaviors on get() or set().
 */
public class BoxImplementations {
	/** A simple implementation of Box. */
	public static class BoxImp<T> implements Box<T> {
		/** The (possibly-null) object being held. */
		protected volatile T obj;

		protected BoxImp(T init) {
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

	/** A simple implementation of Box.Nullable. */
	public static class NullableImp<T> implements Box.Nullable<T> {
		/** The (possibly-null) object being held. */
		protected volatile T obj;

		protected NullableImp(T init) {
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

	/** A simple implementation of Box.Double. */
	public static class DoubleImp implements Box.Double {
		/** The (possibly-null) object being held. */
		protected volatile double obj;

		protected DoubleImp(double init) {
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

	/** A simple implementation of Box.Int. */
	public static class IntImp implements Box.Int {
		/** The (possibly-null) object being held. */
		protected volatile int obj;

		protected IntImp(int init) {
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
}
