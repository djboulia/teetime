package webscraper.net;

import java.net.URL;
import java.net.HttpURLConnection;
import java.net.Authenticator;
import java.net.PasswordAuthentication;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;

import webscraper.net.request.RequestFactory;
import webscraper.net.request.AjaxRequest;
import webscraper.net.request.FormRequest;
import webscraper.net.request.WebRequest;
import webscraper.net.request.MultipartFormRequest;
import webscraper.net.util.CookieManager;

import javax.net.ssl.TrustManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLSocketFactory;

import java.security.KeyStore;
import java.security.GeneralSecurityException;

import java.io.IOException;
import java.io.FileInputStream;

import java.util.logging.Logger;

/**
 * HTTPClientSession
 * 
 * Base class to initiate a "conversation" with a back end web site, where elements such
 * as authentication, cookies, and security keys are preserved across multiple individual 
 * connections.
 *
 * An HTTPClientSession is created once to begin a series of HTTPClientRequests.  HTTPClientRequests
 * represent one round trip with the server, e.g. an individual GET or POST.  As a result,
 * multiple individual requests can occur within one HTTPClientSession.  Use the newXxxRequest()
 * methods of HTTPClientSession to initiate request objects with the appropriate authentication, security
 * certificates and cookies loaded in the request.
 *
 */

public class WebSession {
	
	private CookieManager cookieMgr = null;
	private String baseUrl = "";
	
	private boolean hasCredentials = false;
	private String userid = "";
	private String password = "";
	private boolean saveCookies = false;
	private boolean saveHeaders = false;
	private boolean useCustomKeyStore = false;
	private String trustedKeyStore = null;
	private String trustedKeyStorePassword = null;
	
    public class MyAuthenticator extends Authenticator {
    	private String strUserid;
    	private String strPassword;
    	
    	public MyAuthenticator( String strUserid, String strPassword ) {
    		super();
    		this.strUserid = strUserid;
    		this.strPassword = strPassword;
    	}
        // This method is called when a password-protected URL is accessed
        protected PasswordAuthentication getPasswordAuthentication() {
            // Return the information
            return new PasswordAuthentication(strUserid, strPassword.toCharArray());
        }
    }
    
    private static final Logger getLogger() {
    	return Logger.getLogger("zero.util.net");
    }
    
	/** 
	 * @param baseUrl : the url base to use for new requests
	 **/
	public WebSession( String baseUrl ) {
		this.baseUrl = baseUrl;
		this.cookieMgr =  new CookieManager();
	}
	
	public String getBaseUrl( ) {
		return baseUrl;
	}
	
	public void setBaseUrl( String baseUrl ) {
		this.baseUrl = baseUrl;
	}

	
	public void setCredentials( String userid, String password ) {
		this.userid = userid;
		this.password = password;
		hasCredentials = true;
		
		getLogger().fine( "user=" + this.userid );
	}

	public boolean hasCredentials() {
		return hasCredentials;
	}

	public String getUserid() {
		return userid;
	}
	
	public String getPassword() {
		return password;
	}
	
	public boolean getSaveHeaders() {
		return saveHeaders;
	}
	
	public void setSaveHeaders( boolean val ) {
		saveHeaders = val;
		getLogger().fine( "saveHeaders = " + saveHeaders );
	}

	
	public boolean getSaveCookies() {
		return saveCookies;
	}
	
	public void setCookie( String name, String value, String host, String path, String expires ) {
		cookieMgr.setCookie( name, value, host, path, expires) ;
	}
	
	public void setSaveCookies( boolean val ) {
		saveCookies = val;
		getLogger().fine( "saveCookies = " + saveCookies );
	}
	
	public CookieManager getCookieManager() {
		return this.cookieMgr;
	}
			
	public boolean useCustomKeyStore() {
		return useCustomKeyStore ;
	}
		
	public void setCustomKeyStore( String filename, String password ) {
		useCustomKeyStore = true;
		trustedKeyStore = filename;
		trustedKeyStorePassword = password; 
		getLogger().fine( "filename = " + trustedKeyStore );
	}

	protected KeyManager[] getKeyManagers() throws IOException, GeneralSecurityException {
	                
	        String alg=KeyManagerFactory.getDefaultAlgorithm();
	        KeyManagerFactory kmFact=KeyManagerFactory.getInstance(alg);
	                
	        FileInputStream fis=new FileInputStream(trustedKeyStore);
	        KeyStore ks=KeyStore.getInstance("jks");
	        ks.load(fis, trustedKeyStorePassword.toCharArray());
	        fis.close();
	        
	        kmFact.init(ks, trustedKeyStorePassword.toCharArray());
	        
	        return kmFact.getKeyManagers();
	}
	                        
	                        
	protected TrustManager[] getTrustManagers() throws IOException, GeneralSecurityException {
	                        
	        String alg=TrustManagerFactory.getDefaultAlgorithm();
	        TrustManagerFactory tmFact=TrustManagerFactory.getInstance(alg);
	                        
	        FileInputStream fis=new FileInputStream(trustedKeyStore);
	        KeyStore ks=KeyStore.getInstance("jks");
	        ks.load(fis, trustedKeyStorePassword.toCharArray());
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
			    	getLogger().fine( "Warning: URL Host: "+urlHostName+" vs. "+session.getPeerHost());
			        return true;
			    }
			};
			 
			HttpsURLConnection.setDefaultHostnameVerifier(hv);	
	
		}
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
	
	public HttpURLConnection getConnection( String strUrl ) throws Exception {
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

		if (hasCredentials()) {
			setAuthorizationHeader( conn );
		}
		
       	cookieMgr.setCookies(conn, getSaveCookies());	// store cookies to remember session state on server 	
	    return conn;
	}
	

	public void postProcessConnection( HttpURLConnection conn ) throws IOException {
       	cookieMgr.storeCookies(conn);	// remember cookies for next call        	
	}
	
	
	// factory methods to create new requests
	public WebRequest createWebRequest( String path ) {
		WebRequest req = RequestFactory.createWebRequest( this, path );

		return req;
	}
	
	public FormRequest createFormRequest( int method, String path ) {
		FormRequest req = RequestFactory.createFormRequest( this, method, path );

		return req;
	}
	
	public MultipartFormRequest createMultipartFormRequest( String path ) {
		MultipartFormRequest req = RequestFactory.createMultipartFormRequest( this, path );

		return req;
	}
	
	public AjaxRequest createAjaxRequest( String path ) {
		AjaxRequest req = RequestFactory.createAjaxRequest( this, path );

		return req;
	}
	
	
	public String toString() {
		return "HTTPClientSession(url=" + this.getBaseUrl() + ")";
	}
}

