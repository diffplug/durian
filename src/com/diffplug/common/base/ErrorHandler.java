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

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/** 
 * ErrorHandler makes it easy to create implementations of the standard
 * functional interfaces (which don't allow checked exceptions).
 * 
 * Even for cases where you aren't required to stuff some code into a
 * functional interface, ErrorHandler is useful as a concise way to
 * specify how errors will be handled. 
 */
public class ErrorHandler {
	private final Consumer<Throwable> handler;

	/** Creates an OnError which will pass any errors to this handler. */
	public static ErrorHandler onError(Consumer<Throwable> handler) {
		return new ErrorHandler(handler);
	}

	protected ErrorHandler(Consumer<Throwable> error) {
		this.handler = error;
	}

	/** Suppresses errors entirely. */
	public static ErrorHandler suppress() {
		return suppress;
	}

	private static final ErrorHandler suppress = new ErrorHandler(obj -> {});

	/** Rethrows any exceptions as runtime exceptions. */
	public static ErrorHandler rethrow() {
		return rethrow;
	}

	private static final ErrorHandler rethrow = new ErrorHandler(error -> {
		throw asRuntime(error);
	});

	/**
	 * Logs any exceptions.
	 * 
	 * By default, log() just calls Exception.printStackTrace(). To modify this behavior
	 * in your application, call DurianPlugins.registerErrorHandlerLog() on startup.
	 */
	public static ErrorHandler log() {
		if (log == null) {
			// There is an acceptable race condition here - log might get set multiple times.
			// This would happen if multiple threads called log() at the same time
			// during initialization, and this is likely to actually happen in practice.
			// BUT, there are no adverse symptoms (unless users are relying on identity
			// equality of this return value, which there's no reason to do) because
			// DurianPlugins guarantees that its methods will have the exact same
			// return value for the duration of the library's runtime existence.
			//
			// It is important for this method to be fast, so it's better to accept
			// that log() might return different ErrorHandler instances which are wrapping
			// the same actual Consumer<Throwable>.
			log = new ErrorHandler(DurianPlugins.getInstance().getErrorHandlerLog());
		}
		return log;
	}

	private static ErrorHandler log;

	/**
	 * Opens a dialog to notify the user of any exceptions.
	 * 
	 * By default, log() just calls Exception.printStackTrace(). To modify this behavior
	 * in your application, call DurianPlugins.registerErrorHandlerDialog() on startup.
	 */
	public static ErrorHandler dialog() {
		if (dialog == null) {
			// There is an acceptable race condition here.  See ErrorHandler.log() for details.
			dialog = new ErrorHandler(DurianPlugins.getInstance().getErrorHandlerDialog());
		}
		return dialog;
	}

	private static ErrorHandler dialog;

	/** Passes the given error to be handled by the ErrorHandler. */
	public final void handle(Throwable error) {
		handler.accept(error);
	}

	/** Attempts to run the given runnable. */
	public final void run(Throwing.Runnable runnable) {
		wrap(runnable).run();
	}

	/** Attempts to call the given supplier, returns onFailure if there is a failure. */
	public final <T> T getWithDefault(Throwing.Supplier<T> supplier, T onFailure) {
		return wrapWithDefault(supplier, onFailure).get();
	}

	/**
	 * tryGetWithDefault() doesn't make sense for ErrorHandler.rethrow(), because the default
	 * value will never be returned. This we provide this one-off method for 
	 */
	public static <T> T rethrowGet(Throwing.Supplier<T> supplier) {
		try {
			return supplier.get();
		} catch (Throwable e) {
			throw asRuntime(e);
		}
	}

	/** Returns a Runnable whose exceptions are handled by this ErrorHandler. */
	public final Runnable wrap(Throwing.Runnable runnable) {
		return () -> {
			try {
				runnable.run();
			} catch (Throwable e) {
				handler.accept(e);
			}
		};
	}

	/** Returns a Consumer whose exceptions are handled by this ErrorHandler. */
	public final <T> Consumer<T> wrap(Throwing.Consumer<T> consumer) {
		return val -> {
			try {
				consumer.accept(val);
			} catch (Throwable e) {
				handler.accept(e);
			}
		};
	}

	/** Attempts to call the given function, and returns the given value on failure. */
	public final <T, R> Function<T, R> wrapWithDefault(Throwing.Function<T, R> function, R onFailure) {
		return input -> {
			try {
				return function.apply(input);
			} catch (Throwable e) {
				handler.accept(e);
				return onFailure;
			}
		};
	}

	/** Attempts to call the given supplier, and returns the given value on failure. */
	public final <T> Supplier<T> wrapWithDefault(Throwing.Supplier<T> supplier, T onFailure) {
		return () -> {
			try {
				return supplier.get();
			} catch (Throwable e) {
				handler.accept(e);
				return onFailure;
			}
		};
	}

	/** Converts the given exception to a RuntimeException, with a minimum of new exceptions to obscure the cause. */
	public static RuntimeException asRuntime(Throwable e) {
		if (e instanceof RuntimeException) {
			return (RuntimeException) e;
		} else {
			return new RuntimeException(e);
		}
	}
}
