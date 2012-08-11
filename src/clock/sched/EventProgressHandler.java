package clock.sched;

import java.util.Calendar;

import android.app.AlertDialog;
import android.app.Notification;
import android.content.Context;
import clock.db.Event;

public class EventProgressHandler {
	
	private static final long GO_OUT_REMINDER_TIME = 1000 * 60 * 5;	// '5 minutes to go' reminder
	private static final float USER_STEP = 100f;					// 100 meter step is counted as movement
	private static Event lastEvent;
	private static float lastDistanceToEventLocation = 0f;
	private static boolean userOnHisWay;
	private static boolean userHasBeenNotified;
	private static boolean userHasBeenWakedUp;
	
		
	/**
	 * Should be called from clock handler, after alarm received. 
	 */
	public static synchronized void handleEventProgress(Context context, Event event, long timesLeftToGoOut, long arrangeTime)
	{
		// First invocation		
		setLastEvent(getLastEvent() == null? event : getLastEvent());	
		
		// If event has been changed
		if (!getLastEvent().equals(event)) 								
		{
			setLastEvent(event);
			setUserOnHisWay(false);
			setUserHasBeenNotified(false);
			setUserHasBeenWakedUp(false);
			setLastDistanceToEventLocation(0);
		}
		
		if (timesLeftToGoOut < 0)
		{
			//TODO: user is late
		}
		
		else if (!isUserOnHisWay())
		{
			// Alarm user to wake up if needed
			if (!isUserHasBeenWakedUp() && isItTimeToWakeUp(timesLeftToGoOut, arrangeTime))
			{
				alarmUser("Time to start arrangements, go out in: " + arrangeTime + " minutes.");
				setUserHasBeenWakedUp(true);
			}
			
			
			// Remind user to go out if needed
			if (!isUserHasBeenNotified() && timesLeftToGoOut <= GO_OUT_REMINDER_TIME)
			{
				notifyUser("Time to go out in: " + GO_OUT_REMINDER_TIME + " minutes.", event);
				setUserHasBeenNotified(true);
			}
		}
		
	}

	/**
	 * Should be called from location handler, after location changed update received.
	 */
	public static synchronized void handleEventProgress(Context context, Event event, float distanceToEventLocation)
	{
		// First invocation	
		setLastEvent(getLastEvent() == null? event : getLastEvent());	
				
		// If event has been changed
		if (!getLastEvent().equals(event)) 								
		{
			setLastEvent(event);
			setUserOnHisWay(false);
			setUserHasBeenNotified(false);
			setUserHasBeenWakedUp(false);
			setLastDistanceToEventLocation(0);
		}
		
		// The 'movement of user in space' check is to mask user notification and alarms
		if (getLastDistanceToEventLocation() != 0)
		{
			// Check if user has moved toward the event location
			if (getLastDistanceToEventLocation() - distanceToEventLocation >= USER_STEP)
			{
				setUserOnHisWay(true);
			}
			else
			{			
				// Check if user has not moved or if his moved the other way
				float absoluteMovement = getLastDistanceToEventLocation() - distanceToEventLocation;
				absoluteMovement = absoluteMovement < 0f? absoluteMovement * (-1f) : absoluteMovement;
				if (absoluteMovement < USER_STEP ||
						distanceToEventLocation - getLastDistanceToEventLocation() >= USER_STEP)
				{
					setUserOnHisWay(false);
					//TODO: check if user is late
				}
			}
		}
		
		// Finally set last distance to the current one for next time calculation
		setLastDistanceToEventLocation(distanceToEventLocation);
		
	}

	private static boolean isItTimeToWakeUp(long timesLeftToGoOut, long arrangeTime) 
	{
		// In case no arrangement time is needed
		if (arrangeTime == 0) return false;
		
		Calendar currentCalendar = Calendar.getInstance();
		long timeToWakeUp = timesLeftToGoOut - arrangeTime;
		
		return timeToWakeUp <= currentCalendar.getTimeInMillis()? true : false;
	}
	
	private static void alarmUser(String msg)
	{
		//TODO: alert user with wake up alarm clock
	}
	
	private static void notifyUser(String msg, Event event)
	{
		//TODO: notify user with a reminder. with option to see:
		//	duration, distance, weather in location, etc.
	}


	// ********************************* Synchronized getters / setters: *********************************//
	
	private static synchronized Event getLastEvent() {
		return lastEvent;
	}

	private static synchronized void setLastEvent(Event lastEvent) {
		EventProgressHandler.lastEvent = lastEvent;
	}

	private static synchronized float getLastDistanceToEventLocation() {
		return lastDistanceToEventLocation;
	}

	private static synchronized void setLastDistanceToEventLocation(
			float lastDistanceToEventLocation) {
		EventProgressHandler.lastDistanceToEventLocation = lastDistanceToEventLocation;
	}

	private static synchronized boolean isUserOnHisWay() {
		return userOnHisWay;
	}

	private static synchronized void setUserOnHisWay(boolean userOnHisWay) {
		EventProgressHandler.userOnHisWay = userOnHisWay;
	}

	private static synchronized boolean isUserHasBeenNotified() {
		return userHasBeenNotified;
	}

	private static synchronized void setUserHasBeenNotified(
			boolean userHasBeenNotified) {
		EventProgressHandler.userHasBeenNotified = userHasBeenNotified;
	}

	private static synchronized boolean isUserHasBeenWakedUp() {
		return userHasBeenWakedUp;
	}

	private static synchronized void setUserHasBeenWakedUp(
			boolean userHasBeenWakedUp) {
		EventProgressHandler.userHasBeenWakedUp = userHasBeenWakedUp;
	}
	
	// ***************************************************************************************************//

}
