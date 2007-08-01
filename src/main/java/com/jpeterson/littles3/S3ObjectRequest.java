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

package com.jpeterson.littles3;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.Principal;

import javax.servlet.http.HttpServletRequest;

import com.jpeterson.littles3.bo.CanonicalUser;

/**
 * Data structure for parsing an S3 object request.
 * 
 * @author Jesse Peterson
 */
public class S3ObjectRequest {
	private String serviceEndpoint;

	private String bucket;

	private String key;

	private CanonicalUser requestor;

	/**
	 * Empty constructor.
	 */
	protected S3ObjectRequest() {
	}

	/**
	 * Create an <code>S3Object</code> based on the request supporting virtual
	 * hosting of buckets.
	 * 
	 * @param req
	 *            The original request.
	 * @param baseHost
	 *            The <code>baseHost</code> is the HTTP Host header that is
	 *            "expected". This is used to help determine how the bucket name
	 *            will be interpreted. This is used to implement the "Virtual
	 *            Hosting of Buckets".
	 * @return An object initialized from the request.
	 * @throws IllegalArgumentException
	 *             Invalid request.
	 */
	public static S3ObjectRequest create(HttpServletRequest req, String baseHost)
			throws IllegalArgumentException {
		S3ObjectRequest o = new S3ObjectRequest();
		String pathInfo = req.getPathInfo();
		int pathInfoLength;
		String requestURL;
		String serviceEndpoint;
		String bucket = null;
		String key = null;
		String host;

		baseHost = baseHost.toLowerCase();

		host = req.getHeader("Host");
		if (host != null) {
			host = host.toLowerCase();
		}

		try {
			requestURL = URLDecoder.decode(req.getRequestURL().toString(),
					"UTF-8");
		} catch (UnsupportedEncodingException e) {
			// should never happen
			e.printStackTrace();
			IllegalArgumentException t = new IllegalArgumentException(
					"Unsupport encoding: UTF-8");
			t.initCause(e);
			throw t;
		}

		if (!requestURL.endsWith(pathInfo)) {
			String m = "requestURL [" + requestURL
					+ "] does not end with pathInfo [" + pathInfo + "]";
			throw new IllegalArgumentException(m);
		}

		pathInfoLength = pathInfo.length();

		serviceEndpoint = requestURL.substring(0, requestURL.length()
				- pathInfoLength);

		if ((host == null) || // http 1.0 form
				(host.equals(baseHost))) { // ordinary method
			// http 1.0 form
			// bucket first part of path info
			// key second part of path info
			if (pathInfoLength > 1) {
				int index = pathInfo.indexOf('/', 1);
				if (index > -1) {
					bucket = pathInfo.substring(1, index);

					if (pathInfoLength > (index + 1)) {
						key = pathInfo.substring(index + 1);
					}
				} else {
					bucket = pathInfo.substring(1);
				}
			}
		} else if (host.endsWith("." + baseHost)) {
			// bucket prefix of host
			// key is path info
			bucket = host.substring(0, host.length() - 1 - baseHost.length());
			if (pathInfoLength > 1) {
				key = pathInfo.substring(1);
			}
		} else {
			// bucket is host
			// key is path info
			bucket = host;
			if (pathInfoLength > 1) {
				key = pathInfo.substring(1);
			}
		}

		o.setServiceEndpoint(serviceEndpoint);
		o.setBucket(bucket);
		o.setKey(key);
		o.setRequestor(requestor(req));

		return o;
	}

	/**
	 * The service endpoint. In a URL of "http://localhost/bucket/key", would
	 * return "http://localhost".
	 * 
	 * @return The service endpoint.
	 */
	public String getServiceEndpoint() {
		return serviceEndpoint;
	}

	/**
	 * Set the service endpoint.
	 * 
	 * @param serviceEndpoint
	 *            The service endpoint.
	 */
	public void setServiceEndpoint(String serviceEndpoint) {
		this.serviceEndpoint = serviceEndpoint;
	}

	/**
	 * The bucket. In a URL of "http://localhost/bucket/key", would return
	 * "bucket".
	 * 
	 * @return The bucket. May be <code>null</code> if no bucket specified.
	 */
	public String getBucket() {
		return bucket;
	}

	/**
	 * Set the bucket.
	 * 
	 * @param bucket
	 *            The bucket.
	 */
	public void setBucket(String bucket) {
		this.bucket = bucket;
	}

	/**
	 * The key. In a URL of "http://localhost/bucket/key", would return "key".
	 * 
	 * @return The key. May be <code>null</code> if no key specified.
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Set the key.
	 * 
	 * @param key
	 *            The key.
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * Get the principal who made the request.
	 * 
	 * @return The principal who made the request.
	 */
	public CanonicalUser getRequestor() {
		return requestor;
	}

	/**
	 * Set the principal who made the request.
	 * 
	 * @param requestor
	 *            The principal who made the request.
	 */
	public void setRequestor(CanonicalUser requestor) {
		this.requestor = requestor;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();

		buffer.append("serviceEndpoint:[").append(serviceEndpoint);
		buffer.append("], bucket:[").append(bucket);
		buffer.append("], key:[").append(key);
		buffer.append("], requestor:[").append(requestor).append("]");

		return buffer.toString();
	}

	/**
	 * Determine the principal making the request.
	 * 
	 * @param req
	 *            an HttpServletRequest object that contains the request the
	 *            client has made of the servlet
	 * @return The principal making the request. Will be a
	 *         <code>CanonicalUser</code> with an id of the user principal
	 *         name if the request is authenticated or an "anonymous"
	 *         <code>Canonicaluser</code> is the request is non authenticated.
	 */
	public static CanonicalUser requestor(HttpServletRequest req) {
		Principal authenticatedUser = req.getUserPrincipal();
		// TODO: remove hack
		boolean hack = true;

		if (hack) {
			String username = req.getHeader("x-hack-user");
			if (username != null) {
				System.out.println("HACK! USING USERNAME FROM HEADER: "
						+ username);
				return new CanonicalUser(username);
			}
		}

		if (authenticatedUser == null) {
			return new CanonicalUser(CanonicalUser.ID_ANONYMOUS);
		} else {
			return new CanonicalUser(authenticatedUser.getName());
		}
	}
}
