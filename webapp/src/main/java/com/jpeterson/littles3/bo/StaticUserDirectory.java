package com.jpeterson.littles3.bo;

/**
 * Static implementation of <code>UserDirectory</code> that will return the same
 * AWSSecretAccessKey for any AWSAccessKeyId.
 * 
 * @author Jesse Peterson
 */
public class StaticUserDirectory implements UserDirectory {

	public StaticUserDirectory() {

	}

	public String getAwsSecretAccessKey(String awsAccessKeyId) {
		return "pqi9yert";
	}

	public CanonicalUser getCanonicalUser(String awsAccessKeyId) {
		CanonicalUser user;

		user = new CanonicalUser("jep");
		user.setDisplayName("Jesse Peterson");
		return user;
	}
}
