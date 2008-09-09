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
import java.util.Enumeration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jpeterson.littles3.bo.Acp;
import com.jpeterson.littles3.bo.AllUsersGroup;
import com.jpeterson.littles3.bo.AuthenticatedUsersGroup;
import com.jpeterson.littles3.bo.CanonicalUser;
import com.jpeterson.littles3.bo.FileS3Object;
import com.jpeterson.littles3.bo.Grantee;
import com.jpeterson.littles3.bo.ResourcePermission;
import com.jpeterson.littles3.bo.S3Object;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class FileS3ObjectTupleBinding extends TupleBinding {
	private Log logger;

	/**
	 * Grantee types
	 */
	private static final int GRANTEE_CANONICAL_USER = 1;

	private static final int GRANTEE_ALL_USERS_GROUP = 2;

	private static final int GRANTEE_AUTHENTICATED_USERS_GROUP = 3;

	public FileS3ObjectTupleBinding() {
		super();
		logger = LogFactory.getLog(this.getClass());
	}

	@Override
	public Object entryToObject(TupleInput entry) {
		String bucket;
		String key;
		URL storageUrl;
		Acp acp;

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

		acp = new Acp();
		s3Object.setAcp(acp);

		acp.setOwner(new CanonicalUser(entry.readString()));

		int size = entry.readInt();

		for (int i = 0; i < size; i++) {
			int granteeType = entry.readInt();
			Grantee grantee;

			switch (granteeType) {
			case GRANTEE_CANONICAL_USER:
				grantee = new CanonicalUser(entry.readString());
				break;

			case GRANTEE_ALL_USERS_GROUP:
				grantee = AllUsersGroup.getInstance();
				break;

			case GRANTEE_AUTHENTICATED_USERS_GROUP:
				grantee = AuthenticatedUsersGroup.getInstance();
				break;

			default:
				throw new IllegalArgumentException("Unsupported grantee type: "
						+ granteeType);
			}

			acp.grant(grantee, entry.readString());
		}

		return s3Object;
	}

	@Override
	public void objectToEntry(Object object, TupleOutput entry) {
		S3Object s3Object = (S3Object) object;
		Acp acp;
		Grantee grantee;
		CanonicalUser user;

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

		acp = s3Object.getAcp();

		user = acp.getOwner();
		entry.writeString(user.getId());

		int size = acp.size();
		entry.writeInt(size);

		Enumeration grants = acp.grants();
		for (int i = 0; (i < size); i++) {
			if (!grants.hasMoreElements()) {
				throw new IllegalArgumentException(
						"ACP grant size doesn't match number of grants");
			}

			ResourcePermission grant = (ResourcePermission) grants
					.nextElement();
			grantee = grant.getGrantee();
			if (grantee instanceof CanonicalUser) {
				entry.writeInt(GRANTEE_CANONICAL_USER);
				entry.writeString(((CanonicalUser) grantee).getId());
			} else if (grantee instanceof AllUsersGroup) {
				entry.writeInt(GRANTEE_ALL_USERS_GROUP);
			} else if (grantee instanceof AuthenticatedUsersGroup) {
				entry.writeInt(GRANTEE_AUTHENTICATED_USERS_GROUP);
			} else {
				throw new IllegalArgumentException("Unsupport grantee"
						+ grantee);
			}
			entry.writeString(grant.getActions());
		}
	}
}
