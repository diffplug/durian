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

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/** Helper functions for manipulating {@link Consumer}. */
public class Consumers {
	/** A Consumer which does nothing. */
	public static <T> Consumer<T> doNothing() {
		return value -> {};
	}

	/** The equivalent of {@link Function#compose}, but for Consumer. */
	public static <T, R> Consumer<T> compose(Function<? super T, ? extends R> function, Consumer<? super R> consumer) {
		return value -> consumer.accept(function.apply(value));
	}

	/**
	 * A Consumer which always passes its its input to whatever Consumer is supplied by target.
	 * <p>
	 * By passing something mutable, such as a {@link Box}, you can redirect the returned consumer.
	 */
	public static <T> Consumer<T> redirectable(Supplier<Consumer<T>> target) {
		return value -> target.get().accept(value);
	}
}
