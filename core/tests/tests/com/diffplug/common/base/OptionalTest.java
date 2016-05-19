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

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import junit.framework.TestCase;

import com.diffplug.common.annotations.GwtCompatible;
import com.diffplug.common.annotations.GwtIncompatible;
import com.diffplug.common.collect.FluentIterable;
import com.diffplug.common.collect.ImmutableList;
import com.diffplug.common.testing.NullPointerTester;

/**
 * Unit test for {@link Optional}.
 *
 * @author Kurt Alfred Kluever
 */
@GwtCompatible(emulated = true)
public final class OptionalTest extends TestCase {
	public void testAbsent() {
		Optional<String> optionalName = Optional.empty();
		assertFalse(optionalName.isPresent());
	}

	public void testOf() {
		assertEquals("training", Optional.of("training").get());
	}

	@SuppressWarnings("CheckReturnValue")
	public void testOf_null() {
		try {
			Optional.of(null);
			fail();
		} catch (NullPointerException expected) {}
	}

	public void testFromNullable() {
		Optional<String> optionalName = Optional.ofNullable("bob");
		assertEquals("bob", optionalName.get());
	}

	public void testFromNullable_null() {
		// not promised by spec, but easier to test
		assertSame(Optional.empty(), Optional.ofNullable(null));
	}

	public void testIsPresent_no() {
		assertFalse(Optional.empty().isPresent());
	}

	public void testIsPresent_yes() {
		assertTrue(Optional.of("training").isPresent());
	}

	@SuppressWarnings("CheckReturnValue")
	public void testGet_empty() {
		Optional<String> optional = Optional.empty();
		try {
			optional.get();
			fail();
		} catch (NoSuchElementException expected) {}
	}

	public void testGet_present() {
		assertEquals("training", Optional.of("training").get());
	}

	public void testOr_T_present() {
		assertEquals("a", Optional.of("a").orElse("default"));
	}

	public void testOr_T_empty() {
		assertEquals("default", Optional.empty().orElse("default"));
	}

	public void testOr_supplier_present() {
		assertEquals("a", Optional.of("a").orElseGet(Suppliers.ofInstance("fallback")));
	}

	public void testOr_supplier_empty() {
		assertEquals("fallback", Optional.empty().orElseGet(Suppliers.ofInstance("fallback")));
	}

	@SuppressWarnings("CheckReturnValue")
	public void testOr_nullSupplier_empty() {
		Supplier<Object> nullSupplier = Suppliers.ofInstance(null);
		Optional<Object> absentOptional = Optional.empty();
		assertNull(absentOptional.orElseGet(nullSupplier));
	}

	public void testOr_nullSupplier_present() {
		Supplier<String> nullSupplier = Suppliers.ofInstance(null);
		assertEquals("a", Optional.of("a").orElseGet(nullSupplier));
	}

	public void testOr_Optional_present() {
		assertEquals("a", Optional.of("a").orElse("fallback"));
	}

	public void testOr_Optional_empty() {
		assertEquals(Optional.of("fallback"), Optional.empty().orElse(Optional.of("fallback")));
	}

	public void testOrNull_present() {
		assertEquals("a", Optional.of("a").orElse(null));
	}

	public void testOrNull_empty() {
		assertNull(Optional.empty().orElse(null));
	}

	// TODO(kevinb): use EqualsTester

	public void testEqualsAndHashCode_empty() {
		assertEquals(Optional.<String> empty(), Optional.<Integer> empty());
		assertEquals(Optional.empty().hashCode(), Optional.empty().hashCode());
		assertThat(Optional.empty().hashCode())
				.isNotEqualTo(Optional.of(1).hashCode());
	}

	public void testEqualsAndHashCode_present() {
		assertEquals(Optional.of("training"), Optional.of("training"));
		assertFalse(Optional.of("a").equals(Optional.of("b")));
		assertFalse(Optional.of("a").equals(Optional.empty()));
		assertEquals(Optional.of("training").hashCode(), Optional.of("training").hashCode());
	}

	public void testToString_empty() {
		assertEquals("Optional.empty", Optional.empty().toString());
	}

	public void testToString_present() {
		assertEquals("Optional[training]", Optional.of("training").toString());
	}

	private static Optional<Integer> getSomeOptionalInt() {
		return Optional.of(1);
	}

	private static FluentIterable<? extends Number> getSomeNumbers() {
		return FluentIterable.from(ImmutableList.<Number> of());
	}

	/*
	 * The following tests demonstrate the shortcomings of or() and test that the casting workaround
	 * mentioned in the method Javadoc does in fact compile.
	 */

	@SuppressWarnings("unused") // compilation test
	public void testSampleCodeError1() {
		Optional<Integer> optionalInt = getSomeOptionalInt();
		// Number value = optionalInt.orElse(0.5); // error
	}

	@SuppressWarnings("unused") // compilation test
	public void testSampleCodeError2() {
		FluentIterable<? extends Number> numbers = getSomeNumbers();
		Optional<? extends Number> first = numbers.first();
		// Number value = first.orElse(0.5); // error
	}

	@SuppressWarnings("unused") // compilation test
	public void testSampleCodeFine1() {
		Optional<Number> optionalInt = Optional.of((Number) 1);
		Number value = optionalInt.orElse(0.5); // fine
	}

	@SuppressWarnings("unused") // compilation test
	public void testSampleCodeFine2() {
		FluentIterable<? extends Number> numbers = getSomeNumbers();

		// Sadly, the following is what users will have to do in some circumstances.

		@SuppressWarnings("unchecked") // safe covariant cast
		Optional<Number> first = (Optional) numbers.first();
		Number value = first.orElse(0.5); // fine
	}

	@GwtIncompatible("NullPointerTester")
	public void testNullPointers() throws NoSuchMethodException, SecurityException {
		NullPointerTester npTester = new NullPointerTester();
		npTester.ignore(Optional.class.getMethod("equals", Object.class));
		npTester.ignore(Optional.class.getMethod("ifPresent", Consumer.class));
		npTester.ignore(Optional.class.getMethod("ofNullable", Object.class));
		npTester.ignore(Optional.class.getMethod("orElse", Object.class));
		npTester.ignore(Optional.class.getMethod("orElseGet", Supplier.class));
		npTester.ignore(Optional.class.getMethod("orElseThrow", Supplier.class));
		npTester.testAllPublicConstructors(Optional.class);
		npTester.testAllPublicStaticMethods(Optional.class);
		npTester.testAllPublicInstanceMethods(Optional.empty());
		npTester.testAllPublicInstanceMethods(Optional.of("training"));
	}
}
