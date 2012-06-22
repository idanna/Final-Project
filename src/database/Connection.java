package database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class Connection extends SQLiteOpenHelper {

	public static final String TABLE_EVENTS = "events";
	public static final String COLUMN_ID = "id";
	public static final String COLUMN_DAY = "day";
	public static final String COLUMN_MONTH = "month";
	public static final String COLUMN_YEAR = "year";
	public static final String COLUMN_HOUR = "hour";
	public static final String COLUMN_MIN = "min";
	public static final String COLUMN_LOCATION = "location";
	public static final String COLUMN_DETAILS = "details";

	private static final String DATABASE_NAME = "events.db";
	private static final int DATABASE_VERSION = 1;

	// Database creation sql statement
	private static final String DATABASE_CREATE = "create table "
			+ TABLE_EVENTS + "(" 
			+ COLUMN_ID + " integer primary key autoincrement, " 
			+ COLUMN_DAY + " int not null,"
			+ COLUMN_MONTH + " int not null,"
			+ COLUMN_YEAR + " int not null,"
			+ COLUMN_HOUR + " int not null,"
			+ COLUMN_MIN + " int not null,"
			+ COLUMN_LOCATION + " text not null,"
			+ COLUMN_DETAILS + " text,);";

	public Connection(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(Connection.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
		onCreate(db);
	}

}