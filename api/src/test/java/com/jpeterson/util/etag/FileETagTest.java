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

package com.jpeterson.util.etag;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FileETagTest extends TestCase {
	private Log logger;

	/**
	 * Create the test case
	 * 
	 * @param testName
	 *            name of the test case
	 */
	public FileETagTest(String testName) {
		super(testName);

		logger = LogFactory.getLog(this.getClass());
		logger.debug("FileETagTest");
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(FileETagTest.class);
	}

	/**
	 * Test flags property access methods.
	 */
	public void test_flags() {
		FileETag etag;
		int flags = FileETag.FLAG_CONTENT | FileETag.FLAG_MTIME
				| FileETag.FLAG_SIZE;

		etag = new FileETag();

		assertEquals("Unexpected value", FileETag.DEFAULT_FLAGS, etag
				.getFlags());
		etag.setFlags(flags);
		assertEquals("Unexpected value", flags, etag.getFlags());
	}

	/**
	 * Test the calculate method when using file last modified, file size, and
	 * file content values.
	 */
	public void test_calculateLastModifiedSizeContent() {
		File file;
		String content = "Hello, world!";
		String expected;
		FileETag etag;

		try {
			// create temporary file
			file = File.createTempFile("temp", "txt");

			// make sure that it gets cleaned up
			file.deleteOnExit();

			// put some date in the file
			FileOutputStream out = new FileOutputStream(file);
			out.write(content.getBytes());
			out.flush();
			out.close();

			// manipulate the last modified value to a "known" value
			SimpleDateFormat date = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
			long lastModified = date.parse("06/21/2007 11:19:36").getTime();
			file.setLastModified(lastModified);

			// determined expected
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			messageDigest.update(content.getBytes());
			StringBuffer buffer = new StringBuffer();
			buffer.append(lastModified);
			buffer.append(content.length());
			expected = new String(Hex.encodeHex(messageDigest.digest(buffer
					.toString().getBytes())));

			etag = new FileETag();
			etag.setFlags(FileETag.FLAG_CONTENT | FileETag.FLAG_MTIME
					| FileETag.FLAG_SIZE);
			String value = etag.calculate(file);

			assertEquals("Unexpected value", expected, value);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail("Unexpected exception");
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unexpected exception");
		} catch (ParseException e) {
			e.printStackTrace();
			fail("Unexpected exception");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			fail("Unexpected exception");
		}
	}

	/**
	 * Test the calculate method when using both file last modified and file
	 * size values.
	 */
	public void test_calculateLastModifiedAndSize() {
		File file;
		String content = "Hello, world!";
		String expected;
		FileETag etag;

		try {
			// create temporary file
			file = File.createTempFile("temp", "txt");

			// make sure that it gets cleaned up
			file.deleteOnExit();

			// put some date in the file
			FileOutputStream out = new FileOutputStream(file);
			out.write(content.getBytes());
			out.flush();
			out.close();

			// manipulate the last modified value to a "known" value
			SimpleDateFormat date = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
			long lastModified = date.parse("06/21/2007 11:19:36").getTime();
			file.setLastModified(lastModified);

			// determined expected
			StringBuffer buffer = new StringBuffer();
			buffer.append(lastModified);
			buffer.append(content.length());
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			expected = new String(Hex.encodeHex(messageDigest.digest(buffer
					.toString().getBytes())));

			etag = new FileETag();
			etag.setFlags(FileETag.FLAG_MTIME | FileETag.FLAG_SIZE);
			String value = etag.calculate(file);

			assertEquals("Unexpected value", expected, value);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail("Unexpected exception");
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unexpected exception");
		} catch (ParseException e) {
			e.printStackTrace();
			fail("Unexpected exception");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			fail("Unexpected exception");
		}
	}

	/**
	 * Test the calculate method using the default characteristics: file last
	 * modified and file size.
	 */
	public void test_calculateDefaultLastModifiedAndSize() {
		File file;
		String content = "Hello, world!";
		String expected;
		FileETag etag;

		try {
			// create temporary file
			file = File.createTempFile("temp", "txt");

			// make sure that it gets cleaned up
			file.deleteOnExit();

			// put some date in the file
			FileOutputStream out = new FileOutputStream(file);
			out.write(content.getBytes());
			out.flush();
			out.close();

			// manipulate the last modified value to a "known" value
			SimpleDateFormat date = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
			long lastModified = date.parse("06/21/2007 11:19:36").getTime();
			file.setLastModified(lastModified);

			// determined expected
			StringBuffer buffer = new StringBuffer();
			buffer.append(lastModified);
			buffer.append(content.length());
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			expected = new String(Hex.encodeHex(messageDigest.digest(buffer
					.toString().getBytes())));

			etag = new FileETag();
			String value = etag.calculate(file);

			assertEquals("Unexpected value", expected, value);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail("Unexpected exception");
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unexpected exception");
		} catch (ParseException e) {
			e.printStackTrace();
			fail("Unexpected exception");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			fail("Unexpected exception");
		}
	}

	/**
	 * Test the calculate method when using file last modified.
	 */
	public void test_calculateLastModified() {
		File file;
		String content = "Hello, world!";
		String expected;
		FileETag etag;

		try {
			// create temporary file
			file = File.createTempFile("temp", "txt");

			// make sure that it gets cleaned up
			file.deleteOnExit();

			// put some date in the file
			FileOutputStream out = new FileOutputStream(file);
			out.write(content.getBytes());
			out.flush();
			out.close();

			// manipulate the last modified value to a "known" value
			SimpleDateFormat date = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
			long lastModified = date.parse("06/21/2007 11:19:36").getTime();
			file.setLastModified(lastModified);

			// determined expected
			StringBuffer buffer = new StringBuffer();
			buffer.append(lastModified);
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			expected = new String(Hex.encodeHex(messageDigest.digest(buffer
					.toString().getBytes())));

			etag = new FileETag();
			etag.setFlags(FileETag.FLAG_MTIME);
			String value = etag.calculate(file);

			assertEquals("Unexpected value", expected, value);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail("Unexpected exception");
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unexpected exception");
		} catch (ParseException e) {
			e.printStackTrace();
			fail("Unexpected exception");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			fail("Unexpected exception");
		}
	}

	/**
	 * Test the calculate method when using file size.
	 */
	public void test_calculateSize() {
		File file;
		String content = "Hello, world!";
		String expected;
		FileETag etag;

		try {
			// create temporary file
			file = File.createTempFile("temp", "txt");

			// make sure that it gets cleaned up
			file.deleteOnExit();

			// put some date in the file
			FileOutputStream out = new FileOutputStream(file);
			out.write(content.getBytes());
			out.flush();
			out.close();

			// manipulate the last modified value to a "known" value
			SimpleDateFormat date = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
			long lastModified = date.parse("06/21/2007 11:19:36").getTime();
			file.setLastModified(lastModified);

			// determined expected
			StringBuffer buffer = new StringBuffer();
			buffer.append(content.length());
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			expected = new String(Hex.encodeHex(messageDigest.digest(buffer
					.toString().getBytes())));

			etag = new FileETag();
			etag.setFlags(FileETag.FLAG_SIZE);
			String value = etag.calculate(file);

			assertEquals("Unexpected value", expected, value);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail("Unexpected exception");
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unexpected exception");
		} catch (ParseException e) {
			e.printStackTrace();
			fail("Unexpected exception");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			fail("Unexpected exception");
		}
	}

	/**
	 * Test the calculate method when using file content.
	 */
	public void test_calculateContent() {
		File file;
		String content = "Hello, world!";
		String expected;
		FileETag etag;

		try {
			// create temporary file
			file = File.createTempFile("temp", "txt");

			// make sure that it gets cleaned up
			file.deleteOnExit();

			// put some date in the file
			FileOutputStream out = new FileOutputStream(file);
			out.write(content.getBytes());
			out.flush();
			out.close();

			// manipulate the last modified value to a "known" value
			SimpleDateFormat date = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
			long lastModified = date.parse("06/21/2007 11:19:36").getTime();
			file.setLastModified(lastModified);

			// determined expected
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			expected = new String(Hex.encodeHex(messageDigest.digest(content
					.getBytes())));

			etag = new FileETag();
			etag.setFlags(FileETag.FLAG_CONTENT);
			String value = etag.calculate(file);

			assertEquals("Unexpected value", expected, value);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail("Unexpected exception");
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unexpected exception");
		} catch (ParseException e) {
			e.printStackTrace();
			fail("Unexpected exception");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			fail("Unexpected exception");
		}
	}

	/**
	 * Test the calculate method when using no flags.
	 */
	public void test_calculateNoFlags() {
		File file;
		String content = "Hello, world!";
		FileETag etag;

		try {
			// create temporary file
			file = File.createTempFile("temp", "txt");

			// make sure that it gets cleaned up
			file.deleteOnExit();

			// put some date in the file
			FileOutputStream out = new FileOutputStream(file);
			out.write(content.getBytes());
			out.flush();
			out.close();

			// manipulate the last modified value to a "known" value
			SimpleDateFormat date = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
			long lastModified = date.parse("06/21/2007 11:19:36").getTime();
			file.setLastModified(lastModified);

			etag = new FileETag();
			etag.setFlags(0);
			String value = etag.calculate(file);

			assertNotNull("Unexpected value", value);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail("Unexpected exception");
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unexpected exception");
		} catch (ParseException e) {
			e.printStackTrace();
			fail("Unexpected exception");
		}
	}
}
