package clock.sched;

import java.sql.Time;
import java.util.concurrent.TimeUnit;

import clock.db.DbAdapter;
import clock.db.Event;
import clock.outsources.GoogleTrafficHandler.TrafficData;
import clock.outsources.dependencies.WeatherModel;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class EventInfo extends Activity implements OnClickListener{
	
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
		setContentView(R.layout.event_info);
		googleMapsBtn = (Button)this.findViewById(R.id.googleMapsBtn);
		timesLeftTextView = (TextView)this.findViewById(R.id.timesLeftTextView);
		durationTextView = (TextView)this.findViewById(R.id.durationTextView);
		distanceTextView = (TextView)this.findViewById(R.id.distanceTextView);
		conditionTextView = (TextView)this.findViewById(R.id.conditionTextView);
		temperatureTextView = (TextView)this.findViewById(R.id.temperatureTextView);
		humidityTextView = (TextView)this.findViewById(R.id.humidityTextView);
		windTextView = (TextView)this.findViewById(R.id.windTextView);

	}
	
	@Override
   protected void onStart()
   {
	   	super.onStart();
	   	Bundle b = getIntent().getExtras();
		
		if (b.containsKey("event"))
		{
			Event event = Event.CreateFromString(b.getString("event"));
			setFields(event);
		}
		else
		{
			Log.e("Info","Can't get event details");
			setAllFieldsToNone();
		}
   }



	private void setFields(Event event) {
		
		// Calculate times left to display in hours and minutes
		long timesLeftToEvent = event.getTimesLeftToEvent();
		if (timesLeftToEvent > 0)
		{
			String timeLeftStr = String.format("%d hrs, %d min", 
				    TimeUnit.MILLISECONDS.toHours(timesLeftToEvent),
				    TimeUnit.MILLISECONDS.toMinutes(timesLeftToEvent) - 
				    TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timesLeftToEvent)));
			timesLeftTextView.setText(timeLeftStr);
		}
		else
		{
			timesLeftTextView.setText("Event has passed");
		}
		try
		{
			setTrafficInfo(event);
			setWeatherInfo(event);
		}
		catch (Exception e) {
			Log.e("EventInfo", "While trying to set fields: " + e.getMessage());
			setAllFieldsToNone();
		}
	}



	private void setAllFieldsToNone() {
		durationTextView.setText("Duration - No Info");
		distanceTextView.setText("Distance - No Info");
		conditionTextView.setText("Condition - No Info");
		temperatureTextView.setText("Temperature - No Info");
		humidityTextView.setText("Humidity - No Info");
		windTextView.setText("Wind - No Info");	
	}



	private void setWeatherInfo(Event event) throws Exception {
		WeatherModel weatherModel = GoogleAdapter.getWeatherModel(event.getLocation());
		conditionTextView.setText("Condition - " + weatherModel.getCondition());
		temperatureTextView.setText("Temperature - " + weatherModel.getTemperature());
		humidityTextView.setText("Humidity - " + weatherModel.getHumidity() + "%");
		windTextView.setText("Wind Direction - " + weatherModel.getWind());
	}



	private void setTrafficInfo(Event event) throws Exception {
		TrafficData trafficData = GoogleAdapter.getTrafficData(this, event, null);
		long duration = trafficData.getDuration();
		String durationStr;
		String distanceStr;
		if (duration >= 0)
		{
			durationStr = String.format("%d hrs, %d min", 
				    TimeUnit.SECONDS.toHours(duration),
				    TimeUnit.SECONDS.toMinutes(duration) - 
				    TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(duration)));
			float distance = trafficData.getDistance();
			distance = distance / 1000f;
			distanceStr = (distance + " Km");
		}
		else
		{
			durationStr = distanceStr = "NONE";
		}
		durationTextView.setText("Duration - " + durationStr);
		distanceTextView.setText("Distance - " + distanceStr);
		
	}



	@Override
	public void onClick(View v) {
		if (v == googleMapsBtn)
		{
			//TODO:
			finish();	
		}
		
	}

}
