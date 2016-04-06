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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/** Class for manually constructing a tree, or for copying an existing tree. */
public class TreeNode<T> {
	private TreeNode<T> parent;
	private T content;
	private List<TreeNode<T>> children;

	/** Creates a TreeNode with the given parent and content. Automatically adds itself as a child of its parent. */
	public TreeNode(TreeNode<T> parent, T content) {
		this(parent, content, 0);
	}

	/**
	 * Creates a TreeNode with the given parent, content, and initial child capacity. Automatically adds itself as a child of its parent.
	 * <p>
	 * {@code childCapacity} is provided strictly for performance reasons.
	 */
	@SuppressWarnings("unchecked")
	public TreeNode(TreeNode<T> parent, T content, int childCapacity) {
		this.parent = parent;
		this.content = content;
		if (parent != null) {
			// if it's a Collections.emptyList(), then we need to make it a list we can add to
			if (parent.children == Collections.EMPTY_LIST) {
				parent.children = new ArrayList<>();
			}
			parent.children.add(this);
		}
		if (childCapacity == 0) {
			children = Collections.EMPTY_LIST;
		} else {
			children = new ArrayList<>(childCapacity);
		}
	}

	/** Returns the object which is encapsulated by this TreeNode. */
	public T getContent() {
		return content;
	}

	/** Sets the object which is encapsulated by this TreeNode. */
	public void setContent(T content) {
		this.content = content;
	}

	/** Returns the (possibly-null) parent of this TreeNode. */
	public TreeNode<T> getParent() {
		return parent;
	}

	/** Returns the children of this TreeNode. */
	public List<TreeNode<T>> getChildren() {
		return Collections.unmodifiableList(children);
	}

	/** Removes this TreeNode from its parent. */
	public void removeFromParent() {
		Objects.requireNonNull(parent);
		parent.children.remove(this);
		parent = null;
	}

	@Override
	public String toString() {
		return "TreeNode[" + content + "]";
	}

	/** Returns the path of this node, using the content's {@code toString()} method and {@code /} as the delimiter. */
	public String getPath() {
		return getPath(Object::toString);
	}

	/** Returns the path of this node, using the given {@code toString} method and {@code /} as the delimiter. */
	public String getPath(Function<? super T, String> toString) {
		return getPath(toString, "/");
	}

	/** Returns the path of this node, using the given {@code toString} method and {@code delimiter}. */
	public String getPath(Function<? super T, String> toString, String delimiter) {
		return TreeQuery.path(treeDef(), this, node -> toString.apply(node.getContent()), delimiter);
	}

	/**
	 * Returns a "deep" toString, including the entire tree below this level.
	 * 
	 * @see TreeQuery#toString(TreeDef, Object, Function, String)
	 */
	public String toStringDeep() {
		return TreeQuery.toString(treeDef(), this, node -> node.getContent().toString());
	}

	/** Creates a deep copy of this TreeNode. */
	public TreeNode<T> copy() {
		return copy(treeDef(), this, TreeNode::getContent);
	}

	/** Recursively sorts all children using the given comparator of their content. */
	public void sortChildrenByContent(Comparator<? super T> comparator) {
		Comparator<TreeNode<T>> byContent = Comparator.comparing(TreeNode::getContent, comparator);
		sortChildrenByNode(byContent);
	}

	/** Recursively sorts all children using the given comparator of TreeNode. */
	public void sortChildrenByNode(Comparator<TreeNode<T>> comparator) {
		Collections.sort(children, comparator);
		for (TreeNode<T> child : children) {
			child.sortChildrenByNode(comparator);
		}
	}

	/** Creates a hierarchy of TreeNodes that copies the structure and content of the given tree. */
	public static <T> TreeNode<T> copy(TreeDef<T> treeDef, T root) {
		return copy(treeDef, root, Function.identity());
	}

	/**
	 * Creates a hierarchy of TreeNodes that copies the structure and content of the given tree,
	 * using {@code mapper} to calculate the content of the nodes.
	 */
	public static <T, R> TreeNode<R> copy(TreeDef<T> treeDef, T root, Function<? super T, ? extends R> mapper) {
		List<T> children = treeDef.childrenOf(root);
		R mapped = mapper.apply(root);
		TreeNode<R> copyRoot = new TreeNode<>(null, mapped, children.size());
		copyRecurse(copyRoot, treeDef, root, children, mapper);
		return copyRoot;
	}

	private static <T, R> void copyRecurse(TreeNode<R> copiedRoot, TreeDef<T> treeDef, T root, List<T> children, Function<? super T, ? extends R> mapper) {
		for (T child : children) {
			R mapped = mapper.apply(child);
			List<T> grandChildren = treeDef.childrenOf(child);
			copyRecurse(new TreeNode<>(copiedRoot, mapped, grandChildren.size()), treeDef, child, grandChildren, mapper);
		}
	}

	/** {@link TreeDef.Parented} for TreeNodes. */
	@SuppressWarnings("unchecked")
	public static <T> TreeDef.Parented<TreeNode<T>> treeDef() {
		return (TreeDef.Parented<TreeNode<T>>) TREE_DEF;
	}

	@SuppressWarnings("rawtypes")
	private static final TreeDef.Parented TREE_DEF = new TreeDef.Parented<TreeNode<Object>>() {
		@Override
		public List<TreeNode<Object>> childrenOf(TreeNode<Object> root) {
			return root.getChildren();
		}

		@Override
		public TreeNode<Object> parentOf(TreeNode<Object> child) {
			return child.parent;
		}
	};

	////////////////
	// Test stuff //
	////////////////
	/**
	 * Creates a hierarchy of {@code TreeNode<String>} using an easy-to-read array of strings.
	 * <p>
	 * Spaces are used to represent parent / child relationships, e.g.
	 * <pre>
	 * TreeNode&lt;String&gt; root = createTestData(
	 *     "root",
	 *     " bigNode1",
	 *     " bigNode2",
	 *     "  child1",
	 *     "  child2",
	 *     " bigNode3"
	 * );
	 * </pre>
	 * There can only be one root node, and that is the node that is returned.
	 */
	public static TreeNode<String> createTestData(String... testData) {
		List<String> test = Arrays.asList(testData);

		// make the first node (which should have 0 leading spaces)
		assert (test.size() > 0);
		assert (0 == TreeNode.leadingSpaces(test.get(0)));

		TreeNode<String> rootNode = new TreeNode<>(null, test.get(0));
		TreeNode<String> lastNode = rootNode;
		int lastSpaces = 0;

		for (int i = 1; i < test.size(); ++i) {
			int newSpaces = TreeNode.leadingSpaces(test.get(i));
			String name = test.get(i).substring(newSpaces);
			if (newSpaces == lastSpaces + 1) {
				// one level deeper, so the last guy should be the parent
				lastNode = new TreeNode<>(lastNode, name);
				lastSpaces = newSpaces;
			} else if (newSpaces <= lastSpaces) {
				// any level back up, or the same level
				TreeNode<String> properParent = lastNode.getParent();
				int diff = lastSpaces - newSpaces;
				for (int j = 0; j < diff; ++j) {
					properParent = properParent.getParent();
				}
				lastNode = new TreeNode<>(properParent, name);
				lastSpaces = newSpaces;
			} else {
				throw new IllegalArgumentException("Last element \"" + test.get(i - 1) + "\""
						+ " and this element \"" + test.get(i) + "\" have too many spaces between them.");
			}
		}
		return rootNode;
	}

	/** Helps makeDummyTree */
	private static int leadingSpaces(String name) {
		int i = 0;
		while ((i < name.length()) && (name.charAt(i) == ' ')) {
			++i;
		}
		return i;
	}

	/**
	 * Finds a child TreeNode based on its path.
	 * <p>
	 * Searches the child nodes for the first element, then that
	 * node's children for the second element, etc.
	 * 
	 * @throws IllegalArgumentException if no such node exists
	 */
	@SuppressWarnings("unchecked")
	public TreeNode<T> findByPath(T... path) {
		return findByPath(Arrays.asList(path));
	}

	/** @see #findByPath(Object...) */
	public TreeNode<T> findByPath(List<T> path) {
		Optional<TreeNode<T>> result = TreeQuery.findByPath(treeDef(), this, TreeNode::getContent, path, Function.identity());
		if (result.isPresent()) {
			return result.get();
		} else {
			throw new IllegalArgumentException(this.toString() + " has no element with path " + path);
		}
	}

	/**
	 * Searches breadth-first for the TreeNode with the given content.
	 * 
	 * @throws IllegalArgumentException if no such node exists
	 */
	public TreeNode<T> findByContent(T content) {
		Optional<TreeNode<T>> opt = TreeStream.breadthFirst(treeDef(), this).filter(node -> node.getContent().equals(content)).findFirst();
		if (opt.isPresent()) {
			return opt.get();
		} else {
			throw new IllegalArgumentException(this.toString() + " has no child with content " + content);
		}
	}
}
