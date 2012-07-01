package clock.sched;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver 
{

	@Override
	public void onReceive(Context context, Intent i) 
	{
		Bundle b = i.getExtras();
		String msg = b.getString("event_details");
		Log.d("ALARM", msg);
		Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
	}

}
