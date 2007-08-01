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

package com.jpeterson.littles3;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.security.AccessControlException;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.web.servlet.FrameworkServlet;

import com.jpeterson.littles3.bo.Acp;
import com.jpeterson.littles3.bo.AllUsersGroup;
import com.jpeterson.littles3.bo.AuthenticatedUsersGroup;
import com.jpeterson.littles3.bo.Bucket;
import com.jpeterson.littles3.bo.CanonicalUser;
import com.jpeterson.littles3.bo.ResourcePermission;
import com.jpeterson.littles3.bo.S3Object;
import com.jpeterson.littles3.dao.je.JeCentral;
import com.jpeterson.littles3.service.BucketAlreadyExistsException;
import com.jpeterson.littles3.service.BucketNotEmptyException;
import com.jpeterson.littles3.service.StorageService;
import com.jpeterson.util.etag.ETag;
import com.jpeterson.util.etag.FileETag;
import com.jpeterson.util.http.Range;
import com.jpeterson.util.http.RangeFactory;
import com.jpeterson.util.http.RangeInputStream;
import com.jpeterson.util.http.RangeSet;

public class StorageEngine extends FrameworkServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * HTTP Header that can be used to override the actual method. Useful in
	 * situations, for instance, where a firewall only allows "GET" AND "POST"
	 * methods, but you need to use "PUT" and "DELETE" methods. You can specify
	 * this HTTP header and the appropriate value.
	 */
	public static final String HEADER_HTTP_METHOD_OVERRIDE = "X-HTTP-Method-Override";

	private Log logger;

	/**
	 * Default configuration file name.
	 */
	public static final String DEFAULT_CONFIGURATION = "StorageEngine.properties";

	/**
	 * Configuration property defining the HTTP Host that this engine is
	 * serving.
	 */
	public static final String CONFIG_HOST = "host";

	private ETag eTag;

	private Configuration configuration;

	private static SimpleDateFormat iso8601 = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

	private static TimeZone utc = TimeZone.getTimeZone("UTC");

	static {
		iso8601.setTimeZone(utc);
	}

	private static final String HEADER_X_AMZ_ACL = "x-amz-acl";

	private static final String ACL_PRIVATE = "private";

	private static final String ACL_PUBLIC_READ = "public-read";

	private static final String ACL_PUBLIC_READ_WRITE = "public-read-write";

	private static final String ACL_AUTHENTICATED_READ = "authenticated-read";

	/**
	 * Basic constructor. Initializes the logger.
	 */
	public StorageEngine() {
		super();
		logger = LogFactory.getLog(this.getClass());
	}

	/**
	 * Initialize the servlet.
	 * 
	 * @throws ServletException
	 *             if an exception occurs that interrupts the servlet's normal
	 *             operation
	 */
	public void initFrameworkServlet() throws ServletException {
		FileETag eTag = new FileETag();
		eTag.setFlags(FileETag.FLAG_CONTENT);
		setETag(eTag);

		if (false) {
			JeCentral jeCentral = (JeCentral) getWebApplicationContext()
					.getBean("jeCentral");
			if (jeCentral != null) {
				try {
					jeCentral.init();
				} catch (Exception e) {
					e.printStackTrace();
					throw new ServletException("Unable to open JE environment",
							e);
				}
			}
		}

		try {
			configuration = new PropertiesConfiguration(DEFAULT_CONFIGURATION);
		} catch (ConfigurationException e) {
			logger
					.warn("Unable to load default properties-based configuration: "
							+ DEFAULT_CONFIGURATION);
			configuration = new PropertiesConfiguration();
		}
	}

	public void destroy() {
		JeCentral jeCentral = (JeCentral) getWebApplicationContext().getBean(
				"jeCentral");

		if (jeCentral != null) {
			try {
				jeCentral.destroy();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		super.destroy();
	}

	/**
	 * Get the ETag calculator.
	 * 
	 * @return The ETag calculator.
	 */
	public ETag getETag() {
		return eTag;
	}

	/**
	 * Set the ETag calculator.
	 * 
	 * @param eTag
	 *            The ETag calculator.
	 */
	public void setETag(ETag eTag) {
		this.eTag = eTag;
	}

	/**
	 * Subclasses must implement this method to do the work of request handling,
	 * receiving a centralized callback for GET, POST, PUT and DELETE.
	 * 
	 * @param request
	 *            current HTTP request
	 * @param response
	 *            current HTTP response
	 * @throws Exception
	 *             in case of any kind of processing failure
	 */
	protected void doService(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		String method;

		method = getMethod(request);
		logger.debug("Method: " + method);

		if (method.equalsIgnoreCase("GET")) {
			// read
			methodGet(request, response);
		} else if (method.equalsIgnoreCase("HEAD")) {
			// headers
			methodHead(request, response);
		} else if (method.equalsIgnoreCase("PUT")) {
			// create
			methodPut(request, response);
		} else if (method.equalsIgnoreCase("DELETE")) {
			// remove
			methodDelete(request, response);
		}
	}

	/**
	 * Returns the HTTP method of the request. Implements logic to allow an
	 * "override" method, specified by the header
	 * <code>HEADER_HTTP_METHOD_OVERRIDE</code>. If the override method is
	 * provided, it takes precedence over the actual method derived from
	 * <code>request.getMethod()</code>.
	 * 
	 * @param request
	 *            The request being processed.
	 * @return The method of the request.
	 * @see #HEADER_HTTP_METHOD_OVERRIDE
	 */
	public static String getMethod(HttpServletRequest request) {
		String method;

		method = request.getHeader(HEADER_HTTP_METHOD_OVERRIDE);

		if (method == null) {
			method = request.getMethod();
		}

		return method;
	}

	/**
	 * Metadata
	 * 
	 * @param req
	 *            the request object that is passed to the servlet
	 * @param resp
	 *            the response object that the servlet uses to return the
	 *            headers to the client
	 * @throws IOException
	 *             if an input or output error occurs
	 * @throws ServletException
	 *             if the request for the HEAD could not be handled
	 */
	public void methodHead(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// write the body. the servlet container makes sure to not send the body
		// for the HEAD
		processHeadGet(req, resp);
	}

	/**
	 * Read
	 * 
	 * @param req
	 *            an HttpServletRequest object that contains the request the
	 *            client has made of the servlet
	 * @param resp
	 *            an HttpServletResponse object that contains the response the
	 *            servlet sends to the client
	 * @throws IOException
	 *             if an input or output error is detected when the servlet
	 *             handles the GET request
	 * @throws ServletException
	 *             if the request for the GET could not be handled
	 */
	public void methodGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		processHeadGet(req, resp);
	}

	/**
	 * Process HTTP HEAD and GET
	 * 
	 * @param req
	 *            an HttpServletRequest object that contains the request the
	 *            client has made of the servlet
	 * @param resp
	 *            an HttpServletResponse object that contains the response the
	 *            servlet sends to the client
	 * @throws IOException
	 *             if an input or output error is detected when the servlet
	 *             handles the GET request
	 * @throws ServletException
	 *             if the request for the GET could not be handled
	 */
	@SuppressWarnings("unchecked")
	public void processHeadGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		if (logger.isDebugEnabled()) {
			logger.debug("Context path: " + req.getContextPath());
			logger.debug("Path info: " + req.getPathInfo());
			logger.debug("Path translated: " + req.getPathTranslated());
			logger.debug("Query string: " + req.getQueryString());
			logger.debug("Request URI: " + req.getRequestURI());
			logger.debug("Request URL: " + req.getRequestURL());
			logger.debug("Servlet path: " + req.getServletPath());
			logger.debug("Servlet name: " + this.getServletName());

			for (Enumeration headerNames = req.getHeaderNames(); headerNames
					.hasMoreElements();) {
				String headerName = (String) headerNames.nextElement();
				String headerValue = req.getHeader(headerName);
				logger.debug("Header- " + headerName + ": " + headerValue);
			}
		}

		try {
			S3ObjectRequest or = S3ObjectRequest.create(req, configuration
					.getString(CONFIG_HOST));

			if (or.getKey() != null) {
				S3Object s3Object;
				StorageService storageService;

				try {
					storageService = (StorageService) getWebApplicationContext()
							.getBean("storageService");
					s3Object = storageService.load(or.getBucket(), or.getKey());

					if (s3Object == null) {
						resp.sendError(HttpServletResponse.SC_NOT_FOUND,
								"NoSuchKey");
						return;
					}
				} catch (DataAccessException e) {
					resp.sendError(HttpServletResponse.SC_NOT_FOUND,
							"NoSuchKey");
					return;
				}

				if (req.getParameter("acl") != null) {
					// retrieve access control policy
					String response;
					Acp acp = s3Object.getAcp();

					try {
						acp.canRead(or.getRequestor());
					} catch (AccessControlException e) {
						resp.sendError(HttpServletResponse.SC_FORBIDDEN,
								"AccessDenied");
						return;
					}

					response = Acp.encode(acp);
					resp.setContentLength(response.length());
					resp.setContentType("application/xml");
					resp.setStatus(HttpServletResponse.SC_OK);

					Writer out = resp.getWriter();
					out.write(response);
					out.flush(); // commit response
					out.close();
					out = null;
				} else {
					// retrieve object
					InputStream in = null;
					OutputStream out = null;
					byte[] buffer = new byte[4096];
					int count;
					String value;

					try {
						s3Object.canRead(or.getRequestor());
					} catch (AccessControlException e) {
						resp.sendError(HttpServletResponse.SC_FORBIDDEN,
								"AccessDenied");
						return;
					}

					// headers
					resp.setContentType(s3Object.getContentType());
					if ((value = s3Object.getContentDisposition()) != null) {
						resp.setHeader("Content-Disposition", value);
					}
					// TODO: set the Content-Range, if request includes Range
					// TODO: add "x-amz-meta-" metadata
					// TODO: add "x-amz-missing-meta", if any

					resp.setDateHeader("Last-Modified", s3Object
							.getLastModified());
					if ((value = s3Object.getETag()) != null) {
						resp.setHeader("ETag", value);
					}
					if ((value = s3Object.getContentMD5()) != null) {
						resp.setHeader("Content-MD5", value);
					}
					if ((value = s3Object.getContentDisposition()) != null) {
						resp.setHeader("Content-Disposition", value);
					}
					resp.setHeader("Accept-Ranges", "bytes");

					String rangeRequest = req.getHeader("Range");

					if (rangeRequest != null) {
						// request for a range
						RangeSet rangeSet = RangeFactory
								.processRangeHeader(rangeRequest);

						// set content length
						rangeSet.resolve(s3Object.getContentLength());

						if (rangeSet.size() > 1) {
							// requires multi-part response
							// TODO: implement
							resp
									.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
						}

						Range[] ranges = (Range[]) rangeSet
								.toArray(new Range[0]);

						resp.setHeader("Content-Range", formatRangeHeaderValue(
								ranges[0], s3Object.getContentLength()));
						resp.setHeader("Content-Length", Long.toString(rangeSet
								.getLength()));

						in = new RangeInputStream(s3Object.getInputStream(),
								ranges[0]);
						resp.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
					} else {
						// request for entire content
						// Used instead of resp.setContentLength((int)); because
						// Amazon
						// limit is 5 gig, which is bigger than an int
						resp.setHeader("Content-Length", Long.toString(s3Object
								.getContentLength()));

						in = s3Object.getInputStream();
						resp.setStatus(HttpServletResponse.SC_OK);
					}

					// body
					out = resp.getOutputStream();

					while ((count = in.read(buffer, 0, buffer.length)) > 0) {
						out.write(buffer, 0, count);
					}

					out.flush(); // commit response
					out.close();
					out = null;
				}
				return;
			} else if (or.getBucket() != null) {
				// operation on a bucket
				StorageService storageService;
				String prefix;
				String marker;
				int maxKeys = Integer.MAX_VALUE;
				String delimiter;
				String response;
				String value;

				storageService = (StorageService) getWebApplicationContext()
						.getBean("storageService");

				if (req.getParameter("acl") != null) {
					// retrieve access control policy
					Acp acp;

					try {
						acp = storageService.loadBucket(or.getBucket())
								.getAcp();
					} catch (DataAccessException e) {
						resp.sendError(HttpServletResponse.SC_NOT_FOUND,
								"NoSuchBucket");
						return;
					}

					try {
						acp.canRead(or.getRequestor());
					} catch (AccessControlException e) {
						resp.sendError(HttpServletResponse.SC_FORBIDDEN,
								"AccessDenied");
						return;
					}

					response = Acp.encode(acp);
					resp.setContentLength(response.length());
					resp.setContentType("application/xml");
					resp.setStatus(HttpServletResponse.SC_OK);

					Writer out = resp.getWriter();
					out.write(response);
					out.flush(); // commit response
					out.close();
					out = null;
				} else {
					prefix = req.getParameter("prefix");
					if (prefix == null) {
						prefix = "";
					}
					marker = req.getParameter("marker");
					value = req.getParameter("max-keys");
					if (value != null) {
						try {
							maxKeys = Integer.parseInt(value);
						} catch (NumberFormatException e) {
							logger.info("max-keys must be numeric: " + value);
						}
					}

					delimiter = req.getParameter("delimiter");

					response = storageService.listKeys(storageService
							.loadBucket(or.getBucket()), prefix, marker,
							delimiter, maxKeys, or.getRequestor());

					resp.setContentLength(response.length());
					resp.setContentType("application/xml");
					resp.setStatus(HttpServletResponse.SC_OK);

					Writer out = resp.getWriter();
					out.write(response);
				}
				return;
			} else {
				// operation on the service
				StorageService storageService;
				List buckets;

				storageService = (StorageService) getWebApplicationContext()
						.getBean("storageService");

				buckets = storageService.findBuckets("");

				StringBuffer buffer = new StringBuffer();

				buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
				buffer
						.append("<ListAllMyBucketsResult xmlns=\"http://s3.amazonaws.com/doc/2006-03-01/\">");
				buffer.append("<Owner>");
				buffer.append("<ID/>"); // TODO: implement
				buffer.append("<DisplayName/>"); // TODO: implementF
				buffer.append("</Owner>");
				buffer.append("<Buckets>");
				for (Iterator iter = buckets.iterator(); iter.hasNext();) {
					Bucket bucket = (Bucket) iter.next();
					buffer.append("<Bucket>");
					buffer.append("<Name>").append(bucket.getName()).append(
							"</Name>");
					buffer.append("<CreationDate>").append(
							iso8601.format(bucket.getCreated())).append(
							"</CreationDate>");
					buffer.append("</Bucket>");
				}
				buffer.append("</Buckets>");
				buffer.append("</ListAllMyBucketsResult>");

				resp.setContentLength(buffer.length());
				resp.setContentType("application/xml");
				resp.setStatus(HttpServletResponse.SC_OK);

				Writer out = resp.getWriter();
				out.write(buffer.toString());
				return;
			}
		} catch (IllegalArgumentException e) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "InvalidURI");
			return;
		}
	}

	/**
	 * Write
	 * 
	 * @param req
	 *            the HttpServletRequest object that contains the request the
	 *            client made of the servlet
	 * @param resp
	 *            the HttpServletResponse object that contains the response the
	 *            servlet returns to the client
	 * @throws IOException
	 *             if an input or output error occurs while the servlet is
	 *             handling the PUT request
	 * @throws ServletException
	 *             if the request for the PUT cannot be handled
	 */
	public void methodPut(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		OutputStream out = null;

		try {
			S3ObjectRequest or = S3ObjectRequest.create(req, configuration
					.getString(CONFIG_HOST));
			logger.debug("S3ObjectRequest: " + or);

			CanonicalUser requestor = or.getRequestor();

			if (or.getKey() != null) {
				String value;
				long contentLength;
				MessageDigest messageDigest = MessageDigest.getInstance("MD5");
				DigestOutputStream digestOutputStream = null;
				S3Object oldS3Object = null;
				S3Object s3Object;
				StorageService storageService;
				Bucket bucket;
				String bucketName = or.getBucket();
				String key = or.getKey();

				if (!isValidKey(key)) {
					resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
							"KeyTooLong");
					return;
				}

				storageService = (StorageService) getWebApplicationContext()
						.getBean("storageService");

				// make sure requestor can "WRITE" to the bucket
				try {
					bucket = storageService.loadBucket(bucketName);
				} catch (DataAccessException e) {
					resp.sendError(HttpServletResponse.SC_NOT_FOUND,
							"NoSuchBucket");
					return;
				}

				try {
					oldS3Object = storageService.load(bucket.getName(), key);
				} catch (DataRetrievalFailureException e) {
					// ignore
				}

				// create a new S3Object for this request to store an object
				try {
					s3Object = storageService.createS3Object(bucket, key,
							requestor);
				} catch (DataAccessException e) {
					resp.sendError(HttpServletResponse.SC_NOT_FOUND,
							"NoSuchBucket");
					return;
				} catch (AccessControlException e) {
					resp.sendError(HttpServletResponse.SC_FORBIDDEN,
							"AccessDenied");
					return;
				}

				out = s3Object.getOutputStream();
				digestOutputStream = new DigestOutputStream(out, messageDigest);

				// Used instead of req.getContentLength(); because Amazon
				// limit is 5 gig, which is bigger than an int
				value = req.getHeader("Content-Length");
				if (value == null) {
					resp.sendError(HttpServletResponse.SC_LENGTH_REQUIRED,
							"MissingContentLength");
					return;
				}
				contentLength = Long.valueOf(value).longValue();

				if (contentLength > 5368709120L) {
					resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
							"EntityTooLarge");
					return;
				}

				long written = 0;
				int count;
				byte[] b = new byte[4096];
				ServletInputStream in = req.getInputStream();

				while (((count = in.read(b, 0, b.length)) > 0)
						&& (written < contentLength)) {
					digestOutputStream.write(b, 0, count);
					written += count;
				}
				digestOutputStream.flush();

				if (written != contentLength) {
					// transmission truncated
					if (out != null) {
						out.close();
						out = null;
					}
					if (digestOutputStream != null) {
						digestOutputStream.close();
						digestOutputStream = null;
					}
					// clean up
					storageService.remove(s3Object, requestor);
					resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
							"IncompleteBody");
					return;
				}

				s3Object.setContentDisposition(req
						.getHeader("Content-Disposition"));
				s3Object.setContentLength(contentLength);
				s3Object.setContentMD5(req.getHeader("Content-MD5"));
				value = req.getContentType();
				logger.debug("Put - Content-Type: " + value);
				if (value == null) {
					value = S3Object.DEFAULT_CONTENT_TYPE;
				}
				s3Object.setContentType(value);
				logger.debug("Put - get content-type: "
						+ s3Object.getContentType());
				s3Object.setLastModified(System.currentTimeMillis());

				// calculate ETag, hex encoding of MD5
				value = new String(Hex.encodeHex(digestOutputStream
						.getMessageDigest().digest()));
				resp.setHeader("ETag", value);
				s3Object.setETag(value);

				grantCannedAccessPolicies(req, s3Object.getAcp(), requestor);

				// NOTE: This could be reengineered to have a two-phase commit.
				if (oldS3Object != null) {
					storageService.remove(oldS3Object, requestor);
				}
				storageService.store(s3Object, requestor);
			} else if (or.getBucket() != null) {
				StorageService storageService;
				Bucket bucket;

				// validate bucket
				String bucketName = or.getBucket();

				if (!isValidBucketName(bucketName)) {
					resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
							"InvalidBucketName");
					return;
				}

				storageService = (StorageService) getWebApplicationContext()
						.getBean("storageService");

				try {
					bucket = storageService.createBucket(bucketName, requestor);
				} catch (BucketAlreadyExistsException e) {
					resp.sendError(HttpServletResponse.SC_CONFLICT,
							"BucketAlreadyExists");
					return;
				}

				grantCannedAccessPolicies(req, bucket.getAcp(), requestor);

				storageService.storeBucket(bucket, requestor);
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			logger.error("Unable to use MD5", e);
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"InternalError");
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (out != null) {
				out.close();
				out = null;
			}
		}
	}

	/**
	 * Delete
	 * 
	 * @param req
	 *            the HttpServletRequest object that contains the request the
	 *            client made of the servlet
	 * @param resp
	 *            the HttpServletResponse object that contains the response the
	 *            servlet returns to the client
	 * @param IOException
	 *            if an input or output error occurs while the servlet is
	 *            handling the DELETE request
	 * @param ServletException
	 *            if the request for the DELETE cannot be handled
	 */
	public void methodDelete(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		S3ObjectRequest or = S3ObjectRequest.create(req, configuration
				.getString(CONFIG_HOST));

		logger.debug("S3ObjectRequest: " + or);

		CanonicalUser requestor = or.getRequestor();

		if (or.getKey() != null) {
			S3Object s3Object;
			StorageService storageService;

			storageService = (StorageService) getWebApplicationContext()
					.getBean("storageService");

			try {
				s3Object = storageService.load(or.getBucket(), or.getKey());
			} catch (DataRetrievalFailureException e) {
				resp.sendError(HttpServletResponse.SC_NOT_FOUND, "NoSuchKey");
				return;
			}
			storageService.remove(s3Object, requestor);

			resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
			return;
		} else if (or.getBucket() != null) {
			StorageService storageService;

			// validate bucket
			String bucketName = or.getBucket();

			storageService = (StorageService) getWebApplicationContext()
					.getBean("storageService");

			try {
				storageService.deleteBucket(storageService
						.loadBucket(bucketName), requestor);
			} catch (BucketNotEmptyException e) {
				resp.sendError(HttpServletResponse.SC_CONFLICT,
						"BucketNotEmpty");
				return;
			}

			resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
			return;
		}

		resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
	}

	public static String formatRangeHeaderValue(Range range, long absoluteLength) {
		StringBuffer buffer = new StringBuffer();

		buffer.append("bytes ");
		buffer.append(range.getStart());
		buffer.append("-");
		buffer.append(range.getEnd());
		buffer.append("/");
		buffer.append(absoluteLength);

		return buffer.toString();
	}

	/**
	 * Validates a bucket name. Bucket names can only contain alphanumeric
	 * characters, underscore (_), period (.), and dash(-). Bucket names must be
	 * between 3 and 255 characters long.
	 * 
	 * @param name
	 *            The name of the bucket.
	 * @return <code>True</code> if the bucket name is valid,
	 *         <code>false</code> otherwise.
	 */
	public static boolean isValidBucketName(String name) {
		// alphanumeric, underscore, period, dash. between 3-255 characters
		if (name == null) {
			return false;
		}

		char[] chars = name.toCharArray();

		if ((chars.length < 3) || (chars.length > 255)) {
			return false;
		}

		for (int i = 0; i < chars.length; i++) {
			if ((chars[i] >= 'a') && (chars[i] <= 'z')) {
				return true;
			}

			if ((chars[i] >= 'A') && (chars[i] <= 'Z')) {
				return true;
			}

			if ((chars[i] >= '0') && (chars[i] <= '9')) {
				return true;
			}

			if (chars[i] == '_') {
				return true;
			}

			if (chars[i] == '.') {
				return true;
			}

			if (chars[i] == '-') {
				return true;
			}
		}

		return false;
	}

	/**
	 * Validates a key. A key can be at most 1024 bytes long.
	 * 
	 * @param name
	 *            The key.
	 * @return <code>True</code> if the key is valid, <code>false</code>
	 *         otherwise.
	 */
	public static boolean isValidKey(String name) {
		if (name.length() > 1024) {
			return false;
		}

		return true;
	}

	/**
	 * Grant the canned access policies for buckets or objects as part of a
	 * <code>PUT</code> operation. The canned access policies are specified in
	 * the Amazon S3 Developer Guide.
	 * 
	 * @param acp
	 *            The Access Control Policy to grant the canned access policies
	 *            to.
	 * @param owner
	 *            The principal making the request who is the owner of the
	 *            resource.
	 */
	public static void grantCannedAccessPolicies(HttpServletRequest req,
			Acp acp, CanonicalUser owner) {
		String xAmzAcl;

		xAmzAcl = req.getHeader(HEADER_X_AMZ_ACL);

		if ((xAmzAcl == null) || (xAmzAcl.equals(ACL_PRIVATE))) {
			acp.grant(owner, ResourcePermission.ACTION_FULL_CONTROL);
		} else if (xAmzAcl.equals(ACL_PUBLIC_READ)) {
			acp.grant(owner, ResourcePermission.ACTION_FULL_CONTROL);
			acp.grant(AllUsersGroup.getInstance(),
					ResourcePermission.ACTION_READ);
		} else if (xAmzAcl.equals(ACL_PUBLIC_READ_WRITE)) {
			acp.grant(owner, ResourcePermission.ACTION_FULL_CONTROL);
			acp.grant(AllUsersGroup.getInstance(),
					ResourcePermission.ACTION_READ);
			acp.grant(AllUsersGroup.getInstance(),
					ResourcePermission.ACTION_WRITE);
		} else if (xAmzAcl.equals(ACL_AUTHENTICATED_READ)) {
			acp.grant(owner, ResourcePermission.ACTION_FULL_CONTROL);
			acp.grant(AuthenticatedUsersGroup.getInstance(),
					ResourcePermission.ACTION_READ);
		}
	}
}