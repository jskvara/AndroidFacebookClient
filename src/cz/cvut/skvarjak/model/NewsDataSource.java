package cz.cvut.skvarjak.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class NewsDataSource extends AbstractDataSource {
	protected static final String TAG = "FacebookClient.NewsDataSource";
	public static final String TABLE_NAME = "news";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_FROM = "fromName"; // from is reserved
	public static final String COLUMN_MESSAGE = "message";
	public static final String COLUMN_TYPE = "type";
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

	public Cursor queryRowNewest() {
		return mDb.query(TABLE_NAME, new String[] { "MAX("
				+ COLUMN_TIME + ")" }, null, null, null, null, "1");
	}

	public long insert(ContentValues initialValues) {
		return mDb.insert(TABLE_NAME, null, initialValues);
	}

	public long update(ContentValues values) {
		return mDb.update(TABLE_NAME, values, COLUMN_ID + "=?",
				new String[] { values.getAsString(COLUMN_ID) });
	}

	public int delete(String whereClause, String[] whereArgs) {
		return mDb.delete(TABLE_NAME, whereClause, whereArgs);
	}

	public int deleteRow(Long id) {
		return mDb.delete(TABLE_NAME, COLUMN_ID + "=?",
				new String[] { id.toString() });
	}
}