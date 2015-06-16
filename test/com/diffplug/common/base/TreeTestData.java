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

import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

/** Creates a hierarchy of TreeNodes from a list of strings. */
public class TreeTestData {
	@Test
	public void testTrivial() {
		TreeNode<String> root = TreeNode.createTestData("root");
		Assert.assertEquals("root", root.getContent());
		Assert.assertEquals(null, root.getParent());
		Assert.assertEquals(Collections.emptyList(), root.getChildren());
	}

	@Test
	public void testLinear() {
		TreeNode<String> root = TreeNode.createTestData("root", " middle", "  child");
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
		TreeNode<String> root = TreeNode.createTestData("root", " middle", "  child", " middle", "  child");
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
}
