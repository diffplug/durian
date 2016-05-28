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

import static com.google.common.truth.Truth.assertThat;

import java.util.function.BiConsumer;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;

/** Stupid-simple test for the very simple Box class. */
public class BoxTest {
	@Test
	public void testToString() {
		BiConsumer<Object, String> expectToString = (obj, expected) -> {
			assertThat(obj.toString()).isEqualTo(expected);
		};

		// standard Box
		Box<String> boxValue = Box.ofVolatile("contain");
		Box<String> boxValueFast = Box.of("contain");
		Box<String> boxFromMethods = Box.from(boxValue::get, boxValue::set);

		expectToString.accept(boxValue, "Box.ofVolatile[contain]");
		expectToString.accept(boxValueFast, "Box.of[contain]");
		expectToString.accept(boxFromMethods, "Box.from[contain]");

		// standard Box.Nullable
		Box.Nullable<String> boxValueNullable = Box.Nullable.ofVolatileNull();
		Box.Nullable<String> boxValueNullableFast = Box.Nullable.ofNull();
		Box.Nullable<String> boxFromMethodsNullable = Box.Nullable.from(boxValueNullable::get, boxValueNullable::set);

		expectToString.accept(boxValueNullable, "Box.Nullable.ofVolatile[null]");
		expectToString.accept(boxValueNullableFast, "Box.Nullable.of[null]");
		expectToString.accept(boxFromMethodsNullable, "Box.Nullable.from[null]");

		boxValueNullable.set("contain");
		boxValueNullableFast.set("contain");
		expectToString.accept(boxValueNullable, "Box.Nullable.ofVolatile[contain]");
		expectToString.accept(boxValueNullableFast, "Box.Nullable.of[contain]");
		expectToString.accept(boxFromMethodsNullable, "Box.Nullable.from[contain]");

		// standard Box.Dbl
		Box.Dbl dblValue = Box.Dbl.ofVolatile(0);
		Box.Dbl dblValueFast = Box.Dbl.of(0);
		Box.Dbl dblFromMethods = Box.Dbl.from(dblValue::getAsDouble, dblValue::set);

		expectToString.accept(dblValue, "Box.Dbl.ofVolatile[0.0]");
		expectToString.accept(dblValueFast, "Box.Dbl.of[0.0]");
		expectToString.accept(dblFromMethods, "Box.Dbl.from[0.0]");

		// standard Box.Int
		Box.Int intValue = Box.Int.ofVolatile(0);
		Box.Int intValueFast = Box.Int.of(0);
		Box.Int intFromMethods = Box.Int.from(intValue::getAsInt, intValue::set);

		expectToString.accept(intValue, "Box.Int.ofVolatile[0]");
		expectToString.accept(intValueFast, "Box.Int.of[0]");
		expectToString.accept(intFromMethods, "Box.Int.from[0]");

		// standard Box.Long
		Box.Lng longValue = Box.Lng.ofVolatile(0);
		Box.Lng longValueFast = Box.Lng.of(0);
		Box.Lng longFromMethod = Box.Lng.from(longValue::getAsLong, longValue::set);

		expectToString.accept(longValue, "Box.Long.ofVolatile[0]");
		expectToString.accept(longValueFast, "Box.Long.of[0]");
		expectToString.accept(longFromMethod, "Box.Long.from[0]");
	}

	@Test
	public void testFromMethods() {
		Box<String> testValue = Box.ofVolatile("");
		Box<String> forValue = Box.from(testValue::get, testValue::set);
		forValue.set("A");
		Assert.assertEquals("A", forValue.get());
		forValue.set("B");
		Assert.assertEquals("B", forValue.get());

		Function<Object, String> toString = Object::toString;
		System.out.println("toString=" + toString);
	}
}
