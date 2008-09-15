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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class CanonicalUserTest extends TestCase {
	private Log logger;

	/**
	 * Create the test case
	 * 
	 * @param testName
	 *            name of the test case
	 */
	public CanonicalUserTest(String testName) {
		super(testName);

		logger = LogFactory.getLog(this.getClass());
		logger.debug("CanonicalUserTest");
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(CanonicalUserTest.class);
	}

	/**
	 * Test the constructor.
	 */
	public void test_constructor() {
		CanonicalUser user;
		String id = "id";

		user = new CanonicalUser(id);

		assertEquals("Unexpected value", id, user.getId());
	}

	/**
	 * Test the <code>displayName</code> property access methods.
	 */
	public void test_displayName() {
		CanonicalUser user;
		String id = "id";
		String displayName = "displayName";

		user = new CanonicalUser(id);

		assertEquals("Unexpected value", id, user.getDisplayName());
		user.setDisplayName(displayName);
		assertEquals("Unexpected value", displayName, user.getDisplayName());
	}

	/**
	 * Test the <code>getName()</code> method.
	 */
	public void test_getName() {
		CanonicalUser user;
		String id = "id";

		user = new CanonicalUser(id);

		assertEquals("Unexpected value", id, user.getName());
	}

	/**
	 * Test the <code>equals()</code> method.
	 */
	public void test_equals() {
		CanonicalUser user;
		String id = "id";

		user = new CanonicalUser(id);

		assertTrue("Should be equal", user.equals(user));
		assertTrue("Should be equal", user.equals(new CanonicalUser(id)));
		assertFalse("Should not be equal", user.equals(null));
		assertFalse("Should not be equal", user.equals(new CanonicalUser(
				"canonicalUser")));
	}

	/**
	 * Test the <code>toString()</code> method.
	 */
	public void test_toString() {
		CanonicalUser user;
		String id = "id";

		user = new CanonicalUser(id);

		assertEquals("Unexpected value", "id " + id, user.toString());
	}

	/**
	 * Test that an instance is serializable.
	 */
	public void test_serialization1() {
		CanonicalUser user, reconstitutedUser;
		ByteArrayInputStream bais;
		ByteArrayOutputStream baos;
		ObjectInputStream ois;
		ObjectOutputStream oos;

		user = new CanonicalUser("test");

		try {
			baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);

			oos.writeObject(user);

			bais = new ByteArrayInputStream(baos.toByteArray());
			ois = new ObjectInputStream(bais);

			reconstitutedUser = (CanonicalUser) ois.readObject();

			assertEquals("Unexpected user", user, reconstitutedUser);
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unexpected exception");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			fail("Unexpected exception");
		}
	}

	/**
	 * Test that an instance is serializable.
	 */
	public void test_serialization2() {
		CanonicalUser user, reconstitutedUser;
		ByteArrayInputStream bais;
		ByteArrayOutputStream baos;
		ObjectInputStream ois;
		ObjectOutputStream oos;

		user = new CanonicalUser("test");

		try {
			user.setDisplayName("Unit Test");
			
			baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);

			oos.writeObject(user);

			bais = new ByteArrayInputStream(baos.toByteArray());
			ois = new ObjectInputStream(bais);

			reconstitutedUser = (CanonicalUser) ois.readObject();

			assertEquals("Unexpected user", user, reconstitutedUser);
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unexpected exception");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			fail("Unexpected exception");
		}
	}
}
