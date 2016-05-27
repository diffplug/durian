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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;

public class ConverterBenchmark {
	@Test
	public void simpleLittleBenchmark() {
		Converter<Integer, Integer> converter = Converter.from(i -> i + 1, i -> i - 1);
		ConverterNonNull<Integer, Integer> nonNull = ConverterNonNull.from(i -> i + 1, i -> i - 1, "increment");

		int trialPer = 1000000;
		List<Integer> trials = new ArrayList<>(trialPer);
		for (int i = 0; i < trialPer; ++i) {
			trials.add(i);
		}

		Profiler profiler = new Profiler();
		profiler.addTest("converter standard", () -> {
			for (Integer trial : trials) {
				Integer plusOne = converter.revert(trial);
				Integer minusOne = converter.convert(plusOne);
				Assert.assertEquals(trial, minusOne);
			}
		});
		profiler.addTest("converter  nonNull", () -> {
			for (Integer trial : trials) {
				Integer plusOne = converter.revertNonNull(trial);
				Integer minusOne = converter.convertNonNull(plusOne);
				Assert.assertEquals(trial, minusOne);
			}
		});
		profiler.addTest("           nonNull", () -> {
			for (Integer trial : trials) {
				Integer plusOne = nonNull.revert(trial);
				Integer minusOne = nonNull.convert(plusOne);
				Assert.assertEquals(trial, minusOne);
			}
		});
		profiler.runRandomTrials(100);
	}

	/** Returns the amount of time in seconds since lap was last called. The first call returns a very large number. */
	public static class LapTimer {
		private long start = 0;

		private double FACTOR = 1.0 / TimeUnit.MILLISECONDS.convert(1, TimeUnit.SECONDS);

		public double lap() {
			long newTime = System.currentTimeMillis();
			double elapsed = (newTime - start) * FACTOR;
			start = newTime;
			return elapsed;
		}

		public static double UI_LIMIT = 0.1;

		public static class Nano {
			private long start = 0;

			private double FACTOR = 1.0 / TimeUnit.NANOSECONDS.convert(1, TimeUnit.SECONDS);

			public double lap() {
				long newTime = System.nanoTime();
				double elapsed = (newTime - start) * FACTOR;
				start = newTime;
				return elapsed;
			}
		}
	}

	/** Profiles some code. */
	public static class Profiler {

		private List<Test> tests = Lists.newArrayList();

		/** Adds the given test to the profiler. */
		public void addTest(String name, ITimed test) {
			tests.add(new Test(name, test));
		}

		/** Adds the given test to the profiler. */
		public void addTest(String name, Runnable test) {
			tests.add(new Test(name, new ITimed() {
				LapTimer timer = new LapTimer();

				@Override
				public double time() throws Throwable {
					timer.lap();
					test.run();
					return timer.lap();
				}
			}));
		}

		/** Runs the tests in a random order, but guaranteed to run numTrials for each. */
		public void runRandomTrials(int numTrials) {
			List<Test> shuffledTests = Lists.newArrayList(tests);
			LapTimer timer = new LapTimer();
			for (int i = 0; i < numTrials; ++i) {
				Collections.shuffle(shuffledTests);
				System.out.print("Running trial " + (i + 1) + " of " + numTrials + " ... ");
				timer.lap();
				for (Test test : shuffledTests) {
					test.runTrial();
				}
				System.out.println(" complete after " + format(timer.lap()) + ".");

				// print the results after every run, in case it crashes
				printResults();
			}
		}

		/** Prints the results of the trials. */
		private void printResults() {
			for (Test test : tests) {
				test.printResults();
			}
		}

		/** Wraps up a single ITimed under test. */
		private static class Test {
			private final String name;
			private final ITimed underTest;
			private List<Double> times = Lists.newArrayList();

			public Test(String name, ITimed test) {
				this.name = name;
				this.underTest = test;
			}

			public void runTrial() {
				try {
					times.add(underTest.time());
				} catch (Throwable e) {
					times.add(Double.NaN);
					e.printStackTrace();
				}
			}

			public void printResults() {
				System.out.print(name + ": ");

				double max = times.get(0);
				double min = times.get(0);
				double total = times.get(0);
				for (int i = 1; i < times.size(); ++i) {
					double time = times.get(i);
					max = Math.max(max, time);
					min = Math.min(min, time);
					total += time;
				}

				System.out.print("min=" + format(min) + " mean=" + format(total / times.size()) + " max=" + format(max));
				System.out.println();
			}
		}

		/** Formats the given double. */
		private static String format(double elapsedSec) {
			int elapsedMs = (int) Math.round(elapsedSec * 1000);
			return Integer.toString(elapsedMs) + " ms";
		}

		/** Interface for an ITimed object. */
		public interface ITimed {
			double time() throws Throwable;
		}

		/** Default implementation of a timed event. */
		public static abstract class DefaultTimed implements ITimed {
			private LapTimer timer = new LapTimer();

			/** Setup a single test. */
			protected abstract void init();

			/** Executes the timed portion of a test. */
			protected abstract void timed() throws Throwable;

			/** Performs cleanup on a given test. */
			protected abstract void cleanup();

			/** Runs init(), timed(), cleanup(). Returns the elapsed time of timed(). */
			@Override
			public double time() throws Throwable {
				init();
				timer.lap();
				timed();
				double time = timer.lap();
				cleanup();
				return time;
			}
		}
	}
}
