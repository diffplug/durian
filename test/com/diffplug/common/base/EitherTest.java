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

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;

public class EitherTest {
	@Test
	public void testLeft() {
		Either<TimeUnit, String> left = Either.createLeft(TimeUnit.DAYS);
		Assert.assertTrue(left.isLeft());
		Assert.assertFalse(left.isRight());
		Assert.assertEquals(TimeUnit.DAYS, left.getLeft());
		Assert.assertEquals(Optional.of(TimeUnit.DAYS), left.asOptionalLeft());
		Assert.assertEquals(Optional.empty(), left.asOptionalRight());
		try {
			left.getRight();
			Assert.fail();
		} catch (Unhandled e) {}
		Assert.assertEquals("DAYS", left.fold(TimeUnit::toString, Function.identity()));

		Box.Nullable<TimeUnit> leftSide = Box.Nullable.ofNull();
		Box.Nullable<String> rightSide = Box.Nullable.ofNull();
		left.acceptBoth(leftSide, rightSide, TimeUnit.HOURS, "wahoo");
		Assert.assertEquals(TimeUnit.DAYS, leftSide.get());
		Assert.assertEquals("wahoo", rightSide.get());
	}

	@Test
	public void testRight() {
		Either<TimeUnit, String> right = Either.createRight("word");
		Assert.assertTrue(right.isRight());
		Assert.assertFalse(right.isLeft());
		Assert.assertEquals("word", right.getRight());
		Assert.assertEquals(Optional.of("word"), right.asOptionalRight());
		Assert.assertEquals(Optional.empty(), right.asOptionalLeft());
		try {
			right.getLeft();
			Assert.fail();
		} catch (Unhandled e) {}
		Assert.assertEquals("word", right.fold(TimeUnit::toString, Function.identity()));

		Box.Nullable<TimeUnit> leftSide = Box.Nullable.ofNull();
		Box.Nullable<String> rightSide = Box.Nullable.ofNull();
		right.acceptBoth(leftSide, rightSide, TimeUnit.HOURS, "wahoo");
		Assert.assertEquals(TimeUnit.HOURS, leftSide.get());
		Assert.assertEquals("word", rightSide.get());
	}

	@Test
	public void testMapForRight() {
		Either<Double, Integer> source = Either.create(null, 42);
		{
			Either<String, Integer> mapLeft = source.mapLeft(Object::toString);
			Assert.assertFalse(mapLeft.asOptionalLeft().isPresent());
			Assert.assertEquals(new Integer(42), mapLeft.asOptionalRight().get());
		}
		{
			Either<Double, String> mapRight = source.mapRight(Object::toString);
			Assert.assertFalse(mapRight.asOptionalLeft().isPresent());
			Assert.assertEquals("42", mapRight.asOptionalRight().get());
		}
	}

	@Test
	public void testMapForLeft() {
		Either<Double, Integer> source = Either.create(3.14, null);
		{
			Either<String, Integer> mapLeft = source.mapLeft(Object::toString);
			Assert.assertFalse(mapLeft.asOptionalRight().isPresent());
			Assert.assertEquals("3.14", mapLeft.asOptionalLeft().get());
		}
		{
			Either<Double, String> mapRight = source.mapRight(Object::toString);
			Assert.assertFalse(mapRight.asOptionalRight().isPresent());
			Assert.assertEquals(new Double(3.14), mapRight.asOptionalLeft().get());
		}
	}
}
