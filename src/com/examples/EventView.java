package com.examples;

import java.nio.channels.SelectableChannel;
import java.util.Date;

import database.DbAdapter;
import database.Event;

import android.R.integer;
import android.app.Activity;
import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
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
	
	private DbAdapter dbAdapter;
	
   /** Called when the activity is first created. */
   @Override
   	public void onCreate(Bundle savedInstanceState) 
   	{
	   	super.onCreate(savedInstanceState);
	   	setContentView(R.layout.day_events);

	   	date_picker = (DatePicker) this.findViewById(R.id.datePicker);
	   	time_picker = (TimePicker) this.findViewById(R.id.timePicker);
	   	location_text = (EditText) this.findViewById(R.id.locationText);
	   	details_text = (EditText) this.findViewById(R.id.detailsText);
	   	dbAdapter = new DbAdapter(this);
	   	((Button)this.findViewById(R.id.add_event_btn)).setOnClickListener(this);
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
	   //TODO: pretty ugly all those setters.
	   event.setDay(date_picker.getDayOfMonth());
	   event.setMonth(date_picker.getMonth());
	   event.setYear(date_picker.getYear());
	   event.setHour(time_picker.getCurrentHour());
	   event.setMin(time_picker.getCurrentHour());
	   event.setLocation(location_text.getText().toString());
	   event.setDetails(details_text.getText().toString());
	   // saving event to the database
	   dbAdapter.open();
	   dbAdapter.createEvent(event);
	   dbAdapter.close();
	   
	   Intent i = this.getIntent();
	   i.putExtra("newEvent", event.toString());
	   setResult(RESULT_OK, this.getIntent());
	   //Close activity
		finish();
	}
	   
}