/*
 * Original Guava code is copyright (C) 2015 The Guava Authors.
 * Modifications from Guava are copyright (C) 2016 DiffPlug.
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
package com.diffplug.common.base;

import static com.google.common.truth.Truth.assertThat;

import java.util.function.BiConsumer;

import org.junit.Test;

import com.google.common.util.concurrent.Runnables;

/** Stupid-simple test for the very simple Box class. */
public class BoxTest {
	@Test
	public void testToString() {
		Converter<String, String> converter = Converter.from(str -> "_" + str, str -> str.substring(1), "f");
		BiConsumer<Object, String> expectToString = (obj, expected) -> {
			assertThat(obj.toString()).isEqualTo(expected);
		};

		// standard Box
		{
			Box<String> boxOf = Box.of("contain");
			Box<String> boxOfVolatile = Box.ofVolatile("contain");
			Box<String> boxFrom = Box.from(boxOfVolatile::get, boxOfVolatile::set);

			expectToString.accept(boxOf, "Box.of[contain]");
			expectToString.accept(boxOfVolatile, "Box.ofVolatile[contain]");
			expectToString.accept(boxFrom, "Box.from[contain]");

			expectToString.accept(boxOf.map(converter), "[Box.of[contain] mapped to [_contain] by f]");
			expectToString.accept(boxOfVolatile.map(converter), "[Box.ofVolatile[contain] mapped to [_contain] by f]");
			expectToString.accept(boxFrom.map(converter), "[Box.from[contain] mapped to [_contain] by f]");
		}

		// standard Box.Nullable
		{
			Box.Nullable<String> boxOf = Box.Nullable.of("contain");
			Box.Nullable<String> boxOfVolatile = Box.Nullable.ofVolatile("contain");
			Box.Nullable<String> boxFrom = Box.Nullable.from(boxOfVolatile::get, boxOfVolatile::set);

			expectToString.accept(boxOf, "Box.Nullable.of[contain]");
			expectToString.accept(boxOfVolatile, "Box.Nullable.ofVolatile[contain]");
			expectToString.accept(boxFrom, "Box.Nullable.from[contain]");

			expectToString.accept(boxOf.map(converter), "[Box.Nullable.of[contain] mapped to [_contain] by f]");
			expectToString.accept(boxOfVolatile.map(converter), "[Box.Nullable.ofVolatile[contain] mapped to [_contain] by f]");
			expectToString.accept(boxFrom.map(converter), "[Box.Nullable.from[contain] mapped to [_contain] by f]");

			boxOf.set(null);
			boxOfVolatile.set(null);

			expectToString.accept(boxOf, "Box.Nullable.of[null]");
			expectToString.accept(boxOfVolatile, "Box.Nullable.ofVolatile[null]");
			expectToString.accept(boxFrom, "Box.Nullable.from[null]");

			expectToString.accept(boxOf.map(converter), "[Box.Nullable.of[null] mapped to [null] by f]");
			expectToString.accept(boxOfVolatile.map(converter), "[Box.Nullable.ofVolatile[null] mapped to [null] by f]");
			expectToString.accept(boxFrom.map(converter), "[Box.Nullable.from[null] mapped to [null] by f]");
		}

		// Box.Dbl
		Box.Dbl dblValue = Box.Dbl.of(0);
		Box.Dbl dblFromMethods = Box.Dbl.from(dblValue::getAsDouble, dblValue::set);

		expectToString.accept(dblValue, "Box.Dbl.of[0.0]");
		expectToString.accept(dblFromMethods, "Box.Dbl.from[0.0]");

		// Box.Int
		Box.Int intValue = Box.Int.of(0);
		Box.Int intFromMethods = Box.Int.from(intValue::getAsInt, intValue::set);

		expectToString.accept(intValue, "Box.Int.of[0]");
		expectToString.accept(intFromMethods, "Box.Int.from[0]");

		// Box.Long
		Box.Lng longValue = Box.Lng.of(0);
		Box.Lng longFromMethod = Box.Lng.from(longValue::getAsLong, longValue::set);

		expectToString.accept(longValue, "Box.Lng.of[0]");
		expectToString.accept(longFromMethod, "Box.Lng.from[0]");
	}

	@Test
	public void getSetModify() {
		getSetModifyCase(Box.of("init"), "init");
		getSetModifyCase(Box.ofVolatile("init"), "init");
		Box<String> root = Box.of("init");
		Box<String> from = Box.from(root::get, root::set);
		getSetModifyCase(from, "init");

		getSetModifyCase(Box.Nullable.of(null), null);
		getSetModifyCase(Box.Nullable.ofVolatile(null), null);
		Box.Nullable<String> rootNullable = Box.Nullable.of(null);
		Box.Nullable<String> fromNullable = Box.Nullable.from(rootNullable::get, rootNullable::set);
		getSetModifyCase(fromNullable, null);
	}

	@Test
	public void getSetModifyAndMapped() {
		Converter<String, String> converter = Converter.from(
				str -> "123" + str,
				str -> str.substring(3));
		{
			Box<String> value = Box.of("init");
			Box<String> mapped = value.map(converter);
			Runnable assertMapped = () -> {
				assertThat("123" + value.get()).isEqualTo(mapped.get());
			};
			getSetModifyCase(value, "init", assertMapped);
		}
		{
			Box<String> value = Box.ofVolatile("init");
			Box<String> mapped = value.map(converter);
			Runnable assertMapped = () -> {
				assertThat("123" + value.get()).isEqualTo(mapped.get());
			};
			getSetModifyCase(value, "init", assertMapped);
		}
		{
			Box<String> root = Box.of("init");
			Box<String> value = Box.from(root::get, root::set);
			Box<String> mapped = value.map(converter);
			Runnable assertMapped = () -> {
				assertThat("123" + value.get()).isEqualTo(mapped.get());
			};
			getSetModifyCase(value, "init", assertMapped);
		}

		{
			Box.Nullable<String> value = Box.Nullable.of(null);
			Box.Nullable<String> mapped = value.map(converter);
			Runnable assertMapped = () -> {
				assertThat(value.get() == null ? null : "123" + value.get()).isEqualTo(mapped.get());
			};
			getSetModifyCase(value, null, assertMapped);
		}
		{
			Box.Nullable<String> value = Box.Nullable.ofVolatile(null);
			Box.Nullable<String> mapped = value.map(converter);
			Runnable assertMapped = () -> {
				assertThat(value.get() == null ? null : "123" + value.get()).isEqualTo(mapped.get());
			};
			getSetModifyCase(value, null, assertMapped);
		}
		{
			Box.Nullable<String> root = Box.Nullable.of("init");
			Box.Nullable<String> value = Box.Nullable.from(root::get, root::set);
			Box.Nullable<String> mapped = value.map(converter);
			Runnable assertMapped = () -> {
				assertThat(value.get() == null ? null : "123" + value.get()).isEqualTo(mapped.get());
			};
			getSetModifyCase(value, "init", assertMapped);
		}
	}

	private void getSetModifyCase(Box<String> box, String initial, Runnable afterEachAssertion) {
		assertThat(box.get()).isEqualTo(initial);
		afterEachAssertion.run();

		box.set("other");
		assertThat(box.get()).isEqualTo("other");
		afterEachAssertion.run();

		assertThat(box.modify(other -> other + "_")).isEqualTo("other_");
		afterEachAssertion.run();
	}

	private void getSetModifyCase(Box.Nullable<String> box, String initial, Runnable afterEachAssertion) {
		assertThat(box.get()).isEqualTo(initial);
		afterEachAssertion.run();

		box.set("other");
		assertThat(box.get()).isEqualTo("other");
		afterEachAssertion.run();

		assertThat(box.modify(other -> other + "_")).isEqualTo("other_");
		afterEachAssertion.run();
	}

	private void getSetModifyCase(Box<String> box, String initial) {
		getSetModifyCase(box, initial, Runnables.doNothing());
	}

	private void getSetModifyCase(Box.Nullable<String> box, String initial) {
		getSetModifyCase(box, initial, Runnables.doNothing());
	}

	@SuppressWarnings("deprecation")
	@Test
	public void getSetModifyPrimitives() {
		// Box.Dbl
		{
			Box.Dbl box = Box.Dbl.of(0);
			Box.Dbl from = Box.Dbl.from(box::getAsDouble, box::set);
			assertThat(box.getAsDouble()).isEqualTo(0.0);
			assertThat(box.get()).isEqualTo(0.0);
			assertThat(from.getAsDouble()).isEqualTo(0.0);
			assertThat(from.get()).isEqualTo(0.0);

			assertThat(box.modify(v -> v + 1)).isEqualTo(1.0);
			assertThat(box.getAsDouble()).isEqualTo(1.0);
			assertThat(box.get()).isEqualTo(1.0);
			assertThat(from.getAsDouble()).isEqualTo(1.0);
			assertThat(from.get()).isEqualTo(1.0);

			assertThat(from.modify(v -> v + 1)).isEqualTo(2.0);
			assertThat(box.getAsDouble()).isEqualTo(2.0);
			assertThat(box.get()).isEqualTo(2.0);
			assertThat(from.getAsDouble()).isEqualTo(2.0);
			assertThat(from.get()).isEqualTo(2.0);

			box.set(-1);
			assertThat(box.getAsDouble()).isEqualTo(-1.0);
			assertThat(box.get()).isEqualTo(-1.0);
			assertThat(from.getAsDouble()).isEqualTo(-1.0);
			assertThat(from.get()).isEqualTo(-1.0);
		}

		// Box.Int
		{
			Box.Int box = Box.Int.of(0);
			Box.Int from = Box.Int.from(box::getAsInt, box::set);
			assertThat(box.getAsInt()).isEqualTo(0);
			assertThat(box.get()).isEqualTo(0);
			assertThat(from.getAsInt()).isEqualTo(0);
			assertThat(from.get()).isEqualTo(0);

			assertThat(box.modify(v -> v + 1)).isEqualTo(1);
			assertThat(box.getAsInt()).isEqualTo(1);
			assertThat(box.get()).isEqualTo(1);
			assertThat(from.getAsInt()).isEqualTo(1);
			assertThat(from.get()).isEqualTo(1);

			assertThat(from.modify(v -> v + 1)).isEqualTo(2);
			assertThat(box.getAsInt()).isEqualTo(2);
			assertThat(box.get()).isEqualTo(2);
			assertThat(from.getAsInt()).isEqualTo(2);
			assertThat(from.get()).isEqualTo(2);

			box.set(-1);
			assertThat(box.getAsInt()).isEqualTo(-1);
			assertThat(box.get()).isEqualTo(-1);
			assertThat(from.getAsInt()).isEqualTo(-1);
			assertThat(from.get()).isEqualTo(-1);
		}

		// Box.Long
		{
			Box.Lng box = Box.Lng.of(0);
			Box.Lng from = Box.Lng.from(box::getAsLong, box::set);
			assertThat(box.getAsLong()).isEqualTo(0);
			assertThat(box.get()).isEqualTo(0);
			assertThat(from.getAsLong()).isEqualTo(0);
			assertThat(from.get()).isEqualTo(0);

			assertThat(box.modify(v -> v + 1)).isEqualTo(1);
			assertThat(box.getAsLong()).isEqualTo(1);
			assertThat(box.get()).isEqualTo(1);
			assertThat(from.getAsLong()).isEqualTo(1);
			assertThat(from.get()).isEqualTo(1);

			assertThat(from.modify(v -> v + 1)).isEqualTo(2);
			assertThat(box.getAsLong()).isEqualTo(2);
			assertThat(box.get()).isEqualTo(2);
			assertThat(from.getAsLong()).isEqualTo(2);
			assertThat(from.get()).isEqualTo(2);

			box.set(-1);
			assertThat(box.getAsLong()).isEqualTo(-1);
			assertThat(box.get()).isEqualTo(-1);
			assertThat(from.getAsLong()).isEqualTo(-1);
			assertThat(from.get()).isEqualTo(-1);
		}
	}
}
