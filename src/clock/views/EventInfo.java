package clock.views;


import java.util.concurrent.TimeUnit;
import clock.db.DbAdapter;
import clock.db.Event;
import clock.outsources.GoogleTrafficHandler.TrafficData;
import clock.outsources.dependencies.WeatherModel;
import clock.sched.GoogleAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class EventInfo extends Activity implements OnClickListener{
	
	public WeatherModel weatherModel;
	public TrafficData trafficData;

	private class GoogleAsybJob extends AsyncTask<Context, Void, Boolean> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			setFieldsAsRetrieving();
		}

		/** The system calls this to perform work in a worker thread and
	      * delivers it the parameters given to AsyncTask.execute() */
		@Override
	    protected Boolean doInBackground(Context... context) {
			Log.d("GoogleAsync", "backgournd starts");
			boolean allGood = true;
			try
			{
				trafficData = GoogleAdapter.getTrafficData(context[0], event, null);
			}
			catch (Exception e) {
				Log.e("EventInfo", "While trying to set traffic fields: " + e.getMessage());
				allGood = false;
			}
			try
			{
				weatherModel = GoogleAdapter.getWeatherModel(event.getLocation());
			}
			catch (Exception e)
			{
				Log.e("EventInfo", "While trying to set weather fields: " + e.getMessage());
				allGood = false;
			}
			Log.d("GoogleAsync", "backgournd ends");
			
			return allGood;
		}
	    
	    /** The system calls this to perform work in the UI thread and delivers
	      * the result from doInBackground() */
		@Override
	    protected void onPostExecute(Boolean allGood) {
			Log.d("GoogleAsync", "onPostExecute");
			if(allGood)
			{
				setWeatherInfo();
				setTrafficInfo();
				
			}
			else
			{
				setTrafficFieldsTo(NO_INFO);
				setWeatherFieldsTo(NO_INFO);
			}
	    }
		
	}
	
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
	private GoogleAsybJob asyncFieldsUpdate;
	private static final String NO_INFO = "No Info";
	private static final String RETRIEVING = "retreiving";

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
			Log.d("GoogleAsync", "AFTER SET FIELDS");
		}
		else
		{
			Log.e("Info","Can't get event details");
			setTrafficFieldsTo(NO_INFO);
			setWeatherFieldsTo(NO_INFO);
		}
   }
	
	@Override
	protected void onStop() {
		super.onStop();
		asyncFieldsUpdate.cancel(true);
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
			String timeLeftStr = String.format("%d day, %d hrs, %d min",
					TimeUnit.MILLISECONDS.toDays(timesLeftToEvent),
				    (TimeUnit.MILLISECONDS.toHours(timesLeftToEvent) % 24),
				    (TimeUnit.MILLISECONDS.toMinutes(timesLeftToEvent) % 60));
			timesLeftTextView.setText(timeLeftStr);
		}
		else
		{
			timesLeftTextView.setText("Event has passed");
		}
		
		asyncFieldsUpdate = new GoogleAsybJob();
		asyncFieldsUpdate.execute(this);
	}

	private void setTrafficFieldsTo(String value) {
		durationTextView.setText("Duration - " + value);
		distanceTextView.setText("Distance - " + value);
	}
	
	private void setWeatherFieldsTo(String value) {
		conditionTextView.setText("Condition - " + value);
		temperatureTextView.setText("Temperature - " + value);
		humidityTextView.setText("Humidity - " + value);
		windTextView.setText("Wind - " + value);	
	}

	public void setFieldsAsRetrieving()
	{
		setTrafficFieldsTo(RETRIEVING);
		setWeatherFieldsTo(RETRIEVING);
	}
	
	private void setWeatherInfo() {
		conditionTextView.setText("Condition - " 
				+ (weatherModel.getCondition() == null?  NO_INFO : weatherModel.getCondition()));
		
		temperatureTextView.setText("Temperature - " 
				+ (weatherModel.getTemperature() == null?  NO_INFO : weatherModel.getTemperature()));
		
		humidityTextView.setText("Humidity - " 
				+ (weatherModel.getHumidity()  == null?  NO_INFO : weatherModel.getHumidity() + "%"));
		
		windTextView.setText("Wind Direction - " 
				+ (weatherModel.getWind() == null?  NO_INFO : weatherModel.getWind()));
	}

	private void setTrafficInfo() {
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
