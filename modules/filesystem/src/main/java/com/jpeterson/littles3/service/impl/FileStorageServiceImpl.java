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

package com.jpeterson.littles3.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;

import com.javaexchange.RandomGUID;
import com.jpeterson.littles3.bo.Acp;
import com.jpeterson.littles3.bo.Bucket;
import com.jpeterson.littles3.bo.CanonicalUser;
import com.jpeterson.littles3.bo.FileS3Object;
import com.jpeterson.littles3.bo.ResourcePermission;
import com.jpeterson.littles3.bo.S3Object;
import com.jpeterson.littles3.dao.BucketDao;
import com.jpeterson.littles3.dao.S3ObjectDao;
import com.jpeterson.littles3.service.BucketAlreadyExistsException;
import com.jpeterson.littles3.service.BucketNotEmptyException;
import com.jpeterson.littles3.service.StorageService;

public class FileStorageServiceImpl implements StorageService {
	public static final String CONFIG_STORAGE_LOCATION = "storageLocation";

	public static final String CONFIG_DIRECTORY_BUCKETS = "dir.buckets";

	private static final String DIRECTORY_BUCKETS = "buckets";

	private Configuration configuration;

	private Log logger;

	private static final String fileSeparator = System
			.getProperty("file.separator");

	private BucketDao bucketDao;

	private S3ObjectDao s3ObjectDao;

	public FileStorageServiceImpl() {
		logger = LogFactory.getLog(this.getClass());
	}

	public S3Object createS3Object(Bucket bucket, String key,
			CanonicalUser owner) throws IOException {
		String guid;
		File storageFile;
		Acp acp;
		S3Object s3Object;

		logger.debug("Creating S3Object for bucket[" + bucket.getName()
				+ "] + key[" + key + "]");

		String bucketPath = generateBucketPath(bucket.getName()).toString();
		File bucketDirectory = new File(bucketPath);
		if (!bucketDirectory.exists()) {
			throw new IOException("Bucket doesn't exist");
		}

		// makes sure that the file URL doesn't exist yet
		do {
			guid = new RandomGUID().valueAfterMD5;

			storageFile = new File(bucketPath + guid.substring(0, 2), guid);
		} while (storageFile.exists());

		acp = new Acp();
		acp.setOwner(owner);

		s3Object = new FileS3Object(bucket.getName(), key, storageFile.toURI()
				.toURL());
		s3Object.setAcp(acp);
		return s3Object;
	}

	public S3Object load(String bucket, String key) throws DataAccessException {
		S3Object object = s3ObjectDao.loadS3Object(bucket, key);

		return object;
	}

	public void store(S3Object s3Object) throws DataAccessException {
		Acp acp;

		acp = s3Object.getAcp();
		if (acp.size() == 0) {
			// add a default grant
			acp.grant(acp.getOwner(), ResourcePermission.ACTION_FULL_CONTROL);
		}

		s3ObjectDao.storeS3Object(s3Object);
	}

	public void remove(S3Object s3Object) throws DataAccessException {
		s3ObjectDao.removeS3Object(s3Object);
		s3Object.deleteData();
	}

	public Bucket createBucket(String name, CanonicalUser owner)
			throws IOException {
		File bucketDirectory;
		Acp acp;
		Bucket bucket;

		bucketDirectory = new File(generateBucketPath(name).toString());

		if (bucketDirectory.exists()) {
			throw new BucketAlreadyExistsException("Bucket exists");
		}

		if (!bucketDirectory.mkdir()) {
			throw new IOException("Could not create bucket");
		}

		acp = new Acp();
		acp.setOwner(owner);

		bucket = new Bucket();
		bucket.setAcp(acp);
		bucket.setName(name);
		bucket.setCreated(new Date());

		return bucket;
	}

	public Bucket loadBucket(String name) throws DataAccessException {
		return bucketDao.loadBucket(name);
	}

	public void storeBucket(Bucket bucket) throws DataAccessException {
		Acp acp;

		acp = bucket.getAcp();
		if (acp.size() == 0) {
			// add a default grant
			acp.grant(acp.getOwner(), ResourcePermission.ACTION_FULL_CONTROL);
		}

		bucketDao.storeBucket(bucket);
	}

	public void deleteBucket(Bucket bucket) throws IOException {
		File bucketDirectory;

		logger.debug("Request to delete bucket: " + bucket.getName());

		bucketDirectory = new File(generateBucketPath(bucket.getName())
				.toString());

		if (bucketDirectory.exists()) {
			String[] files = bucketDirectory.list();

			if (files.length > 0) {
				logger.debug("Bucket not empty. Number of files in directory: "
						+ files.length);
				throw new BucketNotEmptyException("Bucket is not empty");
			}

			if (!bucketDirectory.delete()) {
				throw new IOException("Could not delete bucket");
			}
		}

		bucketDao.removeBucket(bucket);
	}

	public List<Bucket> findBuckets(String username) throws IOException {
		StringBuffer buffer = new StringBuffer();
		Configuration configuration = getConfiguration();
		String storageLocation = configuration
				.getString(CONFIG_STORAGE_LOCATION);
		String bucketDirectory = configuration.getString(
				CONFIG_DIRECTORY_BUCKETS, DIRECTORY_BUCKETS);

		logger.debug("Finding buckets for user " + username);

		buffer.append(storageLocation);

		if (!storageLocation.endsWith(fileSeparator)) {
			buffer.append(fileSeparator);
		}

		buffer.append(bucketDirectory);

		if (!bucketDirectory.endsWith(fileSeparator)) {
			buffer.append(fileSeparator);
		}

		File dir = new File(buffer.toString());

		if (!dir.isDirectory()) {
			return new ArrayList<Bucket>();
		}

		// TODO: apply username filter

		File[] bucketFiles = dir.listFiles();
		ArrayList<Bucket> buckets = new ArrayList<Bucket>();

		for (int i = 0; i < bucketFiles.length; i++) {
			Bucket bucket = new Bucket();
			bucket.setName(bucketFiles[i].getName());
			// TODO: not really creation date, need to add to database
			bucket.setCreated(new Date(bucketFiles[i].lastModified()));
			buckets.add(bucket);
		}

		return buckets;
	}

	public String listKeys(Bucket bucket, String prefix, String marker,
			String delimiter, int maxKeys) throws DataAccessException {
		return s3ObjectDao.listKeys(bucket.getName(), prefix, marker,
				delimiter, maxKeys);
	}

	public BucketDao getBucketDao() {
		return bucketDao;
	}

	public void setBucketDao(BucketDao bucketDao) {
		this.bucketDao = bucketDao;
	}

	public S3ObjectDao getS3ObjectDao() {
		return s3ObjectDao;
	}

	public void setS3ObjectDao(S3ObjectDao s3ObjectDao) {
		this.s3ObjectDao = s3ObjectDao;
	}

	/**
	 * Get the configuration for the object.
	 * 
	 * @return The configuration for the object.
	 */
	public Configuration getConfiguration() {
		return configuration;
	}

	/**
	 * Set the configuration for the object.
	 * 
	 * @param configuration
	 *            The configuration for the object.
	 */
	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	/**
	 * Generates a local path for the bucket. The path is a directory.
	 * 
	 * @param bucket
	 *            The bucket name.
	 * @return A <code>StringBuffer</code> containing the local path to a
	 *         directory for the bucket.
	 */
	public StringBuffer generateBucketPath(String bucket) {
		StringBuffer buffer = new StringBuffer();
		Configuration configuration = getConfiguration();
		String storageLocation = configuration
				.getString(CONFIG_STORAGE_LOCATION);
		String bucketDirectory = configuration.getString(
				CONFIG_DIRECTORY_BUCKETS, DIRECTORY_BUCKETS);

		logger.debug("Generating bucket path for bucket[" + bucket + "]");

		buffer.append(storageLocation);

		if (!storageLocation.endsWith(fileSeparator)) {
			buffer.append(fileSeparator);
		}

		buffer.append(bucketDirectory);

		if (!bucketDirectory.endsWith(fileSeparator)) {
			buffer.append(fileSeparator);
		}

		buffer.append(bucket);
		buffer.append(fileSeparator);

		return buffer;
	}
}
