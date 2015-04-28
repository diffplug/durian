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
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import com.diffplug.common.base.TreeNode;
import com.diffplug.common.base.TreeStream;

public class TreeStreamTest {
	@Test
	public void toParentTest() {
		toParentTestCase("root");
		toParentTestCase("A", "root");
		toParentTestCase("a", "2", "1", "root");
		toParentTestCase("3", "2", "1", "root");
	}

	private void toParentTestCase(String root, String... values) {
		List<String> actual = TreeStream.toParent(TreeNode.treeDef(), getNode(root))
				.map(TreeNode::getObj)
				.collect(Collectors.toList());
		Assert.assertEquals(root, actual.get(0));
		Assert.assertEquals(Arrays.asList(values), actual.subList(1, actual.size()));

		List<String> stream = TreeStream.toParent(TreeNode.treeDef(), getNode(root))
				.map(TreeNode::getObj)
				.collect(Collectors.toList());
		Assert.assertEquals(actual, stream);
	}

	@Test
	public void breadthFirstTest() {
		breadthFirstTestCase("root", "A", "1", "B", "2", "C", "3", "a");
	}

	private void breadthFirstTestCase(String root, String... values) {
		List<String> actual = TreeStream.breadthFirst(TreeNode.treeDef(), getNode(root))
				.map(TreeNode::getObj)
				.collect(Collectors.toList());

		Assert.assertEquals(root, actual.get(0));
		Assert.assertEquals(Arrays.asList(values), actual.subList(1, actual.size()));

		List<String> stream = TreeStream.breadthFirst(TreeNode.treeDef(), getNode(root))
				.map(TreeNode::getObj)
				.collect(Collectors.toList());
		Assert.assertEquals(actual, stream);
	}

	@Test
	public void depthFirstTest() {
		depthFirstTestCase(TreeNode.treeDef(), "root", "A", "B", "C", "1", "2", "3", "a");
	}

	private void depthFirstTestCase(TreeDef<TreeNode<String>> treeDef, String root, String... values) {
		List<String> actual = TreeStream.depthFirst(TreeNode.treeDef(), getNode(root))
				.map(TreeNode::getObj)
				.collect(Collectors.toList());

		Assert.assertEquals(root, actual.get(0));
		Assert.assertEquals(Arrays.asList(values), actual.subList(1, actual.size()));

		List<String> stream = TreeStream.depthFirst(TreeNode.treeDef(), getNode(root)).map(TreeNode::getObj)
				.collect(Collectors.toList());
		Assert.assertEquals(actual, stream);
	}

	@Test
	public void filterTest() {
		// filter out non-alphabetic nodes
		depthFirstTestCase(TreeNode.<String> treeDef().filter(node -> node.obj.codePoints().allMatch(Character::isAlphabetic)),
				"root", "A", "B", "C");
	}

	private TreeNode<String> getNode(String name) {
		return TreeTestData.getByName(root, name);
	}

	// @formatter:off
	private TreeNode<String> root = TreeTestData.create(
			"root",
			" A",
			"  B",
			"   C",
			" 1",
			"  2",
			"   3",
			"   a");
	// @formatter:on
}
