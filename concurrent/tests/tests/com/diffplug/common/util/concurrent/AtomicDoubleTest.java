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
package com.diffplug.common.util.concurrent;

import junit.framework.*;

/**
 * Unit test for {@link AtomicDouble}.
 */
public class AtomicDoubleTest extends JSR166TestCase {

	private static final double[] VALUES = {
			Double.NEGATIVE_INFINITY,
			-Double.MAX_VALUE,
			(double) Long.MIN_VALUE,
			(double) Integer.MIN_VALUE,
			-Math.PI,
			-1.0,
			-Double.MIN_VALUE,
			-0.0,
			+0.0,
			Double.MIN_VALUE,
			1.0,
			Math.PI,
			(double) Integer.MAX_VALUE,
			(double) Long.MAX_VALUE,
			Double.MAX_VALUE,
			Double.POSITIVE_INFINITY,
			Double.NaN,
			Float.MAX_VALUE,
	};

	/** The notion of equality used by AtomicDouble */
	static boolean bitEquals(double x, double y) {
		return Double.doubleToRawLongBits(x) == Double.doubleToRawLongBits(y);
	}

	static void assertBitEquals(double x, double y) {
		assertEquals(Double.doubleToRawLongBits(x),
				Double.doubleToRawLongBits(y));
	}

	/**
	 * constructor initializes to given value
	 */
	public void testConstructor() {
		for (double x : VALUES) {
			AtomicDouble a = new AtomicDouble(x);
			assertBitEquals(x, a.get());
		}
	}

	/**
	 * default constructed initializes to zero
	 */
	public void testConstructor2() {
		AtomicDouble a = new AtomicDouble();
		assertBitEquals(0.0, a.get());
	}

	/**
	 * get returns the last value set
	 */
	public void testGetSet() {
		AtomicDouble at = new AtomicDouble(1.0);
		assertBitEquals(1.0, at.get());
		for (double x : VALUES) {
			at.set(x);
			assertBitEquals(x, at.get());
		}
	}

	/**
	 * get returns the last value lazySet in same thread
	 */
	public void testGetLazySet() {
		AtomicDouble at = new AtomicDouble(1.0);
		assertBitEquals(1.0, at.get());
		for (double x : VALUES) {
			at.lazySet(x);
			assertBitEquals(x, at.get());
		}
	}

	/**
	 * compareAndSet succeeds in changing value if equal to expected else fails
	 */
	public void testCompareAndSet() {
		double prev = Math.E;
		double unused = Math.E + Math.PI;
		AtomicDouble at = new AtomicDouble(prev);
		for (double x : VALUES) {
			assertBitEquals(prev, at.get());
			assertFalse(at.compareAndSet(unused, x));
			assertBitEquals(prev, at.get());
			assertTrue(at.compareAndSet(prev, x));
			assertBitEquals(x, at.get());
			prev = x;
		}
	}

	/**
	 * compareAndSet in one thread enables another waiting for value
	 * to succeed
	 */

	public void testCompareAndSetInMultipleThreads() throws Exception {
		final AtomicDouble at = new AtomicDouble(1.0);
		Thread t = newStartedThread(new CheckedRunnable() {
			public void realRun() {
				while (!at.compareAndSet(2.0, 3.0)) {
					Thread.yield();
				}
			}
		});

		assertTrue(at.compareAndSet(1.0, 2.0));
		awaitTermination(t);
		assertBitEquals(3.0, at.get());
	}

	/**
	 * repeated weakCompareAndSet succeeds in changing value when equal
	 * to expected
	 */
	public void testWeakCompareAndSet() {
		double prev = Math.E;
		double unused = Math.E + Math.PI;
		AtomicDouble at = new AtomicDouble(prev);
		for (double x : VALUES) {
			assertBitEquals(prev, at.get());
			assertFalse(at.weakCompareAndSet(unused, x));
			assertBitEquals(prev, at.get());
			while (!at.weakCompareAndSet(prev, x)) {
				;
			}
			assertBitEquals(x, at.get());
			prev = x;
		}
	}

	/**
	 * getAndSet returns previous value and sets to given value
	 */
	public void testGetAndSet() {
		double prev = Math.E;
		AtomicDouble at = new AtomicDouble(prev);
		for (double x : VALUES) {
			assertBitEquals(prev, at.getAndSet(x));
			prev = x;
		}
	}

	/**
	 * getAndAdd returns previous value and adds given value
	 */
	public void testGetAndAdd() {
		for (double x : VALUES) {
			for (double y : VALUES) {
				AtomicDouble a = new AtomicDouble(x);
				double z = a.getAndAdd(y);
				assertBitEquals(x, z);
				assertBitEquals(x + y, a.get());
			}
		}
	}

	/**
	 * addAndGet adds given value to current, and returns current value
	 */
	public void testAddAndGet() {
		for (double x : VALUES) {
			for (double y : VALUES) {
				AtomicDouble a = new AtomicDouble(x);
				double z = a.addAndGet(y);
				assertBitEquals(x + y, z);
				assertBitEquals(x + y, a.get());
			}
		}
	}

	/**
	 * a deserialized serialized atomic holds same value
	 */
	public void testSerialization() throws Exception {
		AtomicDouble a = new AtomicDouble();
		AtomicDouble b = serialClone(a);
		assertNotSame(a, b);
		a.set(-22.0);
		AtomicDouble c = serialClone(a);
		assertNotSame(b, c);
		assertBitEquals(-22.0, a.get());
		assertBitEquals(0.0, b.get());
		assertBitEquals(-22.0, c.get());
		for (double x : VALUES) {
			AtomicDouble d = new AtomicDouble(x);
			assertBitEquals(serialClone(d).get(), d.get());
		}
	}

	/**
	 * toString returns current value
	 */
	public void testToString() {
		AtomicDouble at = new AtomicDouble();
		assertEquals("0.0", at.toString());
		for (double x : VALUES) {
			at.set(x);
			assertEquals(Double.toString(x), at.toString());
		}
	}

	/**
	 * intValue returns current value.
	 */
	public void testIntValue() {
		AtomicDouble at = new AtomicDouble();
		assertEquals(0, at.intValue());
		for (double x : VALUES) {
			at.set(x);
			assertEquals((int) x, at.intValue());
		}
	}

	/**
	 * longValue returns current value.
	 */
	public void testLongValue() {
		AtomicDouble at = new AtomicDouble();
		assertEquals(0L, at.longValue());
		for (double x : VALUES) {
			at.set(x);
			assertEquals((long) x, at.longValue());
		}
	}

	/**
	 * floatValue returns current value.
	 */
	public void testFloatValue() {
		AtomicDouble at = new AtomicDouble();
		assertEquals(0.0f, at.floatValue());
		for (double x : VALUES) {
			at.set(x);
			assertEquals((float) x, at.floatValue());
		}
	}

	/**
	 * doubleValue returns current value.
	 */
	public void testDoubleValue() {
		AtomicDouble at = new AtomicDouble();
		assertEquals(0.0d, at.doubleValue());
		for (double x : VALUES) {
			at.set(x);
			assertBitEquals(x, at.doubleValue());
		}
	}

	/**
	 * compareAndSet treats +0.0 and -0.0 as distinct values
	 */
	public void testDistinctZeros() {
		AtomicDouble at = new AtomicDouble(+0.0);
		assertFalse(at.compareAndSet(-0.0, 7.0));
		assertFalse(at.weakCompareAndSet(-0.0, 7.0));
		assertBitEquals(+0.0, at.get());
		assertTrue(at.compareAndSet(+0.0, -0.0));
		assertBitEquals(-0.0, at.get());
		assertFalse(at.compareAndSet(+0.0, 7.0));
		assertFalse(at.weakCompareAndSet(+0.0, 7.0));
		assertBitEquals(-0.0, at.get());
	}
}
