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
	private static final long MIN_TIME_INTERVAL = 0;						// Ignore minimum time interval - only location matters
	private static final float MIN_DISTANCE_INTERVAL_PERCENTAGE = 0.03f;
	private static final float IN_DESTINATION_AREA = 10;					// User arrived when he's 10 meters away
	private static Context current_context;
	
	public static void setLocationListener(Context context, Event event, float distanceToEventLocation)
	{	
		// Need context for the 'onLocationChanged' - db adapter
		current_context = context;
		try
		{
			
			//TODO: this should be calculated as setAlarm in clock handler
			Criteria criteria = new Criteria();
			criteria.setAccuracy(Criteria.ACCURACY_FINE);
			LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE); 
			PendingIntent pendingIntent = getPendingIntent(context, event);
			float minDistanceInterval = calNextInterval(distanceToEventLocation);
			lm.requestLocationUpdates(MIN_TIME_INTERVAL, minDistanceInterval, criteria, pendingIntent);
		}
		catch (Exception ex)
		{
			Log.e("LOCATION", "Set listener has failed: " + ex.getMessage());
		}
	}
	
	private static float calNextInterval(float distanceToEventLocation) 
	{
		Log.d("LOCATION", "Current ditance to event location is: " + distanceToEventLocation/1000f + " Km");
		distanceToEventLocation = distanceToEventLocation * MIN_DISTANCE_INTERVAL_PERCENTAGE;
		Log.d("LOCATION", "Next update when user will move: " + distanceToEventLocation/1000f + " Km");
		return distanceToEventLocation;
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
		Log.d("LOCATION", "Listener has been canceled for event: " + event.toString());
	}

	@Override
	public void onLocationChanged(Location location) 
	{
		Log.d("LOCATION", "Inside Listener");
		DbAdapter db = new DbAdapter(current_context);
//		db.open();
		Event nextEvent = db.getNextEvent();
//		db.close();
		if (nextEvent != null)
		{
			try
			{
				EventProgressHandler.handleEventProgress(current_context, nextEvent, location);
				float distanceToEventLocation = GoogleAdapter.getDistanceToEventLocation(current_context, nextEvent, location);
				Log.d("LOCATION", "Location has been changed setting new listener, currently " + distanceToEventLocation/1000f + " Km away");
				setLocationListener(current_context, nextEvent, distanceToEventLocation);
			}
			catch (Exception ex)
			{
				Log.e("Location handler", "On location changed has failed: " + ex.getMessage());
			}
		}
	}

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
//			db.open();
			Event nextEvent = db.getNextEvent();
//			db.close();
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
					Log.e("LOCATION", "Reached unknown place while provider status changed");
					break;
				}
			}	
		}
		
	}

	/**
	 * This is a dummy location request specially when running on emulator
	 * if there's no request before calling getLastKnownLocation then it will return null location.
	 * @param context
	 */
	public static void setFirstLocationRequest(Context context) {
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE); 
		String provider = lm.getBestProvider(criteria, true);
		lm.requestLocationUpdates(provider, 0, 0, new LocationListener() {
			
			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProviderEnabled(String provider) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProviderDisabled(String provider) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onLocationChanged(Location location) {
				// TODO Auto-generated method stub
				
			}
		});		
	}

}
