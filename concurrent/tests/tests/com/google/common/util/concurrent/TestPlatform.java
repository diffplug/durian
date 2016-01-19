/*
 * Original Guava code is copyright (C) 2015 The Guava Authors.
 * Modifications from Guava are copyright (C) 2015 DiffPlug.
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
package com.google.common.util.concurrent;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.util.concurrent.FuturesTest.failureWithCause;
import static com.google.common.util.concurrent.FuturesTest.pseudoTimedGetUninterruptibly;
import static com.google.common.util.concurrent.Uninterruptibles.getUninterruptibly;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.fail;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import com.google.common.annotations.GwtCompatible;

/**
 * Methods factored out so that they can be emulated differently in GWT.
 */
@GwtCompatible(emulated = true)
final class TestPlatform {
	static void verifyGetOnPendingFuture(Future<?> future) {
		checkNotNull(future);
		try {
			pseudoTimedGetUninterruptibly(future, 10, MILLISECONDS);
			fail();
		} catch (TimeoutException expected) {} catch (ExecutionException e) {
			throw failureWithCause(e, "");
		}
	}

	static void verifyTimedGetOnPendingFuture(Future<?> future) {
		try {
			getUninterruptibly(future, 0, SECONDS);
			fail();
		} catch (TimeoutException expected) {} catch (ExecutionException e) {
			throw failureWithCause(e, "");
		}
	}

	static void verifyThreadWasNotInterrupted() {
		assertFalse(Thread.currentThread().isInterrupted());
	}

	static void clearInterrupt() {
		Thread.interrupted();
	}

	private TestPlatform() {}
}
