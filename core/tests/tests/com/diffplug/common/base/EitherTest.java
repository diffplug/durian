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

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;

import com.diffplug.common.testing.EqualsTester;

public class EitherTest {
	/** Harness for testing functions which take two Consumers. */
	private <L, R> void assertBothSides(L expectedLeft, R expectedRight, BiConsumer<Consumer<L>, Consumer<R>> underTest) {
		Box.Nullable<L> leftSide = Box.Nullable.ofNull();
		Box.Nullable<R> rightSide = Box.Nullable.ofNull();
		underTest.accept(leftSide, rightSide);
		Assert.assertEquals(expectedLeft, leftSide.get());
		Assert.assertEquals(expectedRight, rightSide.get());
	}

	/** Harness for testing functions which take one Consumer. */
	private <T> void assertOneSide(T expected, Consumer<Consumer<T>> underTest) {
		Box.Nullable<T> result = Box.Nullable.ofNull();
		underTest.accept(result);
		Assert.assertEquals(expected, result.get());
	}

	@Test
	public void testLeft() {
		Either<TimeUnit, String> left = Either.createLeft(TimeUnit.DAYS);
		// assert the original
		assertOnLeft(left);
		// assert after it has had both sides mapped
		assertOnLeft(left.mapLeft(Function.identity()));
		assertOnLeft(left.mapRight(unused -> {
			throw new AssertionError("Shouldn't have been called");
		}));
	}

	private void assertOnLeft(Either<TimeUnit, String> left) {
		Assert.assertTrue(left.isLeft());
		Assert.assertFalse(left.isRight());
		Assert.assertEquals(TimeUnit.DAYS, left.getLeft());
		Assert.assertEquals(Optional.of(TimeUnit.DAYS), left.asOptionalLeft());
		Assert.assertEquals(Optional.empty(), left.asOptionalRight());
		try {
			left.getRight();
			Assert.fail("Expected exception");
		} catch (NoSuchElementException e) {}
		Assert.assertEquals("DAYS", left.fold(TimeUnit::toString, Function.identity()));

		assertOneSide(TimeUnit.DAYS, left::ifLeft);
		assertOneSide(null, left::ifRight);

		assertBothSides(TimeUnit.DAYS, "wahoo", (l, r) -> left.acceptBoth(l, r, TimeUnit.HOURS, "wahoo"));
		assertBothSides(TimeUnit.DAYS, null, (l, r) -> left.accept(l, r));
		Assert.assertEquals("DAYS", left.fold(Object::toString, Object::toString));
	}

	@Test
	public void testRight() {
		Either<TimeUnit, String> right = Either.createRight("word");
		// assert the original
		assertOnRight(right);
		// assert after it has had both sides mapped
		assertOnRight(right.mapRight(Function.identity()));
		assertOnRight(right.mapLeft(unused -> {
			throw new AssertionError("Shouldn't have been called");
		}));
	}

	private void assertOnRight(Either<TimeUnit, String> right) {
		Assert.assertTrue(right.isRight());
		Assert.assertFalse(right.isLeft());
		Assert.assertEquals("word", right.getRight());
		Assert.assertEquals(Optional.of("word"), right.asOptionalRight());
		Assert.assertEquals(Optional.empty(), right.asOptionalLeft());
		try {
			right.getLeft();
			Assert.fail("Expected exception");
		} catch (NoSuchElementException e) {}
		Assert.assertEquals("word", right.fold(TimeUnit::toString, Function.identity()));

		assertOneSide(null, right::ifLeft);
		assertOneSide("word", right::ifRight);

		assertBothSides(TimeUnit.HOURS, "word", (l, r) -> right.acceptBoth(l, r, TimeUnit.HOURS, "wahoo"));
		assertBothSides(null, "word", (l, r) -> right.accept(l, r));
		Assert.assertEquals("word", right.fold(Object::toString, Object::toString));
	}

	@Test
	public void testCreate() {
		Assert.assertEquals(Either.createLeft("left"), Either.create("left", null));
		Assert.assertEquals(Either.createRight("right"), Either.create(null, "right"));
		try {
			Either.create(null, null);
			Assert.fail("Expected exception");
		} catch (IllegalArgumentException e) {}
		try {
			Either.create("left", "right");
			Assert.fail("Expected exception");
		} catch (IllegalArgumentException e) {}
	}

	@Test
	public void testEquals() {
		new EqualsTester()
				.addEqualityGroup(Either.createLeft("test"), Either.createLeft("test"))
				.addEqualityGroup(Either.createRight("test"), Either.createRight("test"))
				.testEquals();
	}
}
