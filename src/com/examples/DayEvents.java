package com.examples;

import database.Event;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;

public class DayEvents extends Activity implements OnClickListener 
{	
	DatePicker date_picker; 
	TimePicker time_picker;
	
   /** Called when the activity is first created. */
   @Override
   	public void onCreate(Bundle savedInstanceState) 
   	{
	   	super.onCreate(savedInstanceState);
	   	setContentView(R.layout.day_events);
	   	
	   	date_picker = (DatePicker) this.findViewById(R.id.datePicker1);
	   	time_picker = (TimePicker) this.findViewById(R.id.timePicker1);
	   	
	   	((Button)this.findViewById(R.id.add_event_btn)).setOnClickListener(this);
   	}
   
   @Override
   protected void onStart()
   {
	   super.onStart();
	   // setting the content for the current date:
	   String title = "Events for the:" + getDate(); 

	   //TODO: update date_picker from the current event instance
	   //TODO: update time_picker from the current event instance
   }
   
   	private String getDate()
   	{
   		Bundle extras = getIntent().getExtras();
   		
   		//TODO: insert to log error if needed
   		String date = "Error with date";
   		if(extras !=null) 
   		{
   			date = extras.getString("date");
   		}
   		
   		return date;
   	}
   	
   	@Override
	public void onClick(View v)
   	{
   		Event event = new Event();   		
   		event.setDay(date_picker.getDayOfMonth());
   		event.setMonth(date_picker.getMonth());
   		event.setYear(date_picker.getYear());
   		event.setHour(time_picker.getCurrentHour());
   		event.setMin(time_picker.getCurrentHour());
   		
   		//TODO: set event location and details
   		
   		//TODO: push event to events DB
   	}
	   
}