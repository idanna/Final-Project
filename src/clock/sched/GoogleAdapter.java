package clock.sched;

import clock.db.Event;
import clock.outsources.GoogleTrafficHandler;
import clock.outsources.GoogleWeatherHandler;

public class GoogleAdapter {
	
	private static GoogleTrafficHandler gTrafficHandler = new GoogleTrafficHandler();
	private static GoogleWeatherHandler gWeatherHandler = new GoogleWeatherHandler();
	
	private static String lastOrigin;
	private static Event lastEvent;
	
	public static long getTravelTimeToEvent(Event nextEvent) {
		// TODO Auto-generated method stub
		return 0;
	}

	public static float getDistanceToEventLocation(Event event) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public static boolean isLegalAddress(String address)
	{
		// TODO Auto-generated method stub
		return true;
	}

	public static boolean isInternetConnected() {
		// TODO Auto-generated method stub
		return true;
	}

}
