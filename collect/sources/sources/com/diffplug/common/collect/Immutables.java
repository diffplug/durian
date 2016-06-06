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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;

import com.diffplug.common.base.Converter;
import com.diffplug.common.base.ConverterNullable;

/**
 * This class contains static methods for manipulating
 * and creating immutable collections.
 *
 * 1. Creating mutated copies of existing immutable collections
 * 2. Mapping Optional to/from ImmutableSet
 * 3. Java 8 Collectors for immutable collections
 *
 * ## Mutating existing immutable collections
 *
 * For each kind of `ImmutableCollection`, this class contains a static method:
 *
 * - `ImmutableCollection mutateCollection(ImmutableCollection source, Consumer<MutableCollection> mutator)`
 *
 * e.g. `ImmutableSet mutateSet(ImmutableSet source, Consumer<Set> mutator)` and so on for `List`, `Set`, `Map`, `SortedSet`, `SortedMap`, and `BiMap`.
 *
 * The methods work as follows:
 *
 * - Copy the `ImmutableCollection` into a new `MutableCollection`. (e.g. `ImmutableSet` into `LinkedHashSet`)
 * - Pass this `MutableCollection` to the `mutator`, which modifies it.
 * - Copy the (now-modified) `MutableCollection` into a new `ImmutableCollection`.
 * - Return this `ImmutableCollection`.
 *
 * There are also `UnaryOperator<ImmutableCollection> mutatorCollection(Consumer<MutableCollection> mutator)`
 * variants for each of the collection types.  These are a short hand for this:
 *
 * ```java
 * public static <T> UnaryOperator<ImmutableList<T>> mutatorList(Consumer<List<T>> mutator) {
 *     return input -> mutateList(input, mutator);
 * }
 * ```
 *
 * There also methods for per-element mutators.
 *
 * ## Mapping Optional to/from ImmutableSet
 *
 * - {@link #optionalFrom(ImmutableCollection)}
 * - {@link #optionalToSet(Optional)}
 *
 * ## Creating new immutable collections using Java 8 Collector's
 *
 * - {@link #toList()}
 * - {@link #toSet()}
 * - {@link #toMap(Function, Function)}
 * - {@link #toSortedSet(Comparator)}
 * - {@link #toSortedMap(Comparator, Function, Function)}
 * - {@link #toBiMap(Function, Function)}
 *
 * Each collector method also contains a version in which the constructor takes an `initialCapacity` argument, which helps with performance.
 */
public class Immutables {
	private Immutables() {}

	////////////
	// Mutate //
	////////////
	/** Returns a mutated version of the given list. */
	public static <T> ImmutableList<T> mutateList(ImmutableList<T> source, Consumer<List<T>> mutator) {
		List<T> mutable = new ArrayList<>(source);
		mutator.accept(mutable);
		return ImmutableList.copyOf(mutable);
	}

	/** Returns a mutated version of the given set. */
	public static <T> ImmutableSet<T> mutateSet(ImmutableSet<T> source, Consumer<Set<T>> mutator) {
		Set<T> mutable = new LinkedHashSet<>(source);
		mutator.accept(mutable);
		return ImmutableSet.copyOf(mutable);
	}

	/** Returns a mutated version of the given map. */
	public static <K, V> ImmutableMap<K, V> mutateMap(ImmutableMap<K, V> source, Consumer<Map<K, V>> mutator) {
		Map<K, V> mutable = new LinkedHashMap<>(source);
		mutator.accept(mutable);
		return ImmutableMap.copyOf(mutable);
	}

	/** Returns a mutated version of the given sorted set. */
	public static <T> ImmutableSortedSet<T> mutateSortedSet(ImmutableSortedSet<T> source, Consumer<NavigableSet<T>> mutator) {
		NavigableSet<T> mutable = new TreeSet<>(source);
		mutator.accept(mutable);
		return ImmutableSortedSet.copyOfSorted(mutable);
	}

	/** Returns a mutated version of the given sorted map. */
	public static <K, V> ImmutableSortedMap<K, V> mutateSortedMap(ImmutableSortedMap<K, V> source, Consumer<NavigableMap<K, V>> mutator) {
		NavigableMap<K, V> mutable = new TreeMap<>(source);
		mutator.accept(mutable);
		return ImmutableSortedMap.copyOfSorted(mutable);
	}

	/** Returns a mutated version of the given sorted map. */
	public static <K, V> ImmutableBiMap<K, V> mutateBiMap(ImmutableBiMap<K, V> source, Consumer<BiMap<K, V>> mutator) {
		BiMap<K, V> mutable = HashBiMap.create(source);
		mutator.accept(mutable);
		return ImmutableBiMap.copyOf(mutable);
	}

	/////////////
	// Mutator //
	/////////////
	/** Returns a function which mutates a list using the given mutator. */
	public static <T> UnaryOperator<ImmutableList<T>> mutatorList(Consumer<List<T>> mutator) {
		Objects.requireNonNull(mutator);
		return input -> mutateList(input, mutator);
	}

	/** Returns a function which mutates a set using the given mutator. */
	public static <T> UnaryOperator<ImmutableSet<T>> mutatorSet(Consumer<Set<T>> mutator) {
		Objects.requireNonNull(mutator);
		return input -> mutateSet(input, mutator);
	}

	/** Returns a function which mutates a map using the given mutator. */
	public static <K, V> UnaryOperator<ImmutableMap<K, V>> mutatorMap(Consumer<Map<K, V>> mutator) {
		Objects.requireNonNull(mutator);
		return input -> mutateMap(input, mutator);
	}

	/** Returns a function which mutates a sorted set using the given mutator. */
	public static <T> UnaryOperator<ImmutableSortedSet<T>> mutatorSortedSet(Consumer<NavigableSet<T>> mutator) {
		Objects.requireNonNull(mutator);
		return input -> mutateSortedSet(input, mutator);
	}

	/** Returns a function which mutates a sorted map using the given mutator. */
	public static <K, V> UnaryOperator<ImmutableSortedMap<K, V>> mutatorSortedMap(Consumer<NavigableMap<K, V>> mutator) {
		Objects.requireNonNull(mutator);
		return input -> mutateSortedMap(input, mutator);
	}

	/** Returns a function which mutates a sorted map using the given mutator. */
	public static <K, V> UnaryOperator<ImmutableBiMap<K, V>> mutatorBiMap(Consumer<BiMap<K, V>> mutator) {
		Objects.requireNonNull(mutator);
		return input -> mutateBiMap(input, mutator);
	}

	//////////////////////////
	// Per-element mutation //
	//////////////////////////
	/** Returns a mutated version of the given set.  Function can return null to indicate the element should be removed. */
	public static <T, R> ImmutableList<R> perElementMutateList(ImmutableList<T> source, Function<? super T, ? extends R> mutator) {
		Objects.requireNonNull(mutator);
		ImmutableList.Builder<R> builder = ImmutableList.builder(source.size());
		for (T element : source) {
			R result = mutator.apply(element);
			if (result != null) {
				builder.add(result);
			}
		}
		return builder.build();
	}

	/** Returns a mutated version of the given set.  Function can return null to indicate the element should be removed. */
	public static <T, R> ImmutableSet<R> perElementMutateSet(ImmutableSet<T> source, Function<? super T, ? extends R> mutator) {
		Objects.requireNonNull(mutator);
		ImmutableSet.Builder<R> builder = ImmutableSet.builder(source.size());
		for (T element : source) {
			R result = mutator.apply(element);
			if (result != null) {
				builder.add(result);
			}
		}
		return builder.build();
	}

	/** Uses a `Converter<T, R>` to generate a `Converter<Optional<T>, Optional<R>>`. */
	public static <T, R> Converter<Optional<T>, Optional<R>> perElementConverterOpt(ConverterNullable<T, R> perElement) {
		Objects.requireNonNull(perElement);
		return Converter.from(
				optT -> optT.map(perElement::convert),
				optR -> optR.map(perElement::revert),
				"perElement=" + perElement);
	}

	/** Uses a `Converter<T, R>` to generate a `Converter<ImmutableSet<T>, ImmutableSet<R>>`. */
	public static <T, R> Converter<ImmutableSet<T>, ImmutableSet<R>> perElementConverterSet(ConverterNullable<T, R> perElement) {
		Objects.requireNonNull(perElement);
		return Converter.from(
				setOfT -> perElementMutateSet(setOfT, perElement::convert),
				setOfR -> perElementMutateSet(setOfR, perElement::revert),
				perElement.toString());
	}

	/** Uses a `Converter<T, R>` to generate a `Converter<ImmutableList<T>, ImmutableList<R>>`. */
	public static <T, R> Converter<ImmutableList<T>, ImmutableList<R>> perElementConverterList(ConverterNullable<T, R> perElement) {
		Objects.requireNonNull(perElement);
		return Converter.from(
				setOfT -> perElementMutateList(setOfT, perElement::convert),
				setOfR -> perElementMutateList(setOfR, perElement::revert),
				perElement.toString());
	}

	////////////////////////
	// Optional <-> Stuff //
	////////////////////////
	/** Converts an {@link Optional} to an {@link ImmutableSet}. */
	public static <T> ImmutableSet<T> optionalToSet(Optional<T> selection) {
		if (selection.isPresent()) {
			return ImmutableSet.of(selection.get());
		} else {
			return ImmutableSet.of();
		}
	}

	/**
	 * Converts an {@link ImmutableCollection} to an {@link Optional}.
	 *
	 * @throws IllegalArgumentException if there are multiple elements.
	 */
	public static <T> Optional<T> optionalFrom(ImmutableCollection<T> collection) {
		switch (collection.size()) {
		case 0:
			return Optional.empty();
		case 1:
			return Optional.of(collection.iterator().next());
		default:
			throw new IllegalArgumentException("Collection contains multiple elements:" + collection);
		}
	}

	///////////////////////
	// Java 8 Collectors //
	///////////////////////
	// Inspired by http://blog.comsysto.com/2014/11/12/java-8-collectors-for-guava-collections/
	/** A Collector which returns an ImmutableList. */
	public static <T> Collector<T, ?, ImmutableList<T>> toList() {
		return toList(ImmutableCollection.Builder.DEFAULT_INITIAL_CAPACITY);
	}

	/** A Collector which returns an ImmutableList, with the given initial capacity. */
	public static <T> Collector<T, ?, ImmutableList<T>> toList(int initialCapacity) {
		// called for each combiner element (once if single-threaded, multiple if parallel)
		Supplier<ImmutableList.Builder<T>> supplier = () -> ImmutableList.builder(initialCapacity);
		// called for every element in the stream
		BiConsumer<ImmutableList.Builder<T>, T> accumulator = ImmutableList.Builder::add;
		// combines multiple collectors for parallel streams
		BinaryOperator<ImmutableList.Builder<T>> combiner = (l, r) -> l.addAll(r.build());
		// converts the builder into the list
		Function<ImmutableList.Builder<T>, ImmutableList<T>> finisher = ImmutableList.Builder::build;
		return Collector.of(supplier, accumulator, combiner, finisher);
	}

	/** A Collector which returns an ImmutableSet. */
	public static <T> Collector<T, ?, ImmutableSet<T>> toSet() {
		return toSet(ImmutableCollection.Builder.DEFAULT_INITIAL_CAPACITY);
	}

	/** A Collector which returns an ImmutableSet, with the given initial capacity. */
	public static <T> Collector<T, ?, ImmutableSet<T>> toSet(int initialCapacity) {
		Supplier<ImmutableSet.Builder<T>> supplier = () -> ImmutableSet.builder(initialCapacity);
		BiConsumer<ImmutableSet.Builder<T>, T> accumulator = ImmutableSet.Builder::add;
		BinaryOperator<ImmutableSet.Builder<T>> combiner = (l, r) -> l.addAll(r.build());
		Function<ImmutableSet.Builder<T>, ImmutableSet<T>> finisher = ImmutableSet.Builder::build;
		return Collector.of(supplier, accumulator, combiner, finisher);
	}

	/** A Collector which returns an ImmutableMap using the given pair of key and value functions. */
	public static <T, K, V> Collector<T, ?, ImmutableMap<K, V>> toMap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {
		return toMap(ImmutableCollection.Builder.DEFAULT_INITIAL_CAPACITY, keyMapper, valueMapper);
	}

	/** A Collector which returns an ImmutableMap using the given initial capacity and pair of key and value functions */
	public static <T, K, V> Collector<T, ?, ImmutableMap<K, V>> toMap(int initialCapacity, Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {
		Objects.requireNonNull(keyMapper);
		Objects.requireNonNull(valueMapper);
		Supplier<ImmutableMap.Builder<K, V>> supplier = () -> ImmutableMap.builder(initialCapacity);
		BiConsumer<ImmutableMap.Builder<K, V>, T> accumulator = (b, v) -> b.put(keyMapper.apply(v), valueMapper.apply(v));
		BinaryOperator<ImmutableMap.Builder<K, V>> combiner = (l, r) -> l.putAll(r.build());
		Function<ImmutableMap.Builder<K, V>, ImmutableMap<K, V>> finisher = ImmutableMap.Builder::build;
		return Collector.of(supplier, accumulator, combiner, finisher);
	}

	/** A Collector which returns an ImmutableSortedSet which is ordered by the given comparator. */
	public static <T> Collector<T, ?, ImmutableSortedSet<T>> toSortedSet(Comparator<? super T> comparator) {
		return toSortedSet(ImmutableCollection.Builder.DEFAULT_INITIAL_CAPACITY, comparator);
	}

	/** A Collector which returns an ImmutableSortedSet using the given initial capacity and ordered by the given comparator. */
	public static <T> Collector<T, ?, ImmutableSortedSet<T>> toSortedSet(int initialCapacity, Comparator<? super T> comparator) {
		Objects.requireNonNull(comparator);
		Supplier<ImmutableSortedSet.Builder<T>> supplier = () -> new ImmutableSortedSet.Builder(initialCapacity, comparator);
		BiConsumer<ImmutableSortedSet.Builder<T>, T> accumulator = ImmutableSortedSet.Builder::add;
		BinaryOperator<ImmutableSortedSet.Builder<T>> combiner = (l, r) -> l.addAll(r.build());
		Function<ImmutableSortedSet.Builder<T>, ImmutableSortedSet<T>> finisher = ImmutableSortedSet.Builder::build;
		return Collector.of(supplier, accumulator, combiner, finisher);
	}

	/** A Collector which returns an ImmutableSortedMap ordered by the given comparator, and populated by the given pair of key and value functions. */
	public static <T, K extends Comparable<?>, V> Collector<T, ?, ImmutableSortedMap<K, V>> toSortedMap(Comparator<? super K> comparator, Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {
		return toSortedMap(ImmutableCollection.Builder.DEFAULT_INITIAL_CAPACITY, comparator, keyMapper, valueMapper);
	}

	/** A Collector which returns an ImmutableSortedMap using the given initial capacity, ordered by the given comparator, and populated by the given pair of key and value functions. */
	public static <T, K extends Comparable<?>, V> Collector<T, ?, ImmutableSortedMap<K, V>> toSortedMap(int initialCapacity, Comparator<? super K> comparator, Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {
		Objects.requireNonNull(comparator);
		Objects.requireNonNull(keyMapper);
		Objects.requireNonNull(valueMapper);
		Supplier<ImmutableSortedMap.Builder<K, V>> supplier = () -> new ImmutableSortedMap.Builder(initialCapacity, comparator);
		BiConsumer<ImmutableSortedMap.Builder<K, V>, T> accumulator = (b, v) -> b.put(keyMapper.apply(v), valueMapper.apply(v));
		BinaryOperator<ImmutableSortedMap.Builder<K, V>> combiner = (l, r) -> l.putAll(r.build());
		Function<ImmutableSortedMap.Builder<K, V>, ImmutableSortedMap<K, V>> finisher = ImmutableSortedMap.Builder::build;
		return Collector.of(supplier, accumulator, combiner, finisher);
	}

	/** A Collector which returns an ImmutableBiMap which is ordered by the given comparator, and populated by the given pair of key and value functions. */
	public static <T, K, V> Collector<T, ?, ImmutableBiMap<K, V>> toBiMap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {
		return toBiMap(ImmutableCollection.Builder.DEFAULT_INITIAL_CAPACITY, keyMapper, valueMapper);
	}

	/** A Collector which returns an ImmutableBiMap which is ordered by the given comparator, and populated by the given pair of key and value functions. */
	public static <T, K, V> Collector<T, ?, ImmutableBiMap<K, V>> toBiMap(int initialCapacity, Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {
		Objects.requireNonNull(keyMapper);
		Objects.requireNonNull(valueMapper);
		Supplier<ImmutableBiMap.Builder<K, V>> supplier = () -> ImmutableBiMap.builder(initialCapacity);
		BiConsumer<ImmutableBiMap.Builder<K, V>, T> accumulator = (b, v) -> b.put(keyMapper.apply(v), valueMapper.apply(v));
		BinaryOperator<ImmutableBiMap.Builder<K, V>> combiner = (l, r) -> l.putAll(r.build());
		Function<ImmutableBiMap.Builder<K, V>, ImmutableBiMap<K, V>> finisher = ImmutableBiMap.Builder::build;
		return Collector.of(supplier, accumulator, combiner, finisher);
	}
}
