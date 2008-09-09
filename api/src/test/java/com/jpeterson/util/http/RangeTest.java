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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class RangeTest extends TestCase {
	private Log logger;

	/**
	 * Create the test case
	 * 
	 * @param testName
	 *            name of the test case
	 */
	public RangeTest(String testName) {
		super(testName);

		logger = LogFactory.getLog(this.getClass());
		logger.debug("RangeTest");
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(RangeTest.class);
	}

	/**
	 * Test the <code>setStart()</code> and <code>getStart()</code> methods.
	 */
	public void test_start() {
		Range range = new Range();

		range.setStart(1);

		assertEquals("Unexpected start", 1, range.getStart());
	}

	/**
	 * Test the <code>setEnd()</code> and <code>getEnd()</code> methods.
	 */
	public void test_end() {
		Range range = new Range();

		range.setEnd(4);

		assertEquals("Unexpected start", 4, range.getEnd());
	}

	/**
	 * Test the constructor that takes a <code>start</code> and
	 * <code>end</code> value.
	 */
	public void test_constructorWithStartAndEnd() {
		Range range;

		range = new Range(3, 9);
		assertEquals("Unexpected start", 3, range.getStart());
		assertEquals("Unexpected end", 9, range.getEnd());
	}

	/**
	 * Test the <code>resolve()</code> method.
	 */
	public void test_resolve() {
		Range range;

		// 'open-ended' range
		range = new Range(5);

		assertEquals("Unexpected start", 5, range.getStart());
		assertEquals("Unexpected end", Range.OPEN_ENDED, range.getEnd());

		range.resolve(10);

		assertEquals("Unexpected start", 5, range.getStart());
		assertEquals("Unexpected end", 9, range.getEnd());

		// suffix range
		range = new Range(-10);

		assertEquals("Unexpected start", -10, range.getStart());
		assertEquals("Unexpected end", Range.OPEN_ENDED, range.getEnd());

		range.resolve(100);

		assertEquals("Unexpected start", 90, range.getStart());
		assertEquals("Unexpected end", 99, range.getEnd());

		// already resolved
		range = new Range(2, 4);

		assertEquals("Unexpected start", 2, range.getStart());
		assertEquals("Unexpected end", 4, range.getEnd());

		range.resolve(100);

		assertEquals("Unexpected start", 2, range.getStart());
		assertEquals("Unexpected end", 4, range.getEnd());
	}

	/**
	 * Test the resolve that results in the range being "squeezed".
	 */
	public void test_resolveSqueeze() {
		Range range;

		range = new Range(5, 20);
		range.resolve(10);

		assertEquals("Should have been squeezed", new Range(5, 9), range);
	}

	/**
	 * Test resolving a range that is outside of the absolute length. Should
	 * throw an exception.
	 */
	public void test_resolveIllegalState() {
		Range range;

		range = new Range(20, 30);

		try {
			range.resolve(10);
			fail("Should have thrown an exception");
		} catch (IllegalStateException e) {
			// expected
		}
	}

	/**
	 * Test the <code>getLength()</code> method.
	 */
	public void test_getLength() {
		Range range;

		// 'open-ended' range
		range = new Range(5);

		assertEquals("Unexpected length", -1, range.getLength());

		// suffix range
		range = new Range(-10);

		assertEquals("Unexpected length", -1, range.getLength());

		// already resolved
		range = new Range(2, 4);

		assertEquals("Unexpected length", 3, range.getLength());
	}

	/**
	 * Test the <code>inclues()</code> method.
	 */
	public void test_includes() {
		Range range;

		range = new Range();
		range.setStart(2);
		range.setEnd(4);
		assertFalse("Unexpected value", range.includes(0));
		assertFalse("Unexpected value", range.includes(1));
		assertTrue("Unexpected value", range.includes(2));
		assertTrue("Unexpected value", range.includes(3));
		assertTrue("Unexpected value", range.includes(4));
		assertFalse("Unexpected value", range.includes(5));
		assertFalse("Unexpected value", range.includes(6));

		range = new Range();
		range.setStart(3);
		range.setEnd(3);
		assertFalse("Unexpected value", range.includes(0));
		assertFalse("Unexpected value", range.includes(1));
		assertFalse("Unexpected value", range.includes(2));
		assertTrue("Unexpected value", range.includes(3));
		assertFalse("Unexpected value", range.includes(4));
		assertFalse("Unexpected value", range.includes(5));
		assertFalse("Unexpected value", range.includes(6));
	}

	/**
	 * Test the <code>combinable()</code> method.
	 */
	public void test_combinable() {
		Range r1, r2, r3, r4, r5, r6;

		r1 = new Range();
		r1.setStart(5);
		r1.setEnd(10);

		r2 = new Range();
		r2.setStart(4);
		r2.setEnd(8);

		r3 = new Range();
		r3.setStart(10);
		r3.setEnd(12);

		r4 = new Range();
		r4.setStart(1);
		r4.setEnd(3);

		assertTrue(r1.combinable(r2));
		assertTrue(r1.combinable(r3));
		assertFalse(r1.combinable(r4));

		r5 = new Range(0, 0);
		r6 = new Range(4, 8);

		assertTrue(r4.combinable(r5));
		assertTrue(r4.combinable(r6));
	}

	/**
	 * Test the <code>equals()</code> method.
	 */
	public void test_equals() {
		Range r1, r2, r3, r4, r5;

		r1 = new Range();
		r1.setStart(1);
		r1.setEnd(10);

		r2 = new Range();
		r2.setStart(1);
		r2.setEnd(10);

		r3 = new Range();
		r3.setStart(2);
		r3.setEnd(10);

		r4 = new Range();
		r4.setStart(1);
		r4.setEnd(9);

		r5 = new Range();
		r5.setStart(0);
		r5.setEnd(11);

		assertTrue(r1.equals(r1));
		assertTrue(r1.equals(r2));
		assertTrue(r2.equals(r1));
		assertFalse(r1.equals(null));
		assertFalse(r1.equals("foo"));
		assertFalse(r1.equals(r3));
		assertFalse(r1.equals(r4));
		assertFalse(r1.equals(r5));
	}

	/**
	 * Test the <code>hashCode</code> method.
	 */
	public void test_hashCode() {
		Range r1, r2;

		r1 = new Range();
		r1.setStart(1);
		r1.setEnd(10);

		r2 = new Range();
		r2.setStart(1);
		r2.setEnd(10);

		assertEquals(r1.hashCode(), r1.hashCode());
		assertEquals(r1.hashCode(), r2.hashCode());
	}
}