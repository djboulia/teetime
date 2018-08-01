package teetime.ui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;


public class Page1 extends Composite {
	private Text userid;
	private Text password;
	private Label theTime;
	private Label lblMsg;

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public Page1(Composite parent, int style) {
		super(parent, style);
		
		Label lblNewLabel = new Label(this, SWT.NONE);
		lblNewLabel.setBounds(10, 88, 59, 14);
		lblNewLabel.setText("Userid");
		
		Label lblPassword = new Label(this, SWT.NONE);
		lblPassword.setBounds(10, 120, 59, 14);
		lblPassword.setText("Password");
		
		userid = new Text(this, SWT.BORDER);
		userid.setBounds(85, 88, 98, 19);
		
		password = new Text(this, SWT.BORDER | SWT.PASSWORD);
		password.setBounds(85, 120, 98, 19);
		
		Label lblLogIn = new Label(this, SWT.CENTER);
		lblLogIn.setFont(SWTResourceManager.getFont("Lucida Grande", 14, SWT.BOLD));
		lblLogIn.setBounds(175, 10, 80, 24);
		lblLogIn.setText("Log In");
		
		Label lblSep = new Label(this, SWT.SEPARATOR | SWT.VERTICAL);
		lblSep.setText("sep");
		lblSep.setBounds(214, 53, 2, 125);
		
		Label lblinfo = new Label(this, SWT.WRAP);
		lblinfo.setBounds(235, 53, 187, 111);
		lblinfo.setText("");
		
		Label lblPrestonwoodUserInformation = new Label(this, SWT.NONE);
		lblPrestonwoodUserInformation.setBounds(10, 53, 177, 14);
		lblPrestonwoodUserInformation.setText("Prestonwood User Information:\r\nPrestonwood User Information:\r\n");
		
		Label lblCurrentTime = new Label(this, SWT.NONE);
		lblCurrentTime.setBounds(175, 213, 80, 14);
		lblCurrentTime.setText("Current Time:");
		
		theTime = new Label(this, SWT.NONE);
		theTime.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
		theTime.setBounds(175, 236, 80, 14);
		
		lblMsg = new Label(this, SWT.NONE);
		lblMsg.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
		lblMsg.setBounds(10, 276, 412, 14);

	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	
	public void setCurrentTime( Date theDate ) {
		DateFormat formatter = new SimpleDateFormat( " h:mm:ss aa");
		String s = formatter.format(theDate);

		theTime.setText( s );
	}
	
	public String getUserid() {
		System.err.println( "getUserid: " + userid.getText());
		return userid.getText();
	}
	
	public String getPassword() {
		return password.getText();
	}
	
	public void setMsg( String str ) {
		lblMsg.setText( str );
	}
}
