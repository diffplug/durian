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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/** Creates a hierarchy of TreeNodes from a list of strings. */
public class TreeTestData {
	@Test
	public void testTrivial() {
		TreeNode<String> root = create("root");
		Assert.assertEquals("root", root.getContent());
		Assert.assertEquals(null, root.getParent());
		Assert.assertEquals(Collections.emptyList(), root.getChildren());
	}

	@Test
	public void testLinear() {
		TreeNode<String> root = create("root", " middle", "  child");
		Assert.assertEquals("root", root.getContent());
		Assert.assertEquals(null, root.getParent());
		Assert.assertEquals(1, root.getChildren().size());

		TreeNode<String> middle = root.getChildren().get(0);
		Assert.assertEquals("middle", middle.getContent());
		Assert.assertEquals(root, middle.getParent());
		Assert.assertEquals(1, middle.getChildren().size());

		TreeNode<String> child = middle.getChildren().get(0);
		Assert.assertEquals("child", child.getContent());
		Assert.assertEquals(middle, child.getParent());
		Assert.assertEquals(0, child.getChildren().size());
	}

	@Test
	public void testVee() {
		TreeNode<String> root = create("root", " middle", "  child", " middle", "  child");
		Assert.assertEquals("root", root.getContent());
		Assert.assertEquals(null, root.getParent());
		Assert.assertEquals(2, root.getChildren().size());

		testVeeMiddleChild(root, root.getChildren().get(0));
		testVeeMiddleChild(root, root.getChildren().get(1));
	}

	private void testVeeMiddleChild(TreeNode<String> root, TreeNode<String> middle) {
		Assert.assertEquals("middle", middle.getContent());
		Assert.assertEquals(root, middle.getParent());
		Assert.assertEquals(1, middle.getChildren().size());

		TreeNode<String> child = middle.getChildren().get(0);
		Assert.assertEquals("child", child.getContent());
		Assert.assertEquals(middle, child.getParent());
		Assert.assertEquals(0, child.getChildren().size());
	}

	/**
	 * Makes a tree out of generic Node objects using a String array
	 * with spaces to represent the parent / child relationships, e.g.
	 * makeDummyTree(new String[]{
	 * "root",
	 * " bigNode1",
	 * " bigNode2",
	 * "  child1",
	 * "  child2",
	 * " bigNode3"
	 * });
	 * There can only be one rootNode, and that is the node that is returned.
	 */
	public static TreeNode<String> create(List<String> testData) {
		// make the first node (which should have 0 leading spaces)
		Assert.assertTrue(testData.size() > 0);
		Assert.assertTrue(0 == leadingSpaces(testData.get(0)));

		TreeNode<String> rootNode = new TreeNode<String>(null, testData.get(0));
		TreeNode<String> lastNode = rootNode;
		int lastSpaces = 0;

		for (int i = 1; i < testData.size(); ++i) {
			int newSpaces = leadingSpaces(testData.get(i));
			String name = testData.get(i).substring(newSpaces);

			if (newSpaces == lastSpaces + 1) {
				// one level deeper, so the last guy should be the parent
				lastNode = new TreeNode<String>(lastNode, name);
				lastSpaces = newSpaces;
			} else if (newSpaces <= lastSpaces) {
				// any level back up, or the same level
				TreeNode<String> properParent = lastNode.getParent();
				int diff = lastSpaces - newSpaces;
				for (int j = 0; j < diff; ++j) {
					properParent = properParent.getParent();
				}
				lastNode = new TreeNode<String>(properParent, name);
				lastSpaces = newSpaces;
			} else {
				throw new IllegalArgumentException("Last element \"" + testData.get(i - 1) + "\""
						+ " and this element \"" + testData.get(i) + "\" have too many spaces between them.");
			}
		}

		return rootNode;
	}

	/** Delegates to create(List<String> testData). */
	public static TreeNode<String> create(String... testData) {
		return create(Arrays.asList(testData));
	}

	/** Finds the given node by path within root. */
	public static TreeNode<String> getByPath(TreeNode<String> root, String... names) {
		Assert.assertEquals(names[0], root.getContent());
		TreeNode<String> value = root;
		for (int i = 1; i < names.length; ++i) {
			String toMatch = names[i];
			value = value.getChildren().stream().filter(node -> node.getContent().equals(toMatch)).findFirst().get();
		}
		return value;
	}

	/** Finds the given node by name in the root. */
	public static TreeNode<String> getByName(TreeNode<String> root, String name) {
		Map<String, TreeNode<String>> mapByName = new HashMap<>();
		TreeIterable.breadthFirst(TreeNode.treeDef(), root).forEach(node -> {
			TreeNode<String> oldValue = mapByName.put(node.getContent(), node);
			Assert.assertTrue("Multiple nodes with name " + node.getContent(), oldValue == null);
		});
		TreeNode<String> node = mapByName.get(name);
		Assert.assertNotNull(node);
		return node;
	}

	/** Helps makeDummyTree */
	private static int leadingSpaces(String name) {
		int i = 0;
		while ((i < name.length()) && (name.charAt(i) == ' ')) {
			++i;
		}
		return i;
	}
}
