package teetime;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Collections;
import java.util.Comparator;

/**
 * A list of available tee time slots with utility functions for 
 * sorting and filtering the list based on time and course preferences
 * 
 * @author djboulia
 *
 */
public class TimeSlots {
	
	private ArrayList<TimeSlot> times;
	
	/**
	 * Custom sort function which respects course preference order as well as 
	 * tee time order
	 *
	 */
	private class CoursePrefsComparator implements Comparator<TimeSlot> {
		CoursePreferences prefs;
		
		public CoursePrefsComparator( CoursePreferences prefs ) {
			this.prefs = prefs;
		}
		
		public int compare(TimeSlot ts1, TimeSlot ts2) {
			if (ts1.getTime().after(ts2.getTime())) {
				return 1;
			} else if (ts1.getTime().before(ts2.getTime())) {
				return -1;
			} else {
				// tee times are equal, compare course preferences
				int pref1 = prefs.coursePreference(ts1.getCourse());
				int pref2 = prefs.coursePreference(ts2.getCourse());
				
				if (pref1>pref2) {
					return 1;
				} else if (pref1<pref2) {
					return -1;
				}

				return 0;				
			}
		}
	}
	
	public TimeSlots() {
		times = new ArrayList<TimeSlot>();
	}
	
	/**
	 * Pare down and sort the list based on time and course preferences
	 * 
	 * @param theTime
	 *            the preferred start time; times prior to this will be filtered out
	 * @param prefs
	 *            current preferences for courses
	 */
	public void filter(Date theTime, CoursePreferences prefs) {
		
		if (theTime == null) {
			System.err.println("TimeSlots.filter: No time set!");
			return;
		}
		
		if (prefs == null) {
			System.err.println("TimeSlots.filter: No course preferences set!");		
			return;
		}
		
		Iterator<TimeSlot> it = times.iterator();
		
		while (it.hasNext()) {
			TimeSlot ts = it.next();
	
			if (ts.getTime().before(theTime)) {
				it.remove();
			} else if (!prefs.isCoursePreferred(ts.getCourse())) {
				it.remove();
			}
		}
		
		// now sort the remaining times so that the most preferred courses are first
		CoursePrefsComparator comparator = new CoursePrefsComparator(prefs);
		Collections.sort(times, comparator);
	}
	
	public Iterator<TimeSlot> iterator() {
		return times.iterator();
	}
	
	public boolean add( TimeSlot ts ) {
		return times.add(ts);
	}
	
	public String toString() {
		String result = "";
		
		Iterator<TimeSlot> it = iterator();
		
		while (it.hasNext()) {
			TimeSlot ts = it.next();
			result += ts.toString() + "\n";
		}
		
		return result;
	}

}
