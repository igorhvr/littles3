package com.jpeterson.littles3.dao.filesystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataRetrievalFailureException;

import com.jpeterson.littles3.bo.Bucket;
import com.jpeterson.littles3.dao.BucketDao;

/**
 * An implementation of <code>BucketDao</code> that uses the file system to
 * index and manage the bucket's meta data.
 * 
 * @author Jesse Peterson
 */
public class FileBucketDao extends FileBase implements BucketDao {
	private Log logger;

	public FileBucketDao() {
		super();
		logger = LogFactory.getLog(this.getClass());
		logger.debug("FileBucketDao created");
	}

	/**
	 * This method will load the bucket from a Java serialized file.
	 * 
	 * @param bucket
	 *            The name of the bucket to load.
	 * @throws DataAccessResourceFailureException
	 *             General failure serializing the bucket.
	 */
	public Bucket loadBucket(String bucket) throws DataAccessException {
		File serializedBucketFile;
		FileInputStream fis = null;
		ObjectInputStream in = null;
		Bucket theBucket = null;

		serializedBucketFile = new File(generateMetaStoragePath()
				.append(bucket).append(fileSeparator).append(bucket).append(
						EXTENSION).toString());

		try {
			fis = new FileInputStream(serializedBucketFile);
			in = new ObjectInputStream(fis);
			theBucket = (Bucket) in.readObject();
			in.close();

			return theBucket;
		} catch (FileNotFoundException e) {
			throw new DataRetrievalFailureException("Could not find Bucket: "
					+ bucket);
		} catch (IOException e) {
			throw new DataAccessResourceFailureException(
					"Unable to store Bucket: " + bucket, e);
		} catch (ClassNotFoundException e) {
			throw new DataAccessResourceFailureException(
					"Unable to store Bucket: " + bucket, e);
		}
	}

	/**
	 * This method will remove a Java serialized file representing the bucket.
	 * 
	 * @param bucket
	 *            The name of the bucket to remove.
	 * @throws DataAccessResourceFailureException
	 *             General failure removing the bucket.
	 */
	public void removeBucket(Bucket bucket) throws DataAccessException {
		File bucketDirectory;
		File serializedBucketFile;

		bucketDirectory = new File(generateMetaStoragePath().append(
				bucket.getName()).append(fileSeparator).toString());

		serializedBucketFile = new File(bucketDirectory, bucket.getName()
				+ EXTENSION);

		if (serializedBucketFile.exists()) {
			if (!serializedBucketFile.delete()) {
				throw new DataAccessResourceFailureException(
						"Unable to delete Bucket: " + bucket + " ("
								+ serializedBucketFile.getAbsolutePath() + ")");
			}
		}

		if (bucketDirectory.exists()) {
			String[] files = bucketDirectory.list();

			if (files.length > 0) {
				logger.debug("Bucket not empty. Number of files in directory: "
						+ files.length);
				throw new DataAccessResourceFailureException(
						"Bucket is not empty");
			}

			if (!bucketDirectory.delete()) {
				throw new DataAccessResourceFailureException(
						"Could not delete bucket meta directory: "
								+ bucketDirectory);
			} else {
				// try to delete the 'buckets' directory, if empty
				bucketDirectory.getParentFile().delete();
			}
		}
	}

	/**
	 * This method will store the bucket as a Java serialized file.
	 * 
	 * @param bucket
	 *            The bucket to store.
	 * @throws DataAccessResourceFailureException
	 *             General failure serializing the bucket.
	 */
	public void storeBucket(Bucket bucket) throws DataAccessException {
		File bucketDirectory;
		File serializedBucketFile;
		FileOutputStream fos = null;
		ObjectOutputStream out = null;

		// create bucket meta storage directory if necessary
		bucketDirectory = new File(generateMetaStoragePath().append(
				bucket.getName()).append(fileSeparator).toString());

		if (!bucketDirectory.exists()) {
			if (!bucketDirectory.mkdirs()) {
				throw new DataAccessResourceFailureException(
						"Could not create bucket meta directory: "
								+ bucketDirectory);
			}
		}

		serializedBucketFile = new File(bucketDirectory, bucket.getName()
				+ EXTENSION);

		try {
			fos = new FileOutputStream(serializedBucketFile, false);
			out = new ObjectOutputStream(fos);
			out.writeObject(bucket);
			out.close();
		} catch (IOException e) {
			throw new DataAccessResourceFailureException(
					"Unable to store Bucket: " + bucket, e);
		}
	}

	/**
	 * 
	 * @return Example: C:/temp/StorageEngine/meta/buckets/
	 */
	public StringBuffer generateMetaStoragePath() {
		StringBuffer buffer = new StringBuffer();
		Configuration configuration = getConfiguration();
		String storageLocation = configuration
				.getString(CONFIG_STORAGE_LOCATION);
		String metaDirectory = configuration.getString(CONFIG_DIRECTORY_META,
				DIRECTORY_META);
		String bucketsDirectory = configuration.getString(
				CONFIG_DIRECTORY_BUCKETS, DIRECTORY_BUCKETS);

		buffer.append(storageLocation);

		if (!storageLocation.endsWith(fileSeparator)) {
			buffer.append(fileSeparator);
		}

		buffer.append(metaDirectory);

		if (!metaDirectory.endsWith(fileSeparator)) {
			buffer.append(fileSeparator);
		}

		buffer.append(bucketsDirectory);

		if (!bucketsDirectory.endsWith(fileSeparator)) {
			buffer.append(fileSeparator);
		}

		return buffer;
	}
}
