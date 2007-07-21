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

import org.apache.commons.logging.Log;

public abstract class S3Object {
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

	protected Log logger;

	public S3Object() {

	}

	public String getBucket() {
		return bucket;
	}

	public void setBucket(String bucket) {
		this.bucket = bucket;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public URL getStorageUrl() {
		return storageUrl;
	}

	public void setStorageUrl(URL storageUrl) {
		this.storageUrl = storageUrl;
	}

	public long getContentLength() {
		return contentLength;
	}

	public void setContentLength(long contentLength) {
		this.contentLength = contentLength;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getContentMD5() {
		return contentMD5;
	}

	public void setContentMD5(String contentMD5) {
		this.contentMD5 = contentMD5;
	}

	public String getContentDisposition() {
		return contentDisposition;
	}

	public void setContentDisposition(String contentDisposition) {
		this.contentDisposition = contentDisposition;
	}

	public String getETag() {
		return eTag;
	}

	public void setETag(String eTag) {
		this.eTag = eTag;
	}

	public long getLastModified() {
		return lastModified;
	}

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
	 * @return <code>True</code> if data deleted, <code>false</code>
	 *         otherwise.
	 */
	public abstract boolean deleteData();
}
