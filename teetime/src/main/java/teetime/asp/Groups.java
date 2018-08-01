package teetime.asp;

import webscraper.net.WebSession;

/**
 * Manage page interactions for looking up members
 * 
 * @author djboulia
 *
 */
public class Groups extends AspAction {

	public Groups(WebSession session, String path) {
		super(session, path);
	}

	public boolean send() {

		String result = loadPage(getPath());
		if (result == null) {
			return false;
		}
		
		return true;
	}
}
