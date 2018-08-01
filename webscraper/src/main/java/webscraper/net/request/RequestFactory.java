package webscraper.net.request;

import webscraper.net.WebSession;

public class RequestFactory {
	// factory methods to create new requests
	public static WebRequest createWebRequest( WebSession session, String path ) {
		WebRequest req = new WebRequest( session, path );

		return req;
	}
	
	public static FormRequest createFormRequest(  WebSession session, int method, String path ) {
		FormRequest req = new FormRequest( session, path );
		req.setMethod( method );

		return req;
	}
	
	public static MultipartFormRequest createMultipartFormRequest(  WebSession session, String path ) {
		MultipartFormRequest req = new MultipartFormRequest( session, path );

		return req;
	}
	
	public static AjaxRequest createAjaxRequest(  WebSession session, String path ) {
		AjaxRequest req = new AjaxRequest( session, path );

		return req;
	}
	
}
