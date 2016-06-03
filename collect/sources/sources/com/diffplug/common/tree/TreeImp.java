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
package com.diffplug.common.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

final class TreeImp {
	private TreeImp() {}

	/** Returns a filtered version of the given list. */
	static <T> List<T> filteredList(List<T> unfiltered, Predicate<T> filter) {
		// iterate over the list and count how many pass
		int numPassed = 0;
		for (T element : unfiltered) {
			if (filter.test(element)) {
				++numPassed;
			}
		}

		if (numPassed == 0) {
			// if none passed, then we can use an empty list
			return Collections.emptyList();
		} else if (numPassed == unfiltered.size()) {
			// if they all passed, then we can use the original list
			return unfiltered;
		} else {
			// and if it's a mixture, then we can make an
			// appropriately-sized list and copy the right
			// stuff into it
			ArrayList<T> result = new ArrayList<T>(numPassed);
			for (T element : unfiltered) {
				if (filter.test(element)) {
					result.add(element);
				}
			}
			return result;
		}
	}
}
