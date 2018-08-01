package webscraper.net.request;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.IOException;

import java.net.HttpURLConnection;

import java.util.Map;
import java.util.List;
import java.util.Iterator;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringEscapeUtils;

import webscraper.net.WebSession;

/**
 * WebRequest 
 * 
 * Forms the basis for an interaction within a WebSession.  This is a basic GET or POST
 * request which will return the response from the server.  Other classes build on this class
 * to provide specific handling of different content, e.g. submitting/receiving form data
 * or simulating Ajax requests to the server.
 * 
 **/
public class WebRequest {

	public static final int METHOD_GET = 1;
	public static final int METHOD_POST = 2;

	private WebSession session = null;
	private String url = "";
	private int method = METHOD_GET;
	private String strLastResult = "";
	private int responseCode = 200;
	private String strReferer = "";
	private String strHeaders = "";
	private String strAccept = "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";
	private boolean dumpHeaders = false;
	private boolean dumpCookies = false;

	//
	// [djb 08-11-2017] Be careful if you enable redirects. It is normally "on" by
	// default.
	// I turned it off by default in this implementation because
	// following redirects can lose cookie information that is needed in order
	// to successfully scrape some web sites. If you want to follow
	// redirects and not lose cookie info, you would need to do that manually
	// by following the 301/302 return codes and preserving cookies along the way
	// like a web browser would do
	//
	private boolean followRedirects = false;

	/**
	 * HTTPClientRequest
	 *
	 * @param session
	 *            the parent session object associated with this request
	 * @param path
	 *            the path relative to the base url in the session object
	 *
	 */
	protected WebRequest(WebSession session, String path) {
		this.session = session;

		if (!path.startsWith("/"))
			path = "/" + path;

		this.url = session.getBaseUrl() + path;
	}

	private static final Logger getLogger() {
		Logger log = Logger.getLogger("zero.util.net");
		return log;
	}
	
	public void setDumpHeaders(boolean val) {
		dumpHeaders = val;
	}

	public boolean getDumpHeaders() {
		return dumpHeaders;
	}
	
	public void setDumpCookies(boolean val) {
		dumpCookies = val;
	}

	public boolean getDumpCookies() {
		return dumpCookies;
	}
	
	public void setFollowRedirects(boolean val) {
		followRedirects = val;
	}

	public boolean getFollowRedirects() {
		return followRedirects;
	}

	public String getUrl() {
		return url;
	}

	public int getMethod() {
		return method;
	}

	public void setMethod(int method) {
		if (method != METHOD_POST && method != METHOD_GET)
			throw new IllegalArgumentException("HTTPClientRequest.setMethod: bad method");
		this.method = method;
	}

	public String getContentType() {
		return "";
	}

	public void setAccept(String str) {
		strAccept = str;
		getLogger().fine(strAccept);
	}

	public String getAccept() {
		return strAccept;
	}

	public void setReferer(String str) {
		strReferer = str;
		getLogger().fine(strReferer);
	}

	public String getReferer() {
		return strReferer;
	}

	public int getResponseCode() {
		return responseCode;
	}

	public String getLastResult() {
		return strLastResult;
	}

	private boolean storeLastResult(HttpURLConnection conn) throws IOException {
		responseCode = conn.getResponseCode();

		boolean result = false;
		String line;
		StringBuilder strResult = new StringBuilder("");

		try {
			// Get the response
			BufferedReader rd = null;
			rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));

			while ((line = rd.readLine()) != null) {
				strResult.append(line);
				strResult.append("\n");
			}			
			
			result = true;
		} catch (IOException ioe) {
			System.err.println(strHeaders);
			ioe.printStackTrace();
		}
		
		strLastResult = strResult.toString();

		return result;
	}

	/**
	 * getDecoratedUrl
	 *
	 * an opportunity to mangle the URL sent to the server. For GET requests this
	 * would be where query parameters are added to the URL before making the
	 * request. Default implementation just returns the URL
	 *
	 */
	public String getDecoratedUrl() {
		return getUrl();
	}

	/**
	 * writePostData
	 *
	 * gives subclasses an opportunity to control the output written on POST
	 * requests. The default implementatin does nothing.
	 *
	 * @param os
	 *            the output stream for this connection. Any content written here
	 *            goes as the body of request
	 *
	 */
	protected void writePostData(OutputStream os) {
	}

	private void storeHeaderFields(HttpURLConnection conn) {
		strHeaders = "";

		Map fields = conn.getHeaderFields();
		for (Iterator itset = fields.keySet().iterator(); itset.hasNext();) {
			String key = (String) itset.next();
			List field = (List) fields.get(key);

			for (Iterator it = field.iterator(); it.hasNext();) {
				String hdrItem = (String) it.next();

				/*
				 * ignore 'null' entry since this is not really a key/val header it contains the
				 * HTTP response string, e.g. 'HTTP/1.1 200 OK'
				 */
				if (key != null) {
					strHeaders = strHeaders + key + ": " + hdrItem + "\n";
				}
			}
		}

		getLogger().fine("headers=" + strHeaders);
	}

	public String getStoredHeaders() {
		return strHeaders;
	}

	protected void setHeaderFields(HttpURLConnection conn) {
		if (getReferer().length() > 0) {
			conn.setRequestProperty("Referer", getReferer());
		}

		String strContentType = getContentType();
		if (strContentType.length() > 0) {
			conn.setRequestProperty("Content-type", strContentType);
		}

		String strAccept = getAccept();
		if (strAccept.length() > 0) {
			conn.setRequestProperty("Accept", strAccept);
		}

	}

	protected void dumpHeaders(HttpURLConnection conn) {
		String headers = "";
		Map<String, List<String>> fields = conn.getRequestProperties();

		for (Iterator itset = fields.keySet().iterator(); itset.hasNext();) {
			String key = (String) itset.next();
			List field = (List) fields.get(key);

			for (Iterator it = field.iterator(); it.hasNext();) {
				String hdrItem = (String) it.next();

				/*
				 * ignore 'null' entry since this is not really a key/val header it contains the
				 * HTTP response string, e.g. 'HTTP/1.1 200 OK'
				 */
				if (key != null) {
					headers = headers + key + ": " + hdrItem + "\n";
				}
			}
		}

		System.out.println("<== Begin Request Headers ==>");
		System.out.println(headers);
		System.out.println("<== End Request Headers ==>");
	}
	
	protected void dumpCookies(HttpURLConnection conn) {
		System.out.println("<== Start Cookies: ==>");
		System.out.println(session.getCookieManager().toString(conn.getURL()));
		System.out.println("<== End Cookies ==>");
	}

	/**
	 * makes the HTTP request, storing the results in strLastResult
	 * 
	 * @param strUrl
	 *            - the url to request
	 * @return true if successful, false if an error occurred
	 */
	public boolean connect() {
		boolean bResult = false;

		try {

			String url = getDecoratedUrl();

			HttpURLConnection conn = session.getConnection(url);

			conn.setInstanceFollowRedirects(getFollowRedirects());

			setHeaderFields(conn);

			if (getDumpHeaders()) {
				dumpHeaders(conn);				
			}
			
			if (getDumpCookies()) {
				dumpCookies(conn);
			}

			if (getMethod() == METHOD_POST) {
				conn.setDoOutput(true);

				writePostData(conn.getOutputStream());
			}

			conn.connect();

			if (session.getSaveHeaders()) {
				storeHeaderFields(conn);
			}

			session.postProcessConnection(conn);

			// if the server returns an error (e.g. 500) then there will be no last result
			// and this method will return false. when that happens, we return false for the
			// connect operration as well
			bResult = storeLastResult(conn);

		} catch (Exception e) {
			strLastResult = "";
			e.printStackTrace();
		}

		return bResult;
	}

	/**
	 * helper to urlencode parameter strings
	 **/
	public static String urlencode(String str) {
		String strReturn = "";

		try {
			strReturn = java.net.URLEncoder.encode(str, "UTF-8");
			getLogger().fine("urlencode(\"" + str + "\") returning " + strReturn);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return strReturn;

	}

	/**
	 * helper to print a resulting HTML string with the html chars escaped
	 **/
	public static final String escapeHTML(String s) {
		return StringEscapeUtils.escapeHtml4(s);
	}

	public String toString() {
		return "HTTPClientRequest(url=" + this.getUrl() + ")";
	}

}
