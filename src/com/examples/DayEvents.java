package com.examples;


import java.sql.Date;
import java.util.EventListener;

import android.app.Activity;
import android.location.GpsStatus.Listener;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
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
   }
   
   	private String getDate()
   	{
   		Bundle extras = getIntent().getExtras();
   		String date = "Error with date"; // for debuging.
   		if(extras !=null) 
   		{
   			date = extras.getString("date");
   		}
   		
   		return date;
   	}
   	
   	@Override
	public void onClick(View v)
   	{
   		@SuppressWarnings("deprecation")
		Date date = new Date(date_picker.getYear(), date_picker.getMonth(), date_picker.getDayOfMonth());
   		
   	}
	   
}