/*
 * Copyright (C) 2007 The Guava Authors
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

/**
 * This package contains generic interfaces for defining a tree structure on
 * existing data, as well as utilties for traversing, querying, copying, and
 * comparing these trees.
 *
 * {@link com.diffplug.common.tree.TreeDef} defines a "singly-linked" tree, where nodes know their children
 * but not their parents.  {@link com.diffplug.common.tree.TreeDef.Parented} defines a "double-linked" tree,
 * where children also know who their parents are.
 *
 * Once you have defined a `TreeDef`, you can use {@link com.diffplug.common.tree.TreeIterable} to iterate
 * over it, or {@link com.diffplug.common.tree.TreeStream} to iterate over it as a Java 8 {@link java.util.stream.Stream Stream}.
 *
 * {@link com.diffplug.common.tree.TreeQuery} helps with queries such as:
 *
 * - {@link com.diffplug.common.tree.TreeQuery#isDescendantOf(com.diffplug.common.tree.TreeDef.Parented, Object, Object) isDescendantOf}
 * - {@link com.diffplug.common.tree.TreeQuery#lowestCommonAncestor(com.diffplug.common.tree.TreeDef.Parented, java.util.List) lowestCommonAncestor}
 * - {@link com.diffplug.common.tree.TreeQuery#path(com.diffplug.common.tree.TreeDef.Parented, Object) path}
 * - {@link com.diffplug.common.tree.TreeQuery#findByPath(TreeDef, Object, java.util.List, java.util.function.BiPredicate) findByPath}
 * - {@link com.diffplug.common.tree.TreeQuery#copyLeavesIn(TreeDef, Object, java.util.function.BiFunction) copyLeavesIn} and {@link TreeQuery#copyRootOut(TreeDef, Object, java.util.function.BiFunction) copyRootOut}
 *
 * Generally, in a `TreeDef<T>`, the `T` type contains both the tree's data
 * and its tree structure.  For example, in a `TreeDef<File>`, the `File` contains
 * both its name and its child folders and files. Sometimes you'd like to copy the
 * tree's data independently of its tree structure, or to stuff data without innate
 * tree structure into a newly created tree structure. {@link TreeNode} is a
 * general-purpose doubly-linked tree with mechanisms for:
 *
 * - {@linkplain com.diffplug.common.tree.TreeNode#copy(TreeDef, Object) copying the tree structure} of an existing tree.
 * - {@linkplain com.diffplug.common.tree.TreeNode#createTestData(String...) creating easy-to-read} test data.
 * - As well as general create / read / modify capabilities.
 *
 * If you need to compare trees, {@link com.diffplug.common.tree.TreeComparison} has you covered, and can even
 * compare trees of different types.  If the nodes in your tree have a good `toString()`
 * implementation, then you can combine `TreeNode.createTestData` to create easy-to-read
 * tests with helpful error messages:
 *
 * ```java
 * TreeNode<String> expected = TreeNode.createTestData(
 *     "root",
 *     " middle",
 *     "  child",
 *     " middle",
 *     "  child");
 * TreeComparison.of(expected, MyTreeType.treeDef(), myTree)
 *     .mapToSame(TreeNode::getContent, MyTreeType::toString)
 *     .assertEqual();
 * ```
 */
@javax.annotation.ParametersAreNonnullByDefault
package com.diffplug.common.tree;
