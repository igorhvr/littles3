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

import com.jpeterson.littles3.bo.S3Object;

/**
 * Manage S3 object resource.
 * 
 * @author Jesse Peterson
 */
public interface S3ObjectDao {
	/**
	 * Load the S3 object.
	 * 
	 * @param bucket
	 *            The bucket name for the particular object resource.
	 * @param key
	 *            The key for the particular object resource.
	 * @return The S3 object for the provided bucket + key.
	 * @throws DataAccessException
	 *             Unable to load the object resource.
	 */
	public S3Object loadS3Object(String bucket, String key)
			throws DataAccessException;

	/**
	 * Save the S3 object.
	 * 
	 * @param s3Object
	 *            The S3 object to save.
	 * @throws DataAccessException
	 *             Unable to save the S3 object.
	 */
	public void storeS3Object(S3Object s3Object) throws DataAccessException;

	/**
	 * Delete the S3 object.
	 * 
	 * @param s3Object
	 *            The S3 object to remove.
	 * @throws DataAccessException
	 *             Unabel to remove the S3 object.
	 */
	public void removeS3Object(S3Object s3Object) throws DataAccessException;

	/**
	 * Generate a list of the keys in a bucket.
	 * 
	 * @param bucket
	 *            The bucket containing the keys to list.
	 * @param prefix
	 *            Restring the list to only contain results that begin with the
	 *            specified prefix.
	 * @param marker
	 *            Enable pagination of large result sets.
	 * @param delimiter
	 *            Used to roll up common keys into a summary.
	 * @param maxKeys
	 *            Limit the number of results returned in response to your
	 *            query.
	 * @return An XML document with the listing results.
	 * @throws DataAccessException
	 *             Unable to generate the list.
	 */
	public String listKeys(String bucket, String prefix, String marker,
			String delimiter, int maxKeys) throws DataAccessException;
}
