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

import javax.annotation.Nullable;

/** Exceptions designed for checking programming errors (e.g. unexpected default or else clauses). */
public class Unhandled extends IllegalArgumentException {
	private static final long serialVersionUID = 1781209245301127264L;

	public Unhandled(String message) {
		super(message);
	}

	public static Unhandled classException(Object o) {
		if (o == null) {
			return new Unhandled("Unhandled class 'null'");
		} else {
			return new Unhandled("Unhandled class '" + o.getClass().getName() + "'");
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

	public static Unhandled operationException() {
		return new Unhandled("Unsupported operation.");
	}

	public static Unhandled exception(String template, Object... args) {
		return new Unhandled(format(template, args));
	}

	/**
	 * Ripped from Guava's Preconditions.format().
	 * 
	 * Substitutes each {@code %s} in {@code template} with an argument. These are matched by
	 * position: the first {@code %s} gets {@code args[0]}, etc.  If there are more arguments than
	 * placeholders, the unmatched arguments will be appended to the end of the formatted message in
	 * square braces.
	 *
	 * @param template a non-null string containing 0 or more {@code %s} placeholders.
	 * @param args the arguments to be substituted into the message template. Arguments are converted
	 *     to strings using {@link String#valueOf(Object)}. Arguments can be null.
	 */
	static String format(String template, @Nullable Object... args) {
		template = String.valueOf(template); // null -> "null"

		// start substituting the arguments into the '%s' placeholders
		StringBuilder builder = new StringBuilder(template.length() + 16 * args.length);
		int templateStart = 0;
		int i = 0;
		while (i < args.length) {
			int placeholderStart = template.indexOf("%s", templateStart);
			if (placeholderStart == -1) {
				break;
			}
			builder.append(template.substring(templateStart, placeholderStart));
			builder.append(args[i++]);
			templateStart = placeholderStart + 2;
		}
		builder.append(template.substring(templateStart));

		// if we run out of placeholders, append the extra args in square braces
		if (i < args.length) {
			builder.append(" [");
			builder.append(args[i++]);
			while (i < args.length) {
				builder.append(", ");
				builder.append(args[i++]);
			}
			builder.append(']');
		}

		return builder.toString();
	}
}
