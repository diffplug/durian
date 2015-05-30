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
import org.junit.Test;

/** Stupid-simple test for the very simple Box class. */
public class BoxTest {
	private static String staticValue;

	private static String getStaticValue() {
		return staticValue;
	}

	private static void setStaticValue(String value) {
		staticValue = value;
	}

	@Test
	public void testStatic() {
		Box<String> forValue = Box.from(BoxTest::getStaticValue, BoxTest::setStaticValue);
		forValue.set("A");
		Assert.assertEquals("A", forValue.get());
		forValue.set("B");
		Assert.assertEquals("B", forValue.get());
	}

	private String value;

	private String getValue() {
		return value;
	}

	private void setValue(String value) {
		this.value = value;
	}

	@Test
	public void testInstance() {
		Box<String> forValue = Box.from(this, BoxTest::getValue, BoxTest::setValue);
		forValue.set("A");
		Assert.assertEquals("A", forValue.get());
		forValue.set("B");
		Assert.assertEquals("B", forValue.get());
	}
}
