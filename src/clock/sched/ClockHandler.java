package clock.sched;

import java.util.Calendar;

import clock.db.DbAdapter;
import clock.db.Event;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class ClockHandler extends BroadcastReceiver 
{
	public static void setAlarm(Context context, Event event) 
	{
		AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		PendingIntent pendingIntent = getPendingIntent(context, event);
		Calendar time = Calendar.getInstance();
		Log.d("ALARM", "Set Alarm To:" + event.toString());
		time.set(event.getYear(), event.getMonth() - 1, event.getDay(), event.getHour(), event.getMin(), 0);
		alarmMgr.set(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(), pendingIntent);
	}
	
	private static PendingIntent getPendingIntent(Context context, Event event)
	{
		Intent intent = new Intent(context, ClockHandler.class);
		intent.putExtra("eventStr", event.toString());
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
		return pendingIntent;
	}
	
	public static long getTimesLeftToEvent(Event event)
	{
		Calendar currentCalendar = Calendar.getInstance();
		Calendar eventCalendar = event.toCalendar();
				
		return eventCalendar.getTimeInMillis() - currentCalendar.getTimeInMillis();
	}
	
	
	@Override
	public void onReceive(Context context, Intent i) 
	{
		Log.d("ALARM", "Inside OnReceive:");
		Bundle b = i.getExtras();
		String eventStr = b.getString("eventStr");
		Event eventToHandle = Event.CreateFromString(eventStr);
		Log.d("ALARM", "In bundle: " + eventToHandle.toString());
		DbAdapter db = new DbAdapter(context);
		db.open();
		Event nextEvent = db.getNextEvent();
		db.close();
		if(nextEvent == null)
		{
			Log.d("ALARM", "There's not to alarm");
		}
		else
		{
			try {
				Log.d("ALARM", "next Event:" + nextEvent.toString());
				//Toast.makeText(context, nextEvent.toString(), Toast.LENGTH_LONG).show();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static void cancelEventAlarm(Context context, Event latestEvent) 
	{
		Log.d("ALARM", "Cancel Event:" + latestEvent.toString());
		AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		PendingIntent pendingIntent = getPendingIntent(context, latestEvent);
		alarmMgr.cancel(pendingIntent);		
	}

}
