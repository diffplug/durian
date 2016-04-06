/*
 * Copyright 2016 DiffPlug
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Queries against {@link TreeDef} trees, e.g. lowest common ancestor, list of parents, etc. */
public class TreeQuery {
	/** Returns true iff child is a descendant of parent. */
	public static <T> boolean isDescendantOf(TreeDef.Parented<T> treeDef, T child, T parent) {
		T candidateParent = treeDef.parentOf(child);
		while (candidateParent != null) {
			if (candidateParent.equals(parent)) {
				return true;
			} else {
				candidateParent = treeDef.parentOf(candidateParent);
			}
		}
		return false;
	}

	/** Returns true iff child is a descendant of parent, or if child is equal to parent. */
	public static <T> boolean isDescendantOfOrEqualTo(TreeDef.Parented<T> treeDef, T child, T parent) {
		if (child.equals(parent)) {
			return true;
		} else {
			return isDescendantOf(treeDef, child, parent);
		}
	}

	/** Returns the root of the given tree. */
	public static <T> T root(TreeDef.Parented<T> treeDef, T node) {
		T lastParent;
		T parent = node;
		do {
			lastParent = parent;
			parent = treeDef.parentOf(lastParent);
		} while (parent != null);
		return lastParent;
	}

	/** Creates a mutable list whose first element is {@code node}, and last element is its root parent. */
	public static <T> List<T> toRoot(TreeDef.Parented<T> treeDef, T node) {
		List<T> list = new ArrayList<>();
		T tip = node;
		while (tip != null) {
			list.add(tip);
			tip = treeDef.parentOf(tip);
		}
		return list;
	}

	/**
	 * Copies the given tree of T to CopyType, starting at the leaf nodes
	 * of the tree and moving in to the root node, which allows CopyType to
	 * be immutable (but does not require it).
	 *
	 * @param def		defines the structure of the tree
	 * @param root		root of the tree
	 * @param nodeMapper	given an unmapped node, and a list of CopyType nodes which have already been mapped, return a mapped node.
	 * @return a CopyType with the same contents as the source tree
	 */
	public static <T, CopyType> CopyType copyLeavesIn(TreeDef<T> def, T root, BiFunction<T, List<CopyType>, CopyType> nodeMapper) {
		List<CopyType> childrenMapped = def.childrenOf(root).stream().map(child -> {
			return copyLeavesIn(def, child, nodeMapper);
		}).collect(Collectors.toList());
		return nodeMapper.apply(root, childrenMapped);
	}

	/**
	 * Copies the given tree of T to CopyType, starting at the root node
	 * of the tree and moving out to the leaf nodes, which generally requires
	 * CopyType to be mutable (if you want CopyType nodes to know who their
	 * children are).
	 *
	 * @param def		defines the structure of the tree
	 * @param root		root of the tree
	 * @param nodeMapper	given an unmapped node, and a parent CopyType which has already been mapped, return a mapped node.
	 *                      This function must have the side effect that the returned node should be added as a child of its
	 *                      parent node.
	 * @return a CopyType with the same contents as the source tree
	 */
	public static <T, CopyType> CopyType copyRootOut(TreeDef<T> def, T root, BiFunction<T, CopyType, CopyType> mapper) {
		List<T> children = def.childrenOf(root);
		CopyType copyRoot = mapper.apply(root, null);
		copyMutableRecurse(def, root, children, copyRoot, mapper);
		return copyRoot;
	}

	private static <T, CopyType> void copyMutableRecurse(TreeDef<T> def, T root, List<T> children, CopyType copiedRoot, BiFunction<T, CopyType, CopyType> mapper) {
		for (T child : children) {
			List<T> grandChildren = def.childrenOf(child);
			copyMutableRecurse(def, root, grandChildren, mapper.apply(child, copiedRoot), mapper);
		}
	}

	/**
	 * Creates a mutable list whose first element is {@code node}, and last element is {@code parent}.
	 * @throws IllegalArgumentException if {@code parent} is not a parent of {@code node}
	 */
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
	private static <T> Optional<T> lowestCommonAncestor(TreeDef.Parented<T> treeDef, T nodeA, T nodeB) {
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
	public static <T> Optional<T> lowestCommonAncestor(TreeDef.Parented<T> treeDef, T... nodes) {
		return lowestCommonAncestor(treeDef, Arrays.asList(nodes));
	}

	/** Returns the common parent of N elements. */
	public static <T> Optional<T> lowestCommonAncestor(TreeDef.Parented<T> treeDef, List<T> nodes) {
		if (nodes.size() == 0) {
			return Optional.empty();
		} else {
			Optional<T> soFar = Optional.of(nodes.get(0));
			for (int i = 1; i < nodes.size() && soFar.isPresent(); ++i) {
				soFar = lowestCommonAncestor(treeDef, soFar.get(), nodes.get(i));
			}
			return soFar;
		}
	}

	/**
	 * Returns the path of the given node.
	 * 
	 * @param treeDef	the treeDef
	 * @param node		the root of the tree
	 * @param toString	a function to map each node to a string in the path 
	 * @param delimiter	a string to use as a path separator
	 */
	public static <T> String path(TreeDef.Parented<T> treeDef, T node, Function<? super T, String> toString, String delimiter) {
		List<T> toRoot = toRoot(treeDef, node);
		ListIterator<T> iterator = toRoot.listIterator(toRoot.size());
		StringBuilder builder = new StringBuilder();
		while (iterator.hasPrevious()) {
			T segment = iterator.previous();
			// add the node
			builder.append(toString.apply(segment));
			// add the separator if it makes sense
			if (iterator.hasPrevious()) {
				builder.append(delimiter);
			}
		}
		return builder.toString();
	}

	/**
	 * Returns the path of the given node, using {@code /} as the path delimiter.
	 * 
	 * @see #path(com.diffplug.common.base.TreeDef.Parented, Object, Function, String)
	 */
	public static <T> String path(TreeDef.Parented<T> treeDef, T node, Function<? super T, String> toString) {
		return path(treeDef, node, toString, "/");
	}

	/**
	 * Returns the path of the given node, using {@code /} as the path delimiter and {@link Object#toString()} as the mapping function.
	 * 
	 * @see #path(com.diffplug.common.base.TreeDef.Parented, Object, Function, String)
	 */
	public static <T> String path(TreeDef.Parented<T> treeDef, T node) {
		return path(treeDef, node, Object::toString);
	}

	/**
	 * Finds a child TreeNode based on its path.
	 * <p>
	 * Searches the child nodes for the first element, then that
	 * node's children for the second element, etc.
	 * 
	 * @param treeDef	defines a tree
	 * @param node		starting point for the search
	 * @param path		the path of nodes which we're looking
	 * @param equality	a function for determining equality between the tree nodes and the path elements
	 */
	public static <T, P> Optional<T> findByPath(TreeDef<T> treeDef, T node, List<P> path, BiPredicate<T, P> equality) {
		T value = node;
		for (P segment : path) {
			Optional<T> valueOpt = treeDef.childrenOf(value).stream().filter(n -> equality.test(n, segment)).findFirst();
			if (!valueOpt.isPresent()) {
				return valueOpt;
			}
			value = valueOpt.get();
		}
		return Optional.of(value);
	}

	/**
	 * Finds a child TreeNode based on its path.
	 * <p>
	 * Searches the child nodes for the first element, then that
	 * node's children for the second element, etc.
	 * 
	 * @param treeDef		defines a tree
	 * @param node			starting point for the search
	 * @param treeMapper	maps elements in the tree to some value for comparison with the path elements
	 * @param path			the path of nodes which we're looking
	 * @param pathMapper	maps elements in the path to some value for comparison with the tree elements
	 */
	public static <T, P> Optional<T> findByPath(TreeDef<T> treeDef, T node, Function<? super T, ?> treeMapper, List<P> path, Function<? super P, ?> pathMapper) {
		return findByPath(treeDef, node, path, (treeSide, pathSide) -> {
			return Objects.equals(treeMapper.apply(treeSide), pathMapper.apply(pathSide));
		});
	}

	/**
	 * Finds a child TreeNode based on its path.
	 * <p>
	 * Searches the child nodes for the first element, then that
	 * node's children for the second element, etc.
	 * 
	 * @param treeDef		defines a tree
	 * @param node			starting point for the search
	 * @param path			the path of nodes which we're looking
	 * @param mapper		maps elements to some value for comparison between the tree and the path
	 */
	public static <T> Optional<T> findByPath(TreeDef<T> treeDef, T node, List<T> path, Function<? super T, ?> mapper) {
		return findByPath(treeDef, node, mapper, path, mapper);
	}

	/**
	 * Converts the entire tree into a string-based representation.
	 * 
	 * @see #toString(TreeDef, Object, Function, String)
	 */
	public static <T> String toString(TreeDef<T> treeDef, T root) {
		return toString(treeDef, root, Object::toString);
	}

	/**
	 * Converts the entire tree into a string-based representation.
	 * 
	 * @see #toString(TreeDef, Object, Function, String)
	 */
	public static <T> String toString(TreeDef<T> treeDef, T root, Function<? super T, String> toString) {
		return toString(treeDef, root, toString, " ");
	}

	/**
	 * Converts the entire tree into a string-based representation.
	 * 
	 * @param treeDef	the treeDef
	 * @param root		the root of the tree
	 * @param toString	the function which generates the name for each node in the tree
	 * @param indent	the string to use for each level of indentation
	 */
	public static <T> String toString(TreeDef<T> treeDef, T root, Function<? super T, String> toString, String indent) {
		StringBuilder builder = new StringBuilder();
		builder.append(toString.apply(root));
		builder.append("\n");
		toStringHelper(treeDef, root, toString, indent, builder, indent);
		return builder.toString();
	}

	private static <T> void toStringHelper(TreeDef<T> treeDef, T root, Function<? super T, String> toString, String indent, StringBuilder builder, String prefix) {
		for (T child : treeDef.childrenOf(root)) {
			builder.append(prefix);
			builder.append(toString.apply(child));
			builder.append("\n");
			toStringHelper(treeDef, child, toString, indent, builder, prefix + indent);
		}
	}
}
