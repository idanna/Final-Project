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
	private LocationHandler locationHandler;
	
	public AlarmsManager(Context context, DbAdapter dbAdapter) 
	{
		super();
		this.dbAdapter = dbAdapter;
		this.context = context;
		this.locationHandler = new LocationHandler(context);
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
			this.latestEvent = newEvent;
			locationHandler.setLocationListener(this.latestEvent);
		}
		
	}

	private void cancelEvent(Event event)
	{
		
	}
	
	private void getLatestEven(Event event)
	{
		
	}

}
