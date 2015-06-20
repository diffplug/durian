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

import java.io.PrintWriter;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/** 
 * Executes code and wraps functions, sending any errors to a {@code Consumer<Throwable>} error handler,
 * see <a href="https://github.com/diffplug/durian/blob/master/test/com/diffplug/common/base/ErrorsExample.java">ErrorsExample</a>.
 */
public abstract class Errors implements Consumer<Throwable> {
	/** Package-private for testing - resets all of the static member variables. */
	static void resetForTesting() {
		log = null;
		dialog = null;
	}

	protected final Consumer<Throwable> handler;

	protected Errors(Consumer<Throwable> error) {
		this.handler = error;
	}

	/**
	 * Creates an Errors.Handling which passes any exceptions it receives
	 * to the given handler.
	 * <p>
	 * The handler is free to throw a RuntimeException if it wants to. If it always
	 * throws a RuntimeException, then you should instead create an Errors.Rethrowing
	 * using {@link #createRethrowing}.
	 */
	public static Handling createHandling(Consumer<Throwable> handler) {
		return new Handling(handler);
	}

	/**
	 * Creates an Errors.Rethrowing which transforms any exceptions it receives into a RuntimeException
	 * as specified by the given function, and then throws that RuntimeException.
	 * <p>
	 * If that function happens to throw an unchecked error itself, that'll work just fine too.
	 */
	public static Rethrowing createRethrowing(Function<Throwable, RuntimeException> transform) {
		return new Rethrowing(transform);
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

	private static final Rethrowing rethrow = createRethrowing(Errors::asRuntime);

	/**
	 * Logs any exceptions.
	 * <p>
	 * By default, log() calls Throwable.printStackTrace(). To modify this behavior
	 * in your application, call DurianPlugins.set(Errors.Plugins.Log.class, error -> myCustomLog(error));
	 * 
	 * @see DurianPlugins
	 * @see Errors.Plugins.OnErrorThrowAssertion
	 */
	@SuppressFBWarnings(value = "LI_LAZY_INIT_STATIC", justification = "This race condition is fine, as explained in the comment below.")
	public static Handling log() {
		if (log == null) {
			// There is an acceptable race condition here - log might get set multiple times.
			// This would happen if multiple threads called log() at the same time
			// during initialization, and this is likely to actually happen in practice.
			// 
			// Because DurianPlugins guarantees that its methods will have the exact same
			// return value for the duration of the library's runtime existence, the only
			// adverse symptom of this race condition is that there will temporarily be
			// multiple instances of Errors which are wrapping the same Consumer<Throwable>.
			//
			// It is important for this method to be fast, so it's better to accept
			// that suppress() might return different Errors instances which are wrapping
			// the same actual Consumer<Throwable>, rather than to incur the cost of some
			// type of synchronization.
			log = createHandling(DurianPlugins.get(Plugins.Log.class, Plugins::defaultLog));
		}
		return log;
	}

	private static Handling log;

	/**
	 * Opens a dialog to notify the user of any exceptions.  It should be used in cases where
	 * an error is too severe to be silently logged.
	 * <p>
	 * By default, dialog() opens a JOptionPane. To modify this behavior in your application,
	 * call DurianPlugins.set(Errors.Plugins.Dialog.class, error -> openMyDialog(error));
	 * <p>
	 * For a non-interactive console application, a good implementation of would probably
	 * print the error and call System.exit().
	 * 
	 * @see DurianPlugins
	 * @see Errors.Plugins.OnErrorThrowAssertion
	 */
	@SuppressFBWarnings(value = "LI_LAZY_INIT_STATIC", justification = "This race condition is fine, as explained in the comment below.")
	public static Handling dialog() {
		if (dialog == null) {
			// There is an acceptable race condition here.  See Errors.log() for details.
			dialog = createHandling(DurianPlugins.get(Plugins.Dialog.class, Plugins::defaultDialog));
		}
		return dialog;
	}

	private static Handling dialog;

	/** Passes the given error to this Errors. */
	@Override
	public void accept(Throwable error) {
		handler.accept(error);
	}

	/** Attempts to run the given runnable. */
	public void run(Throwing.Runnable runnable) {
		wrap(runnable).run();
	}

	/** Returns a Runnable whose exceptions are handled by this Errors. */
	public Runnable wrap(Throwing.Runnable runnable) {
		return () -> {
			try {
				runnable.run();
			} catch (Throwable e) {
				handler.accept(e);
			}
		};
	}

	/** Returns a Consumer whose exceptions are handled by this Errors. */
	public <T> Consumer<T> wrap(Throwing.Consumer<T> consumer) {
		return val -> {
			try {
				consumer.accept(val);
			} catch (Throwable e) {
				handler.accept(e);
			}
		};
	}

	/**
	 * An {@link Errors} which is free to rethrow the exception, but it might not.
	 * <p>
	 * If we want to wrap a method with a return value, since the handler might
	 * not throw an exception, we need a default value to return.
	 */
	public static class Handling extends Errors {
		protected Handling(Consumer<Throwable> error) {
			super(error);
		}

		/** Attempts to call {@code supplier} and returns {@code onFailure} if an exception is thrown. */
		public <T> T getWithDefault(Throwing.Supplier<T> supplier, T onFailure) {
			return wrapWithDefault(supplier, onFailure).get();
		}

		/** Returns a Supplier which wraps {@code supplier} and returns {@code onFailure} if an exception is thrown. */
		public <T> Supplier<T> wrapWithDefault(Throwing.Supplier<T> supplier, T onFailure) {
			return () -> {
				try {
					return supplier.get();
				} catch (Throwable e) {
					handler.accept(e);
					return onFailure;
				}
			};
		}

		/** Returns a Function which wraps {@code function} and returns {@code onFailure} if an exception is thrown. */
		public <T, R> Function<T, R> wrapWithDefault(Throwing.Function<T, R> function, R onFailure) {
			return input -> {
				try {
					return function.apply(input);
				} catch (Throwable e) {
					handler.accept(e);
					return onFailure;
				}
			};
		}

		/** Returns a Predicate which wraps {@code predicate} and returns {@code onFailure} if an exception is thrown. */
		public <T> Predicate<T> wrapWithDefault(Throwing.Predicate<T> predicate, boolean onFailure) {
			return input -> {
				try {
					return predicate.test(input);
				} catch (Throwable e) {
					handler.accept(e);
					return onFailure;
				}
			};
		}
	}

	/**
	 * An {@link Errors} which is guaranteed to always throw a RuntimeException.
	 * <p>
	 * If we want to wrap a method with a return value, it's pointless to specify
	 * a default value because if the wrapped method fails, a RuntimeException is
	 * guaranteed to throw.
	 */
	public static class Rethrowing extends Errors {
		private final Function<Throwable, RuntimeException> transform;

		protected Rethrowing(Function<Throwable, RuntimeException> transform) {
			super(error -> {
				throw transform.apply(error);
			});
			this.transform = transform;
		}

		/** Attempts to call {@code supplier} and rethrows any exceptions as unchecked exceptions. */
		public <T> T get(Throwing.Supplier<T> supplier) {
			return wrap(supplier).get();
		}

		/** Returns a Supplier which wraps {@code supplier} and rethrows any exceptions as unchecked exceptions. */
		public <T> Supplier<T> wrap(Throwing.Supplier<T> supplier) {
			return () -> {
				try {
					return supplier.get();
				} catch (Throwable e) {
					throw transform.apply(e);
				}
			};
		}

		/** Returns a Function which wraps {@code function} and rethrows any exceptions as unchecked exceptions. */
		public <T, R> Function<T, R> wrap(Throwing.Function<T, R> function) {
			return arg -> {
				try {
					return function.apply(arg);
				} catch (Throwable e) {
					throw transform.apply(e);
				}
			};
		}

		/** Returns a Predicate which wraps {@code predicate} and rethrows any exceptions as unchecked exceptions. */
		public <T> Predicate<T> wrap(Throwing.Predicate<T> predicate) {
			return arg -> {
				try {
					return predicate.test(arg);
				} catch (Throwable e) {
					throw transform.apply(e); // 1 855 548 2505
				}
			};
		}
	}

	/** Casts or wraps the given exception to be a RuntimeException. */
	public static RuntimeException asRuntime(Throwable e) {
		if (e instanceof RuntimeException) {
			return (RuntimeException) e;
		} else {
			return new RuntimeException(e);
		}
	}

	/** Namespace for the plugins which Errors supports. */
	public interface Plugins {
		/** Plugin interface for {@link Errors#log}. */
		public interface Log extends Consumer<Throwable> {}

		/** Plugin interface for {@link Errors#dialog}. */
		public interface Dialog extends Consumer<Throwable> {}

		/** Default behavior of {@link Errors#log} is @{code Throwable.printStackTrace()}. */
		static void defaultLog(Throwable error) {
			error.printStackTrace();
		}

		/** Default behavior of {@link Errors#dialog} is @{code JOptionPane.showMessageDialog} without a parent. */
		static void defaultDialog(Throwable error) {
			SwingUtilities.invokeLater(() -> {
				error.printStackTrace();
				String title = error.getClass().getSimpleName();
				JOptionPane.showMessageDialog(null, error.getMessage() + "\n\n" + StringPrinter.buildString(printer -> {
					PrintWriter writer = printer.toPrintWriter();
					error.printStackTrace(writer);
					writer.close();
				}), title, JOptionPane.ERROR_MESSAGE);
			});
		}

		/**
		 * An implementation of all of the {@link Errors} plugins which throws an AssertionError
		 * on any exception.  This can be helpful for JUnit tests.
		 * <p>
		 * To enable this in your application, you can either:
		 * <ul>
		 * <li>Execute this code at the very beginning of your application:<pre>
		 * DurianPlugins.set(Errors.Plugins.Log.class, new OnErrorThrowAssertion());
		 * DurianPlugins.set(Errors.Plugins.Dialog.class, new OnErrorThrowAssertion());
		 * </pre></li>
		 * <li>Set these system properties:<pre>
		 * durian.plugins.com.diffplug.common.base.Errors.Plugins.Log=com.diffplug.common.base.Errors$Plugins$OnErrorThrowAssertion
		 * durian.plugins.com.diffplug.common.base.Errors.Plugins.Dialog=com.diffplug.common.base.Errors$Plugins$OnErrorThrowAssertion
		 * </pre></li>
		 * </ul>
		 * 
		 * @see DurianPlugins
		 */
		public static class OnErrorThrowAssertion implements Log, Dialog {
			@Override
			public void accept(Throwable error) {
				throw new AssertionError(error);
			}
		}
	}
}
