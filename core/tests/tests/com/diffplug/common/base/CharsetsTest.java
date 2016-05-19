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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import junit.framework.TestCase;

import com.diffplug.common.annotations.GwtCompatible;
import com.diffplug.common.annotations.GwtIncompatible;

/**
 * Unit test for {@link Charsets}.
 *
 * @author Mike Bostock
 */
@GwtCompatible(emulated = true)
public class CharsetsTest extends TestCase {

	@GwtIncompatible("Non-UTF-8 Charset")
	public void testUsAscii() {
		assertEquals(Charset.forName("US-ASCII"), StandardCharsets.US_ASCII);
	}

	@GwtIncompatible("Non-UTF-8 Charset")
	public void testIso88591() {
		assertEquals(Charset.forName("ISO-8859-1"), StandardCharsets.ISO_8859_1);
	}

	public void testUtf8() {
		assertEquals(Charset.forName("UTF-8"), StandardCharsets.UTF_8);
	}

	@GwtIncompatible("Non-UTF-8 Charset")
	public void testUtf16be() {
		assertEquals(Charset.forName("UTF-16BE"), StandardCharsets.UTF_16BE);
	}

	@GwtIncompatible("Non-UTF-8 Charset")
	public void testUtf16le() {
		assertEquals(Charset.forName("UTF-16LE"), StandardCharsets.UTF_16LE);
	}

	@GwtIncompatible("Non-UTF-8 Charset")
	public void testUtf16() {
		assertEquals(Charset.forName("UTF-16"), StandardCharsets.UTF_16);
	}

	@GwtIncompatible("Non-UTF-8 Charset")
	public void testWhyUsAsciiIsDangerous() {
		byte[] b1 = "朝日新聞".getBytes(StandardCharsets.US_ASCII);
		byte[] b2 = "聞朝日新".getBytes(StandardCharsets.US_ASCII);
		byte[] b3 = "????".getBytes(StandardCharsets.US_ASCII);
		byte[] b4 = "ニュース".getBytes(StandardCharsets.US_ASCII);
		byte[] b5 = "スューー".getBytes(StandardCharsets.US_ASCII);
		// Assert they are all equal (using the transitive property)
		assertTrue(Arrays.equals(b1, b2));
		assertTrue(Arrays.equals(b2, b3));
		assertTrue(Arrays.equals(b3, b4));
		assertTrue(Arrays.equals(b4, b5));
	}
}
