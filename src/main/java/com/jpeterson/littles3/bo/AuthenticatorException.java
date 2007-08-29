package com.jpeterson.littles3.bo;

/**
 * Unable to authenticate the request.
 * 
 * @author Jesse Peterson
 */
public abstract class AuthenticatorException extends Exception {
	/**
	 * Basic implementation. You must manually update as necessary.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Construct a new exception with <code>null</code> as its detailed
	 * message.
	 */
	public AuthenticatorException() {
		super();
	}

	/**
	 * Constructs a new exception with the specified detail message.
	 * 
	 * @param message
	 *            the detail message.
	 */
	public AuthenticatorException(String message) {
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
	public AuthenticatorException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a new exception with the specified cause and detail message of
	 * <code>(cause==null ? null : cause.toString())</code>.
	 * 
	 * @param cause
	 *            the cause.
	 */
	public AuthenticatorException(Throwable cause) {
		super(cause);
	}
}
