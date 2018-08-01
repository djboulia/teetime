package teetime;

import java.util.Date;

public class TimeSlot {

	private Date time;
	private String id;
	private int course; // see CoursePreferences
	private String players[] = { "", "", "", "" };

	public TimeSlot() {
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public String getId() {
		return id; 
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getCourse() {
		return course;
	}

	public void setCourse(int course) {
		this.course = course;
	}

	public void setPlayer(int i, String str) {
		if (i < 0 || i > 3)
			throw new IllegalArgumentException("setPlayer index out of bounds!");

		players[i] = str;
	}

	/**
	 * @param i
	 *            player number 1, 2, 3, or 4
	 * @return true if the position is available, false otherwise
	 */
	public boolean PlayerPositionAvailable(int i) {

		if (i < 1 || i > 4)
			throw new IllegalArgumentException("Invalid parameter! Player position " + Integer.toString(i));

		if (players[i - 1].equalsIgnoreCase("available"))
			return true;
		else
			return false;
	}

	/**
	 * Check if the time slot has any players in it
	 * 
	 * @return true if all four positions are available in this time slot, false
	 *         otherwise
	 */
	public boolean isEmpty() {
		for (int i = 1; i <= players.length; i++) {
			if (!PlayerPositionAvailable(i))
				return false;
		}
		return true;
	}

	public String getPlayersAsString() {
		return "[" + players[0] + "|" + players[1] + "|" + players[2] + "|" + players[3] + "]";
	}

	public String toString() {
		return getTime() + " " + getId() + " " + CoursePreferences.toString(getCourse()) + " "
				+ getPlayersAsString();
	}
}
