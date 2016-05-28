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

/** Exceptions designed for checking programming errors (e.g. unexpected default or else clauses). */
public class Unhandled extends IllegalArgumentException {
	private static final long serialVersionUID = 0L;

	public Unhandled(String message) {
		super(message);
	}

	public static Unhandled classException(Object o) {
		if (o == null) {
			return new Unhandled("Unhandled class 'null'");
		} else {
			if (o instanceof Class) {
				return new Unhandled("Unhandled class '" + ((Class<?>) o).getName() + "'");
			} else {
				return new Unhandled("Unhandled class '" + o.getClass().getName() + "'");
			}
		}
	}

	public static Unhandled enumException(Enum<?> e) {
		if (e == null) {
			return new Unhandled("Unhandled enum value 'null'");
		} else {
			return new Unhandled("Unhandled enum value '" + e.name() + "' for enum class '" + e.getClass() + "'");
		}
	}

	public static Unhandled byteException(byte b) {
		return new Unhandled("Unhandled byte '" + b + "'");
	}

	public static Unhandled charException(char c) {
		return new Unhandled("Unhandled char '" + c + "'");
	}

	public static Unhandled shortException(short s) {
		return new Unhandled("Unhandled short '" + s + "'");
	}

	public static Unhandled integerException(int i) {
		return new Unhandled("Unhandled integer '" + i + "'");
	}

	public static Unhandled floatException(float f) {
		return new Unhandled("Unhandled float '" + f + "'");
	}

	public static Unhandled doubleException(double d) {
		return new Unhandled("Unhandled double '" + d + "'");
	}

	public static Unhandled stringException(String str) {
		return new Unhandled("Unhandled string '" + str + "'");
	}

	public static Unhandled objectException(Object obj) {
		return new Unhandled("Unhandled object '" + obj + "'");
	}
}
