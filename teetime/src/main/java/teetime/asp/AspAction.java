package teetime.asp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import webscraper.net.WebSession;
import webscraper.net.request.WebRequest;

/**
 * Base class for ASP web page interactions
 * 
 * Holds session state and various underlying ASP form variables
 * 
 * @author djboulia
 *
 */
public class AspAction {
	private String viewState;
	private String viewStateGenerator;
	private WebSession session;
	private String path;
	
	public AspAction(WebSession session, String path) {
		this.session = session;
		this.path = path;
	}

	public String getPath() {
		return path;
	}
	
	public String getViewState() {
		return viewState;
	}

	public void setViewState(String viewState) {
		this.viewState = viewState;
	}

	public String getViewStateGenerator() {
		return viewStateGenerator;
	}

	public void setViewStateGenerator(String viewStateGenerator) {
		this.viewStateGenerator = viewStateGenerator;
	}

	public WebSession getSession() {
		return session;
	}

	private static final Pattern patViewState = Pattern
			.compile("<input type=\"hidden\" name=\"__VIEWSTATE\" id=\"__VIEWSTATE\" value=\"(.*?)(\" />)");
	private static final Pattern patViewStateGenerator = Pattern.compile(
			"<input type=\"hidden\" name=\"__VIEWSTATEGENERATOR\" id=\"__VIEWSTATEGENERATOR\" value=\"(.*?)(\" />)");

	private static String parseViewState(String data) throws java.io.IOException {
		String viewState = "";

		System.err.println("in ViewState");

		// normalize all white space to make our regex search patterns work smoothly
		String str = data.replaceAll("\\s+", " ");

		System.err.println(" str = " + str);

		// find all input boxes
		Matcher matcher = patViewState.matcher(str);

		while (matcher.find()) {

			if (matcher.groupCount() != 2) {
				// some sort of parse error -- abort
				System.err.println("Unexpected format in results!");
				for (int i = 0; i <= matcher.groupCount(); i++) {
					System.err.println("group " + i + ": [" + matcher.group(i) + "]");
				}
				throw new java.io.IOException("Bad format");
			}

			System.err.println("found viewState --" + matcher.group(1) + "--");
			viewState = matcher.group(1);

		}

		System.err.println("ViewState = " + viewState);
		return viewState;
	}

	private static String parseViewStateGenerator(String data) throws java.io.IOException {
		String viewState = "";

		// normalize all white space to make our regex search patterns work smoothly
		String str = data.replaceAll("\\s+", " ");

		System.err.println(" str = " + str);

		// find all input boxes
		Matcher matcher = patViewStateGenerator.matcher(str);

		while (matcher.find()) {

			if (matcher.groupCount() != 2) {
				// some sort of parse error -- abort
				System.err.println("Unexpected format in results!");
				for (int i = 0; i <= matcher.groupCount(); i++) {
					System.err.println("group " + i + ": [" + matcher.group(i) + "]");
				}
				throw new java.io.IOException("Bad format");
			}

			System.err.println("found viewStateGenerator --" + matcher.group(1) + "--");
			viewState = matcher.group(1);

		}

		return viewState;
	}

	//
	// handle loading the given page, including doing pre/post processing on the connection
	// ASP pages keep "viewstate" variables that we keep track of as we navigate various 
	// pages to maintain appropriate state
	// 
	protected String loadPage(String path) {
		// load the bookings pages
		WebSession session = getSession();
		WebRequest conn = session.createWebRequest(path);

		if (!conn.connect()) {
			System.err.println("Error accessing " + conn.getUrl());
			return null;
		}

		String result = conn.getLastResult();
//		System.out.println(result);

		if (result != null) {
			try {
				setViewState(parseViewState(result));
				setViewStateGenerator(parseViewStateGenerator(result));
			} catch (java.io.IOException e) {
				e.printStackTrace();
				return null;
			}			
		}
		
		return result;
	}
}
