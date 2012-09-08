package clock.sched;

import clock.Parse.ParseHandler;
import clock.db.DbAdapter;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * Asks the user for the initial data the app need.
 * 1. phone number - for push services.
 * 2. initial manual arrngment time.
 * Should not let the user to go back to main screen unless data is inserted.
 * @author Idan
 *
 */
public class InitDataView extends Activity implements OnClickListener
{

//TODO: Should not let the user to go back to main screen unless data is inserted.
	
	private DbAdapter dbAdapter;
	private TextView arrTextView;
	private TextView phoneTextView;
	private Button confirmBtn;
	private TextView userNameTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dbAdapter = new DbAdapter(this);
		setContentView(R.layout.init_data_dialog);
		phoneTextView = (TextView)this.findViewById(R.id.init_phone);
		arrTextView = (TextView)this.findViewById(R.id.init_arrg_time);
		userNameTextView = (TextView)this.findViewById(R.id.init_name);
		confirmBtn = (Button)this.findViewById(R.id.init_save_btn);
		confirmBtn.setOnClickListener(this);
	}
	

	@Override
	public void onClick(View v) {
		String phoneNumber = phoneTextView.getText().toString();
		String userName = userNameTextView.getText().toString();
		String channelHash = ParseHandler.numberToChannelHash(phoneNumber);
//		int userChannel = numberToChannel(phoneNumber);
		int arrTime = Integer.valueOf(arrTextView.getText().toString());
		dbAdapter.setInitData(userName, arrTime, channelHash);
		setResult(RESULT_OK);
		finish();
	}

}
