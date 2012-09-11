package clock.Parse;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.CalendarView;
import clock.db.DbAdapter;
import clock.db.InvitedEvent;
import clock.sched.InitDataView;

import com.parse.ParsePush;

public class ParseHandler extends BroadcastReceiver {
	
	public final static String CONFIRM = "confirm";
	public final static String INVITE = "invitation";
	public final static String IGNORE = "ignore";
	
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
				DbAdapter db = new DbAdapter(context);
				if(pushType.equals(CONFIRM))
				{
					String confirmerName = dataParsed[1];
					int confirmEventId = Integer.parseInt(dataParsed[2]);
					db.addConfirmerNameToEvent(confirmEventId, confirmerName);
				} else if (pushType.equals(INVITE))
				{
					String senderChannel = dataParsed[1];
					InvitedEvent invitedEvent = InvitedEvent.createFromString(dataParsed[2]);
					db.insertInvitedEvent(invitedEvent);
					Log.d(TAG, "received action " + action + " on channel " + channel + " with extras:");
					Log.d(TAG, "Event is:" + invitedEvent.toString());									
				} else if(pushType.equals(INVITE))
				{
					//TODO: other user ignores.
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
		sendMsgFromParams(channelToSend, invitedEvent.getSenderUserName() + " wants to meet with you!", 
							INVITE + "@S@" + invitedEvent.getChannel() + "@S@" + invitedEvent.encodeToString());
		Log.d(TAG, "sending-invitation");
	}

	public static void confirmEvent(InvitedEvent invitedEvent, String userName) {
		
		sendMsgFromParams(invitedEvent.getChannel(), userName + " confirms your event!", 
							CONFIRM + "@S@" + userName + "@S@" + invitedEvent.getOriginalId());
		Log.d(TAG, "sending-confirm with user name: " + userName);		
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

	public static void ignoreEvent(InvitedEvent invitedEvent, String userName) {
		sendMsgFromParams(invitedEvent.getChannel(), userName + " wont come.", 
				IGNORE + "@S@" + userName + "@S@" + invitedEvent.getOriginalId());
		
	}
	
	private static void sendMsgFromParams(String channel, String msg, String data)
	{
		ParsePush push = new ParsePush();
		push.setChannel(channel);
		try {
			push.setData(new JSONObject("{\"alert\": \"" + msg + "\", \"action\": \"clock.Parse.ParseHandler\", \"data\": \"" + 
											data + "\"}"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		push.sendInBackground();
		
	}

}
