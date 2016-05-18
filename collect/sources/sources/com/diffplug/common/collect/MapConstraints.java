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

import static com.diffplug.common.base.Preconditions.checkNotNull;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;

import javax.annotation.Nullable;

import com.google.j2objc.annotations.WeakOuter;

import com.diffplug.common.annotations.Beta;
import com.diffplug.common.annotations.GwtCompatible;

/**
 * Factory and utilities pertaining to the {@code MapConstraint} interface.
 *
 * @see Constraints
 * @author Mike Bostock
 * @since 3.0
 * @deprecated Use {@link Preconditions} for basic checks. In place of
 *     constrained maps, we encourage you to check your preconditions
 *     explicitly instead of leaving that work to the map implementation.
 *     For the specific case of rejecting null, consider {@link ImmutableMap}.
 *     This class is scheduled for removal in Guava 20.0.
 */
@Beta
@GwtCompatible
@Deprecated
final class MapConstraints {
	private MapConstraints() {}

	/**
	 * Returns a constrained view of the specified entry, using the specified
	 * constraint. The {@link Entry#setValue} operation will be verified with the
	 * constraint.
	 *
	 * @param entry the entry to constrain
	 * @param constraint the constraint for the entry
	 * @return a constrained view of the specified entry
	 */
	private static <K, V> Entry<K, V> constrainedEntry(
			final Entry<K, V> entry, final MapConstraint<? super K, ? super V> constraint) {
		checkNotNull(entry);
		checkNotNull(constraint);
		return new ForwardingMapEntry<K, V>() {
			@Override
			protected Entry<K, V> delegate() {
				return entry;
			}

			@Override
			public V setValue(V value) {
				constraint.checkKeyValue(getKey(), value);
				return entry.setValue(value);
			}
		};
	}

	/**
	 * Returns a constrained view of the specified set of entries, using the
	 * specified constraint. The {@link Entry#setValue} operation will be verified
	 * with the constraint, along with add operations on the returned set. The
	 * {@code add} and {@code addAll} operations simply forward to the underlying
	 * set, which throws an {@link UnsupportedOperationException} per the map and
	 * multimap specification.
	 *
	 * <p>The returned multimap is not serializable.
	 *
	 * @param entries the entries to constrain
	 * @param constraint the constraint for the entries
	 * @return a constrained view of the specified entries
	 */
	private static <K, V> Set<Entry<K, V>> constrainedEntrySet(
			Set<Entry<K, V>> entries, MapConstraint<? super K, ? super V> constraint) {
		return new ConstrainedEntrySet<K, V>(entries, constraint);
	}

	/**
	 * Returns a constrained view of the specified map, using the specified
	 * constraint. Any operations that add new mappings will call the provided
	 * constraint. However, this method does not verify that existing mappings
	 * satisfy the constraint.
	 *
	 * <p>The returned map is not serializable.
	 *
	 * @param map the map to constrain
	 * @param constraint the constraint that validates added entries
	 * @return a constrained view of the specified map
	 */
	public static <K, V> Map<K, V> constrainedMap(
			Map<K, V> map, MapConstraint<? super K, ? super V> constraint) {
		return new ConstrainedMap<K, V>(map, constraint);
	}

	/** @see MapConstraints#constrainedMap */
	static class ConstrainedMap<K, V> extends ForwardingMap<K, V> {
		private final Map<K, V> delegate;
		final MapConstraint<? super K, ? super V> constraint;
		private transient Set<Entry<K, V>> entrySet;

		ConstrainedMap(Map<K, V> delegate, MapConstraint<? super K, ? super V> constraint) {
			this.delegate = checkNotNull(delegate);
			this.constraint = checkNotNull(constraint);
		}

		@Override
		protected Map<K, V> delegate() {
			return delegate;
		}

		@Override
		public Set<Entry<K, V>> entrySet() {
			Set<Entry<K, V>> result = entrySet;
			if (result == null) {
				entrySet = result = constrainedEntrySet(delegate.entrySet(), constraint);
			}
			return result;
		}

		@Override
		public V put(K key, V value) {
			constraint.checkKeyValue(key, value);
			return delegate.put(key, value);
		}

		@Override
		public void putAll(Map<? extends K, ? extends V> map) {
			delegate.putAll(checkMap(map, constraint));
		}
	}

	/** @see MapConstraints#constrainedEntries */
	private static class ConstrainedEntries<K, V> extends ForwardingCollection<Entry<K, V>> {
		final MapConstraint<? super K, ? super V> constraint;
		final Collection<Entry<K, V>> entries;

		ConstrainedEntries(
				Collection<Entry<K, V>> entries, MapConstraint<? super K, ? super V> constraint) {
			this.entries = entries;
			this.constraint = constraint;
		}

		@Override
		protected Collection<Entry<K, V>> delegate() {
			return entries;
		}

		@Override
		public Iterator<Entry<K, V>> iterator() {
			return new TransformedIterator<Entry<K, V>, Entry<K, V>>(entries.iterator()) {
				@Override
				Entry<K, V> transform(Entry<K, V> from) {
					return constrainedEntry(from, constraint);
				}
			};
		}

		// See Collections.CheckedMap.CheckedEntrySet for details on attacks.

		@Override
		public Object[] toArray() {
			return standardToArray();
		}

		@Override
		public <T> T[] toArray(T[] array) {
			return standardToArray(array);
		}

		@Override
		public boolean contains(Object o) {
			return Maps.containsEntryImpl(delegate(), o);
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			return standardContainsAll(c);
		}

		@Override
		public boolean remove(Object o) {
			return Maps.removeEntryImpl(delegate(), o);
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			return standardRemoveAll(c);
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			return standardRetainAll(c);
		}
	}

	/** @see MapConstraints#constrainedEntrySet */
	private static class ConstrainedEntrySet<K, V> extends ConstrainedEntries<K, V>
			implements Set<Entry<K, V>> {
		ConstrainedEntrySet(Set<Entry<K, V>> entries, MapConstraint<? super K, ? super V> constraint) {
			super(entries, constraint);
		}

		// See Collections.CheckedMap.CheckedEntrySet for details on attacks.

		@Override
		public boolean equals(@Nullable Object object) {
			return Sets.equalsImpl(this, object);
		}

		@Override
		public int hashCode() {
			return Sets.hashCodeImpl(this);
		}
	}

	private static <K, V> Map<K, V> checkMap(
			Map<? extends K, ? extends V> map, MapConstraint<? super K, ? super V> constraint) {
		Map<K, V> copy = new LinkedHashMap<K, V>(map);
		for (Entry<K, V> entry : copy.entrySet()) {
			constraint.checkKeyValue(entry.getKey(), entry.getValue());
		}
		return copy;
	}
}
