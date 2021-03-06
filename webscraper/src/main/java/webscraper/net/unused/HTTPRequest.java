package webscraper.net.unused;

/** djb [10/17/2017]  This class was superseded by the HTTPClietnSession class, so
 * 					  moved it to webscraper.net.unused.  Should be deleted eventually
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.HttpURLConnection;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.TrustManagerFactory;

import webscraper.net.util.CookieManager;

import javax.net.ssl.TrustManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import java.security.KeyStore;
import java.security.GeneralSecurityException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.Map;
import java.util.List;
import java.util.Iterator;
import java.util.logging.Logger;


public class HTTPRequest {
	
	public static final String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded; charset=UTF-8";

	private CookieManager _cookieMgr = null;

	private String _url = "";
	private boolean _doCredentials = false;
	private String _userid = "";
	private String _password = "";
	private boolean _doNewCookieSession = true;
	private boolean _doHeader = false;
	private boolean _doPost = false;
	private String _strLastResult = "";
	private String _strLastError = "";
	private String _strHeaders = "";
	private String _strReferer = "";
	private int _responseCode = 200;
	private String _strContentType = "";
	private String _strAuthorizationHeader = "";
	private boolean _useCustomKeyStore = false;
	private String _trustedKeyStore = null;
	private String _trustedKeyStorePassword = null;
	private ByteArrayOutputStream _parms = new ByteArrayOutputStream();
	
    public class MyAuthenticator extends Authenticator {
    	private String m_strUserid;
    	private String m_strPassword;
    	
    	public MyAuthenticator( String strUserid, String strPassword ) {
    		super();
    		m_strUserid = strUserid;
    		m_strPassword = strPassword;
    	}
        // This method is called when a password-protected URL is accessed
        protected PasswordAuthentication getPasswordAuthentication() {
            // Return the information
            return new PasswordAuthentication(m_strUserid, m_strPassword.toCharArray());
        }
    }

	public HTTPRequest() {
		_url = "";
		_cookieMgr =  new CookieManager();
	}

	private static final Logger getLogger() {
    	return Logger.getLogger("zero.util.net");
    }

	
	public String getUrl( ) {
		return _url;
	}

	public void setUrl( String str ) {
		_url = str;
		getLogger().fine( _url );
	}
	
	public void setCredentials( String userid, String password ) {
		_userid = userid;
		_password = password;
		_doCredentials = true;
		
		getLogger().fine( "user=" + _userid );
	}
	
	public void setContentType( String strContentType ) {
		_strContentType = strContentType;
	}
	
	public String getContentType( ) {
		return _strContentType ;
	}
	
	public void setAuthorizationHeader( String str ) {
		_strAuthorizationHeader = str;
	}
	
	public String getAuthorizationHeader( ) {
		return _strAuthorizationHeader ;
	}
	
	public boolean useCredentials() {
		return _doCredentials;
	}
	
	public boolean useNewCookieSession() {
		return _doNewCookieSession;
	}
	
	public void setCookie( String name, String value, String host, String path, String expires ) {
		_cookieMgr.setCookie( name, value, host, path, expires) ;
	}
	
	public String getUserid() {
		return _userid;
	}
	
	public String getPassword() {
		return _password;
	}
	
	public void setNewCookieSession( boolean val ) {
		_doNewCookieSession = val;
		getLogger().fine( "_doNewCookieSession: " + _doNewCookieSession );
	}
	
	public void setHeader( boolean val ) {
		_doHeader = val;
		getLogger().fine( "_doHeader: " + _doHeader );
	}
	
	public boolean useHeader() {
		return _doHeader;
	}

	public void setPost( boolean val ) {
		_doPost = val;
		getLogger().fine( "_doPost: " + _doPost );
	}
	
	public boolean usePost() {
		return _doPost;
	}
	
	public void setPostFieds( String data ) {
		_parms.reset();
		
		PrintWriter pw = new PrintWriter(_parms);
		pw.write(data);
		pw.flush();
	}
	
	public void setReferer( String str ) {
		_strReferer = str;
		getLogger().fine( _strReferer );
	}
	
	public String getReferer() {
		return _strReferer;
	}
	
	public String getLastResult() {
		return _strLastResult;
	}
	
	public String getLastErrorString() {
		return _strLastError;
	}

	public boolean useCustomKeyStore() {
		return _useCustomKeyStore ;
	}
		
	public void setCustomKeyStore( String filename, String password ) {
		_useCustomKeyStore = true;
		_trustedKeyStore = filename;
		_trustedKeyStorePassword = password; 
		getLogger().fine( "filename = " + _trustedKeyStore );
	}

	protected KeyManager[] getKeyManagers() throws IOException, GeneralSecurityException {
	                
	        String alg=KeyManagerFactory.getDefaultAlgorithm();
	        KeyManagerFactory kmFact=KeyManagerFactory.getInstance(alg);
	                
	        FileInputStream fis=new FileInputStream(_trustedKeyStore);
	        KeyStore ks=KeyStore.getInstance("jks");
	        ks.load(fis, _trustedKeyStorePassword.toCharArray());
	        fis.close();
	        
	        kmFact.init(ks, _trustedKeyStorePassword.toCharArray());
	        
	        return kmFact.getKeyManagers();
	}
	                        
	                        
	protected TrustManager[] getTrustManagers() throws IOException, GeneralSecurityException {
	                        
	        String alg=TrustManagerFactory.getDefaultAlgorithm();
	        TrustManagerFactory tmFact=TrustManagerFactory.getInstance(alg);
	                        
	        FileInputStream fis=new FileInputStream(_trustedKeyStore);
	        KeyStore ks=KeyStore.getInstance("jks");
	        ks.load(fis, _trustedKeyStorePassword.toCharArray());
	        fis.close();
	                                
	        tmFact.init(ks);
	                         
	        return tmFact.getTrustManagers();
	}
	                        
	                        
	protected SSLSocketFactory getSSLSocketFactory() throws IOException, GeneralSecurityException {
	                
	        TrustManager[] tms=getTrustManagers();
	                        
	        KeyManager[] kms=getKeyManagers();
	                
	        SSLContext context=SSLContext.getInstance("SSL");
	        context.init(null, tms, null);
	                 
	        return context.getSocketFactory();
	}

	/** 
	 * setupHttpsConnection
	 *
	 * this exists primarily to allow connections to test certificates where the CA
	 * can't be readily verified.  There are several internal IBM servers that use test certificates.
	 * without a custom key store that trusts the test certificate, HTTPS connections will fail
	 *
	 * The setDefaultSSLSocketFactory call below replaces the default SSL socket factory with an 
	 * implementation that uses a custom key store of trusted certificates rather than the
	 * default JVM keystore.  I found the
	 * sample code for how to create this here: http://www.delphifaq.com/faq/f5003.shtml
	 * 
	 * In order to set a custom key store, you will need a key store file with the certificate of the 
	 * test server in it. One easy way to do this is via the InstallCert code found here.  
	 * http://blogs.sun.com/andreas/entry/no_more_unable_to_find  Point it at
	 * the URL of the test server and it will add the cert to a key store file name jssecacerts.  You can
	 * then rename this and move it where ever you want, using the setCustomKeyStore method above to 
	 * set it as the key store for this request.  (note that you may be prompted for a password to the
	 * default keystore, which is either changeit or changeme on Mac OSX)
	 *
	 * A second problem which can also come up with test certificates is that the host name on the cert
	 * doesn't match the server name. This will also cause the connection to fail.  The code below
	 * automatically turns off host name verification by replacing the HostnameVerifier with an implementation
	 * that always lets it pass.  Example code found here: http://forums.sun.com/thread.jspa?threadID=521779
	 *
	 */
	protected void setupHttpsConnection() throws IOException, GeneralSecurityException {

		// if a custom key store is specified, modify the ssl connection defaults to use it
		if (useCustomKeyStore()) {	
			
		    HttpsURLConnection.setDefaultSSLSocketFactory( getSSLSocketFactory() );

			// see comments above on why this is necessary
			HostnameVerifier hv = new HostnameVerifier() {
			    public boolean verify(String urlHostName, SSLSession session) {
			    	getLogger().warning( "Warning: URL Host: "+urlHostName+" vs. "+session.getPeerHost());
			        return true;
			    }
			};
			 
			HttpsURLConnection.setDefaultHostnameVerifier(hv);	
	
		}
	}

	protected HttpURLConnection getConnection( String strUrl ) throws Exception {
	    URL url = new URL(strUrl);
	    
	    if (!url.getProtocol().toUpperCase().equals("HTTP") && 
	    	!url.getProtocol().toUpperCase().equals("HTTPS")) 
	    {
	    	throw new IllegalArgumentException("HTTPRequest.getConnection: invalid protocol " + 
	    										url.getProtocol() + " specified. Only HTTP/HTTPS supported.");
	    }
	    	    
	    if (url.getProtocol().toUpperCase().equals("HTTPS")) {
	    	setupHttpsConnection();
	    }	       
	    	
   	    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
	    return conn;
	}
	
	private void storeHeaderFields( HttpURLConnection conn ) {
		_strHeaders = "";
		
		Map fields = conn.getHeaderFields();
		for (Iterator itset = fields.keySet().iterator(); itset.hasNext(); ) {
			String key = (String)itset.next();
			List field = (List)fields.get(key);
			
			for (Iterator it=field.iterator(); it.hasNext(); ) {
				String hdrItem = (String)it.next();

				/* ignore 'null' entry since this is not really a key/val header
				 * it contains the HTTP response string, e.g. 'HTTP/1.1 200 OK' 
				 */
				if (key != null) {
					_strHeaders = _strHeaders + key + ": " + hdrItem + "\n";
				}
			}
		}
		
		getLogger().fine( "headers=" + _strHeaders);
	}
	
	public String getHeaders() {
		if (!useHeader()) {
			getLogger().warning( "Warning - getHeader called without useHeader option set!");
		}
		return _strHeaders;
	}
	
	private void storeResponseCode( int code ) {
		_responseCode = code;
	}
	
	public int getResponseCode() {
		return _responseCode;
	}
	
	private void writePostFields( OutputStream os ) throws IOException {
		_parms.writeTo( os );
	}

	/**
	 * Set the basic auth header ourselves.  I initially used
	 * the built in java.net.Authenticator.setDefault() to set my own authenticator to handle
	 * this.  But I found that it would cache the authorization header and subsequent
	 * calls with a different username/password still used the previously set credentials.
	 * 
	 * @param conn - an HttpURLConnection to set the authorization header for
	 */
	private void setAuthorizationHeader( HttpURLConnection conn ) {
		String userPassword = getUserid() + ":" + getPassword();
		String encoding = new String(org.apache.commons.codec.binary.Base64.encodeBase64(userPassword.getBytes()));
		conn.setRequestProperty ("Authorization", "Basic " + encoding);	
	}

	/**
	 * makes the HTTP request, storing the results in the _strLastResult
	 * 
	 * @param strUrl - the url to request
	 * @return true if successful, false if an error occurred
	 */
	public boolean doRequest( ) {
		boolean bResult = false;
		_strLastResult = "";

		try {	        
			
			HttpURLConnection conn = getConnection(_url);
			
			if (useCredentials()) {
				setAuthorizationHeader( conn );
			}
			
        	_cookieMgr.setCookies(conn, !useNewCookieSession());	// store cookies to remember session state on server 
        	
			if (usePost()) {
				conn.setDoOutput(true);
			}
			
			if (getReferer().length() > 0) {
				conn.setRequestProperty("Referer", getReferer());
			}
			
			if (getContentType().length() > 0) {
				conn.setRequestProperty("Content-type", getContentType());
			}

			if (getAuthorizationHeader().length() > 0) {
				conn.setRequestProperty("Authorization", getAuthorizationHeader());
			}

			if (usePost()) {
				writePostFields( conn.getOutputStream() );
			}
			
			conn.connect();
			
			if (useHeader()) {
				storeHeaderFields(conn);
			}
			
			storeResponseCode(conn.getResponseCode());
	        	
        	_cookieMgr.storeCookies(conn);	// remember cookies for next call
        	
        	// Get the response
			BufferedReader rd = null;			
        	rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	       
			String line;
			StringBuilder strResult = new StringBuilder("");
	    	while ((line = rd.readLine()) != null) {
	    		strResult.append( line );
	    		strResult.append( "\n" );
		   	 }
	    	_strLastResult = strResult.toString();
	    	
	    	bResult = true;
		} catch(Exception e) {
			e.printStackTrace();
		}
	      	
		return bResult;
	}	
	
	/**
	 * helper to urlencode parameter strings
	 **/
	public static String urlencode( String str ) {
		String strReturn = "";
		
		try {
			strReturn = java.net.URLEncoder.encode(str, "UTF-8");
			getLogger().fine( "urlencode(\"" + str + "\") returning " + strReturn);
		} catch( Exception e ) {
			e.printStackTrace();
		}
		
		return strReturn;

	}

	public String toString() {
		return "HTTPRequest(url=" + this.getUrl() + ")";
	}
}

