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
import java.util.Collections;
import java.util.List;

/** Allows Trees to be defined across any object. */
public final class TreeNode<T> {
	final TreeNode<T> parent;
	final T obj;
	List<TreeNode<T>> children = null;

	public TreeNode(TreeNode<T> parent, T obj) {
		this.parent = parent;
		this.obj = obj;
		if (parent != null) {
			if (parent.children == null) {
				parent.children = new ArrayList<>();
			}
			parent.children.add(this);
		}
	}

	public TreeNode<T> getParent() {
		return parent;
	}

	public T getObj() {
		return obj;
	}

	public List<TreeNode<T>> getChildren() {
		return children == null ? Collections.emptyList() : children;
	}

	/** Creates a hierarchy of TreeNodes that copies the structure and content of the given Tree. */
	public static <T> TreeNode<T> copy(T root, TreeDef<T> treeDef) {
		TreeNode<T> copyRoot = new TreeNode<>(null, root);
		copyRecurse(copyRoot, treeDef);
		return copyRoot;
	}

	private static <T> void copyRecurse(TreeNode<T> root, TreeDef<T> treeDef) {
		List<T> children = treeDef.childrenOf(root.obj);
		for (T child : children) {
			copyRecurse(new TreeNode<>(root, child), treeDef);
		}
	}

	/** TreeDef for the generic TreeNode class. */
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
