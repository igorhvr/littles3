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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class FileS3ObjectTest extends TestCase {
	private Log logger;

	/**
	 * Create the test case
	 * 
	 * @param testName
	 *            name of the test case
	 */
	public FileS3ObjectTest(String testName) {
		super(testName);

		logger = LogFactory.getLog(this.getClass());
		logger.debug("FileS3ObjectTest");
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(FileS3ObjectTest.class);
	}

	/**
	 * Test the constructor.
	 */
	public void test_constructor() {
		FileS3Object s3Object;
		URL storageUrl;

		try {
			storageUrl = new URL("file:///C:/temp/foo.txt");
		} catch (MalformedURLException e) {
			e.printStackTrace();
			fail("Unexpected exception");
			return;
		}
		s3Object = new FileS3Object("bucket", "key", storageUrl);

		assertEquals("Unexpected value", "bucket", s3Object.getBucket());
		assertEquals("Unexpected value", "key", s3Object.getKey());
		assertEquals("Unexpected value", storageUrl, s3Object.getStorageUrl());
	}

	/**
	 * Test the content length property.
	 */
	public void test_contentLength() {
		File file;
		FileS3Object s3Object;

		try {
			file = File.createTempFile("unitTest", null);

			file.deleteOnExit();

			FileOutputStream out = new FileOutputStream(file);

			out.write("12345".getBytes());

			out.close();

			s3Object = new FileS3Object("bucket", "key", file.toURL());

			assertEquals("Unexpected value", 5, s3Object.getContentLength());
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unexpected exception");
			return;
		}
	}

	/**
	 * Test the last modified property.
	 */
	public void test_lastModified() {
		File file;
		FileS3Object s3Object;

		try {
			file = File.createTempFile("unitTest", null);

			file.deleteOnExit();

			FileOutputStream out = new FileOutputStream(file);

			out.write("12345".getBytes());

			out.close();

			s3Object = new FileS3Object("bucket", "key", file.toURL());

			assertEquals("Unexpected value", file.lastModified(), s3Object
					.getLastModified());
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unexpected exception");
			return;
		}
	}
}
