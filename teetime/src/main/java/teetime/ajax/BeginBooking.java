package teetime.ajax;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import teetime.TimeSlot;
import webscraper.net.WebSession;

/**
 * Manage page interactions for locking a given tee time
 * 
 * @author djboulia
 *
 */
public class BeginBooking extends AjaxAction {
	private String redirectPath;

	public BeginBooking(WebSession session, String path) {
		super(session, path);
	}

	public String getRedirectPath() {
		return redirectPath;
	}

	private String buildInput(String id) {
		//	"{"methodName":"ttTeeSheet.ProceedStartBooking","data":{"teetimeid":"75596801"}}"
		
		JSONObject data = new JSONObject();		
		data.put("teetimeid", id);
		
		JSONObject postData = new JSONObject();
		postData.put("methodName", "ttTeeSheet.ProceedStartBooking");
		postData.put("data", data);
		
		String json = postData.toString();
		return json;
	}
	
	public boolean send(TimeSlot teetime) {
		
		// see if this time is available by requesting the lock
//		String json = "{\"methodName\":\"ttTeeSheet.ProceedStartBooking\",\"data\":{\"teetimeid\":\"" + teetime.getId() +"\"}}";		
		
		String json = buildInput( teetime.getId() );
		System.out.println("RequestLock json: " + json);
		
		String result = doPost(json);
		
		if (result==null) {
			System.err.println("Error requesting lock.");
			return false;
		}
		
		// System.out.println( result );

		if (!getResult(result)) {
			System.err.println("Didn't obtain lock! Response was --> " + result);
			return false;
		}

		return true;
	}
	
	private boolean getResult( String result ) {
		redirectPath = null;
		
		try {
			JSONObject jsonResult = parseAjaxResult(result);
//			System.err.println("Found object: " + jsonResult.toString());
			
			if (jsonResult != null) {
								
				if (jsonResult.getString("method").compareTo("Redirect")==0) {
					
					// the methodparameters property has an object with redirect url
					JSONObject jsonRedirect = jsonResult.getJSONObject("methodparameters");
					
					redirectPath = jsonRedirect.getString("url");
					return true;
				}
			}
		} catch(JSONException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	// TODO: delete this after verifying the new code works

	private static final Pattern patLockRedirect = Pattern
			.compile("\\\\\"url\\\\\":\\\\\"(.*?)(\\\\\"}}\"})");

	/**
	 * @param data
	 *            the response string to parse
	 * @return the redirect path if successful, null otherwise
	 * @throws java.io.IOException
	 */
	public static String parseRedirect(String data) throws java.io.IOException {

		// Request
		// {"methodName": "ttSheet.RequestLock", "data": {"teetimeid": 8385822,
		// "FilterQuery": 8}}
		// Response
		// {"d":"{\"method\":\"Redirect\",\"methodparameters\":{\"url\":\"/TeeTimes/Booking.aspx\"}}"}

		// normalize all white space to make our regex search patterns work smoothly
		String str = data.replaceAll("\\s+", " ");

		System.err.println(" str = " + str);

		// find all input boxes
		Matcher mList = patLockRedirect.matcher(str);

		while (mList.find()) {

			if (mList.groupCount() != 2) {
				// some sort of parse error -- abort
				System.err.println("Unexpected format in results!");
				for (int i = 0; i <= mList.groupCount(); i++) {
					System.err.println("group " + i + ": [" + mList.group(i) + "]");
				}
				throw new java.io.IOException("Bad format");
			}

			String redirect = mList.group(1);
			
			System.err.println("found redirect --" + redirect + "--");

			return redirect;
		}

		return null;
	}

}
