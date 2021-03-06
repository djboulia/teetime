package teetime.asp;

import webscraper.net.WebSession;
import webscraper.net.request.FormRequest;
import webscraper.net.request.WebRequest;

public class Login extends AspAction {

	public static final int HTTP_REDIRECT = 302;

	public Login(WebSession session, String path) {
		super(session, path);
	}
	
	public boolean send(String userid, String password) {

		String result = loadPage(getPath());
		if (result == null) {
			return false;
		}
		
		FormRequest form = getSession().createFormRequest(WebRequest.METHOD_POST, getPath());

		form.addParameter("manScript_HiddenField", "");
		form.addParameter("__EVENTTARGET", "");
		form.addParameter("__EVENTARGUMENT", "");
		form.addParameter("__VIEWSTATE", getViewState());
		form.addParameter("lng", "en-US");
		form.addParameter("DES_JSE", "1");
		form.addParameter("__VIEWSTATEGENERATOR", getViewStateGenerator());
		form.addParameter("__SCROLLPOSITIONX", "0");
		form.addParameter("__SCROLLPOSITIONY", "247");
		form.addParameter(
				"p$lt$middlebody$pageplaceholder$p$lt$zoneRight$CHOLogin$LoginControl$ctl00$Login1$UserName",
				userid);
		form.addParameter(
				"p$lt$middlebody$pageplaceholder$p$lt$zoneRight$CHOLogin$LoginControl$ctl00$Login1$Password",
				password);
		form.addParameter(
				"p$lt$middlebody$pageplaceholder$p$lt$zoneRight$CHOLogin$LoginControl$ctl00$Login1$LoginButton",
				"Login");

		System.out.println("Posting form data to " + form.getUrl());

		// submit the form
		boolean success = form.connect();
		if (!success) {
			System.err.println("Error accessing " + form.getUrl());
			return false;
		}

		// form submitted, check to see that we have the login credentials
		success = validLogin(form);
		if (!success ) {
			System.err.println("Error logging in to " + form.getUrl());
			return false;
		}
		
		result = form.getLastResult();
//		System.out.println( result );
		
		return true;
	}

	private boolean validLogin(FormRequest form) {
		// we don't get a page back that says "you've logged in", so we look for a
		// redirect and check that we got the ASP authentication cookies back
		int code = form.getResponseCode();

		if (code != HTTP_REDIRECT) {
			System.out.println("Response code: " + code);
			return false;
		}

		String strHeaders = form.getStoredHeaders();
		System.out.println("Headers: " + strHeaders);

		return strHeaders.indexOf("ASPXFORMSAUTH") >= 0;
	}

}
