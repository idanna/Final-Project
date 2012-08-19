package clock.db;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

public class Event
{
	public enum eComparison{
		BEFORE,
		OVERLAPPED,
		AFTER
	}
	
	private long id;
	//TODO: should be all changed to string
	private int day;
	private int month;
	private int year;
	private int hour;
	private int min;
	private String location;
	private String details;
	private boolean withAlarm;
	private boolean userHasBeenNotified;
	private boolean userHasBeenWakedUp;
	
	

	private Event()
	{ };
	
	@Override
	public String toString() 
	{
		return getpadedZeroStr(this.hour) + ":" + getpadedZeroStr(this.min) + " " + this.location + "- " + this.details;
	}
	
	@Override
	public boolean equals(Object o) 
	{
		Event other = (Event) o;
		return other.id == this.id; 
	}
	public static Event createNewInstance()
	{
		Event event = new Event();
		event.setLocation("");
   		event.setDetails("");
   		event.withAlarm = false;
   		event.userHasBeenNotified = false;
   		event.userHasBeenWakedUp = false;
   		return event;
	}
	
	/**
	 * 
	 * @param eventStr - Should be in the format dd-MM-YY|HH:mm|location|details|id|with_alarm|userHasBeenNotified|userHasBeenWakedUp
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
		e.id = Long.parseLong(prop[4]);
		e.withAlarm = Boolean.parseBoolean(prop[5]);
		e.userHasBeenNotified = Boolean.parseBoolean(prop[6]);
		e.userHasBeenWakedUp = Boolean.parseBoolean(prop[7]);
		
		return e;
	}
	/**
	 * sets events properties from UI elements.
	 */
	public void setPropFromViews(DatePicker date, TimePicker time, EditText location, EditText details, boolean withAlarm) 
	{
	   this.day = date.getDayOfMonth();
	   this.month = date.getMonth() + 1;
	   this.year = date.getYear();
	   this.hour = time.getCurrentHour();
	   this.min = time.getCurrentMinute();
	   this.location = location.getText().toString();
	   this.details = details.getText().toString();
	   this.withAlarm = withAlarm;
	   
	   //If properties has been reset then event progress fields should be reset as well
	   this.userHasBeenNotified = false;
	   this.userHasBeenWakedUp = false;
	}
	
	/**
	 * return comparison value between first and second events (only by event start time)
	 */
	public static eComparison compareBetweenEvents(Event firstEvent, Event secondEvent) 
	{
		eComparison result;
		Calendar firstCalendar = Calendar.getInstance();
		Calendar secondCalendar = Calendar.getInstance();
		firstCalendar.set(firstEvent.year, firstEvent.month, firstEvent.day, firstEvent.hour, firstEvent.min);
		secondCalendar.set(secondEvent.year, secondEvent.month, secondEvent.day, secondEvent.hour, secondEvent.min);
		int comparedValue = firstCalendar.compareTo(secondCalendar);
		
		if (comparedValue < 0) 
			result = eComparison.BEFORE;
		else if (comparedValue == 0)
			result = eComparison.OVERLAPPED;
		else
			result = eComparison.AFTER;
		
		return result;
	}
	
	public Calendar toCalendar() {
		Calendar res = Calendar.getInstance();
		res.set(year, month, day, hour, min);
		return res;
	}
	
	/**
	 * 
	 * @return The number of milli seconds left to the event.
	 */
	public long getTimesLeftToEvent()
	{
		Calendar currentCalendar = Calendar.getInstance();
		Calendar eventCalendar = this.toCalendar();
				
		return eventCalendar.getTimeInMillis() - currentCalendar.getTimeInMillis();
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
	
	public boolean getWithAlarmStatus()
	{
		return withAlarm;
	}
	
	public void setWithAlarmStatus(boolean status)
	{
		this.withAlarm = status;
	}
	
	public boolean isUserHasBeenNotified() {
		return userHasBeenNotified;
	}

	public void setUserHasBeenNotified(boolean userHasBeenNotified) {
		this.userHasBeenNotified = userHasBeenNotified;
	}

	public boolean isUserHasBeenWakedUp() {
		return userHasBeenWakedUp;
	}

	public void setUserHasBeenWakedUp(boolean userHasBeenWakedUp) {
		this.userHasBeenWakedUp = userHasBeenWakedUp;
	}
	
	/**
	 * returning the event in format dd-MM-YY|HH:mm|location|details|id|with_alarm|userHasBeenNotified|userHasBeenWakedUp
	 */
	public String encodeToString()
	{
		return day + "-" + month + "-" + year + 
				"|" + hour + ":" + min + 
				"|" + location + 
				"|" + details + 
				"|" + id + 
				"|" +  withAlarm + 
				"|" +  userHasBeenNotified +
				"|" +  userHasBeenWakedUp;
	}
	
	/**
	 * 
	 * @param event
	 * @return String in the format of "YYYY-MM-DD HH-mm-00"
	 */
	public static String getSqlTimeRepresent(Event event) 
	{
		String retStr = event.getYear() + "-";
		retStr += getpadedZeroStr(event.getMonth()) + "-";
		retStr += getpadedZeroStr(event.getDay()) + " ";
		retStr += getpadedZeroStr(event.getHour()) + "-";
		retStr += getpadedZeroStr(event.getMin()) + "-00";
		return retStr;
	}
	
	/*
	 * Return string of of two digits representing the num given
	 * Exmp: input: 1 => "01" input 24 => "24"
	 */
	private static String getpadedZeroStr(int num)
	{
		String retStr = num < 10 ? "0" + String.valueOf(num) : String.valueOf(num);
		return retStr;
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

	private eComparison compareToNow(Event event) 
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd' 'HH-mm-ss");
        String currentTimeStr = sdf.format(new Date());
        Event currentTime = new Event();
        currentTime.setDateFromSql(currentTimeStr);
        return compareBetweenEvents(event, currentTime);
	}

	public boolean isAfterNow() 
	{
		return compareToNow(this) == eComparison.AFTER;
	}

}