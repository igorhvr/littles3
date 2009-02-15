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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.AccessControlException;
import java.security.Permission;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * General base functionality for S3 bucket and object items.
 * 
 * @author Jesse Peterson
 */
public abstract class Resource implements Serializable {
	/**
	 * If incompatible serialization changes are made, mostly deleting methods,
	 * this must be changed.
	 */
	private static final long serialVersionUID = 1L;

	private Acp acp;

	transient protected Log logger;

	/**
	 * Basic constructor.
	 */
	public Resource() {
		init();
	}

	/**
	 * Initialization routines. Allows the <code>transient</code> to be
	 * initialized when deserialized.
	 */
	private void init() {
		logger = LogFactory.getLog(this.getClass());
	}

	/**
	 * Get the Access Control Policy (ACP) for this resource.
	 * 
	 * @return The Access Control Policy (ACP) for this resource.
	 */
	public Acp getAcp() {
		return acp;
	}

	/**
	 * Set the Access Control Policy (ACP) for this resource.
	 * 
	 * @param acp
	 *            The Access Control Policy for this resource.
	 */
	public void setAcp(Acp acp) {
		this.acp = acp;
	}

	/**
	 * Determines if the <code>principal</code> can "read" the resource. The
	 * meaning of "read" depends on the particular implementation of the
	 * resource.
	 * 
	 * @param grantee
	 *            The entity to check and see if they can "read" the resource.
	 *            This is typically the authenticated principal requesting to
	 *            read the resource.
	 * @throws AccessControlException
	 *             Thrown if the <code>principal</code> can not "read" the
	 *             resource.
	 */
	public void canRead(Grantee grantee) throws AccessControlException {
		if (acp == null) {
			throw new AccessControlException(
					"Access Control Policy is null, therefore, no grants");
		}

		Permission permission = new ResourcePermission(grantee,
				ResourcePermission.ACTION_READ);
		acp.checkPermission(permission);
	}

	/**
	 * Determines if the <code>principal</code> can "write" the resource. The
	 * meaning of "write" depends on the particular implementation of the
	 * resource.
	 * 
	 * @param grantee
	 *            The entity to check and see if they can "write" the resource.
	 *            This is typically the authenticated principal requesting to
	 *            write the resource.
	 * @throws AccessControlException
	 *             Thrown if the <code>principal</code> can not "write" the
	 *             resource.
	 */
	public void canWrite(Grantee grantee) throws AccessControlException {
		if (acp == null) {
			throw new AccessControlException(
					"Access Control Policy is null, therefore, no grants");
		}

		Permission permission = new ResourcePermission(grantee,
				ResourcePermission.ACTION_WRITE);
		acp.checkPermission(permission);
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		in.defaultReadObject();

		// initialize transient variable(s)
		init();
	}
}
