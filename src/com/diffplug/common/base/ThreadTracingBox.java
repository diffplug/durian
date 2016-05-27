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

import java.util.function.Predicate;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class ThreadTracingBox<T> implements Box<T> {
	@javax.annotation.Nullable
	volatile Thread lastSet;

	final Box<T> delegate;

	public ThreadTracingBox(Box<T> delegate) {
		this.delegate = delegate;
	}

	@Override
	public T get() {
		return delegate.get();
	}

	@Override
	public void set(T value) {
		Thread thread = Thread.currentThread();
		if (lastSet == null) {
			lastSet = thread;
		} else if (lastSet != thread) {
			Errors.log().accept(new InconsistentThreadCallException(lastSet, thread));
		}
		delegate.set(value);
	}

	@Override
	public String toString() {
		return delegate.toString();
	}

	static class InconsistentThreadCallException extends RuntimeException {
		private static final long serialVersionUID = -7946660913539412568L;

		final Thread expected, actual;

		public InconsistentThreadCallException(Thread expected, Thread actual) {
			this.expected = expected;
			this.actual = actual;
		}

		@Override
		public String getMessage() {
			return "Expected thread " + expected + " but was " + actual;
		}
	}

	static Policy policy;

	public static <T> Box<T> wrap(Box<T> toWrap) {
		if (policy == null) {
			policy = DurianPlugins.get(Policy.class, Policy.NONE);
		}
		if (policy.trace(toWrap)) {
			return new ThreadTracingBox<>(toWrap);
		} else {
			return toWrap;
		}
	}

	/**
	 * Plugin which gets notified of every call to {@link Rx#subscribe Rx.subscribe}, allowing various kinds of tracing.
	 * <p>
	 * By default, no tracing is done. To enable tracing, do one of the following:
	 * <ul>
	 * <li>Execute this at the very beginning of your application: {@code DurianPlugins.set(RxTracingPolicy.class, new MyTracingPolicy());}</li>
	 * <li>Set this system property: {@code durian.plugins.com.diffplug.common.rx.RxTracingPolicy=fully.qualified.name.to.MyTracingPolicy}</li>
	 * </ul>
	 * {@link LogSubscriptionTrace} is a useful tracing policy for debugging errors within callbacks.
	 * @see DurianPlugins
	 */
	public interface Policy {
		/**
		 * Given an observable, and an {@link Rx} which is about to be subscribed to this observable,
		 * return a (possibly instrumented) {@code Rx}.
		 * 
		 * @param observable The {@link IObservable}, {@link Observable}, or {@link ListenableFuture} which is about to be subscribed to.
		 * @param listener The {@link Rx} which is about to be subscribed.
		 * @return An {@link Rx} which may (or may not) be instrumented.  To ensure that the program's behavior
		 * is not changed, implementors should ensure that all method calls are delegated unchanged to the original listener eventually.
		 */
		boolean trace(Box<?> toTrace);

		/** An {@code RxTracingPolicy} which performs no tracing, and has very low overhead. */
		public static final Policy NONE = new Policy() {
			@Override
			public boolean trace(Box<?> toTrace) {
				return false;
			}
		};

		/** An {@code RxTracingPolicy} which performs no tracing, and has very low overhead. */
		public static final Policy ALL = new Policy() {
			@Override
			public boolean trace(Box<?> toTrace) {
				return true;
			}
		};

		public static class Some implements Policy {
			/** The Predicate which determines which Boxes will have their `set()` methods logged. */
			@SuppressFBWarnings(value = "MS_SHOULD_BE_FINAL", justification = "This is public on purpose, and is only functional in a debug mode.")
			public static Predicate<Box<?>> shouldTrace = Predicates.alwaysTrue();

			@Override
			public boolean trace(Box<?> toTrace) {
				return shouldTrace.test(toTrace);
			}
		}
	}
}
