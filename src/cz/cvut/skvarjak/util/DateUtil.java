package cz.cvut.skvarjak.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import android.util.Log;

public class DateUtil {
	protected static final String TAG = "FacebookClient.DateUtil";
	public static final String FACEBOOK_LONG_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
	public static final String DISPLAY_DATE_FORMAT = "d.M.yyyy HH:mm:ss";
	public static final TimeZone TIME_ZONE = Calendar.getInstance().getTimeZone();

	private DateUtil() {
	}

	public static String format(String date) {
		if (date == null || date.equals("")) {
			return "";
		}

		Date d;
		try {
			d = parse(date);
		} catch (Exception e) {
			Log.d(TAG, e.getMessage());
			return "";
		}

		return format(d);
	}

	public static String format(long date) {
		Date d = new Date(date);

		return format(d);
	}

	public static String format(Date date) {
		final SimpleDateFormat sdf = new SimpleDateFormat(DISPLAY_DATE_FORMAT);
		sdf.setTimeZone(TIME_ZONE);

		return sdf.format(date);
	}

	public static Date parse(String date) throws IllegalArgumentException,
			ParseException {
		if (date == null || date.equals("")) {
			throw new IllegalArgumentException("Date is empty");
		}

		Date d = new SimpleDateFormat(FACEBOOK_LONG_DATE_FORMAT).parse(date);

		return d;
	}
}
