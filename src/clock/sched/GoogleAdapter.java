package clock.sched;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import clock.db.Event;
import clock.outsources.GoogleTrafficHandler;
import clock.outsources.GoogleTrafficHandler.TrafficData;
import clock.outsources.GoogleWeatherHandler;

public class GoogleAdapter {
	
	private static GoogleTrafficHandler gTrafficHandler = new GoogleTrafficHandler();
	private static GoogleWeatherHandler gWeatherHandler = new GoogleWeatherHandler();
	
	private static String lastOrigin;
	private static Event lastEvent;
	private static float lastDistanceToEventLocation = 0;
	
	/**
	 * 
	 * @param context
	 * @param event
	 * @param location - if set to null then consider last known location from OS service
	 * @return
	 * @throws Exception
	 */
	public static TrafficData getTrafficData(Context context, Event event, Location location) throws Exception
	{
		String origin = getOrigin(context, location);
		TrafficData trafficData = gTrafficHandler.calculateTrafficInfo(origin, event.getLocation());
		return trafficData;
	}
	
	/**
	 * 
	 * @param context
	 * @param event
	 * @param location - if set to null then consider last known location from OS service
	 * @return
	 * @throws Exception
	 */
	public static long getTravelTimeToEvent(Context context, Event event, Location location) throws Exception
	{
		String origin = getOrigin(context, location);
		TrafficData trafficData = gTrafficHandler.calculateTrafficInfo(origin, event.getLocation());
		return trafficData.getDuration();
	}

	/**
	 * 
	 * @param context
	 * @param event
	 * @param location - if set to null then consider last known location from OS service
	 * @return
	 * @throws Exception
	 */
	public static float getDistanceToEventLocation(Context context, Event event, Location location) throws Exception
	{
		String origin = getOrigin(context, location);
		if (isCacheDetails(origin, event))
		{
			return lastDistanceToEventLocation;
		}
		
		TrafficData trafficData = gTrafficHandler.calculateTrafficInfo(origin, event.getLocation());
		return trafficData.getDistance();
	}

	private static boolean isCacheDetails(String origin, Event event) {
		if (origin.equals(lastOrigin) && event.equals(lastEvent)
				&& lastDistanceToEventLocation != 0)
		{
			return true;
		}
		
		return false;
			
	}

	private static String getOrigin(Context context, Location location) 
	{
		if (location == null)
		{
			Criteria criteria = new Criteria();
			criteria.setAccuracy(Criteria.ACCURACY_FINE);
			LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
			String provider = lm.getBestProvider(criteria, true);
			location = lm.getLastKnownLocation(provider);
		}
		return location.getLongitude() + "," + location.getLatitude();
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
