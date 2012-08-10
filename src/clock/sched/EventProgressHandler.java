package clock.sched;

import android.app.Notification;
import android.content.Context;
import clock.db.Event;

public class EventProgressHandler {
	
	private static final long GO_OUT_REMINDER_TIME = 1000 * 60 * 5;	//5 minutes to go reminder
	private static Event lastEvent;
	private static long TIMES_TO_GO_OUT = 0l;
//	private static float lastDistanceToEventLocation = 0f;
//	private static long lastTimesLeftToGoOut = 0l;
//	private static long lastArrangeTime = 0l;
	
		
	/**
	 * Should be called from clock handler, after alarm received. 
	 */
	public static void handleEventProgress(Context context, Event event, long timesLeftToGoOut, long arrangeTime)
	{
		// initialization
		lastEvent = lastEvent == null? event : lastEvent;
//		lastTimesLeftToGoOut = timesLeftToGoOut;
//		lastArrangeTime = arrangeTime;
		
		if (timesLeftToGoOut > 0 && timesLeftToGoOut <= GO_OUT_REMINDER_TIME)
		{
			notifyUser("Time to go out in: " + GO_OUT_REMINDER_TIME + " minutes.");
		}
		
	}
	
	/**
	 * Should be called from location handler, after location changed update received.
	 */
	public static void handleEventProgress(Context context, Event event, float distanceToEventLocation)
	{
		// initialization
		lastEvent = lastEvent == null? event : lastEvent;
//		lastDistanceToEventLocation = distanceToEventLocation;
		
	}
	
	private static void alarmUser(String msg)
	{
		
	}
	
	private static void notifyUser(String msg)
	{
		
	}



}
