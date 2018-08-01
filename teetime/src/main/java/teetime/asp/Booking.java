package teetime.asp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import webscraper.net.WebSession;

/**
 * Manage page interactions for searching a given date for available tee times
 * 
 * @author djboulia
 *
 */
public class Booking extends AspAction {
	private String lockJson;

	private static final Pattern patLockJson = Pattern
			.compile("\"userId\":\"(.*?)\",\"teetimeid\":\"(.*?)\",\"urlCurr\":\"(.*?)\",\"urlBack\":\"(.*?)\"");

	public Booking(WebSession session, String path) {
		super(session, path);
	}

	public String getLockJson() {
		return lockJson;
	}

	public boolean send() {

		String result = loadPage(getPath());
		if (result == null) {
			return false;
		}
		
		try {
			lockJson = parseLockJson(result);
			System.err.println(lockJson);
		} catch (java.io.IOException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}

	public static String parseLockJson(String data) throws java.io.IOException {

		// Request
		// {"methodName": "ttBooking.GetJonasLock", "data": {"userId": "80062",
		// "teetimeid": "8385788", "urlCurr":
		// Response

		// normalize all white space to make our regex search patterns work smoothly
		String str = data.replaceAll("\\s+", " ");

//		System.err.println(" str = " + str);

		// find all input boxes
		Matcher mList = patLockJson.matcher(str);

		if (!mList.find())
			return null;

		if (mList.groupCount() != 4) {
			// some sort of parse error -- abort
			System.err.println("Unexpected format in results!");
			for (int i = 0; i <= mList.groupCount(); i++) {
				System.err.println("group " + i + ": [" + mList.group(i) + "]");
			}
			throw new java.io.IOException("Bad format");
		}

		// System.err.println( "found userid -- " + mList.group(1) + "--" );
		return "{\"methodName\": \"ttBooking.GetJonasLock\", \"data\": {\"userId\": \"" + mList.group(1)
				+ "\", \"teetimeid\": \"" + mList.group(2) + "\", \"urlCurr\": \"" + mList.group(3)
				+ "\", \"urlBack\": \"" + mList.group(4) + "\"}}";
	}

}
