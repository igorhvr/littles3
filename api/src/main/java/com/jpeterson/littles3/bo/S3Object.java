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

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Object representation of an S3 object.
 * 
 * @author Jesse Peterson
 */
public abstract class S3Object extends Resource {
	/**
	 * If incompatible serialization changes are made, mostly deleting methods,
	 * this must be changed.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Default content type per the Amazon S3 Developer Guide (API Version
	 * 2006-03-01).
	 */
	public static final String DEFAULT_CONTENT_TYPE = "binary/octet-stream";

	protected String bucket;

	protected String key;

	protected URL storageUrl;

	protected long contentLength;

	protected String contentType = DEFAULT_CONTENT_TYPE;

	protected String contentMD5;

	protected String contentDisposition;

	protected String eTag;

	protected long lastModified;

	protected HashMap<String, List<String>> metadata = new HashMap<String, List<String>>();

	/**
	 * Basic constructor. Subclasses should be sure to call this parent
	 * constructor.
	 */
	public S3Object() {
		super();
	}

	/**
	 * Get the bucket containing this object.
	 * 
	 * @return The bucket containing this object.
	 */
	public String getBucket() {
		return bucket;
	}

	/**
	 * Set the bucket containing this object.
	 * 
	 * @param bucket
	 *            The bucket containing this object.
	 */
	public void setBucket(String bucket) {
		this.bucket = bucket;
	}

	/**
	 * Get the key for this object.
	 * 
	 * @return The key for this object.
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Set the key for this object.
	 * 
	 * @param key
	 *            The key for this object.
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * Get the storage URL for where the object's data is stored.
	 * 
	 * @return The storage URL for where the object's data is stored.
	 */
	public URL getStorageUrl() {
		return storageUrl;
	}

	/**
	 * Set the storage URL for where the object's data is stored.
	 * 
	 * @param storageUrl
	 *            The storage URL for where the object's data is stored.
	 */
	public void setStorageUrl(URL storageUrl) {
		this.storageUrl = storageUrl;
	}

	/**
	 * Get the length of the object's data.
	 * 
	 * @return The length of the object's data, in bytes.
	 */
	public long getContentLength() {
		return contentLength;
	}

	/**
	 * Set the length of the object's data.
	 * 
	 * @param contentLength
	 *            The length of the object's data, in bytes.
	 */
	public void setContentLength(long contentLength) {
		this.contentLength = contentLength;
	}

	/**
	 * Get the content type of the object's data.
	 * 
	 * @return The content type of the object's data.
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * Set the content type of the object's data.
	 * 
	 * @param contentType
	 *            The content type of the object's data.
	 */
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	/**
	 * Get the MD5 checksum of the object's content.
	 * 
	 * @return The MD5 checksum of the object's content.
	 */
	public String getContentMD5() {
		return contentMD5;
	}

	/**
	 * Set the MD5 checksum of the object's content.
	 * 
	 * @param contentMD5
	 *            The MD5 checksum of the object's content.
	 */
	public void setContentMD5(String contentMD5) {
		this.contentMD5 = contentMD5;
	}

	/**
	 * Get the content disposition.
	 * 
	 * @return The content disposition.
	 */
	public String getContentDisposition() {
		return contentDisposition;
	}

	/**
	 * Set the content disposition.
	 * 
	 * @param contentDisposition
	 *            The content disposition.
	 */
	public void setContentDisposition(String contentDisposition) {
		this.contentDisposition = contentDisposition;
	}

	/**
	 * Get the entity tag for the object content.
	 * 
	 * @return The entity tag for the object content.
	 */
	public String getETag() {
		return eTag;
	}

	/**
	 * Set the entity tag for the object content.
	 * 
	 * @param eTag
	 *            The entity tag for the object content.
	 */
	public void setETag(String eTag) {
		this.eTag = eTag;
	}

	/**
	 * Get the last modified timestamp of the object content.
	 * 
	 * @return The last modified timestamp of the object content.
	 */
	public long getLastModified() {
		return lastModified;
	}

	/**
	 * Set the last modified timestamp of the object content.
	 * 
	 * @param lastModified
	 *            The last modified timestamp of the object content.
	 */
	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}

	/**
	 * Get the <code>InputStream</code> to read the Object data from.
	 * 
	 * @return An <code>InputStream</code> to read the Object data from.
	 */
	public abstract InputStream getInputStream();

	/**
	 * Get the <code>OutputStream</code> to write the Object data to.
	 * 
	 * @return An <code>OutputStream</code> to write the Object data to.
	 */
	public abstract OutputStream getOutputStream();

	/**
	 * Delete the Object data.
	 * 
	 * @return <code>True</code> if data deleted, <code>false</code> otherwise.
	 */
	public abstract boolean deleteData();

	/**
	 * Get the first metadata value for a particular <code>name</code>.
	 * 
	 * @param name
	 *            Name of the metadata.
	 * @return The metadata name's value. May be <code>null</code> if no
	 *         metadata for the provided <code>name</code>.
	 */
	public String getMetadataValue(String name) {
		List<String> values = metadata.get(name);

		if ((values != null) && (values.size() > 0)) {
			return values.get(0);
		}

		return null;
	}

	/**
	 * Get the metadata values for a particular <code>name</code>.
	 * 
	 * @param name
	 *            Name of the metadata.
	 * @return An iterator or <code>String</code> values.
	 */
	public Iterator<String> getMetadataValues(String name) {
		List<String> values = metadata.get(name);

		if (values != null) {
			return values.iterator();
		}

		return new ArrayList<String>().iterator();
	}

	/**
	 * Get the metadata names.
	 * 
	 * @return The metadata names.
	 */
	public Iterator<String> getMetadataNames() {
		return metadata.keySet().iterator();
	}

	/**
	 * Add a value to the metadata.
	 * 
	 * @param name
	 *            The metadata <code>name</code>.
	 * @param value
	 *            The metadata <code>value</code>.
	 */
	public void addMetadata(String name, String value) {
		List<String> values = metadata.get(name);
		if (values == null) {
			values = new ArrayList<String>();
			metadata.put(name, values);
		}

		values.add(value);
	}
}
