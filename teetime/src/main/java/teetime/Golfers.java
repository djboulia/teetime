package teetime;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * A collection of golfers
 * 
 * @author djboulia
 *
 */
public class Golfers {
	
	private ArrayList<Golfer> golfers;
	
	
	public Golfers() {
		golfers = new ArrayList<Golfer>();
	}
	
	public Iterator<Golfer> iterator() {
		return golfers.iterator();
	}
	
	public boolean add( Golfer golfer ) {
		return golfers.add(golfer);
	}
	
	public int size() {
		return golfers.size();
	}
	
	public String toString() {
		String result = "";
		
		Iterator<Golfer> it = iterator();
		
		while (it.hasNext()) {
			Golfer golfer = it.next();
			result += golfer.toString() + "\n";
		}
		
		return result;
	}

}
