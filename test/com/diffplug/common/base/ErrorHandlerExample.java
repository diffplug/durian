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

import java.util.Arrays;
import java.util.List;

import com.diffplug.common.base.ErrorHandler;
import com.diffplug.common.base.Throwing;

// @formatter:off
@SuppressWarnings({"serial"})
public class ErrorHandlerExample {
	public void whyItsGreat() throws Exception {
		List<Food> foodOnPlate = Arrays.asList(
				cook("salmon"),
				cook("asparagus"),
				cook("enterotoxin"));

		// without ErrorHandler, we have to write this
		foodOnPlate.forEach(val -> {
			try {
				eat(val);
			} catch (Barf e) {
				// get out the baking soda
			}
		});

		// With ErrorHandler, we can succinctly
		//                               sweep it under the rug
		foodOnPlate.forEach(ErrorHandler.suppress().wrap(this::eat));
		//                               save it for later
		foodOnPlate.forEach(ErrorHandler.log().wrap(this::eat));
		//                               make mom deal with it
		foodOnPlate.forEach(ErrorHandler.rethrow().wrap(this::eat));
		//                               ask the user deal with it
		foodOnPlate.forEach(ErrorHandler.dialog().wrap(this::eat));

		// We can also make our own ErrorHandlers, which can be reused across a project
		ErrorHandler retryHandler = ErrorHandler.createHandling(error -> {
			if (error instanceof Barf) {
				Food cause = ((Barf) error).looksLikeItUsedToBe();
				try {
					eat(cause);
				} catch (Barf barf) {
					// at least we tried really hard
				}
			}
		});
		foodOnPlate.forEach(retryHandler.wrap(this::eat));
	}

	public void whereDoLogAndDialogComeFrom() {
		// ErrorHandler.log() promises to "log", and ErrorHandler.dialog() promises to alert the user.
		// Doing those things is very different depending on whether your code is running as a web
		// application, console application, or desktop gui

		// One way around this ambiguity is to avoid using ErrorHandler.log() and ErrorHandler.dialog()
		// entirely, and only use your own custom ErrorHandlers.  But if you are shipping a framework,
		// and your users might end up using ErrorHandler.log() and .dialog(), then you might want to
		// specify what those do.

		// By default, ErrorHandler.log() is just Throwable.printStackTrace, and ErrorHandler.dialog()
		// opens a JOptionPane. You can modify this behavior with the following:
		DurianPlugins.register(ErrorHandler.Plugins.Log.class, error -> {
			// log to twitter
		});
		DurianPlugins.register(ErrorHandler.Plugins.Dialog.class, error -> {
			// Headless application: email the sysadmin and exit
			// Web application: ajax an alert() to the user
		});
		// The trick is, you have to call these methods BEFORE ErrorHandler.log() or ErrorHandler.dialog()
		// are used anywhere in your whole application.  Once log() or dialog() have been used, they are
		// fixed for the duration of the runtime. If you're writing a library, then you probably shouldn't
		// try to change them.  If you're writing an application or framework, then you probably should.
	}

	@Override
	public Object clone() {
		// We'd like to return a Food,
		// but the only way to get it is the cook() method,
		// which throws a checked exception 
		try {
			return cook("spaghetti");
		} catch (IAmOnFire e) {
			// TODO: water()
			return CEREAL;
		}

		// Our superclass doesn't let us propagate the exception,
		// so we've got to either
		//     A) return a default value
		//     B) rethrow the checked exception, wrapped in a RuntimeException
		//
		// If we decide to take the "default value" route, we might want to suppress
		// the memory of being on fire, or we might want to log it to twitter
	}

	@Override
	public void finalize() {
		// If we're taking the "default value" route, ErrorHandler has you covered
		Food logged = ErrorHandler.log().getWithDefault(() -> cook("spaghetti"), CEREAL); // log to twitter
		Food suppressed = ErrorHandler.suppress().getWithDefault(() -> cook("spaghetti"), CEREAL); // suppress to insecurity buffer

		// If we're taking the "rethrow RuntimeException" route, then specifying a
		// default value would be nonsensical, so we don't do it
		Food rethrow = ErrorHandler.rethrow().get(() -> cook("spaghetti"));

		// I'm stressed, don't judge me
		ErrorHandler.suppress().run(() -> {
			eat(logged);
			eat(suppressed);
			eat(rethrow);
		});
	}

	public void finalizeAdvanced() {
		// The previous example uncovered one of ErrorHandler's secrets. There are two 
		// subclasses of ErrorHandler: ErrorHandler.Handling, and ErrorHandler.Rethrowing.

		// An instance of ErrorHandler.Rethrowing is an ErrorHandler that guarantees by construction
		// to always handle errors by throwing a RuntimeException.  This means that when it is returning
		// a value from a fallible function, it doesn't need a default value.
		ErrorHandler.Rethrowing rethrowing = ErrorHandler.createRethrowing(error -> {
			// Note that we're returning the exception, not throwing it.
			// The ErrorHandler will throw it for us, thus "guaranteed by construction".
			// It'd be okay if we threw it ourselves too, but it's not as pretty.
			return new RuntimeException("AHHHHHHHHHHHHHHHHHH!!!!");
		});
		Food thrown = rethrowing.get(() -> cook("spaghetti")); // no default needed

		// An instance of ErrorHandler.Handling is an ErrorHandler that doesn't make this guarantee.
		// Since it isn't going to handle errors by throwing an exception (well, it might, but it isn't
		// promising to, so it might not), a default value is required.
		ErrorHandler.Handling handling = ErrorHandler.createHandling(error -> {
			if (error instanceof RuntimeException) {
				throw (RuntimeException) error;
			} else {
				System.err.println("Hot! Hot! Hot! Hot!");
			}
		});
		Food handled = handling.getWithDefault(() -> cook("spaghetti"), CEREAL); // gotta have that default

		// A plain-old ErrorHandler can deal with functions that don't return values (namely Runnable and Consumer).
		// But to work with functions that do return a value (namely Supplier and Function), you're gonna
		// need to have it in its true Handling / Rethrowing form.  In a modern IDE with autocomplete,
		// The Right Thing will just automatically happen for you.
		if (thrown.equals(handled)) {
			// I guess I wasted my time with the two subclasses, because it didn't matter in the end.
		} else {
			// Time well spent.
		}
	}

	@SuppressWarnings("unused")
	public void wrapping() {
		// If your functional interface has 0 or 1 outputs, and 0 or 1 inputs, then ErrorHandler can wrap it into its standard Java 8 form
		Throwing.Runnable marathon = () -> { throw new IAmOnFire(); };
		java.lang.Runnable marathonSafe = ErrorHandler.log().wrap(marathon);

		Throwing.Consumer<Food> eat = this::eat;
		java.util.function.Consumer<Food> eatSafe = ErrorHandler.log().wrap(eat);

		Throwing.Supplier<Food> cookSpatula = () -> cook("spatula");
		java.util.function.Supplier<Food> cookSpatulaOrGetCereal = ErrorHandler.log().wrapWithDefault(cookSpatula, CEREAL);
		java.util.function.Supplier<Food> cookSpatulaOrBurn = ErrorHandler.rethrow().wrap(cookSpatula);

		Throwing.Function<String, Food> cookAnything = this::cook;
		java.util.function.Function<String, Food> cookAnythingOrGetCereal = ErrorHandler.log().wrapWithDefault(this::cook, CEREAL);
		java.util.function.Function<String, Food> cookAnythingOrBurn = ErrorHandler.rethrow().wrap(this::cook);

		// If your function has more than 1 input, you can either
		//    A) Make a wrapper function that calls ErrorHandler.get() to return a value (recommended)
		//    B) Make a "wrapper wrapper" (see ErrorHandlerMultipleInputs.png for more details)
		// If your function has more than 1 output, see ErrorHandlerMultipleOutputs.png
	}

	@SuppressWarnings("unused")
	public void throwingSpecfic() {
		// Throwing.Specific lets you express functional interfaces which throw a specific exception
		Throwing.Specific.Consumer<Food, Barf> eatSignature = this::eat;
		Throwing.Specific.Function<String, Food, IAmOnFire> cookSignature = this::cook;

		// This can be helpful for writing generic code for working with a specific kind of exception, but
		// it isn't helpful for writing code for generic exceptions (because type erasure).
		//
		// The only way to know the type of List<T> is to grab an element out of the
		// list and call getClass() on it.  And then you still mostly don't know the type of the list.
		// This same limitation ripples throw exception handling in the following way:
		class BarfHarness {
			// If you knew it was Barf at compile time then you're all set!
			void exceptionKnownAtCompileTime(Throwing.Specific.Runnable<Barf> eatAndThen) {
				try {
					eatAndThen.run();
				} catch (Barf e) {
					// and on Janitor's day too!
				}
			}

			// If the exception was generic at compile time, then you have to catch the most-general possible exception
			<E extends Throwable> void exceptionGenericAtCompileTime(Throwing.Specific.Runnable<E> eatAndThen) {
				try {
					eatAndThen.run();
					// You can try "catch (E e)", but you'll get: "Cannot use the type parameter E in a catch block"
					// You can try "catch (Barf e)", but you'll get: "Unreachable catch block for ErrorHandlerExample.Barf. This exception is never thrown from the try statement body"
				} catch (Throwable e) {
					// Looks like we're stuck catching Throwable.  Why should we bother having the exception be generic?
				}
			}
		}

		// Well, we probably shouldn't.  It didn't do us any good.
		// Throwing.Specific.* is most useful when it's implementing plain-old Throwing.*
		Throwing.Runnable runnableNonSpecific = () -> { throw new IAmOnFire(); };
		Throwing.Specific.Runnable<Throwable> runnable = runnableNonSpecific;

		// Here's the voluminous source code for Throwing:
		// public interface Runnable extends Specific.Runnable<Throwable> {}
		// public interface Supplier<T> extends Specific.Supplier<T, Throwable> {}
		// public interface Consumer<T> extends Specific.Consumer<T, Throwable> {}
		// public interface Function<T, R> extends Specific.Function<T, R, Throwable> {}
		// public interface Predicate<T> extends Specific.Predicate<T, Throwable> {}

		// Now you know how to cook spaghetti and eat salmon using Java >= 8 and generic exceptions!
	}

	/** Immutable, unfortunately. */
	private class IAmOnFire extends Exception {}

	/** A monoid over the group "food". */
	class Food {}

	/** Needs another day or two to flesh out some implementation details. */
	private class Barf extends Exception {
		public Food looksLikeItUsedToBe() {
			// TODO: Computer vision
			return null;
		}
	}

	/** Endofunctor of the monoid over the food. */
	void eat(Food food) throws Barf {}

	/** Returns the Hamming distance between the ingredients. */
	Food cook(String ingredients) throws IAmOnFire {
		return null;
	}

	/** This is obtained after stamping the natural numbers in ascending order on individual grains. */
	private static final Food CEREAL = null;
}
//@formatter:on
