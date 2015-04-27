/**
 * Copyright 2015 DiffPlug
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

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/** Function which defines a Tree structure based on a root object. */
@FunctionalInterface
public interface TreeDef<T> {
	/** Returns all the children of the given node. */
	List<T> childrenOf(T node);

	/** Returns a new TreeDef which whose childrenOf() method is filtered by the given predicate. */
	default TreeDef<T> filter(Predicate<T> predicate) {
		return TreeDef.of(node -> filteredList(childrenOf(node), predicate));
	}

	/** Creates a TreeDef which is implemented by the given function. */
	public static <T> TreeDef<T> of(Function<T, List<T>> childFunc) {
		return new TreeDef<T>() {
			@Override
			public List<T> childrenOf(T node) {
				return childFunc.apply(node);
			}
		};
	}

	/**
	 * A "doubly-linked" tree, where nodes know about both their parent and their children.
	 * 
	 * It is CRITICAL that the TreeDef.Parented is consistent - if Vader claims that Luke
	 * and Leia are his children, then both Luke and Leia must say that Vader is their parent.
	 * 
	 * If Luke or Leia don't agree that Vader is their father, then the algorithms that use
	 * this TreeDef.Parented are likely to fail in unexpected ways.
	 */
	public interface Parented<T> extends TreeDef<T> {
		/** Returns the parent of the given node. */
		T parentOf(T node);

		/** Returns a new TreeDef which whose childrenOf() and parentOf() methods are filtered by the given predicate. */
		@Override
		default Parented<T> filter(Predicate<T> predicate) {
			return of(node -> filteredList(childrenOf(node), predicate), node -> {
				if (predicate.test(node)) {
					return parentOf(node);
				} else {
					return null;
				}
			});
		}

		/** Creates a TreeDef.Parented which is implemented by the given two functions. */
		public static <T> TreeDef.Parented<T> of(Function<T, List<T>> childFunc, Function<T, T> parentFunc) {
			return new TreeDef.Parented<T>() {
				@Override
				public List<T> childrenOf(T node) {
					return childFunc.apply(node);
				}

				@Override
				public T parentOf(T node) {
					return parentFunc.apply(node);
				}
			};
		}
	}

	/** Returns a filtered version of the given list. */
	static <T> List<T> filteredList(List<T> unfiltered, Predicate<T> filter) {
		return unfiltered.stream().filter(filter).collect(Collectors.toList());
	}
}
