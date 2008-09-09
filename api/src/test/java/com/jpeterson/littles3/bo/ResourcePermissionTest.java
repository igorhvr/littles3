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

import java.io.FilePermission;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class ResourcePermissionTest extends TestCase {
	private Log logger;

	/**
	 * Create the test case
	 * 
	 * @param testName
	 *            name of the test case
	 */
	public ResourcePermissionTest(String testName) {
		super(testName);

		logger = LogFactory.getLog(this.getClass());
		logger.debug("ResourcePermissionTest");
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(ResourcePermissionTest.class);
	}

	/**
	 * Test accessing the grantee property.
	 */
	public void test_grantee() {
		ResourcePermission permission;
		Grantee grantee = new CanonicalUser("id");

		permission = new ResourcePermission(grantee,
				ResourcePermission.ACTION_FULL_CONTROL);

		assertEquals("Unexpected value", grantee, permission.getGrantee());
	}

	/**
	 * Test accessing the name property.
	 */
	public void test_name() {
		ResourcePermission permission;
		Grantee grantee = new CanonicalUser("id");

		permission = new ResourcePermission(grantee,
				ResourcePermission.ACTION_FULL_CONTROL);

		assertEquals("Unexpected value", "id id", permission.getName());
	}

	/**
	 * Test accessing the action property.
	 */
	public void test_actions() {
		ResourcePermission permission;
		Grantee grantee = new CanonicalUser("id");

		permission = new ResourcePermission(grantee,
				ResourcePermission.ACTION_FULL_CONTROL);
		assertEquals("Unexpected value", "FULL_CONTROL",
				permission.getActions());
		permission = new ResourcePermission(grantee,
				ResourcePermission.ACTION_READ);
		assertEquals("Unexpected value", "READ", permission.getActions());
		permission = new ResourcePermission(grantee,
				ResourcePermission.ACTION_WRITE);
		assertEquals("Unexpected value", "WRITE", permission.getActions());
		permission = new ResourcePermission(grantee,
				ResourcePermission.ACTION_READ_ACP);
		assertEquals("Unexpected value", "READ_ACP", permission.getActions());
		permission = new ResourcePermission(grantee,
				ResourcePermission.ACTION_WRITE_ACP);
		assertEquals("Unexpected value", "WRITE_ACP", permission.getActions());
		permission = new ResourcePermission(grantee, "  reaD , Write ");
		assertEquals("Unexpected value", "READ,WRITE", permission.getActions());
	}

	/**
	 * Test the <code>equals()</code> method.
	 */
	public void test_equals() {
		ResourcePermission permission, another;
		CanonicalUser grantee;

		grantee = new CanonicalUser("id");
		permission = new ResourcePermission(grantee,
				ResourcePermission.ACTION_FULL_CONTROL);

		assertTrue("Should be equal", permission.equals(permission));
		assertFalse("Should not be equal", permission
				.equals("ResourcePermission"));

		another = new ResourcePermission(grantee,
				ResourcePermission.ACTION_READ);
		assertFalse("Should not be equal", permission.equals(another));

		another = new ResourcePermission(new CanonicalUser("foo"),
				ResourcePermission.ACTION_FULL_CONTROL);
		assertFalse("Should not be equal", permission.equals(another));

		another = new ResourcePermission(new CanonicalUser(grantee.getId()),
				ResourcePermission.ACTION_FULL_CONTROL);
		assertTrue("Should be equal", permission.equals(another));
	}

	/**
	 * Test the <code>hashCode()</code> method.
	 */
	public void test_hashCode() {
		ResourcePermission permission, another;
		CanonicalUser grantee;

		grantee = new CanonicalUser("id");
		permission = new ResourcePermission(grantee,
				ResourcePermission.ACTION_FULL_CONTROL);

		assertEquals("Should be equal", permission.hashCode(), permission
				.hashCode());

		another = new ResourcePermission(new CanonicalUser(grantee.getId()),
				ResourcePermission.ACTION_FULL_CONTROL);
		assertEquals("Should be equal", permission.hashCode(), another
				.hashCode());
	}

	/**
	 * Test the <code>implies()</code> method.
	 */
	public void test_implies() {
		ResourcePermission permission, another;
		CanonicalUser grantee;

		grantee = new CanonicalUser("id");
		permission = new ResourcePermission(grantee,
				ResourcePermission.ACTION_FULL_CONTROL);

		assertTrue("Should imply", permission.implies(permission));
		assertFalse("Should not imply", permission.implies(null));
		assertFalse("Should not imply", permission.implies(new FilePermission(
				"/etc", "read")));

		another = new ResourcePermission(grantee,
				ResourcePermission.ACTION_READ);
		assertTrue("Should imply", permission.implies(another));
		another = new ResourcePermission(grantee,
				ResourcePermission.ACTION_WRITE);
		assertTrue("Should imply", permission.implies(another));
		another = new ResourcePermission(grantee,
				ResourcePermission.ACTION_READ_ACP);
		assertTrue("Should imply", permission.implies(another));
		another = new ResourcePermission(grantee,
				ResourcePermission.ACTION_WRITE_ACP);
		assertTrue("Should imply", permission.implies(another));
		another = new ResourcePermission(grantee, "read, write");
		assertTrue("Should imply", permission.implies(another));
		another = new ResourcePermission(grantee, "read, read_acp");
		assertTrue("Should imply", permission.implies(another));

		permission = new ResourcePermission(grantee,
				ResourcePermission.ACTION_READ);
		another = new ResourcePermission(grantee,
				ResourcePermission.ACTION_WRITE);
		assertFalse("Should not imply", permission.implies(another));

		permission = new ResourcePermission(grantee,
				ResourcePermission.ACTION_FULL_CONTROL);
		another = new ResourcePermission(new CanonicalUser("foo"),
				ResourcePermission.ACTION_FULL_CONTROL);
		assertFalse("Should not imply", permission.implies(another));

		permission = new ResourcePermission(AuthenticatedUsersGroup
				.getInstance(), "read, read_acp");
		another = new ResourcePermission(grantee,
				ResourcePermission.ACTION_READ);
		assertTrue("Should imply", permission.implies(another));
		another = new ResourcePermission(grantee,
				ResourcePermission.ACTION_WRITE);
		assertFalse("Should not imply", permission.implies(another));

		permission = new ResourcePermission(AuthenticatedUsersGroup
				.getInstance(), "read, read_acp");
		another = new ResourcePermission(new CanonicalUser(
				CanonicalUser.ID_ANONYMOUS), ResourcePermission.ACTION_READ);
		assertFalse("Should not imply", permission.implies(another));

		permission = new ResourcePermission(AllUsersGroup.getInstance(), "read");
		another = new ResourcePermission(grantee,
				ResourcePermission.ACTION_READ);
		assertTrue("Should imply", permission.implies(another));
		another = new ResourcePermission(grantee,
				ResourcePermission.ACTION_WRITE);
		assertFalse("Should not imply", permission.implies(another));

		permission = new ResourcePermission(AllUsersGroup.getInstance(), "read");
		another = new ResourcePermission(new CanonicalUser(
				CanonicalUser.ID_ANONYMOUS), ResourcePermission.ACTION_READ);
		assertTrue("Should imply", permission.implies(another));
		another = new ResourcePermission(new CanonicalUser(
				CanonicalUser.ID_ANONYMOUS), ResourcePermission.ACTION_WRITE);
		assertFalse("Should not imply", permission.implies(another));
	}

	/**
	 * Test constructor with an invalid action.
	 */
	public void test_invalidAction() {
		try {
			new ResourcePermission(new CanonicalUser("foo"), "delete");
			fail("Should have thrown an exception");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}
}
