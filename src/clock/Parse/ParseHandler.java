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

import com.parse.ParseObject;
import com.parse.signpost.http.HttpResponse;

public class ParseHandler extends BroadcastReceiver {
	
	  private static final String TAG = "ParseHandler";

	  @Override
	  public void onReceive(Context context, Intent intent) {
	    try {
	      String action = intent.getAction();
	      String channel = intent.getExtras().getString("com.parse.Channel");
	      JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));

	      Log.d(TAG, "received action " + action + " on channel " + channel + " with extras:");
	      Iterator itr = json.keys();
	      while (itr.hasNext()) {
	        String key = (String) itr.next();
	        Log.d(TAG, "..." + key + " => " + json.getString(key));
	      }
	    } catch (JSONException e) {
	      Log.d(TAG, "JSONException: " + e.getMessage());
	    }
	  }

	public static void sendMsg(String string) {
		
		postData(string);
		
		ParseObject testObject = new ParseObject("TestObject");
		testObject.put("details", string);
		testObject.saveInBackground();		
	}
	
	private static void postData(String string) {
	    // Create a new HttpClient and Post Header
	    HttpClient httpclient = new DefaultHttpClient();
	    HttpPost httppost = new HttpPost("http://guarded-hamlet-8595.herokuapp.com/main/push");

	    try {
	        // Add your data
	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	        nameValuePairs.add(new BasicNameValuePair("msg", string));
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
