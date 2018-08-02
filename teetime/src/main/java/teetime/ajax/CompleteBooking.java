package teetime.ajax;

import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;

import teetime.Golfer;
import teetime.Golfers;
import webscraper.net.WebSession;

//
// this is the data structure we need to build for a reservation
//
//{
//"methodName": "ttBooking.ProceedBooking",
//"data": {
//    "Mode": "booking",
//    "Bookings": [{
//        "BookingId": 0,
//        "OwnerId": 593198,
//        "Reservations": [{
//            "ReservationId": 0,
//            "ReservationType": 0,
//            "FullName": "Donald Boulia",
//            "Transport": "2",
//            "Caddy": "False",
//            "Rentals": "",
//            "MemberId": 593198
//        }, {
//            "ReservationId": 0,
//            "ReservationType": -1,
//            "FullName": "",
//            "Transport": "2",
//            "Caddy": "False",
//            "Rentals": ""
//        }, {
//            "ReservationId": 0,
//            "ReservationType": -1,
//            "FullName": "",
//            "Transport": "2",
//            "Caddy": "False",
//            "Rentals": ""
//        }, {
//            "ReservationId": 0,
//            "ReservationType": -1,
//            "FullName": "",
//            "Transport": "2",
//            "Caddy": "False",
//            "Rentals": ""
//        }],
//        "Holes": 18,
//        "allowed": null,
//        "enabled": true,
//        "Notes": ""
//    }]
//}
//}

public class CompleteBooking extends AjaxAction {
	
	public CompleteBooking(WebSession session, String path) {
		super(session, path);
	}
	

	/**
	 * create a reservation object for this golfer
	 * 
	 * @param golfer the golfer in this reservation object
	 * @return the reservation object
	 */
	private JSONObject reservation( Golfer golfer ) {
		JSONObject json = new JSONObject();
		
		json.put("ReservationId", 0);
		json.put("ReservationType", 0);
		json.put("FullName", golfer.getName());
		json.put("Transport", "2");
		json.put("Caddy", "False");
		json.put("Rentals", "");
		json.put("MemberId", Integer.parseInt(golfer.getId()));
		
		return json;
	}

	/**
	 * if a booked tee time doesn't have four golfers, we create an
	 * empty record for the remaining slots
	 * 
	 * @return an empty reservation
	 */
	private JSONObject emptyReservation( ) {
		JSONObject json = new JSONObject();
		
		json.put("ReservationId", 0);
		json.put("ReservationType", -1);
		json.put("FullName", "");
		json.put("Transport", "2");
		json.put("Caddy", "False");
		json.put("Rentals", "");
		
		return json;
	}
	
	/**
	 * build an array of golfers who are part of this tee time
	 * 
	 * @param golfers - the list of golfers to add to the tee time
	 * @return the JSON array of golfers in this reservation
	 */
	private JSONArray reservations(Golfers golfers) {
		JSONArray array = new JSONArray();

		int i = 0;
		Iterator<Golfer> it = golfers.iterator();
		
		while (it.hasNext()) {
			Golfer golfer = it.next();
			JSONObject json = reservation(golfer);
			
			array.put(json);

			i++;
		}
		
		// if less than a foursome was given, fill in the rest as empty slots
		while (i<4) {
			JSONObject json = emptyReservation();
			
			array.put(json);

			i++;
		}

		return array;
	}
	
	/**
	 * the top level data for this tee time.  Specifies some overall reservation data
	 * and contains a reservation object listing the golfers on this tee time
	 * 
	 * @param golfers the golfers on this tee time
	 * @return an array with one object representing the booking information
	 */
	private JSONArray bookings( Golfers golfers ) {
		JSONArray array = new JSONArray();
		JSONObject json = new JSONObject();

		Iterator<Golfer> it = golfers.iterator();
		
		Golfer owner = it.next();

		json.put("BookingId", 0);
		json.put("OwnerId", Integer.parseInt(owner.getId()));
		json.put("Reservations", reservations(golfers));
		json.put("Holes", 18);
		json.put("allowed", JSONObject.NULL);
		json.put("enabled", true);
		json.put("Notes", "");
		
		array.put(json);
		return array;
	}
	
	/**
	 * data object holds the booking information
	 * 
	 * @param golfers the golfers on this tee time reservation
	 * @return an object representing the data portion of this request
	 */
	private JSONObject data( Golfers golfers ) {
		JSONObject json = new JSONObject();
		
		json.put("Mode", "booking");
		json.put("Bookings", bookings(golfers));
		
		return json;
	}
	
	private String buildInput( Golfers golfers ) {
		JSONObject postData = new JSONObject();
		postData.put("methodName", "ttBooking.ProceedBooking");
		postData.put("data", data(golfers));

		String json = postData.toString();
		
		return json;
	}

	/**
	 * do some checks to make sure the list of golfers we have is valid
	 * 
	 * @param golfers
	 * @return true if valid, false otherwise
	 */
	boolean validFoursome(Golfers golfers) {
		Iterator<Golfer> it = golfers.iterator();
		
		if (!it.hasNext()) {
			System.err.println("No golfers in foursome!");
			return false;
		}

		if (golfers.size()>4) {
			System.err.println("Too many golfers in foursome!");
			return false;
		}
		
		return true;
	}
	
	public boolean send(Golfers golfers) {
		if (!validFoursome(golfers)) {
			return false;
		}
		
		String json = buildInput(golfers);
		
		// see if this time is available by requesting the lock
		String result = doPost(json);
		
		if (result == null) {
			System.err.println("Error completing booking.");
			return false;
		}

		// System.out.println( result );
		if (!goodResult(result)) {
			System.err.println("Did NOT complete booking! Result ->" + result);
			return false;
		}

		return true;
	}

	/**
	 * expecting to find this JSON
	 * {\"method\":\"Confirmation\",\"methodparameters\":{\"title\":\"Booking... 
	 * 
	 * @param result server response
	 * @return true if we find the right result, false otherwise
	 */
	private boolean goodResult(String result) {
		JSONObject jsonResult = parseAjaxResult(result);
		
		if (jsonResult!=null) {
			if (jsonResult.getString("method").compareTo("Confirmation")==0) {
				return true;
			}
		}
		
		return false;
	}	

}
