package database;

public class Event
{
	private long id;
	//TODO: should be all changed to string
	private int day;
	private int month;
	private int year;
	private int hour;
	private int min;
	private String location;
	private String details;
	
	private Event()
	{ };
	
	/**
	 * returning the event in format dd-MM-YY|HH:mm|location|details
	 */
	@Override
	public String toString() 
	{
		return day + "-" + month + "-" + year + "|" + hour + ":" + min + "|" + location + "|" + details;
	}

	public static Event createNewInstance()
	{
		Event event = new Event();
		event.setLocation("");
   		event.setDetails("");
   		return event;
	}
	
	/**
	 * 
	 * @param eventStr - Should be in the format dd-MM-YY|HH:mm|location|details
	 * @return new event parsed from string
	 */
	public static Event CreateFromString(String eventStr) 
	{
		Event e = new Event();
		String[] prop = eventStr.split("\\|");
		String[] date = prop[0].split("-");
		String[] time = prop[1].split("\\:");
		e.day = Integer.parseInt(date[0]);
		e.month = Integer.parseInt(date[1]);
		e.year = Integer.parseInt(date[2]);
		e.hour = Integer.parseInt(time[0]);
		e.min = Integer.parseInt(time[1]);
		e.location = prop[2];
		e.details = prop[3];
		
		return e;
	}
	
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	public int getDay() {
		return day;
	}

	public void setDay(int day) {
		this.day = day;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public int getMin() {
		return min;
	}

	public void setMin(int min) {
		this.min = min;
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public int getHour() {
		return hour;
	}

	public void setHour(int hour) {
		this.hour = hour;
	}
}