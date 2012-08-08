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
	
	public static void setLocationListener(Context context, Event event, float distanceToEventLocation)
	{	
		// Need context for the 'onLocationChanged' - db adapter
		current_context = context;
		try
		{
//			float distanceToEventLocation = GoogleAdapter.getDistanceToEventLocation(context, event);
			Criteria criteria = new Criteria();
			criteria.setAccuracy(Criteria.ACCURACY_FINE);
			LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE); 
			PendingIntent pendingIntent = getPendingIntent(context, event);
			float minDistanceInterval = distanceToEventLocation * MIN_DISTANCE_INTERVAL_PERCENTAGE;
			lm.requestLocationUpdates(MIN_TIME_INTERVAL, minDistanceInterval, criteria, pendingIntent);
		}
		catch (Exception ex)
		{
			//TODO: 
		}
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
			try
			{
				float distanceToEventLocation = GoogleAdapter.getDistanceToEventLocation(current_context, nextEvent, location);
				
				// TODO: set DISTANCE_UP value
				if (distanceToEventLocation > DISTANCE_UP)
				{
						EventProgressHandler.handleEventProgress(nextEvent, distanceToEventLocation);
				}
			}
			catch (Exception ex)
			{
				//TODO:
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
