package com.jpeterson.littles3.bo;

/**
 * Indicates that the S3 request timestamp is not within 15 minutes of the
 * server's system time.
 * 
 * @author Jesse Peterson
 */
public class RequestTimeTooSkewedException extends AuthenticatorException {

	/**
	 * Basic implementation. You must manually update as necessary.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Construct a new exception with <code>null</code> as its detailed
	 * message.
	 */
	public RequestTimeTooSkewedException() {
		super();
	}

	/**
	 * Constructs a new exception with the specified detail message.
	 * 
	 * @param message
	 *            the detail message.
	 */
	public RequestTimeTooSkewedException(String message) {
		super(message);
	}

	/**
	 * Constructs a new exception with the specified detail message and cause.
	 * 
	 * @param message
	 *            the detail message.
	 * @param cause
	 *            the cause.
	 */
	public RequestTimeTooSkewedException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a new exception with the specified cause and detail message of
	 * <code>(cause==null ? null : cause.toString())</code>.
	 * 
	 * @param cause
	 *            the cause.
	 */
	public RequestTimeTooSkewedException(Throwable cause) {
		super(cause);
	}
}
