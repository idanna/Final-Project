package clock.sched;

import java.util.Calendar;

import clock.db.DbAdapter;
import clock.db.Event;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ClockHandler extends BroadcastReceiver 
{
	private static final long TIMES_UP = 0l;
	
	/**
	 * Setting an alarm to the event time - extra Time (in minutes);
	 * @param context
	 * @param event The event to schedule
	 * @param extraTime Extra minutes to substract from the event actual time. 
	 */
	public static void setAlarm(Context context, Event event, int extraTime) 
	{
		AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		PendingIntent pendingIntent = getPendingIntent(context, event);
		Calendar time = Calendar.getInstance();
		Log.d("ALARM", "Set Alarm To:" + event.toString());
		time.set(event.getYear(), event.getMonth() - 1, event.getDay(), event.getHour(), event.getMin(), 0);

		// TODO: set the time to 1/3 if it's greater then some minimum
		time.add(Calendar.MINUTE, -extraTime);
		alarmMgr.set(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(), pendingIntent);
	}
	
	private static PendingIntent getPendingIntent(Context context, Event event)
	{
		Intent intent = new Intent(context, ClockHandler.class);
		intent.putExtra("eventStr", event.encodeToString());
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
		return pendingIntent;
	}

	// moved into Event member funcion.
	//	public static long getTimesLeftToEvent(Event event)	
	@Override
	public void onReceive(Context context, Intent i) 
	{
		Log.d("ALARM", "Inside OnReceive:");
		DbAdapter db = new DbAdapter(context);
		db.open();
		Event nextEvent = db.getNextEvent();
		db.close();
		if (nextEvent != null)
		{
			long timesLeftToEvent = nextEvent.getTimesLeftToEvent();
			
			// If the event time has not passed yet
			if (timesLeftToEvent > TIMES_UP)
			{
				try
				{
					long travelTime = GoogleAdapter.getTravelTimeToEvent(context, nextEvent, null);
					long arrangeTime = nextEvent.getWithAlarmStatus() == true ? db.getArrangeTime() : 0;
					long timesLeftToGoOut = timesLeftToEvent - travelTime;
					
					// User interaction if needed
					EventProgressHandler.handleEventProgress(context, nextEvent, timesLeftToGoOut, arrangeTime);
					
					setNextAlarm(context, arrangeTime, travelTime, nextEvent);
				}
				catch (Exception ex)
				{
					
				}
			}
			else
			{
				//TODO: Event time is up
				nextEvent = db.getNextEvent();
			}			
		}
		
	} 


	private void setNextAlarm(Context context,long arrangeTime, long travelTime, Event nextEvent) 
	{
		Log.d("ALARM", "Inside setNextAlarm:");
		try // what if there's an internet problem when trying to set the next alarm ? (same in AlarmManager)
		{
			Log.d("ALARM", "set: " + nextEvent + "Travel/Arrage" + travelTime + "//" + arrangeTime);
			ClockHandler.setAlarm(context, nextEvent, (int)(travelTime + arrangeTime));
		} 
		catch (Exception e) 
		{
			Log.d("ALARM", "Could'nt set cont alarm to: " + nextEvent);
		}
	}

	public static void cancelEventAlarm(Context context, Event event) 
	{
		Log.d("ALARM", "Cancel Event:" + event.toString());
		AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		PendingIntent pendingIntent = getPendingIntent(context, event);
		alarmMgr.cancel(pendingIntent);		
	}
}
