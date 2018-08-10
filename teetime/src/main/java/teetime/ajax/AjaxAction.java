package teetime.ajax;

import org.json.JSONException;
import org.json.JSONObject;

import webscraper.net.WebSession;
import webscraper.net.request.AjaxRequest;

public class AjaxAction {
	
	private WebSession session;
	private String path;
	private String referer;
	private String errorMessage;


	public AjaxAction(WebSession session, String path) {
		this.session = session;	
		this.path = path;
	}

	public WebSession getSession() {
		return session;
	}
	
	public String getPath() {
		return path;
	}
	
	public String getReferer() {
		return this.referer;
	}

	public void setReferer(String referer) {
		this.referer = referer;
	}
	
	protected void setErrorMessage(String str) {
		this.errorMessage = str;
	}
	
	public String getErrorMessage() {
		return this.errorMessage;
	}

	
	protected String doPost(String json) {
		
		AjaxRequest ajax = getSession().createAjaxRequest(getPath());
		
		ajax.setReferer(getReferer());	// NOTE: this MUST be set for ASP.NET interactions

		ajax.setJson(json);

		boolean success = ajax.connect();
		if (!success) {
			// look for a 500 error code, and see if we can extract the error message
			if (ajax.getResponseCode() == 500) {
				String error = ajax.getHeaderField("jsonerror");
				System.err.println("Error: " + error);
				setErrorMessage(error);
			} else {
				System.err.println("Error code " + ajax.getResponseCode() + " making Ajax request to " + getPath() );
			}
			return null;
		}

		String result = ajax.getLastResult();
		
		return result;
	}
	
	/**
	 * Decode the responses coming back from the server
	 * 
	 * oddly, the return JSON is actually an object with one key labeled "d" which 
	 * contains a string that is itself an embedded JSON string.  So we have to
	 * first extract the string at key "d", then re-parse it as a JSON object
	 * 
	 * @param result return string from the server (expected to be a JSON string)
	 * @return A JSONObject for the embedded portion of the response, or null if error
	 */
	public JSONObject parseAjaxResult(String result) {
		try {
			JSONObject jsonResult = new JSONObject(result);
//			System.err.println("Found object: " + jsonResult.toString());
			
			if (jsonResult.has("d")) {
				
				String strd = jsonResult.getString("d");				
				JSONObject jsond = new JSONObject(strd);
				
				return jsond;
			}
		} catch(JSONException e) {
			e.printStackTrace();
		}	
		
		return null;
	}
}
