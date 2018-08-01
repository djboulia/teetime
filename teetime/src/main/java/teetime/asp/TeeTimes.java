package teetime.asp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import teetime.CoursePreferences;
import teetime.TimeSlots;
import teetime.TimeSlot;
import webscraper.net.WebSession;
import webscraper.net.request.FormRequest;
import webscraper.net.request.WebRequest;

/**
 * Manage page interactions for searching a given date for available tee times
 * 
 * @author djboulia
 *
 */
public class TeeTimes extends AspAction {
	private TimeSlots availableTimes;

	private static final Pattern patTable = Pattern.compile("<table class=\"TT4SheetPlayers4\" (.*?)(</table>)");
	private static final Pattern patTimeList = Pattern.compile("<tr>(.*?)(</tr>)");
	private static final Pattern patTime = Pattern.compile(
			"<td (.*?) class=\"TT4Button teetime-Available w80\" data-parameter-teetimeid=\"(.*?)\" data-action=\"BookTime\" (.*?)<div class=\"ButtonContent\"> (.*?) </div>(.*?)lblCourse\">(.*?)</span>(.*?)(</td>)");
	private static final Pattern patPlayers = Pattern.compile("<td class=\"TT4Play\">(.*?)(</td>)");

	public TeeTimes(WebSession session, String path) {
		super(session, path);
	}

	/**
	 * Available tee times are defined as those with at least one time slot still
	 * open. If you need a foursome, you will need to check that all four slots are
	 * really available. Use the TimeSlot.isEmpty() function to determine if all
	 * positions in the timeslot are available.
	 * 
	 * @return a list of time slots with at least one position available
	 */
	public TimeSlots getAvailableTimes() {
		return availableTimes;
	}

	public boolean send(Date theTime) {

		String result = loadPage(getPath());
		if (result == null) {
			return false;
		}
		
		// read in the page data; need to establish state with the server so that we can
		// then issue a request to search for available tee times. For the most part,
		// we only do this first request to get at the "viewstate" data returned to us
		// we parse that with parseViewState below
		
		try {
					
			// "2011|7|28"
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy|M|dd");
			String selDate = formatter.format(theTime);

			// "2011|7"
			formatter = new SimpleDateFormat("yyyy|M");
			String monthView = formatter.format(theTime);

			// "7/28/2011"
			formatter = new SimpleDateFormat("M/dd/yyyy");
			String filterDate = formatter.format(theTime);

			// this actually issues the request to search for tee times on a given date
			FormRequest form = getSession().createFormRequest(WebRequest.METHOD_POST, getPath());
			form.addParameter("__EVENTARGUMENT", "");
			form.addParameter("__EVENTTARGET",
					"p$lt$middlebody$pageplaceholder$p$lt$holder$p$lt$ttTeeSheet$AdvanceFilter$btnFind");
			form.addParameter("__VIEWSTATE", getViewState());
			form.addParameter("__VIEWSTATEGENERATOR", getViewStateGenerator());
			form.addParameter("__SCROLLPOSITIONX", "0");
			form.addParameter("__SCROLLPOSITIONY", "247");
			form.addParameter("manScript_HiddenField", "");
			form.addParameter("lng", "en-US");
			form.addParameter("DES_Group", "DATEGROUP");
			form.addParameter("DES_SharedNames", 
					"PopupCal=p_lt_middlebody_pageplaceholder_p_lt_holder_p_lt_ttTeeSheet_Dates_desCalendar_PN|PopupMYPForCal_MANY=p_lt_middlebody_pageplaceholder_p_lt_holder_p_lt_ttTeeSheet_Dates_desCalendar_PN_MYP_PN|CalendarGeneratorHtmlClient`2=p_lt_middlebody_pageplaceholder_p_lt_holder_p_lt_ttTeeSheet_AdvanceFilter_desDate_PU_PN|CalendarContextMenu_Menu_NestedCalendarForDateTextBoxdesSpecialDates=p_lt_middlebody_pageplaceholder_p_lt_holder_p_lt_ttTeeSheet_AdvanceFilter_desDate_PU_PN_CM|StandardDateTextBoxContextMenu_MenudesSpecialDates=p_lt_middlebody_pageplaceholder_p_lt_holder_p_lt_ttTeeSheet_AdvanceFilter_desDate_CM");
			form.addParameter("DES_ScriptFileIDState", 
					"0|1|2|4|7|8|9|12|15|16|17|18|19|20|22|24|31|32|33|34|38|41|42|44|45|47|49|54|57");
			form.addParameter(
					"p$lt$middlebody$pageplaceholder$p$lt$holder$p$lt$ttTeeSheet$AdvanceFilter$desDate_PU_PN_SelVal",
					selDate);
			form.addParameter(
					"p$lt$middlebody$pageplaceholder$p$lt$holder$p$lt$ttTeeSheet$AdvanceFilter$desDate_PU_PN_MonthView",
					monthView);
			form.addParameter(
					"p$lt$middlebody$pageplaceholder$p$lt$holder$p$lt$ttTeeSheet$Dates$desCalendar_PN_MYP_PN_SelVal",
					"");
			form.addParameter(
					"p$lt$middlebody$pageplaceholder$p$lt$holder$p$lt$ttTeeSheet$Dates$desCalendar_PN_SelVal",
					"");
			form.addParameter(
					"p$lt$middlebody$pageplaceholder$p$lt$holder$p$lt$ttTeeSheet$Dates$desCalendar_PN_MonthView",
					monthView);
			form.addParameter(
					"p_lt_middlebody_pageplaceholder_p_lt_holder_p_lt_ttTeeSheet_menuNavigation_SelectedTab",
					"0");
			form.addParameter(
					"p$lt$middlebody$pageplaceholder$p$lt$holder$p$lt$ttTeeSheet$Dates$desCalendar_Value",
					"");
			form.addParameter(
					"p$lt$middlebody$pageplaceholder$p$lt$holder$p$lt$ttTeeSheet$AdvanceFilter$desDate",
					filterDate);
			form.addParameter(
					"p$lt$middlebody$pageplaceholder$p$lt$holder$p$lt$ttTeeSheet$AdvanceFilter$desDate_PU_Value",
					"");
			form.addParameter(
					"p$lt$middlebody$pageplaceholder$p$lt$holder$p$lt$ttTeeSheet$AdvanceFilter$rblDayTime",
					"0");
			form.addParameter(
					"p$lt$middlebody$pageplaceholder$p$lt$holder$p$lt$ttTeeSheet$AdvanceFilter$cblCourse$0",
					"118");
			form.addParameter(
					"p$lt$middlebody$pageplaceholder$p$lt$holder$p$lt$ttTeeSheet$AdvanceFilter$cblCourse$1",
					"119");
			form.addParameter(
					"p$lt$middlebody$pageplaceholder$p$lt$holder$p$lt$ttTeeSheet$AdvanceFilter$cblCourse$2",
					"120");

			boolean success = form.connect();
			if (!success) {
				System.err.println("Error searching for tee times");
				return false;
			}

			result = form.getLastResult();
			availableTimes = parseTimes(result, theTime);
			// System.err.println( _availableTimes );

			// System.out.println( result );
		} catch (java.io.IOException ioe) {
			ioe.printStackTrace();
		}

		return true;
	}

	public static TimeSlots parseTimes(String data, Date theDate) throws java.io.IOException {

		TimeSlots list = new TimeSlots();

		// normalize all white space to make our regex search patterns work smoothly
		String str = data.replaceAll("\\s+", " ");
//		System.err.println(" str = " + str);
		
		// find the outer table with all tee times
		Matcher matcher = patTable.matcher(str);
		
		if (!matcher.find() || matcher.groupCount()!=2) {
			System.err.println("Error = couldn't find tee time table");
			return list;
		}
		
		str = matcher.group(1);
//		System.err.println("table str = " + str);

		// find all tee times
		matcher = patTimeList.matcher(str);

		while (matcher.find()) {

			if (matcher.groupCount() != 2) {
				// some sort of parse error -- abort
				System.err.println("Unexpected format in results!");
				for (int i = 0; i <= matcher.groupCount(); i++) {
					System.err.println("group " + i + ": [" + matcher.group(i) + "]");
				}
				throw new java.io.IOException("Bad format");
			}

			// if this row contains an available tee time, process it
			String strTimeSlot = matcher.group(1);
			if (strTimeSlot.contains("TT4Button teetime-Available w80")) {
				
//				for (int i = 0; i <= matcher.groupCount(); i++) {
//					System.err.println("group " + i + ": [" + matcher.group(i) + "]");
//				}
				
				TimeSlot ts = parseTime(matcher.group(1), theDate);
				list.add(ts);
			}
			
		}

		return list;
	}

	public static TimeSlot parseTime(String data, Date theDate) throws java.io.IOException {

		TimeSlot ts = new TimeSlot();

		// normalize all white space to make our regex search patterns work smoothly
		String str = data.replaceAll("\\s+", " ");
		// System.err.println( " str = " + str );

		// find all tee time data
		Matcher mTimes = patTime.matcher(str);

		if (!mTimes.find()) {
			System.err.println("Invalid format for parseTime: str = " + str);
			System.err.println("--> regex : " + patTime.pattern());
			return null;
		}

		if (mTimes.groupCount() != 8) {
			// some sort of parse error -- abort
			System.err.println("Unexpected format in results!");
			for (int i = 0; i <= mTimes.groupCount(); i++) {
				System.err.println("group " + i + ": [" + mTimes.group(i) + "]");
			}
			throw new java.io.IOException("Bad format");
		}

		String teeTimeId = mTimes.group(2);
		String time = mTimes.group(4);
		String course = mTimes.group(6);

//		System.err.println( "found teetime -- time:" + time + ", id:" + teeTimeId + ", course:" + course + "--");

		ts.setId(teeTimeId);
		ts.setCourse(CoursePreferences.findCourseByName(course));

		DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy h:mm a");
		try {
			
			// the parsed info only has the current time... but we know the date we
			// requested in the
			// search, so use that to build a complete date and time
			Calendar gcalendar = new GregorianCalendar();
			gcalendar.setTime(theDate);

			int month = gcalendar.get(Calendar.MONTH) + 1; // month val returned is zero based

			String strDate = month + "/" + gcalendar.get(Calendar.DAY_OF_MONTH) 
					+ "/" + gcalendar.get(Calendar.YEAR)
					+ " " + time;

			Date teeTime = (Date) formatter.parse(strDate);
			ts.setTime(teeTime);
		} catch (Exception e) {
			System.err.println("Failed to parse tee time date.");
		}

		// find names of all the players in the foursome
		Matcher mNames = patPlayers.matcher(str);

		int names = 0;
		while (mNames.find() && names < 4) {

			if (mNames.groupCount() != 2) {
				// some sort of parse error -- abort
				System.err.println("Unexpected format in results!");
				for (int i = 0; i <= mNames.groupCount(); i++) {
					System.err.println("group " + i + ": [" + mNames.group(i) + "]");
				}
				throw new java.io.IOException("Bad format");
			}
			// System.err.println( "found player -- " + mNames.group(1) + "--" );

			ts.setPlayer(names, mNames.group(1));

			names++;
		}

		System.err.println(ts);
		return ts;
	}
}
