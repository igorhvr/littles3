/*
 * Copyright 2007 Jesse Peterson
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

package com.jpeterson.util.http;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * <p>
 * <code>RangeInputStream</code> is a <code>FilterInputStream</code> that
 * masks off portions of the <code>InputStream</code> that do not fall within
 * the <code>Range</code>. This can be helpful in implementing an HTTP server
 * that accepts Range requests.
 * </p>
 * <p>
 * This also supports reading sequential ranges. For instance, you could first
 * read range 2-5 and then range 12-17.
 * </p>
 * 
 * @author Jesse Peterson
 */
public class RangeInputStream extends FilterInputStream {
	private Range range;

	private long index = 0;

	/**
	 * Create a new <code>RangeInputStream</code> based on the provided
	 * <code>InputStream</code> and apply the provided <code>Range</code>.
	 * 
	 * @param in
	 *            The <code>InputStream</code> to filter.
	 * @param range
	 *            The <code>Range</code> to apply.
	 * @throws IllegalArgumentException
	 *             Thrown if the current read index is beyond the range start.
	 *             This isn't likely in the constructor, as a valid range start
	 *             value minimum is 0 and the read index also starts at 0.
	 */
	public RangeInputStream(InputStream in, Range range) {
		super(in);
		setRange(range);
	}

	/**
	 * Get the <code>Range</code> currently being applied.
	 * 
	 * @return The <code>Range</code> currently being applied.
	 */
	public Range getRange() {
		return range;
	}

	/**
	 * Set the <code>Range</code> to apply.
	 * 
	 * @param range
	 *            The <code>Range</code> to apply.
	 * @throws IllegalArgumentException
	 *             Thrown if the current read index is beyond the range start.
	 */
	public void setRange(Range range) {
		if (index > range.getStart()) {
			throw new IllegalArgumentException("Current read index[" + index
					+ "] is beyond range start[" + range.getStart() + "]");
		}

		this.range = range;
	}

	/**
	 * Reads the next byte of data from the input stream that is within the
	 * specified range.
	 * 
	 * @return The next by te of data, or <code>-1</code> if the end of the
	 *         stream is reached.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	public int read() throws IOException {
		long count;
		long start;

		// skip data before the range
		start = range.getStart();
		while (index < start) {
			// get rid of the data in front
			count = in.skip(start - index);
			index += count;
		}

		// have we reached the end of the range
		if (index > range.getEnd()) {
			return -1;
		}

		int c = in.read();
		if (c == -1) {
			return -1;
		}

		index++;

		return c;
	}

	/**
	 * Simple implementation that uses the <code>java.io.InputStream</code>
	 * basic implementatin that just calls <code>read()</code> multiple times.
	 * 
	 * @para b the buffer into which the data is read.
	 * @param off
	 *            the start offset in array <code>b</code> at which the data
	 *            is written
	 * @param len
	 *            the maximum number of bytes to read.
	 * @throws IOException
	 *             if an I/O error occurs.
	 * @throws NullPointerException
	 *             if <code>b</code> is <code>null</code>.
	 * @see #read()
	 */
	public int simpleread(byte[] b, int off, int len) throws IOException {
		// from java.io.InputStream implementation
		if (b == null) {
			throw new NullPointerException();
		} else if ((off < 0) || (off > b.length) || (len < 0)
				|| ((off + len) > b.length) || ((off + len) < 0)) {
			throw new IndexOutOfBoundsException();
		} else if (len == 0) {
			return 0;
		}

		int c = read();
		if (c == -1) {
			return -1;
		}
		b[off] = (byte) c;

		int i = 1;
		try {
			for (; i < len; i++) {
				c = read();
				if (c == -1) {
					break;
				}
				if (b != null) {
					b[off + i] = (byte) c;
				}
			}
		} catch (IOException e) {

		}
		return i;
	}

	/**
	 * Advanced implementation that performs a more efficient read of a buffer
	 * of data.
	 * 
	 * @para b the buffer into which the data is read.
	 * @param off
	 *            the start offset in array <code>b</code> at which the data
	 *            is written
	 * @param len
	 *            the maximum number of bytes to read.
	 * @throws IOException
	 *             if an I/O error occurs.
	 * @throws NullPointerException
	 *             if <code>b</code> is <code>null</code>.
	 * @see #read()
	 */
	public int read(byte[] b, int off, int len) throws IOException {
		// from java.io.InputStream implementation
		if (b == null) {
			throw new NullPointerException();
		} else if ((off < 0) || (off > b.length) || (len < 0)
				|| ((off + len) > b.length) || ((off + len) < 0)) {
			throw new IndexOutOfBoundsException();
		} else if (len == 0) {
			return 0;
		}

		long count;
		long start, end;
		int read;

		// skip data before the range
		start = range.getStart();
		while (index < start) {
			// get rid of the data in front
			count = in.skip(start - index);
			index += count;
		}

		// have we reached the end of the range
		end = range.getEnd();
		if (index > end) {
			return -1;
		}

		long available = end - index + 1;

		// only read up to the end of the range
		if (available < len) {
			read = in.read(b, off, (int) available);
		} else {
			read = in.read(b, off, len);
		}

		index += read;

		return read;
	}
}
