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

public class Connection extends SQLiteOpenHelper 
{
	public static final String TABLE_EVENTS = "events";
	public static final String TABLE_ADDRESS = "address";

	public static final String COLUMN_ID = "id";
	
	// adrress attr
	public static final String COLUMN_STREET = "streets";
	public static final String COLUMN_CITY = "city";
	
	// events attr
	public static final String COLUMN_DATE = "date";
//	public static final String COLUMN_DAY = "day";
//	public static final String COLUMN_MONTH = "month";
//	public static final String COLUMN_YEAR = "year";
//	public static final String COLUMN_HOUR = "hour";
//	public static final String COLUMN_MIN = "min";
	public static final String COLUMN_LOCATION = "location";
	public static final String COLUMN_DETAILS = "details";
	public static final String COLUMN_ALARM = "alarm";

	private static final String DATABASE_NAME = "smart_clock.db";
	private static final int DATABASE_VERSION = 1;

    //The Android's default system path of your application database.
    private static String DB_PATH = "/data/data/clock.sched/databases/";
 
    private final Context myContext;	
    /**
     * Constructor
     * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
     * @param context
     * @throws IOException 
     */
    public Connection(Context context) throws IOException {
 
    	super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.myContext = context;
        this.createDataBase();
    }	
    
    public SQLiteDatabase openDataBase() throws SQLException
    {
    	//Open the database
        String myPath = DB_PATH + DATABASE_NAME;
    	return SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);

    }
    
	@Override
	public void onCreate(SQLiteDatabase database) {
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}
	 	
	/**
     * Creates a empty database on the system and rewrites it with your own database.
     * */
    private void createDataBase() throws IOException{
 
    	boolean dbExist = checkDataBase();
    	
    	if(!dbExist)
    	{
    		//By calling this method and empty database will be created into the default system path
               //of your application so we are gonna be able to overwrite that database with our database.
        	this.getReadableDatabase();
        	try 
        	{ 
				copyDataBase();
    		} 
        	catch (IOException e) {
 
        		throw new Error("Error copying database");
 
        	}
    	}
 
    }
 
    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDataBase(){
 
    	SQLiteDatabase checkDB = null;
 
    	try{
    		String myPath = DB_PATH + DATABASE_NAME;
    		checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
 
    	}
    	catch(SQLiteException e){
 
    	} 
    	if(checkDB != null){
 
    		checkDB.close();
    	}
 
    	return checkDB != null ? true : false;
    }
 
    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     * */
    private void copyDataBase() throws IOException{
 
    	//Open your local db as the input stream
    	InputStream myInput = myContext.getAssets().open(DATABASE_NAME);
 
    	// Path to the just created empty db
    	String outFileName = DB_PATH + DATABASE_NAME;
 
    	//Open the empty db as the output stream
    	OutputStream myOutput = new FileOutputStream(outFileName);
 
    	//transfer bytes from the inputfile to the outputfile
    	byte[] buffer = new byte[1024];
    	int length;
    	while ((length = myInput.read(buffer))>0){
    		myOutput.write(buffer, 0, length);
    	}
 
    	//Close the streams
    	myOutput.flush();
    	myOutput.close();
    	myInput.close();
 
    }

}