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

public class RangeFactoryTest extends TestCase {
	private Log logger;

	/**
	 * Create the test case
	 * 
	 * @param testName
	 *            name of the test case
	 */
	public RangeFactoryTest(String testName) {
		super(testName);

		logger = LogFactory.getLog(this.getClass());
		logger.debug("RangeFactoryTest");
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(RangeFactoryTest.class);
	}

	public void test_basic() {
		String value;
		RangeSet rangeSet;
		Range[] ranges;
		Range range;

		value = "bytes=0-499";
		rangeSet = RangeFactory.processRangeHeader(value);

		ranges = (Range[]) rangeSet.toArray(new Range[0]);

		assertEquals("Unexpected number of ranges", 1, ranges.length);
		assertEquals("Unexpected range", new Range(0, 499), ranges[0]);

		value = "bytes=500-999";
		rangeSet = RangeFactory.processRangeHeader(value);

		ranges = (Range[]) rangeSet.toArray(new Range[0]);

		assertEquals("Unexpected number of ranges", 1, ranges.length);
		assertEquals("Unexpected range", new Range(500, 999), ranges[0]);

		value = "bytes=-500";
		try {
			rangeSet = RangeFactory.processRangeHeader(value);
			fail("Expected exception");
		} catch (UnsupportedOperationException e) {
			// expected
		}

		value = "bytes=9500-";
		rangeSet = RangeFactory.processRangeHeader(value);

		ranges = (Range[]) rangeSet.toArray(new Range[0]);

		assertEquals("Unexpected number of ranges", 1, ranges.length);
		range = new Range();
		range.setStart(9500);
		assertEquals("Unexpected range", range, ranges[0]);

		value = "bytes=0-0,-1";
		try {
			rangeSet = RangeFactory.processRangeHeader(value);
			fail("Expected exception");
		} catch (UnsupportedOperationException e) {
			// expected
		}

		value = "bytes=500-600,601-999";
		rangeSet = RangeFactory.processRangeHeader(value);

		ranges = (Range[]) rangeSet.toArray(new Range[0]);

		assertEquals("Unexpected number of ranges", 1, ranges.length);
		assertEquals("Unexpected range", new Range(500, 999), ranges[0]);

		value = "bytes=500-700,601-999";
		rangeSet = RangeFactory.processRangeHeader(value);

		ranges = (Range[]) rangeSet.toArray(new Range[0]);

		assertEquals("Unexpected number of ranges", 1, ranges.length);
		assertEquals("Unexpected range", new Range(500, 999), ranges[0]);
	}
}
