
/**
 * TeeTimeUI.java
 * 
 * SWT based interface for grabbing tee times.  See TeeTimeCmdLine.java for a command line version
 * 
 */
import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import teetime.ui.*;
import teetime.Golfer;
import teetime.Golfers;
import teetime.TimeSlot;

public class TeeTimeUI {

	static final int WIDTH = 450;
	static final int HEIGHT = 400;

	static int pageNum = 0;
	static StackLayout layout;
	static Composite contentPanel;

	static ArrayList<Composite> pages = new ArrayList<Composite>();

	public static void main(String[] args) {
		final Display display = new Display();
		final Shell shell = new Shell(display);
		
		shell.setText("Tee Time Reservation");
		Rectangle clientArea = shell.getClientArea();
		shell.setBounds(clientArea.x + 10, clientArea.y + 10, WIDTH, HEIGHT);
		
		// create the composite that the pages will share
		contentPanel = new Composite(shell, SWT.BORDER);
		contentPanel.setBounds(clientArea.x + 10, clientArea.y + 10, WIDTH - 20, HEIGHT - 70);
		layout = new StackLayout();
		contentPanel.setLayout(layout);

		// create the first page's content
		final Page1 page1 = new Page1(contentPanel, SWT.NONE);
		pages.add(page1);

		// create the second page's content
		final Page2 page2 = new Page2(contentPanel, SWT.NONE);
		pages.add(page2);

		// create the third page's content
		final Page3 page3 = new Page3(contentPanel, SWT.NONE);
		pages.add(page3);

		// create the fourth page's content
		final PageSuccess pageSuccess = new PageSuccess(contentPanel, SWT.NONE);
		pages.add(pageSuccess);

		// create the fifth page's content
		final PageFailure pageFailure = new PageFailure(contentPanel, SWT.NONE);
		pages.add(pageFailure);

		// create the button that will switch between the pages
		Button pageButton = new Button(shell, SWT.PUSH);
		pageButton.setText("OK");
		pageButton.setBounds(clientArea.x + (WIDTH / 2 - 40), clientArea.y + (HEIGHT - 50), 80, 25);
		pageButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {

				// do some pre-checking of values before moving on to next page
				Composite page = pages.get(pageNum);
				if (page instanceof Page1) {
					// validate there is actually a userid/pw combo
					if (page1.getUserid().equals("") || page1.getPassword().equals("")) {
						page1.setMsg("Please enter a valid userid and password");
						return;
					}
				} else if (page instanceof Page3) {
					// if someone presses the button from the GetTeeTime page, then it's effectively
					// a cancel
					// skip to the failure page
					pageNum++;
				} else if (page instanceof PageSuccess) {
					// skip the failure page if we get here
					pageNum++;
				}

				// advance to the next page
				pageNum++;

				// when we hit the last page, exit the app
				if (pageNum < pages.size()) {
					page = pages.get(pageNum);
					layout.topControl = page;
					contentPanel.layout();

					// process current page data
					if (page instanceof Page3) {
						try {
							DateFormat formatter = new SimpleDateFormat("h:mm a MM/dd/yyyy");
							Date teeTime = formatter.parse(page2.getTime() + " " + page2.getDate());
							teetime.CoursePreferences prefs = new teetime.CoursePreferences(page2.getCourse1(),
									page2.getCourse2(), page2.getCourse3());

							System.err.println("tee time: " + teeTime);
							System.err.println("courses: " + prefs);

							getTeeTime(page1.getUserid(), page1.getPassword(), teeTime, prefs);
						} catch (ParseException e) {
							e.printStackTrace();
						}
					}

				} else {
					shell.dispose();
				}

			}
		});

		layout.topControl = pages.get(0);
		contentPanel.layout();

		// build a timer to show the current time
		final int time = 500;

		Runnable timer = new Runnable() {
			public void run() {

				Date now = new Date();

				Composite page = pages.get(pageNum);
				if (page instanceof Page1) {
					Page1 page1 = (Page1) page;
					page1.setCurrentTime(now);
				} else if (page instanceof Page2) {
					Page2 page2 = (Page2) page;
					page2.setCurrentTime(now);
				} else if (page instanceof Page3) {
					Page3 page3 = (Page3) page;
					page3.setCurrentTime(now);
				}

				display.timerExec(time, this);
			}
		};
		display.timerExec(time, timer);

		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	public static void getTeeTime(final String userid, final String password, final Date teeTime,
			final teetime.CoursePreferences prefs) {

		// at this point we can kick off the background process
		Job job = new Job("My Job") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {

				// Do something long running
				// ...
				System.err.println("Starting long running job");

				boolean retry = true;

				Logger logger = Logger.getLogger("zero.util.net");
				logger.setLevel(Level.ALL);

				statusMsg("Attempting to reserve tee times on " + prefs);
				newAttempt();

				Golfers golfers = new Golfers();
				golfers.add( new Golfer("Donald Boulia", "593198") );
				golfers.add( new Golfer("Kirsten Boulia", "596924") );
				golfers.add( new Golfer("Lauren Boulia", "596926") );
				golfers.add( new Golfer("Ryder Boulia", "596927") );

				teetime.TeeTime session = new teetime.TeeTime("prestonwood.com");
				session.setCoursePreferences(prefs);

				statusMsg("Logging in...");

				if (session.login(userid, password)) {

					statusMsg("Logged in as " + userid);

					try {

						do {
							TimeSlot ts = session.reserve(teeTime, golfers);
							if (ts != null) {
								statusMsg("Got the reservation!");
								System.out.println("Got the reservation!");

								// now that we have the reservation, advance to next page
								successMsg(ts);

								retry = false;
							} else {
								if (retry) {
									newAttempt();
									statusMsg("No times available, retrying...");
									System.out.println("No times available, retrying...");
								} else {
									statusMsg("No times available!");
									System.out.println("No times available!");
								}
							}
						} while (retry);

					} catch (Exception e) {
						e.printStackTrace();
						failurePage();
					}

					session.logout();
				} else {
					failurePage();
				}

				System.err.println("Ending long running job");
				return Status.OK_STATUS;
			}
		};

		// Start the Job
		job.schedule();

	}

	public static Page1 getPage1() {
		Composite page = pages.get(0);
		if (!(page instanceof Page1)) {
			throw new IllegalArgumentException("getPage1 - unexpected value!");
		}

		return (Page1) page;
	}

	public static Page3 getPage3() {
		Composite page = pages.get(2);
		if (!(page instanceof Page3)) {
			throw new IllegalArgumentException("getPage3 - unexpected value!");
		}

		return (Page3) page;
	}

	public static PageSuccess getPageSuccess() {
		Composite page = pages.get(3);
		if (!(page instanceof PageSuccess)) {
			throw new IllegalArgumentException("getPageSuccess - unexpected value!");
		}

		return (PageSuccess) page;
	}

	public static PageFailure getPageFailure() {
		Composite page = pages.get(4);
		if (!(page instanceof PageFailure)) {
			throw new IllegalArgumentException("getPageSuccess - unexpected value!");
		}

		return (PageFailure) page;
	}

	public static void statusMsg(String status) {
		final Page3 page3 = getPage3();
		final String statusString = status;

		// so we can call this from non UI threads we use asyncExec
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				page3.setStatus(statusString);
			}
		});
	}

	public static void newAttempt() {
		final Page3 page3 = getPage3();

		// so we can call this from non UI threads we use asyncExec
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				page3.increaseAttempts();
			}
		});
	}

	public static void successMsg(final TimeSlot ts) {
		final Page1 page1 = getPage1();
		final PageSuccess pageSuccess = getPageSuccess();

		// so we can call this from non UI threads we use asyncExec
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				pageNum = 3;
				pageSuccess.setUserid(page1.getUserid());
				pageSuccess.setTimeSlot(ts);
				layout.topControl = pageSuccess;
				contentPanel.layout();
			}
		});
	}

	public static void failurePage() {
		final PageFailure pageFailure = getPageFailure();

		// so we can call this from non UI threads we use asyncExec
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				pageNum = 4;
				layout.topControl = pageFailure;
				contentPanel.layout();
			}
		});
	}
}
