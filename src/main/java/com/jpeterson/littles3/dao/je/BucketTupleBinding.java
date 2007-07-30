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

import java.util.Date;
import java.util.Enumeration;

import com.jpeterson.littles3.bo.Acp;
import com.jpeterson.littles3.bo.AllUsersGroup;
import com.jpeterson.littles3.bo.AuthenticatedUsersGroup;
import com.jpeterson.littles3.bo.Bucket;
import com.jpeterson.littles3.bo.CanonicalUser;
import com.jpeterson.littles3.bo.Grantee;
import com.jpeterson.littles3.bo.ResourcePermission;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class BucketTupleBinding extends TupleBinding {

	/**
	 * Grantee types
	 */
	private static final int GRANTEE_CANONICAL_USER = 1;

	private static final int GRANTEE_ALL_USERS_GROUP = 2;

	private static final int GRANTEE_AUTHENTICATED_USERS_GROUP = 3;

	public BucketTupleBinding() {
		super();
	}

	@Override
	public Object entryToObject(TupleInput entry) {
		Bucket bucket = new Bucket();
		Acp acp;

		// Data must be read in the same order that it was
		// originally written.

		bucket.setName(entry.readString());
		bucket.setCreated(new Date(entry.readLong()));

		acp = new Acp();
		bucket.setAcp(acp);

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

		return bucket;
	}

	@Override
	public void objectToEntry(Object object, TupleOutput entry) {
		Bucket bucket = (Bucket) object;
		Acp acp;
		Grantee grantee;
		CanonicalUser user;

		// Data must be read in the same order that it was
		// originally written.

		entry.writeString(bucket.getName());
		entry.writeLong(bucket.getCreated().getTime());

		acp = bucket.getAcp();

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
