package clock.sched;

import java.security.acl.LastOwnerException;

import android.content.Context;
import android.util.Log;

import clock.db.DbAdapter;
import clock.db.Event;

public class AlarmsManager 
{
	private DbAdapter dbAdapter;
	private Context context;
	private Event latestEvent;
	
	public AlarmsManager(Context context, DbAdapter dbAdapter) 
	{
		super();
		this.dbAdapter = dbAdapter;
		this.context = context;
	}

	public void newEvent(Event newEvent)
	{
		if (latestEvent == null)
		{
			dbAdapter.open();
			latestEvent = dbAdapter.getNextEvent();
			dbAdapter.close();
		}
		
		//DOTO: should be 3 options, BEFORE, AFTER, CRASH
		if (Event.isEarlier(newEvent, latestEvent))
		{
			handleLatestEventChange(newEvent);
		}
		
	}
	
	private void handleLatestEventChange(Event eventToSchedual) 
	{
		ClockHandler.cancelEventAlarm(context, latestEvent);
		ClockHandler.setAlarm(context, eventToSchedual);		
	}

	private void cancelEvent(Event event)
	{
		
	}
	
	private void getLatestEven(Event event)
	{
		
	}

}
