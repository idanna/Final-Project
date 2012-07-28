package clock.sched;

import java.util.Calendar;

import clock.db.DbAdapter;
import clock.db.Event;

import clock.sched.R;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

public class EventView extends Activity implements OnClickListener, OnKeyListener 
{	
	protected Button set_date_btn;
	protected Button set_time_btn;
	protected Button add_event_btn;
	protected DatePicker date_picker; 
	protected TimePicker time_picker;
	protected AutoCompleteTextView location_text;
	protected EditText details_text;
	protected Event event;
	
	private AlarmsManager alarmManager;
	private DbAdapter dbAdapter;
	
   /** Called when the activity is first created. */
   @Override
   	public void onCreate(Bundle savedInstanceState) 
   	{
	   	super.onCreate(savedInstanceState);
	   	setContentView(R.layout.day_events);

	   	set_date_btn = (Button)this.findViewById(R.id.setDatePickerBtn);
	   	set_time_btn = (Button)this.findViewById(R.id.setTimePickerBtn);
	   	add_event_btn = (Button)this.findViewById(R.id.add_event_btn);
	   	date_picker = (DatePicker) this.findViewById(R.id.datePicker);
	   	time_picker = (TimePicker) this.findViewById(R.id.timePicker);
	   	location_text = (AutoCompleteTextView) this.findViewById(R.id.locationText);
	   	location_text.setOnKeyListener(this);
	   	details_text = (EditText) this.findViewById(R.id.detailsText);
	   	dbAdapter= new DbAdapter(this);
	   	add_event_btn.setOnClickListener(this);
	   	set_date_btn.setOnClickListener(this);
	   	set_time_btn.setOnClickListener(this);
	   	alarmManager = new AlarmsManager(this, dbAdapter);
   	}
   
   @Override
   protected void onStart()
   {
	   	super.onStart();
	   	Bundle b = getIntent().getExtras();
	   	String selctedDate;
		selctedDate = (String) b.get("selectedDate").toString();
   		event = Event.createNewInstance();
   		// there was a day selected at the calander.
	   	if (event != null)
	   	{
		   	String[] date = selctedDate.split("-");
			event.setDay(Integer.parseInt(date[0]));
			event.setMonth(Integer.parseInt(date[1]));
			event.setYear(Integer.parseInt(date[2])); 
	   	}
	   	
	   	Log.d("UI SET TO:", event.toString());
   		setPageFields();
   }
   
   private void setPageFields() 
   {   
	   date_picker.updateDate(event.getYear(), event.getMonth() - 1, event.getDay());
	   time_picker.setCurrentHour(event.getHour());
	   time_picker.setCurrentMinute(event.getMin());
	   date_picker.setVisibility(View.INVISIBLE);
	   set_time_btn.setEnabled(false);
   }
   
   @Override
   public void onClick(View v)
   {
	   if (v == add_event_btn)
	   {
		   event.setPropFromViews(date_picker, time_picker, location_text, details_text);
		   // if the event is before the current time, no need to trouble the alarm manager about that.
		   if(!Event.earlyThenNow(event))
		   {
			   alarmManager.newEvent(event);
		   }
		   // saving event to the database
		   saveToDB();
		   LocationHandler.setLocationListener(this);
		   returnResult();	
	   }
	   if (v == set_date_btn)
	   {
		   date_picker.setVisibility(View.VISIBLE);
		   time_picker.setVisibility(View.INVISIBLE);
		   set_date_btn.setEnabled(false);
		   set_time_btn.setEnabled(true);
	   }
	   if (v == set_time_btn)
	   {
		   date_picker.setVisibility(View.INVISIBLE);
		   time_picker.setVisibility(View.VISIBLE);
		   set_date_btn.setEnabled(true);
		   set_time_btn.setEnabled(false);
	   }
	}

	private void returnResult() 
	{
	   Intent i = this.getIntent();
	   i.putExtra("newEvent", event.toString());
	   setResult(RESULT_OK, i);
	   //Close activity
	   finish();		
	}

	private void saveToDB() 
	{
	   dbAdapter.open();
	   dbAdapter.createEvent(event);
	   dbAdapter.close();		
	}

	@Override
	public boolean onKey(View arg0, int arg1, KeyEvent eventCode) 
	{
		if(eventCode.getAction() == KeyEvent.ACTION_UP)
		{
		   dbAdapter.open();
		   String[] sugg = dbAdapter.getStreetSugg(location_text.getText().toString());
		   dbAdapter.close();
		   ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, sugg);
		   location_text.setAdapter(adapter);		   
		}
		
		return false;
	}
	   
}