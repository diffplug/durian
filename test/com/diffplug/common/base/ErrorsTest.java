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

import com.diffplug.common.base.Errors.Plugins.Dialog;
import com.diffplug.common.base.Errors.Plugins.Log;

public class ErrorsTest {
	@Test(expected = AssertionError.class)
	public void testAssertionPlugin() {
		DurianPlugins.resetForTesting();
		Errors.resetForTesting();
		try {
			// set the Log handler to the be the OnErrorThrowAssertion
			System.setProperty("durian.plugins.com.diffplug.common.base.Errors.Plugins.Log",
					"com.diffplug.common.base.Errors$Plugins$OnErrorThrowAssertion");
			// send something to the suppress handler, but we should get an AssertionError
			Errors.log().run(() -> {
				throw new RuntimeException("Didn't see this coming.");
			});
		} finally {
			System.clearProperty("durian.plugins.com.diffplug.common.base.Errors.Plugins.Suppress");
			DurianPlugins.resetForTesting();
			Errors.resetForTesting();
		}
	}

	@Test
	public void testWrapWithDefault() {
		// function
		Assert.assertEquals("called", Errors.suppress().wrapWithDefault(input -> "called", "default").apply(null));
		Assert.assertEquals("default", Errors.suppress().wrapWithDefault(input -> {
			throw new IllegalArgumentException();
		}, "default").apply(null));
		// supplier
		Assert.assertEquals("called", Errors.suppress().wrapWithDefault(() -> "called", "default").get());
		Assert.assertEquals("default", Errors.suppress().wrapWithDefault(() -> {
			throw new IllegalArgumentException();
		}, "default").get());
		// predicate
		Assert.assertEquals(true, Errors.suppress().wrapWithDefault(input -> true, false).test(null));
		Assert.assertEquals(false, Errors.suppress().wrapWithDefault(input -> {
			throw new IllegalArgumentException();
		}, false).test(null));
	}

	@Test
	public void testWiresCrossed() {
		DurianPlugins.resetForTesting();
		Errors.resetForTesting();

		DurianPlugins.register(Errors.Plugins.Log.class, new TestHandler("Log"));
		DurianPlugins.register(Errors.Plugins.Dialog.class, new TestHandler("Dialog"));

		try {
			Errors.suppress().run(ErrorsTest::throwException);

			try {
				Errors.rethrow().run(ErrorsTest::throwException);
			} catch (RuntimeException e) {
				// it should pass the RuntimeException unphased
				Assert.assertNull(e.getCause());
			}
			try {
				Errors.log().run(ErrorsTest::throwException);
			} catch (RuntimeException e) {
				Assert.assertEquals("Log", e.getMessage());
			}
			try {
				Errors.dialog().run(ErrorsTest::throwException);
			} catch (RuntimeException e) {
				Assert.assertEquals("Dialog", e.getMessage());
			}
		} finally {
			DurianPlugins.resetForTesting();
			Errors.resetForTesting();
		}
	}

	private static void throwException() {
		throw new RuntimeException();
	}

	/** Implementation of the various Errors which throws a RuntimeException with the given message. */
	public static class TestHandler implements Log, Dialog {
		private String message;

		public TestHandler(String message) {
			this.message = message;
		}

		@Override
		public void accept(Throwable error) {
			throw new RuntimeException(message);
		}
	}
}
