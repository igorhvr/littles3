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
import java.net.URI;
import java.security.acl.Group;

/**
 * Represents an S3 group used in S3 access control.
 * 
 * @author Jesse Peterson
 */
public abstract class GroupBase implements Grantee, Group, Serializable {
	/**
	 * If incompatible serialization changes are made, mostly deleting methods,
	 * this must be changed.
	 */
	private static final long serialVersionUID = 1L;

	private URI uri;

	/**
	 * Basic constructor.
	 * 
	 * @param uri
	 *            The URI identifying the group.
	 */
	public GroupBase(URI uri) {
		setUri(uri);
	}

	/**
	 * Get the URI identifying the group.
	 * 
	 * @return The URI identifying the group.
	 */
	public URI getUri() {
		return uri;
	}

	/**
	 * Set the URI identifying the group.
	 * 
	 * @param uri
	 *            The URI identifying the group.
	 */
	private void setUri(URI uri) {
		this.uri = uri;
	}

	/**
	 * Returns the name of the group.
	 * 
	 * @return the name of the group. Implemented by returning the string
	 *         representation of the uri.
	 */
	public String getName() {
		return uri.toString();
	}

	/**
	 * Returns a string representation of this group.
	 * 
	 * @return a string representation of this group.
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();

		buffer.append("uri ").append(uri.toString());

		return buffer.toString();
	}
}
