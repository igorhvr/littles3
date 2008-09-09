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

import java.util.Date;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class BucketTest extends TestCase {
	private Log logger;

	/**
	 * Create the test case
	 * 
	 * @param testName
	 *            name of the test case
	 */
	public BucketTest(String testName) {
		super(testName);

		logger = LogFactory.getLog(this.getClass());
		logger.debug("BucketTest");
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(BucketTest.class);
	}

	/**
	 * Test the name property.
	 */
	public void test_name() {
		Bucket bucket = new Bucket();

		assertNull("Unexpected value", bucket.getName());
		bucket.setName("test");
		assertEquals("Unexpected value", "test", bucket.getName());
	}

	/**
	 * Test the created property.
	 */
	public void test_created() {
		Bucket bucket = new Bucket();
		Date now = new Date();

		assertNull("Unexpected value", bucket.getCreated());
		bucket.setCreated(now);
		assertEquals("Unexpected value", now, bucket.getCreated());
	}
}
