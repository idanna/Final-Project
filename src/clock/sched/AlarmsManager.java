package clock.sched;

import java.security.acl.LastOwnerException;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import clock.db.DbAdapter;
import clock.db.Event;
import clock.db.Event.eComparison;
import clock.outsources.GoogleTrafficHandler;
import clock.outsources.GoogleTrafficHandler.TrafficData;

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
	private LocationHandler locationHandler;;
//	private LocationHandler locationHandler;
	
	public AlarmsManager(Context context, DbAdapter dbAdapter) 
	{
		super();
		this.locationHandler = new LocationHandler(context);
		this.dbAdapter = dbAdapter;
		this.context = context;
		this.locationHandler = new LocationHandler(context);
	}
	/**
	 * Informs the alarm manager about a new event.
	 * saves the alarm in db, manage the alarm set/cancel in case needed.
	 * @param newEvent 
	 * @throws Exception in case of problems with google geo. Event is not saved nor alarms change.
	 */
	public void newEvent(Event newEvent) throws Exception
	{
		// first trying to get time to place, to assure address is correct.
		//$$ only for now
		//long timeToPlace = locationHandler.getMinTimeInterval(newEvent.getLocation()).getDuration();
		long timeToPlace = 0;
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
			}
			
			this.latestEvent = newEvent;
			ClockHandler.setAlarm(context, latestEvent, ((int)timeToPlace + arrageTime));
//					locationHandler.setLocationListener(this.latestEvent);
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
				long timeToPlace = locationHandler.getMinTimeInterval(latestEvent.getLocation()).getDuration();
				int timeToArrange = dbAdapter.getArrangeTime();
				ClockHandler.setAlarm(context, latestEvent, (int)(timeToArrange + timeToPlace));
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
