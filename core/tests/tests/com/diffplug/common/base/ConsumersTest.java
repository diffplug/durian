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

import java.util.function.Consumer;

import org.junit.Test;

public class ConsumersTest {
	@Test
	public void testDoNothing() {
		Consumers.doNothing().accept(null);
		Consumers.doNothing().accept(new Object());
	}

	@Test
	public void testCompose() {
		Box.Nullable<Integer> parsed = Box.Nullable.ofNull();
		Consumer<String> underTest = Consumers.compose(Integer::parseInt, parsed);
		underTest.accept("5");
		assertThat(parsed.get()).isEqualTo(5);
	}

	@Test
	public void testRedirectable() {
		Box.Nullable<String> a = Box.Nullable.ofNull();
		Box.Nullable<String> b = Box.Nullable.ofNull();

		Box<Consumer<String>> target = Box.of(a);
		Consumer<String> underTest = Consumers.redirectable(target);

		assertThat(a.get()).isNull();
		assertThat(b.get()).isNull();

		underTest.accept("a");
		assertThat(a.get()).isEqualTo("a");
		assertThat(b.get()).isNull();

		target.set(b);
		underTest.accept("b");
		assertThat(a.get()).isEqualTo("a");
		assertThat(b.get()).isEqualTo("b");
	}
}
