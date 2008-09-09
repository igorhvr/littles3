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

import com.jpeterson.littles3.bo.S3Object;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class S3ObjectBucketKeyTupleBinding extends TupleBinding {

	/**
	 * Converts a entry buffer into an S3ObjectBucketKey.
	 * 
	 * @param entry
	 *            is the source entry buffer.
	 * @return the resulting Object. It will be an
	 *         <code>S3ObjectBucketKey</code> object with the bucket and key
	 *         initialized from the <code>entry</code>.
	 * @Override
	 */
	public Object entryToObject(TupleInput entry) {
		// Data must be read in the same order that it was
		// originally written.
		S3Object s3Object = new S3ObjectBucketKey();

		s3Object.setBucket(entry.readString());
		s3Object.setKey(entry.readString());

		return s3Object;
	}

	/**
	 * Converts an S3Object into a entry buffer.
	 * 
	 * @Override
	 */
	public void objectToEntry(Object object, TupleOutput entry) {
		S3Object s3Object = (S3Object) object;

		entry.writeString(s3Object.getBucket());
		entry.writeString(s3Object.getKey());
	}
}
