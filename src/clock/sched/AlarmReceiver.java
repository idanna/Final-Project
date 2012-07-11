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

public class AlarmReceiver extends BroadcastReceiver 
{
	public static void setAlarm(Context c, Event event) 
	{
		AlarmManager alarmMgr = (AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(c, AlarmReceiver.class);
		intent.putExtra("eventStr", event.toString());
		PendingIntent pendingIntent = PendingIntent.getBroadcast(c, 0, intent, 0);
		Calendar time = Calendar.getInstance();
		Log.d("ALARM", event.toString());
		time.set(event.getYear(), event.getMonth(), event.getDay(), event.getHour(), event.getMin(), 0);
		alarmMgr.set(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(), pendingIntent);
	}
	
	@Override
	public void onReceive(Context context, Intent i) 
	{
		Bundle b = i.getExtras();
		String eventStr = b.getString("eventStr");
		DbAdapter db = new DbAdapter(context);
		db.open();
		Event nextEvent = db.getNextEvent();
		db.close();
		if(nextEvent == null)
		{
			Log.d("ALARM", "There's not to alarm");
		}
		
		try {
			Log.d("ALARM", "next Event:" + nextEvent.toString());
			//Toast.makeText(context, nextEvent.toString(), Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

}
