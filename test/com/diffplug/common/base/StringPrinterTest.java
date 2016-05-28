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

import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.function.Consumer;

import org.junit.Assert;
import org.junit.Test;

public class StringPrinterTest {
	private static final String TEST_STR = "ABC123\nDo-Re-Mi\n";

	private static void printTestStr(StringPrinter printer) {
		printer.print("ABC");
		printer.println("123");
		printer.println("Do-Re-Mi");
	}

	@Test
	public void testBasicFunctionality() {
		StringBuilder builder = new StringBuilder();
		StringPrinter printer = new StringPrinter(builder::append);
		printTestStr(printer);
		Assert.assertEquals(TEST_STR, builder.toString());
	}

	@Test
	public void testStringCreator() {
		Assert.assertEquals(TEST_STR, StringPrinter.buildString(printer -> {
			printTestStr(printer);
		}));
	}

	@Test
	public void testToOutputStream() {
		Assert.assertEquals(TEST_STR, StringPrinter.buildString(printer -> {
			Errors.rethrow().run(() -> {
				printer.toOutputStream(StandardCharsets.UTF_8).write(TEST_STR.getBytes(StandardCharsets.UTF_8));
			});
		}));
	}

	@Test
	public void testToPrintStream() {
		Assert.assertEquals(TEST_STR, StringPrinter.buildString(printer -> {
			Errors.rethrow().run(() -> {
				printer.toPrintStream().print(TEST_STR);
			});
		}));
	}

	/** Tests each method in turn. */
	@Test
	public void testToWriter() {
		writerTestCase("c",
				test -> test.append('c'));
		writerTestCase(TEST_STR,
				test -> test.append(TEST_STR));
		writerTestCase(TEST_STR.substring(1, 5),
				test -> test.append(TEST_STR, 1, 5));
		writerTestCase(TEST_STR,
				test -> test.write(TEST_STR.toCharArray()));
		writerTestCase(TEST_STR.substring(1, 5),
				test -> test.write(TEST_STR.toCharArray(), 1, 4));
		writerTestCase("c",
				test -> test.write('c'));
	}

	private void writerTestCase(String expected, Throwing.Consumer<Writer> underTest) {
		Assert.assertEquals(expected, StringPrinter.buildString(printer -> {
			Errors.rethrow().run(() -> {
				underTest.accept(printer.toWriter());
			});
		}));
	}

	/** "I can eat glass" in Japanese, according to http://www.columbia.edu/~fdc/utf8/index.html#glass */
	private static final String TEST_UTF = "私はガラスを食べられます。それは私を傷つけません。";

	@Test
	public void testEncodings() {
		testCharset(StandardCharsets.UTF_8);
		testCharset(StandardCharsets.UTF_16);
		testCharset(StandardCharsets.UTF_16BE);
		testCharset(StandardCharsets.UTF_16LE);
	}

	private void testCharset(Charset charset) {
		// get the bytes for this string
		byte[] bytes = TEST_UTF.getBytes(charset);

		// write the complicated UTF string one byte at a time to an OutputStream
		String byteByByteOutputStream = StringPrinter.buildString(printer -> {
			Errors.rethrow().run(() -> {
				OutputStream output = printer.toOutputStream(charset);
				for (byte b : bytes) {
					output.write(b);
				}
			});
		});
		Assert.assertEquals(TEST_UTF, byteByByteOutputStream);

		// write the complicated UTF string one byte at a time to a PrintStream
		String byteByBytePrintStream = StringPrinter.buildString(printer -> {
			Errors.rethrow().run(() -> {
				OutputStream output = printer.toPrintStream(charset);
				for (byte b : bytes) {
					output.write(b);
				}
			});
		});
		Assert.assertEquals(TEST_UTF, byteByBytePrintStream);
	}

	@Test
	public void testStringsToLines() {
		// requires assembly
		testCaseStringsToLines("some\nsimple lines\n",
				"some", "\n", "simple ", "lines", "\n");
		// requires splitting
		testCaseStringsToLines("some\nsimple lines\n",
				"some\nsimple lines\n");
		// ensure requires newline
		testCaseStringsToLines("no newline\n",
				"no newline\nno output");
	}

	private void testCaseStringsToLines(String expected, String... inputs) {
		// assemble the result
		StringBuilder result = new StringBuilder();
		// create a harness for converting strings to lines
		Consumer<String> underTest = StringPrinter.stringsToLines(perLine -> {
			// there should be no newline
			Assert.assertEquals(-1, perLine.indexOf('\n'));
			// we'll append the result to StringBuilder
			result.append(perLine);
			result.append('\n');
		});
		// feed the input to the test harness
		Arrays.asList(inputs).forEach(underTest::accept);

		Assert.assertEquals(expected, result.toString());
	}
}
