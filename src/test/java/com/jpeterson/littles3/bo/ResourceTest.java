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

import java.security.AccessControlException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class ResourceTest extends TestCase {
	private Log logger;

	/**
	 * Create the test case
	 * 
	 * @param testName
	 *            name of the test case
	 */
	public ResourceTest(String testName) {
		super(testName);

		logger = LogFactory.getLog(this.getClass());
		logger.debug("ResourceTest");
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(ResourceTest.class);
	}

	/**
	 * Test acp property access methods.
	 */
	public void test_acp() {
		Resource resource = new MyResource();
		Acp acp = new Acp();

		assertNull("Unexpected value", resource.getAcp());
		resource.setAcp(acp);
		assertEquals("Unexpected value", acp, resource.getAcp());
	}

	/**
	 * Test the <code>canRead()</code> method.
	 */
	public void test_canRead() {
		Resource resource;
		Acp acp;
		Grantee id, foo, bar, baz, anonymous;

		resource = new MyResource();

		acp = new Acp();
		resource.setAcp(acp);

		id = new CanonicalUser("id");
		foo = new CanonicalUser("foo");
		bar = new CanonicalUser("bar");
		baz = new CanonicalUser("baz");
		anonymous = AnonymousUser.getInstance();

		acp.grant(id, ResourcePermission.ACTION_FULL_CONTROL);
		acp.grant(foo, ResourcePermission.ACTION_WRITE);
		acp.grant(foo, ResourcePermission.ACTION_READ_ACP);
		acp.grant(bar, ResourcePermission.ACTION_WRITE);
		acp.grant(bar, ResourcePermission.ACTION_READ_ACP);
		acp.grant(AllUsersGroup.getInstance(), ResourcePermission.ACTION_READ);

		resource.canRead(id);
		resource.canRead(foo);
		resource.canRead(bar);
		resource.canRead(baz);
		resource.canRead(anonymous);
	}

	/**
	 * Test the <code>canRead()</code> method with no Access Control Policy.
	 */
	public void test_canReadNoAcp() {
		Resource resource;
		Grantee id;

		resource = new MyResource();

		id = new CanonicalUser("id");

		try {
			resource.canRead(id);
			fail("Should have thrown an exception");
		} catch (AccessControlException e) {
			// expected
		}
	}

	/**
	 * Test the <code>canWrite()</code> method.
	 */
	public void test_canWrite() {
		Resource resource;
		Acp acp;
		Grantee id, foo, bar, baz, anonymous;

		resource = new MyResource();

		acp = new Acp();
		resource.setAcp(acp);

		id = new CanonicalUser("id");
		foo = new CanonicalUser("foo");
		bar = new CanonicalUser("bar");
		baz = new CanonicalUser("baz");
		anonymous = AnonymousUser.getInstance();

		acp.grant(id, ResourcePermission.ACTION_FULL_CONTROL);
		acp.grant(foo, ResourcePermission.ACTION_WRITE);
		acp.grant(foo, ResourcePermission.ACTION_READ_ACP);
		acp.grant(bar, ResourcePermission.ACTION_WRITE);
		acp.grant(bar, ResourcePermission.ACTION_READ_ACP);
		acp.grant(AllUsersGroup.getInstance(), ResourcePermission.ACTION_READ);

		resource.canWrite(id);
		resource.canWrite(foo);
		resource.canWrite(bar);
		try {
			resource.canWrite(baz);
			fail("Should have thrown an exception");
		} catch (AccessControlException e) {
			// expected
		}
		try {
			resource.canWrite(anonymous);
			fail("Should have thrown an exception");
		} catch (AccessControlException e) {
			// expected
		}
	}

	/**
	 * Test the <code>canWrite()</code> method with no Access Control Policy.
	 */
	public void test_canWriteNoAcp() {
		Resource resource;
		Grantee id;

		resource = new MyResource();

		id = new CanonicalUser("id");

		try {
			resource.canWrite(id);
			fail("Should have thrown an exception");
		} catch (AccessControlException e) {
			// expected
		}
	}

	/**
	 * Class for testing abstract <code>Resource</code> class.
	 * 
	 * @author Jesse Peterson
	 */
	private class MyResource extends Resource {
		public MyResource() {
			super();
		}
	}
}
