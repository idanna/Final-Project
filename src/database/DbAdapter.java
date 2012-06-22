package database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

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
		connection = new Connection(context);
	}

	public void open() throws SQLException 
	{
		database = connection.getWritableDatabase();
	}

	public void close() 
	{
		connection.close();
	}

	public Event createEvent(Event event) 
	{
		//Maybe we need to keep table sorted by date time, might be a good optimization
		
		ContentValues values = new ContentValues();
		
		values.put(Connection.COLUMN_DAY, event.getDay());
		values.put(Connection.COLUMN_MONTH, event.getMonth());
		values.put(Connection.COLUMN_YEAR, event.getYear());
		values.put(Connection.COLUMN_HOUR, event.getHour());
		values.put(Connection.COLUMN_MIN, event.getMin());
		values.put(Connection.COLUMN_LOCATION, event.getLocation());
		values.put(Connection.COLUMN_DETAILS, event.getDetails());
		
		long insertId = database.insert(Connection.TABLE_EVENTS, null,
				values);
		Cursor cursor = database.query(Connection.TABLE_EVENTS,
				allColumns, Connection.COLUMN_ID + " = " + insertId, null,
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

	public List<Event> getAllComments() 
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

	private Event cursorToEvent(Cursor cursor) 
	{
		Event event = new Event();
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
}