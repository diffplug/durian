/**
 * Copyright (C) 2015 DiffPlug, LLC - All Rights Reserved
 * Unauthorized copying of this file via any medium is strictly prohibited.
 * Proprietary and confidential.
 * Please send any inquiries to Ned Twigg <ned.twigg@diffplug.com>
 */
package com.diffplug.common.base;

import java.util.function.Consumer;
import java.util.function.Supplier;

/** Utilities for creating consumers. */
public class Consumers {
	/** A consumer which does nothing. */
	public static <T> Consumer<T> doNothing() {
		return value -> {};
	}

	/**
	 * A Consumer which always passes its input to the given target.
	 * 
	 * By passing something mutable, such as a Box<Consumer<T>>, as
	 * the input, you can redirect the returned consumer.
	 */
	public static <T> Consumer<T> redirectable(Supplier<Consumer<T>> target) {
		return value -> target.get().accept(value);
	}
}
