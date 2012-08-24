package clock.db;

import java.io.FileOutputStream;
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

	// events attr
	public static final String COLUMN_ID = "id";
	public static final String COLUMN_DATE = "date";
	public static final String COLUMN_LOCATION = "location";
	public static final String COLUMN_DETAILS = "details";
	public static final String COLUMN_ALARM = "alarm";
	public static final String COLUMN_NOTIFIED = "notified";
	public static final String COLUMN_WAKEDUP = "wakedup";

	// DURATION
	public static final String COLUMN_ARR_TIME = "arrange_time";
	public static final String COLUMN_WEATHER = "weather";
	public static final String COLUMN_TEMPETURE = "tempeture";
	public static final String COLUMN_DAY_OF_WEEK = "day";
	
	private static final String DATABASE_NAME = "smart_clock.db";
	private static final int DATABASE_VERSION = 1;

    private static final String CREATE_EVENT_TABLE = "create table "
    		+ TABLE_EVENTS + "("
    		+ COLUMN_ID + " integer primary key autoincrement, "
    		+ COLUMN_DATE + " text not null,"
    		+ COLUMN_LOCATION + " text not null,"
    		+ COLUMN_DETAILS + " text,"
    		+ COLUMN_ALARM + " integer,"
    		+ COLUMN_NOTIFIED + " integer," 
    		+ COLUMN_WAKEDUP + " integer);";
    
    private static final String CREATE_RECORDS_TABLE = "create table " 
			+ TABLE_RECORDS + " ( " + COLUMN_ARR_TIME + " integer, " 
			+ COLUMN_DAY_OF_WEEK + "text, "
			+ COLUMN_WEATHER + " text, "
			+ COLUMN_TEMPETURE + "integer);";
    
    private static final String DATABASE_CREATE = CREATE_EVENT_TABLE + CREATE_RECORDS_TABLE;
//    private final Context myContext;	
    /**
     * Constructor
     * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
     * @param context
     * @throws IOException 
     */
    public Connection(Context context) 
    {
    	super(context, DATABASE_NAME, null, DATABASE_VERSION);
//        this.myContext = context;
//        this.createDataBase();
    }	
        
	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//		Log.d(Connection.class.getName(),
//				"Upgrading database from version " + oldVersion + " to "
//				+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
		onCreate(db);
	}
	
}