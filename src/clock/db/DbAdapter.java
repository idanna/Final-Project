package clock.db;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import clock.sched.AlarmsManager.eCondition;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Abstracts the work with the database.
 * Handles all queries, inserts and removes operations. 
 * @author Idan
 *
 */
public class DbAdapter 
{
	private SQLiteDatabase database;
	private Connection connection;
	
	public DbAdapter(Context context) 
	{
		connection = new Connection(context);
	}
	
	/**
	 * Open the db connection.
	 * Must be called before any other function.
	 * @throws SQLException
	 */
	private void open() throws SQLException 
	{
		if (database == null || !database.isOpen())
		{
			database = connection.getWritableDatabase();
		}
	}

	/**
	 * Close the db connection.
	 * Must be called after work.
	 */
	private void close() 
	{
		if (database != null && database.isOpen())
		{
			database.close();
			connection.close();
		}
	}

	/**
	 * Inserts an event row to the Events table.
	 * @param event - the event to be inserted.
	 * @return the Event inserted.
	 */
	public long insertEvent(Event event) 
	{
		ContentValues values = new ContentValues();
		String dateSqlFormat = Event.getSqlTimeRepresent(event); 
		values.put(Connection.COLUMN_DATE, dateSqlFormat);
		values.put(Connection.COLUMN_LOCATION, event.getLocation());
		values.put(Connection.COLUMN_DETAILS, event.getDetails());
		values.put(Connection.COLUMN_ALARM, event.getWithAlarmStatus() == false? 0 : 1);
		values.put(Connection.COLUMN_NOTIFIED, event.isUserHasBeenNotified() == false? 0 : 1);
		values.put(Connection.COLUMN_WAKEDUP, event.getUserHasBeenWakedUp());
		this.open();
		long insertId = database.insert(Connection.TABLE_EVENTS, null, values);
		this.close();
		return insertId;
	}

	/**
	 * Deletes an event using the Id.
	 * @param event - the event to be deleted.
	 */
	public void deleteEvent(Event event) 
	{
		long id = event.getId();
		this.open();
		database.delete(Connection.TABLE_EVENTS, Connection.COLUMN_ID
				+ " = ?", new String[] { String.valueOf(event.getId()) });
		this.close();
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
		this.open();
		Cursor cursor = database.rawQuery("SELECT * FROM " + Connection.TABLE_EVENTS + " WHERE " +
				"date <= '" + yearMonth + "-31 23-59-59'" + " AND " +
				"date >= '" + yearMonth + "-00 00-00-00'", null); 
	
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
		this.close();
		return eventsMap;
	}
	
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
        this.open();
		Cursor cursor = database.rawQuery(query , null);
		Log.d("SQL", "NEXT EVENT SQL"  + query);
		
		cursor.moveToFirst();
		if (!cursor.isAfterLast())
		{
			cursor.moveToFirst();
			retEvent = cursorToEvent(cursor);
		}
		this.close();
		Log.d("DB", "Next event Id: " + (retEvent == null ? "NONE" : retEvent.getId()));
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
		event.setUserHasBeenWakedUp(cursor.getLong(6));
		return event;
	}

	private InvitedEvent cursorToInvitedEvent(Cursor cursor) 
	{
		InvitedEvent event = InvitedEvent.createNewInstance();
		event.setId(cursor.getLong(0));
		event.setOriginalId(cursor.getLong(1));
		event.setDateFromSql(cursor.getString(2));
		event.setLocation(cursor.getString(3));
		event.setDetails(cursor.getString(4));
		event.setChannel(cursor.getString(5));
		event.setSenderUserName(cursor.getString(6));
		return event;
	}
	
	/**
	 * Insert a record to the records table.
	 * @param event - event to be save - only day name saved for now
	 * @param arrangementTime - time took to arrange
	 * @param enumCondition - weather condition enum
	 * @param tempeture 
	 * @return the row ID of the newly inserted row, or -1 if an error occurred 
	 */
	public long insertRecord(Event event, int arrangementTime, eCondition enumCondition, int tempeture) 
	{
		ContentValues values = new ContentValues(); 
		values.put(Connection.COLUMN_ARR_TIME, arrangementTime);
		values.put(Connection.COLUMN_DAY_OF_WEEK, event.getDayName());
		values.put(Connection.COLUMN_WEATHER, enumCondition.toString());
		values.put(Connection.COLUMN_TEMPETURE, String.valueOf(tempeture));
		this.open();
		long insertId = database.insert(Connection.TABLE_RECORDS, null, values);
		this.close();
		return insertId;
	}
	
	/**
	 * 
	 * @param dayName
	 * @param contidion
	 * @param tempeture
	 * @return arrangement time or -1 if no suggestion was found.
	 */

	public int getArrangmentTime(String dayName, eCondition contidion, int tempeture) 
	{
		int arrangeTime = -1;
		String query = "SELECT (SUM(" + Connection.COLUMN_ARR_TIME + ")/COUNT(" + Connection.COLUMN_ARR_TIME + 
				")) FROM " + Connection.TABLE_RECORDS + 
				" WHERE " + Connection.COLUMN_WEATHER + "='" + contidion + 
				"' AND " + Connection.COLUMN_TEMPETURE + ">" + (tempeture - 5) + 
				" AND " + Connection.COLUMN_TEMPETURE + "<" + (tempeture + 5) + 
				" GROUP BY " + Connection.COLUMN_WEATHER + ", " + Connection.COLUMN_TEMPETURE;
		Log.d("ARRANGE", query);
		this.open();
		Cursor cursor = database.rawQuery(query, null);
	
		cursor.moveToFirst();
		if(!cursor.isAfterLast()) // did we get any results ? 
		{
			arrangeTime = cursor.getInt(0);			
		}
		else // no results, getting agg from all db.
		{
			query = "SELECT (SUM(" + Connection.COLUMN_ARR_TIME + ")/COUNT(" + Connection.COLUMN_ARR_TIME + 
					")) FROM " + Connection.TABLE_RECORDS;
			Log.d("ARRANGE", "Fall Back" + query);
			cursor = database.rawQuery(query, null);
			cursor.moveToFirst();
			arrangeTime = cursor.getInt(0);
		}
		this.close();
		return arrangeTime;
	}

	/**
	 * return 
	 * @param sqlTimeRepresent
	 * @return
	 */
	public Event getOneBefore(String sqlTimeRepresent)
	{
		return getOneBeforeOrAfter(sqlTimeRepresent, true);
	}

	public Event getOneAfter(String sqlTimeRepresent)
	{
		Event oneAfter = getOneBeforeOrAfter(sqlTimeRepresent, false);
		String oneAfterStr = oneAfter == null ? "NONE" : String.valueOf(oneAfter.getId());
		Log.d("EVENT", "one after is: " + oneAfterStr);
		return getOneBeforeOrAfter(sqlTimeRepresent, false);
	}
	
	/**
	 * Update all changed fields of the event in the DB.
	 * @param event - event to be updated in db.
	 */
	public void updateEvent(Event event)
	{
		ContentValues valuesMap = new ContentValues();
		valuesMap.put(Connection.COLUMN_DATE, Event.getSqlTimeRepresent(event));
		valuesMap.put(Connection.COLUMN_LOCATION, event.getLocation());
		valuesMap.put(Connection.COLUMN_DETAILS, event.getDetails());
		valuesMap.put(Connection.COLUMN_ALARM, event.getWithAlarmStatus() == true ? 1 : 0);
		valuesMap.put(Connection.COLUMN_NOTIFIED, event.getNotified() == true ? 1 : 0);
		valuesMap.put(Connection.COLUMN_WAKEDUP, event.getWakedUp());
		
		this.open();
		database.updateWithOnConflict(Connection.TABLE_EVENTS, valuesMap, Connection.COLUMN_ID + " = ?", new String[] { String.valueOf(event.getId()) }, SQLiteDatabase.CONFLICT_REPLACE);
		this.close();
	}
		
	private Event getOneBeforeOrAfter(String sqlTimeRepresent, boolean isOneBefore) 
	{
		String compareSign, order;
		if(isOneBefore)
		{
			compareSign = " < ";
			order = " DESC ";
		}
		else
		{
			compareSign = " > ";
			order = ""; // empty mean ascending.
		}
		String query = "SELECT * FROM " + Connection.TABLE_EVENTS + 
						" WHERE " + Connection.COLUMN_DATE + compareSign + "'" + sqlTimeRepresent + "'" +
						" ORDER BY " + Connection.COLUMN_DATE + " " + order + " LIMIT 1";
		Log.d("SQL", query);
		this.open();
		Cursor cursor = database.rawQuery(query, null);
		Event oneBefore = null;
		cursor.moveToFirst();
		if(!cursor.isAfterLast())
		{
			oneBefore = cursorToEvent(cursor);
		}
		this.close();
		return oneBefore;
	}
	
	public Event getEventById(int eventId) 
	{
		this.open();
		Cursor cursor = database.rawQuery("SELECT * FROM " + Connection.TABLE_EVENTS + " WHERE " + Connection.COLUMN_ID + " = " + eventId, null);
		cursor.moveToFirst();
		this.close();
		return cursorToEvent(cursor);
	}

	public void insertInvitedEvent(InvitedEvent invitedEvent) {
		ContentValues values = new ContentValues(); 
		values.put(Connection.COLUMN_DATE, Event.getSqlTimeRepresent(invitedEvent));
		values.put(Connection.COLUMN_LOCATION, invitedEvent.getLocation());
		values.put(Connection.COLUMN_DETAILS, invitedEvent.getDetails());
		values.put(Connection.COLUMN_INVITER_CHANNEL, invitedEvent.getChannel());
		values.put(Connection.COLUMN_ORIGINAL_ID, invitedEvent.getId());
		values.put(Connection.COLUMN_SENDER_USER_NAME, invitedEvent.getSenderUserName());
		this.open();
		database.insert(Connection.TABLE_INVITED, null, values);
		this.close();
	}
	
	/**
	 * returns the waiting invitation from the table, if none return null
	 * @return
	 */
	public InvitedEvent[] getWaitingInvitation() {
		InvitedEvent[] retList = null;
		this.open();
		Cursor cursor = database.rawQuery("SELECT * FROM " + Connection.TABLE_INVITED, null);
		Cursor inviNumCur = database.rawQuery("SELECT COUNT(*) FROM " + Connection.TABLE_INVITED, null);
		inviNumCur.moveToFirst();
		int inviNum = inviNumCur.getInt(0);
		if(inviNum > 0)
		{
			retList = new InvitedEvent[inviNum];
			cursor.moveToFirst();
			int i = 0;
			while (!cursor.isAfterLast())
			{
				InvitedEvent invitedEvent = cursorToInvitedEvent(cursor);
				retList[i] = invitedEvent;
				cursor.moveToNext();
				i++;
			}
			
		}
		this.close();
		return retList;
	}

	public void deleteInvitedEvent(long confirmedEventId) { 
		this.open();
		database.delete(Connection.TABLE_INVITED, Connection.COLUMN_ID + "=" + confirmedEventId, null);			  	
		 Log.d("EVENT", "Invited Event number " + confirmedEventId + " has been deleted from db");
		this.close();
	 }
	
	/**
	 * Inserts initial data required for the App to start. (asked at first time)
	 * @param arrTime - user manual arrangment time in minutes
	 * @param userChannel - channel hash code - for sending notifycation. 
	 */
	public void setInitData(String userName, int arrTime, String userChannel) 
	{
		this.open();
		ContentValues values = new ContentValues(); 
		values.put(Connection.COLUMN_CHANNEL_HASH, userChannel);
		values.put(Connection.COLUMN_USER_NAME, userName);
		database.insert(Connection.TABLE_DATA, null, values);
		values = new ContentValues(); 
		values.put(Connection.COLUMN_ARR_TIME, TimeUnit.MINUTES.toMillis(arrTime));
		values.put(Connection.COLUMN_DAY_OF_WEEK, "INIT");
		values.put(Connection.COLUMN_WEATHER, "INIT");
		values.put(Connection.COLUMN_TEMPETURE, -1);
		database.insert(Connection.TABLE_RECORDS, null, values);		
		this.close();
	}

	
	public boolean hasInitialData() 
	{
		boolean retVal;
		this.open();
		Cursor c = database.query(Connection.TABLE_DATA, null, null, null, null, null, null);
		c.moveToFirst();
		retVal = c.isAfterLast();
		this.close();
		return !retVal;
	}

	public String getUserName() 
	{
		String retVal;
		this.open();
		Cursor c = database.query(Connection.TABLE_DATA, null, null, null, null, null, null);
		c.moveToFirst();
		retVal = c.getString(1);
		this.close();
		return retVal;
	}

	
	public String getUserChannel() {
		String retVal;
		this.open();
		Cursor c = database.query(Connection.TABLE_DATA, null, null, null, null, null, null);
		c.moveToFirst();
		retVal = c.getString(0);
		this.close();
		return retVal;
	}

	public void addConfirmerNameToEvent(int confirmEventId, String confirmerName) {
		this.open();
		ContentValues values = new ContentValues(); 
		values.put(Connection.COLUMN_ID, confirmEventId);
		values.put(Connection.COLUMN_USER_NAME, confirmerName);
		database.insert(Connection.TABLE_CONFIRMED_EVENTS, null, values);		
		this.close();
	}

	public String[] getEventConfirmersList(long id) 
	{
		String[] retVal = null;
		String query = "SELECT " + Connection.COLUMN_USER_NAME + " FROM " + Connection.TABLE_CONFIRMED_EVENTS
						+ " WHERE " + Connection.COLUMN_ID + " = " + String.valueOf(id);
		this.open();
		Cursor c = database.rawQuery(query, null);
		c.moveToFirst();
		int rows = c.getCount();
		if(rows > 0) {
			retVal = new String[rows];
			int i = 0;
			while (!c.isAfterLast()) {
				retVal[i] = c.getString(0);
				c.moveToNext();
				i++;
			}
		}
		
		this.close();
		return retVal;
	}	
}
