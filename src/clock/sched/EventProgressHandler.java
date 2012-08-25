package clock.sched;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;
import clock.db.DbAdapter;
import clock.db.Event;
import clock.outsources.GoogleTrafficHandler.TrafficData;
import clock.outsources.dependencies.WeatherModel;

public class EventProgressHandler{
	
	private static final long GO_OUT_REMINDER_TIME = 1000 * 60 * 15;	// '15 minutes to go' reminder
	private static boolean userHasBeenNotified;
	private static long userHasBeenWakedUp;
	private static final int NOTIFICATION_ID = 1;
	private static final int WAKEUP_ID = 2;
	private static final int CRITICAL_ID = 3;
	
		
	/**
	 * Should be called from clock handler, after alarm received. 
	 */
	synchronized public static void handleEventProgress(Context context, Event event, long timesLeftToGoOut, long arrangeTime)
	{
		Log.d("PROGRESS", "Handling event progress from clock handler");
		loadDetailsFromEvent(event);

		// Alarm user to wake up if needed
		if (isItTimeToWakeUp(timesLeftToGoOut, arrangeTime))
			wakeupUser(context, event, arrangeTime);		
		
		// Remind user to go out if needed
		if (timesLeftToGoOut <= GO_OUT_REMINDER_TIME)
		{
			String msg = "Time to go out in " + TimeUnit.MILLISECONDS.toMinutes(timesLeftToGoOut) +  " Minutes";			
			notifyUser(context, msg);
		}
		
		saveDetailsToEvent(event, context);
	}


	/**
	 * Should be called from location handler, after location changed update received.
	 */
	synchronized public static void handleEventProgress(Context context, Event event, Location location)
	{
		Log.d("PROGRESS", "Handling event progress from location handler");
		loadDetailsFromEvent(event);
		
		if (userHasBeenWakedUp != 0)
		{
			try
			{
				String origin = GoogleAdapter.getOrigin(context, location);
				WeatherModel weatherData = GoogleAdapter.getWeatherModel(origin);
				AlarmsManager alarmsManager = new AlarmsManager(context, new DbAdapter(context));
				int arrangementTime = getArrangementTime();
				alarmsManager.UserGotOut(event, arrangementTime, weatherData);
			}
			catch (Exception e) {
				Log.e("PROGRESS", "Can't update User Got Out, error: " + e.getMessage());
			}
		}
		else
		{
			try
			{
				TrafficData trafficData = GoogleAdapter.getTrafficData(context, event, location);
				long diffTime = TimeUnit.SECONDS.toMillis(trafficData.getDuration()) - event.getTimesLeftToEvent();
				
				if (diffTime > 0)
				{
					String msg = "You're going to be late for event - " + event.toString();
					criticalMsg(context, msg);
				}
				else if (diffTime <= GO_OUT_REMINDER_TIME)
				{
					String msg = "Time to go out in " + TimeUnit.MILLISECONDS.toMinutes(diffTime) +  " Minutes";
					notifyUser(context, msg);
				}
				
			}
			catch (Exception e) {
				Log.e("PROGRESS", "Error while trying to check if user is late: " + e.getMessage());
			}
		}
		
		saveDetailsToEvent(event, context);
		
	}

	private static int getArrangementTime() {
		long currentTime = System.currentTimeMillis();
		return (int)(currentTime - userHasBeenWakedUp);
	}


	synchronized private static boolean isItTimeToWakeUp(long timesLeftToGoOut, long arrangeTime) 
	{
		
		// In case no arrangement time is needed
		if (arrangeTime == 0) return false;
		
		Calendar currentCalendar = Calendar.getInstance();
		long timeToWakeUp = timesLeftToGoOut - arrangeTime;
		
		return timeToWakeUp <= currentCalendar.getTimeInMillis()? true : false;
	}
	
	synchronized private static void wakeupUser(Context context, Event event, long arrangeTimeInMillis)
	{
		if (userHasBeenWakedUp == 0)
			return;
		Log.d("PROGRESS", "Waking up the user");
		
		String msg = "Times to wake up, Arrange time is " 
				+ TimeUnit.MILLISECONDS.toMinutes(arrangeTimeInMillis)
				+ " Minutes";
		
		//TODO: can't start activity from context
		//TODO: set location handler to notify after 100 meters movement
		
		userHasBeenWakedUp = System.currentTimeMillis();
		
		// Reset location handler to notify after 100 meters movement
		LocationHandler.cancelLocationListener(context, event);
		LocationHandler.setLocationListener(context, event, 1000);	//1000 since update is set to 10% of distance
		
		Log.d("PROGRESS", "User has been waked up with message: " + msg);
	}
	
	synchronized private static void notifyUser(Context context, String msg)
	{
		if (userHasBeenNotified)
			return;
		
		Log.d("PROGRESS", "Notifying user with: " + msg);
		
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		
		Notification notification = getBasicNotification(context, msg);
		notification.defaults |= Notification.DEFAULT_SOUND;

		notificationManager.notify(NOTIFICATION_ID, notification);
		Log.d("PROGRESS", "User has been notified");
		
		userHasBeenNotified = true;
	}


	synchronized private static void criticalMsg(Context context, String msg) 
	{
		if (userHasBeenNotified)
			return;
		
		Log.d("PROGRESS", "Critical message to user with: " + msg);
		
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		
		Notification notification = getBasicNotification(context, msg);
		notification.defaults |= Notification.DEFAULT_SOUND;
		notification.flags |= Notification.FLAG_INSISTENT;		//This will cause annoying notification
				
		notificationManager.notify(CRITICAL_ID, notification);
		Log.d("PROGRESS", "User has been critical messaged");
		
		userHasBeenNotified = true;		
	}
	
	synchronized private static Notification getBasicNotification(Context context, String msg) 
	{
		int icon = R.drawable.icon;
		long when = System.currentTimeMillis();		//For showing the notification now
		Notification notification = new Notification(icon, msg, when);
		String title = "AppDate";
		Intent intent = new Intent(context, EventProgressHandler.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
		
		notification.setLatestEventInfo(context, title, msg, pendingIntent);
		
		return notification;
	}

	synchronized private static void loadDetailsFromEvent(Event event) {
		userHasBeenNotified = event.isUserHasBeenNotified();
		userHasBeenWakedUp = event.getUserHasBeenWakedUp();
		
	}
	
	/**
	 * This will save the event's userHasBeenNotified and userHasBeenWakedUp.
	 * The event then will be updated in DB.
	 * @param event
	 * @param context
	 */
	synchronized private static void saveDetailsToEvent(Event event, Context context) {
		event.setUserHasBeenNotified(userHasBeenNotified);
		event.setUserHasBeenWakedUp(userHasBeenWakedUp);
		DbAdapter dbAdapter = new DbAdapter(context);
		dbAdapter.open();
		dbAdapter.deleteEvent(event);
		dbAdapter.insertEvent(event);
		dbAdapter.close();
	}
}
