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
import java.security.Permission;
import java.security.acl.Group;

/**
 * Implementation of the S3 resource permission.
 * 
 * @author Jesse Peterson
 */
public class ResourcePermission extends Permission {
	// inspired by java.io.FilePermission implementation
	public static final String ACTION_READ = "READ";

	public static final String ACTION_WRITE = "WRITE";

	public static final String ACTION_READ_ACP = "READ_ACP";

	public static final String ACTION_WRITE_ACP = "WRITE_ACP";

	public static final String ACTION_FULL_CONTROL = "FULL_CONTROL";

	private static final int READ = 0x1;

	private static final int WRITE = 0x2;

	private static final int READ_ACP = 0x4;

	private static final int WRITE_ACP = 0x8;

	private static final int FULL_CONTROL = READ | WRITE | READ_ACP | WRITE_ACP;

	private static final int NONE = 0x0;

	private Grantee grantee;

	// lazy init
	private String actions = null;

	private transient int mask;

	/**
	 * Default value.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * A permission for a resource, where a grantee is granted an action.
	 * 
	 * @param grantee
	 *            The grantee of the permission.
	 * @param actions
	 *            The action being granted to the grantee.
	 */
	public ResourcePermission(Grantee grantee, String actions) {
		super(grantee.toString());
		this.grantee = grantee;
		init(getMask(actions));
	}

	/**
	 * Get the grantee that this permission is for.
	 * 
	 * @return The grantee that this permission is for.
	 */
	public Grantee getGrantee() {
		return grantee;
	}

	/**
	 * Validates and saves the mask.
	 * 
	 * @param mask
	 *            The actions mask.
	 */
	private void init(int mask) {
		if ((mask & FULL_CONTROL) != mask) {
			throw new IllegalArgumentException("invalid actions mask");
		}

		if (mask == NONE) {
			throw new IllegalArgumentException("invalid actions mask");
		}

		this.mask = mask;
	}

	/**
	 * Create a String representation of the actions encoded in the bit mask.
	 * 
	 * @param mask
	 *            The bit mask of actions.
	 * @return A String representation, in the canonical form. The order is
	 *         "READ", "WRITE", "READ_ACP", "WRITE_ACP". If all actions, returns
	 *         the shorthand action "FULL_CONTROL".
	 */
	private static String getActions(int mask) {
		StringBuffer buffer = new StringBuffer();
		boolean comma = false;

		if ((mask & FULL_CONTROL) == FULL_CONTROL) {
			return (ACTION_FULL_CONTROL);
		}

		if ((mask & READ) == READ) {
			comma = true;
			buffer.append(ACTION_READ);
		}

		if ((mask & WRITE) == WRITE) {
			if (comma)
				buffer.append(',');
			else
				comma = true;
			buffer.append(ACTION_WRITE);
		}

		if ((mask & READ_ACP) == READ_ACP) {
			if (comma)
				buffer.append(',');
			else
				comma = true;
			buffer.append(ACTION_READ_ACP);
		}

		if ((mask & WRITE_ACP) == WRITE_ACP) {
			if (comma)
				buffer.append(',');
			else
				comma = true;
			buffer.append(ACTION_WRITE_ACP);
		}

		return buffer.toString();
	}

	@Override
	public String getActions() {
		if (actions == null) {
			actions = getActions(mask);
		}
		return actions;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}

		if (!(obj instanceof ResourcePermission)) {
			return false;
		}

		ResourcePermission that = (ResourcePermission) obj;

		return (this.mask == that.mask) && (this.grantee.equals(that.grantee));
	}

	@Override
	public int hashCode() {
		int hashCode = 0;

		if (grantee != null) {
			hashCode ^= grantee.hashCode();
		}

		hashCode ^= getActions().hashCode();

		return hashCode;
	}

	@Override
	public boolean implies(Permission permission) {
		if (!(permission instanceof ResourcePermission)) {
			return false;
		}

		ResourcePermission that = (ResourcePermission) permission;

		return ((this.mask & that.mask) == that.mask)
				&& impliesIgnoreMask(that);
	}

	/**
	 * Checks if the Permission's actions are a proper subset of the this
	 * object's actions. Returns the effective mask iff the this
	 * ResourcePermission's grantee also imlies that ResourcePermission's
	 * grantee.
	 * 
	 * @param that
	 *            the ResourcePermission to check against.
	 * @return <code>true</code> if this grantee equals that grantee of if
	 *         this grantee is a <code>Group</code> and that grantee is a
	 *         member of this group; <code>false</code> otherwise.
	 */
	boolean impliesIgnoreMask(ResourcePermission that) {
		if (this.grantee.equals(that.grantee)) {
			return true;
		}

		if (this.grantee instanceof Group) {
			Group group = (Group) this.grantee;

			return group.isMember(that.grantee);
		}

		return false;
	}

	/**
	 * Parse the actions string into a bit mask.
	 * 
	 * @param actions
	 *            The actions string. Possible actions are "READ", "WRITE",
	 *            "READ_ACP", "WRITE_ACP", "FULL_CONTROL". Multiple actions can
	 *            be specified by separating them by a comma (',').
	 * @return
	 */
	private static int getMask(String actions) {
		int mask = NONE;

		if (actions == null) {
			return mask;
		}

		// process 'backwards'
		char[] a = actions.toCharArray();

		int i = a.length - 1;
		if (i < 0) {
			return mask;
		}

		while (i != -1) {
			char c;

			// skip whitespace
			while ((i != -1)
					&& ((c = a[i]) == ' ' || c == '\r' || c == '\n'
							|| c == '\f' || c == '\t')) {
				i--;
			}

			// look for the action strings
			int matchlen;

			if (i >= 3 && (a[i - 3] == 'r' || a[i - 3] == 'R')
					&& (a[i - 2] == 'e' || a[i - 2] == 'E')
					&& (a[i - 1] == 'a' || a[i - 1] == 'A')
					&& (a[i] == 'd' || a[i] == 'D')) {
				matchlen = 4;
				mask |= READ;
			} else if (i >= 4 && (a[i - 4] == 'w' || a[i - 4] == 'W')
					&& (a[i - 3] == 'r' || a[i - 3] == 'R')
					&& (a[i - 2] == 'i' || a[i - 2] == 'I')
					&& (a[i - 1] == 't' || a[i - 1] == 'T')
					&& (a[i] == 'e' || a[i] == 'E')) {
				matchlen = 5;
				mask |= WRITE;
			} else if (i >= 7 && (a[i - 7] == 'r' || a[i - 7] == 'R')
					&& (a[i - 6] == 'e' || a[i - 6] == 'E')
					&& (a[i - 5] == 'a' || a[i - 5] == 'A')
					&& (a[i - 4] == 'd' || a[i - 4] == 'D')
					&& (a[i - 3] == '_')
					&& (a[i - 2] == 'a' || a[i - 2] == 'A')
					&& (a[i - 1] == 'c' || a[i - 1] == 'C')
					&& (a[i] == 'p' || a[i] == 'P')) {
				matchlen = 8;
				mask |= READ_ACP;
			} else if (i >= 8 && (a[i - 8] == 'w' || a[i - 8] == 'W')
					&& (a[i - 7] == 'r' || a[i - 7] == 'R')
					&& (a[i - 6] == 'i' || a[i - 6] == 'I')
					&& (a[i - 5] == 't' || a[i - 5] == 'T')
					&& (a[i - 4] == 'e' || a[i - 4] == 'E')
					&& (a[i - 3] == '_')
					&& (a[i - 2] == 'a' || a[i - 2] == 'A')
					&& (a[i - 1] == 'c' || a[i - 1] == 'C')
					&& (a[i] == 'p' || a[i] == 'P')) {
				matchlen = 9;
				mask |= WRITE_ACP;
			} else if (i >= 11 && (a[i - 11] == 'f' || a[i - 11] == 'F')
					&& (a[i - 10] == 'u' || a[i - 10] == 'U')
					&& (a[i - 9] == 'l' || a[i - 9] == 'L')
					&& (a[i - 8] == 'l' || a[i - 8] == 'L')
					&& (a[i - 7] == '_')
					&& (a[i - 6] == 'c' || a[i - 6] == 'C')
					&& (a[i - 5] == 'o' || a[i - 5] == 'O')
					&& (a[i - 4] == 'n' || a[i - 4] == 'N')
					&& (a[i - 3] == 't' || a[i - 3] == 'T')
					&& (a[i - 2] == 'r' || a[i - 2] == 'R')
					&& (a[i - 1] == 'o' || a[i - 1] == 'O')
					&& (a[i] == 'l' || a[i] == 'L')) {
				matchlen = 12;
				mask |= READ;
				mask |= WRITE;
				mask |= READ_ACP;
				mask |= WRITE_ACP;
			} else {
				throw new IllegalArgumentException("invalid actions: "
						+ actions);
			}

			// make sure we didn't just match the tail of a word. Also skip to
			// the comma.
			boolean seencomma = false;
			while (i >= matchlen && !seencomma) {
				switch (a[i - matchlen]) {
				case ',':
					seencomma = true;
				case ' ':
				case '\r':
				case '\n':
				case '\f':
				case '\t':
					break;
				default:
					throw new IllegalArgumentException("invalid actions: "
							+ actions);
				}
				i--;
			}

			// point i at the location of the comma minus one (or -1).
			i -= matchlen;
		}

		return mask;
	}

	/**
	 * writeObject is called to save the state of the ResourcePermission to a
	 * stream. The actions are serialized, and the superclass takes care of the
	 * name.
	 * 
	 * @param s
	 *            The ObjectOuptputStream to serialize the object to.
	 * @throws IOException
	 *             Unable to write the object.
	 */
	private void writeObject(ObjectOutputStream s) throws IOException {
		// write out the actions. The superclass takes care of the name
		// call getActions to make sure actions field is initialized
		if (actions == null) {
			getActions();
		}
		s.defaultWriteObject();
	}

	/**
	 * readObject is called to restore the state of the ResourcePermission from
	 * a stream.
	 * 
	 * @param s
	 *            The ObjectInputStream to serialize the object from.
	 * @throws IOException
	 *             Unable to read the object.
	 * @throws ClassNotFoundException
	 *             Unalbe to read the object.
	 */
	private void readObject(ObjectInputStream s) throws IOException,
			ClassNotFoundException {
		// Read in the actions, then restore everything els by calling init.
		s.defaultReadObject();
		init(getMask(actions));
	}
}
