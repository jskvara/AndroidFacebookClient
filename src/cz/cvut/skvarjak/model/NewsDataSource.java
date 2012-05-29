package cz.cvut.skvarjak.model;

import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class NewsDataSource extends AbstractDataSource {
	protected static final String TAG = "FacebookClient.NewsDataSource";
	public static final String TABLE_NAME = "news";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_FROM = "fromName"; // from is reserved
	public static final String COLUMN_FROM_ID = "from_id";
	public static final String COLUMN_MESSAGE = "message";
	public static final String COLUMN_TYPE = "type";
	public static final String COLUMN_LINK = "link";
	public static final String COLUMN_PHOTO = "photo";
	public static final String COLUMN_TIME = "time";
	public static final String COLUMN_LIKES = "likes";
	public static final String COLUMN_COMMENTS = "comments";

	public NewsDataSource(Context ctx) {
		super(ctx);
	}

	public Cursor query(String[] columns, String selection,
			String[] selectionArgs, String sortOrder) {
		return mDb.query(TABLE_NAME, columns, selection, selectionArgs,
				null, null, sortOrder);
	}

	public Cursor queryRow(String id) {
		return mDb.query(TABLE_NAME, null, COLUMN_ID + "=?",
				new String[] {id}, null, null, COLUMN_ID, "1");
	}

	protected Cursor getMaxTime() {
		return mDb.query(TABLE_NAME, new String[] { "MAX("
				+ COLUMN_TIME + ")" }, null, null, null, null, "1");
	}
	
	public long getNewestTime() {
		long newestTime = 99999999999999L;
		Cursor c = getMaxTime();
		if (c.moveToFirst()) {
			newestTime = c.getLong(0);
		}
		c.close();
		
		return newestTime;
	}
	
	protected Cursor getMinTime() {
		return mDb.query(TABLE_NAME, new String[] { "MIN("
				+ COLUMN_TIME + ")" }, null, null, null, null, "1");
	}
	
	public long getOldestTime() {
		long oldestTime = 0;
		Cursor c = getMinTime();
		if (c.moveToFirst()) {
			oldestTime = c.getLong(0);
		}
		c.close();
		
		return oldestTime;
	}

	public long insert(ContentValues initialValues) {
		if (!mDb.isDbLockedByOtherThreads() && !mDb.isDbLockedByCurrentThread()) {
			return mDb.insertOrThrow(TABLE_NAME, null, initialValues);
		}
		
		return 0L;
	}

	public long update(ContentValues values) {
		return mDb.update(TABLE_NAME, values, COLUMN_ID + "=?",
				new String[] { values.getAsString(COLUMN_ID) });
	}

	public int delete(String whereClause, String[] whereArgs) {
		return mDb.delete(TABLE_NAME, whereClause, whereArgs);
	}
	
	public int deleteOld() {
		long dayAgo = new Date(System.currentTimeMillis() - 24L * 3600 * 1000).getTime();
		return mDb.delete(TABLE_NAME, COLUMN_TIME + "<" + dayAgo, null);
	}

	public int deleteRow(Long id) {
		return mDb.delete(TABLE_NAME, COLUMN_ID + "=?",
				new String[] { id.toString() });
	}
}