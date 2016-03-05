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
package com.diffplug.common.collect.testing.google;

import static com.diffplug.common.collect.testing.features.CollectionFeature.SUPPORTS_ITERATOR_REMOVE;
import static com.diffplug.common.collect.testing.features.CollectionSize.ZERO;
import static com.diffplug.common.collect.testing.features.MapFeature.SUPPORTS_REMOVE;

import java.util.Iterator;

import com.diffplug.common.annotations.GwtCompatible;
import com.diffplug.common.collect.testing.features.CollectionFeature;
import com.diffplug.common.collect.testing.features.CollectionSize;
import com.diffplug.common.collect.testing.features.MapFeature;

/**
 * Tester for {@code BiMap.remove}.
 *
 * @author Louis Wasserman
 */
@GwtCompatible
public class BiMapRemoveTester<K, V> extends AbstractBiMapTester<K, V> {
	@SuppressWarnings("unchecked")
	@MapFeature.Require(SUPPORTS_REMOVE)
	@CollectionSize.Require(absent = ZERO)
	public void testRemoveKeyRemovesFromInverse() {
		getMap().remove(k0());
		expectMissing(e0());
	}

	@SuppressWarnings("unchecked")
	@MapFeature.Require(SUPPORTS_REMOVE)
	@CollectionSize.Require(absent = ZERO)
	public void testRemoveKeyFromKeySetRemovesFromInverse() {
		getMap().keySet().remove(k0());
		expectMissing(e0());
	}

	@SuppressWarnings("unchecked")
	@MapFeature.Require(SUPPORTS_REMOVE)
	@CollectionSize.Require(absent = ZERO)
	public void testRemoveFromValuesRemovesFromInverse() {
		getMap().values().remove(v0());
		expectMissing(e0());
	}

	@SuppressWarnings("unchecked")
	@MapFeature.Require(SUPPORTS_REMOVE)
	@CollectionSize.Require(absent = ZERO)
	public void testRemoveFromInverseRemovesFromForward() {
		getMap().inverse().remove(v0());
		expectMissing(e0());
	}

	@SuppressWarnings("unchecked")
	@MapFeature.Require(SUPPORTS_REMOVE)
	@CollectionSize.Require(absent = ZERO)
	public void testRemoveFromInverseKeySetRemovesFromForward() {
		getMap().inverse().keySet().remove(v0());
		expectMissing(e0());
	}

	@SuppressWarnings("unchecked")
	@MapFeature.Require(SUPPORTS_REMOVE)
	@CollectionSize.Require(absent = ZERO)
	public void testRemoveFromInverseValuesRemovesFromInverse() {
		getMap().inverse().values().remove(k0());
		expectMissing(e0());
	}

	@CollectionFeature.Require(SUPPORTS_ITERATOR_REMOVE)
	@CollectionSize.Require(absent = ZERO)
	public void testKeySetIteratorRemove() {
		int initialSize = getNumElements();
		Iterator<K> iterator = getMap().keySet().iterator();
		iterator.next();
		iterator.remove();
		assertEquals(initialSize - 1, getMap().size());
		assertEquals(initialSize - 1, getMap().inverse().size());
	}
}
