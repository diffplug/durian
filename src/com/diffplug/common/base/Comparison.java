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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/** Utility class to make reading the result of "compareTo" results a little easier. */
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

	/** Returns a Comparison result from the two given comparables. */
	public static <T extends Comparable<T>> Comparison compare(T a, T b) {
		return from(a.compareTo(b));
	}

	/** Returns a Comparison from the given result of Comparable.compareTo(). */
	@SuppressFBWarnings(value = "UC_USELESS_CONDITION", justification = "Throwing Unhandled keeps the code more explicit.")
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
