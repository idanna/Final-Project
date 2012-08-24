package clock.sched;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import android.app.AlertDialog;
import android.app.Notification;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
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
			wakeupUser(context, arrangeTime);		
		
		// Remind user to go out if needed
		if (timesLeftToGoOut <= GO_OUT_REMINDER_TIME)
		{
			final String msg = "Time to go out in " + TimeUnit.MILLISECONDS.toMinutes(timesLeftToGoOut) +  " Minutes";
			DialogInterface.OnClickListener notifyApprovedListener = new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					Log.d("PROGRESS", "Dialog result, button clicked: " + which);
					userHasBeenNotified = true;
					Log.d("PROGRESS", "User has been notified: " + msg);						
				}
			};
			
			notifyUser(context, msg, notifyApprovedListener);
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
			
//			if (diffTime > 0)
//			{
//				criticalMsg(context, event);
//			}
//			else if (diffTime <= GO_OUT_REMINDER_TIME)
//			{
//				notifyUser(context, diffTime, event);
//			}
			
		}
		catch (Exception e) {
			Log.e("PROGRESS", "Error while trying to check if user is late: " + e.getMessage());
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
	
	synchronized private static void wakeupUser(Context context, long arrangeTimeInMillis)
	{
		if (userHasBeenWakedUp)
			return;
		
		//TODO: alert user with wake up alarm clock
		userHasBeenWakedUp = true;
		Log.d("PROGRESS", "User has been waked up and arrange time is: " +
				TimeUnit.MILLISECONDS.toMinutes(arrangeTimeInMillis) + " Minutes");
	}
	
	synchronized private static void notifyUser(Context context, String msg, DialogInterface.OnClickListener callBackListener)
	{
		if (userHasBeenNotified)
			return;
		
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
		dialogBuilder.setMessage(msg)
		.setPositiveButton("Ok", callBackListener)
		.setCancelable(false)
		.setNegativeButton("Snooze", callBackListener)
		.setNeutralButton("Show Info", callBackListener);
		
		AlertDialog alertDialog = dialogBuilder.create();		
		alertDialog.show();
	}
	
	synchronized private static void loadDetailsFromEvent(Event event) {
		userHasBeenNotified = event.isUserHasBeenNotified();
		userHasBeenWakedUp = event.isUserHasBeenWakedUp();
		
	}
	
	synchronized private static void saveDetailsToEvent(Event event) {
		event.setUserHasBeenNotified(userHasBeenNotified);
		event.setUserHasBeenWakedUp(userHasBeenWakedUp);
		
	}

}
