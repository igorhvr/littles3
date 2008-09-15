package com.jpeterson.littles3.dao.filesystem;

import org.apache.commons.configuration.Configuration;

public abstract class FileBase {
	public static final String CONFIG_STORAGE_LOCATION = "storageLocation";

	public static final String CONFIG_DIRECTORY_BUCKETS = "dir.buckets";

	public static final String CONFIG_DIRECTORY_META = "dir.meta";

	public static final String CONFIG_DIRECTORY_OBJECTS = "dir.objects";

	public static final String EXTENSION = ".ser";

	protected static final String DIRECTORY_BUCKETS = "buckets";

	protected static final String DIRECTORY_META = "meta";

	protected static final String DIRECTORY_OBJECTS = "objects";

	protected static final String fileSeparator = System
			.getProperty("file.separator");

	private Configuration configuration;

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
	 *         directory for the bucket. Example:
	 *         <code>C:/temp/StorageEngine/buckets/</code>
	 */
	public StringBuffer generateBucketStoragePath() {
		StringBuffer buffer = new StringBuffer();
		Configuration configuration = getConfiguration();
		String storageLocation = configuration
				.getString(CONFIG_STORAGE_LOCATION);
		String bucketDirectory = configuration.getString(
				CONFIG_DIRECTORY_BUCKETS, DIRECTORY_BUCKETS);

		buffer.append(storageLocation);

		if (!storageLocation.endsWith(fileSeparator)) {
			buffer.append(fileSeparator);
		}

		buffer.append(bucketDirectory);

		if (!bucketDirectory.endsWith(fileSeparator)) {
			buffer.append(fileSeparator);
		}

		return buffer;
	}

	/**
	 * Generates a local path for the bucket meta data. The path is a directory.
	 * 
	 * @param bucket
	 *            The bucket name.
	 * @return A <code>StringBuffer</code> containing the local path to a
	 *         directory for the bucket. Example:
	 *         <code>C:/temp/StorageEngine/meta/</code>
	 */
	public StringBuffer generateBucketMetaPath() {
		StringBuffer buffer = new StringBuffer();
		Configuration configuration = getConfiguration();
		String storageLocation = configuration
				.getString(CONFIG_STORAGE_LOCATION);
		String bucketDirectory = configuration.getString(CONFIG_DIRECTORY_META,
				DIRECTORY_META);

		buffer.append(storageLocation);

		if (!storageLocation.endsWith(fileSeparator)) {
			buffer.append(fileSeparator);
		}

		buffer.append(bucketDirectory);

		if (!bucketDirectory.endsWith(fileSeparator)) {
			buffer.append(fileSeparator);
		}

		return buffer;
	}
}
