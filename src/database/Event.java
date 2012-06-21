package database;

public class Event {
	private long id;
	private String date;
	private String time;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	// Will be used by the ArrayAdapter in the ListView
	@Override
	public String toString() {
		return date;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}
}