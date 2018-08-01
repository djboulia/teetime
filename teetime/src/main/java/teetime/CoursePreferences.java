package teetime;

public class CoursePreferences {
	public static final int NONE = 0;
	public static final int FAIRWAYS = 0x01;
	public static final int MEADOWS = 0x02;
	public static final int HIGHLANDS = 0x04;

	private int prefs[] = {NONE,NONE,NONE};

	public CoursePreferences(int course1, int course2, int course3) {
		this.prefs[0] = course1;
		this.prefs[1] = course2;
		this.prefs[2] = course3;
	}

	/**
	 * 
	 * @param course1
	 *            - the name of the first course, or Any if all courses are in play
	 * @param course2
	 *            - the name of the second preferred course, or None if only one
	 *            course is preferred
	 * @param course3
	 *            - the name of the third preferred course, or None if only two
	 *            courses are preferred
	 */
	public CoursePreferences(String course1, String course2, String course3) {

		if (course1.equalsIgnoreCase("any")) {	// no prefereence, just pick three
			prefs[0] = FAIRWAYS;
			prefs[1] = HIGHLANDS;
			prefs[2] = MEADOWS;
		} else {
			prefs[0] = findCourseByName(course1);
			
			if (!course2.equalsIgnoreCase("none") && isValidCourseName(course2)) {
				prefs[1] = findCourseByName(course2);

				if (!course3.equalsIgnoreCase("none") && isValidCourseName(course3)) {
					prefs[2] = findCourseByName(course3);
				}
			}
		}
	}

	public boolean isCoursePreferred(int course) {
		return coursePreference(course) > 0;
	}
	
	/**
	 * 
	 * check the preference level of this course 
	 * 
	 * @param course the course to test
	 * @return 0 if the course isn't preferred, 1 if it's the first choice, 2 for second, 3 for third
	 */
	public int coursePreference(int course) {
		if (prefs[0] == course) {
			return 1;
		} else if (prefs[1] == course) {
			return 2;
		} else if (prefs[2] == course) {
			return 3;
		} 
		
		return 0;
	}

	/**
	 * 
	 * check the preference level of this course 
	 * 
	 * @param course the course to test
	 * @return 0 if the course isn't preferred, 1 if it's the first choice, 2 for second, 3 for third
	 */
	public int coursePreference(String course) {
		return coursePreference( findCourseByName(course) );
	}

	public static int findCourseByName(String course) {
		if (course.equalsIgnoreCase("meadows"))
			return MEADOWS;
		if (course.equalsIgnoreCase("highlands"))
			return HIGHLANDS;
		if (course.equalsIgnoreCase("fairways"))
			return FAIRWAYS;
		System.err.println("CoursePreferences.findCourseByName: error, invalid course " + course + " found!");
		return -1;
	}

	public boolean isValidCourseName(String course) {
		return findCourseByName(course) >= 0;
	}
	
	public String[] getCourseList() {
		String list[] = new String[3];
		
		list[0] = toString(prefs[0]);
		list[1] = toString(prefs[1]);
		list[2] = toString(prefs[2]);
		
		return list;
	}

	public String toString() {
		String str = "";

		str += toString(prefs[0]) + ",";
		str += toString(prefs[1]) + ",";
		str += toString(prefs[2]);
		
		return str;
	}

	public static String toString(int course) {
		String str = "";

		switch (course) {
		case NONE:
			str = "None";
			break;

		case FAIRWAYS:
			str = "Fairways";
			break;

		case MEADOWS:
			str = "Meadows";
			break;

		case HIGHLANDS:
			str = "Highlands";
			break;

		default:
			str = "Unknown";
		}
		return str;
	}
}
