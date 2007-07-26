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

package com.jpeterson.littles3.bo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AnonymousUserTest extends TestCase {
	private Log logger;

	/**
	 * Create the test case
	 * 
	 * @param testName
	 *            name of the test case
	 */
	public AnonymousUserTest(String testName) {
		super(testName);

		logger = LogFactory.getLog(this.getClass());
		logger.debug("AnonymousUserTest");
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(AnonymousUserTest.class);
	}

	/**
	 * Test the <code>getName()</code> method.
	 */
	public void test_getName() {
		AnonymousUser user = AnonymousUser.getInstance();

		assertEquals("Unexpected value", "anonymous", user.getName());
	}

	/**
	 * Test the <code>equals()</code> method.
	 */
	public void test_equals() {
		AnonymousUser user = AnonymousUser.getInstance();

		assertTrue("Should be equal", user.equals(AnonymousUser.getInstance()));
		assertFalse("Should not be equal", user.equals(null));
		assertFalse("Should not be equal", user.equals(new CanonicalUser("id")));
	}

	/**
	 * Test the <code>toString()</code> method.
	 */
	public void test_toString() {
		AnonymousUser user = AnonymousUser.getInstance();

		assertEquals("Unexpected value", "anonymous", user.toString());
	}
}
