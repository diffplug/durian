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

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;

import com.diffplug.common.base.Consumers;
import com.diffplug.common.base.Converter;
import com.diffplug.common.base.ConverterNullable;

public class ImmutablesTest {
	////////////
	// Mutate //
	////////////
	@Test
	public void mutateList() {
		Assert.assertEquals(ImmutableList.of(), Immutables.mutateList(ImmutableList.of(1), List::clear));
		Assert.assertEquals(ImmutableList.of(1), Immutables.mutateList(ImmutableList.of(1), Consumers.doNothing()));
		Assert.assertEquals(ImmutableList.of(1), Immutables.mutateList(ImmutableList.of(), list -> list.add(1)));
	}

	@Test
	public void mutateSet() {
		Assert.assertEquals(ImmutableSet.of(), Immutables.mutateSet(ImmutableSet.of(1), Set::clear));
		Assert.assertEquals(ImmutableSet.of(1), Immutables.mutateSet(ImmutableSet.of(1), Consumers.doNothing()));
		Assert.assertEquals(ImmutableSet.of(1), Immutables.mutateSet(ImmutableSet.of(), set -> set.add(1)));
	}

	@Test
	public void mutateMap() {
		Assert.assertEquals(ImmutableMap.of(), Immutables.mutateMap(ImmutableMap.of(1, 1), Map::clear));
		Assert.assertEquals(ImmutableMap.of(1, 1), Immutables.mutateMap(ImmutableMap.of(1, 1), Consumers.doNothing()));
		Assert.assertEquals(ImmutableMap.of(1, 1), Immutables.mutateMap(ImmutableMap.of(), map -> map.put(1, 1)));
	}

	@Test
	public void mutateSortedSet() {
		ImmutableSortedSet<Integer> naturalEmpty = ImmutableSortedSet.<Integer> orderedBy(Comparator.naturalOrder()).build();
		ImmutableSortedSet<Integer> reverseEmpty = ImmutableSortedSet.<Integer> orderedBy(Comparator.reverseOrder()).build();

		// make sure mutation preserves the underlying comparator
		UnaryOperator<ImmutableSortedSet<Integer>> add213 = Immutables.mutatorSortedSet(set -> set.addAll(Arrays.asList(2, 1, 3)));
		ImmutableSortedSet<Integer> natural = add213.apply(naturalEmpty);
		ImmutableSortedSet<Integer> reverse = add213.apply(reverseEmpty);

		Assert.assertEquals(natural.asList(), ImmutableList.of(1, 2, 3));
		Assert.assertEquals(reverse.asList(), ImmutableList.of(3, 2, 1));
	}

	@Test
	public void mutateSortedMap() {
		ImmutableSortedMap<Integer, String> naturalEmpty = ImmutableSortedMap.<Integer, String> orderedBy(Comparator.naturalOrder()).build();
		ImmutableSortedMap<Integer, String> reverseEmpty = ImmutableSortedMap.<Integer, String> orderedBy(Comparator.reverseOrder()).build();

		// make sure mutation preserves the underlying comparator
		UnaryOperator<ImmutableSortedMap<Integer, String>> add213 = Immutables.mutatorSortedMap(map -> {
			map.put(2, "2");
			map.put(1, "1");
			map.put(3, "3");
		});
		ImmutableSortedMap<Integer, String> natural = add213.apply(naturalEmpty);
		ImmutableSortedMap<Integer, String> reverse = add213.apply(reverseEmpty);

		Assert.assertEquals(natural.keySet().asList(), ImmutableList.of(1, 2, 3));
		Assert.assertEquals(reverse.keySet().asList(), ImmutableList.of(3, 2, 1));
	}

	@Test
	public void mutateBiMap() {
		Assert.assertEquals(ImmutableBiMap.of(), Immutables.mutateBiMap(ImmutableBiMap.of(1, "1"), Map::clear));
		Assert.assertEquals(ImmutableBiMap.of(1, "1"), Immutables.mutateBiMap(ImmutableBiMap.of(1, "1"), Consumers.doNothing()));
		Assert.assertEquals(ImmutableBiMap.of(1, "1"), Immutables.mutateBiMap(ImmutableBiMap.of(), map -> map.put(1, "1")));
	}

	//////////////////////////
	// Per-element mutation //
	//////////////////////////
	private ConverterNullable<String, Integer> positiveIntParser() {
		return ConverterNullable.from(str -> {
			try {
				int value = Integer.parseInt(str);
				return value < 0 ? null : value;
			} catch (IllegalArgumentException e) {
				return null;
			}
		}, i -> i < 0 ? null : Integer.toString(i), "intParser");
	}

	@Test
	public void testPerElementConverterOpt() {
		Converter<Optional<String>, Optional<Integer>> optConverter = Immutables.perElementConverterOpt(positiveIntParser());
		// forward
		Assert.assertEquals(Optional.empty(), optConverter.convert(Optional.empty()));
		Assert.assertEquals(Optional.of(1), optConverter.convert(Optional.of("1")));
		Assert.assertEquals(Optional.empty(), optConverter.convert(Optional.of("a")));
		// backwards
		Assert.assertEquals(Optional.empty(), optConverter.revert(Optional.empty()));
		Assert.assertEquals(Optional.of("1"), optConverter.revert(Optional.of(1)));
		Assert.assertEquals(Optional.empty(), optConverter.revert(Optional.of(-1)));
	}

	@Test
	public void testPerElementConverterSet() {
		Converter<ImmutableSet<String>, ImmutableSet<Integer>> setConverter = Immutables.perElementConverterSet(positiveIntParser());
		// forward
		Assert.assertEquals(ImmutableSet.of(), setConverter.convert(ImmutableSet.of()));
		Assert.assertEquals(ImmutableSet.of(1), setConverter.convert(ImmutableSet.of("1")));
		Assert.assertEquals(ImmutableSet.of(), setConverter.convert(ImmutableSet.of("a")));
		// backwards
		Assert.assertEquals(ImmutableSet.of(), setConverter.revert(ImmutableSet.of()));
		Assert.assertEquals(ImmutableSet.of("1"), setConverter.revert(ImmutableSet.of(1)));
		Assert.assertEquals(ImmutableSet.of(), setConverter.revert(ImmutableSet.of(-1)));
	}

	@Test
	public void testPerElementConverterList() {
		Converter<ImmutableList<String>, ImmutableList<Integer>> listConverter = Immutables.perElementConverterList(positiveIntParser());
		// forward
		Assert.assertEquals(ImmutableList.of(), listConverter.convert(ImmutableList.of()));
		Assert.assertEquals(ImmutableList.of(1), listConverter.convert(ImmutableList.of("1")));
		Assert.assertEquals(ImmutableList.of(), listConverter.convert(ImmutableList.of("a")));
		// backwards
		Assert.assertEquals(ImmutableList.of(), listConverter.revert(ImmutableList.of()));
		Assert.assertEquals(ImmutableList.of("1"), listConverter.revert(ImmutableList.of(1)));
		Assert.assertEquals(ImmutableList.of(), listConverter.revert(ImmutableList.of(-1)));
	}

	////////////////////////
	// Optional <-> Stuff //
	////////////////////////
	@Test
	public void testOptionalToSet() {
		Assert.assertEquals(ImmutableSet.of(), Immutables.optionalToSet(Optional.empty()));
		Assert.assertEquals(ImmutableSet.of("A"), Immutables.optionalToSet(Optional.of("A")));
	}

	@Test
	public void testOptionalFrom() {
		Assert.assertEquals(Optional.empty(), Immutables.optionalFrom(ImmutableSet.of()));
		Assert.assertEquals(Optional.empty(), Immutables.optionalFrom(ImmutableList.of()));
		Assert.assertEquals(Optional.of("A"), Immutables.optionalFrom(ImmutableSet.of("A")));
		Assert.assertEquals(Optional.of("A"), Immutables.optionalFrom(ImmutableList.of("A")));
		try {
			Immutables.optionalFrom(ImmutableSet.of("A", "B"));
			Assert.fail("Expected exception");
		} catch (IllegalArgumentException e) {}
		try {
			Immutables.optionalFrom(ImmutableList.of("A", "B"));
			Assert.fail("Expected exception");
		} catch (IllegalArgumentException e) {}
	}

	///////////////////////
	// Java 8 Collectors //
	///////////////////////
	private List<Integer> zeroTo100() {
		return IntStream.range(0, 100).boxed().collect(Collectors.toList());
	}

	@Test
	public void testToList() {
		Assert.assertEquals(ImmutableList.copyOf(zeroTo100()), zeroTo100().parallelStream().collect(Immutables.toList()));
	}

	@Test
	public void testToSet() {
		ImmutableSet<Integer> set = ImmutableSet.copyOf(zeroTo100());
		List<Integer> zeroTo100x3 = Lists.newArrayList();
		for (int i = 0; i < 3; ++i) {
			zeroTo100x3.addAll(zeroTo100());
		}
		Assert.assertEquals(set, zeroTo100x3.parallelStream().collect(Immutables.toSet()));
	}

	@Test
	public void testToSortedSet() {
		List<Integer> values = zeroTo100();
		Collections.shuffle(values, new Random(0));

		List<Integer> zeroTo100x3 = Lists.newArrayList();
		for (int i = 0; i < 3; ++i) {
			zeroTo100x3.addAll(zeroTo100());
		}
		Assert.assertEquals(ImmutableSet.copyOf(zeroTo100()), zeroTo100x3.parallelStream().collect(Immutables.toSortedSet(Comparator.naturalOrder())));
	}

	@Test
	public void testToSortedSetReverse() {
		List<Integer> hundredToZero = zeroTo100();
		Collections.reverse(hundredToZero);
		Assert.assertEquals(hundredToZero, zeroTo100().parallelStream().collect(Immutables.toSortedSet(Comparator.reverseOrder())).asList());
	}

	@Test
	public void testToMap() {
		ImmutableMap<Integer, Integer> expected = ImmutableMap.of(1, 1);
		Assert.assertEquals(expected, expected.entrySet().parallelStream().collect(Immutables.toMap(Map.Entry::getKey, Map.Entry::getValue)));
	}

	@Test
	public void testToSortedMap() {
		ImmutableMap<Integer, Integer> expected = ImmutableMap.of(1, 1, 2, 2);
		Assert.assertEquals(expected.entrySet().asList(), expected.entrySet().parallelStream().collect(Immutables.toSortedMap(Comparator.naturalOrder(), Map.Entry::getKey, Map.Entry::getValue)).entrySet().asList());
	}

	@Test
	public void testToSortedMapReversed() {
		ImmutableMap<Integer, Integer> expected = ImmutableMap.of(2, 2, 1, 1);
		Assert.assertEquals(expected.entrySet().asList(), expected.entrySet().parallelStream().collect(Immutables.toSortedMap(Ordering.natural().reverse(), Map.Entry::getKey, Map.Entry::getValue)).entrySet().asList());
	}

	@Test
	public void testToBiMap() {
		ImmutableBiMap<Integer, Integer> expected = ImmutableBiMap.of(2, 2, 1, 1);
		Assert.assertEquals(expected.entrySet().asList(), expected.entrySet().parallelStream().collect(Immutables.toBiMap(Map.Entry::getKey, Map.Entry::getValue)).entrySet().asList());
	}
}
