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

package com.jpeterson.littles3.service;

import java.io.IOException;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;

import com.jpeterson.littles3.bo.Bucket;
import com.jpeterson.littles3.bo.CanonicalUser;
import com.jpeterson.littles3.bo.S3Object;
import com.jpeterson.littles3.dao.BucketDao;
import com.jpeterson.littles3.dao.S3ObjectDao;

public interface StorageService {

	/**
	 * Create an S3Object.
	 * 
	 * @param bucket
	 *            The bucket the object is to be created in.
	 * @param key
	 *            The object key.
	 * @param owner
	 *            The owner of the object.
	 * @return An S3Object that can be populated and saved.
	 * @throws IOException
	 *             Unable to create the object.
	 * @throws AccessControlException
	 *             Unable to create object because the owner doesn't have WRITE
	 *             permission to the bucket.
	 */
	public S3Object createS3Object(Bucket bucket, String key,
			CanonicalUser owner) throws IOException;

	/**
	 * Load an S3Object.
	 * 
	 * @param bucket
	 *            The name of the bucket containing the object.
	 * @param key
	 *            The object key.
	 * @return
	 * @throws DataRetrievalFailureException
	 *             Unable to find the <code>S3Object</code> for the provided
	 *             <code>bucket</code> and <code>key</code>.
	 * @throws DataAccessException
	 */
	public S3Object load(String bucket, String key) throws DataAccessException;

	public void store(S3Object s3Object) throws DataAccessException;

	public void remove(S3Object s3Object) throws DataAccessException;

	public void setS3ObjectDao(S3ObjectDao s3ObjectDao);

	/**
	 * Create a bucket.
	 * 
	 * @param name
	 *            The name of the bucket.
	 * @param owner
	 *            The owner of the bucket.
	 * @throws BucketAlreadyExistsException
	 *             Thrown if the bucket to create already exists.
	 * @throws IOException
	 *             Unable to create bucket.
	 */
	public Bucket createBucket(String name, CanonicalUser owner)
			throws IOException;

	public Bucket loadBucket(String name) throws DataAccessException;

	public void storeBucket(Bucket bucket) throws DataAccessException;

	/**
	 * Delete a bucket.
	 * 
	 * @param bucket
	 *            The bucket to delete.
	 * @throws BucketNotEmptyException
	 *             Thrown if the bucket to delete is not empty.
	 * @throws IOException
	 *             Unable to delete bucket.
	 */
	public void deleteBucket(Bucket bucket) throws IOException;

	/**
	 * Find the buckets for a user.
	 * 
	 * @param username
	 *            The username of the user who uses the buckets.
	 * @return A list of the Buckets. Will be empty if the user has no buckets.
	 * @throws IOException
	 *             Unable to find a user's buckets.
	 */
	public List<Bucket> findBuckets(String username) throws IOException;

	public String listKeys(Bucket bucket, String prefix, String marker,
			String delimiter, int maxKeys) throws DataAccessException;

	public void setBucketDao(BucketDao bucketDao);
}
