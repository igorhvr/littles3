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

package com.jpeterson.littles3.bo;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;

import com.jpeterson.littles3.S3ObjectRequest;

/**
 * Performs Amazon S3 Authentication.
 * 
 * @author Jesse Peterson
 */
public class S3Authenticator implements Authenticator {
	private static final String HEADER_AUTHORIZATION = "Authorization";

	private static final String AUTHORIZATION_TYPE = "AWS";

	private static final String AWS_ACCESS_KEY_ID = "0PN5J17HBGZHT7JJ3X82";

	private static final String AWS_SECRET_ACCESS_KEY = "uV3F3YluFJax1cknvbcGwgjvx4QpvB+leU8dUj2o";

	/**
	 * Empty constructor.
	 */
	public S3Authenticator() {

	}

	/**
	 * Authenticate the request using the prescribed Amazon S3 authentication
	 * mechanisms.
	 * 
	 * @param req
	 *            The original HTTP request.
	 * @param s3Request
	 *            The S3 specific information for authenticating the request.
	 * @return The authenticated <code>CanonicalUser</code> making the
	 *         request.
	 * @throws RequestTimeTooSkewedException
	 *             Thrown if the request timestamp is outside of the allotted
	 *             timeframe.
	 */
	public CanonicalUser authenticate(HttpServletRequest req,
			S3ObjectRequest s3Request) throws AuthenticatorException {
		// check to see if anonymous request
		String authorization = req.getHeader(HEADER_AUTHORIZATION);

		if (authorization == null) {
			return new CanonicalUser(CanonicalUser.ID_ANONYMOUS);
		}

		// attempting to be authenticated request

		if (false) {
			// check timestamp of request
			Date timestamp = s3Request.getTimestamp();
			if (timestamp == null) {
				throw new RequestTimeTooSkewedException("No timestamp provided");
			}

			GregorianCalendar calendar = new GregorianCalendar();
			Date now = calendar.getTime();
			calendar.add(Calendar.MINUTE, 15);
			Date maximumDate = calendar.getTime();
			calendar.add(Calendar.MINUTE, -30);
			Date minimumDate = calendar.getTime();

			if (timestamp.before(minimumDate)) {
				throw new RequestTimeTooSkewedException("Timestamp ["
						+ timestamp + "] too old. System time: " + now);
			}

			if (timestamp.after(maximumDate)) {
				throw new RequestTimeTooSkewedException("Timestamp ["
						+ timestamp + "] too new. System time: " + now);
			}
		}

		// authenticate request
		String[] fields = authorization.split(" ");

		if (fields.length != 2) {
			throw new InvalidSecurityException(
					"Unsupported authorization format");
		}

		if (!fields[0].equals(AUTHORIZATION_TYPE)) {
			throw new InvalidSecurityException(
					"Unsupported authorization type: " + fields[0]);
		}

		String[] keys = fields[1].split(":");

		if (keys.length != 2) {
			throw new InvalidSecurityException(
					"Invalid AWSAccesskeyId:Signature");
		}

		String accessKeyId = keys[0];
		String signature = keys[1];
		String secretAccessKey = getSecretAccessKey(accessKeyId);
		String calculatedSignature;

		try {
			SecretKey key = new SecretKeySpec(secretAccessKey.getBytes(),
					"HmacSHA1");
			Mac m = Mac.getInstance("HmacSHA1");
			m.init(key);
			m.update(s3Request.getStringToSign().getBytes());
			byte[] mac = m.doFinal();
			calculatedSignature = new String(Base64.encodeBase64(mac));
		} catch (NoSuchAlgorithmException e) {
			throw new InvalidSecurityException(e);
		} catch (InvalidKeyException e) {
			throw new InvalidSecurityException(e);
		}

		System.out.println("-----------------");
		System.out.println("signature: " + signature);
		System.out.println("calculatedSignature: " + calculatedSignature);
		System.out.println("-----------------");

		if (calculatedSignature.equals(signature)) {
			// authenticated!
			return new CanonicalUser(accessKeyId);
		} else {
			throw new SignatureDoesNotMatchException(
					"Provided signature doesn't match calculated value");
		}
	}

	/**
	 * Get the secret access key for the access key id.
	 * 
	 * @param accessKeyId
	 *            The access key id of the secret access key to retrieve.
	 * @return The secret access key for the access key id.
	 * @throws InvalidAccessKeyIdException
	 *             The provided access key id is not found.
	 */
	private String getSecretAccessKey(String accessKeyId)
			throws InvalidAccessKeyIdException {
		// TODO: add a real implementation
		if (AWS_ACCESS_KEY_ID.equals(accessKeyId)) {
			return AWS_SECRET_ACCESS_KEY;
		}

		throw new InvalidAccessKeyIdException("Invalid access key id: "
				+ accessKeyId);
	}
}
