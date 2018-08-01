package webscraper.net.request;

import java.io.OutputStream;
import java.net.HttpURLConnection;

import webscraper.net.WebSession;

public class AjaxRequest extends WebRequest {
	private String json;

	protected AjaxRequest(WebSession session, String path) {		
		super(session, path);
		setDumpHeaders(true);
		setDumpCookies(true);
		setAccept("text/javascript, text/html, application/xml, text/xml, */*");
		this.setMethod(WebRequest.METHOD_POST);
	}

	public String getContentType() {
		return "application/json; charset=UTF-8";
	}

	protected void setHeaderFields(HttpURLConnection conn) {
		super.setHeaderFields(conn);

		conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");
		conn.setRequestProperty("X-Prototype-Version", "1.7.1");
	}

	public void setJson(String json) {
		this.json = json;
	}

	protected void writePostData(OutputStream os) {

		try {
			byte[] b = json.getBytes("UTF-8");
			os.write(b);
			
			System.out.println("Wrote " + b.length + " bytes: " + b.toString());
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public String toString() {
		return "AjaxRequest(url=" + this.getUrl() + ")";
	}

}
