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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import junit.framework.TestCase;

import com.diffplug.common.annotations.GwtCompatible;
import com.diffplug.common.annotations.GwtIncompatible;
import com.diffplug.common.collect.ImmutableSet;
import com.diffplug.common.testing.GcFinalization;
import com.diffplug.common.testing.NullPointerTester;
import com.diffplug.common.testing.SerializableTester;

/**
 * Tests for {@link Enums}.
 *
 * @author Steve McKay
 */
@GwtCompatible(emulated = true)
public class EnumsTest extends TestCase {

	private enum TestEnum {
		CHEETO, HONDA, POODLE,
	}

	public void testGetIfPresent() {
		assertThat(Enums.getIfPresent(TestEnum.class, "CHEETO")).isEqualTo(Optional.of(TestEnum.CHEETO));
		assertThat(Enums.getIfPresent(TestEnum.class, "HONDA")).isEqualTo(Optional.of(TestEnum.HONDA));
		assertThat(Enums.getIfPresent(TestEnum.class, "POODLE")).isEqualTo(Optional.of(TestEnum.POODLE));

		assertThat(Enums.getIfPresent(TestEnum.class, "CHEETO")).isEqualTo(Optional.of(TestEnum.CHEETO));
		assertThat(Enums.getIfPresent(TestEnum.class, "HONDA")).isEqualTo(Optional.of(TestEnum.HONDA));
		assertThat(Enums.getIfPresent(TestEnum.class, "POODLE")).isEqualTo(Optional.of(TestEnum.POODLE));
	}

	public void testGetIfPresent_caseSensitive() {
		assertThat(Enums.getIfPresent(TestEnum.class, "cHEETO")).isEqualTo(Optional.empty());
		assertThat(Enums.getIfPresent(TestEnum.class, "Honda")).isEqualTo(Optional.empty());
		assertThat(Enums.getIfPresent(TestEnum.class, "poodlE")).isEqualTo(Optional.empty());
	}

	public void testGetIfPresent_whenNoMatchingConstant() {
		assertThat(Enums.getIfPresent(TestEnum.class, "WOMBAT")).isEqualTo(Optional.empty());
	}

	@GwtIncompatible("weak references")
	public void testGetIfPresent_doesNotPreventClassUnloading() throws Exception {
		WeakReference<?> shadowLoaderReference = doTestClassUnloading();
		GcFinalization.awaitClear(shadowLoaderReference);
	}

	// Create a second ClassLoader and use it to get a second version of the TestEnum class.
	// Run Enums.getIfPresent on that other TestEnum and then return a WeakReference containing the
	// new ClassLoader. If Enums.getIfPresent does caching that prevents the shadow TestEnum
	// (and therefore its ClassLoader) from being unloaded, then this WeakReference will never be
	// cleared.
	@GwtIncompatible("weak references")
	private WeakReference<?> doTestClassUnloading() throws Exception {
		URLClassLoader myLoader = (URLClassLoader) getClass().getClassLoader();
		URLClassLoader shadowLoader = new URLClassLoader(myLoader.getURLs(), null);
		@SuppressWarnings("unchecked")
		Class<TestEnum> shadowTestEnum = (Class<TestEnum>) Class.forName(TestEnum.class.getName(), false, shadowLoader);
		assertNotSame(shadowTestEnum, TestEnum.class);
		Set<TestEnum> shadowConstants = new HashSet<TestEnum>();
		for (TestEnum constant : TestEnum.values()) {
			Optional<TestEnum> result = Enums.getIfPresent(shadowTestEnum, constant.name());
			assertTrue(result.isPresent());
			shadowConstants.add(result.get());
		}
		assertEquals(ImmutableSet.copyOf(shadowTestEnum.getEnumConstants()), shadowConstants);
		Optional<TestEnum> result = Enums.getIfPresent(shadowTestEnum, "blibby");
		assertThat(result).isEqualTo(Optional.empty());
		return new WeakReference<ClassLoader>(shadowLoader);
	}

	public void testStringConverter_convert() {
		Converter<String, TestEnum> converter = Enums.stringConverter(TestEnum.class);
		assertEquals(TestEnum.CHEETO, converter.convert("CHEETO"));
		assertEquals(TestEnum.HONDA, converter.convert("HONDA"));
		assertEquals(TestEnum.POODLE, converter.convert("POODLE"));
		assertNull(converter.convert(null));
		assertNull(converter.reverse().convert(null));
	}

	public void testStringConverter_convertError() {
		Converter<String, TestEnum> converter = Enums.stringConverter(TestEnum.class);
		try {
			converter.convert("xxx");
			fail();
		} catch (IllegalArgumentException expected) {}
	}

	public void testStringConverter_reverse() {
		Converter<String, TestEnum> converter = Enums.stringConverter(TestEnum.class);
		assertEquals("CHEETO", converter.reverse().convert(TestEnum.CHEETO));
		assertEquals("HONDA", converter.reverse().convert(TestEnum.HONDA));
		assertEquals("POODLE", converter.reverse().convert(TestEnum.POODLE));
	}

	@GwtIncompatible("NullPointerTester")
	public void testStringConverter_nullPointerTester() throws Exception {
		Converter<String, TestEnum> converter = Enums.stringConverter(TestEnum.class);
		NullPointerTester tester = new NullPointerTester();
		tester.testAllPublicInstanceMethods(converter);
	}

	public void testStringConverter_nullConversions() {
		Converter<String, TestEnum> converter = Enums.stringConverter(TestEnum.class);
		assertNull(converter.convert(null));
		assertNull(converter.reverse().convert(null));
	}

	@GwtIncompatible("Class.getName()")
	public void testStringConverter_toString() {
		assertEquals(
				"Enums.stringConverter(com.diffplug.common.base.EnumsTest$TestEnum.class)",
				Enums.stringConverter(TestEnum.class).toString());
	}

	public void testStringConverter_serialization() {
		SerializableTester.reserializeAndAssert(Enums.stringConverter(TestEnum.class));
	}

	@GwtIncompatible("NullPointerTester")
	public void testNullPointerExceptions() {
		NullPointerTester tester = new NullPointerTester();
		tester.testAllPublicStaticMethods(Enums.class);
	}

	@Retention(RetentionPolicy.RUNTIME)
	private @interface ExampleAnnotation {}

	private enum AnEnum {
		@ExampleAnnotation
		FOO, BAR
	}

	@GwtIncompatible("reflection")
	public void testGetField() {
		Field foo = Enums.getField(AnEnum.FOO);
		assertEquals("FOO", foo.getName());
		assertTrue(foo.isAnnotationPresent(ExampleAnnotation.class));

		Field bar = Enums.getField(AnEnum.BAR);
		assertEquals("BAR", bar.getName());
		assertFalse(bar.isAnnotationPresent(ExampleAnnotation.class));
	}
}
