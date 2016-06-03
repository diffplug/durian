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

import static com.google.common.truth.Truth.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import org.junit.Test;

import com.diffplug.common.base.Predicates;

public class TreeImpTest {
	@Test
	public void testFilterAllOrNone() {
		testCaseFilterAllOrNone(Collections.emptyList());
		testCaseFilterAllOrNone(Arrays.asList(1));
		testCaseFilterAllOrNone(Arrays.asList(1, 2));
		testCaseFilterAllOrNone(Arrays.asList(1, 2, 3));
	}

	private void testCaseFilterAllOrNone(List<Integer> orig) {
		// all fail, list should be empty
		List<Integer> noneOf = TreeImp.filteredList(orig, Predicates.alwaysFalse());
		assertThat(noneOf).isEmpty();
		// all pass, the list should be identicaly
		List<Integer> allOf = TreeImp.filteredList(orig, Predicates.alwaysTrue());
		assertThat(allOf).isSameAs(orig);
	}

	@Test
	public void testFilterSpecific() {
		// keep and remove beginning
		testCaseFilterSpecific(e -> e == 1, 1);
		testCaseFilterSpecific(e -> e != 1, 2, 3);
		// keep and remove middle
		testCaseFilterSpecific(e -> e == 2, 2);
		testCaseFilterSpecific(e -> e != 2, 1, 3);
		// keep and remove end
		testCaseFilterSpecific(e -> e == 3, 3);
		testCaseFilterSpecific(e -> e != 3, 1, 2);
	}

	private void testCaseFilterSpecific(Predicate<Integer> predicate, Object... result) {
		assertThat(TreeImp.filteredList(Arrays.asList(1, 2, 3), predicate)).containsExactly(result);
	}
}
