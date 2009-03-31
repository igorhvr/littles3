package com.jpeterson.littles3.bo;

public interface UserDirectory {
	public String getAwsSecretAccessKey(String awsAccessKeyId);

	public CanonicalUser getCanonicalUser(String awsAccessKeyId);
}
