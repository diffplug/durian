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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** Queries against trees, e.g. lowest common ancestor, list of parents, etc. */
public class TreeQuery {
	/** Returns a mutable list whose first element is the child, and last element is the root. */
	public static <T> List<T> toRoot(TreeDef.Parented<T> treeDef, T node) {
		List<T> list = new ArrayList<>();
		T tip = node;
		while (tip != null) {
			list.add(tip);
			tip = treeDef.parentOf(tip);
		}
		return list;
	}

	/** Returns a mutable list whose first element is the child, and last element is the root. */
	public static <T> List<T> toParent(TreeDef.Parented<T> treeDef, T node, T parent) {
		List<T> list = new ArrayList<>();
		T tip = node;
		while (true) {
			list.add(tip);
			tip = treeDef.parentOf(tip);
			if (tip == null) {
				throw new IllegalArgumentException(parent + " is not a parent of " + node);
			} else if (tip.equals(parent)) {
				list.add(parent);
				return list;
			}
		}
	}

	/** Returns the common parent of the two given elements. */
	public static <T> Optional<T> lowestCommonAncestor(TreeDef.Parented<T> treeDef, T nodeA, T nodeB) {
		class TreeSearcher {
			private T tip;
			private final Set<T> visited;

			public TreeSearcher(T start) {
				this.tip = start;
				this.visited = new HashSet<>();
			}

			public boolean hasMore() {
				return tip != null;
			}

			public Optional<T> march(TreeSearcher other) {
				if (other.visited.contains(tip)) {
					return Optional.of(tip);
				} else {
					visited.add(tip);
					tip = treeDef.parentOf(tip);
					return Optional.empty();
				}
			}
		}

		// make a list of a's parents (bottom of stack is 'a' itself, top of is root)
		TreeSearcher searchA = new TreeSearcher(nodeA);
		TreeSearcher searchB = new TreeSearcher(nodeB);

		Optional<T> commonAncestor = searchB.march(searchA);
		// so long as both searches
		while (searchA.hasMore() && searchB.hasMore()) {
			commonAncestor = searchA.march(searchB);
			if (commonAncestor.isPresent()) {
				return commonAncestor;
			}
			commonAncestor = searchB.march(searchA);
			if (commonAncestor.isPresent()) {
				return commonAncestor;
			}
		}
		while (searchA.hasMore() && !commonAncestor.isPresent()) {
			commonAncestor = searchA.march(searchB);
		}
		while (searchB.hasMore() && !commonAncestor.isPresent()) {
			commonAncestor = searchB.march(searchA);
		}
		return commonAncestor;
	}

	/** Returns the common parent of N elements. */
	@SafeVarargs
	public static <T> Optional<T> lowestCommonAncestorN(TreeDef.Parented<T> treeDef, T... nodes) {
		if (nodes.length == 0) {
			return Optional.empty();
		} else {
			Optional<T> soFar = Optional.of(nodes[0]);
			for (int i = 1; i < nodes.length && soFar.isPresent(); ++i) {
				soFar = lowestCommonAncestor(treeDef, soFar.get(), nodes[i]);
			}
			return soFar;
		}
	}
}
