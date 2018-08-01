package teetime.ajax;

import org.json.JSONObject;

import webscraper.net.WebSession;

/**
 * Manage page interactions for locking a given tee time
 * 
 * @author djboulia
 *
 */
public class LockTeeTime extends AjaxAction {

	public LockTeeTime(WebSession session, String path) {
		super(session, path);
	}

	private String buildInput() {
		//	"{\"methodName\":\"ttBooking.ProceedLock\",\"data\":{\"locks\":\"Jonas\",\"callback\":\"JonasLockConfirmed\"}}"
		
		JSONObject data = new JSONObject();		
		data.put("locks", "Jonas");
		data.put("callback", "JonasLockConfirmed");
		
		JSONObject postData = new JSONObject();
		postData.put("methodName", "ttBooking.ProceedLock");
		postData.put("data", data);
		
		String json = postData.toString();
		return json;
	}
	
	public boolean send() {

		String json = buildInput();
		
		String result = doPost(json);
		
		if (result == null) {
			System.err.println("Error requesting lock.");
			return false;
		}

		// System.out.println( result );
		if (!goodResult(result)) {
			System.err.println("Didn't obtain lock. Result ->" + result);
			return false;
		}

		return true;
	}
	
	/**
	 * expecting to find this JSON
	 * {\"locks\":\"Jonas\",\"callback\":\"JonasLockConfirmed\"}
	 * 
	 * @param result server response
	 * @return true if we find the right result, false otherwise
	 */
	private boolean goodResult(String result) {
		JSONObject jsonResult = parseAjaxResult(result);
		
		if (jsonResult!=null) {
			if (jsonResult.getString("locks").compareTo("Jonas")==0 &&
					jsonResult.getString("callback").compareTo("JonasLockConfirmed")==0) {
				return true;
			}
		}
		
		return false;
	}

}
