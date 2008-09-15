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

import java.io.Serializable;

/**
 * Represents an S3 canonical user used in S3 access control.
 * 
 * @author Jesse Peterson
 */
public class CanonicalUser implements Grantee, Serializable {
	/**
	 * If incompatible serialization changes are made, mostly deleting methods,
	 * this must be changed.
	 */
	private static final long serialVersionUID = 1L;

	private String id;

	private String displayName;

	/**
	 * "Special" id that indicates the anonymous user id.
	 */
	public static final String ID_ANONYMOUS = "http://www.jpeterson.com/canonicalUser/anonymous";

	/**
	 * Basic constructor.
	 * 
	 * @param id
	 *            The id that uniquely and permanently identifies the user.
	 */
	public CanonicalUser(String id) {
		setId(id);
	}

	/**
	 * Get the id that uniquely and permanently identifies the user.
	 * 
	 * @return The id that uniquely and permanently identifies the user.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Set the id that uniquely and permanently identifies the user.
	 * 
	 * @param id
	 *            uniquely and permanently identifies the user.
	 */
	private void setId(String id) {
		this.id = id;
	}

	/**
	 * Get the string that identifies the user to a human.
	 * 
	 * @return The string that identifies the user to a human. If
	 *         <code>displayName</code> is <code>null</code>, return the value
	 *         from <code>getId()</code>.
	 */
	public String getDisplayName() {
		if (displayName == null) {
			return getId();
		}
		return displayName;
	}

	/**
	 * Set the string that identifies the user to a human.
	 * 
	 * @param displayName
	 *            The string that identifies the user to a human.
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * Returns the name of this principal.
	 * 
	 * @return the name of this principal. Implemented by returning the value
	 *         from <code>getItd()</code>.
	 */
	public String getName() {
		return getId();
	}

	/**
	 * Used to determine if the user is "anonymous". This means that the request
	 * was not authenticated.
	 * 
	 * @return <code>true</code> if this represents an authenticated principal,
	 *         <code>false</code> if this represents an anonymous principal.
	 */
	public boolean isAnonymous() {
		return id.equals(ID_ANONYMOUS);
	}

	/**
	 * Compares this principal to the specified object. Returns
	 * <code>true</code> if the object passed in matches the principal
	 * represented by the implementation of this interface.
	 * 
	 * @param another
	 *            principal to compare with.
	 * @return <code>true</code> if the principal passed in is the same as that
	 *         encapsulated by this principal, and <code>false</code> otherwise.
	 *         This only depends on the value of id, not the display name.
	 */
	public boolean equals(Object another) {
		if ((another != null) && (another instanceof CanonicalUser)) {
			if (this.getId().equals(((CanonicalUser) another).getId())) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns a hashcode for this principal.
	 * 
	 * @return a hashcode for this principal.
	 */
	public int hashCode() {
		return getName().hashCode();
	}

	/**
	 * Returns a string representation of this principal.
	 * 
	 * @return a string representation of this principal.
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();

		buffer.append("id ").append(id);

		return buffer.toString();
	}
}
