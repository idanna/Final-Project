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
			Connection.COLUMN_DATE };

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

	public Event createComment(String comment) 
	{
		ContentValues values = new ContentValues();
		values.put(Connection.COLUMN_DATE, comment);
		long insertId = database.insert(Connection.TABLE_EVENTS, null,
				values);
		Cursor cursor = database.query(Connection.TABLE_EVENTS,
				allColumns, Connection.COLUMN_ID + " = " + insertId, null,
				null, null, null);
		cursor.moveToFirst();
		Event newComment = cursorToEvent(cursor);
		cursor.close();
		return newComment;
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
		event.setDate(cursor.getString(1));
		return event;
	}
}