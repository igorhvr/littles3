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

/**
 * Represents an S3 anonymous user used in S3 access control.
 * 
 * @author Jesse Peterson
 */
public class AnonymousUser implements Grantee {
	private static AnonymousUser instance;

	/**
	 * Empty constructor.
	 */
	private AnonymousUser() {

	}

	/**
	 * Get an instance of the <code>AnonymousUser</code>.
	 * 
	 * @return An instance of the <code>AnonymousUser</code>.
	 */
	public static AnonymousUser getInstance() {
		if (instance == null) {
			instance = new AnonymousUser();
		}
		return instance;
	}

	/**
	 * Returns the name of this principal.
	 * 
	 * @return the name of this principal. Implemented by returning the value
	 *         from <code>getItd()</code>.
	 */
	public String getName() {
		return "anonymous";
	}

	/**
	 * Compares this principal to the specified object. Returns
	 * <code>true</code> if the object passed in matches the principal
	 * represented by the implementation of this interface.
	 * 
	 * @param another
	 *            principal to compare with.
	 * @return <code>true</code> if the principal passed in is the same as
	 *         that encapsulated by this principal, and <code>false</code>
	 *         otherwise.
	 */
	public boolean equals(Object another) {
		if (another == this) {
			return true;
		}

		if ((another != null) && (another instanceof AnonymousUser)) {
			if (this.getName().equals(((AnonymousUser) another).getName())) {
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
		return getName();
	}
}
