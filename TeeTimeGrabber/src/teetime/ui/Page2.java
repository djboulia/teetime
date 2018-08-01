package teetime.ui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Combo;


public class Page2 extends Composite {

	private Label theTime;
	private DateTime dateTime;
	private Combo teeTime;

	// these combo boxes are tied together such that selecting a course
	// in one of the boxes will affect choices in the others. this is
	// handled by registering selection listeners on course1 and course2
	Combo course1 = new Combo(this, SWT.READ_ONLY);
	Combo course2 = new Combo(this, SWT.READ_ONLY);
	Combo course3 = new Combo(this, SWT.READ_ONLY);
	
	private String [][] courses2nd = { 	{"None", "Highlands", "Meadows", "Fairways"},
										{"None", "Meadows", "Fairways"},
										{"None", "Highlands", "Fairways"},
										{"None", "Highlands", "Meadows"}
								   	 };
	
	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public Page2(Composite parent, int style) {
		super(parent, style);
		
		Label lblPrestonwoodTeeTime = new Label(this, SWT.CENTER);
		lblPrestonwoodTeeTime.setText("Prestonwood Tee Time Selection");
		lblPrestonwoodTeeTime.setFont(SWTResourceManager.getFont("Lucida Grande", 14, SWT.BOLD));
		lblPrestonwoodTeeTime.setBounds(54, 10, 343, 24);
		
		Label lblTimePreference = new Label(this, SWT.NONE);
		lblTimePreference.setText("Time Preference:");
		lblTimePreference.setBounds(31, 47, 177, 14);
		
		Label label_1 = new Label(this, SWT.SEPARATOR);
		label_1.setText("sep");
		label_1.setBounds(224, 47, 2, 201);
		
		Label lblCoursePreference = new Label(this, SWT.NONE);
		lblCoursePreference.setText("Course Preference:");
		lblCoursePreference.setBounds(243, 47, 177, 14);
		
		Label lblStartingTeeTime = new Label(this, SWT.NONE);
		lblStartingTeeTime.setText("Starting Tee Time:");
		lblStartingTeeTime.setBounds(31, 67, 177, 14);
		
		Label lblDate = new Label(this, SWT.NONE);
		lblDate.setText("Date:");
		lblDate.setBounds(31, 115, 42, 14);
		
		dateTime = new DateTime(this, SWT.BORDER | SWT.CALENDAR | SWT.SHORT);
		dateTime.setFont(SWTResourceManager.getFont("Lucida Grande", 11, SWT.NORMAL));
		dateTime.setBounds(31, 135, 157, 146);

		final Label labelDate = new Label(this, SWT.NONE);
		labelDate.setBounds(79, 115, 92, 14);
		labelDate.setText( getDate() );

		// add a listener that updates the labelDate field when the
		// calendar widget value changes
		dateTime.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				System.err.println("default selected");
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				labelDate.setText( getDate() );
			}
			
		});
		
		Label label = new Label(this, SWT.NONE);
		label.setText("Current Time:");
		label.setBounds(185, 285, 80, 14);
		
		theTime = new Label(this, SWT.NONE);
		theTime.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
		theTime.setBounds(185, 308, 80, 14);
		
		course1.setItems(new String[] {"Any", "Highlands", "Meadows", "Fairways"});
		course1.setBounds(261, 87, 136, 22);
		course1.select(0);
		course1.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				System.err.println("default selected");
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (course1.getSelectionIndex()!=0) {
					// enable course2, set the remaining selections
					course2.setEnabled(true);
					course2.setItems(courses2nd[course1.getSelectionIndex()]);
					course2.select(0);
				} else {
					// disable course2 and course3, set them to None
					course2.setEnabled(false);
					course2.select(0);
				}

				course3.setEnabled(false);
				course3.select(0);
			}
			
		});
		
		course2.setEnabled(false);
		course2.setItems(new String[] {"None"});
		course2.setBounds(261, 158, 136, 22);
		course2.select(0);
		course2.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				System.err.println("default selected");
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				System.err.println("course2 selected");
				
				if (course2.getSelectionIndex()!=0) {
					course3.setEnabled(true);

					int selCourse1 = course1.getSelectionIndex();
					switch( selCourse1 ) {
					case 1: // Highlands
						course3.setItems((course2.getText().equals("Meadows")) 
								? new String[] {"None", "Fairways"} 
								: new String[] {"None", "Meadows"});
						course3.select(0);
						break;
					case 2: // Meadows
						course3.setItems((course2.getText().equals("Highlands")) 
								? new String[] {"None", "Fairways"} 
								: new String[] {"None", "Highlands"});
						course3.select(0);
						break;
					case 3: // Fairways
						course3.setItems((course2.getText().equals("Highlands")) 
								? new String[] {"None", "Meadows"} 
								: new String[] {"None", "Highlands"});
						course3.select(0);
						break;
					default:
						System.err.println("Unexpected value " + course2.getText() + " for course2!");
					}
				} else {
					// disable course3, set them to None
					course3.setEnabled(false);
					course3.select(0);
				}
			}
			
		});
		
		
		course3.setEnabled(false);
		course3.setItems(new String[] {"None"});
		course3.setBounds(261, 226, 136, 22);
		course3.select(0);
		
		Label lblFirstChoice = new Label(this, SWT.NONE);
		lblFirstChoice.setText("First Choice:");
		lblFirstChoice.setBounds(261, 67, 136, 14);
		
		Label lblSecondChoice = new Label(this, SWT.NONE);
		lblSecondChoice.setText("Second Choice:");
		lblSecondChoice.setBounds(261, 138, 136, 14);
		
		Label lblThirdChoice = new Label(this, SWT.NONE);
		lblThirdChoice.setText("Third Choice:");
		lblThirdChoice.setBounds(261, 207, 136, 14);
		
		teeTime = new Combo(this, SWT.READ_ONLY);
		teeTime.setItems(new String[] {"7:30 AM", "7:40 AM", "7:50 AM", "8:00 AM", "8:10 AM", "8:20 AM", "8:30 AM", "8:40 AM", "8:50 AM", "9:00 AM", "9:10 AM", "9:20 AM", "9:30 AM", "9:40 AM", "9:50 AM", "10:00 AM", "10:10 AM", "10:20 AM", "10:30 AM", "10:40 AM", "10:50 AM", "11:00 AM", "11:10 AM", "11:20 AM", "11:30 AM", "11:40 AM", "11:50 AM", "12:00 PM"});
		teeTime.setBounds(31, 87, 107, 22);
		teeTime.select(0);
		
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
	
	public String getDate( ) {
		int month = dateTime.getMonth() + 1;
		return month + "/" + dateTime.getDay() + "/" + dateTime.getYear();
	}

	public String getTime( ) {
		System.err.println("getTime: " + teeTime.getText());
		return teeTime.getText();
	}
	
	/**
	 * 
	 * @return Any, Highlands, Meadows or Fairways
	 */
	public String getCourse1() {
		return course1.getText();
	}

	/**
	 * 
	 * @return None, Highlands, Meadows or Fairways
	 */
	public String getCourse2() {
		return course2.getText();
	}

	/**
	 * 
	 * @return None, Highlands, Meadows or Fairways
	 */
	public String getCourse3() {
		return course3.getText();
	}

}
