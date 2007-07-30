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
import java.security.Permission;
import java.security.Permissions;
import java.util.Enumeration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implementation of S3 Access Control Policy.
 * 
 * @author Jesse Peterson
 */
public class Acp {
	protected Log logger;

	private CanonicalUser owner;

	private int grantCount = 0;

	private Permissions permissions = new Permissions();

	/**
	 * Maximum number of grants supported in one Access Control Policy.
	 */
	public static final int MAX_GRANT_COUNT = 100;

	/**
	 * Basic constructor.
	 */
	public Acp() {
		logger = LogFactory.getLog(this.getClass());
	}

	/**
	 * Determines whether the access request indicated by the specified
	 * permission should be allowed or denied, based on the Access Control
	 * Policy. This method quietly returns if the access request is permitted,
	 * or throws a suitable <code>AccessControlException</code> otherwise.
	 * 
	 * @param permission
	 *            the request permission
	 * @throws AccessControlException
	 *             if the specified permission is not permitted.
	 */
	public void checkPermission(Permission permission)
			throws AccessControlException {
		if (permissions.implies(permission)) {
			// permission granted
			return;
		}

		throw new AccessControlException(
				"Access Control Policy does not grant this permission",
				permission);
	}

	/**
	 * Get the owner of the resource that this Access Control Policy protects.
	 * 
	 * @return The owner of the resource that this Acces Control Policy
	 *         protects.
	 */
	public CanonicalUser getOwner() {
		return owner;
	}

	/**
	 * Set the owner of the resource that this Access Control Policy protects.
	 * 
	 * @param owner
	 *            The owner of the resource that this Access Control Policy
	 *            protects.
	 */
	public void setOwner(CanonicalUser owner) {
		this.owner = owner;
	}

	/**
	 * Grant a <code>permission</code> to the <code>grantee</code>.
	 * 
	 * @param grantee
	 *            The grantee being granted the permission.
	 * @param permission
	 *            The permission to grant to the grantee. Should be one of the
	 *            available ResourcePermission "actions".
	 * @throws IndexOutOfBoundsException
	 *             Thrown if too many grants are made.
	 * @see ResourcePermission#ACTION_READ
	 * @see ResourcePermission#ACTION_WRITE
	 * @see ResourcePermission#ACTION_READ_ACP
	 * @see ResourcePermission#ACTION_WRITE_ACP
	 * @see ResourcePermission#ACTION_FULL_CONTROL
	 */
	public void grant(Grantee grantee, String permission)
			throws IndexOutOfBoundsException {
		if (grantCount >= MAX_GRANT_COUNT) {
			throw new IndexOutOfBoundsException(
					"Maximum number of grants reached: " + MAX_GRANT_COUNT);
		}

		Permission perm;
		perm = new ResourcePermission(grantee, permission);
		permissions.add(perm);

		++grantCount;
	}

	/**
	 * Returns an enumeration of all of the grants in this Access Control
	 * Policy.
	 * 
	 * @return an enumeration of the grants in the form of a
	 *         <code>ResourcePermission</code> for each grant.
	 */
	public Enumeration grants() {
		return permissions.elements();
	}

	/**
	 * Returns the number of grants in this Access Control Policy.
	 * 
	 * @return the number of grants in this Acces Control Policy.
	 */
	public int size() {
		return grantCount;
	}

	/**
	 * Determines if the <code>principal</code> can "read" the resource. The
	 * meaning of "read" depends on the particular implementation of the
	 * resource.
	 * 
	 * @param grantee
	 *            The entity to check and see if they can "read" the resource.
	 * @throws AccessControlException
	 *             Thrown if the <code>principal</code> can not "read" the
	 *             resource.
	 */
	public void canRead(Grantee grantee) throws AccessControlException {
		Permission permission;

		// owner has "READ_ACP" permission implicitly.
		if (grantee.equals(owner)) {
			return;
		}

		permission = new ResourcePermission(grantee,
				ResourcePermission.ACTION_READ_ACP);
		checkPermission(permission);
	}

	/**
	 * Determines if the <code>principal</code> can "write" the resource. The
	 * meaning of "write" depends on the particular implementation of the
	 * resource.
	 * 
	 * @param grantee
	 *            The entity to check and see if they can "write" the resource.
	 * @throws AccessControlException
	 *             Thrown if the <code>principal</code> can not "write" the
	 *             resource.
	 */
	public void canWrite(Grantee grantee) throws AccessControlException {
		Permission permission;

		// owner has "WRITE_ACP" permission implicitly.
		if (grantee.equals(owner)) {
			return;
		}

		permission = new ResourcePermission(grantee,
				ResourcePermission.ACTION_WRITE_ACP);
		checkPermission(permission);
	}

	/**
	 * Encode an Acp as XML.
	 * 
	 * @param acp
	 *            The Acp to encode.
	 * @return The Acp encoded in XML.
	 */
	public static String encode(Acp acp) {
		StringBuffer buffer = new StringBuffer();

		buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		buffer.append("<AccessControlPolicy>");
		buffer.append("<Owner>");
		buffer.append("<ID>").append(acp.getOwner().getId()).append("</ID>");
		buffer.append("<DisplayName>").append(acp.getOwner().getDisplayName())
				.append("</DisplayName>");
		buffer.append("</Owner>");
		buffer.append("<AccessControlList>");
		for (Enumeration grants = acp.grants(); grants.hasMoreElements();) {
			ResourcePermission grant = (ResourcePermission) grants
					.nextElement();

			buffer.append("<Grant>");

			Grantee grantee = grant.getGrantee();
			if (grantee instanceof GroupBase) {
				GroupBase group = (GroupBase) grantee;
				buffer
						.append("<Grantee xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"Group\">");
				buffer.append("<URI>").append(group.getUri().toString())
						.append("</URI>");
				buffer.append("</Grantee>");
			} else {
				CanonicalUser user = (CanonicalUser) grantee;
				buffer
						.append("<Grantee xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"CanonicalUser\">");
				buffer.append("<ID>").append(user.getId()).append("</ID>");
				buffer.append("<DisplayName>").append(user.getDisplayName())
						.append("</DisplayName>");
				buffer.append("</Grantee>");
			}
			// TODO: mighty hard coded...could be made a bit better
			String permission = grant.getActions();
			if (permission.equals("READ,WRITE,READ_ACP,WRITE_ACP")) {
				permission = "FULL_CONTROL";
			}
			buffer.append("<Permission>").append(permission).append(
					"</Permission>");

			buffer.append("</Grant>");
		}
		buffer.append("</AccessControlList>");
		buffer.append("</AccessControlPolicy>");

		return buffer.toString();
	}
}
