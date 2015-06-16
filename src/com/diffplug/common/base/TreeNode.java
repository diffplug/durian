/*
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
import java.util.Collections;
import java.util.List;

/** Class for manually constructing a tree, or for copying an existing tree. */
public final class TreeNode<T> {
	private TreeNode<T> parent;
	private T content;
	private List<TreeNode<T>> children = Collections.emptyList();

	/** Creates a TreeNode with the given parent and content. Automatically adds itself as a child of its parent. */
	public TreeNode(TreeNode<T> parent, T obj) {
		this.parent = parent;
		this.content = obj;
		if (parent != null) {
			// if it's empty, it's a Collections.emptyList(), so we need to make a list we can add to
			if (parent.children.isEmpty()) {
				parent.children = new ArrayList<>();
			}
			parent.children.add(this);
		}
	}

	/** Returns the object which is encapsulated by this TreeNode. */
	public T getContent() {
		return content;
	}

	/** Returns the parent of this TreeNode. */
	public TreeNode<T> getParent() {
		return parent;
	}

	/** Returns the children of this TreeNode. */
	public List<TreeNode<T>> getChildren() {
		return Collections.unmodifiableList(children);
	}

	@Override
	public String toString() {
		return "TreeNode[" + content + "]";
	}

	/** Creates a hierarchy of TreeNodes that copies the structure and content of the given Tree. */
	public static <T> TreeNode<T> copy(T root, TreeDef<T> treeDef) {
		TreeNode<T> copyRoot = new TreeNode<>(null, root);
		copyRecurse(copyRoot, treeDef);
		return copyRoot;
	}

	private static <T> void copyRecurse(TreeNode<T> root, TreeDef<T> treeDef) {
		List<T> children = treeDef.childrenOf(root.content);
		for (T child : children) {
			copyRecurse(new TreeNode<>(root, child), treeDef);
		}
	}

	/** {@link TreeDef.Parented} for TreeNodes. */
	public static <T> TreeDef.Parented<TreeNode<T>> treeDef() {
		return new TreeDef.Parented<TreeNode<T>>() {
			@Override
			public List<TreeNode<T>> childrenOf(TreeNode<T> root) {
				return root.getChildren();
			}

			@Override
			public TreeNode<T> parentOf(TreeNode<T> child) {
				return child.parent;
			}
		};
	}
}
