package teetime.ui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.Group;


public class Page3 extends Composite {
	
	private Label theTime;
	private Label theStatus;
	private Label attempts;
	
	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public Page3(Composite parent, int style) {
		super(parent, style);
		
		Label lblReservingTeeTime = new Label(this, SWT.CENTER);
		lblReservingTeeTime.setText("Reserving Tee Time");
		lblReservingTeeTime.setFont(SWTResourceManager.getFont("Lucida Grande", 14, SWT.BOLD));
		lblReservingTeeTime.setBounds(55, 10, 343, 24);
		
		Label lblCurrentStatus = new Label(this, SWT.NONE);
		lblCurrentStatus.setAlignment(SWT.CENTER);
		lblCurrentStatus.setBounds(175, 40, 103, 14);
		lblCurrentStatus.setText("Current Status");
		
		theStatus = new Label(this, SWT.NONE);
		theStatus.setFont(SWTResourceManager.getFont("Lucida Grande", 11, SWT.ITALIC));
		theStatus.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
		theStatus.setBounds(69, 94, 320, 74);
		theStatus.setText("Status Area");
		
		Group group = new Group(this, SWT.NONE);
		group.setBounds(41, 69, 369, 126);
		
		Label label = new Label(this, SWT.NONE);
		label.setText("Current Time:");
		label.setBounds(41, 211, 80, 14);
		
		theTime = new Label(this, SWT.NONE);
		theTime.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
		theTime.setBounds(41, 234, 80, 14);
		
		attempts = new Label(this, SWT.NONE);
		attempts.setAlignment(SWT.CENTER);
		attempts.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
		attempts.setText( "0" );
		attempts.setBounds(317, 234, 80, 14);
		
		Label lblAttempts = new Label(this, SWT.NONE);
		lblAttempts.setText("Attempts:");
		lblAttempts.setBounds(328, 211, 80, 14);

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
	
	public void setStatus( String status ) {
		theStatus.setText( status );
	}
	
	public void setAttempts( int num ) {
		attempts.setText(Integer.toString(num));
	}
	
	// increment attempt counter
	public void increaseAttempts() {
		String countStr = attempts.getText();
		int count = Integer.parseInt(countStr);
		count++;
		setAttempts( count );		
	}


}
