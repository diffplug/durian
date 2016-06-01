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

import com.diffplug.common.collect.MutableClassToInstanceMapConstraints.ConstrainedMap;
import com.diffplug.common.primitives.Primitives;

/**
 * A mutable class-to-instance map backed by an arbitrary user-provided map.
 * See also {@link ImmutableClassToInstanceMap}.
 *
 * <p>See the Guava User Guide article on <a href=
 * "https://github.com/google/guava/wiki/NewCollectionTypesExplained#classtoinstancemap">
 * {@code ClassToInstanceMap}</a>.
 *
 * @author Kevin Bourrillion
 * @since 2.0
 */
public final class MutableClassToInstanceMap<B> extends ConstrainedMap<Class<? extends B>, B>
		implements ClassToInstanceMap<B> {

	/**
	 * Returns a new {@code MutableClassToInstanceMap} instance backed by a {@link
	 * HashMap} using the default initial capacity and load factor.
	 */
	public static <B> MutableClassToInstanceMap<B> create() {
		return new MutableClassToInstanceMap<B>(new HashMap<Class<? extends B>, B>());
	}

	/**
	 * Returns a new {@code MutableClassToInstanceMap} instance backed by a given
	 * empty {@code backingMap}. The caller surrenders control of the backing map,
	 * and thus should not allow any direct references to it to remain accessible.
	 */
	public static <B> MutableClassToInstanceMap<B> create(Map<Class<? extends B>, B> backingMap) {
		return new MutableClassToInstanceMap<B>(backingMap);
	}

	private MutableClassToInstanceMap(Map<Class<? extends B>, B> delegate) {
		super(delegate, VALUE_CAN_BE_CAST_TO_KEY);
	}

	private static final MutableClassToInstanceMapConstraint<Class<?>, Object> VALUE_CAN_BE_CAST_TO_KEY = new MutableClassToInstanceMapConstraint<Class<?>, Object>() {
		@Override
		public void checkKeyValue(Class<?> key, Object value) {
			cast(key, value);
		}
	};

	@Override
	public <T extends B> T putInstance(Class<T> type, T value) {
		return cast(type, put(type, value));
	}

	@Override
	public <T extends B> T getInstance(Class<T> type) {
		return cast(type, get(type));
	}

	private static <B, T extends B> T cast(Class<T> type, B value) {
		return Primitives.wrap(type).cast(value);
	}

	private static final long serialVersionUID = 0;
}
