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

import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.Enumeration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class GroupBaseTest extends TestCase {
	private Log logger;

	/**
	 * Create the test case
	 * 
	 * @param testName
	 *            name of the test case
	 */
	public GroupBaseTest(String testName) {
		super(testName);

		logger = LogFactory.getLog(this.getClass());
		logger.debug("GroupBaseTest");
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(GroupBaseTest.class);
	}

	/**
	 * Test the constructor.
	 */
	public void test_constructor() {
		GroupBase group;
		URI uri;
		String uriString = "http://www.foo.com";

		try {
			uri = new URI(uriString);
		} catch (URISyntaxException e) {
			e.printStackTrace();
			fail("Unexpected exception");
			return;
		}

		group = new MyGroupBase(uri);

		assertEquals("Unexpected value", uri, group.getUri());
	}

	/**
	 * Test the <code>getName()</code> method.
	 */
	public void test_getName() {
		GroupBase group;
		URI uri;
		String uriString = "http://www.foo.com";

		try {
			uri = new URI(uriString);
		} catch (URISyntaxException e) {
			e.printStackTrace();
			fail("Unexpected exception");
			return;
		}

		group = new MyGroupBase(uri);

		assertEquals("Unexpected value", uriString, group.getName());
	}

	/**
	 * Test the <code>toString()</code> method.
	 */
	public void test_getString() {
		GroupBase group;
		URI uri;
		String uriString = "http://www.foo.com";

		try {
			uri = new URI(uriString);
		} catch (URISyntaxException e) {
			e.printStackTrace();
			fail("Unexpected exception");
			return;
		}

		group = new MyGroupBase(uri);

		assertEquals("Unexpected value", "uri " + uriString, group.toString());
	}

	/**
	 * Class for unit testing an abstract class.
	 * 
	 * @author Jesse Peterson
	 */
	private class MyGroupBase extends GroupBase {
		public MyGroupBase(URI uri) {
			super(uri);
		}

		public boolean addMember(Principal user) {
			// TODO Auto-generated method stub
			return false;
		}

		public boolean isMember(Principal member) {
			// TODO Auto-generated method stub
			return false;
		}

		public Enumeration<? extends Principal> members() {
			// TODO Auto-generated method stub
			return null;
		}

		public boolean removeMember(Principal user) {
			// TODO Auto-generated method stub
			return false;
		}

	}
}
