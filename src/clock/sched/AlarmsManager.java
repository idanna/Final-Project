package clock.sched;

import android.content.Context;

import clock.db.DbAdapter;
import clock.db.Event;
import clock.db.Event.eComparison;
import clock.exceptions.IllegalAddressException;
import clock.exceptions.InternetDisconnectedException;
import clock.outsources.GoogleWeatherHandler;

/**
 * Manager All alarms service in DataBase 
 * And the Android alarm services.
 * Manage all the internal alarm scheduling logic.
 *
 */
public class AlarmsManager 
{
	private DbAdapter dbAdapter;
	private Context context;
	private Event latestEvent;
	private LocationHandler locationHandler;;
//	private LocationHandler locationHandler;
	
	public AlarmsManager(Context context, DbAdapter dbAdapter) 
	{
		super();
		this.dbAdapter = dbAdapter;
		this.context = context;
	}
	
	/**
	 * Informs the alarm manager about a new event.
	 * saves the alarm in db, manage the alarm set/cancel in case needed.
	 * @param newEvent 
	 * @throws InternetDisconnectedException in case of problems with the internet connection
	 * @throws IllegalAddressException in case of problems with the new event address
	 **/
	public void newEvent(Event newEvent) throws IllegalAddressException, InternetDisconnectedException
	{
		if (!GoogleAdapter.isInternetConnected())
		{
			throw new InternetDisconnectedException();
		}
		if (!GoogleAdapter.isLegalAddress(newEvent.getLocation()))
		{
			throw new IllegalAddressException();
		}
		long timeToPlace = GoogleAdapter.getTravelTimeToEvent(newEvent);
		
		dbAdapter.open();
		refreshLastEvent();
		dbAdapter.insertEvent(newEvent);
		if(newEvent.getWithAlarmStatus() == true && newEvent.isAfterNow() &&
						(latestEvent == null || Event.compareBetweenEvents(newEvent, latestEvent) == eComparison.BEFORE))
		{
			int arrageTime = newEvent.getWithAlarmStatus() == true ? dbAdapter.getArrangeTime() : 0;
			if (latestEvent != null)
			{
				ClockHandler.cancelEventAlarm(context, latestEvent);
				LocationHandler.cancelLocationListener(context, latestEvent);
			}
			
			this.latestEvent = newEvent;
			ClockHandler.setAlarm(context, latestEvent, ((int)timeToPlace + arrageTime));
			LocationHandler.setLocationListener(context, latestEvent);
		}
		
		dbAdapter.close();		
	}

	public void deleteEvent(Event event) throws Exception
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
				long timeToPlace = GoogleAdapter.getTravelTimeToEvent(latestEvent);
				int timeToArrange = dbAdapter.getArrangeTime();
				ClockHandler.setAlarm(context, latestEvent, (int)(timeToArrange + timeToPlace));
				LocationHandler.setLocationListener(context, latestEvent);
			}
			
			ClockHandler.cancelEventAlarm(context, event);
			LocationHandler.cancelLocationListener(context, event);
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
