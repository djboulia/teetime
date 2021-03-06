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
public class MemberName extends AspAction {
	private String userName;

	private static final Pattern patUserName = Pattern
			.compile("<div class=\"currentUserLabelText\">Welcome (.*?)(</div>)");
	
	public MemberName(WebSession session, String path) {
		super(session, path);
	}

	private void setUserName( String str ) {
		userName = str;
	}
	
	public String getUserName( ) {
		return userName;
	}
	
	private String parseUserName( String str ) {
		Matcher matcher = patUserName.matcher(str);
		
		if (!matcher.find() || matcher.groupCount()!=2) {
			System.err.println("Error = couldn't find user name");
			return null;
		}
		
		String name = matcher.group(1);
		
		return name;
	}

	public boolean send() {

		String result = loadPage(getPath());
		if (result == null) {
			return false;
		}
		
		String name = parseUserName(result);
		System.err.println("Found user name " + name);
		
		if (name == null) {
			return false;
		} else {
			setUserName(name);
		}
		
		return true;
	}

}
