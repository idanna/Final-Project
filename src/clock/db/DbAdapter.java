package clock.db;

import java.io.IOException;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

/**
 * Abstracts the work with the database.
 * Handles all queries, inserts and removes operations. 
 * @author Idan
 *
 */
public class DbAdapter 
{
	// Database fields
	private SQLiteDatabase database;
	private Connection connection;
	private String[] allColumns = { Connection.COLUMN_ID,
									Connection.COLUMN_DATE,
									Connection.COLUMN_LOCATION,
									Connection.COLUMN_DETAILS,
									Connection.COLUMN_ALARM,
									Connection.COLUMN_NOTIFIED,
									Connection.COLUMN_WAKEDUP};
	
	public DbAdapter(Context context) 
	{
//		try {
		connection = new Connection(context);
//		} 
//		catch (IOException e){
//			Log.e("DBAdapter", "Error in constructor while trying to create new Connection");
//		}
	}
	
	/**
	 * Open the db connection.
	 * Must be called before any other function.
	 * @throws SQLException
	 */
	public void open() throws SQLException 
	{
//		database = connection.openDataBase();
		database = connection.getWritableDatabase();
	}

	/**
	 * Close the db connection.
	 * Must be called after work.
	 */
	public void close() 
	{
		database.close();
		connection.close();
	}

	/**
	 * Inserts an event row to the Events table.
	 * @param event - the event to be inserted.
	 * @return the Event inserted.
	 */
	public Event insertEvent(Event event) 
	{
		ContentValues values = new ContentValues();
		String dateSqlFormat = Event.getSqlTimeRepresent(event); 
		values.put(Connection.COLUMN_DATE, dateSqlFormat);
		values.put(Connection.COLUMN_LOCATION, event.getLocation());
		values.put(Connection.COLUMN_DETAILS, event.getDetails());
		values.put(Connection.COLUMN_ALARM, event.getWithAlarmStatus() == false? 0 : 1);
		values.put(Connection.COLUMN_NOTIFIED, event.isUserHasBeenNotified() == false? 0 : 1);
		values.put(Connection.COLUMN_WAKEDUP, event.isUserHasBeenWakedUp() == false? 0 : 1);
		long insertId = database.insert(Connection.TABLE_EVENTS, null, values);
		Cursor cursor = database.query(Connection.TABLE_EVENTS, allColumns, Connection.COLUMN_ID + " = " + insertId, null,
										null, null, null);
		
		cursor.moveToFirst();
		Event newEvent = cursorToEvent(cursor);
		cursor.close();
		return newEvent;
	}

	/**
	 * Deletes an event using the Id.
	 * @param event - the event to be deleted.
	 */
	public void deleteEvent(Event event) 
	{
		long id = event.getId();
		database.delete(Connection.TABLE_EVENTS, Connection.COLUMN_ID
				+ " = " + id, null);
		Log.d("EVENT", "Event number " + id + ", Name: " + event.toString() + " has been deleted from db");
	}
	
	/**
	 * Returns a map of 'Day' => 'Events' associated with the month, year events.
	 * @param month - requested month
	 * @param year - requested year.
	 * @return
	 */
	public HashMap<String, List<Event>> getEventsMapForMonth(int month, int year)
	{
		//TODO: quickly replace 31 to the const MAX_DAYS_IN_MONTH
		HashMap<String, List<Event>> eventsMap = new HashMap<String, List<Event>>(31);
		String yearMonth = String.valueOf(year) + "-";
		yearMonth += month < 10 ? "0" + month : month;
		Cursor cursor = database.rawQuery("SELECT * FROM " + Connection.TABLE_EVENTS + " WHERE " +
				"date <= '" + yearMonth + "-31 23-59-59'" + " AND " +
				"date >= '" + yearMonth + "-00 00-00-00'", null); 
				//database.query(Connection.TABLE_EVENTS, allColumns, Connection.COLUMN_MONTH + "=" + month, 
				//null, null, null, null);
	
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) 
		{
			Event event = cursorToEvent(cursor);
			List<Event> dayList = eventsMap.get(String.valueOf(event.getDay()));
			if (dayList == null) // first event for day
			{
				dayList = new ArrayList<Event>();
				eventsMap.put(String.valueOf(event.getDay()), dayList);
			}
			dayList.add(event);
			cursor.moveToNext();
		}
		
		return eventsMap;
	}
	
	/**
	 * Returns all the events in the database.
	 * @return
	 */
//	public List<Event> getAllEvents() 
//	{
//		List<Event> events = new ArrayList<Event>();
//
//		Cursor cursor = database.query(Connection.TABLE_EVENTS,
//				allColumns, null, null, null, null, null);
//		cursor.moveToFirst();
//		while (!cursor.isAfterLast()) {
//			Event event = cursorToEvent(cursor);
//			events.add(event);
//			cursor.moveToNext();
//		}
//
//		
//		// Make sure to close the cursor
//		cursor.close();
//		return events;
//	}
		
	/**
	 * get the next event from the database (where alarm field != PAST_EVENT).
	 * returns null if no upcoming events.
	 */
	public Event getNextEvent()
	{
		Event retEvent = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd' 'HH-mm-ss");
        String currentTime = sdf.format(new Date());
        String query = "SELECT * FROM " + Connection.TABLE_EVENTS + " WHERE " + 
        		"date > '" + currentTime + "' order by date limit 1";
		Cursor cursor = database.rawQuery(query , null);
//		Log.d("Next SQL", query);
		
		cursor.moveToFirst();
		if (!cursor.isAfterLast())
		{
			cursor.moveToFirst();
			retEvent = cursorToEvent(cursor);
		}
//		if (retEvent != null)
//		{
//			Log.d("NEXT EVENT", retEvent.toString());
//		}
//		else
//		{
//			Log.d("NEXT EVENT", " NO EVENT");
//		}
		
		return retEvent;
	}	
	
	/**
	 * returns a new event instance.
	 * @param cursor - cursor to the row with the event.
	 * @return
	 */
	private Event cursorToEvent(Cursor cursor) 
	{
		Event event = Event.createNewInstance();
		event.setId(cursor.getLong(0));
		event.setDateFromSql(cursor.getString(1));
		event.setLocation(cursor.getString(2));
		event.setDetails(cursor.getString(3));
		event.setWithAlarmStatus(cursor.getInt(4) == 1);
		event.setUserHasBeenNotified(cursor.getInt(5) == 1);
		event.setUserHasBeenWakedUp(cursor.getInt(6) == 1);
		return event;
	}
	
}