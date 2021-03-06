package clock.sched;

import java.util.ArrayList;
import java.util.Calendar;
import clock.db.DbAdapter;
import clock.db.Event;
import clock.exceptions.CantGetLocationException;
import clock.exceptions.EventsCollideException;
import clock.exceptions.GoogleWeatherException;
import clock.exceptions.IllegalAddressException;
import clock.exceptions.InternetDisconnectedException;
import clock.exceptions.OutOfTimeException;

import clock.views.R;
import clock.views.R.drawable;
import clock.views.R.id;
import clock.views.R.layout;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

public class EventView extends Activity implements OnClickListener, OnKeyListener, OnCheckedChangeListener, OnItemClickListener
{	
	private class AutoCompleteHelper extends AsyncTask<String, Void, ArrayList<String>> {
			
	    @Override
		protected void onPreExecute() {
			super.onPreExecute();
			addressProgressBar.setVisibility(View.VISIBLE);
		}

		/** The system calls this to perform work in a worker thread and
	      * delivers it the parameters given to AsyncTask.execute() */
		@Override
	    protected ArrayList<String> doInBackground(String... address) {
			Log.d("AC-Helper", "Timer has start running");
			String text = location_text.getText().toString();
			return GoogleAdapter.getSuggestions(text);
		}
	    
	    /** The system calls this to perform work in the UI thread and delivers
	      * the result from doInBackground() */
		@Override
	    protected void onPostExecute(ArrayList<String> sugg) {
			Log.d("AC-Helper", "onPostExecute");
			addressProgressBar.setVisibility(View.INVISIBLE);
			if(!isCancelled() && sugg != null && !sugg.isEmpty())
			{
		    	ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, sugg);
		    	location_text.setAdapter(adapter);
		    	location_text.showDropDown();				
			}
	    }
	    
	}
	
	private class TrySaveInBackground extends AsyncTask<Context, Void, String> {

		@Override
		protected String doInBackground(Context... context)  {
			String exceptionError = null;
			   try 
			   {
				   if(isInEditMode) {
					   alarmManager.updateFinish(event, isItemSelectedFromList);
				   }
				   else {
					   alarmManager.newEvent(event, isItemSelectedFromList);				   
				   }
				   
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
			   returnResult();
			}
			
		}
		
	}
	
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
	protected ProgressBar addressProgressBar;
	
	protected AsyncTask<String, Void, ArrayList<String>> autoCompleteHelper;
	protected Context context = this;
	protected boolean isItemSelectedFromList;
	protected boolean isInEditMode;
	private ProgressDialog dialog;
	
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
	   	time_picker.setIs24HourView(true);
	   	location_text = (AutoCompleteTextView) this.findViewById(R.id.locationText);
	   	location_text.setDropDownBackgroundResource(R.drawable.bg);
	   	location_text.setOnKeyListener(this);
	   	location_text.setFocusable(true);
	   	location_text.setOnItemClickListener(this);
	   	details_text = (EditText) this.findViewById(R.id.detailsText);
	   	dbAdapter= new DbAdapter(this);
	   	add_event_btn.setOnClickListener(this);
	   	set_date_btn.setOnClickListener(this);
	   	set_time_btn.setOnClickListener(this);
	   	alarm_on_off.setOnCheckedChangeListener(this);
	   	alarmOnOffStatus = false;
	   	alarmManager = new AlarmsManager(this, dbAdapter);
	   	addressProgressBar = (ProgressBar) this.findViewById(R.id.addressProgressBar);
	   	addressProgressBar.setVisibility(View.INVISIBLE);
	   	autoCompleteHelper = new AutoCompleteHelper();	 
	   	isItemSelectedFromList = false;
	   	isInEditMode = false;
   	}
   
   @Override
   protected void onStart()
   {
	   	super.onStart();
	   	isInEditMode = false;
	   	Bundle b = getIntent().getExtras();
	   	if(b.containsKey("selectedDate")) // new event.
	   	{
	   		setForNewEvent(b.get("selectedDate").toString());
	   	}
	   	else // edit event
	   	{
	   		String eventStr = b.get("editEvent").toString();
	   		isInEditMode = true;
	   		event = Event.CreateFromString(eventStr);
	   	}
	   	   	
   		setPageFields();
   }
   
   private void setForNewEvent(String newEventInitDate) 
   {
  		event = Event.createNewInstance();
  		// there was a day selected at the calander.
	   	if (event != null)
	   	{
	   		//Setting date in new event
		   	String[] date = newEventInitDate.split("-");
			event.setDay(Integer.parseInt(date[0]));
			event.setMonth(Integer.parseInt(date[1]));
			event.setYear(Integer.parseInt(date[2])); 
			
			//Setting time in new event
			Calendar cal = Calendar.getInstance();
			event.setHour(cal.get(Calendar.HOUR_OF_DAY));
			event.setMin(cal.get(Calendar.MINUTE));
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
	   alarm_on_off.setChecked(event.getWithAlarmStatus());
   }
   
   @Override
   public void onClick(View v)
   {
	   if (v == add_event_btn)
	   {
		   autoCompleteHelper.cancel(true);
		   addressProgressBar.setVisibility(View.INVISIBLE);
		   dialog = ProgressDialog.show(EventView.this, "", 
                   "Checking Data. Please wait...", true);
		   dialog.setCancelable(false);
		   dialog.setCanceledOnTouchOutside(false);
		   dialog.show();
		   event.setPropFromViews(date_picker, time_picker, location_text, details_text, alarmOnOffStatus);
		   TrySaveInBackground saveJob = new TrySaveInBackground();
		   saveJob.execute(this);
	   }
	   if(v == set_date_btn || v == set_time_btn)
	   {
		   boolean dateVisibility = v == set_date_btn;
		   setDateTimeBtns(dateVisibility, !dateVisibility);		   
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
   			
		   	set_date_btn.setEnabled(!dateVisibility);
		   	set_time_btn.setEnabled(!timeVisibility);   		
	}
   	
	private void returnResult() 
	{
	   Intent i = this.getIntent();
	   String extraKey = isInEditMode == true ? "updatedEvent" : "newEvent";
	   i.putExtra(extraKey, event.encodeToString());		   
	   setResult(RESULT_OK, i);
	   //Close activity
	   finish();		
	}
	
	@Override
	public boolean onKey(View v, int keyCode, KeyEvent eventCode) 
	{
		isItemSelectedFromList = false;
		String text = location_text.getText().toString();
		location_text.dismissDropDown();
		if(text.length() > 2 && eventCode.getAction() == KeyEvent.ACTION_UP)
		{
			autoCompleteHelper.cancel(true);
			this.autoCompleteHelper = new AutoCompleteHelper();
		    this.autoCompleteHelper.execute(text);
		}
		
		return false;
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (buttonView == alarm_on_off)
			alarmOnOffStatus = isChecked;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		isItemSelectedFromList = true;
		
	}
	   
}