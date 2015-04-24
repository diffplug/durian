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
 * A simple class for receiving a stream of Strings.
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

	/**
	 * Creates an OutputStream which will print its content to the given StringPrinter, encoding bytes according to the given Charset.
	 * Doesn't matter if you close the stream or not, because StringPrinter doesn't have a close().
	 * 
	 * Strings are sent to the consumer as soon as their consituent bytes are written to this OutputStream.
	 * 
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
		return ErrorHandler.rethrow().get(() -> {
			return new PrintStream(toOutputStream(charset), true, charset.name());
		} );
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
				StringBuilder sb = new StringBuilder(csq.length());
				sb.append(csq);
				consumer.accept(sb.toString());
				return this;
			}

			@Override
			public Writer append(CharSequence csq, int start, int end) {
				StringBuilder sb = new StringBuilder(end - start);
				sb.append(csq.subSequence(start, end));
				consumer.accept(sb.toString());
				return this;
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
}
