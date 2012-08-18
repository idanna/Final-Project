package clock.sched;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import android.app.AlertDialog;
import android.app.Notification;
import android.content.Context;
import android.location.Location;
import android.util.Log;
import clock.db.Event;
import clock.outsources.GoogleTrafficHandler.TrafficData;

public class EventProgressHandler {
	
	private static final long GO_OUT_REMINDER_TIME = 1000 * 60 * 10;	// '10 minutes to go' reminder
	private static boolean userHasBeenNotified;
	private static boolean userHasBeenWakedUp;
	
		
	/**
	 * Should be called from clock handler, after alarm received. 
	 */
	synchronized public static void handleEventProgress(Context context, Event event, long timesLeftToGoOut, long arrangeTime)
	{
		loadDetailsFromEvent(event);

		// Alarm user to wake up if needed
		if (isItTimeToWakeUp(timesLeftToGoOut, arrangeTime))
			wakeupUser(arrangeTime);		
		
		// Remind user to go out if needed
		if (timesLeftToGoOut <= GO_OUT_REMINDER_TIME)
		{
			notifyUser(timesLeftToGoOut, event);
		}
		
		saveDetailsToEvent(event);
	}


	/**
	 * Should be called from location handler, after location changed update received.
	 */
	synchronized public static void handleEventProgress(Context context, Event event, Location location)
	{
		loadDetailsFromEvent(event);
		
		try
		{
			TrafficData trafficData = GoogleAdapter.getTrafficData(context, event, location);
			long diffTime = TimeUnit.SECONDS.toMillis(trafficData.getDuration()) - event.getTimesLeftToEvent();
			
			if (diffTime > 0)
			{
				criticalMsg(event);
			}
			else if (diffTime <= GO_OUT_REMINDER_TIME)
			{
				notifyUser(diffTime, event);
			}
			
		}
		catch (Exception e) {
			Log.e("EventProgressHandler", "Error while trying to check if user is late: " + e.getMessage());
		}
		
		saveDetailsToEvent(event);
		
	}



	private static boolean isItTimeToWakeUp(long timesLeftToGoOut, long arrangeTime) 
	{
		// In case no arrangement time is needed
		if (arrangeTime == 0) return false;
		
		Calendar currentCalendar = Calendar.getInstance();
		long timeToWakeUp = timesLeftToGoOut - arrangeTime;
		
		return timeToWakeUp <= currentCalendar.getTimeInMillis()? true : false;
	}
	
	synchronized private static void wakeupUser(long arrangeTimeInMillis)
	{
		if (userHasBeenWakedUp)
			return;
		
		//TODO: alert user with wake up alarm clock
		userHasBeenWakedUp = true;
	}
	
	synchronized private static void notifyUser(long timesLeftInMillis, Event event)
	{
		if (userHasBeenNotified)
			return;
		//TODO: notify user with a reminder. with option to see:
		//	duration, distance, weather in location, etc.
		userHasBeenNotified = true;
	}
	
	synchronized private static void criticalMsg(Event event) {
		
		userHasBeenNotified = true;
	}
	
	synchronized private static void loadDetailsFromEvent(Event event) {
		// TODO Auto-generated method stub
		
	}
	

	synchronized private static void saveDetailsToEvent(Event event) {
		// TODO Auto-generated method stub
		
	}

}
