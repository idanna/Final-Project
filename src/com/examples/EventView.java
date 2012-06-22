package com.examples;

import database.Event;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

public class EventView extends Activity implements OnClickListener 
{	
	protected DatePicker date_picker; 
	protected TimePicker time_picker;
	protected EditText location_text;
	protected EditText details_text;
	protected Event event;
	
   /** Called when the activity is first created. */
   @Override
   	public void onCreate(Bundle savedInstanceState) 
   	{
	   	super.onCreate(savedInstanceState);
	   	setContentView(R.layout.day_events);
	   	
	   	Bundle b = getIntent().getExtras();
	   	event = (Event) b.getSerializable("event");
	   	if (event == null)
	   	{
	   		//TODO: error to log
	   		event = Event.createNewInstance();
	   	}
	   	
	   	date_picker = (DatePicker) this.findViewById(R.id.datePicker);
	   	time_picker = (TimePicker) this.findViewById(R.id.timePicker);
	   	location_text = (EditText) this.findViewById(R.id.locationText);
	   	details_text = (EditText) this.findViewById(R.id.detailsText);
	   	
	   	((Button)this.findViewById(R.id.add_event_btn)).setOnClickListener(this);
   	}
   
   

   @Override
   protected void onStart()
   {
	   	super.onStart();
   		setPageFields();
   }
   
   private void setPageFields() 
   {
	   
	   date_picker.updateDate(event.getYear(), event.getMonth(), event.getDay());
	   time_picker.setCurrentHour(event.getHour());
	   time_picker.setCurrentMinute(event.getMin());
	   	
   }
   	
	@Override
	public void onClick(View v)
	{
		event.setDay(date_picker.getDayOfMonth());
		event.setMonth(date_picker.getMonth());
		event.setYear(date_picker.getYear());
		event.setHour(time_picker.getCurrentHour());
		event.setMin(time_picker.getCurrentHour());
		event.setLocation(location_text.toString());
		event.setDetails(details_text.toString());
		
		setResult(RESULT_OK, this.getIntent());
		//Close activity
		finish();
	}
	   
}