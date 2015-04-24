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
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Registry for plugin implementations which allows global override and handles the retrieval of correct
 * implementation based on order of precedence:
 * <ol>
 * <li>plugin registered globally via {@code register} methods in this class</li>
 * <li>plugin registered and retrieved using {@link java.lang.System#getProperty(String)} (see get methods for
 * property names)</li>
 * <li>default implementation</li>
 * </ol>
 *
 * This class is ripped almost verbatim from <a href="https://github.com/ReactiveX/RxJava/blob/86147542573004f4df84d2a2de83577cf62fe787/src/main/java/rx/plugins/RxJavaPlugins.java">RxJava's RxJavaPlugins</a>. Many thanks to them!
 */
public class DurianPlugins {
	private static final DurianPlugins INSTANCE = new DurianPlugins();

	public static DurianPlugins getInstance() {
		return INSTANCE;
	}

	/** Package-protected constructor. */
	DurianPlugins() {}

	/** Called for failures in ErrorHandler.log(). */
	public interface ErrorHandlerLog extends Consumer<Throwable> {}

	/** Called for failures in ErrorHandler.dialog(). */
	public interface ErrorHandlerDialog extends Consumer<Throwable> {}

	private final AtomicReference<ErrorHandlerLog> errorHandlerLog = new AtomicReference<>();
	private final AtomicReference<ErrorHandlerDialog> errorHandlerDialog = new AtomicReference<>();

	/**
	 * Registers an {@link ErrorHandlerLog} implementation as a global override of any injected or default
	 * implementations.
	 * 
	 * @param impl
	 *            {@link ErrorHandlerLog} implementation
	 * @throws IllegalStateException
	 *             if called more than once or after the default was initialized (if usage occurs before trying
	 *             to register)
	 */
	public void registerErrorHandlerLog(ErrorHandlerLog impl) throws IllegalStateException {
		if (!errorHandlerLog.compareAndSet(null, impl)) {
			throw new IllegalStateException("Another strategy was already registered: " + errorHandlerLog.get());
		}
	}

	/**
	 * Registers an {@link ErrorHandlerDialog} implementation as a global override of any injected or default
	 * implementations.
	 * 
	 * @param impl
	 *            {@link ErrorHandlerDialog} implementation
	 * @throws IllegalStateException
	 *             if called more than once or after the default was initialized (if usage occurs before trying
	 *             to register)
	 */
	public void registerErrorHandlerDialog(ErrorHandlerDialog impl) throws IllegalStateException {
		if (!errorHandlerDialog.compareAndSet(null, impl)) {
			throw new IllegalStateException("Another strategy was already registered: " + errorHandlerDialog.get());
		}
	}

	/**
	 * Retrieves the instance of {@link ErrorHandlerDialog} to use based on order of precedence as
	 * defined in {@link DurianPlugins} class header.
	 * <p>
	 * Override the default by calling {@link #registerErrorHandlerDialog(ErrorHandlerDialog)}
	 * or by setting the property {@code durian.plugin.ErrorDialogHandler.implementation} with the
	 * full classname to load.
	 * 
	 * @return {@link ErrorHandlerDialog} implementation to use
	 */
	public Consumer<Throwable> getErrorHandlerDialog() {
		return getOrDefault(errorHandlerDialog, DurianPlugins::defaultErrorHandlerDialog, ErrorHandlerDialog.class);
	}

	public Consumer<Throwable> getErrorHandlerLog() {
		return getOrDefault(errorHandlerLog, DurianPlugins::defaultErrorHandlerLog, ErrorHandlerLog.class);
	}

	/** Unless the library user hooks us into something, "ErrorHandler.log()" is just printStackTrace(). */
	static void defaultErrorHandlerLog(Throwable error) {
		error.printStackTrace();
	}

	/** Unless the library user hooks us into something, "ErrorHandler.dialog()" is just JOptionPane.showMessageDialog without a parent. */
	static void defaultErrorHandlerDialog(Throwable error) {
		SwingUtilities.invokeLater(() -> {
			JOptionPane.showMessageDialog(null,
					error.getMessage() + "\n\n" + StringPrinter.buildString(printer -> {
				PrintWriter writer = printer.toPrintWriter();
				error.printStackTrace(writer);
				writer.close();
			} ),
					error.getClass().getSimpleName(),
					JOptionPane.ERROR_MESSAGE);
		} );
	}

	/** Full implementation of the caching / lookup strategy, applied to the given AtomicReference, etc. */
	private static <T> T getOrDefault(AtomicReference<T> reference, T defaultValue, Class<T> clazz) {
		if (reference.get() == null) {
			// check for an implementation from System.getProperty first
			Object impl = getPluginImplementationViaProperty(clazz);
			if (impl == null) {
				// nothing set via properties so initialize with default 
				reference.compareAndSet(null, defaultValue);
				// we don't return from here but call get() again in case of thread-race so the winner will always get returned
			} else {
				// we received an implementation from the system property so use it
				@SuppressWarnings("unchecked")
				T castImpl = (T) impl;
				reference.compareAndSet(null, castImpl);
			}
		}
		return reference.get();
	}

	private static Object getPluginImplementationViaProperty(Class<?> pluginClass) {
		String classSimpleName = pluginClass.getSimpleName();
		/*
		 * Check system properties for plugin class.
		 * <p>
		 * This will only happen during system startup thus it's okay to use the synchronized
		 * System.getProperties as it will never get called in normal operations.
		 */
		String implementingClass = System.getProperty("durian.plugin." + classSimpleName + ".implementation");
		if (implementingClass != null) {
			try {
				Class<?> cls = Class.forName(implementingClass);
				// narrow the scope (cast) to the type we're expecting
				cls = cls.asSubclass(pluginClass);
				return cls.newInstance();
			} catch (ClassCastException e) {
				throw new RuntimeException(classSimpleName + " implementation is not an instance of " + classSimpleName + ": " + implementingClass);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(classSimpleName + " implementation class not found: " + implementingClass, e);
			} catch (InstantiationException e) {
				throw new RuntimeException(classSimpleName + " implementation not able to be instantiated: " + implementingClass, e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(classSimpleName + " implementation not able to be accessed: " + implementingClass, e);
			}
		} else {
			return null;
		}
	}
}
