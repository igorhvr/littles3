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
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.AccessControlException;
import java.security.Permission;
import java.security.Permissions;
import java.util.Enumeration;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * Implementation of S3 Access Control Policy.
 * 
 * @author Jesse Peterson
 */
public class Acp implements Serializable {
	/**
	 * If incompatible serialization changes are made, mostly deleting methods,
	 * this must be changed.
	 */
	private static final long serialVersionUID = 1L;

	transient protected Log logger;

	private CanonicalUser owner;

	private int grantCount = 0;

	private Permissions permissions = new Permissions();

	/**
	 * Maximum number of grants supported in one Access Control Policy.
	 */
	public static final int MAX_GRANT_COUNT = 100;

	private static final String ELEMENT_ACCESS_CONTROL_POLICY = "AccessControlPolicy";

	private static final String ELEMENT_OWNER = "Owner";

	private static final String ELEMENT_ACCESS_CONTROL_LIST = "AccessControlList";

	private static final String ELEMENT_GRANT = "Grant";

	private static final String ELEMENT_PERMISSION = "Permission";

	private static final String ELEMENT_GRANTEE = "Grantee";

	private static final String ELEMENT_URI = "URI";

	private static final String ELEMENT_ID = "ID";

	private static final String ELEMENT_DISPLAY_NAME = "DisplayName";

	private static final String ATTRIBUTE_TYPE = "type";

	private static final String ATTRIBUTE_TYPE_VALUE_GROUP = "Group";

	private static final String ATTRIBUTE_TYPE_VALUE_CANONICAL_USER = "CanonicalUser";

	private static final Namespace NAMESPACE_XSI = Namespace.getNamespace(
			"xsi", "http://www.w3.org/2001/XMLSchema-instance");

	// private static final Namespace NAMESPACE_S3 =
	// Namespace.getNamespace("s3", "http://s3.amazonaws.com/doc/2006-03-01/");

	/**
	 * Basic constructor.
	 */
	public Acp() {
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
		Document accessControlPolicy;
		Element accessControlPolicyElement;
		Element ownerElement;
		Element accessControlListElement;
		Element grantElement;

		accessControlPolicyElement = new Element(ELEMENT_ACCESS_CONTROL_POLICY);

		ownerElement = new Element(ELEMENT_OWNER);
		encodeCanonicalUser(acp.getOwner(), ownerElement);
		accessControlPolicyElement.addContent(ownerElement);

		accessControlListElement = new Element(ELEMENT_ACCESS_CONTROL_LIST);

		for (Enumeration grants = acp.grants(); grants.hasMoreElements();) {
			ResourcePermission grant = (ResourcePermission) grants
					.nextElement();
			Grantee grantee = grant.getGrantee();

			grantElement = new Element(ELEMENT_GRANT);

			if (grantee instanceof GroupBase) {
				encodeGroupGrantee((GroupBase) grantee, grantElement);
			} else {
				encodeCanonicalUserGrantee((CanonicalUser) grantee,
						grantElement);
			}

			grantElement.addContent(new Element(ELEMENT_PERMISSION)
					.setText(grant.getActions()));

			accessControlListElement.addContent(grantElement);
		}
		accessControlPolicyElement.addContent(accessControlListElement);

		accessControlPolicy = new Document(accessControlPolicyElement);

		return new XMLOutputter(Format.getCompactFormat())
				.outputString(accessControlPolicy);
	}

	/**
	 * Encode a group grantee.
	 * 
	 * @param group
	 *            The group to encode.
	 * @param parent
	 *            The parent element to contain the grantee fragment.
	 */
	private static void encodeGroupGrantee(GroupBase group, Element parent) {
		Element granteeElement = new Element(ELEMENT_GRANTEE);

		granteeElement.setAttribute(ATTRIBUTE_TYPE, ATTRIBUTE_TYPE_VALUE_GROUP,
				NAMESPACE_XSI);
		granteeElement.addContent(new Element(ELEMENT_URI).setText(group
				.getUri().toString()));

		parent.addContent(granteeElement);
	}

	/**
	 * Encode a canonical user grantee.
	 * 
	 * @param user
	 *            The user to encode.
	 * @param parent
	 *            The parent element to contain the grantee fragment.
	 */
	private static void encodeCanonicalUserGrantee(CanonicalUser user,
			Element parent) {
		Element granteeElement = new Element(ELEMENT_GRANTEE);

		granteeElement.setAttribute(ATTRIBUTE_TYPE,
				ATTRIBUTE_TYPE_VALUE_CANONICAL_USER, NAMESPACE_XSI);
		encodeCanonicalUser(user, granteeElement);

		parent.addContent(granteeElement);
	}

	/**
	 * Encode a canonical user.
	 * 
	 * @param user
	 *            The user to encode.
	 * @param parent
	 *            The parent element to contain the canonical user fragment.
	 */
	private static void encodeCanonicalUser(CanonicalUser user, Element parent) {
		parent.addContent(new Element(ELEMENT_ID).setText(user.getId()));
		parent.addContent(new Element(ELEMENT_DISPLAY_NAME).setText(user
				.getDisplayName()));
	}

	/**
	 * Decode an Acp as XML.
	 * 
	 * @param xml
	 *            The Acp encoded as XML.
	 * @return The Acp decoded from the XML.
	 */
	public static Acp decode(InputStream xml) throws IOException {
		Acp acp;
		SAXBuilder builder;
		Document document;
		Element accessControlPolicyElement;
		Element ownerElement;
		Element idElement, displayNameElement;
		Element accessControlListElement;
		Element grantElement;
		Element granteeElement, permissionElement;
		Element uriElement;
		Attribute typeAttribute;
		CanonicalUser user;
		Namespace defaultNamespace;

		acp = new Acp();

		builder = new SAXBuilder();

		try {
			document = builder.build(xml);

			accessControlPolicyElement = document.getRootElement();

			if (!(ELEMENT_ACCESS_CONTROL_POLICY
					.equals(accessControlPolicyElement.getName()))) {
				// TODO: indicate error

				System.out.println("Constant ELEMENT_ACCESS_CONTROL_POLICY: "
						+ ELEMENT_ACCESS_CONTROL_POLICY);
				System.out.println("accessControlPolicyElement.getName(): "
						+ accessControlPolicyElement.getName());

				throw new IOException("Invalid root element: "
						+ accessControlPolicyElement);
			}

			defaultNamespace = accessControlPolicyElement.getNamespace();

			if ((ownerElement = accessControlPolicyElement.getChild(
					ELEMENT_OWNER, defaultNamespace)) == null) {
				throw new IOException(
						"Invalid XML. Should have 'Owner' element");
			}

			if ((idElement = ownerElement
					.getChild(ELEMENT_ID, defaultNamespace)) == null) {
				throw new IOException("Invalid XML. Should have 'ID' element");
			}

			user = new CanonicalUser(idElement.getText());

			if ((displayNameElement = ownerElement.getChild(
					ELEMENT_DISPLAY_NAME, defaultNamespace)) != null) {
				user.setDisplayName(displayNameElement.getText());
			}

			acp.setOwner(user);

			if ((accessControlListElement = accessControlPolicyElement
					.getChild(ELEMENT_ACCESS_CONTROL_LIST, defaultNamespace)) == null) {
				throw new IOException(
						"Invalid XML. Should have 'AccessControlList' element");
			}

			for (Iterator grants = accessControlListElement.getChildren(
					ELEMENT_GRANT, defaultNamespace).iterator(); grants
					.hasNext();) {
				Grantee grantee;

				grantElement = (Element) grants.next();
				if ((granteeElement = grantElement.getChild(ELEMENT_GRANTEE,
						defaultNamespace)) == null) {
					throw new IOException(
							"Invalid XML. Should have 'Grantee' element");
				}
				if ((permissionElement = grantElement.getChild(
						ELEMENT_PERMISSION, defaultNamespace)) == null) {
					throw new IOException(
							"Invalid XML. Should have 'Permission' element");
				}

				if ((typeAttribute = granteeElement.getAttribute(
						ATTRIBUTE_TYPE, NAMESPACE_XSI)) == null) {
					throw new IOException(
							"Invalid XML. Should have 'type' attribute");
				}
				String typeValue = typeAttribute.getValue();
				if (ATTRIBUTE_TYPE_VALUE_CANONICAL_USER.equals(typeValue)) {
					if ((idElement = granteeElement.getChild(ELEMENT_ID,
							defaultNamespace)) == null) {
						throw new IOException(
								"Invalid XML. Should have 'ID' element");
					}

					user = new CanonicalUser(idElement.getText());

					if ((displayNameElement = granteeElement.getChild(
							ELEMENT_DISPLAY_NAME, defaultNamespace)) != null) {
						user.setDisplayName(displayNameElement.getText());
					}

					grantee = user;
				} else if (ATTRIBUTE_TYPE_VALUE_GROUP.equals(typeValue)) {
					if ((uriElement = granteeElement.getChild(ELEMENT_URI,
							defaultNamespace)) == null) {
						throw new IOException(
								"Invalid XML. Should have 'URI' element");
					}

					String uriValue = uriElement.getValue();
					if (AllUsersGroup.URI_STRING.equals(uriValue)) {
						grantee = AllUsersGroup.getInstance();
					} else if (AuthenticatedUsersGroup.URI_STRING
							.equals(uriValue)) {
						grantee = AuthenticatedUsersGroup.getInstance();
					} else {
						throw new IOException("Unknown group uri: " + uriValue);
					}
				} else {
					throw new IOException("Unknown type: " + typeValue);
				}

				try {
					acp.grant(grantee, permissionElement.getValue());
				} catch (IllegalArgumentException e) {
					IOException ex = new IOException("Invalid permission: "
							+ permissionElement.getValue());
					ex.initCause(e);
					throw ex;
				}
			}
		} catch (JDOMException e) {
			IOException ex = new IOException(e.getMessage());
			ex.initCause(e);
			throw ex;
		}

		return acp;
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
