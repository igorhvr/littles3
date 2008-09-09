package com.jpeterson.littles3.bo;

import javax.servlet.http.HttpServletRequest;

import com.jpeterson.littles3.S3ObjectRequest;

/**
 * S3 authentication provider.
 * 
 * @author Jesse Peterson
 */
public interface Authenticator {
	/**
	 * Authenticates the request.
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
			S3ObjectRequest s3Request) throws AuthenticatorException;
}
