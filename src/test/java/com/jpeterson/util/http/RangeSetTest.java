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

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class RangeSetTest extends TestCase {
	private Log logger;

	/**
	 * Create the test case
	 * 
	 * @param testName
	 *            name of the test case
	 */
	public RangeSetTest(String testName) {
		super(testName);

		logger = LogFactory.getLog(this.getClass());
		logger.debug("RangeSetTest");
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(RangeSetTest.class);
	}

	/**
	 * Basic test with three ranges that do not intersect.
	 */
	public void test_one() {
		RangeSet rangeSet = new RangeSet();
		Range r, r1, r2, r3;

		r1 = new Range(1, 2);
		r2 = new Range(4, 6);
		r3 = new Range(8, 9);

		rangeSet.add(r1);
		rangeSet.add(r2);
		rangeSet.add(r3);

		Iterator iter = rangeSet.iterator();

		assertTrue("Expected another range", iter.hasNext());
		r = (Range) iter.next();
		assertEquals("Unexpected range", r1, r);
		assertTrue("Expected another range", iter.hasNext());
		r = (Range) iter.next();
		assertEquals("Unexpected range", r2, r);
		assertTrue("Expected another range", iter.hasNext());
		r = (Range) iter.next();
		assertEquals("Unexpected range", r3, r);
		assertFalse("Unexpected range", iter.hasNext());
	}

	/**
	 * Basic test with three ranges that do not intersect and verify that they
	 * are in the correct order.
	 */
	public void test_two() {
		RangeSet rangeSet = new RangeSet();
		Range r, r1, r2, r3;

		r1 = new Range(1, 2);
		r2 = new Range(4, 6);
		r3 = new Range(8, 9);

		rangeSet.add(r2);
		rangeSet.add(r3);
		rangeSet.add(r1);

		Iterator iter = rangeSet.iterator();

		assertTrue("Expected another range", iter.hasNext());
		r = (Range) iter.next();
		assertEquals("Unexpected range", r1, r);
		assertTrue("Expected another range", iter.hasNext());
		r = (Range) iter.next();
		assertEquals("Unexpected range", r2, r);
		assertTrue("Expected another range", iter.hasNext());
		r = (Range) iter.next();
		assertEquals("Unexpected range", r3, r);
		assertFalse("Unexpected range", iter.hasNext());
	}

	/**
	 * Basic test with three ranges with two that intersect
	 */
	public void test_three() {
		RangeSet rangeSet = new RangeSet();
		Range r, r1, r2, r3;

		r1 = new Range(1, 4);
		r2 = new Range(3, 7);
		r3 = new Range(9, 9);

		rangeSet.add(r2);
		rangeSet.add(r3);
		rangeSet.add(r1);

		Iterator iter = rangeSet.iterator();

		assertTrue("Expected another range", iter.hasNext());
		r = (Range) iter.next();
		assertEquals("Unexpected range", new Range(1, 7), r);
		assertTrue("Expected another range", iter.hasNext());
		r = (Range) iter.next();
		assertEquals("Unexpected range", r3, r);
		assertFalse("Unexpected range", iter.hasNext());
	}

	/**
	 * Basic test with three ranges that intersect
	 */
	public void test_four() {
		RangeSet rangeSet = new RangeSet();
		Range r, r1, r2, r3;

		r1 = new Range(1, 4);
		r2 = new Range(3, 7);
		r3 = new Range(6, 9);

		rangeSet.add(r2);
		rangeSet.add(r3);
		rangeSet.add(r1);

		Iterator iter = rangeSet.iterator();

		assertTrue("Expected another range", iter.hasNext());
		r = (Range) iter.next();
		assertEquals("Unexpected range", new Range(1, 9), r);
		assertFalse("Unexpected range", iter.hasNext());
	}

	/**
	 * Basic test to resolve.
	 */
	public void test_five() {
		RangeSet rangeSet = new RangeSet();
		Range r, r1, r2, r3;

		r1 = new Range(1, 2);
		r2 = new Range(4, 6);
		r3 = new Range(-2);

		rangeSet.add(r1);
		rangeSet.add(r2);
		rangeSet.add(r3);

		rangeSet.resolve(12);

		Iterator iter = rangeSet.iterator();

		assertTrue("Expected another range", iter.hasNext());
		r = (Range) iter.next();
		assertEquals("Unexpected range", r1, r);
		assertTrue("Expected another range", iter.hasNext());
		r = (Range) iter.next();
		assertEquals("Unexpected range", r2, r);
		assertTrue("Expected another range", iter.hasNext());
		r = (Range) iter.next();
		assertEquals("Unexpected range", new Range(10, 11), r);
		assertFalse("Unexpected range", iter.hasNext());
	}

	/**
	 * Basic test to resolve.
	 */
	public void test_six() {
		RangeSet rangeSet = new RangeSet();
		Range r, r1, r2, r3;

		r1 = new Range(1, 2);
		r2 = new Range(4, 6);
		r3 = new Range(5);

		rangeSet.add(r1);
		rangeSet.add(r3);
		rangeSet.add(r2);

		rangeSet.resolve(12);

		Iterator iter = rangeSet.iterator();

		assertTrue("Expected another range", iter.hasNext());
		r = (Range) iter.next();
		assertEquals("Unexpected range", r1, r);
		assertTrue("Expected another range", iter.hasNext());
		r = (Range) iter.next();
		assertEquals("Unexpected range", new Range(4, 11), r);
		assertFalse("Unexpected range", iter.hasNext());
	}

	/**
	 * Basic test to resolve where the set is larger than the absolute length,
	 * so it is squeezed.
	 */
	public void test_seven() {
		RangeSet rangeSet = new RangeSet();
		Range r, r1, r2, r3;

		r1 = new Range(1, 2);
		r2 = new Range(4, 6);
		r3 = new Range(5);

		rangeSet.add(r1);
		rangeSet.add(r3);
		rangeSet.add(r2);

		rangeSet.resolve(12);

		Iterator iter = rangeSet.iterator();

		assertTrue("Expected another range", iter.hasNext());
		r = (Range) iter.next();
		assertEquals("Unexpected range", r1, r);
		assertTrue("Expected another range", iter.hasNext());
		r = (Range) iter.next();
		assertEquals("Unexpected range", new Range(4, 11), r);
		assertFalse("Unexpected range", iter.hasNext());
	}

	/**
	 * Test the inclusion interface.
	 */
	public void test_inclusion() {
		RangeSet rangeSet = new RangeSet();
		Range r1, r2, r3;

		r1 = new Range(1, 2);
		r2 = new Range(3, 6);
		r3 = new Range(8, 9);

		rangeSet.add(r1);
		rangeSet.add(r2);
		rangeSet.add(r3);

		assertFalse(rangeSet.includes(0));
		assertTrue(rangeSet.includes(2));
		assertTrue(rangeSet.includes(5));
		assertFalse(rangeSet.includes(7));
		assertTrue(rangeSet.includes(9));
		assertFalse(rangeSet.includes(12));
	}
}
