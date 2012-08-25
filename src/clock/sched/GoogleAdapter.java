package clock.sched;

import java.util.ArrayList;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.util.Log;
import clock.db.Event;
import clock.exceptions.CantGetLocationException;
import clock.outsources.GoogleTrafficHandler;
import clock.outsources.GoogleTrafficHandler.TrafficData;
import clock.outsources.GoogleWeatherHandler;
import clock.outsources.dependencies.WeatherModel;

public class GoogleAdapter {
	
	private static GoogleTrafficHandler gTrafficHandler = new GoogleTrafficHandler();
	private static GoogleWeatherHandler gWeatherHandler = new GoogleWeatherHandler();
	private static ConnectivityManager connectivityManager = null;
	
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

	private static String getOrigin(Context context, Location location) throws CantGetLocationException 
	{
		if (location == null)
		{
			//Retry policy - we set to 1 retry to get location, if not succeeded we throw CantGetLocationException 
			Criteria criteria = new Criteria();
			criteria.setAccuracy(Criteria.ACCURACY_FINE);
			LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
			String provider = lm.getBestProvider(criteria, true);
			location = lm.getLastKnownLocation(provider);
			if (location == null)
			{
				throw new CantGetLocationException();
			}
		}
		
		return location.getLatitude() + "," + location.getLongitude();
	}

	public static ArrayList<String> getSuggestions(String address)
	{
		ArrayList<String> res = new ArrayList<String>();
		try
		{
			res = gTrafficHandler.getSuggestion(address);
		}
		catch (Exception ex)
		{
			Log.e("Google", "Can't get address check");
		}
		
		return res;
	}

	public static boolean isInternetConnected(Context context) {
		
		// If it's the first invocation or the last invocation results with null active network info.
		//		then set new instance
		connectivityManager = (connectivityManager == null || connectivityManager.getActiveNetworkInfo() == null)? 
				(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE) : connectivityManager;
		
		return connectivityManager.getActiveNetworkInfo() != null && 
			   connectivityManager.getActiveNetworkInfo().isConnectedOrConnecting();

	}
	
	public static WeatherModel getWeatherModel(String location) throws Exception
	{
		return gWeatherHandler.processWeatherRequest(location);
	}

	public static TrafficData getDummyTrafficData() {
		// TODO For debug purposes.
		return gTrafficHandler.getDummyTrafficData();
	}

}
