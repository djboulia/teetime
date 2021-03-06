package teetime.ui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.Group;
import teetime.TimeSlot;


public class PageSuccess extends Composite {

	private String userid = "";
	private Label lblCourse;
	private Label lblTeeTime;
	private Label lblDate;
	private Label lblLocked;
	private Label lblInfo;
	
	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public PageSuccess(Composite parent, int style) {
		super(parent, style);
		
		Label lblSuccess = new Label(this, SWT.CENTER);
		lblSuccess.setText("Success!");
		lblSuccess.setFont(SWTResourceManager.getFont("Lucida Grande", 14, SWT.BOLD));
		lblSuccess.setBounds(48, 11, 343, 24);
		
		Group group = new Group(this, SWT.NONE);
		group.setBounds(95, 72, 249, 113);
		
		Label lbl1 = new Label(group, SWT.NONE);
		lbl1.setFont(SWTResourceManager.getFont("Lucida Grande", 11, SWT.BOLD));
		lbl1.setBounds(28, 28, 59, 14);
		lbl1.setText("Course:");
		
		Label lbl2 = new Label(group, SWT.NONE);
		lbl2.setFont(SWTResourceManager.getFont("Lucida Grande", 11, SWT.BOLD));
		lbl2.setText("Tee Time:");
		lbl2.setBounds(28, 48, 59, 14);
		
		Label lbl3 = new Label(group, SWT.NONE);
		lbl3.setFont(SWTResourceManager.getFont("Lucida Grande", 11, SWT.BOLD));
		lbl3.setText("Date:");
		lbl3.setBounds(28, 68, 59, 14);
		
		lblCourse = new Label(group, SWT.NONE);
		lblCourse.setFont(SWTResourceManager.getFont("Lucida Grande", 11, SWT.ITALIC));
		lblCourse.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
		lblCourse.setBounds(123, 28, 88, 14);
		
		lblTeeTime = new Label(group, SWT.NONE);
		lblTeeTime.setFont(SWTResourceManager.getFont("Lucida Grande", 11, SWT.ITALIC));
		lblTeeTime.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
		lblTeeTime.setBounds(123, 48, 88, 14);
		
		lblDate = new Label(group, SWT.NONE);
		lblDate.setFont(SWTResourceManager.getFont("Lucida Grande", 11, SWT.ITALIC));
		lblDate.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
		lblDate.setBounds(123, 68, 88, 14);
		
		lblLocked = new Label(this, SWT.NONE);
		lblLocked.setAlignment(SWT.CENTER);
		lblLocked.setBounds(36, 41, 367, 24);
		
		lblInfo = new Label(this, SWT.WRAP);
		lblInfo.setAlignment(SWT.CENTER);
		lblInfo.setBounds(75, 208, 289, 55);
		
		setLabels();
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	
	public void setUserid( String userid ) {
		this.userid = userid;
		setLabels();
	}
	
	public String getUserid() {
		return userid;
	}
	
	private void setLabels() {
		lblLocked.setText("The current tee time is now booked for user " + getUserid() + ":");
		lblInfo.setText("");
	}
	
	public void setTimeSlot( TimeSlot ts ) {
		lblCourse.setText( teetime.CoursePreferences.toString( ts.getCourse() ) );
		
		Date teeTime = ts.getTime();
		DateFormat formatter;
		
        formatter = new SimpleDateFormat("h:mm a");
		String time = formatter.format( teeTime );
		lblTeeTime.setText( time );

		formatter = new SimpleDateFormat("MM/dd/yyyy");
        String date = formatter.format( teeTime );
		lblDate.setText( date );
	}
}
