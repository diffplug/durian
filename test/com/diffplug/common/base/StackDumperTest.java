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

import java.io.PrintStream;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class StackDumperTest {
	PrintStream cleanOut, cleanErr;

	@Before
	public void setupStreams() {
		testCaseOut.setLength(0);
		testCaseErr.setLength(0);

		cleanOut = System.out;
		cleanErr = System.err;

		System.setOut(new StringPrinter(str -> {
			testCaseOut.append(str.replace("\r", ""));
		}).toPrintStream());
		System.setErr(new StringPrinter(str -> {
			testCaseErr.append(str.replace("\r", ""));
		}).toPrintStream());
		StackDumper.pristineSysErr = new StringPrinter(System.err::print);
	}

	@After
	public void restoreStreams() {
		System.setOut(cleanOut);
		System.setErr(cleanErr);
	}

	/** Everything which has been printed to System.out during this test case. */
	StringBuilder testCaseOut = new StringBuilder();
	/** Everything which has been printed to System.err during this test case. */
	StringBuilder testCaseErr = new StringBuilder();

	private void runWithCleanStack(Throwing.Runnable runnable) throws Throwable {
		Box.Nullable<Throwable> testError = Box.Nullable.ofNull();
		Thread thread = new Thread() {
			@Override
			public void run() {
				try {
					runnable.run();
				} catch (Throwable e) {
					testError.set(e);
				}
			}
		};
		thread.start();
		thread.join();
		if (testError.get() != null) {
			throw testError.get();
		}
	}

	/** Make sure that the test harness actually works. */
	@Test(expected = IllegalArgumentException.class)
	public void testTestHarness() throws Throwable {
		System.out.println("System.out");
		System.err.println("System.err");

		Assert.assertEquals("System.out\n", testCaseOut.toString());
		Assert.assertEquals("System.err\n", testCaseErr.toString());

		runWithCleanStack(() -> {
			throw new IllegalArgumentException("");
		});
	}

	@Test
	public void testDump() throws Throwable {
		// dump with no stack
		runWithCleanStack(this::testDumpMethod);
	}

	private void testDumpMethod() {
		StackDumper.dump("some message");
		String errOutput = testCaseErr.toString();
		Assert.assertTrue(errOutput, errOutput.startsWith(StringPrinter.buildStringFromLines(
				"+----------\\",
				"| some message",
				"| at com.diffplug.common.base.StackDumperTest.testDumpMethod(StackDumperTest.java:96)")));
		Assert.assertTrue(errOutput, errOutput.endsWith(StringPrinter.buildStringFromLines(
				"| at com.diffplug.common.base.StackDumperTest$1.run(StackDumperTest.java:62)",
				"+----------/")));
	}

	@Test
	public void testDumpFiltered() throws Throwable {
		// dump with no stack
		runWithCleanStack(() -> {
			StackDumper.dump("some message", StackDumper.captureStackBelow(StackDumperTest.class));
			Assert.assertEquals(StringPrinter.buildStringFromLines(
					"+----------\\",
					"| some message",
					"| at com.diffplug.common.base.StackDumperTest$1.run(StackDumperTest.java:62)",
					"+----------/"), testCaseErr.toString());
		});
	}

	@Test
	public void testDumpWhenSysOut() throws Throwable {
		// dump with no stack
		runWithCleanStack(() -> {
			StackDumper.dumpWhenSysOutContains("Who did this?");

			System.out.println("What!?");
			Assert.assertEquals("What!?\n", testCaseOut.toString());
			Assert.assertEquals("", testCaseErr.toString());

			System.out.println("Who did this?");
			Assert.assertEquals("What!?\nWho did this?\n", testCaseOut.toString());
			String errOutput = testCaseErr.toString();
			Assert.assertTrue(errOutput.startsWith(StringPrinter.buildStringFromLines(
					"+----------\\",
					"| Triggered by Who did this?")));
			Assert.assertTrue(errOutput.endsWith(StringPrinter.buildStringFromLines(
					"| at com.diffplug.common.base.StackDumperTest$1.run(StackDumperTest.java:62)",
					"+----------/")));
		});
	}

	@Test
	public void testDumpWhenSysErr() throws Throwable {
		// dump with no stack
		runWithCleanStack(() -> {
			StackDumper.dumpWhenSysErrContains("Who did this?");

			System.err.println("What!?");
			Assert.assertEquals("", testCaseOut.toString());
			Assert.assertEquals("What!?\n", testCaseErr.toString());

			System.err.println("Who did this?");
			Assert.assertEquals("", testCaseOut.toString());
			String errOutput = testCaseErr.toString();
			Assert.assertTrue(errOutput.startsWith(StringPrinter.buildStringFromLines(
					"What!?",
					"Who did this?",
					"+----------\\",
					"| Triggered by Who did this?")));
			Assert.assertTrue(errOutput.endsWith(StringPrinter.buildStringFromLines(
					"| at com.diffplug.common.base.StackDumperTest$1.run(StackDumperTest.java:62)",
					"+----------/")));
		});
	}
}
