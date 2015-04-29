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

import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Some useful collectors which aren't included as a Java 8 built-in. */
public class MoreCollectors {
	/**
	 * Collector which traverses a stream and returns either a single element
	 * (if there was only one element) or empty (if there were 0 or more than 1
	 * elements).  It traverses the entire stream, even if two elements
	 * have been encountered and the empty return value is now certain. 
	 * 
	 * Implementation credit to Misha: http://stackoverflow.com/a/26812693/1153071
	 */
	public static <T> Collector<T, ?, Optional<T>> singleOrEmpty() {
		return Collectors.collectingAndThen(Collectors.toList(),
				lst -> lst.size() == 1 ? Optional.of(lst.get(0)) : Optional.empty());
	}

	/**
	 * Same behavior as MoreCollectors.singleOrEmpty(), except that it returns
	 * early if it is possible to do so.  Unfortunately, it is not possible
	 * to implement early-return behavior using the Collector interface,
	 * so MoreCollectors takes the stream as an argument.
	 * 
	 * Implementation credit to Thomas Jungblut: http://stackoverflow.com/a/26810932/1153071
	 */
	public static <T> Optional<T> singleOrEmptyShortCircuiting(Stream<T> stream) {
		return stream.limit(2).map(Optional::ofNullable).reduce(Optional.empty(),
				(a, b) -> a.isPresent() ^ b.isPresent() ? b : Optional.empty());
	}
}
