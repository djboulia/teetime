package teetime;

public class Golfer {
	private String name;		// golfer name
	private String id;		// golfer id in the tee time system

	public Golfer( String name, String id ) {
		this.setName(name);;
		this.setId(id);
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	public String toString() {
		return "Golfer: " + name + ", id: " + id;
	}
}
