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

import java.io.File;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

public class JeCentral {
	private Environment env = null;

	private Database objectDb = null;

	private Database bucketDb = null;

	private Configuration configuration;

	private Log logger;

	public static final String OBJECT_DB_NAME = "object";

	public static final String BUCKET_DB_NAME = "bucket";

	public JeCentral() {
		logger = LogFactory.getLog(this.getClass());
	}

	/**
	 * Must be called after all properties are set. Initializes the bean.
	 * 
	 * @throws Exception
	 * @see {@link #openEnv()}
	 */
	public void init() throws Exception {
		logger.debug("init() called");
		openEnv();
	}

	/**
	 * Must be called during a "graceful" shutdown.
	 * 
	 * @throws Exception
	 * @see {@link #closeEnv()}
	 */
	public void destroy() throws Exception {
		logger.debug("destroy() called");
		closeEnv();
	}

	public void openEnv() throws DatabaseException {
		logger.debug("openEnv() called");

		String storageLocation = configuration.getString("storageLocation");
		String dirDb = configuration.getString("dir.db");
		String dbObjectName = configuration.getString("db." + OBJECT_DB_NAME);
		String dbBucketName = configuration.getString("db." + BUCKET_DB_NAME);
		EnvironmentConfig envConf = new EnvironmentConfig();
		envConf.setAllowCreate(true);
		env = new Environment(new File(storageLocation, dirDb), envConf);

		DatabaseConfig dbConfig = new DatabaseConfig();
		dbConfig.setAllowCreate(true);
		objectDb = env.openDatabase(null, dbObjectName, dbConfig);
		bucketDb = env.openDatabase(null, dbBucketName, dbConfig);
	}

	public void closeEnv() {
		logger.debug("closeEnv() called");

		try {
			objectDb.close();
			bucketDb.close();
			env.close();
		} catch (DatabaseException DBE) {
		}
	}

	public Database getDatabase(String name) {
		if (name.equals(OBJECT_DB_NAME)) {
			return objectDb;
		} else if (name.equals(BUCKET_DB_NAME)) {
			return bucketDb;
		}

		return null;
	}

	/**
	 * Get the configuration for the servlet.
	 * 
	 * @return The configuration for the servlet.
	 */
	public Configuration getConfiguration() {
		return configuration;
	}

	/**
	 * Set the configuration for the servlet.
	 * 
	 * @param configuration
	 *            The configuration for the servlet.
	 */
	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}
}
