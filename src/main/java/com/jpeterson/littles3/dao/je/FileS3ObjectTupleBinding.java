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

import java.io.IOException;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jpeterson.littles3.bo.FileS3Object;
import com.jpeterson.littles3.bo.S3Object;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class FileS3ObjectTupleBinding extends TupleBinding {
	private Log logger;

	public FileS3ObjectTupleBinding() {
		super();
		logger = LogFactory.getLog(this.getClass());
	}

	@Override
	public Object entryToObject(TupleInput entry) {
		String bucket;
		String key;
		URL storageUrl;

		// Data must be read in the same order that it was
		// originally written.

		bucket = entry.readString();
		key = entry.readString();
		try {
			storageUrl = new URL(entry.readString());
		} catch (IOException e) {
			logger.error(
					"Unable to read the storage URL from the database record",
					e);
			e.printStackTrace();
			return null;
		}

		S3Object s3Object = new FileS3Object(bucket, key, storageUrl);

		s3Object.setContentDisposition(entry.readString());
		s3Object.setContentLength(entry.readLong());
		s3Object.setContentMD5(entry.readString());
		s3Object.setContentType(entry.readString());
		s3Object.setETag(entry.readString());
		s3Object.setLastModified(entry.readLong());

		return s3Object;
	}

	@Override
	public void objectToEntry(Object object, TupleOutput entry) {
		S3Object s3Object = (S3Object) object;

		// Data must be read in the same order that it was
		// originally written.

		entry.writeString(s3Object.getBucket());
		entry.writeString(s3Object.getKey());
		entry.writeString(s3Object.getStorageUrl().toString());
		entry.writeString(s3Object.getContentDisposition());
		entry.writeLong(s3Object.getContentLength());
		entry.writeString(s3Object.getContentMD5());
		entry.writeString(s3Object.getContentType());
		entry.writeString(s3Object.getETag());
		entry.writeLong(s3Object.getLastModified());
	}
}
