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

package com.jpeterson.littles3.dao;

import org.springframework.dao.DataAccessException;

import com.jpeterson.littles3.bo.Bucket;

/**
 * Manage S3 bucket resource.
 * 
 * @author Jesse Peterson
 */
public interface BucketDao {
	/**
	 * Load the S3 bucket.
	 * 
	 * @param bucket
	 *            The bucket name for the particular bucket resource.
	 * @return The S3 bucket for the provided bucket name.
	 * @throws DataAccessException
	 *             Unable to load the bucket resource.
	 */
	public Bucket loadBucket(String bucket) throws DataAccessException;

	/**
	 * Save the S3 bucket.
	 * 
	 * @param bucket
	 *            The S3 bucket to save.
	 * @throws DataAccessException
	 *             Unable to save the S3 bucket.
	 */
	public void storeBucket(Bucket bucket) throws DataAccessException;

	/**
	 * Delete the S3 bucket.
	 * 
	 * @param bucket
	 *            The S3 bucket to remove.
	 * @throws DataAccessException
	 *             Unable to remove the S3 bucket.
	 */
	public void removeBucket(Bucket bucket) throws DataAccessException;
}
