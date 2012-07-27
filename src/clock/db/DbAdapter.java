package clock.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DbAdapter 
{
	// Database fields
	private SQLiteDatabase database;
	private Connection connection;
	private String[] allColumns = { Connection.COLUMN_ID,
									Connection.COLUMN_DAY, 
									Connection.COLUMN_MONTH,
									Connection.COLUMN_YEAR,
									Connection.COLUMN_HOUR,
									Connection.COLUMN_MIN,
									Connection.COLUMN_LOCATION,
									Connection.COLUMN_DETAILS };
	
	public DbAdapter(Context context) 
	{
		try {
			connection = new Connection(context);
		} 
		catch (IOException e){
			Log.d("DBAdapter", "Error in constractor");
		}
	}

	public void open() throws SQLException 
	{
		database = connection.openDataBase();
	}

	public void close() 
	{
		database.close();
		connection.close();
	}
	
	public boolean isAddressTableEmpty()
	{
		boolean isEmpty = true;
		
		this.open();
		
		//TODO: what is the best way to do this?
		
		this.close();
		
		return isEmpty;
	}

	public Event createEvent(Event event) 
	{
		ContentValues values = new ContentValues();		
		values.put(Connection.COLUMN_DAY, event.getDay());
		values.put(Connection.COLUMN_MONTH, event.getMonth());
		values.put(Connection.COLUMN_YEAR, event.getYear());
		values.put(Connection.COLUMN_HOUR, event.getHour());
		values.put(Connection.COLUMN_MIN, event.getMin());
		values.put(Connection.COLUMN_LOCATION, event.getLocation());
		values.put(Connection.COLUMN_DETAILS, event.getDetails());		
		long insertId = database.insert(Connection.TABLE_EVENTS, null, values);
		Cursor cursor = database.query(Connection.TABLE_EVENTS, allColumns, Connection.COLUMN_ID + " = " + insertId, null,
										null, null, null);
		
		cursor.moveToFirst();
		Event newEvent = cursorToEvent(cursor);
		cursor.close();
		return newEvent;
	}

	public void deleteEvent(Event event) 
	{
		long id = event.getId();
		System.out.println("Comment deleted with id: " + id);
		database.delete(Connection.TABLE_EVENTS, Connection.COLUMN_ID
				+ " = " + id, null);
	}
	
	public HashMap<Integer, List<Event>> getEventsMapForMonth(int month)
	{
		//TODO: quickly replace 31 to the const MAX_DAYS_IN_MONTH
		HashMap<Integer, List<Event>> eventsMap = new HashMap<Integer, List<Event>>(31);
		//TODO: optimize this by sorting by the day.
		Cursor cursor = database.rawQuery("SELECT * FROM " + Connection.TABLE_EVENTS + " WHERE month='" + month +"'", null); 
				//database.query(Connection.TABLE_EVENTS, allColumns, Connection.COLUMN_MONTH + "=" + month, 
				//null, null, null, null);
	
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) 
		{
			Event event = cursorToEvent(cursor);
			List<Event> dayList = eventsMap.get(event.getDay());
			if (dayList == null) // first event for day
			{
				dayList = new ArrayList<Event>();
				eventsMap.put(new Integer(event.getDay()), dayList);
			}
			dayList.add(event);
			cursor.moveToNext();
		}
		
		return eventsMap;
	}
	
	public List<Event> getAllEvents() 
	{
		List<Event> events = new ArrayList<Event>();

		Cursor cursor = database.query(Connection.TABLE_EVENTS,
				allColumns, null, null, null, null, null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Event event = cursorToEvent(cursor);
			events.add(event);
			cursor.moveToNext();
		}

		
		// Make sure to close the cursor
		cursor.close();
		return events;
	}
		
	/**
	 * get the next event from the database.
	 * returns null if no upcoming events.
	 */
	public Event getNextEvent()
	{
		Event retEvent = null;
		Cursor cursor = database.rawQuery("select * from events order by year desc, month desc, day desc, hour desc, min desc limit 1", null);
		cursor.moveToFirst();
		if (!cursor.isAfterLast())
		{
			Calendar latestEvent = cursorToCalander(cursor);
			Calendar now = Calendar.getInstance();
			
			cursor.moveToFirst();
			
			//if (latestEvent.compareTo(now) >= 0) // latest event is not relevant.
			{
				retEvent = cursorToEvent(cursor);
			}
		}
		
		return retEvent;
	}
	
	private Calendar cursorToCalander(Cursor cursor)
	{
		Calendar c = Calendar.getInstance();
		c.set(cursor.getInt(3), cursor.getInt(2), cursor.getInt(1), cursor.getInt(4), cursor.getInt(5));
		return c;		
	}
	
	private Event cursorToEvent(Cursor cursor) 
	{
		Event event = Event.createNewInstance();
		event.setId(cursor.getLong(0));
		event.setDay(cursor.getInt(1));
		event.setMonth(cursor.getInt(2));
		event.setYear(cursor.getInt(3));
		event.setHour(cursor.getInt(4));
		event.setMin(cursor.getInt(5));
		event.setLocation(cursor.getString(6));
		event.setDetails(cursor.getString(7));
		return event;
	}

	public String[] getStreetSugg(String constrain) 
	{
		Cursor cursor = database.rawQuery("SELECT DISTINCT " + Connection.COLUMN_STREET + 
				" FROM " + Connection.TABLE_ADDRESS + " where " + Connection.COLUMN_STREET + " LIKE '" + constrain + "%' limit 3", null); 
		cursor.moveToFirst();
		String[] sugg = new String[cursor.getCount()];
		int i = 0;
		while (!cursor.isAfterLast()) 
		{
			sugg[i] = cursor.getString(0);
			cursor.moveToNext();
			i++;
		}
		
		return sugg;
		
	}
}