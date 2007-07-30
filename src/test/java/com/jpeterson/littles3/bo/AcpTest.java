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
import java.util.Enumeration;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AcpTest extends TestCase {
	private Log logger;

	/**
	 * Create the test case
	 * 
	 * @param testName
	 *            name of the test case
	 */
	public AcpTest(String testName) {
		super(testName);

		logger = LogFactory.getLog(this.getClass());
		logger.debug("AcpTest");
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(AcpTest.class);
	}

	/**
	 * Test the <code>grant()</code> method.
	 */
	public void test_grant() {
		Acp acp;
		Grantee id, foo, bar, baz, anonymous;

		acp = new Acp();

		id = new CanonicalUser("id");
		foo = new CanonicalUser("foo");
		bar = new CanonicalUser("bar");
		baz = new CanonicalUser("baz");
		anonymous = new CanonicalUser(CanonicalUser.ID_ANONYMOUS);

		acp.grant(id, ResourcePermission.ACTION_FULL_CONTROL);
		acp.grant(foo, ResourcePermission.ACTION_WRITE);
		acp.grant(foo, ResourcePermission.ACTION_READ_ACP);
		acp.grant(bar, ResourcePermission.ACTION_WRITE);
		acp.grant(bar, ResourcePermission.ACTION_READ_ACP);
		acp.grant(AllUsersGroup.getInstance(), ResourcePermission.ACTION_READ);

		acp.checkPermission(new ResourcePermission(id,
				ResourcePermission.ACTION_READ));
		acp.checkPermission(new ResourcePermission(id,
				ResourcePermission.ACTION_WRITE));
		acp.checkPermission(new ResourcePermission(id,
				ResourcePermission.ACTION_READ_ACP));
		acp.checkPermission(new ResourcePermission(id,
				ResourcePermission.ACTION_WRITE_ACP));

		acp.checkPermission(new ResourcePermission(foo,
				ResourcePermission.ACTION_READ));
		acp.checkPermission(new ResourcePermission(foo,
				ResourcePermission.ACTION_WRITE));
		acp.checkPermission(new ResourcePermission(foo,
				ResourcePermission.ACTION_READ_ACP));
		try {
			acp.checkPermission(new ResourcePermission(foo,
					ResourcePermission.ACTION_WRITE_ACP));
			fail("Should have thrown an exception");
		} catch (AccessControlException e) {
			// expected
		}

		acp.checkPermission(new ResourcePermission(baz,
				ResourcePermission.ACTION_READ));
		try {
			acp.checkPermission(new ResourcePermission(baz,
					ResourcePermission.ACTION_WRITE));
			fail("Should have thrown an exception");
		} catch (AccessControlException e) {
			// expected
		}
		try {
			acp.checkPermission(new ResourcePermission(baz,
					ResourcePermission.ACTION_READ_ACP));
			fail("Should have thrown an exception");
		} catch (AccessControlException e) {
			// expected
		}
		try {
			acp.checkPermission(new ResourcePermission(baz,
					ResourcePermission.ACTION_WRITE_ACP));
		} catch (AccessControlException e) {
			// expected
		}

		acp.checkPermission(new ResourcePermission(anonymous,
				ResourcePermission.ACTION_READ));
		try {
			acp.checkPermission(new ResourcePermission(anonymous,
					ResourcePermission.ACTION_WRITE));
			fail("Should have thrown an exception");
		} catch (AccessControlException e) {
			// expected
		}
		try {
			acp.checkPermission(new ResourcePermission(anonymous,
					ResourcePermission.ACTION_READ_ACP));
			fail("Should have thrown an exception");
		} catch (AccessControlException e) {
			// expected
		}
		try {
			acp.checkPermission(new ResourcePermission(anonymous,
					ResourcePermission.ACTION_WRITE_ACP));
		} catch (AccessControlException e) {
			// expected
		}
	}

	/**
	 * Test granting an illegal permission.
	 */
	public void test_grantIllegalPermission() {
		Acp acp = new Acp();

		try {
			acp.grant(new CanonicalUser("foo"), "delete");
			fail("Should have thrown an exception");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	/**
	 * Test the owner property access methods.
	 */
	public void test_owner() {
		Acp acp = new Acp();
		CanonicalUser owner = new CanonicalUser("foo");

		assertNull("Unexpected value", acp.getOwner());
		acp.setOwner(owner);
		assertEquals("Unexpected value", owner, acp.getOwner());
	}

	/**
	 * Test the ability to get the grants.
	 */
	public void test_grants() {
		Acp acp = new Acp();
		ResourcePermission grant;

		acp.grant(new CanonicalUser("foo"), "FULL_CONTROL");
		acp.grant(new CanonicalUser("bar"), "WRITE");
		acp.grant(AuthenticatedUsersGroup.getInstance(), "READ_ACP");

		Enumeration grants = acp.grants();

		assertTrue("Should be more grants", grants.hasMoreElements());
		grant = (ResourcePermission) grants.nextElement();
		grant.getGrantee();
		assertTrue("Should be more grants", grants.hasMoreElements());
		grants.nextElement();
		assertTrue("Should be more grants", grants.hasMoreElements());
		grants.nextElement();
		assertFalse("Should not be more grants", grants.hasMoreElements());
	}

	/**
	 * Test the size property.
	 */
	public void test_size() {
		Acp acp = new Acp();

		assertEquals("Unexpected size", 0, acp.size());
		acp.grant(new CanonicalUser("foo"), "FULL_CONTROL");
		assertEquals("Unexpected size", 1, acp.size());
		acp.grant(new CanonicalUser("bar"), "WRITE");
		assertEquals("Unexpected size", 2, acp.size());
		acp.grant(AuthenticatedUsersGroup.getInstance(), "READ_ACP");
		assertEquals("Unexpected size", 3, acp.size());
	}

	/**
	 * Test the limit on the number of grants.
	 */
	public void test_tooManyGrants() {
		Acp acp = new Acp();

		for (int i = 0; i < 100; i++) {
			acp.grant(AllUsersGroup.getInstance(), "read");
		}

		try {
			acp.grant(AuthenticatedUsersGroup.getInstance(), "write");
			fail("Should have thrown an exception");
		} catch (IndexOutOfBoundsException e) {
			// expected
		}
	}

	/**
	 * Test the <code>canRead()</code> method.
	 */
	public void test_canRead() {
		Acp acp;
		Grantee id, foo, bar, baz, anonymous;

		acp = new Acp();

		id = new CanonicalUser("id");
		foo = new CanonicalUser("foo");
		bar = new CanonicalUser("bar");
		baz = new CanonicalUser("baz");
		anonymous = new CanonicalUser(CanonicalUser.ID_ANONYMOUS);

		acp.grant(id, ResourcePermission.ACTION_FULL_CONTROL);
		acp.grant(foo, ResourcePermission.ACTION_WRITE);
		acp.grant(foo, ResourcePermission.ACTION_READ_ACP);
		acp.grant(bar, ResourcePermission.ACTION_WRITE);
		acp.grant(bar, ResourcePermission.ACTION_READ_ACP);
		acp.grant(AllUsersGroup.getInstance(), ResourcePermission.ACTION_READ);

		acp.canRead(id);
		acp.canRead(foo);
		acp.canRead(bar);
		try {
			acp.canRead(baz);
			fail("Should have thrown an exception");
		} catch (AccessControlException e) {
			// expected
		}
		try {
			acp.canRead(anonymous);
			fail("Should have thrown an exception");
		} catch (AccessControlException e) {
			// expected
		}
	}

	/**
	 * Test the <code>canRead()</code> method where the owner implicitly can
	 * read.
	 */
	public void test_canReadOwnerImplicitly() {
		Acp acp;
		CanonicalUser id;
		Grantee foo, bar, baz, anonymous;

		acp = new Acp();

		id = new CanonicalUser("id");
		foo = new CanonicalUser("foo");
		bar = new CanonicalUser("bar");
		baz = new CanonicalUser("baz");
		anonymous = new CanonicalUser(CanonicalUser.ID_ANONYMOUS);

		acp.setOwner(id);

		acp.grant(foo, ResourcePermission.ACTION_WRITE);
		acp.grant(foo, ResourcePermission.ACTION_READ_ACP);
		acp.grant(bar, ResourcePermission.ACTION_WRITE);
		acp.grant(bar, ResourcePermission.ACTION_READ_ACP);
		acp.grant(AllUsersGroup.getInstance(), ResourcePermission.ACTION_READ);

		acp.canRead(id);
		acp.canRead(foo);
		acp.canRead(bar);
		try {
			acp.canRead(baz);
			fail("Should have thrown an exception");
		} catch (AccessControlException e) {
			// expected
		}
		try {
			acp.canRead(anonymous);
			fail("Should have thrown an exception");
		} catch (AccessControlException e) {
			// expected
		}
	}

	/**
	 * Test the <code>canWrite()</code> method.
	 */
	public void test_canWrite() {
		Acp acp;
		Grantee id, foo, bar, baz, anonymous;

		acp = new Acp();

		id = new CanonicalUser("id");
		foo = new CanonicalUser("foo");
		bar = new CanonicalUser("bar");
		baz = new CanonicalUser("baz");
		anonymous = new CanonicalUser(CanonicalUser.ID_ANONYMOUS);

		acp.grant(id, ResourcePermission.ACTION_FULL_CONTROL);
		acp.grant(foo, ResourcePermission.ACTION_WRITE);
		acp.grant(foo, ResourcePermission.ACTION_READ_ACP);
		acp.grant(bar, ResourcePermission.ACTION_WRITE);
		acp.grant(bar, ResourcePermission.ACTION_READ_ACP);
		acp.grant(AllUsersGroup.getInstance(), ResourcePermission.ACTION_READ);

		acp.canWrite(id);
		try {
			acp.canWrite(foo);
			fail("Should have thrown an exception");
		} catch (AccessControlException e) {
			// expected
		}
		try {
			acp.canWrite(bar);
			fail("Should have thrown an exception");
		} catch (AccessControlException e) {
			// expected
		}
		try {
			acp.canWrite(baz);
			fail("Should have thrown an exception");
		} catch (AccessControlException e) {
			// expected
		}
		try {
			acp.canWrite(anonymous);
			fail("Should have thrown an exception");
		} catch (AccessControlException e) {
			// expected
		}
	}

	/**
	 * Test the <code>canWrite()</code> method where the owner implicitly can
	 * write.
	 */
	public void test_canWriteOwnerImplicitly() {
		Acp acp;
		CanonicalUser id;
		Grantee foo, bar, baz, anonymous;

		acp = new Acp();

		id = new CanonicalUser("id");
		foo = new CanonicalUser("foo");
		bar = new CanonicalUser("bar");
		baz = new CanonicalUser("baz");
		anonymous = new CanonicalUser(CanonicalUser.ID_ANONYMOUS);

		acp.setOwner(id);

		acp.grant(foo, ResourcePermission.ACTION_WRITE);
		acp.grant(foo, ResourcePermission.ACTION_READ_ACP);
		acp.grant(bar, ResourcePermission.ACTION_WRITE);
		acp.grant(bar, ResourcePermission.ACTION_READ_ACP);
		acp.grant(AllUsersGroup.getInstance(), ResourcePermission.ACTION_READ);

		acp.canWrite(id);
		try {
			acp.canWrite(foo);
			fail("Should have thrown an exception");
		} catch (AccessControlException e) {
			// expected
		}
		try {
			acp.canWrite(bar);
			fail("Should have thrown an exception");
		} catch (AccessControlException e) {
			// expected
		}
		try {
			acp.canWrite(baz);
			fail("Should have thrown an exception");
		} catch (AccessControlException e) {
			// expected
		}
		try {
			acp.canWrite(anonymous);
			fail("Should have thrown an exception");
		} catch (AccessControlException e) {
			// expected
		}
	}
}
