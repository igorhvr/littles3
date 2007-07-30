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

package com.jpeterson.littles3.dao.je;

import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataRetrievalFailureException;

import com.jpeterson.littles3.bo.Bucket;
import com.jpeterson.littles3.dao.BucketDao;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

/**
 * An implementation of <code>BucketDao</code> that uses Oracle Berkeley DB
 * Java Edition to index and manage the bucket's meta data.
 * 
 * @author Jesse Peterson
 */
public class JeBucketDao implements BucketDao {
	private TupleBinding bucketBinding = new BucketTupleBinding();

	private JeCentral jeCentral;

	private Log logger;

	public JeBucketDao() {
		super();
		logger = LogFactory.getLog(this.getClass());
		logger.debug("JeBucketDao created");
	}

	/**
	 * This method retrieves the <code>Bucket</code> representation of a
	 * bucket from the underlying JE database via the bucket's name.
	 * 
	 * @param bucket
	 *            The bucket name.
	 * @throws DataRetrievalFailureException
	 *             Could not find the <code>Bucket</code> for the provided
	 *             <code>bucket</code> name.
	 * @throws DataAccessResourceFailureException
	 *             General failure retrieving the bucket from the JE database.
	 * @throws DataAccessException
	 *             General failure retrieving the bucket.
	 */
	public Bucket loadBucket(String bucket) throws DataAccessException {
		DatabaseEntry theKey;
		DatabaseEntry theData;

		// Environment myDbEnvironment = null;
		Database database = null;

		try {
			theKey = new DatabaseEntry(bucket.getBytes("UTF-8"));
			theData = new DatabaseEntry();

			database = jeCentral.getDatabase(JeCentral.BUCKET_DB_NAME);

			if (database.get(null, theKey, theData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
				return (Bucket) bucketBinding.entryToObject(theData);
			} else {
				throw new DataRetrievalFailureException(
						"Could not find S3Object");
			}
		} catch (DatabaseException e) {
			throw new DataAccessResourceFailureException(
					"Unable to load a database record", e);
		} catch (UnsupportedEncodingException e) {
			// should not happen
			e.printStackTrace();
			throw new DataAccessResourceFailureException(
					"Unexpected encoding error", e);
		}
	}

	public void storeBucket(Bucket bucket) throws DataAccessException {
		DatabaseEntry theKey;
		DatabaseEntry theData;

		// Environment myDbEnvironment = null;
		Database database = null;

		try {
			theKey = new DatabaseEntry(bucket.getName().getBytes("UTF-8"));
			theData = new DatabaseEntry();
			bucketBinding.objectToEntry(bucket, theData);

			database = jeCentral.getDatabase(JeCentral.BUCKET_DB_NAME);

			database.putNoOverwrite(null, theKey, theData);
		} catch (DatabaseException e) {
			throw new DataAccessResourceFailureException(
					"Unable to load a database record", e);
		} catch (UnsupportedEncodingException e) {
			// should not happen
			e.printStackTrace();
		}
	}

	public void removeBucket(Bucket bucket) throws DataAccessException {
		DatabaseEntry theKey;

		// Environment myDbEnvironment = null;
		Database database = null;

		try {
			theKey = new DatabaseEntry(bucket.getName().getBytes("UTF-8"));

			database = jeCentral.getDatabase(JeCentral.BUCKET_DB_NAME);

			database.delete(null, theKey);
		} catch (DatabaseException e) {
			throw new DataAccessResourceFailureException(
					"Unable to load a database record", e);
		} catch (UnsupportedEncodingException e) {
			// should not happen
			e.printStackTrace();
		}
	}

	public JeCentral getJeCentral() {
		return jeCentral;
	}

	public void setJeCentral(JeCentral jeCentral) {
		this.jeCentral = jeCentral;
	}
}
