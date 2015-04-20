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

import java.util.function.Consumer;

import org.junit.Assert;
import org.junit.Test;

public class DurianPluginsTest {
	@Test
	public void testErrorHandlerLogDefault() throws InterruptedException {
		// TODO: test that the logging is happening
	}

	/*
	@Test
	public void testErrorHandlerDialogDefault() throws InterruptedException {
		// if you want to tinker with the error handler dialog, uncomment this test
		new DurianPlugins().getErrorHandlerDialog().accept(new Throwable("This dialog will close in 1 second"));
		Thread.sleep(1000);
	}
	*/

	@Test
	public void testErrorHandlerLogViaRegisterMethod() {
		TestLogHandler logHandler = new TestLogHandler();

		DurianPlugins plugins = new DurianPlugins();
		plugins.registerErrorHandlerLog(logHandler);
		Assert.assertEquals(logHandler, plugins.getErrorHandlerLog());
	}

	@Test
	public void testErrorHandlerLogViaProperty() {
		try {
			System.setProperty("durian.plugin.ErrorHandlerLog.implementation", TestLogHandler.class.getName());
			DurianPlugins plugins = new DurianPlugins();
			Consumer<Throwable> impl = plugins.getErrorHandlerLog();
			Assert.assertTrue(impl instanceof TestLogHandler);
		} finally {
			System.clearProperty("rxjava.plugin.RxJavaErrorHandler.implementation");
		}
	}

	static class TestLogHandler implements DurianPlugins.ErrorHandlerLog {
		@Override
		public void accept(Throwable error) {
			throw new UnsupportedOperationException("No such thing as an error in Durian.");
		}
	}

	@Test
	public void testWiresNotCrossed() {
		TestLogHandler log = new TestLogHandler();
		DurianPlugins.ErrorHandlerDialog dialog = error -> {};

		DurianPlugins plugins = new DurianPlugins();
		plugins.registerErrorHandlerLog(log);
		plugins.registerErrorHandlerDialog(dialog);

		Assert.assertEquals(log, plugins.getErrorHandlerLog());
		Assert.assertEquals(dialog, plugins.getErrorHandlerDialog());
		Assert.assertFalse(plugins.getErrorHandlerDialog() == plugins.getErrorHandlerLog());
	}
}
