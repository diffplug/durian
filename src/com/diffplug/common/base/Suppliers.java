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
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Helper functions for manipulating {@link Supplier}, copied from Guava.
 * <p>
 * The function signatures below are identical to Google's Guava 18.0, except that guava's functional
 * interfaces have been swapped with Java 8's. It is tested against the same test suite
 * as Google Guava to ensure functional compatibility.
 * <p>
 * Most of the implementation has been replaced with lambdas, which means that the following
 * functionality has been removed: equals(), hashCode(), toString(), GWT, and serialization.
 * <p>
 * Lambdas don't support these methods, and there isn't much reason why they should, so we
 * removed them in Durian.
 */
public final class Suppliers {
	private Suppliers() {}

	/**
	 * Returns a new supplier which is the composition of the provided function
	 * and supplier. In other words, the new supplier's value will be computed by
	 * retrieving the value from {@code supplier}, and then applying
	 * {@code function} to that value. Note that the resulting supplier will not
	 * call {@code supplier} or invoke {@code function} until it is called.
	 */
	public static <F, T> Supplier<T> compose(Function<? super F, T> function, Supplier<F> supplier) {
		return () -> function.apply(supplier.get());
	}

	/**
	 * Returns a supplier which caches the instance retrieved during the first
	 * call to {@code get()} and returns that value on subsequent calls to
	 * {@code get()}. See:
	 * <a href="http://en.wikipedia.org/wiki/Memoization">memoization</a>
	 *
	 * <p>The returned supplier is thread-safe.
	 *
	 * <p>If {@code delegate} is an instance created by an earlier call to {@code
	 * memoize}, it is returned directly.
	 */
	public static <T> Supplier<T> memoize(Supplier<T> delegate) {
		return (delegate instanceof MemoizingSupplier) ? delegate : new MemoizingSupplier<>(Objects.requireNonNull(delegate));
	}

	@SuppressFBWarnings(value = "IS2_INCONSISTENT_SYNC", justification = "It's a lightweight mechanism which ensures that delegate only gets called once.")
	static class MemoizingSupplier<T> implements Supplier<T> {
		final Supplier<T> delegate;
		volatile boolean initialized;
		// "value" does not need to be volatile; visibility piggy-backs
		// on volatile read of "initialized".
		T value;

		MemoizingSupplier(Supplier<T> delegate) {
			this.delegate = delegate;
		}

		@Override
		public T get() {
			// A 2-field variant of Double Checked Locking.
			if (!initialized) {
				synchronized (this) {
					if (!initialized) {
						T t = delegate.get();
						value = t;
						initialized = true;
						return t;
					}
				}
			}
			return value;
		}

		@Override
		public String toString() {
			return "Suppliers.memoize(" + delegate + ")";
		}
	}

	/**
	 * Returns a supplier that caches the instance supplied by the delegate and
	 * removes the cached value after the specified time has passed. Subsequent
	 * calls to {@code get()} return the cached value if the expiration time has
	 * not passed. After the expiration time, a new value is retrieved, cached,
	 * and returned. See:
	 * <a href="http://en.wikipedia.org/wiki/Memoization">memoization</a>
	 *
	 * <p>The returned supplier is thread-safe.
	 *
	 * @param duration the length of time after a value is created that it
	 *     should stop being returned by subsequent {@code get()} calls
	 * @param unit the unit that {@code duration} is expressed in
	 * @throws IllegalArgumentException if {@code duration} is not positive
	 * @since 2.0
	 */
	public static <T> Supplier<T> memoizeWithExpiration(Supplier<T> delegate, long duration, TimeUnit unit) {
		return new ExpiringMemoizingSupplier<>(delegate, duration, unit);
	}

	static class ExpiringMemoizingSupplier<T> implements Supplier<T> {
		final Supplier<T> delegate;
		final long durationNanos;
		volatile T value;
		// The special value 0 means "not yet initialized".
		volatile long expirationNanos;

		ExpiringMemoizingSupplier(Supplier<T> delegate, long duration, TimeUnit unit) {
			this.delegate = Objects.requireNonNull(delegate);
			this.durationNanos = unit.toNanos(duration);
			if (!(duration > 0)) {
				throw new IllegalArgumentException();
			}
		}

		@Override
		public T get() {
			// Another variant of Double Checked Locking.
			//
			// We use two volatile reads.  We could reduce this to one by
			// putting our fields into a holder class, but (at least on x86)
			// the extra memory consumption and indirection are more
			// expensive than the extra volatile reads.
			long nanos = expirationNanos;
			long now = System.nanoTime();
			if (nanos == 0 || now - nanos >= 0) {
				synchronized (this) {
					if (nanos == expirationNanos) { // recheck for lost race
						T t = delegate.get();
						value = t;
						nanos = now + durationNanos;
						// In the very unlikely event that nanos is 0, set it to 1;
						// no one will notice 1 ns of tardiness.
						expirationNanos = (nanos == 0) ? 1 : nanos;
						return t;
					}
				}
			}
			return value;
		}

		@Override
		public String toString() {
			// This is a little strange if the unit the user provided was not NANOS,
			// but we don't want to store the unit just for toString
			return "Suppliers.memoizeWithExpiration(" + delegate + ", " + durationNanos + ", NANOS)";
		}
	}

	/**
	 * Returns a supplier that always supplies {@code instance}.
	 */
	public static <T> Supplier<T> ofInstance(@Nullable T instance) {
		return () -> instance;
	}

	/**
	 * Returns a supplier whose {@code get()} method synchronizes on
	 * {@code delegate} before calling it, making it thread-safe.
	 */
	public static <T> Supplier<T> synchronizedSupplier(Supplier<T> delegate) {
		return new ThreadSafeSupplier<>(Objects.requireNonNull(delegate));
	}

	private static class ThreadSafeSupplier<T> implements Supplier<T> {
		final Supplier<T> delegate;

		ThreadSafeSupplier(Supplier<T> delegate) {
			this.delegate = delegate;
		}

		@Override
		public T get() {
			synchronized (delegate) {
				return delegate.get();
			}
		}

		@Override
		public String toString() {
			return "Suppliers.synchronizedSupplier(" + delegate + ")";
		}
	}

	/**
	 * Returns a function that accepts a supplier and returns the result of
	 * invoking {@link Supplier#get} on that supplier.
	 *
	 * @since 8.0
	 */
	public static <T> Function<Supplier<T>, T> supplierFunction() {
		return Supplier::get;
	}
}
