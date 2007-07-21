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

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class S3ObjectRequestTest extends MockObjectTestCase {
	private Log logger;

	/**
	 * Create the test case
	 * 
	 * @param testName
	 *            name of the test case
	 */
	public S3ObjectRequestTest(String testName) {
		super(testName);

		logger = LogFactory.getLog(this.getClass());
		logger.debug("S3ObjectTest");
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(S3ObjectRequestTest.class);
	}

	/**
	 * Test getting/setting the serviceEndpoint.
	 */
	public void test_serviceEndpoint() {
		S3ObjectRequest o = new S3ObjectRequest();

		assertEquals("Unexpected serviceEndpoint", null, o.getServiceEndpoint());
		o.setServiceEndpoint("http://localhost");
		assertEquals("Unexpected serviceEndpoint", "http://localhost", o
				.getServiceEndpoint());
	}

	/**
	 * Test getting/setting the bucket.
	 */
	public void test_bucket() {
		S3ObjectRequest o = new S3ObjectRequest();

		assertEquals("Unexpected bucket", null, o.getBucket());
		o.setBucket("testBucket");
		assertEquals("Unexpected bucket", "testBucket", o.getBucket());
	}

	/**
	 * Test getting/setting the key.
	 */
	public void test_key() {
		S3ObjectRequest o = new S3ObjectRequest();

		assertEquals("Unexpected key", null, o.getKey());
		o.setKey("testKey");
		assertEquals("Unexpected key", "testKey", o.getKey());
	}

	/**
	 * Test a basic <code>create</code>.
	 */
	public void test_create() {
		S3ObjectRequest o;
		Mock mockHttServletRequest = mock(HttpServletRequest.class);

		mockHttServletRequest.expects(once()).method("getPathInfo").will(
				returnValue("/myBucket/myKey.txt"));
		mockHttServletRequest.expects(once()).method("getHeader").with(
				eq("Host")).will(returnValue("localhost"));
		mockHttServletRequest.expects(once()).method("getRequestURL").will(
				returnValue(new StringBuffer(
						"http://localhost/context/myBucket/myKey.txt")));

		o = S3ObjectRequest.create((HttpServletRequest) mockHttServletRequest
				.proxy(), "localhost");

		assertEquals("Unexpected serviceEndpoint", "http://localhost/context",
				o.getServiceEndpoint());
		assertEquals("Unexpected bucket", "myBucket", o.getBucket());
		assertEquals("Unexpected key", "myKey.txt", o.getKey());
	}

	/**
	 * Test a basic <code>create</code> but with a space in the key.
	 */
	public void test_createWithSpace() {
		S3ObjectRequest o;
		Mock mockHttServletRequest = mock(HttpServletRequest.class);

		mockHttServletRequest.expects(once()).method("getPathInfo").will(
				returnValue("/myBucket/my Key.txt"));
		mockHttServletRequest.expects(once()).method("getHeader").with(
				eq("Host")).will(returnValue("localhost"));
		mockHttServletRequest.expects(once()).method("getRequestURL").will(
				returnValue(new StringBuffer(
						"http://localhost/context/myBucket/my%20Key.txt")));

		o = S3ObjectRequest.create((HttpServletRequest) mockHttServletRequest
				.proxy(), "localhost");

		assertEquals("Unexpected serviceEndpoint", "http://localhost/context",
				o.getServiceEndpoint());
		assertEquals("Unexpected bucket", "myBucket", o.getBucket());
		assertEquals("Unexpected key", "my Key.txt", o.getKey());
	}

	/**
	 * Test a <code>create</code> with no key but with a slash character after
	 * the bucket.
	 */
	public void test_createNoKeyBucketEndsWithSlash() {
		S3ObjectRequest o;
		Mock mockHttServletRequest = mock(HttpServletRequest.class);

		mockHttServletRequest.expects(once()).method("getPathInfo").will(
				returnValue("/myBucket/"));
		mockHttServletRequest.expects(once()).method("getHeader").with(
				eq("Host")).will(returnValue("localhost"));
		mockHttServletRequest.expects(once()).method("getRequestURL").will(
				returnValue(new StringBuffer(
						"http://localhost/context/myBucket/")));

		o = S3ObjectRequest.create((HttpServletRequest) mockHttServletRequest
				.proxy(), "localhost");

		assertEquals("Unexpected serviceEndpoint", "http://localhost/context",
				o.getServiceEndpoint());
		assertEquals("Unexpected bucket", "myBucket", o.getBucket());
		assertNull("Unexpected key", o.getKey());
	}

	/**
	 * Test a <code>create</code> using virtual hosting of buckets. Ordinary
	 * method.
	 */
	public void test_virtualHostingOrdinaryMethod() {
		S3ObjectRequest o;
		Mock mockHttServletRequest = mock(HttpServletRequest.class);

		mockHttServletRequest.expects(once()).method("getPathInfo").will(
				returnValue("/johnsmith/homepage.html"));
		mockHttServletRequest.expects(once()).method("getRequestURL").will(
				returnValue(new StringBuffer(
						"http://s3.amazonaws.com/johnsmith/homepage.html")));
		mockHttServletRequest.expects(once()).method("getHeader").with(
				eq("Host")).will(returnValue("s3.amazonaws.com"));

		o = S3ObjectRequest.create((HttpServletRequest) mockHttServletRequest
				.proxy(), "s3.amazonaws.com");

		assertEquals("Unexpected serviceEndpoint", "http://s3.amazonaws.com", o
				.getServiceEndpoint());
		assertEquals("Unexpected bucket", "johnsmith", o.getBucket());
		assertEquals("Unexpected key", "homepage.html", o.getKey());
	}

	/**
	 * Test a <code>create</code> using virtual hosting of buckets. HTTP 1.0,
	 * contains no Host header.
	 */
	public void test_virtualHostingHTTP10() {
		S3ObjectRequest o;
		Mock mockHttServletRequest = mock(HttpServletRequest.class);

		mockHttServletRequest.expects(once()).method("getPathInfo").will(
				returnValue("/johnsmith/homepage.html"));
		mockHttServletRequest.expects(once()).method("getRequestURL").will(
				returnValue(new StringBuffer(
						"http://s3.amazonaws.com/johnsmith/homepage.html")));
		mockHttServletRequest.expects(once()).method("getHeader").with(
				eq("Host")).will(returnValue(null));

		o = S3ObjectRequest.create((HttpServletRequest) mockHttServletRequest
				.proxy(), "s3.amazonaws.com");

		assertEquals("Unexpected serviceEndpoint", "http://s3.amazonaws.com", o
				.getServiceEndpoint());
		assertEquals("Unexpected bucket", "johnsmith", o.getBucket());
		assertEquals("Unexpected key", "homepage.html", o.getKey());
	}

	/**
	 * Test a <code>create</code> using virtual hosting of buckets. Sub-domain
	 * method.
	 */
	public void test_virtualHostingSubDomain() {
		S3ObjectRequest o;
		Mock mockHttServletRequest = mock(HttpServletRequest.class);

		mockHttServletRequest.expects(once()).method("getPathInfo").will(
				returnValue("/homepage.html"));
		mockHttServletRequest.expects(once()).method("getRequestURL").will(
				returnValue(new StringBuffer(
						"http://johnsmith.s3.amazonaws.com/homepage.html")));
		mockHttServletRequest.expects(once()).method("getHeader").with(
				eq("Host")).will(returnValue("johnsmith.s3.amazonaws.com"));

		o = S3ObjectRequest.create((HttpServletRequest) mockHttServletRequest
				.proxy(), "s3.amazonaws.com");

		assertEquals("Unexpected serviceEndpoint",
				"http://johnsmith.s3.amazonaws.com", o.getServiceEndpoint());
		assertEquals("Unexpected bucket", "johnsmith", o.getBucket());
		assertEquals("Unexpected key", "homepage.html", o.getKey());
	}

	/**
	 * Test a <code>create</code> using virtual hosting of buckets. Sub-domain
	 * method with upper case Host header.
	 */
	public void test_virtualHostingSubDomainUpperCase() {
		S3ObjectRequest o;
		Mock mockHttServletRequest = mock(HttpServletRequest.class);

		mockHttServletRequest.expects(once()).method("getPathInfo").will(
				returnValue("/homepage.html"));
		mockHttServletRequest.expects(once()).method("getRequestURL").will(
				returnValue(new StringBuffer(
						"http://johnsmith.s3.amazonaws.com/homepage.html")));
		mockHttServletRequest.expects(once()).method("getHeader").with(
				eq("Host")).will(returnValue("JohnSmith.s3.amazonaws.com"));

		o = S3ObjectRequest.create((HttpServletRequest) mockHttServletRequest
				.proxy(), "s3.amazonaws.com");

		assertEquals("Unexpected serviceEndpoint",
				"http://johnsmith.s3.amazonaws.com", o.getServiceEndpoint());
		assertEquals("Unexpected bucket", "johnsmith", o.getBucket());
		assertEquals("Unexpected key", "homepage.html", o.getKey());
	}

	/**
	 * Test a <code>create</code> using virtual hosting of buckets. Domain is
	 * the bucket.
	 */
	public void test_virtualHostingDomain() {
		S3ObjectRequest o;
		Mock mockHttServletRequest = mock(HttpServletRequest.class);

		mockHttServletRequest.expects(once()).method("getPathInfo").will(
				returnValue("/homepage.html"));
		mockHttServletRequest.expects(once()).method("getRequestURL").will(
				returnValue(new StringBuffer(
						"http://www.johnsmith.net/homepage.html")));
		mockHttServletRequest.expects(once()).method("getHeader").with(
				eq("Host")).will(returnValue("www.johnsmith.net"));

		o = S3ObjectRequest.create((HttpServletRequest) mockHttServletRequest
				.proxy(), "s3.amazonaws.com");

		assertEquals("Unexpected serviceEndpoint", "http://www.johnsmith.net",
				o.getServiceEndpoint());
		assertEquals("Unexpected bucket", "www.johnsmith.net", o.getBucket());
		assertEquals("Unexpected key", "homepage.html", o.getKey());
	}

	/**
	 * Test a <code>create</code> with no key but and no slash character after
	 * the bucket.
	 */
	public void test_createNoKeyBucketNoSlash() {
		S3ObjectRequest o;
		Mock mockHttServletRequest = mock(HttpServletRequest.class);

		mockHttServletRequest.expects(once()).method("getPathInfo").will(
				returnValue("/myBucket"));
		mockHttServletRequest.expects(once()).method("getHeader").with(
				eq("Host")).will(returnValue("localhost"));
		mockHttServletRequest.expects(once()).method("getRequestURL").will(
				returnValue(new StringBuffer(
						"http://localhost/context/myBucket")));

		o = S3ObjectRequest.create((HttpServletRequest) mockHttServletRequest
				.proxy(), "localhost");

		assertEquals("Unexpected serviceEndpoint", "http://localhost/context",
				o.getServiceEndpoint());
		assertEquals("Unexpected bucket", "myBucket", o.getBucket());
		assertNull("Unexpected key", o.getKey());
	}

	/**
	 * Test a <code>create</code> with no bucket.
	 */
	public void test_createNoBucket() {
		S3ObjectRequest o;
		Mock mockHttServletRequest = mock(HttpServletRequest.class);

		mockHttServletRequest.expects(once()).method("getPathInfo").will(
				returnValue("/"));
		mockHttServletRequest.expects(once()).method("getHeader").with(
				eq("Host")).will(returnValue("localhost"));
		mockHttServletRequest.expects(once()).method("getRequestURL").will(
				returnValue(new StringBuffer("http://localhost/context/")));

		o = S3ObjectRequest.create((HttpServletRequest) mockHttServletRequest
				.proxy(), "localhost");

		assertEquals("Unexpected serviceEndpoint", "http://localhost/context",
				o.getServiceEndpoint());
		assertNull("Unexpected bucket", o.getBucket());
		assertNull("Unexpected key", o.getKey());
	}

	/**
	 * Test a <code>create</code> with an invalid request.
	 */
	public void test_createIllegalRequest() {
		Mock mockHttServletRequest = mock(HttpServletRequest.class);

		mockHttServletRequest.expects(once()).method("getPathInfo").will(
				returnValue("/foo"));
		mockHttServletRequest.expects(once()).method("getHeader").with(
				eq("Host")).will(returnValue("localhost"));
		mockHttServletRequest.expects(once()).method("getRequestURL").will(
				returnValue(new StringBuffer("http://localhost/context/bar")));

		try {
			S3ObjectRequest.create((HttpServletRequest) mockHttServletRequest
					.proxy(), "localhost");
			fail("Expected exception");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	/**
	 * Basically a utility test for creating an ISO 8601 date.
	 */
	public void test_isoDate() {
		SimpleDateFormat iso8601 = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		TimeZone utc = TimeZone.getTimeZone("UTC");
		iso8601.setTimeZone(utc);

		GregorianCalendar cal = new GregorianCalendar(2007, 6, 19, 9, 50, 33);

		assertEquals("Unexpected formatted date", "2007-07-19T14:50:33.000Z",
				iso8601.format(cal.getTime()));
	}
}
