
/**
 * TeeTimeGrabber.java
 * 
 * command line utility for grabbing tee times.  See TeeTime.java for a GUI based interface.
 * 
 */

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import teetime.Golfer;
import teetime.Golfers;
import teetime.TimeSlots;
import teetime.CoursePreferences;
import teetime.TeeTime;

public class TeeTimeCmdLine {

	/**
	 * Get tomorrow's date
	 * 
	 * @param time
	 *            integer from 0 to 23 indicating the time of day
	 * @return a date object for tomorrow at the specified time
	 */
	public static Date getTomorrowAt(int time) {
		// today
		Calendar cal = new GregorianCalendar();

		// reset hour, minutes, seconds and millis
		cal.set(Calendar.HOUR_OF_DAY, time);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		// next day
		cal.add(Calendar.DAY_OF_MONTH, 1);

		return cal.getTime();
	}

	/**
	 * Get the date specified by str in a format like this: 8:00 AM 08/14/2017
	 * 
	 * @param str
	 *            the date string to parse
	 * @return a date object for the time specified in str
	 */
	public static Date getDate(String str) throws ParseException {
		DateFormat formatter = new SimpleDateFormat("h:mm a MM/dd/yyyy");
		Date theDate = formatter.parse(str);

		return theDate;
	}

	public static void doReservation(TeeTime session, Date theDate, Golfers golfers) {
		boolean retry = false;
//		boolean retry = true;

		do {
			if (session.reserve(theDate, golfers) != null) {
				System.out.println("Got the reservation!");
				retry = false;
			} else {
				System.err.println(session.getErrorMessage());
				
				if (retry) {
					System.err.println("No times available, retrying...");
				} else {
					System.err.println("No times available!");
				}
			}
		} while (retry);

	}

	public static void doShowTeeTimes(TeeTime session, Date theDate) {
		TimeSlots slots = session.getMatchingTeeTimes(theDate);

		System.err.println("Found Time Slots:");
		System.err.println(slots.toString());
	}
	
	public static void testShowTeeTimes( TeeTime session, CoursePreferences coursePrefs, Golfers golfers ) {
		try {
			// Date theDate = getDate( "11:50 AM 9/7/2017" );
			Date theDate = getTomorrowAt(8);
			System.out.println("Showing tee times at: " + theDate.toString());
			System.out.println("With course preferences: " + coursePrefs.toString());

			// doReservation(session, theDate, golfers);
			doShowTeeTimes(session, theDate);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void testReservation( TeeTime session, CoursePreferences coursePrefs, Golfers golfers ) {
		try {
			// Date theDate = getDate( "11:50 AM 9/7/2017" );
			Date theDate = getTomorrowAt(8);
			System.out.println("Reserving tee time at: " + theDate.toString());
			System.out.println("With course preferences: " + coursePrefs.toString());
			System.out.println("And golfers: " + golfers.toString());

			 doReservation(session, theDate, golfers);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void testShowMembers( TeeTime session, String name ) {
		try {
			Golfers golfers = session.getMemberList(name);
			if ( golfers != null && golfers.size() > 0) {
				System.out.println("Found members:");
				System.out.println(golfers.toString());
			} else {
				System.out.println("No members found!");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void testMemberName( TeeTime session ) {
		try {
			Golfer golfer = session.getMemberInfo();
			if ( golfer != null ) {
				System.out.println("Found golfer:" + golfer.toString());
			} else {
				System.out.println("Name not found!");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Logger logger = Logger.getLogger("zero.util.net");
		logger.setLevel(Level.ALL);

		Golfers golfers = new Golfers();
		golfers.add(new Golfer("Donald Boulia", "593198"));
		golfers.add(new Golfer("Kirsten Boulia", "596924"));
		golfers.add(new Golfer("Lauren Boulia", "596926"));
		golfers.add(new Golfer("Ryder Boulia", "596927"));

		CoursePreferences coursePrefs = new CoursePreferences(
				CoursePreferences.MEADOWS, 
				CoursePreferences.HIGHLANDS,
				CoursePreferences.FAIRWAYS);

		TeeTime session = new TeeTime("prestonwood.com");
		session.setCoursePreferences(coursePrefs);

		String userid = "djboulia";
		String password = "tig1ger";

		if (session.login(userid, password)) {
			System.out.println("Logged in as " + userid);
			
			testReservation(session, coursePrefs, golfers);
//			testShowTeeTimes(session, coursePrefs, golfers);
//			testShowMembers(session, "boulia");
//			testMemberName(session);

			session.logout();
		}

	}

}
