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

import java.util.Comparator;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/** Safe representation of the result of a comparison (better than int). */
public enum Comparison {
	LESSER, EQUAL, GREATER;

	/** Returns the appropriate T based on the Comparison value. */
	public <T> T lesserEqualGreater(T lesser, T equal, T greater) {
		switch (this) {
		case LESSER:
			return lesser;
		case EQUAL:
			return equal;
		case GREATER:
			return greater;
		default:
			throw Unhandled.enumException(this);
		}
	}

	/** Returns a Comparison result from the two given Comparables. */
	public static <T extends Comparable<T>> Comparison compare(T a, T b) {
		return from(a.compareTo(b));
	}

	/** Returns a Comparison result from the two values using a Comparator. */
	public static <T> Comparison compare(Comparator<T> comparator, T a, T b) {
		return from(comparator.compare(a, b));
	}

	/** Returns a Comparison from the given result of a call to <code>Comparable.compareTo()</code> or <code>Comparator.compare</code>. */
	@SuppressFBWarnings(value = "UC_USELESS_CONDITION", justification = "Throwing Unhandled keeps the full-enumeration more explicit.")
	public static Comparison from(int compareToResult) {
		if (compareToResult == 0) {
			return Comparison.EQUAL;
		} else if (compareToResult < 0) {
			return Comparison.LESSER;
		} else if (compareToResult > 0) {
			return Comparison.GREATER;
		} else {
			throw Unhandled.integerException(compareToResult);
		}
	}
}
