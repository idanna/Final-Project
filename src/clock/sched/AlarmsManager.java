package clock.sched;

import android.content.Context;

import clock.db.DbAdapter;
import clock.db.Event;
import clock.db.Event.eComparison;

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
		
		if (latestEvent == null || Event.compareBetweenEvents(newEvent, latestEvent) == eComparison.BEFORE)
		{
			handleLatestEventChange(newEvent);
		}
		
	}
	
	private void handleLatestEventChange(Event newLatest) 
	{
		if(latestEvent != null)
		{
			ClockHandler.cancelEventAlarm(context, latestEvent);
		}
		
		this.latestEvent = newLatest;
		ClockHandler.setAlarm(context, newLatest);		
	}

	private void cancelEvent(Event event)
	{
		
	}
	
	private void getLatestEven(Event event)
	{
		
	}

}
