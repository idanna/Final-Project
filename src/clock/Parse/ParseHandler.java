package clock.Parse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import clock.db.Connection;
import clock.db.DbAdapter;
import clock.db.Event;
import clock.db.InvitedEvent;

import com.parse.ParseObject;
import com.parse.signpost.http.HttpResponse;

public class ParseHandler extends BroadcastReceiver {
	
	public final static String CONFIRM = "confirm";
	public final static String INVITE = "invitation";
	
	private static final String TAG = "ParseHandler";

	  @Override
	  public void onReceive(Context context, Intent intent) 
	  {
		  try {			  				
				String action = intent.getAction();
				String channel = intent.getExtras().getString("com.parse.Channel");
				JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
				String data = json.get("data").toString();
				String[] dataParsed = data.split("@S@");
				String pushType = dataParsed[0];
				String sender = dataParsed[1];
				if(pushType.equals(CONFIRM))
				{
					int confirmEventId = Integer.parseInt(dataParsed[2]);
					//TODO: for now only notifing
				} else if (pushType.equals(INVITE))
				{
					Event invitedEvent = Event.CreateFromString(dataParsed[2]);
					DbAdapter db = new DbAdapter(context);
					db.insertInvitedEvent(invitedEvent, sender);
					Log.d(TAG, "received action " + action + " on channel " + channel + " with extras:");
					Log.d(TAG, "Event is:" + invitedEvent.toString());									
					
				}
		  } catch (JSONException e) {
					Log.d(TAG, "ParseOnRecieve: " + e.getMessage());	
				}
	  }

	public static void sendMsg(Event invitedEvent, String userName, String channel) {
	    // Create a new HttpClient and Post Header
	    HttpClient httpclient = new DefaultHttpClient();
	    HttpPost httppost = new HttpPost("http://guarded-hamlet-8595.herokuapp.com/main/push");

	    try {
	        // Add your data
	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	        nameValuePairs.add(new BasicNameValuePair("msg", userName + " invites you !"));
	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

	        nameValuePairs.add(new BasicNameValuePair("channel", channel));
	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

	        nameValuePairs.add(new BasicNameValuePair("event", INVITE + "@S@" + userName + "@S@" + invitedEvent.encodeToString()));
	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));	        
	        	        
	        // Execute HTTP Post Request
	        //TODO: fix response headers.
	        org.apache.http.HttpResponse response = httpclient.execute(httppost);
	        for (int i = 0; i < response.getAllHeaders().length; i++) {
	        	Log.d("HEADERS", response.getAllHeaders()[i].toString());
			}
	        
	    } catch (ClientProtocolException e) {
	        // TODO Auto-generated catch block
	    } catch (IOException e) {
	        // TODO Auto-generated catch block
	    }
	}

	public static void confirmEvent(InvitedEvent invitedEvent, String userName) {
	    HttpClient httpclient = new DefaultHttpClient();
	    HttpPost httppost = new HttpPost("http://guarded-hamlet-8595.herokuapp.com/main/confirm");

	    try {
	        // Add your data
	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	        nameValuePairs.add(new BasicNameValuePair("user_name", userName));
	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

	        nameValuePairs.add(new BasicNameValuePair("channel", invitedEvent.getChannel()));
	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

	        nameValuePairs.add(new BasicNameValuePair("id", String.valueOf(invitedEvent.getId())));
	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));	        
	        	        
	        // Execute HTTP Post Request
	        //TODO: fix response headers.
	        org.apache.http.HttpResponse response = httpclient.execute(httppost);
	        for (int i = 0; i < response.getAllHeaders().length; i++) {
	        	Log.d("HEADERS", response.getAllHeaders()[i].toString());
			}
	        
	    } catch (ClientProtocolException e) {
	        // TODO Auto-generated catch block
	    } catch (IOException e) {
	        // TODO Auto-generated catch block
	    }
		
	}

}
