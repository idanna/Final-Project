package clock.sched;

import clock.db.DbAdapter;
import clock.db.Event;
import clock.outsources.GDataHandler;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class LocationHandler implements LocationListener 
{
	
	Context context;
	
	public static void setLocationListener(Context c)
	{
		long minimumTimeInterval = 1000L * 60 * 10;		//milliseconds
		float minimumDistanceInterval =  1.0f * 1000;	//meters
		LocationManager lm = (LocationManager) c.getSystemService(Context.LOCATION_SERVICE);
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 
				minimumTimeInterval, minimumDistanceInterval, new LocationHandler(c));
	}
	
	public LocationHandler(Context context)
	{
		this.context = context;
	}
	
//	private String getLocation()
//	{
//		String formattedLocation = "";
//
//		LocationManager lm = (LocationManager) this.context.getSystemService(Context.LOCATION_SERVICE);
//		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L,1.0f, this);
//		
//		try 
//		{
//			//Start GPS service if needed
//			if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
//			{
//				//TODO: this is wrong because it force the user to start GPS, instead, we should ask the user to do so.
//				context.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
//			}
//			Location l = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//			deviceLocation.longtitude = l.getLongitude();
//			deviceLocation.latitude = l.getLatitude();
//			formattedLocation = deviceLocation.getGoogleFormattedLocation();
//		}
//		catch(Exception ex)
//		{
//			Log.d("Scheduler", "Error while trying to get last known location");
//		}
//	
//		return formattedLocation;
//	}

	@Override
	public void onLocationChanged(Location location) 
	{
		String origin = null;
		String destination = null;
		long duration = 0;
		
		DbAdapter db = new DbAdapter(this.context);
		db.open();
		Event nextEvent = db.getNextEvent();
		db.close();
				
		origin = location.getLongitude() + "," + location.getLatitude();
		destination = nextEvent == null? null : nextEvent.getLocation();
		
		if (origin == null || destination == null) 
			Log.e("Location Handler", "On location changed: origin/destination is null");
		else
		{
			GDataHandler googleHandler = new GDataHandler();
			duration = googleHandler.getDuration(origin, destination);
		}
			
		long timesLeftToEvent = nextEvent == null ? 0 : ClockHandler.getTimesLeftToEvent(nextEvent);
		
		if (duration > timesLeftToEvent && timesLeftToEvent > 0)
		{
			//ARE YOU FUCKING CRAZY - you're gonna be so late, your boss will fired you
		}
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

}
