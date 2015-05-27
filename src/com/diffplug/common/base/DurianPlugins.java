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

import java.util.concurrent.ConcurrentHashMap;

/**
 * A registry for plugin implementations which obeys the following contract:
 * 
 * Once someone has requested a class from DurianPlugins, whatever instance was
 * returned will continue to be returned for every future call. This gives the
 * impression of a global constant.  Before the value has been requested, it
 * can be set programmatically or by setting a system property, which allows
 * configuring a system or framework appropriate behavior.
 * 
 * This eternal instance is determined by:
 * - the first call to register(Class<T> pluginClass, T pluginImpl)
 * - if a system property named "durian.plugins.{pluginClass.getCanonicalName()}" is set to
 *  the fully-qualified name of an implementation class with a no-argument constructor, then
 *  an instance of that class will be instantiated and used as the plugin implementation
 *  - the defaultImpl that was specified in the first call to get()
 *
 * This class was inspired by <a href="https://github.com/ReactiveX/RxJava/blob/86147542573004f4df84d2a2de83577cf62fe787/src/main/java/rx/plugins/RxJavaPlugins.java">RxJava's RxJavaPlugins</a>. Many thanks to them!
 */
public class DurianPlugins {
	/** Resets the plugin instance for testing. */
	static void resetForTesting() {
		INSTANCE = new DurianPlugins();
	}

	private DurianPlugins() {}

	private static DurianPlugins INSTANCE = new DurianPlugins();

	/** Map which supports atomic putIfAbsent() and computeIfAbsent(). */
	private final ConcurrentHashMap<Class<?>, Object> map = new ConcurrentHashMap<>();

	/**
	 * Registers an implementation as a global override of any injected or default implementations.
	 * 
	 * @param pluginClass The interface which is being registered.
	 * @param pluginImpl  The implementation of that interface which is being registered.
	 * @throws IllegalStateException If called more than once or if a value has already been requested.
	 */
	public static <T> void register(Class<T> pluginClass, T pluginImpl) throws IllegalStateException {
		INSTANCE.registerInternal(pluginClass, pluginImpl);
	}

	private <T> void registerInternal(Class<T> pluginClass, T pluginImpl) throws IllegalStateException {
		assert(pluginClass.isInstance(pluginImpl));
		Object existingValue = map.putIfAbsent(pluginClass, pluginImpl);
		if (existingValue != null) {
			throw new IllegalStateException("Another " + pluginClass + " was already registered: " + existingValue);
		}
	}

	/**
	 * Returns an instance of pluginClass which is guaranteed to be identical throughout
	 * the runtime existence of this library. The returned instance is determined by:
	 * 
	 * - the first call to register(Class<T> pluginClass, T pluginImpl)
	 * - if a system property named "durian.plugins.{pluginClass.getCanonicalName()}" is set to
	 *  the fully-qualified name of an implementation class with a no-argument constructor, then
	 *  an instance of that class will be instantiated and used as the plugin implementation
	 *  - the defaultImpl that was specified in the first call to get()
	 *  
	 * @param pluginClass The interface which is being requested.
	 * @param defaultImpl A default implementation of that interface.
	 * @return An instance of pluginClass, which is guaranteed to be returned for every future request for pluginClass.
	 */
	public static <T> T get(Class<T> pluginClass, T defaultImpl) {
		return INSTANCE.getInternal(pluginClass, defaultImpl);
	}

	@SuppressWarnings("unchecked")
	private <T> T getInternal(Class<T> pluginClass, T defaultImpl) {
		assert(pluginClass.isInstance(defaultImpl));
		return (T) map.computeIfAbsent(pluginClass, clazz -> {
			// check for an implementation from System.getProperty first
			Object impl = getPluginImplementationViaProperty(clazz);
			return impl != null ? impl : defaultImpl;
		});
	}

	/** Returns an implementation of the given class using the system properties as a registry. */
	private static Object getPluginImplementationViaProperty(Class<?> pluginClass) {
		String className = pluginClass.getCanonicalName();
		if (className == null) {
			throw new IllegalArgumentException("Class " + pluginClass + " does not have a canonical name!");
		}
		// Check system properties for plugin class.
		// This will only happen during system startup thus it's okay to use the synchronized
		// System.getProperties as it will never get called in normal operations.
		String implementingClass = System.getProperty(PROPERTY_PREFIX + className);
		if (implementingClass != null) {
			try {
				Class<?> cls = Class.forName(implementingClass);
				// narrow the scope (cast) to the type we're expecting
				cls = cls.asSubclass(pluginClass);
				return cls.newInstance();
			} catch (ClassCastException e) {
				throw new RuntimeException(className + " implementation is not an instance of " + className + ": " + implementingClass);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(className + " implementation class not found: " + implementingClass, e);
			} catch (InstantiationException e) {
				throw new RuntimeException(className + " implementation not able to be instantiated: " + implementingClass, e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(className + " implementation not able to be accessed: " + implementingClass, e);
			}
		} else {
			return null;
		}
	}

	/** Prefix to system property keys for specifying plugin classes. */
	public static final String PROPERTY_PREFIX = "durian.plugins.";
}
