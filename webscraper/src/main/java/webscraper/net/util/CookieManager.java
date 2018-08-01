package webscraper.net.util;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.logging.Logger;
import java.text.*;

/**
 * CookieManager is a simple utilty for handling cookies when working with
 * java.net.URL and java.net.URLConnection objects.
 * 
 * 
 * Cookiemanager cm = new CookieManager(); URL url = new
 * URL("http://www.hccp.org/test/cookieTest.jsp");
 * 
 * . . .
 *
 * // getting cookies: URLConnection conn = url.openConnection();
 * conn.connect();
 *
 * // setting cookies cm.storeCookies(conn);
 * cm.setCookies(url.openConnection());
 * 
 * @author Ian Brown
 * 
 **/

public class CookieManager {

	private HashMap<String, HashMap> store;

	private static final String SET_COOKIE = "Set-Cookie";
	private static final String COOKIE_VALUE_DELIMITER = ";";
	private static final String PATH = "path";
	private static final String EXPIRES = "expires";
	private static final String DATE_FORMAT = "EEE, dd-MMM-yyyy hh:mm:ss z";
	private static final String SET_COOKIE_SEPARATOR = "; ";
	private static final String COOKIE = "Cookie";

	private static final char NAME_VALUE_SEPARATOR = '=';
	private static final char DOT = '.';

	private DateFormat dateFormat;

	public CookieManager() {
		store = new HashMap<String, HashMap>();
		dateFormat = new SimpleDateFormat(DATE_FORMAT);
	}

	private static final Logger getLogger() {
		return Logger.getLogger("zero.util.net");
	}

	/**
	 * Explicitly set a cookie value for this connection
	 * 
	 * @param name
	 * @param value
	 * @param domain
	 * @param path
	 * @param expires
	 */
	public void setCookie(String name, String value, String domain, String path, String expires) {
		HashMap domainStore; // this is where we will store cookies for this domain

		// now let's check the store to see if we have an entry for this domain
		if (store.containsKey(domain)) {
			// we do, so lets retrieve it from the store
			domainStore = store.get(domain);
		} else {
			// we don't, so let's create it and put it in the store
			domainStore = new HashMap<String, HashMap>();
			store.put(domain, domainStore);
		}

		HashMap<String, String> cookie = new HashMap<String, String>();
		domainStore.put(name, cookie);
		cookie.put(name, value);
		cookie.put(PATH, path);
		cookie.put(EXPIRES, expires);
	}

	/**
	 * Retrieves and stores cookies returned by the host on the other side of the
	 * the open java.net.URLConnection.
	 *
	 * The connection MUST have been opened using the connect() method or a
	 * IOException will be thrown.
	 *
	 * @param conn
	 *            a java.net.URLConnection - must be open, or IOException will be
	 *            thrown
	 * @throws java.io.IOException
	 *             Thrown if conn is not open.
	 */
	public void storeCookies(URLConnection conn) throws IOException {

		// let's determine the domain from where these cookies are being sent
		String domain = getDomainFromHost(conn.getURL().getHost());

		HashMap<String, HashMap> domainStore; // this is where we will store cookies for this domain

		// now let's check the store to see if we have an entry for this domain
		if (store.containsKey(domain)) {
			// we do, so lets retrieve it from the store
			domainStore = store.get(domain);
		} else {
			// we don't, so let's create it and put it in the store
			domainStore = new HashMap<String, HashMap>();
			store.put(domain, domainStore);
		}

		// OK, now we are ready to get the cookies out of the URLConnection

		String headerName = null;
		for (int i = 1; (headerName = conn.getHeaderFieldKey(i)) != null; i++) {
			if (headerName.equalsIgnoreCase(SET_COOKIE)) {
				HashMap<String, String> cookie = new HashMap<String, String>();
				StringTokenizer st = new StringTokenizer(conn.getHeaderField(i), COOKIE_VALUE_DELIMITER);

				// the specification dictates that the first name/value pair
				// in the string is the cookie name and value, so let's handle
				// them as a special case:

				if (st.hasMoreTokens()) {
					String token = st.nextToken();
					String name = token.substring(0, token.indexOf(NAME_VALUE_SEPARATOR));
					String value = token.substring(token.indexOf(NAME_VALUE_SEPARATOR) + 1, token.length());
					domainStore.put(name, (HashMap) cookie);
					cookie.put(name, value);
				}

				while (st.hasMoreTokens()) {
					String token = st.nextToken();

					/**
					 * djb [07/26/2011] added this guard for Secure or HttpOnly attributes at the
					 * end of the cookie which don't follow the cookie=val format. See
					 * http://tools.ietf.org/html/rfc6265#section-4.1.2, sections 4.1.2.5 and
					 * 4.1.2.6
					 */
					int ndx = token.indexOf(NAME_VALUE_SEPARATOR);
					if (ndx >= 0) {
						cookie.put(token.substring(0, ndx).toLowerCase(), token.substring(ndx + 1, token.length()));
					}
				}
			}
		}
	}

	/**
	 * Prior to opening a URLConnection, calling this method will set all unexpired
	 * cookies that match the path or subpaths for thi underlying URL
	 *
	 * The connection MUST NOT have been opened method or an IOException will be
	 * thrown.
	 *
	 * @param conn
	 *            a java.net.URLConnection - must NOT be open, or IOException will
	 *            be thrown
	 * @param includeSession
	 *            a boolean - if true, sets all session cookies, false ignores
	 *            session cookies
	 * @throws java.io.IOException
	 *             Thrown if conn has already been opened.
	 */
	public void setCookies(URLConnection conn, boolean includeSession) throws IOException {

		// let's determine the domain and path to retrieve the appropriate cookies
		URL url = conn.getURL();
		String domain = getDomainFromHost(url.getHost());
		String path = url.getPath();

		getLogger().fine("setting cookies for domain " + domain);

		Map domainStore = (Map) store.get(domain);
		if (domainStore == null)
			return;
		StringBuffer cookieStringBuffer = new StringBuffer();

		Iterator cookieNames = domainStore.keySet().iterator();
		while (cookieNames.hasNext()) {
			String cookieName = (String) cookieNames.next();
			Map cookie = (Map) domainStore.get(cookieName);

			// check cookie to ensure path matches and cookie is not expired
			// if all is cool, add cookie to header string

			if (comparePaths((String) cookie.get(PATH), path) && isNotExpired((String) cookie.get(EXPIRES))) {
				if (includeSession || !isSession(cookie)) {
					cookieStringBuffer.append(cookieName);
					cookieStringBuffer.append("=");
					cookieStringBuffer.append((String) cookie.get(cookieName));
					if (cookieNames.hasNext())
						cookieStringBuffer.append(SET_COOKIE_SEPARATOR);

					getLogger().fine("storing session cookie " + cookieName);
				} else {
					getLogger().fine("includeSession = " + includeSession + ", ignoring session cookie " + cookieName);
				}
			}
		}
		try {
			conn.setRequestProperty(COOKIE, cookieStringBuffer.toString());
		} catch (java.lang.IllegalStateException ise) {
			IOException ioe = new IOException(
					"Illegal State! Cookies cannot be set on a URLConnection that is already connected. "
							+ "Only call setCookies(java.net.URLConnection) AFTER calling java.net.URLConnection.connect().");
			throw ioe;
		}
	}

	private String getDomainFromHost(String host) {
		if (host.indexOf(DOT) != host.lastIndexOf(DOT)) {
			return host.substring(host.indexOf(DOT) + 1);
		} else {
			return host;
		}
	}

	private boolean isSession(Map cookie) {
		String cookieExpires = (String) cookie.get(EXPIRES);
		if (cookieExpires == null)
			return true;
		else
			return false;
	}

	private boolean isNotExpired(String cookieExpires) {
		if (cookieExpires == null)
			return true;
		Date now = new Date();
		try {
			return (now.compareTo(dateFormat.parse(cookieExpires))) <= 0;
		} catch (java.text.ParseException pe) {
			pe.printStackTrace();
			return false;
		}
	}

	private boolean comparePaths(String cookiePath, String targetPath) {
		if (cookiePath == null) {
			return true;
		} else if (cookiePath.equals("/")) {
			return true;
		} else if (targetPath.regionMatches(0, cookiePath, 0, cookiePath.length())) {
			return true;
		} else {
			return false;
		}

	}

	/**
	 * Returns a string representation of stored cookies organized by domain.
	 */
	public String toString() {
		return store.toString();
	}
	
	public String toString( URL url ) {
		String domain = getDomainFromHost(url.getHost());
		String path = url.getPath();

		Map domainStore = (Map) store.get(domain);
		if (domainStore == null)
			return "";
		
		StringBuffer cookieStringBuffer = new StringBuffer();
		Iterator cookieNames = domainStore.keySet().iterator();

		while (cookieNames.hasNext()) {
			String cookieName = (String) cookieNames.next();
			Map cookie = (Map) domainStore.get(cookieName);

			// check cookie to ensure path matches and cookie is not expired
			// if all is cool, add cookie to header string

			if (comparePaths((String) cookie.get(PATH), path) && isNotExpired((String) cookie.get(EXPIRES))) {
				cookieStringBuffer.append(cookieName);
				cookieStringBuffer.append("=");
				cookieStringBuffer.append((String) cookie.get(cookieName));
				
				if (cookieNames.hasNext())
					cookieStringBuffer.append("\n");
			}
		}

		return cookieStringBuffer.toString();
	}

}