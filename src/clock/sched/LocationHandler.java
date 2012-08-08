package clock.sched;

import clock.db.DbAdapter;
import clock.db.Event;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;

public class LocationHandler implements LocationListener 
{
	private static final long MIN_TIME_INTERVAL = 0;		// Ignore minimum time interval - only location matters
	private static final float MIN_DISTANCE_INTERVAL_PERCENTAGE = 0.03f;
	private static final float DISTANCE_UP = 0f;
	private static Context current_context;
	
	public static void setLocationListener(Context context, Event event)
	{	
		// Need context for the 'onLocationChanged' - db adapter
		current_context = context;
		float distanceToEventLocation = GoogleAdapter.getDistanceToEventLocation(event);
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE); 
		PendingIntent pendingIntent = getPendingIntent(context, event);
		float minDistanceInterval = distanceToEventLocation * MIN_DISTANCE_INTERVAL_PERCENTAGE;
		lm.requestLocationUpdates(MIN_TIME_INTERVAL, minDistanceInterval, criteria, pendingIntent);
	}
	
	private static PendingIntent getPendingIntent(Context context, Event event)
	{
		Intent intent = new Intent(context, LocationHandler.class);
		intent.putExtra("eventStr", event.encodeToString());
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
		return pendingIntent;
	}
	
	public static void cancelLocationListener(Context context, Event event)
	{
		PendingIntent pendingIntent = getPendingIntent(context, event);
		LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE); 
		lm.removeUpdates(pendingIntent);
	}

	@Override
	public void onLocationChanged(Location location) 
	{
		DbAdapter db = new DbAdapter(current_context);
		db.open();
		Event nextEvent = db.getNextEvent();
		db.close();
		if (nextEvent != null)
		{
			float distanceToEventLocation = GoogleAdapter.getDistanceToEventLocation(nextEvent);
			
			// TODO: set DISTANCE_UP value
			if (distanceToEventLocation > DISTANCE_UP)
			{
				EventProgressHandler.handleEventProgress(nextEvent, distanceToEventLocation);
			}
			
//			if (timesLeftToEvent < TIMES_UP && distanceLeftToEvent < DISTANCE_UP)
//			{
//				//TODO: user has reached destination
//				Log.d("LocationHandler", "User has reached destination");
//			}
//			else if (timesLeftToEvent <= 0)
//			{
//				//TODO: time is up, no need to recall on location changed
//				//			otherwise we should notice user about this late
//				Log.d("LocationHandler", "Times up, user is late");
//			}
//			else
//			{
//				long minimumTimeInterval = (long)(Double.longBitsToDouble(timesLeftToEvent) * MIN_TIME_PERCENTAGE);
//				float minimumDistanceInterval =  distanceLeftToEvent * MIN_DISTANCE_MIN_TIME_PERCENTAGE;
//				
//				//On each new update, clear the latest location update listener
//				setAndClearLocationManager();
//				
//				//Setup next Location Update Request
//				lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 
//						minimumTimeInterval, minimumDistanceInterval, this);
//			}
		}
//		catch (Exception ex)
//		{
//			Log.e("LocationHandler", "Calculating time and distance intervals failed: " + ex.getMessage());
//		}
						
		
	}
	
//	/**
//	 * Will return the Min time intercal from the current locaion (last best known) and the destination.
//	 * @param destination the place heading for.
//	 * @throws Exception 
//	 */
//	public TrafficData getMinTimeInterval(String destination) throws Exception
//	{
//		
//		String provider = lm.getBestProvider(criteria, true);
//		Location location = lm.getLastKnownLocation(provider);
//		String origin = location.getLongitude() + "," + location.getLatitude();
//		return googleHandler.calculateTrafficInfo(origin, destination);
//	}
//	
//	private void calculateMinTimeAndDistanceIntervals(Location location) throws Exception 
//	{
//		String origin = location.getLongitude() + "," + location.getLatitude();
//		String destination = nextEvent.getLocation();
//		if (origin == null || destination == null)
//		{
//			throw new Exception("Error: LocationHandler, origin or destination is null");
//		}
//		
//		TrafficData trafficData = googleHandler.calculateTrafficInfo(origin, destination);
//		timesLeftToEvent = nextEvent == null ? 0 : nextEvent.getTimesLeftToEvent();
//		
//		if (trafficData.getDuration() == -1l || trafficData.getDistance() == -1f)
//		{
//			throw new Exception("Error: LocationHandler, duration or distance not found");
//		}
//		
//		//include travel time inside timesLeftToEvent
//		timesLeftToEvent -= trafficData.getDuration();	
//		
//		distanceLeftToEvent = trafficData.getDistance();		
//	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO - Notify EventProgressHandler
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO - Notify EventProgressHandler
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		if (current_context != null)
		{
			DbAdapter db = new DbAdapter(current_context);
			db.open();
			Event nextEvent = db.getNextEvent();
			db.close();
			if (nextEvent != null)
			{
				switch (status) {
				case LocationProvider.OUT_OF_SERVICE:
				case LocationProvider.TEMPORARILY_UNAVAILABLE:
					//TODO: do we need to inform the user?
					
					break;		
				case LocationProvider.AVAILABLE:
					onProviderEnabled(provider);
					break;
				default:
					Log.d("LocationHandler", "Reached unknown place while provider status changed");
					break;
				}
			}	
		}
		
	}

}
