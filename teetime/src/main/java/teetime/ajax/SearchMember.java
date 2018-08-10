package teetime.ajax;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import teetime.Golfer;
import teetime.Golfers;
import webscraper.net.WebSession;

public class SearchMember extends AjaxAction {
	private Golfers golfers;

	public SearchMember(WebSession session, String path) {
		super(session, path);
	}

	public Golfers getSearchResults() {
		return golfers;
	}

	private String buildInput(String name) {
		// {"methodName":"Player.SearchMember","data":{"filter":"boulia","data":true,"isfullname":true,"excludemembers"
		//		:"593198","excludeGuests":""}}
		
		JSONObject data = new JSONObject();		
		data.put("filter", name);
		data.put("data", true);
		data.put("isfullname", true);
		data.put("excludemembers", "");
		data.put("excludeGuests", "");
		
		JSONObject postData = new JSONObject();
		postData.put("methodName", "Player.SearchMember");
		postData.put("data", data);
		
		// see if this time is available by requesting the lock
		String json = postData.toString();
		System.out.println("json: " + json);
		
		return json;
	}
	
	public boolean send(String name) {

		String json = buildInput(name);
		
		String result = doPost(json);
		
		if (result==null) {
			System.err.println("Error searching for members.");
			return false;
		}
		
		System.out.println("Response: " + result);
		
		if (!getResult(result)) {
			
			System.err.println("Member search failed. Result ->" + result);

			return false;
		}
		
		return true;
	}

	/**
	 * parses the server response and loads the golfers object with the results
	 * 
	 * @param result server response
	 * @return true if parsed successfully, false otherwise
	 */
	private boolean getResult(String result) {
		golfers = null;
		
		try {
			JSONObject jsonResult = parseAjaxResult(result);
//			System.err.println("Found object: " + jsonResult.toString());
			
			if (jsonResult != null) {
								
				if (jsonResult.getString("method").compareTo("Complete")==0) {
					
					golfers = new Golfers();
					
					// the methodparameters property has an array of search results
					JSONArray jsonGolfers = jsonResult.getJSONArray("methodparameters");
					for (int i=0; i<jsonGolfers.length(); i++) {
						JSONObject jsonGolfer = jsonGolfers.getJSONObject(i);
						Golfer golfer = new Golfer( jsonGolfer.getString("Text"), jsonGolfer.getString("Value"));
						golfers.add(golfer);
					}
					
					return true;
				}
			}
		} catch(JSONException e) {
			e.printStackTrace();
		}
		
		return false;
	}

}
