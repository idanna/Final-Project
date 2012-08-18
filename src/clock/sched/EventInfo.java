package clock.sched;

import java.sql.Time;
import java.util.concurrent.TimeUnit;

import clock.db.DbAdapter;
import clock.db.Event;
import clock.outsources.GoogleTrafficHandler.TrafficData;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class EventInfo extends Activity implements OnClickListener{
	
	private DbAdapter dbAdapter;
	private Button googleMapsBtn;
	private TextView timesLeftTextView;
	private TextView durationTextView;
	private TextView distanceTextView;
	private TextView conditionTextView;
	private TextView temperatureTextView;
	private TextView humidityTextView;
	private TextView windTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dbAdapter = new DbAdapter(this);
		
		googleMapsBtn = (Button)this.findViewById(R.id.googleMapsBtn);
		timesLeftTextView = (TextView)this.findViewById(R.id.timesLeftTextView);
		durationTextView = (TextView)this.findViewById(R.id.durationTextView);
		distanceTextView = (TextView)this.findViewById(R.id.distanceTextView);
		conditionTextView = (TextView)this.findViewById(R.id.conditionTextView);
		temperatureTextView = (TextView)this.findViewById(R.id.temperatureTextView);
		humidityTextView = (TextView)this.findViewById(R.id.humidityTextView);
		windTextView = (TextView)this.findViewById(R.id.windTextView);
		
		dbAdapter.open();
		Event nextEvent = dbAdapter.getNextEvent();
		dbAdapter.close();
		
		if (nextEvent != null)
		{
			setFields(nextEvent);
		}
		else
		{
			Log.e("EventInfo", "Next event is null when opening event information");
		}
	}



	private void setFields(Event event) {
		
		// Calculate times left to display in hours and minutes
		long timesLeftToEvent = event.getTimesLeftToEvent();
		String timeLeftStr = String.format("%d hrs, %d min", 
			    TimeUnit.MILLISECONDS.toHours(timesLeftToEvent),
			    TimeUnit.MILLISECONDS.toMinutes(timesLeftToEvent) - 
			    TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timesLeftToEvent)));
		timesLeftTextView.setText(timeLeftStr);
		
		try
		{
			TrafficData trafficData = GoogleAdapter.getTrafficData(this, event, null);
			long duration = trafficData.getDuration();
			String durationStr = String.format("%d hrs, %d min", 
				    TimeUnit.SECONDS.toHours(duration),
				    TimeUnit.SECONDS.toMinutes(duration) - 
				    TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(duration)));
			durationTextView.setText("Duration - " + durationStr);
			
			float distance = trafficData.getDistance();
			distance = distance / 1000f;
			distanceTextView.setText("Distance - " + distance + " km");
		}
		catch (Exception e) {
			Log.e("EventInfo", "While trying to set fields: " + e.getMessage());
		}
	}



	@Override
	public void onClick(View v) {
		if (v == googleMapsBtn)
		{
			//TODO: open google maps with driving mode to location
		}
		
	}

}
