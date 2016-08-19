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
import java.util.Objects;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.diffplug.common.annotations.GwtCompatible;

/**
 * An ordering that orders elements by applying an order to the result of a
 * function on those elements.
 */
@GwtCompatible(serializable = true)
final class ByFunctionOrdering<F, T> extends Ordering<F> implements Serializable {
	final Function<F, ? extends T> function;
	final Ordering<T> ordering;

	ByFunctionOrdering(Function<F, ? extends T> function, Ordering<T> ordering) {
		this.function = checkNotNull(function);
		this.ordering = checkNotNull(ordering);
	}

	@Override
	public int compare(F left, F right) {
		return ordering.compare(function.apply(left), function.apply(right));
	}

	@Override
	public boolean equals(@Nullable Object object) {
		if (object == this) {
			return true;
		}
		if (object instanceof ByFunctionOrdering) {
			ByFunctionOrdering<?, ?> that = (ByFunctionOrdering<?, ?>) object;
			return this.function.equals(that.function) && this.ordering.equals(that.ordering);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(function, ordering);
	}

	@Override
	public String toString() {
		return ordering + ".onResultOf(" + function + ")";
	}

	private static final long serialVersionUID = 0;
}
