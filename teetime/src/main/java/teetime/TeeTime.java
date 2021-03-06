package teetime;

import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import webscraper.net.WebSession;
import teetime.ajax.BeginBooking;
import teetime.ajax.CompleteBooking;
import teetime.ajax.LockTeeTime;
import teetime.ajax.SearchMember;
import teetime.asp.Booking;
import teetime.asp.Groups;
import teetime.asp.Login;
import teetime.asp.MemberName;
import teetime.asp.TeeTimes;

public class TeeTime {

	private static final String PATH_LOGIN = "/login.aspx";
	private static final String PATH_TEETIME_BASE = "/TeeTimes";
	private static final String PAGE_TEETIME = "/TeeSheet.aspx";
	private static final String PAGE_BOOKING = "/Booking.aspx";
	private static final String PAGE_GROUPS = "/Groups.aspx";
	private static final String PATH_AJAX = "/CMSPages/CHO/AjaxMethodHandler.aspx/AjaxMethod";

	private boolean loggedIn;
	private String siteName;
	private CoursePreferences prefs;
	private String viewState;
	private WebSession session;
	private String errorMessage;

	/**
	 * 
	 * @param siteName base name of the URL, e.g. prestonwood.com, www.google.com, etc.
	 */
	public TeeTime(String siteName) {
		Logger log = Logger.getLogger("zero.util.net");
		log.setLevel(Level.OFF);

		this.siteName = siteName;
		
		startNewSession();
		
		loggedIn = false;
	}
	
	private void startNewSession() {
		session = new WebSession(getSecureUrl());
		session.setSaveCookies(true);
		session.setSaveHeaders(true);
	}
	
	private String getSecureUrl() {
		String baseUrl = "https://" + this.siteName;
		return baseUrl;
	}

	private String getUrl() {
		String baseUrl = "http://" + this.siteName;
		return baseUrl;
	}

	private WebSession getWebSession() {
		return session;
	}
	
	private void setErrorMessage(String str) {
		this.errorMessage = str;
	}

	public String getErrorMessage() {
		return this.errorMessage;
	}

	public boolean login(String userid, String password) {

		Login page = new Login(getWebSession(), PATH_LOGIN);
		
		boolean result = page.send(userid, password);
		if (result) {
			// remember the state variable for subsequent calls
			viewState = page.getViewState();
			loggedIn = true;
		}
		
		return result;
	}

	public void setCoursePreferences(CoursePreferences prefs) {
		this.prefs = prefs;
	}
	
	/**
	 * Return a list of golfers who match the given input string
	 * 
	 * @param name the last name to search against
	 * @return a list of Golfers that match the request
	 */
	public Golfers getMemberList(String name) {
		
		setErrorMessage(null);
		
		if (!loggedIn) {
			System.err.println("getMemberList: Log in first!");
			setErrorMessage("Not logged in");
			return null;
		}

		// have to revert to http vs. https at this point; the site redirects to http
		// after a successful login
		// and the built in HTTPConnection doesn't seem to follow the redirect with the
		// switch in protocol
		getWebSession().setBaseUrl(getUrl());

		String pathGroups = PATH_TEETIME_BASE + PAGE_GROUPS;
		
		Groups page = new Groups(getWebSession(), pathGroups);
		page.setViewState(viewState);
		
		if (page.send()) {

			viewState = page.getViewState();

			SearchMember ajax = new SearchMember(getWebSession(), PATH_AJAX);
			ajax.setReferer(pathGroups);
			
			if (ajax.send(name)) {
				Golfers result = ajax.getSearchResults();
				
				if (result == null) {
					setErrorMessage( ajax.getErrorMessage());
				}
				
				return result;
			}
		}
		
		return null;
	}

	
	/**
	 * Find tee times that match the given input
	 * 
	 * @param theTime the date and time we want.  Any times before this will be filtered out
	 * @return a list of TimeSlots that match the request
	 */
	public TimeSlots getMatchingTeeTimes(Date theTime) {
		
		setErrorMessage(null);

		if (!loggedIn) {
			System.err.println("getMatchingTeeTimes: Log in first!");
			setErrorMessage("Not logged in");
			return null;
		}

		// have to revert to http vs. https at this point; the site redirects to http
		// after a successful login
		// and the built in HTTPConnection doesn't seem to follow the redirect with the
		// switch in protocol
		getWebSession().setBaseUrl(getUrl());

		String path = PATH_TEETIME_BASE + PAGE_TEETIME;
		
		TeeTimes page = new TeeTimes(getWebSession(), path);
		page.setViewState(viewState);
		
		if (page.send(theTime)) {

			viewState = page.getViewState();

			TimeSlots list = page.getAvailableTimes();

			if (list!=null) {
				list.filter(theTime, prefs);
			}

			return list;
		}
		
		return null;
	}

	/**
	 * Find member information for the currently logged in user
	 * 
	 * @return a Golfer object
	 */
	public Golfer getMemberInfo() {
		setErrorMessage(null);

		if (!loggedIn) {
			System.err.println("getMemberInfo: Log in first!");
			setErrorMessage("Not logged in");
			return null;
		}

		// have to revert to http vs. https at this point; the site redirects to http
		// after a successful login
		// and the built in HTTPConnection doesn't seem to follow the redirect with the
		// switch in protocol
		getWebSession().setBaseUrl(getUrl());

		String path = PATH_TEETIME_BASE + PAGE_TEETIME;
		
		MemberName page = new MemberName(getWebSession(), path);
		page.setViewState(viewState);
		
		if (page.send()) {

			viewState = page.getViewState();

			String name = page.getUserName();
			if (name!=null) {
				String[] parts = name.split("\\s+");
				String lastName = parts[parts.length-1];
				
				// look up the last name via the member function
				Golfers golfers = this.getMemberList(lastName);
				
				Iterator<Golfer> it = golfers.iterator();
				
				while (it.hasNext()) {
					Golfer golfer = it.next();
					if (golfer.getName().compareToIgnoreCase(name) == 0) {
						return golfer;										
					}
				}
				
				System.out.println("Found golfers, but no match! " + golfers.toString());
				
			}
			
		}
		
		return null;
	}

	/**
	 * Attempt to book a tee time matching teeTime for the golfers given
	 * 
	 * @param theTime the date and time you want to book
	 * @param golfers the golfers to associate with this tee time
	 * @return the TimeSlot we reserved, or null if we couldn't reserve one
	 */
	public TimeSlot reserve(Date theTime, Golfers golfers) {
		setErrorMessage(null);
		
		TimeSlots list = getMatchingTeeTimes( theTime );
		
		if (list != null) {
			
			Iterator<TimeSlot> it = list.iterator();
			
			while (it.hasNext()) {
				TimeSlot ts = it.next();
				
				if (ts.isEmpty()) {
					System.out.println("Empty TimeSlot " + ts);

					// if we can't hold the time slot, it's likely because we're 
					// competing with someone else for locking it, or the tee
					// sheet isn't open yet.  We keep trying other possible time slots
					// until we get one that we can lock down
					
					if (holdTimeSlot(ts)) {
						System.out.println("Held time slot " + ts);
						
						if (completeBooking(golfers)) {
							return ts;
						} else {
							
							// if we can't complete the booking, we likely have 
							// some error with the tee time.  possibilities such
							// as the person already booked a tee time within
							// 4 hours of this tee time, or an invalid member name
							// if this happens, we don't keep trying
							return null;
						}
					}
				}
			}
		}

		return null;
	}

	private boolean holdTimeSlot(TimeSlot ts) {
		if (!loggedIn) {
			System.err.println("Session.getTimeSlot: Log in first!");
			setErrorMessage("Not logged in");
			return false;
		}

		// have to revert to http vs. https at this point; the site redirects to http
		// after a successful login
		getWebSession().setBaseUrl(getUrl());

		// first request a lock on the available tee time. this could fail, in which
		// case we return
		// false from the send() call. this could happen when another user has accessed
		// the page
		// and locked the tee time before we did
		String referer = PATH_TEETIME_BASE + PAGE_TEETIME;
		
		BeginBooking page = new BeginBooking(getWebSession(), PATH_AJAX);
		page.setReferer(referer);
		
		if (page.send(ts)) {

			// a successful lock of the tee time will return us a page for actually booking
			// the time. follow that redirect to finish booking
			String redirectPath = page.getRedirectPath();

			Booking pageBooking = new Booking(getWebSession(), redirectPath);
			
			if (pageBooking.send()) {
				viewState = pageBooking.getViewState();

				// obtain the lock via Ajax call
				LockTeeTime ajaxLock = new LockTeeTime(getWebSession(), PATH_AJAX);
				ajaxLock.setReferer(redirectPath);
				
				if (ajaxLock.send()) {
					// book the tee time?
					return true;
				} else {
					setErrorMessage(ajaxLock.getErrorMessage());
				}
			}
		} else {
			setErrorMessage(page.getErrorMessage());
		}

		return false;
	}

	private boolean completeBooking(Golfers golfers) {
		if (!loggedIn) {
			System.err.println("Session.completeBooking: Log in first!");
			return false;
		}

		// have to revert to http vs. https at this point; the site redirects to http
		// after a successful login
		getWebSession().setBaseUrl(getUrl());

		// first request a lock on the available tee time. this could fail, in which
		// case we return
		// false from the send() call. this could happen when another user has accessed
		// the page
		// and locked the tee time before we did
		String referer = PATH_TEETIME_BASE + PAGE_BOOKING;
		
		// obtain the lock via Ajax call
		CompleteBooking ajaxBooking = new CompleteBooking(getWebSession(), PATH_AJAX);
		ajaxBooking.setReferer(referer);
		
		if (ajaxBooking.send(golfers)) {
			// booked the tee time?
			return true;
		} else {
			setErrorMessage(ajaxBooking.getErrorMessage());
		}

		return false;
	}

	public void logout() {
		if (loggedIn) {
			
			// reset the session which will drop all existing credentials for the current
			// logged in user
			startNewSession();
			
			loggedIn = false;
		}
	}


}
