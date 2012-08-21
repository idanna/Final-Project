package clock.sched;

import java.util.Calendar;

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
	private static final long TIMES_UP = (5 * 60 * 100);
	
	/**
	 * Setting an alarm to the event time - extra Time (in minutes);
	 * @param context
	 * @param event The event to schedule
	 * @param extraTime Extra seconds to substract from the event actual time. 
	 */
	public static void setAlarm(Context context, Event event, int extraTime) 
	{
		AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		PendingIntent pendingIntent = getPendingIntent(context, event);
		long alarmMiliSecond = calNextAlarm(event, extraTime);
		alarmMgr.set(AlarmManager.RTC_WAKEUP, alarmMiliSecond, pendingIntent);			
	}
	
	private static long calNextAlarm(Event event, int extraTime) 
	{
		Calendar calander = Calendar.getInstance();
		Time t = new Time();
		t.setToNow();
		long currentTime = t.toMillis(false);
		calander.setTimeInMillis(currentTime);
		Log.d("ALARM", "Current Time: " + calander.get(Calendar.YEAR) + "-" + calander.get(Calendar.MONTH) + "-" + 
				calander.get(Calendar.DAY_OF_MONTH) + " " + calander.get(Calendar.HOUR_OF_DAY) + ":" + calander.get(Calendar.MINUTE));

		calander.set(event.getYear(), event.getMonth() - 1, event.getDay(), event.getHour(), event.getMin(), 0);
		calander.add(Calendar.SECOND, -extraTime);
		Log.d("ALARM", "Time To go out: " + calander.get(Calendar.YEAR) + "-" + calander.get(Calendar.MONTH) + "-" + 
				calander.get(Calendar.DAY_OF_MONTH) + " " + calander.get(Calendar.HOUR_OF_DAY) + ":" + calander.get(Calendar.MINUTE));
		long miliToGetOut = calander.getTimeInMillis();
		long miliToNextAlarm = ((miliToGetOut - currentTime) / 2) + currentTime;
		// for debug:
		Calendar debugCal = Calendar.getInstance();
		debugCal.setTimeInMillis(miliToNextAlarm);
		Log.d("ALARM", "Next Alarm: " + debugCal.get(Calendar.YEAR) + "-" + debugCal.get(Calendar.MONTH) + "-" + 
								debugCal.get(Calendar.DAY_OF_MONTH) + " " + debugCal.get(Calendar.HOUR_OF_DAY) + ":" + debugCal.get(Calendar.MINUTE));
		
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
//		Log.d("ALARM", "Inside OnReceive:");
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
			else // ClockHandler move to the next event.
			{
				setAlarm(context, nextEvent, -1); // Negative extra time. next alarm 1 min after this event.
			}			
		}
		
	}

	private void setNextAlarm(Context context,long arrangeTime, long travelTime, Event nextEvent) 
	{
//		Log.d("ALARM", "Inside setNextAlarm:");
		try // what if there's an internet problem when trying to set the next alarm ? (same in AlarmManager)
		{
//			Log.d("ALARM", "set: " + nextEvent + "Travel/Arrage" + travelTime + "//" + arrangeTime);
			ClockHandler.setAlarm(context, nextEvent, (int)(travelTime + arrangeTime));
		} 
		catch (Exception e) 
		{
			Log.e("ALARM", "Could'nt set cont alarm to: " + nextEvent);
		}
	}

	public static void cancelEventAlarm(Context context, Event event) 
	{
//		Log.d("ALARM", "Cancel Event:" + event.toString());
		AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		PendingIntent pendingIntent = getPendingIntent(context, event);
		alarmMgr.cancel(pendingIntent);		
	}
}
