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

package com.jpeterson.util.etag;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;

/**
 * Based on the Apache httpd <a
 * href="http://httpd.apache.org/docs/2.0/mod/core.html#fileetag">FileETag</a>
 * directive. This object can calculate an ETag value for a file based on the
 * file's last modified time and/or its size. Since this is Java, the Apache
 * httpd concept of an inode isn't supported in the calculation, in order to be
 * consistent across different underlying implementations.
 * 
 * @author Jesse Peterson
 */
public class FileETag implements ETag {
	/**
	 * Flag for the file last modified metadata value.
	 */
	public static final int FLAG_MTIME = 0x01;

	/**
	 * Flag for the file size metadata value.
	 */
	public static final int FLAG_SIZE = 0x02;

	/**
	 * Flag to indicate to use the file's content.
	 */
	public static final int FLAG_CONTENT = 0x04;

	/**
	 * Default flag values.
	 */
	public static final int DEFAULT_FLAGS = FLAG_MTIME | FLAG_SIZE;

	private int flags = DEFAULT_FLAGS;

	/**
	 * Empty constructor.
	 */
	public FileETag() {
		super();
	}

	/**
	 * Get the flags used to control what file metadata is used to calculate the
	 * ETag.
	 * 
	 * @return The 'OR'ed value of all active flags. You can use the return
	 *         value 'AND'ed with a particular flag and see if it is != 0 to
	 *         determine if the flag is 'set'.
	 * @see #FLAG_MTIME
	 * @see #FLAG_SIZE
	 */
	public int getFlags() {
		return flags;
	}

	/**
	 * Set the flags used to control what file metadata is used to calculate the
	 * ETag.
	 * 
	 * @param flags
	 *            The 'OR'ed value of all active flags.
	 */
	public void setFlags(int flags) {
		this.flags = flags;
	}

	/**
	 * Calculates the ETag for a file based on file metadata. The file metadata
	 * used in the ETag calculation is set via <code>setFlags(int)</code>.
	 * 
	 * @param o
	 *            The <code>File</code> to calculate the ETag for.
	 * @return The ETag value. May be <code>null</code> if an ETag can not be
	 *         calculated.
	 * @see #setFlags(int)
	 */
	public String calculate(Object o) {
		StringBuffer buffer = new StringBuffer();
		MessageDigest messageDigest;
		byte[] digest;
		File file;

		try {
			file = (File) o;
		} catch (ClassCastException e) {
			System.err.println("Unable to cast the object to a File: " + o);
			return null;
		}

		if (!file.exists()) {
			System.err.println("Unable to calculate ETag; file doesn't exist: "
					+ file);
			return null;
		}

		try {
			messageDigest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}

		// order of the flags is important! changing the order will change the
		// ETag value

		if ((flags & FLAG_CONTENT) != 0) {
			try {
				DigestInputStream digestInputStream = new DigestInputStream(
						new FileInputStream(file), messageDigest);
				byte[] b = new byte[1024];

				while (digestInputStream.read(b, 0, b.length) > 0) {
					// adding content to the MessageDigest
				}
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}

		if ((flags & FLAG_MTIME) != 0) {
			buffer.append(file.lastModified());
		}

		if ((flags & FLAG_SIZE) != 0) {
			buffer.append(file.length());
		}

		digest = messageDigest.digest(buffer.toString().getBytes());

		return new String(Hex.encodeHex(digest));
	}
}
