package com.jpeterson.littles3.dao.filesystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeSet;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataRetrievalFailureException;

import com.javaexchange.RandomGUID;
import com.jpeterson.littles3.bo.Acp;
import com.jpeterson.littles3.bo.CanonicalUser;
import com.jpeterson.littles3.bo.S3Object;
import com.jpeterson.littles3.dao.S3ObjectDao;

/**
 * An implementation of <code>S3ObjectDao</code> that uses the file system to
 * index and manage the S3Object's meta data.
 * 
 * @author Jesse Peterson
 */
public class FileS3ObjectDao extends FileBase implements S3ObjectDao {
	private Log logger;

	public static final int MAXIMUM_MAX_KEYS = 1000;

	private static SimpleDateFormat iso8601 = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

	private static TimeZone utc = TimeZone.getTimeZone("UTC");

	static {
		iso8601.setTimeZone(utc);
	}

	public FileS3ObjectDao() {
		super();
		logger = LogFactory.getLog(this.getClass());
		logger.debug("FileS3ObjectDao created");
	}

	public S3Object loadS3Object(String bucket, String key) {
		Map<String, String> keys;

		// load key index
		try {
			keys = retrieveKeyIndex(bucket, true);
		} catch (IOException e) {
			throw new DataAccessResourceFailureException(
					"Unable to load the key index for bucket: " + bucket, e);
		}

		return intLoadS3Object(keys, bucket, key);
	}

	protected S3Object intLoadS3Object(Map<String, String> keys, String bucket,
			String key) throws DataAccessException {
		S3Object s3Object = null;
		String relativeSerializedObjectFile;
		File serializedObjectFile;
		FileInputStream fis = null;
		ObjectInputStream in = null;

		relativeSerializedObjectFile = keys.get(key);
		if (relativeSerializedObjectFile == null) {
			throw new DataRetrievalFailureException("Could not find S3Object");
		}

		serializedObjectFile = new File(generateMetaStoragePath()
				.append(bucket).append(fileSeparator).append(
						relativeSerializedObjectFile).toString());

		try {
			fis = new FileInputStream(serializedObjectFile);
			in = new ObjectInputStream(fis);
			s3Object = (S3Object) in.readObject();
			in.close();

			return s3Object;
		} catch (FileNotFoundException e) {
			throw new DataRetrievalFailureException(
					"Could not find object in bucket: " + bucket + " key: "
							+ key);
		} catch (IOException e) {
			throw new DataRetrievalFailureException(
					"Could not find object in bucket: " + bucket + " key: "
							+ key);
		} catch (ClassNotFoundException e) {
			throw new DataRetrievalFailureException(
					"Could not find object in bucket: " + bucket + " key: "
							+ key);
		}
	}

	public void storeS3Object(S3Object s3Object) throws DataAccessException {
		String bucketDirectoryPath;
		File serializedObjectFile;
		String bucket = s3Object.getBucket();
		Map<String, String> keys;
		String relativeSerializedObjectFile;
		String key = s3Object.getKey();
		FileOutputStream fos = null;
		ObjectOutputStream out = null;

		bucketDirectoryPath = generateMetaStoragePath().append(bucket).append(
				fileSeparator).toString();

		// load key index
		try {
			keys = retrieveKeyIndex(bucket, true);
		} catch (IOException e) {
			throw new DataAccessResourceFailureException(
					"Unable to load the key index for bucket: " + bucket, e);
		}

		relativeSerializedObjectFile = keys.get(key);
		if (relativeSerializedObjectFile == null) {
			// find a random file to serialize the object to
			String guid;

			// makes sure that the file URL doesn't exist yet
			do {
				guid = new RandomGUID().valueAfterMD5;

				relativeSerializedObjectFile = guid.substring(0, 2)
						+ fileSeparator + guid + EXTENSION;

				serializedObjectFile = new File(bucketDirectoryPath
						+ relativeSerializedObjectFile);
			} while (serializedObjectFile.exists());
		} else {
			serializedObjectFile = new File(bucketDirectoryPath
					+ relativeSerializedObjectFile);
		}

		// make sure the directory for the serialized object file exists
		serializedObjectFile.getParentFile().mkdirs();

		try {
			fos = new FileOutputStream(serializedObjectFile, false);
			out = new ObjectOutputStream(fos);
			out.writeObject(s3Object);
			out.close();
		} catch (IOException e) {
			throw new DataAccessResourceFailureException(
					"Unable to store S3Object: " + serializedObjectFile, e);
		}

		// update the key index
		keys.put(key, relativeSerializedObjectFile);

		// save the key index
		try {
			storeKeyIndex(bucket, keys);
		} catch (IOException e) {
			throw new DataAccessResourceFailureException(
					"Unable to store the key index for bucket: " + bucket, e);
		}
	}

	public void removeS3Object(S3Object s3Object) throws DataAccessException {
		Map<String, String> keys;
		String relativeSerializedObjectFile;
		File serializedObjectFile;
		String bucket = s3Object.getBucket();
		String key = s3Object.getKey();

		// load key index
		try {
			keys = retrieveKeyIndex(bucket, true);
		} catch (IOException e) {
			throw new DataAccessResourceFailureException(
					"Unable to load the key index for bucket: " + bucket, e);
		}

		relativeSerializedObjectFile = keys.get(key);
		if (relativeSerializedObjectFile == null) {
			throw new DataRetrievalFailureException("Could not find S3Object");
		}

		serializedObjectFile = new File(generateMetaStoragePath()
				.append(bucket).append(fileSeparator).append(
						relativeSerializedObjectFile).toString());

		if (!serializedObjectFile.delete()) {
			throw new DataRetrievalFailureException(
					"Could not delete object in bucket: " + bucket + " key: "
							+ key);
		}

		// try to delete the first 2 characters directory
		serializedObjectFile.getParentFile().delete();

		// update the key index
		keys.remove(key);

		// save the key index
		try {
			storeKeyIndex(bucket, keys);
		} catch (IOException e) {
			throw new DataAccessResourceFailureException(
					"Unable to store the key index for bucket: " + bucket, e);
		}
	}

	public String listKeys(String bucket, String prefix, String marker,
			String delimiter, int maxKeys) throws DataAccessException {
		Map<String, String> keys;
		int results = 0;
		S3Object o = null;
		boolean truncated = false;
		List<S3Object> contents = new ArrayList<S3Object>();
		List<String> commonPrefixes = new ArrayList<String>();
		int prefixLength;
		String key = null;
		boolean processed;
		int delimiterIndex;
		String currentPrefix;
		String commonPrefix = null;

		if (logger.isDebugEnabled()) {
			logger.debug("listKeys: bucket[" + bucket + "], prefix[" + prefix
					+ "], marker[" + marker + "], delimiter[" + delimiter
					+ "], maxKeys[" + maxKeys + "]");
		}

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

		// load key index
		try {
			keys = retrieveKeyIndex(bucket, true);
		} catch (IOException e) {
			throw new DataAccessResourceFailureException(
					"Unable to load the key index for bucket: " + bucket, e);
		}

		if (maxKeys > 0) {
			// validate the marker, should start with prefix
			if (marker != null) {
				if (!marker.startsWith(prefix)) {
					logger.info("marker[" + marker
							+ "] doesn't start with prefix[" + prefix
							+ "], ignoring marker");
					marker = null;
				}
			}

			TreeSet<String> orderedKeys = new TreeSet<String>(keys.keySet());
			Iterator<String> objectKeys = orderedKeys.iterator();

			if (marker != null) {
				// skip to key starting with marker
				while (objectKeys.hasNext()) {
					if (objectKeys.next().equals(marker)) {
						if (logger.isDebugEnabled()) {
							logger.debug("Found marker key: " + marker);
						}
						// skip the marker
						if (objectKeys.hasNext()) {
							key = objectKeys.next();
							if (logger.isDebugEnabled()) {
								logger.debug("Key after marker: " + key);
							}
						}
						break;
					}
				}
			} else {
				// skip to key starting with prefix
				while (objectKeys.hasNext()) {
					key = objectKeys.next();
					if (key.startsWith(prefix)) {
						if (logger.isDebugEnabled()) {
							logger.debug("Found key with prefix [" + prefix
									+ "]: " + key);
						}
						break;
					}
				}
			}

			while ((key != null) && (key.startsWith(prefix))) {

				if (results >= maxKeys) {
					truncated = true;
					break;
				}

				// valid result
				o = intLoadS3Object(keys, bucket, key);

				// is it a content or a common prefix?
				processed = false;
				if (delimiter != null) {
					key = o.getKey();
					if ((delimiterIndex = key.indexOf(delimiter, prefixLength)) != -1) {
						// include the delimiter in the common
						// prefix
						currentPrefix = key.substring(0, delimiterIndex
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
					if (logger.isDebugEnabled()) {
						logger.debug("Found a content key: " + o);
					}
					contents.add(o);
					++results;
					processed = true;
				}

				if (objectKeys.hasNext()) {
					key = objectKeys.next();
				} else {
					key = null;
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
		for (S3Object s3Object : contents) {
			buffer.append("<Contents>");
			buffer.append("<Key>").append(s3Object.getKey()).append("</Key>");
			buffer.append("<LastModified>").append(
					iso8601.format(s3Object.getLastModified())).append(
					"</LastModified>");
			buffer.append("<ETag>\"").append(s3Object.getETag()).append(
					"\"</ETag>");
			buffer.append("<Size>").append(s3Object.getContentLength()).append(
					"</Size>");
			buffer.append("<Owner>");
			Acp acp = s3Object.getAcp();
			CanonicalUser owner = acp.getOwner();
			buffer.append("<ID>").append(owner.getId()).append("</ID>");
			buffer.append("<DisplayName>").append(owner.getDisplayName())
					.append("</DisplayName>");
			buffer.append("</Owner>");
			buffer.append("<StorageClass>STANDARD</StorageClass>");
			buffer.append("</Contents>");
		}

		// print common prefixes
		for (Iterator<String> iter = commonPrefixes.iterator(); iter.hasNext();) {
			buffer.append("<CommonPrefixes>");
			buffer.append("<Prefix>").append((String) iter.next()).append(
					"</Prefix>");
			buffer.append("</CommonPrefixes>");
		}

		buffer.append("</ListBucketResult>");

		return buffer.toString();
	}

	/**
	 * 
	 * @return Example: C:/temp/StorageEngine/meta/objects/
	 */
	public StringBuffer generateMetaStoragePath() {
		StringBuffer buffer = new StringBuffer();
		Configuration configuration = getConfiguration();
		String storageLocation = configuration
				.getString(CONFIG_STORAGE_LOCATION);
		String metaDirectory = configuration.getString(CONFIG_DIRECTORY_META,
				DIRECTORY_META);
		String objectsDirectory = configuration.getString(
				CONFIG_DIRECTORY_OBJECTS, DIRECTORY_OBJECTS);

		buffer.append(storageLocation);

		if (!storageLocation.endsWith(fileSeparator)) {
			buffer.append(fileSeparator);
		}

		buffer.append(metaDirectory);

		if (!metaDirectory.endsWith(fileSeparator)) {
			buffer.append(fileSeparator);
		}

		buffer.append(objectsDirectory);

		if (!objectsDirectory.endsWith(fileSeparator)) {
			buffer.append(fileSeparator);
		}

		return buffer;
	}

	@SuppressWarnings("unchecked")
	private Map<String, String> retrieveKeyIndex(String bucket, boolean create)
			throws IOException {
		File serializedKeyIndex;
		FileInputStream fis = null;
		ObjectInputStream in = null;
		HashMap<String, String> keys = null;

		serializedKeyIndex = new File(generateMetaStoragePath().append(bucket)
				.append(fileSeparator).toString(), "keys" + EXTENSION);

		try {
			fis = new FileInputStream(serializedKeyIndex);
			in = new ObjectInputStream(fis);
			keys = (HashMap<String, String>) in.readObject();
			in.close();
		} catch (FileNotFoundException e) {
			// create the key index
			keys = new HashMap<String, String>();
			storeKeyIndex(bucket, keys);
		} catch (IOException e) {
			throw new DataAccessResourceFailureException(
					"Unable to store Bucket: " + bucket, e);
		} catch (ClassNotFoundException e) {
			throw new DataAccessResourceFailureException(
					"Unable to store Bucket: " + bucket, e);
		}

		return keys;
	}

	private void storeKeyIndex(String bucket, Map<String, String> keys)
			throws IOException {
		File bucketDirectory;
		File serializedKeyIndex;
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		File parent;

		bucketDirectory = new File(generateMetaStoragePath().append(bucket)
				.append(fileSeparator).toString());
		serializedKeyIndex = new File(bucketDirectory, "keys" + EXTENSION);

		if (keys.isEmpty()) {
			// delete any existing serialized key index
			if (serializedKeyIndex.delete()) {
				// delete named bucket directory, if empty
				parent = serializedKeyIndex.getParentFile();
				if (parent.delete()) {
					// delete 'objects' directory, if empty
					parent = parent.getParentFile();
					parent.delete();
				}
			}
		} else {
			// create bucket meta storage directory if necessary
			if (!bucketDirectory.exists()) {
				if (!bucketDirectory.mkdirs()) {
					throw new IOException(
							"Could not create objects meta directory: "
									+ bucketDirectory);
				}
			}

			// persist the key index
			fos = new FileOutputStream(serializedKeyIndex, false);
			out = new ObjectOutputStream(fos);
			out.writeObject(keys);
			out.close();
		}
	}
}
