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
package com.diffplug.common.util.concurrent.testing;

import static com.diffplug.common.util.concurrent.MoreExecutors.directExecutor;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import com.diffplug.common.annotations.Beta;
import com.diffplug.common.util.concurrent.ListenableFuture;

/**
 * A simple mock implementation of {@code Runnable} that can be used for
 * testing ListenableFutures.
 *
 * @author Nishant Thakkar
 * @since 10.0
 */
@Beta
public class MockFutureListener implements Runnable {
	private final CountDownLatch countDownLatch;
	private final ListenableFuture<?> future;

	public MockFutureListener(ListenableFuture<?> future) {
		this.countDownLatch = new CountDownLatch(1);
		this.future = future;

		future.addListener(this, directExecutor());
	}

	@Override
	public void run() {
		countDownLatch.countDown();
	}

	/**
	 * Verify that the listener completes in a reasonable amount of time, and
	 * Asserts that the future returns the expected data.
	 * @throws Throwable if the listener isn't called or if it resulted in a
	 *     throwable or if the result doesn't match the expected value.
	 */
	public void assertSuccess(Object expectedData) throws Throwable {
		// Verify that the listener executed in a reasonable amount of time.
		Assert.assertTrue(countDownLatch.await(1L, TimeUnit.SECONDS));

		try {
			Assert.assertEquals(expectedData, future.get());
		} catch (ExecutionException e) {
			throw e.getCause();
		}
	}

	/**
	 * Verify that the listener completes in a reasonable amount of time, and
	 * Asserts that the future throws an {@code ExecutableException} and that the
	 * cause of the {@code ExecutableException} is {@code expectedCause}.
	 */
	public void assertException(Throwable expectedCause) throws Exception {
		// Verify that the listener executed in a reasonable amount of time.
		Assert.assertTrue(countDownLatch.await(1L, TimeUnit.SECONDS));

		try {
			future.get();
			Assert.fail("This call was supposed to throw an ExecutionException");
		} catch (ExecutionException expected) {
			Assert.assertSame(expectedCause, expected.getCause());
		}
	}

	public void assertTimeout() throws Exception {
		// Verify that the listener does not get called in a reasonable amount of
		// time.
		Assert.assertFalse(countDownLatch.await(1L, TimeUnit.SECONDS));
	}
}
