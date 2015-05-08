/**
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

import com.diffplug.common.base.ErrorHandler.Plugins.Dialog;
import com.diffplug.common.base.ErrorHandler.Plugins.Log;
import com.diffplug.common.base.ErrorHandler.Plugins.Rethrow;
import com.diffplug.common.base.ErrorHandler.Plugins.Suppress;

public class ErrorHandlerTest {
	@Test(expected = AssertionError.class)
	public void testAssertionPlugin() {
		DurianPlugins.resetForTesting();
		ErrorHandler.resetForTesting();
		try {
			// set the Suppress handler to the be the OnErrorThrowAssertion
			System.setProperty("durian.plugins.com.diffplug.common.base.ErrorHandler.Plugins.Suppress",
					"com.diffplug.common.base.ErrorHandler$Plugins$OnErrorThrowAssertion");
			// send something to the suppress handler, but we should get an AssertionError
			ErrorHandler.suppress().run(() -> {
				throw new RuntimeException("Didn't see this coming.");
			});
		} finally {
			System.clearProperty("durian.plugins.com.diffplug.common.base.ErrorHandler.Plugins.Suppress");
			DurianPlugins.resetForTesting();
			ErrorHandler.resetForTesting();
		}
	}

	@Test
	public void testWrapWithDefault() {
		// function
		Assert.assertEquals("called", ErrorHandler.suppress().wrapWithDefault(input -> "called", "default").apply(null));
		Assert.assertEquals("default", ErrorHandler.suppress().wrapWithDefault(input -> {
			throw new IllegalArgumentException();
		}, "default").apply(null));
		// supplier
		Assert.assertEquals("called", ErrorHandler.suppress().wrapWithDefault(() -> "called", "default").get());
		Assert.assertEquals("default", ErrorHandler.suppress().wrapWithDefault(() -> {
			throw new IllegalArgumentException();
		}, "default").get());
		// predicate
		Assert.assertEquals(true, ErrorHandler.suppress().wrapWithDefault(input -> true, false).test(null));
		Assert.assertEquals(false, ErrorHandler.suppress().wrapWithDefault(input -> {
			throw new IllegalArgumentException();
		}, false).test(null));
	}

	@Test
	public void testWiresCrossed() {
		DurianPlugins.resetForTesting();
		ErrorHandler.resetForTesting();

		DurianPlugins.register(ErrorHandler.Plugins.Suppress.class, new TestHandler("Suppress"));
		DurianPlugins.register(ErrorHandler.Plugins.Rethrow.class, new TestHandler("Rethrow"));
		DurianPlugins.register(ErrorHandler.Plugins.Log.class, new TestHandler("Log"));
		DurianPlugins.register(ErrorHandler.Plugins.Dialog.class, new TestHandler("Dialog"));

		try {
			try {
				ErrorHandler.suppress().run(ErrorHandlerTest::throwException);
			} catch (RuntimeException e) {
				Assert.assertEquals("Suppress", e.getMessage());
			}
			try {
				ErrorHandler.rethrow().run(ErrorHandlerTest::throwException);
			} catch (RuntimeException e) {
				Assert.assertEquals("Rethrow", e.getMessage());
			}
			try {
				ErrorHandler.log().run(ErrorHandlerTest::throwException);
			} catch (RuntimeException e) {
				Assert.assertEquals("Log", e.getMessage());
			}
			try {
				ErrorHandler.dialog().run(ErrorHandlerTest::throwException);
			} catch (RuntimeException e) {
				Assert.assertEquals("Dialog", e.getMessage());
			}
		} finally {
			DurianPlugins.resetForTesting();
			ErrorHandler.resetForTesting();
		}
	}

	private static void throwException() {
		throw new RuntimeException();
	}

	/** Implementation of the various ErrorHandlers which throws a RuntimeException with the given message. */
	public static class TestHandler implements Suppress, Rethrow, Log, Dialog {
		private String message;

		public TestHandler(String message) {
			this.message = message;
		}

		@Override
		public void accept(Throwable error) {
			throw apply(error);
		}

		@Override
		public RuntimeException apply(Throwable error) {
			return new RuntimeException(message);
		}
	}
}
