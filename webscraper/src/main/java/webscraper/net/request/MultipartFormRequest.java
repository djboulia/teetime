package webscraper.net.request;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.File;
import java.io.ByteArrayOutputStream;

import java.net.HttpURLConnection;

import java.util.Random;
import java.util.logging.Logger;

import webscraper.net.WebSession;

public class MultipartFormRequest extends WebRequest {
	private ByteArrayOutputStream parameterData = new ByteArrayOutputStream();
		
    private static final Logger getLogger() {
    	return Logger.getLogger("zero.util.net");
    }

	protected MultipartFormRequest( WebSession session, String path ) {
		super( session, path );
		setMethod( METHOD_POST) ;	// multi-part msgs are always POST requests
	}

	protected void write(char c) throws IOException {
		parameterData.write(c);
	}
	protected void write(String s) throws IOException {
		parameterData.write(s.getBytes());
	}
	
  	protected void newline() throws IOException {
		write("\r\n");
	}	

	protected void writeln(String s) throws IOException {
		write(s);
		newline();
	}

  	private static Random random = new Random();
	
	protected static String randomString() {
		return Long.toString(random.nextLong(), 36);
	}

	String boundary = "---------------------------" + randomString() + randomString() + randomString();

	private void boundary() throws IOException {
	  write("--");
	  write(boundary);
	}

	public String getContentType( ) {
		return "multipart/form-data; boundary=" + boundary;             
	}
	
	private void writeName(String name) throws IOException {
		newline();
		write("Content-Disposition: form-data; name=\"");
		write(name);
		write('"');
	}
	
	public void addParameter( String param, String val ) throws IOException {
		getLogger().fine( "adding form parameter " + param + "=" + val ) ;
		boundary();
		writeName(param);
		newline(); newline();
		writeln(val);
	}

	private static void pipe(InputStream in, OutputStream out) throws IOException {
		byte[] buf = new byte[500000];
		int nread;
		int total = 0;
		synchronized (in) {
			while((nread = in.read(buf, 0, buf.length)) >= 0) {
				out.write(buf, 0, nread);
				total += nread;
			}
		}
		out.flush();
		in.close();
		buf = null;
	}
	
	/**
	* adds a file parameter to the request
	* @param name parameter name
	* @param filename the name of the file
	* @param is input stream to read the contents of the file from
	* @throws IOException
	*/
	public void addParameter(String name, String filename, InputStream is) throws IOException {
		boundary();
		writeName(name);
		write("; filename=\"");
		write(filename);
		write('"');
		newline();
		write("Content-Type: ");
		String type = HttpURLConnection.guessContentTypeFromName(filename);
		if (type == null) type = "application/octet-stream";
		writeln(type);
		newline();
		pipe(is, parameterData);
		newline();
	}
	
	/**
	* adds a file parameter to the request
	* @param name parameter name
	* @param file the file to upload
	* @throws IOException
	*/
	public void addParameter(String name, File file) throws IOException {
		getLogger().fine( "adding file parameter " + name + "=" + file.getPath() ) ;
		addParameter(name, file.getPath(), new FileInputStream(file));
	}
	
	/**
	* adds a parameter to the request; if the parameter is a File, the file is uploaded, 
	* otherwise the string value of the parameter is passed in the request
	* @param name parameter name
	* @param object parameter value, a File or anything else that can be stringified
	* @throws IOException
	*/
	public void addParameter(String name, Object object) throws IOException {
		if (object instanceof File) {
			addParameter(name, (File) object);
		} else {
			addParameter(name, object.toString());
		}
	}
	
	public void writePostData( OutputStream os ) {
		// POST methods write our multipart stream to the connection
		try {
		    boundary();
		    writeln("--");
			parameterData.writeTo( os ) ;
		    os.close();
   		} catch( Exception e ) {
			System.err.println("MultipartFormData.writePostData: Error writing to output stream");
			e.printStackTrace();
		}
	}

	public String toString() {
		return "MultipartFormRequest(url=" + this.getUrl() + ")";
	}
	
}

