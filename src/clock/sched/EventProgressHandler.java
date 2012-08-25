package clock.sched;

import java.util.concurrent.TimeUnit;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;
import clock.db.Event;
import clock.outsources.GoogleTrafficHandler.TrafficData;

public class EventProgressHandler{
	
	private static final long GO_OUT_REMINDER_TIME = 1000 * 60 * 10;	// '10 minutes to go' reminder
	private static boolean userHasBeenNotified;
	private static boolean userHasBeenWakedUp;
	private static final int NOTIFICATION_ID = 1;
	private static final int WAKEUP_ID = 2;
	private static final int CRITICAL_ID = 3;
	
		
	/**
	 * Should be called from clock handler, after alarm received. 
	 */
	public static void handleEventProgress(Context context, Event event, long timesLeftToGoOut, long arrangeTime)
	{
		Log.d("PROGRESS", "Handling event progress from clock handler");
		loadDetailsFromEvent(event);

		// Alarm user to wake up if needed
		if (isItTimeToWakeUp(timesLeftToGoOut, arrangeTime))
			wakeupUser(context, arrangeTime);		
		
		// Remind user to go out if needed
		if (timesLeftToGoOut <= GO_OUT_REMINDER_TIME)
		{
			String msg = "Time to go out in " + TimeUnit.MILLISECONDS.toMinutes(timesLeftToGoOut) +  " Minutes";			
			notifyUser(context, msg);
		}
		
		saveDetailsToEvent(event);
	}


	/**
	 * Should be called from location handler, after location changed update received.
	 */
	public static void handleEventProgress(Context context, Event event, Location location)
	{
		Log.d("PROGRESS", "Handling event progress from location handler");
		loadDetailsFromEvent(event);
		
		if (userHasBeenWakedUp)	//Case that user has already been waked up and moved 100 meters atleast
		{
			//TODO: notify the learning machine.
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
		
		saveDetailsToEvent(event);
		
	}

	private static boolean isItTimeToWakeUp(long timesLeftToGoOut, long arrangeTime) 
	{
		Log.d("PROGRESS", "Checking if its time to wake up");
		
		//TODO: this is only for now!!!
		return true;
		
/*		// In case no arrangement time is needed
		if (arrangeTime == 0) return false;
		
		Calendar currentCalendar = Calendar.getInstance();
		long timeToWakeUp = timesLeftToGoOut - arrangeTime;
		
		return timeToWakeUp <= currentCalendar.getTimeInMillis()? true : false;*/
	}
	
	private static void wakeupUser(Context context, long arrangeTimeInMillis)
	{
		if (userHasBeenWakedUp)
			return;
		Log.d("PROGRESS", "Waking up the user");
		
		String msg = "Times to wake up, Arrange time is " 
				+ TimeUnit.MILLISECONDS.toMinutes(arrangeTimeInMillis)
				+ " Minutes";
		
		//TODO: can't start activity from context
		//TODO: set location handler to notify after 100 meters movement
		
		userHasBeenWakedUp = true;
		Log.d("PROGRESS", "User has been waked up with message: " + msg);
	}
	
	private static void notifyUser(Context context, String msg)
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


	private static void criticalMsg(Context context, String msg) 
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
	
	private static Notification getBasicNotification(Context context, String msg) {
		int icon = R.drawable.icon;
		long when = System.currentTimeMillis();		//For showing the notification now
		Notification notification = new Notification(icon, msg, when);
		String title = "AppDate";
		Intent intent = new Intent(context, EventProgressHandler.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
		
		notification.setLatestEventInfo(context, title, msg, pendingIntent);
		
		return notification;
	}

	private static void loadDetailsFromEvent(Event event) {
		userHasBeenNotified = event.isUserHasBeenNotified();
		userHasBeenWakedUp = event.isUserHasBeenWakedUp();
		
	}
	
	private static void saveDetailsToEvent(Event event) {
		event.setUserHasBeenNotified(userHasBeenNotified);
		event.setUserHasBeenWakedUp(userHasBeenWakedUp);
		
	}
}
