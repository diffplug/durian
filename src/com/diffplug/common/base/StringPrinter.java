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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * Pipes strings to a {@code Consumer<String>} through an API similar to PrintWriter and PrintStream.
 * 
 * Can present itself as an {@link OutputStream}, {@link PrintStream}, {@link Writer}, or {@link PrintWriter}.
 */
public class StringPrinter {
	private final Consumer<String> consumer;

	/** StringPrinter will pass all the strings it receives to the given consumer. */
	public StringPrinter(Consumer<String> consumer) {
		this.consumer = consumer;
	}

	/** Prints the string and a newline (always '\n'). */
	public void println(String line) {
		consumer.accept(line);
		consumer.accept("\n");
	}

	/** Prints the string. */
	public void print(String content) {
		consumer.accept(content);
	}

	/** Easy way to create a String using a StringPrinter. */
	public static String buildString(Consumer<StringPrinter> printer) {
		StringBuilder builder = new StringBuilder();
		printer.accept(new StringPrinter(builder::append));
		return builder.toString();
	}

	/** Easy way to create a String from a bunch of lines. */
	public static String buildStringFromLines(String... lines) {
		int numChars = lines.length;
		for (String line : lines) {
			numChars += line.length();
		}
		StringBuilder builder = new StringBuilder(numChars);
		for (String line : lines) {
			builder.append(line);
			builder.append('\n');
		}
		return builder.toString();
	}

	/**
	 * Creates an OutputStream which will print its content to the given StringPrinter, encoding bytes according to the given Charset.
	 * Doesn't matter if you close the stream or not, because StringPrinter doesn't have a close().
	 * <p>
	 * Strings are sent to the consumer as soon as their constituent bytes are written to this OutputStream.
	 * <p>
	 * The implementation is lifted from Apache commons-io.  Many thanks to them!
	 */
	public OutputStream toOutputStream(Charset charset) {
		CharsetDecoder decoder = charset.newDecoder()
				.onMalformedInput(CodingErrorAction.REPLACE)
				.onUnmappableCharacter(CodingErrorAction.REPLACE)
				.replaceWith("?");

		ByteBuffer decoderIn = ByteBuffer.allocate(DECODER_BUFFER);
		CharBuffer decoderOut = CharBuffer.allocate(DECODER_BUFFER);
		return new OutputStream() {
			@Override
			public void write(final int b) throws IOException {
				write(new byte[]{(byte) b});
			}

			@Override
			public void write(byte[] b) throws IOException {
				write(b, 0, b.length);
			}

			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				while (len > 0) {
					final int c = Math.min(len, decoderIn.remaining());
					decoderIn.put(b, off, c);
					processInput(false);
					len -= c;
					off += c;
				}
				flushOutput();
			}

			private void processInput(final boolean endOfInput) throws IOException {
				// Prepare decoderIn for reading
				decoderIn.flip();
				CoderResult coderResult;
				while (true) {
					coderResult = decoder.decode(decoderIn, decoderOut, endOfInput);
					if (coderResult.isOverflow()) {
						flushOutput();
					} else if (coderResult.isUnderflow()) {
						break;
					} else {
						// The decoder is configured to replace malformed input and unmappable characters,
						// so we should not get here.
						throw new IOException("Unexpected coder result");
					}
				}
				// Discard the bytes that have been read
				decoderIn.compact();
			}

			private void flushOutput() throws IOException {
				if (decoderOut.position() > 0) {
					consumer.accept(new String(decoderOut.array(), 0, decoderOut.position()));
					decoderOut.rewind();
				}
			}
		};
	}

	/** Buffer size for decoding characters. */
	private static final int DECODER_BUFFER = 128;

	/** Creates a UTF-8 PrintStream which passes its content to this StringPrinter. */
	public PrintStream toPrintStream() {
		return toPrintStream(StandardCharsets.UTF_8);
	}

	/** Creates a PrintStream of the given charset, which passes its content to this StringPrinter. */
	public PrintStream toPrintStream(Charset charset) {
		return Errors.rethrow().get(() -> {
			return new PrintStream(toOutputStream(charset), true, charset.name());
		});
	}

	/** Creates a Writer which passes its content to this StringPrinter. */
	public Writer toWriter() {
		return new Writer() {
			@Override
			public Writer append(char c) {
				consumer.accept(new String(new char[]{c}));
				return this;
			}

			@Override
			public Writer append(CharSequence csq) {
				if (csq instanceof String) {
					consumer.accept((String) csq);
				} else {
					consumer.accept(toStringSafely(csq));
				}
				return this;
			}

			@Override
			public Writer append(CharSequence csq, int start, int end) {
				if (csq instanceof String) {
					consumer.accept(((String) csq).substring(start, end));
				} else {
					consumer.accept(toStringSafely(csq.subSequence(start, end)));
				}
				return this;
			}

			private String toStringSafely(CharSequence csq) {
				String asString = csq.toString();
				if (asString.length() == csq.length()) {
					return asString;
				} else {
					// It's pretty easy to implement CharSequence.toString() incorrectly 
					// http://stackoverflow.com/a/15870428/1153071
					// but for String, we know we won't have them, thus the fast-path above
					Errors.log().accept(new IllegalArgumentException(csq.getClass() + " did not implement toString() correctly."));
					char[] chars = new char[csq.length()];
					for (int i = 0; i < chars.length; ++i) {
						chars[i] = csq.charAt(i);
					}
					return new String(chars);
				}
			}

			@Override
			public void close() throws IOException {}

			@Override
			public void flush() throws IOException {}

			@Override
			public void write(char[] cbuf, int off, int len) throws IOException {
				consumer.accept(new String(cbuf, off, len));
			}

			@Override
			public void write(String str) {
				consumer.accept(str);
			}

			@Override
			public void write(String str, int off, int len) {
				consumer.accept(str.substring(off, off + len));
			}
		};
	}

	/** Creates a PrintWriter which passes its content to this StringPrinter. */
	public PrintWriter toPrintWriter() {
		boolean autoflush = true;
		return new PrintWriter(toWriter(), autoflush);
	}

	/**
	 * Given a consumer of lines, creates a stateful consumer of strings
	 * which will combine its input until it finds a newline, and
	 * split its input when it contains multiple newlines.  Examples
	 * make this more clear:
	 * <pre>
	 * "some", "\n", "simple ", "lines", "\n" -> "some", "simple lines"
	 * "some\nsimple lines\n"                 -> "some", "simple lines"
	 * "no newline\nno output"                -> "no newline"
	 * </pre>
	 * @param perLine a Consumer<String> which will receive strings which were terminated by newlines (but aren't anymore).
	 * @return a Consumer<String> which accepts any strings, and will feed them to perLine. 
	 */
	public static Consumer<String> stringsToLines(Consumer<String> perLine) {
		Box<String> leftover = Box.of("");
		return rawString -> {
			rawString = leftover.get() + rawString.replace("\r", "");

			int lastIdx = 0;
			int idx = 0;
			while ((idx = rawString.indexOf('\n', lastIdx)) > -1) {
				perLine.accept(rawString.substring(lastIdx, idx));
				lastIdx = idx + 1;
			}
			leftover.set(rawString.substring(lastIdx));
		};
	}

	/** Returns a StringPrinter for {@link System#out}. */
	public static StringPrinter systemOut() {
		return new StringPrinter(System.out::print);
	}

	/** Returns a StringPrinter for {@link System#err}. */
	public static StringPrinter systemErr() {
		return new StringPrinter(System.err::print);
	}
}
