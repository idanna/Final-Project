package clock.sched;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import clock.db.DbAdapter;
import clock.db.Event;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.format.Time;
import android.util.Log;

public class ClockHandler extends BroadcastReceiver 
{
	// Stop when event is 2 minutes ahead
	private static final long TIMES_UP = (60 * 1000);

	public static void setAlarm(Context context, Event event, int extraTime)
	{
		setAlarm(context, event, extraTime, false);
	}	
	
	/**
	 * Setting an alarm to the event time - extra Time (in minutes);
	 * @param context
	 * @param event The event to schedule
	 * @param extraTime Extra secnods to substract from the event actual time. 
	 * @param setAfterEvent - if true, then the next alarm will be set to 1 min after the event -
	 * should be used after the time to the event is after TIMES_UP.
	 */
	private static void setAlarm(Context context, Event event, int extraTime, boolean setAfterEvent) 
	{
		AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		PendingIntent pendingIntent = getPendingIntent(context, event);
		long alarmMiliSecond = setAfterEvent == true ? event.toCalendar().getTimeInMillis() + TIMES_UP : 
														calNextAlarm(event, extraTime);
		// for debug
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(alarmMiliSecond);
		Log.d("ALARM", "setAlarm: " + c.getTime());
		alarmMgr.set(AlarmManager.RTC_WAKEUP, alarmMiliSecond, pendingIntent);	
	}
		
	private static void setAfterEventAlarm(Context context, Event event) 
	{
		Log.d("ALARM", "Inside setAfterAlarm");
		setAlarm(context, event, 0, true);
	}
	
	private static long calNextAlarm(Event event, int extraTime) 
	{
		Calendar calander = Calendar.getInstance();
		Time t = new Time();
		t.setToNow();
		long currentTime = t.toMillis(false);
		calander.setTimeInMillis(currentTime);
		Log.d("ALARM", "Current Time: " + calander.getTime());
		
		calander = event.toCalendar();
		calander.add(Calendar.SECOND, -extraTime);
		Log.d("ALARM", "Time To go out: " + calander.getTime());
		long miliToGetOut = calander.getTimeInMillis();
		long miliToNextAlarm = ((miliToGetOut - currentTime) / 2) + currentTime;

		Calendar debugCal = Calendar.getInstance();
		debugCal.setTimeInMillis(miliToNextAlarm);
		Log.d("ALARM", "Next Alarm: " + debugCal.getTime());
		
		//In case user assigned event that time to go out has already passed
		if (miliToNextAlarm < 0)
		{
			miliToNextAlarm = 30 * 1000; //This will cause immediately response in 30 seconds 
		}

		return miliToNextAlarm;
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
			try
			{
				AlarmsManager am = new AlarmsManager(context, db);
				long travelTime = GoogleAdapter.getTravelTimeToEvent(context, nextEvent, null);
				long arrangeTime = am.getArrangmentTime(nextEvent);
				if(arrangeTime == -1)
				{
					//DOTO: there's no arrangment time in db,
					// What should we do ?
				}
				
				long timesLeftToGoOut = timesLeftToEvent - TimeUnit.SECONDS.toMillis(travelTime);				
				// User interaction if needed
				EventProgressHandler.handleEventProgress(context, nextEvent, timesLeftToGoOut, arrangeTime);
				
				// If the event time to go out has not passed yet
				Log.d("ALARM", String.valueOf(timesLeftToGoOut));
				if (timesLeftToGoOut - arrangeTime > TIMES_UP)
				{
					setAlarm(context, nextEvent, (int)(travelTime + arrangeTime));
				}
				else // ClockHandler move to the next event.
				{
					EventProgressHandler.goOutNotification(context, "It is time to go out, Drive safely");
					Log.d("ALARM", "TIMES IS UP!");
					setAfterEventAlarm(context, nextEvent);
				}	
			}
			catch (Exception ex)
			{
				Log.e("ALARM", "Failed to set next alarm for event: " + nextEvent.toString()
						 + "... With Error: " + ex.getStackTrace().toString());
			}
		
		}
		
	}

	public static void cancelEventAlarm(Context context, Event event) 
	{
		AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		PendingIntent pendingIntent = getPendingIntent(context, event);
		alarmMgr.cancel(pendingIntent);	
		Log.d("ALARM", "Reciever has been canceled for event: " + event.toString());
	}
}
