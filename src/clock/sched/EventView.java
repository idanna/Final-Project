package clock.sched;

import clock.db.DbAdapter;
import clock.db.Event;
import clock.db.Event.eComparison;

import clock.sched.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.ToggleButton;

public class EventView extends Activity implements OnClickListener, OnKeyListener, OnCheckedChangeListener 
{	
	protected Button set_date_btn;
	protected Button set_time_btn;
	protected Button add_event_btn;
	protected DatePicker date_picker; 
	protected TimePicker time_picker;
	protected AutoCompleteTextView location_text;
	protected EditText details_text;
	protected Event event;
	protected ToggleButton alarm_on_off;	
	protected AlarmsManager alarmManager;
	protected DbAdapter dbAdapter;
	protected boolean alarmOnOffStatus;
	
   /** Called when the activity is first created. */
   @Override
   	public void onCreate(Bundle savedInstanceState) 
   	{
	   	super.onCreate(savedInstanceState);
	   	setContentView(R.layout.day_events);

	   	set_date_btn = (Button)this.findViewById(R.id.setDatePickerBtn);
	   	set_time_btn = (Button)this.findViewById(R.id.setTimePickerBtn);
	   	add_event_btn = (Button)this.findViewById(R.id.add_event_btn);
	   	alarm_on_off = (ToggleButton)this.findViewById(R.id.alarm_on_off);
	   	date_picker = (DatePicker) this.findViewById(R.id.datePicker);
	   	time_picker = (TimePicker) this.findViewById(R.id.timePicker);
	   	location_text = (AutoCompleteTextView) this.findViewById(R.id.locationText);
	   	location_text.setOnKeyListener(this);
	   	details_text = (EditText) this.findViewById(R.id.detailsText);
	   	dbAdapter= new DbAdapter(this);
	   	add_event_btn.setOnClickListener(this);
	   	set_date_btn.setOnClickListener(this);
	   	set_time_btn.setOnClickListener(this);
	   	alarm_on_off.setOnCheckedChangeListener(this);
	   	alarmOnOffStatus = false;
	   	alarmManager = new AlarmsManager(this, dbAdapter);
   	}
   
   @Override
   protected void onStart()
   {
	   	super.onStart();
	   	Bundle b = getIntent().getExtras();
	   	if(b.containsKey("selectedDate")) // new event.
	   	{
	   		setForNewEvent(b.get("selectedDate").toString());
	   	}
	   	else // edit event
	   	{
	   		String eventStr = b.get("editEvent").toString();
	   		event = Event.CreateFromString(eventStr);
	   	}
	   	
	   	Log.d("UI SET TO:", event.toString());
   		setPageFields();
   }
   
   private void setForNewEvent(String newEventInitDate) 
   {
  		event = Event.createNewInstance();
  		// there was a day selected at the calander.
	   	if (event != null)
	   	{
		   	String[] date = newEventInitDate.split("-");
			event.setDay(Integer.parseInt(date[0]));
			event.setMonth(Integer.parseInt(date[1]));
			event.setYear(Integer.parseInt(date[2])); 
	   	}	
   }

private void setPageFields() 
   {   
	   date_picker.updateDate(event.getYear(), event.getMonth() - 1, event.getDay());
	   time_picker.setCurrentHour(event.getHour());
	   time_picker.setCurrentMinute(event.getMin());
	   date_picker.setVisibility(View.INVISIBLE);
	   location_text.setText(event.getLocation());
	   details_text.setText(event.getDetails());
	   set_time_btn.setEnabled(false);
   }
   
   @Override
   public void onClick(View v)
   {
	   if (v == add_event_btn)
	   {
		   //TODO: check if it's a legal event - if this event overlapped by duration time with other events!!!
			
		   event.setPropFromViews(date_picker, time_picker, location_text, details_text, alarmOnOffStatus);
		   
		   // if the event is before or overlapped the current time, no need to trouble the alarm manager about that.
		   if(Event.compareToNow(event) == eComparison.AFTER)
		   {
			   alarmManager.newEvent(event);
		   }
		   
		   returnResult();	
	   }
	   else // date/time toggling.
	   {
		   boolean dateVisibility = v == set_date_btn;
		   setDateTimeBtns(!dateVisibility, dateVisibility);		   
	   }
	   
	}
   
   	private void setDateTimeBtns(boolean dateVisibility, boolean timeVisibility)
   	{
   			if (dateVisibility == true)
   			{
   			   	date_picker.setVisibility(View.VISIBLE);
   			   	time_picker.setVisibility(View.INVISIBLE);   				
   			}
   			else
   			{
   			   	date_picker.setVisibility(View.INVISIBLE);
   			   	time_picker.setVisibility(View.VISIBLE);   				
   			}
   			
		   	set_date_btn.setEnabled(dateVisibility);
		   	set_time_btn.setEnabled(timeVisibility);   		
	}
   	
	private void returnResult() 
	{
	   Intent i = this.getIntent();
	   i.putExtra("newEvent", event.encodeToString());
	   setResult(RESULT_OK, i);
	   //Close activity
	   finish();		
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

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (buttonView == alarm_on_off)
			alarmOnOffStatus = isChecked;
	}
	   
}