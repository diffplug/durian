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

import static java.util.Objects.requireNonNull;

import java.util.Comparator;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/** Safe representation of the result of a comparison (better than int). */
public enum Comparison {
	LESSER, EQUAL, GREATER;

	/** Returns the appropriate T based on the Comparison value. */
	@CheckReturnValue
	@Nullable
	public <T> T lesserEqualGreater(@Nullable T lesser, @Nullable T equal, @Nullable T greater) {
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

	/** Compares the two {@link Comparable}s and returns the result. */
	@CheckReturnValue
	public static <T extends Comparable<T>> Comparison compare(T a, T b) {
		requireNonNull(a);
		requireNonNull(b);
		return from(a.compareTo(b));
	}

	/** Compares the two objects using the {@link Comparator}. */
	@CheckReturnValue
	public static <T> Comparison compare(Comparator<T> comparator, @Nullable T a, @Nullable T b) {
		return from(comparator.compare(a, b));
	}

	/** Returns a Comparison from the given result of a call to {@link Comparable#compareTo(Object)} or {@link Comparator#compare(Object, Object)}. */
	@SuppressFBWarnings(value = "UC_USELESS_CONDITION", justification = "Throwing Unhandled keeps the full-enumeration more explicit.")
	@CheckReturnValue
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
