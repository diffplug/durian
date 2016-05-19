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

import java.util.Objects;

import junit.framework.TestCase;

import com.diffplug.common.annotations.GwtCompatible;

/**
 * Tests for {@link Objects}.
 *
 * @author Laurence Gonsalves
 */
@GwtCompatible(emulated = true)
public class ObjectsTest extends TestCase {

	public void testEqual() throws Exception {
		assertTrue(Objects.equals(1, 1));
		assertTrue(Objects.equals(null, null));

		// test distinct string objects
		String s1 = "foobar";
		String s2 = new String(s1);
		assertTrue(Objects.equals(s1, s2));

		assertFalse(Objects.equals(s1, null));
		assertFalse(Objects.equals(null, s1));
		assertFalse(Objects.equals("foo", "bar"));
		assertFalse(Objects.equals("1", 1));
	}

	public void testHashCode() throws Exception {
		int h1 = Objects.hash(1, "two", 3.0);
		int h2 = Objects.hash(
				new Integer(1), new String("two"), new Double(3.0));
		// repeatable
		assertEquals(h1, h2);

		// These don't strictly need to be true, but they're nice properties.
		assertTrue(Objects.hash(1, 2, null) != Objects.hash(1, 2));
		assertTrue(Objects.hash(1, 2, null) != Objects.hash(1, null, 2));
		assertTrue(Objects.hash(1, null, 2) != Objects.hash(1, 2));
		assertTrue(Objects.hash(1, 2, 3) != Objects.hash(3, 2, 1));
		assertTrue(Objects.hash(1, 2, 3) != Objects.hash(2, 3, 1));
	}

	public void testFirstNonNull_withNonNull() throws Exception {
		String s1 = "foo";
		String s2 = MoreObjects.firstNonNull(s1, "bar");
		assertSame(s1, s2);

		Long n1 = new Long(42);
		Long n2 = MoreObjects.firstNonNull(null, n1);
		assertSame(n1, n2);
	}

	@SuppressWarnings("CheckReturnValue")
	public void testFirstNonNull_throwsNullPointerException() throws Exception {
		try {
			MoreObjects.firstNonNull(null, null);
			fail("expected NullPointerException");
		} catch (NullPointerException expected) {}
	}
}
