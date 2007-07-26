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

/**
 * The special group that includes every non-anonymous principal.
 * 
 * @author Jesse Peterson
 */
public class AuthenticatedUsersGroup extends GroupBase {
	public static final String URI_STRING = "http://acs.amazonaws.com/groups/global/AuthenticatedUsers";

	private static AuthenticatedUsersGroup instance;

	/**
	 * Can only be one.
	 */
	private AuthenticatedUsersGroup(URI uri) {
		super(uri);
	}

	/**
	 * Get an instance of the <code>AuthenticatedUsersGroup</code>.
	 * 
	 * @return An instance of the <code>AuthenticatedUsersGroup</code>.
	 */
	public static AuthenticatedUsersGroup getInstance() {
		if (instance == null) {
			try {
				instance = new AuthenticatedUsersGroup(new URI(URI_STRING));
			} catch (URISyntaxException e) {
				// should not happen
				e.printStackTrace();
			}
		}
		return instance;
	}

	/**
	 * Adds the specified member to the group.
	 * 
	 * @param user
	 *            the principal to add to this group.
	 * @return <code>false</code>, because membership is implied, not
	 *         assigned.
	 */
	public boolean addMember(Principal user) {
		return false;
	}

	/**
	 * Removes the specified member from the group.
	 * 
	 * @param user
	 *            the principal to remove from this group.
	 * @return <code>false</code>, as you can not remove members from the
	 *         group.
	 */
	public boolean removeMember(Principal user) {
		return false;
	}

	/**
	 * Returns <code>true</code> if the passed principal is a member of the
	 * group.
	 * 
	 * @param member
	 *            the principal whose membership is to be checked.
	 * @return <code>true</code> if member is a <code>CanonicalUser</code>,
	 *         which imlies an authenticated user; <code>false</code>
	 *         otherwise.
	 */
	public boolean isMember(Principal member) {
		if (member instanceof CanonicalUser) {
			return true;
		}

		return false;
	}

	/**
	 * Returns an enumeration of the members in the group. The returned objects
	 * can be instances of either <code>Principal</code> or <code>Group</code>
	 * (which is a subclass of <code>Principal</code>).
	 */
	public Enumeration<? extends Principal> members() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Compares this group to the specified object. Returns <code>true</code>
	 * if the object passed in matches the principal represented by the
	 * implementation of this interface.
	 */
	public boolean equals(Object another) {
		if (another == this) {
			return true;
		}

		if ((another != null) && (another instanceof AuthenticatedUsersGroup)) {
			if (this.getName().equals(
					((AuthenticatedUsersGroup) another).getName())) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Return a hashcode for this group.
	 * 
	 * @return a hashcode for this group.
	 */
	public int hashCode() {
		return getName().hashCode();
	}
}
