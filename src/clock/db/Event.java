package clock.db;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import clock.sched.AlarmsManager;

import android.R.bool;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

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
	/**
	 * sets events properties from UI elements.
	 */
	public void setPropFromViews(DatePicker date, TimePicker time, EditText location, EditText details) 
	{
	   this.day = date.getDayOfMonth();
	   this.month = date.getMonth() + 1;
	   this.year = date.getYear();
	   this.hour = time.getCurrentHour();
	   this.min = time.getCurrentMinute();
	   this.location = location.getText().toString();
	   this.details = details.getText().toString();
	}
	
	/**
	 * return true if and only if the first event is earlier (or at the same time as) then the second.
	 */
	public static boolean isEarlier(Event newEvent, Event nextEvent) 
	{
		Calendar first = Calendar.getInstance();
		Calendar second = Calendar.getInstance();
		first.set(newEvent.year, newEvent.month, newEvent.day, newEvent.hour, newEvent.min);
		second.set(nextEvent.year, nextEvent.month, nextEvent.day, nextEvent.hour, nextEvent.min);
		return first.compareTo(second) <= 0 ? true : false;
	}
	
	public Calendar toCalendar() {
		Calendar res = Calendar.getInstance();
		res.set(year, month, day, hour, min);
		return res;
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

	public static String getSqlRepresent(Event event) 
	{
		String retStr = event.getYear() + "-";
		retStr = padZeroIfNedded(event.getMonth(), retStr);
		retStr += event.getMonth() + "-";
		retStr = padZeroIfNedded(event.getDay(), retStr);
		retStr += event.getDay() + " ";
		retStr = padZeroIfNedded(event.getHour(), retStr);
		retStr += event.getHour() + "-";
		retStr = padZeroIfNedded(event.getMin(), retStr);
		retStr += event.getMin() + "-00";		
		return retStr;
	}
	
	private static String padZeroIfNedded(int num, String str)
	{
		if (num < 10)
		{
			str += "0";
		}
		
		return str;
	}

	public void setDateFromSql(String sqlDateTime) 
	{
		String[] dateTime = sqlDateTime.split(" ");
		String[] date = dateTime[0].split("-");
		String[] time = dateTime[1].split("-");
		this.year = Integer.parseInt(date[0]);
		this.month = Integer.parseInt(date[1]);
		this.day = Integer.parseInt(date[2]);
		this.hour = Integer.parseInt(time[0]);
		this.min = Integer.parseInt(time[1]);
	}

	public static boolean earlyThenNow(Event event) 
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd' 'HH-mm-ss");
        String currentTimeStr = sdf.format(new Date());
        Event currentTime = new Event();
        currentTime.setDateFromSql(currentTimeStr);
        return isEarlier(event, currentTime);
	}

}