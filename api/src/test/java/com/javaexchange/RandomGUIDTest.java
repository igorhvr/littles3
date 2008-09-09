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

package com.javaexchange;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RandomGUIDTest extends TestCase {
	private Log logger;

	/**
	 * Create the test case
	 * 
	 * @param testName
	 *            name of the test case
	 */
	public RandomGUIDTest(String testName) {
		super(testName);

		logger = LogFactory.getLog(this.getClass());
		logger.debug("RandomGUIDTest");
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(RandomGUIDTest.class);
	}

	/**
	 * Basic test. Just verifies that two GUIDs are unique.
	 */
	public void test_simple() {
		RandomGUID guid1, guid2;

		for (int i = 0; i < 100; i++) {
			guid1 = new RandomGUID();
			guid2 = new RandomGUID();
			assertFalse("Duplicate GUIDs", guid1.toString().equals(
					guid2.toString()));
		}
	}
}
