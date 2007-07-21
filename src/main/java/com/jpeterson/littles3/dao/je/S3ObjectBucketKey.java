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

package com.jpeterson.littles3.dao.je;

import java.io.InputStream;
import java.io.OutputStream;

import com.jpeterson.littles3.bo.S3Object;

/**
 * Key for JE database that uses just the bucket and key.
 * 
 * @author Jesse Peterson
 * 
 */
public class S3ObjectBucketKey extends S3Object {

	/**
	 * Returns <code>false</code>.
	 * 
	 * @return <code>false</code>.
	 * @Override
	 */
	public boolean deleteData() {
		return false;
	}

	/**
	 * Returns <code>null</code>.
	 * 
	 * @return <code>null</code>.
	 * @Override
	 */
	public InputStream getInputStream() {
		return null;
	}

	/**
	 * Returns <code>null</code>.
	 * 
	 * @return <code>null</code>.
	 * @Override
	 */
	public OutputStream getOutputStream() {
		return null;
	}

	/**
	 * Create a String representation.
	 * 
	 * @return A String representation, in JSON format, consisting of the bucket
	 *         and key values.
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();

		buffer.append("{");
		buffer.append("\"bucket\":");
		if (getBucket() == null) {
			buffer.append("null");
		} else {
			buffer.append("\"").append(getBucket()).append("\"");
		}
		buffer.append(",");
		buffer.append("\"key\":");
		if (getBucket() == null) {
			buffer.append("null");
		} else {
			buffer.append("\"").append(getKey()).append("\"");
		}
		buffer.append("}");

		return buffer.toString();
	}
}
