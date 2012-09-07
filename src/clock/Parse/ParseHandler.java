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
				String senderChannel = dataParsed[1];
				if(pushType.equals(CONFIRM))
				{
					int confirmEventId = Integer.parseInt(dataParsed[2]);
					//TODO: for now only notifing
				} else if (pushType.equals(INVITE))
				{
					InvitedEvent invitedEvent = InvitedEvent.createFromString(dataParsed[2]);
					DbAdapter db = new DbAdapter(context);
					db.insertInvitedEvent(invitedEvent);
					Log.d(TAG, "received action " + action + " on channel " + channel + " with extras:");
					Log.d(TAG, "Event is:" + invitedEvent.toString());									
					
				}
		  } catch (JSONException e) {
					Log.d(TAG, "ParseOnRecieve: " + e.getMessage());	
				}
	  }
	  
	/**
	 * Send an invitation via Parse push server.
	 * @param invitedEvent - the event to invited
	 * @param userName - inviter user name
	 * @param userChannel - invited channel
	 * @param channelToSend - channel to send. DO NOT send a phone number, call ParseHandler.numberToChannelHash(phoneNumber) and then send.
	 */
	public static void sendInvitation(InvitedEvent invitedEvent, String channelToSend) {
		ParsePush push = new ParsePush();
		push.setChannel(channelToSend);
		try {
			push.setData(new JSONObject("{\"alert\": \"" + invitedEvent.getSenderUserName() + " wants to meet with you!\", \"action\": \"clock.Parse.ParseHandler\", \"data\": \"" + 
											INVITE + "@S@" + invitedEvent.getChannel() + "@S@" + invitedEvent.encodeToString() + "\"}"));
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
		try {
			push.setData(new JSONObject("{\"alert\": \"" + userName + " confirms your event!\", \"action\": \"clock.Parse.ParseHandler\", \"data\": \"" + 
											CONFIRM + "@S@" + userName + "@S@" + invitedEvent.getOriginalId() + "\"}"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.d(TAG, "sending-confirm with user name: " + userName);
		push.sendInBackground();
		
	}
	
	public static String numberToChannelHash(String phoneNumber) 
	{
		String channelHash = "";
		for (int i = 0; i < phoneNumber.length(); i++){
		    char c = phoneNumber.charAt(i);
		    channelHash += (char)('A' + c);
		    //Process char
		}
		
		Log.d("ChannelHash: ", channelHash);
		return channelHash;
	}

}
