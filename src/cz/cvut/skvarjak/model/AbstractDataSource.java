package cz.cvut.skvarjak.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public abstract class AbstractDataSource {
	protected static final String TAG = "FacebookClient.AbstractDbAdapter";
	protected DatabaseHelper mDbHelper;
	protected SQLiteDatabase mDb;
	protected final Context mCtx;
	
	protected static class DatabaseHelper extends SQLiteOpenHelper {
		protected static final int DATABASE_VERSION = 1;
        protected static final String DATABASE_NAME = "cz.cvut.skvarjak.FacebookClient";

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			initFriendsDb(db);
			initNewsDb(db);
		}
		
		public void initFriendsDb(SQLiteDatabase db) {
            String create = "CREATE TABLE " + FriendsDataSource.TABLE_NAME + " (" +
            		FriendsDataSource.COLUMN_ID + " INTEGER PRIMARY KEY," + 
            		FriendsDataSource.COLUMN_NAME + " TEXT);";
            db.execSQL(create);
        }
		
		public void initNewsDb(SQLiteDatabase db) {
			String create = "CREATE TABLE " + NewsDataSource.TABLE_NAME + " (" +
					NewsDataSource.COLUMN_ID + " TEXT PRIMARY KEY, " +
					NewsDataSource.COLUMN_FROM + " TEXT NOT NULL DEFAULT '', " +
					NewsDataSource.COLUMN_FROM_ID + " INTEGER NOT NULL, " +
					NewsDataSource.COLUMN_MESSAGE + " TEXT NOT NULL DEFAULT '', " +
					NewsDataSource.COLUMN_TYPE + " TEXT NOT NULL DEFAULT 'status', " +
					NewsDataSource.COLUMN_LINK + " TEXT NOT NULL DEFAULT '', " +
					NewsDataSource.COLUMN_PHOTO + " TEXT NOT NULL DEFAULT '', " +
					NewsDataSource.COLUMN_TIME + " INTEGER NOT NULL, " +
					NewsDataSource.COLUMN_LIKES + " INTEGER NOT NULL DEFAULT 0, " +
					NewsDataSource.COLUMN_COMMENTS + " INTEGER NOT NULL DEFAULT 0" +
			");";
			db.execSQL(create);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + NewsDataSource.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + FriendsDataSource.TABLE_NAME);
			onCreate(db);
		}
	}

	public AbstractDataSource(Context ctx) {
		this.mCtx = ctx;
		mDbHelper = new DatabaseHelper(mCtx);
	}

	public void open() {
		mDb = mDbHelper.getWritableDatabase();
	}
	
	public void openReadable() {
		mDb = mDbHelper.getReadableDatabase();
	}
	
	public boolean isOpen() {
		return mDb.isOpen();
	}

	public void close() {
		if (mDb != null && mDb.isOpen() && !mDb.isDbLockedByOtherThreads()) {
			mDb.close();
		}
	}
}
