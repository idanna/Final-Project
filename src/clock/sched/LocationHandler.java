package clock.sched;

import clock.db.Event;
import clock.outsources.GoogleTrafficHandler;
import clock.outsources.GoogleTrafficHandler.TrafficData;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;

public class LocationHandler implements LocationListener 
{
	private Context context;
	private GoogleTrafficHandler googleHandler = new GoogleTrafficHandler();
	private static final double MIN_TIME_PERCENTAGE = 0.03d;
	private static final float MIN_DISTANCE_MIN_TIME_PERCENTAGE = 0.03f;
	private static final long TIMES_UP = 5L;		//in seconds
	private static final float DISTANCE_UP = 100;	//in meters
	private long timesLeftToEvent;
	private float distanceLeftToEvent;
	private LocationManager lm;
	private Event nextEvent;
	
	public LocationHandler(Context context)
	{
		this.context = context;
	}

	public void setLocationListener(Event event)
	{
		// Remove listener before setting the next event with the new one
		setAndClearLocationManager();
		this.nextEvent = event;
		
		long minimumTimeInterval = 1000L;			//set first handler to 1 second minimum time
		float minimumDistanceInterval =  1.0f; 		//and 1 meter minimum distance
		
		//Setup first Location Update Request
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 
				minimumTimeInterval, minimumDistanceInterval, this);
	}


	@Override
	public void onLocationChanged(Location location) 
	{
				
		if (nextEvent == null || location == null) 
			Log.e("Location Handler", "On location changed: next event or location is null");
		else
		{
			try
			{
				calculateMinTimeAndDistanceIntervals(location);
				if (timesLeftToEvent < TIMES_UP && distanceLeftToEvent < DISTANCE_UP)
				{
					//TODO: user has reached destination
					Log.d("LocationHandler", "User has reached destination");
				}
				else if (timesLeftToEvent <= 0)
				{
					//TODO: time is up, no need to recall on location changed
					//			otherwise we should notice user about this late
					Log.d("LocationHandler", "Times up, user is late");
				}
				else
				{
					long minimumTimeInterval = (long)(Double.longBitsToDouble(timesLeftToEvent) * MIN_TIME_PERCENTAGE);
					float minimumDistanceInterval =  distanceLeftToEvent * MIN_DISTANCE_MIN_TIME_PERCENTAGE;
					
					//On each new update, clear the latest location update listener
					setAndClearLocationManager();
					
					//Setup next Location Update Request
					lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 
							minimumTimeInterval, minimumDistanceInterval, this);
				}
			}
			catch (Exception ex)
			{
				Log.e("LocationHandler", "Calculating time and distance intervals failed: " + ex.getMessage());
			}
						
		}
		
	}
	
	private void setAndClearLocationManager() 
	{
		if (lm == null)
		{
			//Initiate location manager
			lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		}
		else
		{
			//Remove on update listen
			lm.removeUpdates(this);
		}		
	}

	private void calculateMinTimeAndDistanceIntervals(Location location) throws Exception {
		String origin = location.getLongitude() + "," + location.getLatitude();
		String destination = nextEvent.getLocation();
		if (origin == null || destination == null)
		{
			throw new Exception("Error: LocationHandler, origin or destination is null");
		}
		
		TrafficData trafficData = googleHandler.calculateTrafficInfo(origin, destination);
		timesLeftToEvent = nextEvent == null ? 0 : ClockHandler.getTimesLeftToEvent(nextEvent);
		
		if (trafficData.getDuration() == -1l || trafficData.getDistance() == -1f)
		{
			throw new Exception("Error: LocationHandler, duration or distance not found");
		}
		
		//include travel time inside timesLeftToEvent
		timesLeftToEvent -= trafficData.getDuration();	
		
		distanceLeftToEvent = trafficData.getDistance();		
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		//		Message to user - Application need the gps to be enabled
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		if (nextEvent != null)
		{
			//When provider enable, we take the last known location and start onLocationChanged process
			Location location = lm.getLastKnownLocation(provider);
			onLocationChanged(location);
		}
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
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
