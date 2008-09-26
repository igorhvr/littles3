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
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.httpclient.util.DateParseException;
import org.apache.commons.httpclient.util.DateUtil;

import com.jpeterson.littles3.bo.Authenticator;
import com.jpeterson.littles3.bo.AuthenticatorException;
import com.jpeterson.littles3.bo.CanonicalUser;
import com.jpeterson.littles3.bo.HackAuthenticator;
import com.jpeterson.littles3.bo.S3Authenticator;

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

	private String stringToSign;

	private Date timestamp;

	private static final String PARAMETER_ACL = "acl";

	private static boolean debug = false;

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
	 * @param authenticator
	 *            The authenticator to use to authenticate this request.
	 * @return An object initialized from the request.
	 * @throws IllegalArgumentException
	 *             Invalid request.
	 */
	@SuppressWarnings("unchecked")
	public static S3ObjectRequest create(HttpServletRequest req,
			String baseHost, Authenticator authenticator)
			throws IllegalArgumentException, AuthenticatorException {
		S3ObjectRequest o = new S3ObjectRequest();
		String pathInfo = req.getPathInfo();
		String contextPath = req.getContextPath();
		String requestURI = req.getRequestURI();
		String undecodedPathPart = null;
		int pathInfoLength;
		String requestURL;
		String serviceEndpoint;
		String bucket = null;
		String key = null;
		String host;
		String value;
		String timestamp;

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

		if (debug) {
			System.out.println("---------------");
			System.out.println("requestURI: " + requestURI);
			System.out.println("serviceEndpoint: " + serviceEndpoint);
			System.out.println("---------------");
		}

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
						undecodedPathPart = requestURI.substring(contextPath
								.length()
								+ 1 + bucket.length(), requestURI.length());
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
				undecodedPathPart = requestURI.substring(contextPath.length(),
						requestURI.length());
			}
		} else {
			// bucket is host
			// key is path info
			bucket = host;
			if (pathInfoLength > 1) {
				key = pathInfo.substring(1);
				undecodedPathPart = requestURI.substring(contextPath.length(),
						requestURI.length());
			}
		}

		// timestamp
		timestamp = req.getHeader("Date");

		// CanonicalizedResource
		StringBuffer canonicalizedResource = new StringBuffer();

		canonicalizedResource.append('/');
		if (bucket != null) {
			canonicalizedResource.append(bucket);
		}
		if (undecodedPathPart != null) {
			canonicalizedResource.append(undecodedPathPart);
		}
		if (req.getParameter(PARAMETER_ACL) != null) {
			canonicalizedResource.append("?").append(PARAMETER_ACL);
		}

		// CanonicalizedAmzHeaders
		StringBuffer canonicalizedAmzHeaders = new StringBuffer();
		Map<String, String> headers = new TreeMap<String, String>();
		String headerName;
		String headerValue;

		for (Enumeration headerNames = req.getHeaderNames(); headerNames
				.hasMoreElements();) {
			headerName = ((String) headerNames.nextElement()).toLowerCase();

			if (headerName.startsWith("x-amz-")) {
				for (Enumeration headerValues = req.getHeaders(headerName); headerValues
						.hasMoreElements();) {
					headerValue = (String) headerValues.nextElement();
					String currentValue = headers.get(headerValue);

					if (currentValue != null) {
						// combine header fields with the same name
						headers.put(headerName, currentValue + ","
								+ headerValue);
					} else {
						headers.put(headerName, headerValue);
					}

					if (headerName.equals("x-amz-date")) {
						timestamp = headerValue;
					}
				}
			}
		}

		for (Iterator<String> iter = headers.keySet().iterator(); iter
				.hasNext();) {
			headerName = iter.next();
			headerValue = headers.get(headerName);
			canonicalizedAmzHeaders.append(headerName).append(":").append(
					headerValue).append("\n");
		}

		StringBuffer stringToSign = new StringBuffer();

		stringToSign.append(req.getMethod()).append("\n");
		value = req.getHeader("Content-MD5");
		if (value != null) {
			stringToSign.append(value);
		}
		stringToSign.append("\n");
		value = req.getHeader("Content-Type");
		if (value != null) {
			stringToSign.append(value);
		}
		stringToSign.append("\n");
		value = req.getHeader("Date");
		if (value != null) {
			stringToSign.append(value);
		}
		stringToSign.append("\n");
		stringToSign.append(canonicalizedAmzHeaders);
		stringToSign.append(canonicalizedResource);

		if (debug) {
			System.out.println(":v:v:v:v:");
			System.out.println("undecodedPathPart: " + undecodedPathPart);
			System.out.println("canonicalizedAmzHeaders: "
					+ canonicalizedAmzHeaders);
			System.out.println("canonicalizedResource: "
					+ canonicalizedResource);
			System.out.println("stringToSign: " + stringToSign);
			System.out.println(":^:^:^:^:");
		}

		o.setServiceEndpoint(serviceEndpoint);
		o.setBucket(bucket);
		o.setKey(key);
		try {
			if (timestamp == null) {
				o.setTimestamp(null);
			} else {
				o.setTimestamp(DateUtil.parseDate(timestamp));
			}
		} catch (DateParseException e) {
			o.setTimestamp(null);
		}
		o.setStringToSign(stringToSign.toString());
		o.setRequestor(authenticate(req, o));

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

	/**
	 * The "String to Sign". Used in authentication.
	 * 
	 * @return The "String to Sign".
	 */
	public String getStringToSign() {
		return stringToSign;
	}

	/**
	 * Set the "String to Sign". Used in authentication.
	 * 
	 * @param stringToSign
	 *            The "String to Sign".
	 */
	public void setStringToSign(String stringToSign) {
		this.stringToSign = stringToSign;
	}

	/**
	 * The request timestamp.
	 * 
	 * @return The request timestamp.
	 */
	public Date getTimestamp() {
		return timestamp;
	}

	/**
	 * Set the request timestamp.
	 * 
	 * @param timestamp
	 *            The request timestamp
	 */
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
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
	 *         <code>CanonicalUser</code> with an id of the user principal name
	 *         if the request is authenticated or an "anonymous"
	 *         <code>Canonicaluser</code> is the request is non authenticated.
	 * @throws AuthenticatorException
	 *             Unable to authenticate the request.
	 */
	public static CanonicalUser authenticate(HttpServletRequest req,
			S3ObjectRequest o) throws AuthenticatorException {
		// TODO: remove hack
		try {
			Authenticator hackAuthenticator = new HackAuthenticator();
			return hackAuthenticator.authenticate(req, o);
		} catch (AuthenticatorException e) {
			// ignore
		}

		return new S3Authenticator().authenticate(req, o);
	}
}
