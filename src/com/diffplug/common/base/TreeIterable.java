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

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/** Creates {@code Iterable}s that iterate across a tree defined by a {@link TreeDef} in various orders. */
public class TreeIterable {
	private TreeIterable() {}

	/** Creates an {@code Iterable} that starts at {@code node} and ends at its root parent.  */
	public static <T> Iterable<T> toParent(TreeDef.Parented<T> treeDef, T node) {
		return () -> new Iterator<T>() {
			T tip = node;

			@Override
			public boolean hasNext() {
				return tip != null;
			}

			@Override
			public T next() {
				if (tip == null) {
					throw new NoSuchElementException();
				}
				T next = tip;
				tip = treeDef.parentOf(tip);
				return next;
			}
		};
	}

	/** Creates an {@code Iterable} that starts at {@code node} and iterates deeper into the tree in a bread-first order. */
	public static <T> Iterable<T> breadthFirst(TreeDef<T> treeDef, T node) {
		return () -> new Iterator<T>() {
			Deque<T> queue = new ArrayDeque<>(Arrays.asList(node));

			@Override
			public boolean hasNext() {
				return !queue.isEmpty();
			}

			@Override
			public T next() {
				if (queue.isEmpty()) {
					throw new NoSuchElementException();
				}
				T next = queue.removeFirst();
				treeDef.childrenOf(next).forEach(queue::addLast);
				return next;
			}
		};
	}

	/** Creates an {@code Iterable} that starts at {@code node} and iterates deeper into the tree in a depth-first order. */
	public static <T> Iterable<T> depthFirst(TreeDef<T> treeDef, T node) {
		return () -> new Iterator<T>() {
			Deque<T> queue = new ArrayDeque<>(Arrays.asList(node));

			@Override
			public boolean hasNext() {
				return !queue.isEmpty();
			}

			@Override
			public T next() {
				if (queue.isEmpty()) {
					throw new NoSuchElementException();
				}
				T next = queue.removeLast();
				List<T> children = treeDef.childrenOf(next);
				ListIterator<T> iterator = children.listIterator(children.size());
				while (iterator.hasPrevious()) {
					queue.addLast(iterator.previous());
				}
				return next;
			}
		};
	}
}
