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

import org.junit.Assert;
import org.junit.ComparisonFailure;
import org.junit.Test;

public class TreeComparisonTest {
	@Test
	public void testEqualToItself() {
		TreeComparison.of(root, root).assertEqual();
	}

	@Test
	public void testEqualToCopy() {
		TreeComparison.of(root, root.copy()).assertEqual();
	}

	@Test(expected = ComparisonFailure.class)
	public void testFailure() {
		TreeNode<String> modified = root.copy();
		modified.findByContent("Array.java").removeFromParent();
		TreeComparison.of(root, modified).assertEqual();
	}

	@Test
	public void testFailureText() {
		TreeNode<String> expectedTree = TreeNode.createTestData("root", " src");
		TreeNode<String> actualTree = TreeNode.createTestData("root", " source");
		try {
			TreeComparison.of(expectedTree, actualTree).assertEqual();
			Assert.fail();
		} catch (ComparisonFailure error) {
			Assert.assertEquals("root\n src\n", error.getExpected());
			Assert.assertEquals("root\n source\n", error.getActual());
		}
	}

	// @formatter:off
	private TreeNode<String> root = TreeNode.createTestData(
			"root",
			" src",
			"  org",
			"   math",
			"    Array.java",
			"    Matrix.java",
			"    QuatRot.java",
			"    Vector.java",
			"   model",
			"    generic",
			"     Constant.java",
			"     Constant.xml"
			);
	// @formatter:on
}
