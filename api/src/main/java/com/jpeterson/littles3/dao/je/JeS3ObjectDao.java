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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataRetrievalFailureException;

import com.jpeterson.littles3.bo.Acp;
import com.jpeterson.littles3.bo.CanonicalUser;
import com.jpeterson.littles3.bo.S3Object;
import com.jpeterson.littles3.dao.S3ObjectDao;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

/**
 * An implementation of <code>S3ObjectDao</code> that uses Oracle Berkeley DB
 * Java Edition to index and manage the object's meta data.
 * 
 * @author Jesse Peterson
 */
public class JeS3ObjectDao implements S3ObjectDao {

	private TupleBinding s3ObjectBucketKeyBinding = new S3ObjectBucketKeyTupleBinding();

	private TupleBinding fileS3ObjectBinding = new FileS3ObjectTupleBinding();

	private JeCentral jeCentral;

	private Log logger;

	public static final int MAXIMUM_MAX_KEYS = 1000;

	private static SimpleDateFormat iso8601 = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

	private static TimeZone utc = TimeZone.getTimeZone("UTC");

	static {
		iso8601.setTimeZone(utc);
	}

	/**
	 * Basic constructor.
	 */
	public JeS3ObjectDao() {
		super();
		logger = LogFactory.getLog(this.getClass());
	}

	/**
	 * This method retrieves the <code>S3Object</code> representation of an
	 * object from the underlying JE database via the object's bucket and key.
	 * 
	 * @param bucket
	 *            The bucket identifying the object.
	 * @param key
	 *            The key identifying the object.
	 * @return The <code>S3Object</code> identified by the provided
	 *         <code>bucket</code> + <code>key</code>.
	 * @throws DataRetrievalFailureException
	 *             Could not find the <code>S3Object</code> for the provided
	 *             <code>bucket</code> and <code>key</code>.
	 * @throws DataAccessResourceFailureException
	 *             General failure retrieving the GUID from the JE database.
	 * @throws DataAccessException
	 *             General failure retrieving the GUID.
	 */
	public S3Object loadS3Object(String bucket, String key)
			throws DataAccessException {
		DatabaseEntry theKey;
		DatabaseEntry theData;

		// Environment myDbEnvironment = null;
		Database database = null;
		try {
			S3Object s3ObjectBucketKey = new S3ObjectBucketKey();
			s3ObjectBucketKey.setBucket(bucket);
			s3ObjectBucketKey.setKey(key);

			theKey = new DatabaseEntry();
			s3ObjectBucketKeyBinding.objectToEntry(s3ObjectBucketKey, theKey);
			theData = new DatabaseEntry();

			// TODO: generalize this
			/*
			 * EnvironmentConfig envConfig = new EnvironmentConfig();
			 * envConfig.setAllowCreate(true); myDbEnvironment = new
			 * Environment(new File( "C:/temp/StorageEngine/db"), envConfig);
			 * 
			 * DatabaseConfig dbConfig = new DatabaseConfig();
			 * dbConfig.setAllowCreate(true); database =
			 * myDbEnvironment.openDatabase(null, "sampleDatabase", dbConfig);
			 */

			database = jeCentral.getDatabase(JeCentral.OBJECT_DB_NAME);

			if (database.get(null, theKey, theData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
				return (S3Object) fileS3ObjectBinding.entryToObject(theData);
			} else {
				throw new DataRetrievalFailureException(
						"Could not find S3Object");
			}
		} catch (DatabaseException e) {
			throw new DataAccessResourceFailureException(
					"Unable to load a database record", e);
		} finally {
			/*
			 * if (database != null) { try { database.close(); } catch
			 * (DatabaseException e) { // do nothing } } database = null;
			 * 
			 * if (myDbEnvironment != null) { try { myDbEnvironment.close(); }
			 * catch (DatabaseException e) { // do nothing } } myDbEnvironment =
			 * null;
			 */
		}
	}

	public void storeS3Object(S3Object s3Object) throws DataAccessException {
		DatabaseEntry theKey;
		DatabaseEntry theData;

		// store the object meta data

		// Environment myDbEnvironment = null;
		Database database = null;
		try {

			theKey = new DatabaseEntry();
			s3ObjectBucketKeyBinding.objectToEntry(s3Object, theKey);
			theData = new DatabaseEntry();
			fileS3ObjectBinding.objectToEntry(s3Object, theData);

			// TODO: generalize this
			/*
			 * EnvironmentConfig envConfig = new EnvironmentConfig();
			 * envConfig.setAllowCreate(true); myDbEnvironment = new
			 * Environment(new File( "C:/temp/StorageEngine/db"), envConfig);
			 * 
			 * DatabaseConfig dbConfig = new DatabaseConfig();
			 * dbConfig.setAllowCreate(true); database =
			 * myDbEnvironment.openDatabase(null, "sampleDatabase", dbConfig);
			 */

			database = jeCentral.getDatabase(JeCentral.OBJECT_DB_NAME);

			database.put(null, theKey, theData);
		} catch (DatabaseException e) {
			throw new DataAccessResourceFailureException(
					"Unable to store a database record", e);
		} finally {
			/*
			 * if (database != null) { try { database.close(); } catch
			 * (DatabaseException e) { // do nothing } } database = null;
			 * 
			 * if (myDbEnvironment != null) { try { myDbEnvironment.close(); }
			 * catch (DatabaseException e) { // do nothing } } myDbEnvironment =
			 * null;
			 */
		}
	}

	public void removeS3Object(S3Object s3Object) throws DataAccessException {
		DatabaseEntry theKey;

		// Environment myDbEnvironment = null;
		Database database = null;

		try {
			theKey = new DatabaseEntry();
			s3ObjectBucketKeyBinding.objectToEntry(s3Object, theKey);

			// TODO: generalize this
			/*
			 * EnvironmentConfig envConfig = new EnvironmentConfig();
			 * envConfig.setAllowCreate(true); myDbEnvironment = new
			 * Environment(new File( "C:/temp/StorageEngine/db"), envConfig);
			 * 
			 * DatabaseConfig dbConfig = new DatabaseConfig();
			 * dbConfig.setAllowCreate(true); database =
			 * myDbEnvironment.openDatabase(null, "sampleDatabase", dbConfig);
			 */

			database = jeCentral.getDatabase(JeCentral.OBJECT_DB_NAME);

			database.delete(null, theKey);
		} catch (DatabaseException e) {
			throw new DataAccessResourceFailureException(
					"Unable to store a database record", e);
		} finally {
			/*
			 * if (database != null) { try { database.close(); } catch
			 * (DatabaseException e) { // do nothing } } database = null;
			 * 
			 * if (myDbEnvironment != null) { try { myDbEnvironment.close(); }
			 * catch (DatabaseException e) { // do nothing } } myDbEnvironment =
			 * null;
			 */
		}
	}

	public String listKeys(String bucket, String prefix, String marker,
			String delimiter, int maxKeys) throws DataAccessException {
		DatabaseEntry theKey;
		DatabaseEntry theData;
		boolean truncated = false;
		List<S3Object> contents = new ArrayList<S3Object>();
		List<String> commonPrefixes = new ArrayList<String>();
		int results = 0;
		String commonPrefix = null;
		String currentPrefix;
		S3Object o = null;
		String key;
		OperationStatus operationStatus;
		int prefixLength;
		int delimiterIndex;
		boolean processed;

		if (prefix == null) {
			prefix = "";
		}
		prefixLength = prefix.length();

		if ((delimiter != null) && (delimiter.equals(""))) {
			// make delimiter null for performance reasons
			delimiter = null;
		}

		if (maxKeys > MAXIMUM_MAX_KEYS) {
			maxKeys = MAXIMUM_MAX_KEYS;
		}

		// perform query, saving results

		if (maxKeys > 0) {
			Database database = null;
			Cursor cursor = null;
			try {
				S3Object s3ObjectBucketKey;

				database = jeCentral.getDatabase(JeCentral.OBJECT_DB_NAME);
				cursor = database.openCursor(null, null);

				s3ObjectBucketKey = new S3ObjectBucketKey();

				// validate the marker, should start with prefix
				if (marker != null) {
					if (!marker.startsWith(prefix)) {
						logger.info("marker[" + marker
								+ "] doesn't start with prefix[" + prefix
								+ "], ignoring marker");
						marker = null;
					}
				}

				s3ObjectBucketKey.setBucket(bucket);
				if (marker != null) {
					// try to find cursor starting with marker
					s3ObjectBucketKey.setKey(marker);
				} else {
					// try to find cursor starting with prefix
					s3ObjectBucketKey.setKey(prefix);
				}

				theKey = new DatabaseEntry();
				s3ObjectBucketKeyBinding.objectToEntry(s3ObjectBucketKey,
						theKey);
				theData = new DatabaseEntry();

				// get first record "closest" to the key
				if ((operationStatus = cursor.getSearchKeyRange(theKey,
						theData, LockMode.DEFAULT)) == OperationStatus.SUCCESS) {
					s3ObjectBucketKey = (S3Object) s3ObjectBucketKeyBinding
							.entryToObject(theKey);

					if (bucket.equals(s3ObjectBucketKey.getBucket())) {
						key = s3ObjectBucketKey.getKey();
						if (logger.isTraceEnabled()) {
							logger.trace("bucket: "
									+ s3ObjectBucketKey.getBucket() + " key: "
									+ key);
						}
						if (key.equals(marker)) {
							// skip to next result after the marker
							if ((operationStatus = cursor.getNext(theKey,
									theData, LockMode.DEFAULT)) != OperationStatus.SUCCESS) {
								key = s3ObjectBucketKey.getKey();
							}
						}

						// restrict to results that begin with the prefix
						while ((operationStatus == OperationStatus.SUCCESS)
								&& key.startsWith(prefix)) {
							if (!bucket.equals(s3ObjectBucketKey.getBucket())) {
								break;
							}

							if (results >= maxKeys) {
								truncated = true;
								break;
							}

							// valid result
							o = (S3Object) fileS3ObjectBinding
									.entryToObject(theData);

							// is it a content or a common prefix?
							processed = false;
							if (delimiter != null) {
								key = o.getKey();
								if ((delimiterIndex = key.indexOf(delimiter,
										prefixLength)) != -1) {
									// include the delimiter in the common
									// prefix
									currentPrefix = key
											.substring(0, delimiterIndex
													+ delimiter.length());
									if (currentPrefix.equals(commonPrefix)) {
										// skip common prefix
										processed = true;
									} else {
										// new common prefix
										commonPrefix = currentPrefix;
										commonPrefixes.add(commonPrefix);
										++results;
										processed = true;
									}
								}
							}
							if (!processed) {
								contents.add(o);
								++results;
								processed = true;
							}

							if ((operationStatus = cursor.getNext(theKey,
									theData, LockMode.DEFAULT)) != OperationStatus.SUCCESS) {
								break;
							}

							s3ObjectBucketKey = (S3Object) s3ObjectBucketKeyBinding
									.entryToObject(theKey);
							key = s3ObjectBucketKey.getKey();
						}
					}
				}
			} catch (DatabaseException e) {
				throw new DataAccessResourceFailureException(
						"Unable to read database record", e);
			} finally {
				if (cursor != null) {
					try {
						cursor.close();
					} catch (DatabaseException e) {
						// do nothing
					}
					cursor = null;
				}
			}
		}

		// generate XML from results

		StringBuffer buffer = new StringBuffer();

		buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		buffer
				.append("<ListBucketResult xmlns=\"http://s3.amazonaws.com/doc/2006-03-01/\">");
		buffer.append("<Name>").append(bucket).append("</Name>");
		if (prefix.equals("")) {
			buffer.append("<Prefix/>");
		} else {
			buffer.append("<Prefix>").append(prefix).append("</Prefix>");
		}
		if ((marker == null) || (marker.equals(""))) {
			buffer.append("<Marker/>");
		} else {
			buffer.append("<Marker>").append(marker).append("</Marker>");
		}
		buffer.append("<MaxKeys>").append(maxKeys).append("</MaxKeys>");
		if (delimiter == null) {
			buffer.append("<Delimiter/>");
		} else {
			buffer.append("<Delimiter>").append(delimiter).append(
					"</Delimiter>");
			// next marker is last key
			if (results > 0) {
				buffer.append("<NextMarker>").append(o.getKey()).append(
						"</NextMarker>");
			}
		}
		buffer.append("<IsTruncated>").append(truncated).append(
				"</IsTruncated>");

		// print out contents
		for (Iterator iter = contents.iterator(); iter.hasNext();) {
			o = (S3Object) iter.next();
			buffer.append("<Contents>");
			buffer.append("<Key>").append(o.getKey()).append("</Key>");
			buffer.append("<LastModified>").append(
					iso8601.format(o.getLastModified())).append(
					"</LastModified>");
			buffer.append("<ETag>\"").append(o.getETag()).append("\"</ETag>");
			buffer.append("<Size>").append(o.getContentLength()).append(
					"</Size>");
			buffer.append("<Owner>");
			Acp acp = o.getAcp();
			CanonicalUser owner = acp.getOwner();
			buffer.append("<ID>").append(owner.getId()).append("</ID>");
			buffer.append("<DisplayName>").append(owner.getDisplayName())
					.append("</DisplayName>");
			buffer.append("</Owner>");
			buffer.append("<StorageClass>STANDARD</StorageClass>");
			buffer.append("</Contents>");
		}

		// print common prefixes
		for (Iterator iter = commonPrefixes.iterator(); iter.hasNext();) {
			buffer.append("<CommonPrefixes>");
			buffer.append("<Prefix>").append((String) iter.next()).append(
					"</Prefix>");
			buffer.append("</CommonPrefixes>");
		}

		buffer.append("</ListBucketResult>");

		return buffer.toString();
	}

	public Database getDatabase() {
		return null;
	}

	public JeCentral getJeCentral() {
		return jeCentral;
	}

	public void setJeCentral(JeCentral jeCentral) {
		this.jeCentral = jeCentral;
	}
}
