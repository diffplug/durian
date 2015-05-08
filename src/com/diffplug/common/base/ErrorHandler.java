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

import java.io.PrintWriter;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/** 
 * ErrorHandler makes it easy to create implementations of the standard
 * functional interfaces (which don't allow checked exceptions).
 * 
 * Even for cases where you aren't required to stuff some code into a
 * functional interface, ErrorHandler is useful as a concise way to
 * specify how errors will be handled. 
 */
public abstract class ErrorHandler {
	/** Package-private for testing - resets all of the static member variables. */
	static void resetForTesting() {
		log = null;
		dialog = null;
	}

	protected final Consumer<Throwable> handler;

	/**
	 * Creates an ErrorHandler.Rethrowing which transforms any exceptions it receives into a RuntimeException
	 * as specified by the given function, and then throws that RuntimeException.
	 * 
	 * If that function happens to throw a RuntimeException itself, that'll work just fine, but it's ugly.
	 */
	public static Rethrowing createRethrowing(Function<Throwable, RuntimeException> transform) {
		return new Rethrowing(transform);
	}

	/**
	 * Creates an ErrorHandler.Handling which passes any exceptions it receives
	 * to the given handler.
	 * 
	 * The handler is free to throw a RuntimeException if it wants to. If it always
	 * throws a RuntimeException, then you should instead create an ErrorHandler.Rethrowing
	 * using creeateRethrowAs().
	 */
	public static Handling createHandling(Consumer<Throwable> handler) {
		return new Handling(handler);
	}

	protected ErrorHandler(Consumer<Throwable> error) {
		this.handler = error;
	}

	/** Suppresses errors entirely. */
	public static Handling suppress() {
		return suppress;
	}

	private static final Handling suppress = createHandling(Consumers.doNothing());

	/** Rethrows any exceptions as runtime exceptions. */
	public static Rethrowing rethrow() {
		return rethrow;
	}

	private static final Rethrowing rethrow = createRethrowing(ErrorHandler::asRuntime);

	/**
	 * Logs any exceptions.
	 * 
	 * By default, log() just calls Exception.printStackTrace(). To modify this behavior
	 * in your application, call DurianPlugins.registerErrorHandlerLog() on startup.
	 */
	public static Handling log() {
		if (log == null) {
			// There is an acceptable race condition here.  See ErrorHandler.suppress() for details.
			log = createHandling(DurianPlugins.get(Plugins.Log.class, Plugins::defaultLog));
		}
		return log;
	}

	private static Handling log;

	/**
	 * Opens a dialog to notify the user of any exceptions.
	 * 
	 * By default, log() just calls Exception.printStackTrace(). To modify this behavior
	 * in your application, call DurianPlugins.registerErrorHandlerDialog() on startup.
	 */
	@SuppressFBWarnings(value = "LI_LAZY_INIT_STATIC", justification = "This race condition is fine, as explained in the comment below.")
	public static Handling dialog() {
		if (dialog == null) {
			// There is an acceptable race condition here.  See ErrorHandler.suppress() for details.
			dialog = createHandling(DurianPlugins.get(Plugins.Dialog.class, Plugins::defaultDialog));
		}
		return dialog;
	}

	private static Handling dialog;

	/** Passes the given error to be handled by the ErrorHandler. */
	public final void handle(Throwable error) {
		handler.accept(error);
	}

	/** Attempts to run the given runnable. */
	public final void run(Throwing.Runnable runnable) {
		wrap(runnable).run();
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

	/**
	 * An ErrorHandler which is free to rethrow the exception, but it might not.
	 * 
	 * If we want to wrap a method with a return value, since the handler might
	 * not throw an exception, we need a default value to return.
	 */
	public static class Handling extends ErrorHandler {
		protected Handling(Consumer<Throwable> error) {
			super(error);
		}

		/** Attempts to call the given supplier, returns onFailure if there is a failure. */
		public final <T> T getWithDefault(Throwing.Supplier<T> supplier, T onFailure) {
			return wrapWithDefault(supplier, onFailure).get();
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

		/** Attempts to call the given function, and returns the given value on failure. */
		public final <T> Predicate<T> wrapWithDefault(Throwing.Predicate<T> function, boolean onFailure) {
			return input -> {
				try {
					return function.test(input);
				} catch (Throwable e) {
					handler.accept(e);
					return onFailure;
				}
			};
		}
	}

	/**
	 * An ErrorHandler which is guaranteed to always throw a RuntimeException.
	 * 
	 * If we want to wrap a method with a return value, it's pointless to specify
	 * a default value because if the wrapped method fails, a RuntimeException is
	 * guaranteed to throw.
	 */
	public static class Rethrowing extends ErrorHandler {
		private final Function<Throwable, RuntimeException> transform;

		protected Rethrowing(Function<Throwable, RuntimeException> transform) {
			super(error -> {
				throw transform.apply(error);
			});
			this.transform = transform;
		}

		/** Attempts to call the given supplier, throws some kind of RuntimeException on failure. */
		public final <T> T get(Throwing.Supplier<T> supplier) {
			return wrap(supplier).get();
		}

		/** Attempts to call the given supplier, throws some kind of RuntimeException on failure. */
		public final <T> Supplier<T> wrap(Throwing.Supplier<T> supplier) {
			return () -> {
				try {
					return supplier.get();
				} catch (Throwable e) {
					throw transform.apply(e);
				}
			};
		}

		/** Attempts to call the given function, throws some kind of RuntimeException on failure. */
		public final <T, R> Function<T, R> wrap(Throwing.Function<T, R> function) {
			return arg -> {
				try {
					return function.apply(arg);
				} catch (Throwable e) {
					throw transform.apply(e);
				}
			};
		}

		/** Attempts to call the given function, throws some kind of RuntimeException on failure. */
		public final <T> Predicate<T> wrap(Throwing.Predicate<T> predicate) {
			return arg -> {
				try {
					return predicate.test(arg);
				} catch (Throwable e) {
					throw transform.apply(e); // 1 855 548 2505
				}
			};
		}
	}

	/** Converts the given exception to a RuntimeException, with a minimum of new exceptions to obscure the cause. */
	public static RuntimeException asRuntime(Throwable e) {
		if (e instanceof RuntimeException) {
			return (RuntimeException) e;
		} else {
			return new RuntimeException(e);
		}
	}

	/**
	 * Namespace for the plugins which ErrorHandler supports. Call
	 * DurianPlugins.register(Plugins.Log.class, logImplementation)
	 * if you'd like to change the default behavior.
	 */
	public interface Plugins {
		/** ErrorHandler.log(). */
		public interface Log extends Consumer<Throwable> {}

		/** ErrorHandler.dialog(). */
		public interface Dialog extends Consumer<Throwable> {}

		/** Default behavior of ErrorHandler.log() is Throwable.printStackTrace(). */
		static void defaultLog(Throwable error) {
			error.printStackTrace();
		}

		/** Default behavior of ErrorHandler.dialog() is JOptionPane.showMessageDialog without a parent. */
		static void defaultDialog(Throwable error) {
			SwingUtilities.invokeLater(() -> {
				String title = error.getClass().getSimpleName();
				JOptionPane.showMessageDialog(null, error.getMessage() + "\n\n" + StringPrinter.buildString(printer -> {
					PrintWriter writer = printer.toPrintWriter();
					error.printStackTrace(writer);
					writer.close();
				}), title, JOptionPane.ERROR_MESSAGE);
			});
		}

		/**
		 * An implementation of all of the ErrorHandler built-ins which throws an AssertionError
		 * on any exception.  This can be helpful for JUnit tests.
		 */
		public static class OnErrorThrowAssertion implements Log, Dialog {
			@Override
			public void accept(Throwable error) {
				throw new AssertionError(error);
			}
		}
	}
}
