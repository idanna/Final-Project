package clock.sched;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.Intent;
import android.drm.DrmStore.Action;
import android.graphics.AvoidXfermode;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import clock.Parse.ParseHandler;
import clock.db.DbAdapter;
import clock.db.Event;
import clock.db.InvitedEvent;
import clock.exceptions.CantGetLocationException;
import clock.exceptions.EventsCollideException;
import clock.exceptions.GoogleWeatherException;
import clock.exceptions.IllegalAddressException;
import clock.exceptions.InternetDisconnectedException;
import clock.exceptions.OutOfTimeException;

/**
 * Screen which shows invited event info.
 * @author Idan
 *
 */
public class InvitedEventInfo extends Activity implements OnClickListener
{

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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
	}
	
	@Override
   protected void onStart()
   {
	   	super.onStart();
	   	Bundle b = getIntent().getExtras();
		
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
		Calendar c = event.toCalendar();
		titleTextView.setText(event.getChannel() + " invites you to:");
		whereTextView.setText(event.getLocation());
		whenTextView.setText(c.getTime().toString());
		detailsTextView.setText(event.getDetails());
	}

	@Override
	public void onClick(View v) {
		if (v == confirmBtn)
		{

			try {
				alarmsManager.newEvent(event, true);
				dbAdapter.deleteInvitedEvent(event);
		        ParseHandler.confirmEvent(event, event.getChannel());
			}
			   catch (IllegalAddressException iae)
			   {
				   Toast.makeText(this, "Unknown address",Toast.LENGTH_LONG).show();
			   }
			   catch (InternetDisconnectedException ide)
			   {
				   Toast.makeText(this, "Internet disconnected",Toast.LENGTH_LONG).show();
			   }
			   catch (CantGetLocationException cgle)
			   {
				   Toast.makeText(this, "Can't get device location",Toast.LENGTH_LONG).show();
			   }
			   catch (OutOfTimeException e) {
				   Toast.makeText(this, "You Dont have time To get there!",Toast.LENGTH_LONG).show();
			   }
			   catch (EventsCollideException e) {
				   Toast.makeText(this, "You Dont have time To get there!",Toast.LENGTH_LONG).show();
			   }
			   catch (GoogleWeatherException e) {
				   Toast.makeText(this, "Error with Google Weather",Toast.LENGTH_LONG).show();
			   }
			   catch (Exception e) 
			   {
				   e.printStackTrace();
				   Toast.makeText(this, "Unknown error",Toast.LENGTH_LONG).show();
			   }	
		
		}
		if (v == deleteBtn)
		{
			dbAdapter.deleteInvitedEvent(event);			
		}

	   finish();		
	}


}