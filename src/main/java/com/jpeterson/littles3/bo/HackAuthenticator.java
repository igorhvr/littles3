package com.jpeterson.littles3.bo;

import javax.servlet.http.HttpServletRequest;

import com.jpeterson.littles3.S3ObjectRequest;

/**
 * Simple Authenticator used for development.
 * 
 * @author Jesse Peterson
 */
public class HackAuthenticator implements Authenticator {
	public HackAuthenticator() {

	}

	public CanonicalUser authenticate(HttpServletRequest req,
			S3ObjectRequest s3Request) throws AuthenticatorException {
		String username = req.getHeader("x-hack-user");
		if (username != null) {
			System.out.println("HACK! USING USERNAME FROM HEADER: " + username);
			return new CanonicalUser(username);
		} else {
			throw new InvalidSecurityException(
					"Authentication header 'x-hack-user' not provided");
		}
	}
}
