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
package com.diffplug.common.collect;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.diffplug.common.annotations.GwtCompatible;
import com.diffplug.common.annotations.GwtIncompatible;
import com.diffplug.common.collect.MutableClassToInstanceMapConstraintsTest.TestKeyException;
import com.diffplug.common.collect.MutableClassToInstanceMapConstraintsTest.TestValueException;
import com.diffplug.common.collect.testing.MapTestSuiteBuilder;
import com.diffplug.common.collect.testing.TestStringMapGenerator;
import com.diffplug.common.collect.testing.features.CollectionFeature;
import com.diffplug.common.collect.testing.features.CollectionSize;
import com.diffplug.common.collect.testing.features.MapFeature;

/**
 * Tests for {@link MutableClassToInstanceMapConstraints#constrainedMap}.
 *
 * @author Jared Levy
 * @author Louis Wasserman
 */
@GwtCompatible(emulated = true)
public class ConstrainedMapTest extends TestCase {

	private static final String TEST_KEY = "42";
	private static final String TEST_VALUE = "test";
	private static final MutableClassToInstanceMapConstraint<String, String> TEST_CONSTRAINT = new TestConstraint();

	@GwtIncompatible("suite")
	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTest(MapTestSuiteBuilder
				.using(new ConstrainedMapGenerator())
				.named("Maps.constrainedMap[HashMap]")
				.withFeatures(
						CollectionSize.ANY,
						MapFeature.ALLOWS_NULL_KEYS,
						MapFeature.ALLOWS_NULL_VALUES,
						MapFeature.ALLOWS_ANY_NULL_QUERIES,
						MapFeature.GENERAL_PURPOSE,
						CollectionFeature.SUPPORTS_ITERATOR_REMOVE)
				.createTestSuite());
		suite.addTestSuite(ConstrainedMapTest.class);
		return suite;
	}

	public void testPutWithForbiddenKeyForbiddenValue() {
		Map<String, String> map = MutableClassToInstanceMapConstraints.constrainedMap(
				new HashMap<String, String>(),
				TEST_CONSTRAINT);
		try {
			map.put(TEST_KEY, TEST_VALUE);
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException expected) {
			// success
		}
	}

	public void testPutWithForbiddenKeyAllowedValue() {
		Map<String, String> map = MutableClassToInstanceMapConstraints.constrainedMap(
				new HashMap<String, String>(),
				TEST_CONSTRAINT);
		try {
			map.put(TEST_KEY, "allowed");
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException expected) {
			// success
		}
	}

	public void testPutWithAllowedKeyForbiddenValue() {
		Map<String, String> map = MutableClassToInstanceMapConstraints.constrainedMap(
				new HashMap<String, String>(),
				TEST_CONSTRAINT);
		try {
			map.put("allowed", TEST_VALUE);
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException expected) {
			// success
		}
	}

	public static final class ConstrainedMapGenerator extends TestStringMapGenerator {
		@Override
		protected Map<String, String> create(Entry<String, String>[] entries) {
			Map<String, String> map = MutableClassToInstanceMapConstraints.constrainedMap(
					new HashMap<String, String>(),
					TEST_CONSTRAINT);
			for (Entry<String, String> entry : entries) {
				map.put(entry.getKey(), entry.getValue());
			}
			return map;
		}
	}

	private static final class TestConstraint implements MutableClassToInstanceMapConstraint<String, String> {
		@Override
		public void checkKeyValue(String key, String value) {
			if (TEST_KEY.equals(key)) {
				throw new TestKeyException();
			}
			if (TEST_VALUE.equals(value)) {
				throw new TestValueException();
			}
		}

		private static final long serialVersionUID = 0;
	}
}
