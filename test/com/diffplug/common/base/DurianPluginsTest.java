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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DurianPluginsTest {
	private List<Class<?>> pluginsToStore = Arrays.asList(Errors.Plugins.Log.class);
	private Map<Class<?>, Optional<String>> state = new HashMap<>();

	/** Create a fresh instance of DurianPlugins for each test, and save the system properties that we're gonna futz with. */
	@Before
	public void before() {
		DurianPlugins.resetForTesting();
		for (Class<?> plugin : pluginsToStore) {
			String key = DurianPlugins.PROPERTY_PREFIX + plugin.getCanonicalName();
			// store the current value
			Optional<String> value = Optional.ofNullable(System.getProperty(key));
			state.put(plugin, value);
			// then clear the property
			System.clearProperty(key);
		}
	}

	/** Restore the system properties. */
	@After
	public void after() {
		for (Class<?> plugin : pluginsToStore) {
			String key = DurianPlugins.PROPERTY_PREFIX + plugin.getCanonicalName();
			// restore the stored value
			Optional<String> value = state.get(plugin);
			if (value.isPresent()) {
				System.setProperty(key, value.get());
			} else {
				System.clearProperty(key);
			}
		}
		DurianPlugins.resetForTesting();
	}

	@Test
	public void testRegister() {
		// the instance that will be set as a default
		TestLogHandler logHandler = new TestLogHandler();
		// set the plugin as the default
		DurianPlugins.register(Errors.Plugins.Log.class, logHandler);
		// make sure that it returns the value we set, and not the default value
		Assert.assertEquals(logHandler, DurianPlugins.get(Errors.Plugins.Log.class, new TheWrongLogHandler()));
	}

	@Test(expected = IllegalStateException.class)
	public void testRegisterTooLate() {
		TestLogHandler logHandler = new TestLogHandler();
		// set the value using a default value
		Assert.assertEquals(logHandler, DurianPlugins.get(Errors.Plugins.Log.class, logHandler));
		// try to set the value using register (it should throw an exception)
		DurianPlugins.register(Errors.Plugins.Log.class, logHandler);
	}

	@Test
	public void testSystemProperty() {
		// set the system property to TestLogHandler's name
		System.setProperty("durian.plugins.com.diffplug.common.base.Errors.Plugins.Log", TestLogHandler.class.getName());
		// get the property with TheWrongLogHandler as the default
		Consumer<Throwable> impl = DurianPlugins.get(Errors.Plugins.Log.class, new TheWrongLogHandler());
		// make sure it's the right value
		Assert.assertTrue(impl instanceof TestLogHandler);
	}

	@Test
	public void testDefault() {
		// set the value using the default
		TestLogHandler firstDefault = new TestLogHandler();
		Consumer<Throwable> firstCall = DurianPlugins.get(Errors.Plugins.Log.class, firstDefault);
		Assert.assertEquals(firstDefault, firstCall);

		// get with a different default, but it should still be the first one
		Consumer<Throwable> secondCall = DurianPlugins.get(Errors.Plugins.Log.class, new TheWrongLogHandler());
		Assert.assertEquals(firstDefault, secondCall);
	}

	static class TestLogHandler implements Errors.Plugins.Log {
		@Override
		public void accept(Throwable error) {
			throw new UnsupportedOperationException("No such thing as an error in Durian.");
		}
	}

	static class TheWrongLogHandler implements Errors.Plugins.Log {
		@Override
		public void accept(Throwable error) {}
	}
}
