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

import org.junit.Assert;
import org.junit.Test;

public class ComparisonTest {
	@Test
	public void testPatternMatch() {
		Assert.assertEquals("lesser", Comparison.LESSER.lesserEqualGreater("lesser", "equal", "greater"));
		Assert.assertEquals("equal", Comparison.EQUAL.lesserEqualGreater("lesser", "equal", "greater"));
		Assert.assertEquals("greater", Comparison.GREATER.lesserEqualGreater("lesser", "equal", "greater"));
	}

	@Test
	public void testFrom() {
		Assert.assertEquals(Comparison.LESSER, Comparison.from(new Integer(1).compareTo(2)));
		Assert.assertEquals(Comparison.EQUAL, Comparison.from(new Integer(1).compareTo(1)));
		Assert.assertEquals(Comparison.GREATER, Comparison.from(new Integer(2).compareTo(1)));
	}

	@Test
	public void testCompare() {
		Assert.assertEquals(Comparison.LESSER, Comparison.compare(1, 2));
		Assert.assertEquals(Comparison.EQUAL, Comparison.compare(1, 1));
		Assert.assertEquals(Comparison.GREATER, Comparison.compare(2, 1));
	}
}
