package cz.cvut.skvarjak.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class FriendsDataSource extends AbstractDataSource {
	public static final String TABLE_NAME = "friends";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_NAME = "name";

	public FriendsDataSource(Context ctx) {
		super(ctx);
	}

	public Cursor query(String[] columns, String selection,
			String[] selectionArgs, String sortOrder) {
		return mDb.query(TABLE_NAME, columns, selection, selectionArgs, null,
				null, sortOrder);
	}

	public Cursor queryRow(String id) {
		return mDb.query(TABLE_NAME, null, COLUMN_ID + "=?",
				new String[] { id }, null, null, COLUMN_ID, "1");
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

	public boolean deleteRandom(int count) {
		Cursor c = mDb.query(TABLE_NAME, new String[] { COLUMN_ID }, null,
				null, null, null, "RANDOM()", String.valueOf(count));
		try {
			c.moveToFirst();
			while (c.isAfterLast() == false) {
				String id = c.getString(0);
				mDb.delete(TABLE_NAME, COLUMN_ID + "=?", new String[] { id });
				c.moveToNext();
			}
		} finally {
			c.close();
		}

		return true;
	}

	public int deleteRow(Long id) {
		return mDb.delete(TABLE_NAME, COLUMN_ID + "=?",
				new String[] { id.toString() });
	}
}