package clock.sched;


import java.util.concurrent.TimeUnit;

import clock.Parse.ParseHandler;
import clock.db.DbAdapter;
import clock.db.Event;
import clock.db.InvitedEvent;
import clock.outsources.GoogleTrafficHandler.TrafficData;
import clock.outsources.dependencies.WeatherModel;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class EventInfo extends Activity implements OnClickListener{
	
	private Event event;
	private TextView detailsTextView;
	private TextView timesLeftTextView;
	private TextView durationTextView;
	private TextView distanceTextView;
	private TextView conditionTextView;
	private TextView temperatureTextView;
	private TextView humidityTextView;
	private TextView windTextView;
	private ImageView confListBtn;
	private String[] eventConfirmersList;
	private static final String NO_INFO = "No Info";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.event_info);
		detailsTextView = (TextView)this.findViewById(R.id.detailsTextView);
		timesLeftTextView = (TextView)this.findViewById(R.id.timesLeftTextView);
		durationTextView = (TextView)this.findViewById(R.id.durationTextView);
		distanceTextView = (TextView)this.findViewById(R.id.distanceTextView);
		conditionTextView = (TextView)this.findViewById(R.id.conditionTextView);
		temperatureTextView = (TextView)this.findViewById(R.id.temperatureTextView);
		humidityTextView = (TextView)this.findViewById(R.id.humidityTextView);
		windTextView = (TextView)this.findViewById(R.id.windTextView);
		confListBtn = (ImageView)this.findViewById(R.id.confirmers_list_btn);
		confListBtn.setOnClickListener(this);
	}
	
	@Override
   protected void onStart()
   {
	   	super.onStart();
	   	Bundle b = getIntent().getExtras();
		
		if (b.containsKey("event"))
		{
			event = Event.CreateFromString(b.getString("event"));
			setFields(event);
		}
		else
		{
			Log.e("Info","Can't get event details");
			setTrafficFieldsToNone();
		}
   }
	
	@Override
	public void onClick(View v) {
		if(v == confListBtn) {
			if(eventConfirmersList == null) { // lazy loading
				DbAdapter db = new DbAdapter(this);
				eventConfirmersList = db.getEventConfirmersList(event.getId());
			}
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Friends who confirmed: ");
			builder.setItems(eventConfirmersList, new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog, int item) {
			       
			    }
			});
			
			AlertDialog alert = builder.create();
			alert.show();
		}
		
	}
	
	private void setFields(Event event) {
		//Set details from event
		detailsTextView.setText(event.getDetails());
		
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
		}
		catch (Exception e) {
			Log.e("EventInfo", "While trying to set traffic fields: " + e.getMessage());
			setTrafficFieldsToNone();
		}
		
		try
		{
			setWeatherInfo(event);
		}
		catch (Exception e)
		{
			Log.e("EventInfo", "While trying to set weather fields: " + e.getMessage());
			setWeatherFieldsToNone();
		}
	}

	private void setTrafficFieldsToNone() {
		durationTextView.setText("Duration - " + NO_INFO);
		distanceTextView.setText("Distance - " + NO_INFO);
	}
	
	private void setWeatherFieldsToNone() {
		conditionTextView.setText("Condition - " + NO_INFO);
		temperatureTextView.setText("Temperature - " + NO_INFO);
		humidityTextView.setText("Humidity - " + NO_INFO);
		windTextView.setText("Wind - " + NO_INFO);	
	}

	private void setWeatherInfo(Event event) throws Exception {
		WeatherModel weatherModel = GoogleAdapter.getWeatherModel(event.getLocation());
		conditionTextView.setText("Condition - " 
				+ (weatherModel.getCondition() == null?  NO_INFO : weatherModel.getCondition()));
		
		temperatureTextView.setText("Temperature - " 
				+ (weatherModel.getTemperature() == null?  NO_INFO : weatherModel.getTemperature()));
		
		humidityTextView.setText("Humidity - " 
				+ (weatherModel.getHumidity()  == null?  NO_INFO : weatherModel.getHumidity() + "%"));
		
		windTextView.setText("Wind Direction - " 
				+ (weatherModel.getWind() == null?  NO_INFO : weatherModel.getWind()));
	}

	private void setTrafficInfo(Event event) throws Exception {
		TrafficData trafficData = GoogleAdapter.getTrafficData(this, event, null);
		long duration = trafficData.getDuration();
		String durationStr;
		String distanceStr;
		if (duration >= 0)
		{
			durationStr = String.format("%d hrs, %d min", 
				    TimeUnit.MILLISECONDS.toHours(duration),
				    TimeUnit.MILLISECONDS.toMinutes(duration) - 
				    TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(duration)));
			float distance = trafficData.getDistance();
			distance = distance / 1000f;
			distanceStr = (distance + " Km");
		}
		else
		{
			durationStr = distanceStr = NO_INFO;
		}
		durationTextView.setText("Duration - " + durationStr);
		distanceTextView.setText("Distance - " + distanceStr);
		
	}

}
