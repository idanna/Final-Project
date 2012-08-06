package clock.sched;

import java.security.acl.LastOwnerException;

import android.content.Context;

import clock.db.DbAdapter;
import clock.db.Event;
import clock.db.Event.eComparison;

/**
 * Manager All alarms service infornt the DataBase 
 * And the Android alarm services.
 * Manage all the internal alarm schedualing logic.
 * @author Idan
 *
 */
public class AlarmsManager 
{
	private DbAdapter dbAdapter;
	private Context context;
	private Event latestEvent;
//	private LocationHandler locationHandler;
	
	public AlarmsManager(Context context, DbAdapter dbAdapter) 
	{
		super();
		this.dbAdapter = dbAdapter;
		this.context = context;
		//this.locationHandler = new LocationHandler(context);
	}

	public void newEvent(Event newEvent)
	{
		// saving the new event to db, and ref
		dbAdapter.open();
		dbAdapter.insertEvent(newEvent);
		refreshLastEvent();
		dbAdapter.close();
		if(Event.compareToNow(newEvent) == eComparison.AFTER)
		{
			if (latestEvent == null || Event.compareBetweenEvents(newEvent, latestEvent) == eComparison.BEFORE)
			{
				ClockHandler.cancelEventAlarm(context, latestEvent);
				this.latestEvent = newEvent;
				ClockHandler.setAlarm(context, latestEvent);
//				locationHandler.setLocationListener(this.latestEvent);
			}
			
		}
		
	}

	public void deleteEvent(Event event)
	{
		dbAdapter.open();
		refreshLastEvent();
		dbAdapter.deleteEvent(event);
		dbAdapter.close();
		if (latestEvent != null && latestEvent.equals(event))
		{
			refreshLastEvent();
			if (latestEvent != null)
			{
				ClockHandler.setAlarm(context, latestEvent);
			}
			
			ClockHandler.cancelEventAlarm(context, event);			
		}
	}
	
	/**
	 * Updating data member (if needed) 'lastestEvent' from the database.
	 * Should be called with open db connection.
	 */
	private void refreshLastEvent()
	{
		if (latestEvent == null)
		{
			latestEvent = dbAdapter.getNextEvent();
		}		
	}

}
