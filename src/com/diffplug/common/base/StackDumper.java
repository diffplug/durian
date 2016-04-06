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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Utility methods for dumping the stack - arbitrarily or at specific trigger points (such as when a certain string prints to console).
 * <p>
 * If someone is printing "junk" to the console and you can't figure out why, try {@code StackDumper.dumpWhenSysOutContains("junk")}.
 */
public class StackDumper {
	static StringPrinter pristineSysErr = new StringPrinter(System.err::print);

	/** Dumps the given message and stack to the system error console. */
	public static void dump(String message, List<StackTraceElement> stack) {
		printEmphasized(message + "\n" + stackTraceToString(stack));
	}

	/** Dumps the given message and stack to the system error console. */
	public static void dump(String message, StackTraceElement[] stackTrace) {
		dump(message, Arrays.asList(stackTrace));
	}

	/** Dumps the given message and exception stack to the system error console */
	public static void dump(String message, Throwable exception) {
		printEmphasized(StringPrinter.buildString(printer -> {
			printer.println(message);
			exception.printStackTrace(printer.toPrintWriter());
		}));
	}

	/** Dumps the current stack to the system error console. */
	public static void dump(String message) {
		dump(message, captureStackBelow());
	}

	/** Dumps the first {@code stackLimit} frames of the current stack to the system error console, excluding traces from {@code classPrefixesToExclude}. */
	public static void dump(String message, int stackLimit, String... classPrefixesToExclude) {
		// class names to include
		Predicate<StackTraceElement> isIncluded = trace -> {
			for (String prefix : classPrefixesToExclude) {
				if (trace.getClassName().startsWith(prefix)) {
					return false;
				}
			}
			return true;
		};
		// filter the stack
		List<StackTraceElement> stack = captureStackBelow().stream()
				.filter(trace -> trace.getLineNumber() >= 0)
				.filter(isIncluded)
				.limit(stackLimit)
				.collect(Collectors.toList());
		dump(message, stack);
	}

	/** Dumps a stack trace anytime the trigger string is printed to System.out. */
	public static void dumpWhenSysOutContains(String trigger) {
		System.setOut(wrapAndDumpWhenContains(System.out, trigger));
	}

	/** Dumps a stack trace anytime trigger string is printed to System.err. */
	public static void dumpWhenSysErrContains(String trigger) {
		System.setErr(wrapAndDumpWhenContains(System.err, trigger));
	}

	/**
	 * Returns a PrintStream which will redirect all of its output to the source PrintStream. If
	 * the trigger string is passed through the wrapped PrintStream, then it will dump the
	 * stack trace of the call that printed the trigger.
	 * 
	 * @param source
	 *            the returned PrintStream will delegate to this stream
	 * @param trigger
	 *            the string which triggers a stack dump
	 * @return a PrintStream with the above properties
	 */
	public static PrintStream wrapAndDumpWhenContains(PrintStream source, String trigger) {
		StringPrinter wrapped = new StringPrinter(StringPrinter.stringsToLines(perLine -> {
			source.println(perLine);
			if (perLine.contains(trigger)) {
				dump("Triggered by " + trigger);
			}
		}));
		return wrapped.toPrintStream();
	}

	/** Converts a list of stack trace elements to a String similar to Throwable.printStackTrace(). */
	private static String stackTraceToString(List<StackTraceElement> stack) {
		return StringPrinter.buildString(printer -> {
			for (StackTraceElement element : stack) {
				printer.print("at ");
				printer.print(element.getClassName());
				printer.print(".");
				printer.print(element.getMethodName());
				printer.print("(");
				printer.print(element.getFileName());
				printer.print(":");
				printer.print(Integer.toString(element.getLineNumber()));
				printer.println(")");
			}
		});
	}

	/** Captures all of the current stack which is below the given classes. */
	public static List<StackTraceElement> captureStackBelow(Class<?>... clazzes) {
		List<Class<?>> toIgnore = new ArrayList<>(clazzes.length + 1);
		toIgnore.addAll(Arrays.asList(clazzes));
		toIgnore.add(StackDumper.class);

		Predicate<StackTraceElement> isSkipped = element -> toIgnore.stream().anyMatch(clazz -> {
			String name = element.getClassName();
			return name.equals(clazz.getName()) || name.startsWith(clazz.getName() + "$$Lambda");
		});

		List<StackTraceElement> rawStack = Arrays.asList(Thread.currentThread().getStackTrace());
		ListIterator<StackTraceElement> iterator = rawStack.listIterator();

		// iterate until we find something skipped
		while (iterator.hasNext() && !isSkipped.test(iterator.next())) {}

		boolean foundSomethingToSkip = iterator.hasNext();
		if (foundSomethingToSkip) {
			// iterate unti we find something not skipped
			while (iterator.hasNext() && isSkipped.test(iterator.next())) {}
			// the filtering was successful!
			return rawStack.subList(iterator.previousIndex(), rawStack.size());
		} else {
			// we didn't find something to skip, so we'll return the whole stack
			return rawStack;
		}
	}

	/** 
	 * Prints the given string to the the given printer, wrapped in hierarchy-friendly
	 * braces.  Useful for emphasizing a specific event from a sea of logging statements.
	 */
	private static void printEmphasized(String toPrint) {
		// print the triggered header
		pristineSysErr.println("+----------\\");
		for (String line : toPrint.split("\n")) {
			pristineSysErr.println("| " + line);
		}
		pristineSysErr.println("+----------/");
	}
}
