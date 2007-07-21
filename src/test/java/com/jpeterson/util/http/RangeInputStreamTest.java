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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RangeInputStreamTest extends TestCase {
	private Log logger;

	/**
	 * Create the test case
	 * 
	 * @param testName
	 *            name of the test case
	 */
	public RangeInputStreamTest(String testName) {
		super(testName);

		logger = LogFactory.getLog(this.getClass());
		logger.debug("RangeInputStreamTest");
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(RangeInputStreamTest.class);
	}

	public void test_simpleUseCase() {
		// Stream of bytes: 0123456789abcdef
		// Range: bytes=0-9
		// RangeInputStream: 0123456789
		// Range: bytes=5-6
		// RangeInputStream: 56

		String data = "0123456789abcdef";
		ByteArrayInputStream dataStream;
		RangeInputStream in;
		Range range;
		int b;

		dataStream = new ByteArrayInputStream(data.getBytes());
		range = new Range();
		range.setStart(0);
		range.setEnd(9);
		in = new RangeInputStream(dataStream, range);

		try {
			b = in.read();
			assertEquals('0', b);
			b = in.read();
			assertEquals('1', b);
			b = in.read();
			assertEquals('2', b);
			b = in.read();
			assertEquals('3', b);
			b = in.read();
			assertEquals('4', b);
			b = in.read();
			assertEquals('5', b);
			b = in.read();
			assertEquals('6', b);
			b = in.read();
			assertEquals('7', b);
			b = in.read();
			assertEquals('8', b);
			b = in.read();
			assertEquals('9', b);
			b = in.read();
			assertEquals(-1, b);
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unexpected exception");
		}

		dataStream = new ByteArrayInputStream(data.getBytes());
		range = new Range();
		range.setStart(5);
		range.setEnd(6);
		in = new RangeInputStream(dataStream, range);

		try {
			b = in.read();
			assertEquals('5', b);
			b = in.read();
			assertEquals('6', b);
			b = in.read();
			assertEquals(-1, b);
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unexpected exception");
		}

		dataStream = new ByteArrayInputStream(data.getBytes());
		range = new Range();
		range.setStart(10);
		range.setEnd(10);
		in = new RangeInputStream(dataStream, range);

		try {
			b = in.read();
			assertEquals('a', b);
			b = in.read();
			assertEquals(-1, b);
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unexpected exception");
		}
	}

	public void test_bufferUseCase() {
		// Stream of bytes: 0123456789abcdef
		// Range: bytes=0-9
		// RangeInputStream: 0123456789
		// Range: bytes=5-6
		// RangeInputStream: 56

		String data = "0123456789abcdef";
		ByteArrayInputStream dataStream;
		RangeInputStream in;
		Range range;
		byte[] b;
		int count;

		dataStream = new ByteArrayInputStream(data.getBytes());
		range = new Range();
		range.setStart(0);
		range.setEnd(9);
		in = new RangeInputStream(dataStream, range);

		try {
			b = new byte[10];
			count = in.read(b, 0, b.length);

			assertEquals("Unexpected number of bytes read", 10, count);

			assertEquals('0', b[0]);
			assertEquals('1', b[1]);
			assertEquals('2', b[2]);
			assertEquals('3', b[3]);
			assertEquals('4', b[4]);
			assertEquals('5', b[5]);
			assertEquals('6', b[6]);
			assertEquals('7', b[7]);
			assertEquals('8', b[8]);
			assertEquals('9', b[9]);

			count = in.read(b, 0, b.length);
			assertEquals("Expected end", -1, count);
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unexpected exception");
		}

		dataStream = new ByteArrayInputStream(data.getBytes());
		range = new Range();
		range.setStart(5);
		range.setEnd(6);
		in = new RangeInputStream(dataStream, range);

		try {
			b = new byte[10];
			count = in.read(b, 0, b.length);

			assertEquals("Unexpected number of bytes read", 2, count);

			assertEquals('5', b[0]);
			assertEquals('6', b[1]);

			count = in.read(b, 0, b.length);
			assertEquals("Expected end", -1, count);
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unexpected exception");
		}

		dataStream = new ByteArrayInputStream(data.getBytes());
		range = new Range();
		range.setStart(10);
		range.setEnd(10);
		in = new RangeInputStream(dataStream, range);

		try {
			b = new byte[10];
			count = in.read(b, 0, b.length);

			assertEquals("Unexpected number of bytes read", 1, count);

			assertEquals('a', b[0]);

			count = in.read(b, 0, b.length);
			assertEquals("Expected end", -1, count);
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unexpected exception");
		}
	}

	public void test_multipleRanges() {
		// Stream of bytes: 0123456789abcdef
		// Range: bytes=0-9
		// RangeInputStream: 0123456789
		// Range: bytes=5-6
		// RangeInputStream: 56

		String data = "0123456789abcdef";
		ByteArrayInputStream dataStream;
		RangeInputStream in;
		Range range;
		byte[] b;
		int count;

		try {
			dataStream = new ByteArrayInputStream(data.getBytes());

			range = new Range(0, 5);
			in = new RangeInputStream(dataStream, range);

			b = new byte[10];
			count = in.read(b, 0, b.length);

			assertEquals("Unexpected number of bytes read", 6, count);

			assertEquals('0', b[0]);
			assertEquals('1', b[1]);
			assertEquals('2', b[2]);
			assertEquals('3', b[3]);
			assertEquals('4', b[4]);
			assertEquals('5', b[5]);

			range = new Range(6, 6);
			in.setRange(range);
			count = in.read(b, 0, b.length);

			assertEquals("Unexpected number of bytes read", 1, count);

			assertEquals('6', b[0]);

			range = new Range(10, 15);
			in.setRange(range);
			count = in.read(b, 0, b.length);

			assertEquals("Unexpected number of bytes read", 6, count);

			assertEquals('a', b[0]);
			assertEquals('b', b[1]);
			assertEquals('c', b[2]);
			assertEquals('d', b[3]);
			assertEquals('e', b[4]);
			assertEquals('f', b[5]);

			count = in.read(b, 0, b.length);
			assertEquals("Expected end", -1, count);
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unexpected exception");
		}
	}

	public void test_illegalRange() {
		// Stream of bytes: 0123456789abcdef
		// Range: bytes=0-9
		// RangeInputStream: 0123456789
		// Range: bytes=5-6
		// RangeInputStream: 56

		String data = "0123456789abcdef";
		ByteArrayInputStream dataStream;
		RangeInputStream in;
		Range range;
		byte[] b;
		int count;

		try {
			dataStream = new ByteArrayInputStream(data.getBytes());

			range = new Range(0, 5);
			in = new RangeInputStream(dataStream, range);

			b = new byte[10];
			count = in.read(b, 0, b.length);

			assertEquals("Unexpected number of bytes read", 6, count);

			assertEquals('0', b[0]);
			assertEquals('1', b[1]);
			assertEquals('2', b[2]);
			assertEquals('3', b[3]);
			assertEquals('4', b[4]);
			assertEquals('5', b[5]);

			try {
				in.setRange(range);
				fail("Should throw and exception");
			} catch (IllegalArgumentException e) {
				// expected
			}
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unexpected exception");
		}
	}

	/**
	 * Simple test of the <code>FileInputStream</code> <code>skip()</code>
	 * method. I was trying to verify what return value the skip() provides when
	 * you skip past the actual content size.
	 */
	public void xtest_skip() {
		long count;
		FileInputStream in;
		File file = new File("C:/temp/foo.txt");

		try {
			in = new FileInputStream(file);

			count = in.skip(10);
			System.out.println("Count of skipping '10' first: " + count);

			count = in.skip(10);
			System.out.println("Count of skipping '10' second: " + count);

			count = in.skip(10);
			System.out.println("Count of skipping '10' third: " + count);

			count = in.skip(10);
			System.out.println("Count of skipping '10' fourth: " + count);

			count = in.skip(10);
			System.out.println("Count of skipping '10' fifth: " + count);

			System.out.println("Read: " + in.read());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
