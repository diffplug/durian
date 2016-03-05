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

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collections;
import java.util.Set;

import com.diffplug.common.annotations.GwtCompatible;
import com.diffplug.common.collect.Multiset;
import com.diffplug.common.collect.testing.features.Feature;
import com.diffplug.common.collect.testing.features.TesterAnnotation;

/**
 * Optional features of classes derived from {@link Multiset}.
 *
 * @author Louis Wasserman
 */
@GwtCompatible
public enum MultisetFeature implements Feature<Multiset> {
	/**
	 * Indicates that elements from {@code Multiset.entrySet()} update to reflect changes in the
	 * backing multiset.
	 */
	ENTRIES_ARE_VIEWS;

	@Override
	public Set<Feature<? super Multiset>> getImpliedFeatures() {
		return Collections.emptySet();
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Inherited
	@TesterAnnotation
	public @interface Require {
		public abstract MultisetFeature[]value() default {};

		public abstract MultisetFeature[]absent() default {};
	}
}
