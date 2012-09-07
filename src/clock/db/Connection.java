package clock.db;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Creates, Open, Close db connections.
 * @author Idan
 *
 */
public class Connection extends SQLiteOpenHelper 
{
	public static final String TABLE_EVENTS = "events";
	public static final String TABLE_RECORDS = "records";
	public static final String TABLE_INVITED = "invited";
	public static final String TABLE_DATA = "user_data";

	public static final String COLUMN_ID = "id";
	public static final String COLUMN_DATE = "date";
	public static final String COLUMN_LOCATION = "location";
	public static final String COLUMN_DETAILS = "details";
	public static final String COLUMN_ALARM = "alarm";
	public static final String COLUMN_NOTIFIED = "notified";
	public static final String COLUMN_WAKEDUP = "wakedup";
	
	public static final String COLUMN_INVITER_CHANNEL = "channel";
	public static final String COLUMN_ORIGINAL_ID = "orig_id";

	public static final String COLUMN_ARR_TIME = "arrange_time";
	public static final String COLUMN_WEATHER = "weather";
	public static final String COLUMN_TEMPETURE = "tempeture";
	public static final String COLUMN_DAY_OF_WEEK = "day";
	
	public static final String COLUMN_CHANNEL_HASH = "channel";
	public static final String COLUMN_USER_NAME = "user_name";
	public static final String COLUMN_SENDER_USER_NAME = "sender_username";   
	
	private static final String DATABASE_NAME = "smart_clock.db";
	private static final int DATABASE_VERSION = 5;

    private static final String CREATE_EVENT_TABLE = "create table "
    		+ TABLE_EVENTS + "("
    		+ COLUMN_ID + " integer primary key autoincrement, "
    		+ COLUMN_DATE + " text not null,"
    		+ COLUMN_LOCATION + " text not null,"
    		+ COLUMN_DETAILS + " text,"
    		+ COLUMN_ALARM + " integer,"
    		+ COLUMN_NOTIFIED + " integer," 
    		+ COLUMN_WAKEDUP + " integer);";
    
    private static final String CREATE_INVITED_EVENT_TABLE = "create table "
    		+ TABLE_INVITED + "("
    		+ COLUMN_ID + " integer primary key autoincrement, "
    		+ COLUMN_ORIGINAL_ID + " integer, " 
    		+ COLUMN_DATE + " text not null,"
    		+ COLUMN_LOCATION + " text not null,"
    		+ COLUMN_DETAILS + " text,"
    		+ COLUMN_INVITER_CHANNEL + " text, "
    		+ COLUMN_SENDER_USER_NAME + " text);";
    
    private static final String CREATE_RECORDS_TABLE = "create table " 
			+ TABLE_RECORDS + " ( " + COLUMN_ARR_TIME + " integer, " 
			+ COLUMN_DAY_OF_WEEK + " text, "
			+ COLUMN_WEATHER + " text, "
			+ COLUMN_TEMPETURE + " integer);";
    private static final String CREATE_DATA_TABLE = "create table " 
			+ TABLE_DATA + " ( " + COLUMN_CHANNEL_HASH + " text, "
			+ COLUMN_USER_NAME + " text);"; 
    /**
     * Constructor
     * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
     * @param context
     * @throws IOException 
     */
    public Connection(Context context) 
    {
    	super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }	
        
	@Override
	public void onCreate(SQLiteDatabase database) {		
		database.execSQL(CREATE_EVENT_TABLE);
		database.execSQL(CREATE_RECORDS_TABLE);
		database.execSQL(CREATE_INVITED_EVENT_TABLE);
		database.execSQL(CREATE_DATA_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d(Connection.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
				+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECORDS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_INVITED);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_DATA);
		onCreate(db);
	}
	
}