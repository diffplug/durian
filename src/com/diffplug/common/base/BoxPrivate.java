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

class BoxPrivate {
	/** A simple implementation of Nullable. */
	static class BoxImp<T> implements Box<T> {
		/** The (possibly-null) object being held. */
		private volatile T obj;

		BoxImp(T init) {
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

	/** A simple implementation of Nullable. */
	static class NullableImp<T> implements Box.Nullable<T> {
		/** The (possibly-null) object being held. */
		private volatile T obj;

		NullableImp(T init) {
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

	/** A simple implementation of Nullable. */
	static class DoubleImp implements Box.Double {
		/** The (possibly-null) object being held. */
		private volatile double obj;

		DoubleImp(double init) {
			this.set(init);
		}

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
			return "Box.Double[" + obj + "]";
		}
	}

	/** A simple implementation of Nullable. */
	static class IntImp implements Box.Int {
		/** The (possibly-null) object being held. */
		private volatile int obj;

		IntImp(int init) {
			this.set(init);
		}

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
			return "Box.Double[" + obj + "]";
		}
	}
}
