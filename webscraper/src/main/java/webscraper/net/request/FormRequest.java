package webscraper.net.request;

import java.io.OutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import java.net.HttpURLConnection;

import java.util.Map;

import webscraper.net.WebSession;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

public class FormRequest extends WebRequest {
	private List<String> params = new ArrayList<String>();
		
	protected FormRequest( WebSession session, String path ) {
		super( session, path );
	}

	public String getContentType( ) {
		return "application/x-www-form-urlencoded; charset=UTF-8" ;
	}
	
	public void addParameter( String param, String val ) {
		params.add( urlencode(param) + "=" + urlencode(val) );
	}
	
	private String getParametersString() {
		String urlParams = "";
		
		// add parameters to end of url
		Iterator<String> i = params.iterator();
		while( i.hasNext() ) {
			String str = i.next();
			urlParams += str;

			if (i.hasNext()) {
				urlParams += "&";
			}
		}
		return urlParams;
	}
	
	public String getDecoratedUrl() {
		
		// for GET methods we tack on the parameters to the end of the 
		// URL.  See writePostData for handling of POST
		if (getMethod() == METHOD_GET) {
			String paramString = getParametersString();
			if (paramString.length() > 0) {
				return getUrl() + "?" + getParametersString();
			}
		} 

		return getUrl();
	}
	
	protected void writePostData( OutputStream os ) {
		try {
			byte[] b = getParametersString().getBytes("UTF-8");
			os.write(b);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		System.err.println("POST: " + getParametersString());
	}

	public String toString() {
		return "FormRequest(url=" + this.getUrl() + ")";
	}
	
}

