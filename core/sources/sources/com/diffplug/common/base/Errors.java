/*
 * Original Guava code is copyright (C) 2015 The Guava Authors.
 * Modifications from Guava are copyright (C) 2016 DiffPlug.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.diffplug.common.base;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Converts functions which throw exceptions into functions that don't by passing exceptions to an error policy.
 *
 * Let's say you have a method `void eat(Food food) throws Barf`, and you wanted to pass a `List<Food>` to this
 * method.
 *
 * ```java
 * List<Food> foodOnPlate = Arrays.asList(
 *     cook("salmon"),
 *     cook("asparagus"),
 *     cook("enterotoxin"));
 *
 * // without Errors, we have to write this
 * foodOnPlate.forEach(val -> {
 *     try {
 *         eat(val);
 *     } catch (Barf e) {
 *         // get out the baking soda
 *     }
 * });
 * ```
 *
 * Because the {@link Consumer} required by {@link Iterable#forEach(Consumer)} doesn't allow checked exceptions,
 * and `void eat(Food food) throws Barf` has a checked exception, we can't take advantage of method references.
 *
 * With `Errors`, we can do this succinctly:
 *
 * ```java
 * //                         sweep it under the rug
 * foodOnPlate.forEach(Errors.suppress().wrap(this::eat));
 * //                         save it for later
 * foodOnPlate.forEach(Errors.log().wrap(this::eat));
 * //                         make mom deal with it
 * foodOnPlate.forEach(Errors.rethrow().wrap(this::eat));
 * //                         ask the user deal with it
 * foodOnPlate.forEach(Errors.dialog().wrap(this::eat));
 * ```
 *
 * Errors comes with four built-in error handling policies: {@link #suppress()}, {@link #log()}, {@link #rethrow()}, and {@link #dialog()}.
 * If you don't like their default behaviors, you can change them using {@link Plugins} and {@link DurianPlugins}.
 *
 * You can also create your own error handling policies using {@link #createHandling(Consumer)} and {@link #createRethrowing(Function)}.
 *
 * For a deep-dive into how `Errors` works, see [ErrorsExample.java](https://github.com/diffplug/durian/blob/10631a3480e5491eb6eb6ee06e752d8596914232/test/com/diffplug/common/base/ErrorsExample.java).
 */
public abstract class Errors implements Consumer<Throwable> {
	/** Package-private for testing - resets all of the static member variables. */
	static void resetForTesting() {
		log = null;
		dialog = null;
	}

	protected final Consumer<Throwable> handler;

	protected Errors(Consumer<Throwable> error) {
		this.handler = requireNonNull(error);
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

	private static final Rethrowing rethrow = createRethrowing(Errors::rethrowErrorAndWrapOthersAsRuntime);

	private static RuntimeException rethrowErrorAndWrapOthersAsRuntime(Throwable e) {
		if (e instanceof Error) {
			throw (Error) e;
		} else {
			return Errors.asRuntime(e);
		}
	}

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
		requireNonNull(error);
		handler.accept(error);
	}

	/** Converts this {@code Consumer<Throwable>} to a {@code Consumer<Optional<Throwable>>}. */
	public Consumer<Optional<Throwable>> asTerminal() {
		return errorOpt -> {
			if (errorOpt.isPresent()) {
				accept(errorOpt.get());
			}
		};
	}

	/** Attempts to run the given runnable. */
	public void run(Throwing.Runnable runnable) {
		wrap(runnable).run();
	}

	/** Returns a Runnable whose exceptions are handled by this Errors. */
	public Runnable wrap(Throwing.Runnable runnable) {
		requireNonNull(runnable);
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
		requireNonNull(consumer);
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
	 * 
	 * If we want to wrap a method with a return value, since the handler might
	 * not throw an exception, we need a default value to return.
	 */
	public static class Handling extends Errors {
		protected Handling(Consumer<Throwable> error) {
			super(error);
		}

		/** Attempts to call {@code supplier} and returns {@code onFailure} if an exception is thrown. */
		public <T> T getWithDefault(Throwing.Supplier<T> supplier, @Nullable T onFailure) {
			return wrapWithDefault(supplier, onFailure).get();
		}

		/** Returns a Supplier which wraps {@code supplier} and returns {@code onFailure} if an exception is thrown. */
		public <T> Supplier<T> wrapWithDefault(Throwing.Supplier<T> supplier, @Nullable T onFailure) {
			requireNonNull(supplier);
			return () -> {
				try {
					return supplier.get();
				} catch (Throwable e) {
					handler.accept(e);
					return onFailure;
				}
			};
		}

		/**
		 * Returns a Function which wraps {@code function} and returns {@code onFailure} if an exception is thrown.
		 *
		 * If you are getting an error about {@code the method wrapWithDefault is ambiguous}, use
		 * {@link #wrapFunctionWithDefault(com.diffplug.common.base.Throwing.Function)} or
		 * {@link #wrapPredicateWithDefault(com.diffplug.common.base.Throwing.Predicate).
		 */
		public <T, R> Function<T, R> wrapWithDefault(Throwing.Function<T, R> function, @Nullable R onFailure) {
			return wrapFunctionWithDefault(function, onFailure);
		}

		/**
		 * Returns a Predicate which wraps {@code predicate} and returns {@code onFailure} if an exception is thrown.
		 *
		 * If you are getting an error about {@code the method wrapWithDefault is ambiguous}, use
		 * {@link #wrapFunctionWithDefault(com.diffplug.common.base.Throwing.Function)} or
		 * {@link #wrapPredicateWithDefault(com.diffplug.common.base.Throwing.Predicate).
		 */
		public <T> Predicate<T> wrapWithDefault(Throwing.Predicate<T> predicate, boolean onFailure) {
			return wrapPredicateWithDefault(predicate, onFailure);
		}

		/** Returns a Function which wraps {@code function} and returns {@code onFailure} if an exception is thrown. */
		public <T, R> Function<T, R> wrapFunctionWithDefault(Throwing.Function<T, R> function, @Nullable R onFailure) {
			requireNonNull(function);
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
		public <T> Predicate<T> wrapPredicateWithDefault(Throwing.Predicate<T> predicate, boolean onFailure) {
			requireNonNull(predicate);
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
	 * 
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
			this.transform = requireNonNull(transform);
		}

		/** Attempts to call {@code supplier} and rethrows any exceptions as unchecked exceptions. */
		public <T> T get(Throwing.Supplier<T> supplier) {
			return wrap(supplier).get();
		}

		/** Returns a Supplier which wraps {@code supplier} and rethrows any exceptions as unchecked exceptions. */
		public <T> Supplier<T> wrap(Throwing.Supplier<T> supplier) {
			requireNonNull(supplier);
			return () -> {
				try {
					return supplier.get();
				} catch (Throwable e) {
					throw transform.apply(e);
				}
			};
		}

		/**
		 * Returns a Function which wraps {@code function} and rethrows any exceptions as unchecked exceptions.
		 * <p>
		 * If you are getting an error about {@code the method wrap is ambiguous}, use
		 * {@link #wrapFunction(com.diffplug.common.base.Throwing.Function)} or
		 * {@link #wrapPredicate(com.diffplug.common.base.Throwing.Predicate).
		 * */
		public <T, R> Function<T, R> wrap(Throwing.Function<T, R> function) {
			return wrapFunction(function);
		}

		/**
		 * Returns a Predicate which wraps {@code predicate} and rethrows any exceptions as unchecked exceptions.
		 * <p>
		 * If you are getting an error about {@code the method wrap is ambiguous}, use
		 * {@link #wrapFunction(com.diffplug.common.base.Throwing.Function)} or
		 * {@link #wrapPredicate(com.diffplug.common.base.Throwing.Predicate).
		 * */
		public <T> Predicate<T> wrap(Throwing.Predicate<T> predicate) {
			return wrapPredicate(predicate);
		}

		/** Returns a Function which wraps {@code function} and rethrows any exceptions as unchecked exceptions. */
		public <T, R> Function<T, R> wrapFunction(Throwing.Function<T, R> function) {
			requireNonNull(function);
			return arg -> {
				try {
					return function.apply(arg);
				} catch (Throwable e) {
					throw transform.apply(e);
				}
			};
		}

		/** Returns a Predicate which wraps {@code predicate} and rethrows any exceptions as unchecked exceptions. */
		public <T> Predicate<T> wrapPredicate(Throwing.Predicate<T> predicate) {
			requireNonNull(predicate);
			return arg -> {
				try {
					return predicate.test(arg);
				} catch (Throwable e) {
					throw transform.apply(e); // 1 855 548 2505
				}
			};
		}
	}

	/**
	 * Casts or wraps the given exception to be a RuntimeException.
	 * 
	 * If the input exception is a RuntimeException, it is simply
	 * cast and returned.  Otherwise, it wrapped in a
	 * {@link WrappedAsRuntimeException} and returned.
	 */
	public static RuntimeException asRuntime(Throwable e) {
		requireNonNull(e);
		if (e instanceof RuntimeException) {
			return (RuntimeException) e;
		} else {
			return new WrappedAsRuntimeException(e);
		}
	}

	/** A RuntimeException specifically for the purpose of wrapping non-runtime Throwables as RuntimeExceptions. */
	public static class WrappedAsRuntimeException extends RuntimeException {
		private static final long serialVersionUID = -912202209702586994L;

		public WrappedAsRuntimeException(Throwable e) {
			super(e);
		}
	}

	/** Namespace for the plugins which Errors supports. */
	public interface Plugins {
		/** Plugin interface for {@link Errors#log()}. */
		public interface Log extends Consumer<Throwable> {}

		/** Plugin interface for {@link Errors#dialog()}. */
		public interface Dialog extends Consumer<Throwable> {}

		/** Default behavior of {@link Errors#log()} is @{link Throwable#printStackTrace()}. */
		static void defaultLog(Throwable error) {
			error.printStackTrace();
		}

		/**
		 * Default behavior of {@link Errors#dialog()} is @{link Throwable#printStackTrace()}
		 * and {@link javax.swing.JOptionPane#showMessageDialog(java.awt.Component, Object, String, int) JOptionPane.showMessageDialog}.
		 *
		 * The `JOptionPane` part is called using reflection, and fails silently if swing isn't available.  The `Throwable.printStackTrace`
		 * part works whether swing is available or not.
		 */
		static void defaultDialog(Throwable error) {
			error.printStackTrace();
			try {
				Method invokeLater = Class.forName("javax.swing.SwingUtilities").getMethod("invokeLater", Runnable.class);
				Class<?> javaAwtComponent = Class.forName("java.awt.Component");
				Method showMessageDialog = Class.forName("javax.swing.JOptionPane").getMethod("showMessageDialog", javaAwtComponent, Object.class, String.class, int.class);
				Runnable runnable = () -> {
					try {
						String title = error.getClass().getSimpleName();
						showMessageDialog.invoke(null, null, error.getMessage() + "\n\n" + Throwables.getStackTraceAsString(error), title, 0);
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						// if the reflection fails (e.g. because we're on Android) that's no problem, we already dumped the stacktrace to console
					}
				};
				invokeLater.invoke(null, runnable);
			} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				// if the reflection fails (e.g. because we're on Android) that's no problem, we already dumped the stacktrace to console
			}
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

	/**
	 * A fluent API for calling functions which throw an overly-broad
	 * exception (e.g. Throwable, Exception) in way that will throw
	 * the given exception unchanged, and rethrow other exceptions
	 * wrapped up in a RuntimeException according to {@link Errors#asRuntime(Throwable)}.
	 *
	 * ```java
	 * Errors.constrainTo(IOException.class).run(() -> methodWhichThrowsSomethingTooBroad());
	 * String result = Errors.constrainTo(IOException.class).get(() -> methodWhichThrowsSomethingTooBroad());
	 * ```
	 */
	public static <E extends Throwable> ConstrainedTo<E> constrainTo(Class<E> exceptionClass) {
		return new ConstrainedTo(exceptionClass);
	}

	/**
	 * Generated by calling {@link Errors#constrainTo(Class)}.
	 *
	 * A fluent API for calling functions which throw an overly-broad
	 * exception (e.g. Throwable, Exception) in way that will throw
	 * the given exception unchanged, and rethrow other exceptions
	 * wrapped up in a RuntimeException according to {@link Errors#asRuntime(Throwable)}.
	 */
	public static final class ConstrainedTo<E extends Throwable> {
		final Class<E> exceptionClass;

		private ConstrainedTo(Class<E> exceptionClass) {
			this.exceptionClass = Objects.requireNonNull(exceptionClass);
		}

		/**
		 * Runs the given runnable.  If an exception of the
		 * constrained type is thrown, it is rethrown
		 * unchanged.  If an exception of a different
		 * type is thrown, is is rethrown according to
		 * {@link Errors#asRuntime(Throwable)}.
		 */
		public void run(Throwing.Runnable runnable) throws E {
			try {
				runnable.run();
			} catch (Throwable error) {
				if (exceptionClass.isAssignableFrom(error.getClass())) {
					throw (E) error;
				} else {
					throw Errors.asRuntime(error);
				}
			}
		}

		/**
		 * Returns the value generated by the given supplier.
		 * If an exception of the constrained type is thrown,
		 * it is rethrown unchanged.  If an exception of a
		 * different type is thrown, is is rethrown according to
		 * {@link Errors#asRuntime(Throwable)}.
		 */
		public <T> T get(Throwing.Supplier<T> supplier) throws E {
			try {
				return supplier.get();
			} catch (Throwable error) {
				if (exceptionClass.isAssignableFrom(error.getClass())) {
					throw (E) error;
				} else {
					throw Errors.asRuntime(error);
				}
			}
		}
	}
}
