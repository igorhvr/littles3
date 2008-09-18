package com.jpeterson.littles3.bo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class S3ObjectTest extends TestCase implements Serializable {
	/**
	 * If incompatible serialization changes are made, mostly deleting methods,
	 * this must be changed.
	 */
	private static final long serialVersionUID = 1L;

	private Log logger;

	/**
	 * Create the test case
	 * 
	 * @param testName
	 *            name of the test case
	 */
	public S3ObjectTest(String testName) {
		super(testName);

		logger = LogFactory.getLog(this.getClass());
		logger.debug("S3ObjectTest");
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(S3ObjectTest.class);
	}

	/**
	 * Test the bucket property.
	 */
	public void test_bucket() {
		S3Object s3Object;

		s3Object = new MyS3Object();

		assertNull("Unexpected value", s3Object.getBucket());
		s3Object.setBucket("bucket");
		assertEquals("Unexpected value", "bucket", s3Object.getBucket());
	}

	/**
	 * Test the key property.
	 */
	public void test_key() {
		S3Object s3Object;

		s3Object = new MyS3Object();

		assertNull("Unexpected value", s3Object.getKey());
		s3Object.setKey("key");
		assertEquals("Unexpected value", "key", s3Object.getKey());
	}

	/**
	 * Test the storage URL property.
	 */
	public void test_storageUrl() {
		S3Object s3Object;
		URL url;

		s3Object = new MyS3Object();

		try {
			url = new URL("http://localhost");
		} catch (MalformedURLException e) {
			e.printStackTrace();
			fail("Unexpected exception");
			return;
		}

		assertNull("Unexpected value", s3Object.getStorageUrl());
		s3Object.setStorageUrl(url);
		assertEquals("Unexpected value", url, s3Object.getStorageUrl());
	}

	/**
	 * Test the content length property.
	 */
	public void test_contentLength() {
		S3Object s3Object;

		s3Object = new MyS3Object();

		assertEquals("Unexpected value", 0, s3Object.getContentLength());
		s3Object.setContentLength(100);
		assertEquals("Unexpected value", 100, s3Object.getContentLength());
	}

	/**
	 * Test the content type property.
	 */
	public void test_contentType() {
		S3Object s3Object;

		s3Object = new MyS3Object();

		assertEquals("Unexpected value", S3Object.DEFAULT_CONTENT_TYPE,
				s3Object.getContentType());
		s3Object.setContentType("contentType");
		assertEquals("Unexpected value", "contentType", s3Object
				.getContentType());
	}

	/**
	 * Test the content MD5 property.
	 */
	public void test_contentMD5() {
		S3Object s3Object;

		s3Object = new MyS3Object();

		assertNull("Unexpected value", s3Object.getContentMD5());
		s3Object.setContentMD5("contentMD5");
		assertEquals("Unexpected value", "contentMD5", s3Object.getContentMD5());
	}

	/**
	 * Test the content disposition property.
	 */
	public void test_contentDisposition() {
		S3Object s3Object;

		s3Object = new MyS3Object();

		assertNull("Unexpected value", s3Object.getContentDisposition());
		s3Object.setContentDisposition("contentDisposition");
		assertEquals("Unexpected value", "contentDisposition", s3Object
				.getContentDisposition());
	}

	/**
	 * Test the ETag property.
	 */
	public void test_contentETag() {
		S3Object s3Object;

		s3Object = new MyS3Object();

		assertNull("Unexpected value", s3Object.getETag());
		s3Object.setETag("ETag");
		assertEquals("Unexpected value", "ETag", s3Object.getETag());
	}

	/**
	 * Test the last modified property.
	 */
	public void test_lastModified() {
		S3Object s3Object;

		s3Object = new MyS3Object();

		assertEquals("Unexpected value", 0, s3Object.getLastModified());
		s3Object.setLastModified(100);
		assertEquals("Unexpected value", 100, s3Object.getLastModified());
	}

	/**
	 * Test the metadata.
	 */
	public void test_metadata() {
		S3Object s3Object;
		Iterator<String> iter;

		s3Object = new MyS3Object();

		assertNull("Unexpected value", s3Object.getMetadataValue("foo"));

		iter = s3Object.getMetadataValues("foo");
		assertFalse("Unexpected value", iter.hasNext());

		s3Object.addMetadata("foo", "value");

		assertEquals("Unexpected value", "value", s3Object
				.getMetadataValue("foo"));

		iter = s3Object.getMetadataValues("foo");

		assertTrue("Unexpected value", iter.hasNext());
		assertEquals("Unexpected value", "value", iter.next());
		assertFalse("Unexpected value", iter.hasNext());

		s3Object.addMetadata("bar", "baz");

		assertEquals("Unexpected value", "baz", s3Object
				.getMetadataValue("bar"));

		s3Object.addMetadata("foo", "value2");

		assertEquals("Unexpected value", "value", s3Object
				.getMetadataValue("foo"));

		iter = s3Object.getMetadataValues("foo");

		assertTrue("Unexpected value", iter.hasNext());
		assertEquals("Unexpected value", "value", iter.next());
		assertTrue("Unexpected value", iter.hasNext());
		assertEquals("Unexpected value", "value2", iter.next());
		assertFalse("Unexpected value", iter.hasNext());

		iter = s3Object.getMetadataNames();

		assertTrue("Unexpected value", iter.hasNext());
		String name = iter.next();
		if ("foo".equals(name)) {
			assertTrue("Unexpected value", iter.hasNext());
			assertEquals("Unexpected value", "bar", iter.next());
		} else {
			assertEquals("Unexpected value", "bar", name);
			assertTrue("Unexpected value", iter.hasNext());
			assertEquals("Unexpected value", "foo", iter.next());
		}
		assertFalse("Unexpected value", iter.hasNext());
	}

	/**
	 * Test that an instance is serializable.
	 */
	public void test_serialization() {
		S3Object s3Object, reconstitutedS3Object;
		ByteArrayInputStream bais;
		ByteArrayOutputStream baos;
		ObjectInputStream ois;
		ObjectOutputStream oos;

		s3Object = new MyS3Object();

		s3Object.setBucket("bucket");

		try {
			baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);

			oos.writeObject(s3Object);

			bais = new ByteArrayInputStream(baos.toByteArray());
			ois = new ObjectInputStream(bais);

			reconstitutedS3Object = (S3Object) ois.readObject();

			assertEquals("Unexpected value", "bucket", reconstitutedS3Object
					.getBucket());
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unexpected exception");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			fail("Unexpected exception");
		}
	}

	/**
	 * Test implementation of the abstract S3Object.
	 * 
	 * @author Jesse Peterson
	 */
	private class MyS3Object extends S3Object {
		/**
		 * If incompatible serialization changes are made, mostly deleting
		 * methods, this must be changed.
		 */
		private static final long serialVersionUID = 1L;

		public MyS3Object() {
			super();
		}

		@Override
		public boolean deleteData() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public InputStream getInputStream() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public OutputStream getOutputStream() {
			// TODO Auto-generated method stub
			return null;
		}

	}
}
