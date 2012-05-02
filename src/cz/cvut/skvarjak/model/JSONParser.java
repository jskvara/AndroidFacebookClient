package cz.cvut.skvarjak.model;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class JSONParser {
	protected static final String TAG = "FacebookClient.JSONParser";
	protected JSONObject jsonObject = null;
	
	public JSONParser(String json) {
		try {
			jsonObject = new JSONObject(json);
		} catch (JSONException e) {
			Log.w(TAG, e.getMessage(), e);
		}
	}
	
	public JSONParser(JSONObject jsonObject) {
		this.jsonObject = jsonObject;
	}
	
	public String getFrom() {
		String ret = "";
		try {
			ret = jsonObject.getJSONObject("from").getString("name");
		} catch (JSONException e) {
			Log.w(TAG, e.getMessage(), e);
		}
		
		return ret;
	}
	
	public String getFromId() {
		String ret = "";
		try {
			ret = jsonObject.getJSONObject("from").getString("id");
		} catch (JSONException e) {
			Log.w(TAG, e.getMessage(), e);
		}
		
		return ret;
	}
	
	public String getMessage() {
		String ret = "";
		try {
			ret = jsonObject.getString("message");
		} catch (JSONException e) {
		}
		
		if (ret.equals("")) { // status with place
			try {
				ret = jsonObject.getString("story");
			} catch (JSONException e) {}
		}
		if (ret.equals("")) { // checkin
			try {
				ret = jsonObject.getString("name");
			} catch (JSONException e) {}
		}
		
		return ret;
	}
	
	public int getLikesCount() {
		int ret = 0;
		try {
			JSONObject likes = jsonObject.getJSONObject("likes");
			if (likes != null)  {
				ret = likes.getInt("count");
			}
		} catch (JSONException e) {}
		
		return ret;
	}
	
	public int getCommentsCount() {
		int ret = 0;
		try {
			JSONObject comments = jsonObject.getJSONObject("comments");
			if (comments != null) {
				ret = comments.getInt("count");
			}
		} catch (JSONException e) {
			Log.w(TAG, e.getMessage(), e);
		}
		
		return ret;
	}
	
	public String getTime() {
		String ret = "";
		try {
			ret = jsonObject.getString("updated_time");
		} catch (JSONException e) {
		}
		
		if (ret.equals("")) { // comments have only created_time
			try {
				ret = jsonObject.getString("created_time");
			} catch (JSONException e) {
				Log.w(TAG, e.getMessage() + "Id: " + getId(), e);
			}
		}
		
		return ret;
	}
	
	public String getId() {
		String ret = "";
		try {
			ret = jsonObject.getString("id");
		} catch (JSONException e) {
			Log.w(TAG, e.getMessage(), e);
		}
		
		return ret;
	}
	
	public String getType() {
		String ret = "";
		try {
			ret = jsonObject.getString("type");
		} catch (JSONException e) {
			Log.w(TAG, e.getMessage(), e);
		}
		
		return ret;
	}
	
	public String getLink() {
		String ret = "";
		try {
			ret = jsonObject.getString("link");
		} catch (JSONException e) {
			Log.w(TAG, e.getMessage(), e);
		}
		
		return ret;
	}
	
	public String getPicture() {
		String ret = "";
		try {
			ret = jsonObject.getString("picture");
		} catch (JSONException e) {
			Log.w(TAG, e.getMessage() + ", Id: " + getId(), e);
		}
		
		return ret;
	}
	
	public String getName() {
		String ret = "";
		try {
			ret = jsonObject.getString("name");
		} catch (JSONException e) {
			Log.w(TAG, e.getMessage() + ", Id: " + getId(), e);
		}
		
		return ret;
	}
	
	public JSONParser getComment(int position) {
		JSONObject comments;
		JSONParser jsonParser = null;
		try {
			comments = jsonObject.getJSONObject("comments");
			if (comments != null) {
				jsonParser = new JSONParser(comments.getJSONArray("data").
						getJSONObject(position));
			}
		} catch (JSONException e) {
			Log.w(TAG, e.getMessage(), e);
		}
		
		return jsonParser;
	}
	
	@Override
	public String toString() {
		return jsonObject.toString();
	}
}
