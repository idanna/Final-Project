package clock.views;

import java.util.Calendar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import clock.Parse.ParseHandler;
import clock.db.DbAdapter;
import clock.db.InvitedEvent;
import clock.exceptions.CantGetLocationException;
import clock.exceptions.EventsCollideException;
import clock.exceptions.GoogleWeatherException;
import clock.exceptions.IllegalAddressException;
import clock.exceptions.InternetDisconnectedException;
import clock.exceptions.OutOfTimeException;
import clock.outsources.GoogleTrafficHandler.TrafficData;
import clock.sched.AlarmsManager;
import clock.sched.GoogleAdapter;
/**
 * Screen which shows invited event info.
 * If user select one of the confirm/delte reutrn result OK and extra
 * key 'newEventId' which is the event id in the events table, 
 * if the user delete this key will be null.
 * @author Idan
 */
public class InvitedEventInfo extends Activity implements OnClickListener
{
	private class GoogleAsybJob extends AsyncTask<Context, Void, Boolean> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			distanceTextView.setText("Retrieving");
		}
		
		/** The system calls this to perform work in a worker thread and
	      * delivers it the parameters given to AsyncTask.execute() */
		@Override
	    protected Boolean doInBackground(Context... context) {
			Log.d("GoogleAsync", "backgournd starts");
			boolean allGood = true;
			try {
				trafficData = GoogleAdapter.getTrafficData(context[0], event, null);
			} 
			catch (Exception e) {
				allGood = false;
			}
			return allGood;
		}
	    
	    /** The system calls this to perform work in the UI thread and delivers
	      * the result from doInBackground() */
		@Override
	    protected void onPostExecute(Boolean allGood) {
			Log.d("GoogleAsync", "onPostExecute");
			if(allGood) {
				setDistanceField();
			}
			else {
				distanceTextView.setText("No Info");
			}
	    }
		
	}

	private class TrySaveInBackground extends AsyncTask<Context, Void, String> {

		@Override
		protected String doInBackground(Context... context)  {
			String exceptionError = null;
			   try 
			   {
					alarmsManager.newEventWithTrafficData(event, true, trafficData); // changes the event id according to the db event table id.
					dbAdapter.deleteInvitedEvent(invitedId);
					intent.putExtra("newEventId", String.valueOf(event.getId()));
			        ParseHandler.confirmEvent(event, userName);			   
			   } 
			   catch (IllegalAddressException iae)
			   {
				   exceptionError =  "Unknown address";
			   }
			   catch (InternetDisconnectedException ide)
			   {
				   exceptionError = "Internet disconnected";
			   }
			   catch (CantGetLocationException cgle)
			   {
				   exceptionError = "Can't get device location";
			   }
			   catch (OutOfTimeException e) {
				   exceptionError = "You Dont have time To get there";
			   }
			   catch (EventsCollideException e) {
				   exceptionError ="You Dont have time To get there!";
			   }
			   catch (GoogleWeatherException e) {
				   exceptionError = "Error with Google Weather";
			   }
			   catch (Exception e) 
			   {
				   e.printStackTrace();
				   exceptionError = "Unknown error";
			   }
			   finally
			   {
				   dialog.dismiss();
			   }
			   
			return exceptionError;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if(result != null) // errror accuted
			{
				Toast.makeText(context, result, Toast.LENGTH_LONG).show();
			}
			else
			{
				setResult(RESULT_OK, intent);
				finish();
			}
			
		}
		
	}
	
	private TextView titleTextView;
	private TextView whereTextView;
	private TextView distanceTextView;
	private TextView whenTextView;
	private TextView detailsTextView;
	private Button confirmBtn;
	private Button deleteBtn;
	private DbAdapter dbAdapter;
	private AlarmsManager alarmsManager;
	private InvitedEvent event;
	private String userName;
	private TrafficData trafficData;
	private Context context;
	private Intent intent;
	private long invitedId;
	private ProgressDialog dialog;
	private GoogleAsybJob distanceJob;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		dbAdapter = new DbAdapter(this);
		alarmsManager = new AlarmsManager(this, dbAdapter);
		setContentView(R.layout.invited_event_info);
		titleTextView = (TextView)this.findViewById(R.id.inviter_title);
		whereTextView = (TextView)this.findViewById(R.id.WhereTextView);
		distanceTextView = (TextView)this.findViewById(R.id.distanceTextView);
		whenTextView = (TextView)this.findViewById(R.id.whenTextView);
		detailsTextView = (TextView)this.findViewById(R.id.detailsTextView);
		confirmBtn = (Button)this.findViewById(R.id.invitationConfirmBtn);
		confirmBtn.setOnClickListener(this);
		deleteBtn = (Button)this.findViewById(R.id.deleteInvitationBtn);
		deleteBtn.setOnClickListener(this);
		userName = getIntent().getExtras().getString("user_name");
	}
	
	@Override
	protected void onStart()
   {
	   	super.onStart();
	   	Bundle b = getIntent().getExtras();
		trafficData = null;
	   	
		if (b.containsKey("event"))
		{
			event = InvitedEvent.createFromString((b.getString("event")));
			setFields();
		}
		else
		{
			Log.e("Info","Can't get event details");
		}
   }

	private void setFields() {
		//Set details from event
		distanceJob = new GoogleAsybJob();
		distanceJob.execute(this);
		Calendar c = event.toCalendar();
		titleTextView.setText(event.getSenderUserName() + " invites you to:");
		whereTextView.setText(event.getLocation());
		whenTextView.setText(c.getTime().toString());
		detailsTextView.setText(event.getDetails());
	}

	private void setDistanceField() 
	{
		float distance = trafficData.getDistance() / 1000;
		distanceTextView.setText(String.valueOf(distance) + " km from here.");
	}
	
	@Override
	public void onClick(View v) {
		distanceJob.cancel(true);
		intent = this.getIntent();
		invitedId = event.getId();
		if (v == confirmBtn)
		{
			dialog = ProgressDialog.show(InvitedEventInfo.this, "", 
					"Checking Data. Please wait...", true);
			dialog.setCancelable(false);
			dialog.setCanceledOnTouchOutside(false);
			dialog.show();
			TrySaveInBackground saveJob = new TrySaveInBackground();
			saveJob.execute(this);
		}
		if (v == deleteBtn)
		{
			dbAdapter.deleteInvitedEvent(invitedId);
			ParseHandler.ignoreEvent(event, userName);
			finish();
		}
				
	}

}
