package clock.Parse;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import clock.db.DbAdapter;
import clock.db.Event;
import clock.db.InvitedEvent;

import com.parse.ParsePush;

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
		ParsePush push = new ParsePush();
		push.setChannel(channel);
		push.setMessage(userName + " wants to meet with you!");
		try {
			push.setData(new JSONObject("{\"action\": \"clock.Parse.ParseHandler\", \"data\": \"" + 
											INVITE + "@S@" + userName + "@S@" + invitedEvent.encodeToString() + "\"}"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.d(TAG, "sending-invitation");
		push.sendInBackground();
	}

	public static void confirmEvent(InvitedEvent invitedEvent, String userName) {
		
		ParsePush push = new ParsePush();
		push.setChannel(invitedEvent.getChannel());
		push.setMessage(userName + " wants to meet with you!");
		try {
			push.setData(new JSONObject("{\"action\": \"clock.Parse.ParseHandler\", \"data\": \"" + 
											CONFIRM + "@S@" + userName + "@S@" + invitedEvent.getOriginalId() + "\"}"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.d(TAG, "sending-confirm");
		push.sendInBackground();
		
	}

}
