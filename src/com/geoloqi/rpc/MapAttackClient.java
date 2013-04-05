package com.geoloqi.rpc;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.Header;
import org.apache.http.ParseException;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.geoloqi.ADB;
import com.geoloqi.data.Game;
import com.geoloqi.interfaces.GeoloqiConstants;
import com.geoloqi.interfaces.RPCException;
import com.geoloqi.interfaces.RoleMapping;

public class MapAttackClient implements GeoloqiConstants {
	private final String TAG = "MapAttackClient";

	private static final int TIMEOUT = 60000;

	private static final HttpParams httpParams = new BasicHttpParams();
	private static HttpClient client;
	private static MapAttackClient singleton = null;
	private Context context;

	private Integer mMyRoleId=0;

	private String mMyRoleString;
	private String mUserName;
	private String mInitials;

	// private Integer mMyRoleId = 3;
	// private String mMyRoleString = "";

	public final static String PARAM_USER_ROLE = "userRole";

	public static MapAttackClient getApplicationClient(Context context) {
		if (singleton == null) {
			//a lock required?
			singleton = new MapAttackClient(context);
		}
		return singleton;
	}

	public int getRoleId(){
		
		return mMyRoleId;
		
	}
	
	
	public void setRole(int role_id){
		//make sure it index begin with 0
		mMyRoleId = role_id;
		mMyRoleString = RoleMapping.roleMap.get(mMyRoleId);
		Log.i(TAG, "client" + "set+"+mMyRoleString+mMyRoleId);
	}

	private MapAttackClient(Context context) {
		this.context = context;
		//HttpConnectionParams.setConnectionTimeout(httpParams, TIMEOUT);
		//HttpConnectionParams.setSoTimeout(httpParams, TIMEOUT);
		//client = new DefaultHttpClient(httpParams);

	}


	public boolean hasToken() {
		// return context.getSharedPreferences(PREFERENCES_FILE,
		// Context.MODE_PRIVATE).contains("authToken");
		return true;
	}

	public String getToken() {
		return context.getSharedPreferences(PREFERENCES_FILE,
				Context.MODE_PRIVATE).getString("authToken", null);
	}

	public ArrayList<Game> getGames() throws RPCException {
		return getGames(null, null);
	}

	public ArrayList<Game> getGames(Double latitude, Double longitude)
			throws RPCException {
		MyRequest request = new MyRequest(MyRequest.GET, GAME_LIST_ADDRESS
				+ (latitude == null ? "" : "&latitude=" + latitude
						+ "&longitude=" + longitude));
		Header authHeader;
		/*
		 * try { authHeader = new BasicScheme().authenticate(new
		 * UsernamePasswordCredentials(GEOLOQI_ID, GEOLOQI_SECRET),
		 * request.getRequest()); } catch (AuthenticationException e) { throw
		 * new RPCException(e.getMessage()); }
		 * 
		 * request.addHeaders(authHeader);
		 */
		JSONObject response = send(request);
		try {
			JSONArray gamesArray = response.getJSONArray("games");
			//Log.i(TAG, "AAAAAAAA" + response.getString("games"));
			ArrayList<Game> games = new ArrayList<Game>();
			for (int i = 0; i < gamesArray.length(); i++) {
				games.add(new Game(gamesArray.getJSONObject(i)));
			}
			return games;
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Get the best guess intersection name for the given latitude and
	 * longitude.
	 * 
	 * @param latitude
	 * @param longitude
	 * @return The nearest intersection as a String or null.
	 */
	public String getNearestIntersection(final Double latitude,
			final Double longitude) {
		// Build the request
		final MyRequest request = new MyRequest(MyRequest.GET, String.format(
				"%slocation/context?latitude=%s&longitude=%s", URL_BASE,
				latitude, longitude));

		try {
			// Sign the request
			Header authHeader = new BasicScheme()
					.authenticate(new UsernamePasswordCredentials(GEOLOQI_ID,
							GEOLOQI_SECRET), request.getRequest());
			request.addHeaders(authHeader);

			try {
				// Get the response
				JSONObject response = send(request);
				return response.getString("best_name");
			} catch (RPCException e) {
				Log.e(TAG,
						"Got an RPCException when fetching the nearest intersection name!",
						e);
			} catch (JSONException e) {
				Log.e(TAG,
						"Got a JSONException when fetching the nearest intersection name!",
						e);
			}
		} catch (AuthenticationException e) {
			Log.e(TAG,
					"Got an AuthenticationException when fetching the nearest intersection name!",
					e);
		}

		return null;
	}

	public void joinGame(String game_id) throws RPCException {
		String email;
		
		{// Initialise variables
			SharedPreferences prefs = context.getSharedPreferences(
					PREFERENCES_FILE, Context.MODE_PRIVATE);
			Log.i(TAG, "Saving " + mMyRoleString);
			prefs.edit().putString(PARAM_USER_ROLE, mMyRoleString);
			// skill = mMyRoleString;

			// token = prefs.getString("authToken", null);
			mUserName = prefs.getString("name", "");
			mInitials = prefs.getString("initials", "");
			email = prefs.getString("email", "default@some.com");
			
		}
		
		MyRequest request;
		{// Initialise the request.
			String url = URL_BASE + "game/" + game_id + "/join";
			request = new MyRequest(MyRequest.POST, url);
			Log.i(TAG, "joining at " + url);
			request.addEntityParams(
					pair("role_id", mMyRoleId.toString()),
					pair("role", mMyRoleString),
					pair("name", mUserName),
					pair("initials", mInitials),
					pair("email", email));
			
		}

		String user_id = context.getSharedPreferences(PREFERENCES_FILE,
				Context.MODE_PRIVATE).getString("userID", null);
		String old_game_id = context.getSharedPreferences(PREFERENCES_FILE,
				Context.MODE_PRIVATE).getString("gameID", null);
		if (user_id != null && old_game_id != game_id) {
			request.addEntityParams(pair("id", user_id));
			Log.i(TAG, "trying to re-join game " + game_id + " with user id "
					+ user_id);
		} else {
			Log.i(TAG, "trying to join game " + game_id + " with role "
					+ mMyRoleString + " (user id is " + user_id + ")");
		}

		try {// Send will throw a RuntimeException for the non-JSON return
				// value.
			JSONObject response = send(request);
			Log.i(TAG,
					"you have been given user_id = "
							+ response.getString("user_id"));
			context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
					.edit().putString("userID", response.getString("user_id"))
					.commit();
			context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
					.edit().putString("gameID", game_id).commit();
		} catch (JSONException e) {
			ADB.log("JSONException in MapAttackClient/joinGame: "
					+ e.getMessage());
		} catch (RuntimeException e) {
		}
	}
	
	public void logoutGame(String game_id) throws RPCException {
		
		SharedPreferences prefs = context.getSharedPreferences(
				PREFERENCES_FILE, Context.MODE_PRIVATE);	
		Log.i(TAG, "Logging out game" + game_id);	
		String user_id = prefs.getString("userID", null);
		
		MyRequest request;
		{
			String url = URL_BASE + "game/" + game_id + "/logout";
			request = new MyRequest(MyRequest.POST, url);
			Log.i(TAG, "logging out at " + url);
			if (user_id != null)
				request.addEntityParams(pair("id", user_id));
		}

		try {// Send will throw a RuntimeException for the non-JSON return
				// value.
			JSONObject response = send(request);
			Log.i(TAG,
					"logout status = "
							+ response.getString("status"));
		} catch (JSONException e) {
			ADB.log("JSONException in MapAttackClient/logoutGame: "
					+ e.getMessage());
		} catch (RuntimeException e) {
		}
	}
	
	public void sendMessage(String game_id, String message) {
		

		SharedPreferences prefs = context.getSharedPreferences(
				PREFERENCES_FILE, Context.MODE_PRIVATE);	
		Log.i(TAG, "Sending message" + message);	
		String user_id = prefs.getString("userID", null);
		
		MyRequest request;
		
		{
			String url = URL_BASE + "game/mobile/" + game_id + "/message";
			request = new MyRequest(MyRequest.POST, url);
			Log.i(TAG, "Posting to handler " + url);
			if (user_id != null)
				request.addEntityParams(pair("id", user_id));
			request.addEntityParams(pair("content", message.toString()));
		}

		try {// Send will throw a RuntimeException for the non-JSON return
				// value.
			JSONObject response = send(request);
			Log.i(TAG,
					"message response = "
							+ response);
		} catch (RuntimeException e) {
			ADB.log("RuntimeException in MapAttackClient/sendMessage: "
					+ e.getMessage());
		} catch (RPCException e) {
			ADB.log("RPCException in MapAttackClient/sendMessage: "
					+ e.getMessage());
		}
		
	}

	protected synchronized JSONObject send(MyRequest request)
			throws RPCException {
		client=new DefaultHttpClient();
		ADB.log("param " + request.getRequest().getURI());
		Log.i(TAG, "param " + request.getRequest().getURI());
		JSONObject response;
		try {
			String response_str = EntityUtils.toString(client.execute(
					request.getRequest()).getEntity());
			//Log.e("AAA", response_str);
			response = new JSONObject(response_str);
			// response = new
			// JSONObject(client.execute(request.getRequest()).toString());
			// Log.i(TAG, "AAA" + response.toString());
		} catch (ParseException e) {
			ADB.log("ParseException: " + e.getMessage());
			throw new RuntimeException(e.getMessage());
		} catch (JSONException e) {
			ADB.log("JSONException: " + e.getMessage());
			throw new RuntimeException(e.getMessage());
		} catch (ClientProtocolException e) {
			ADB.log("ClientProtocolException: " + e.getMessage());
			throw new RPCException(e.getMessage());
		} catch (IOException e) {
			ADB.log("IOException: " + e.getMessage());
			throw new RPCException(e.getMessage());
		}

		if (response.has("error")) {
			try {
				throw new RPCException(response.getString("error"));
			} catch (JSONException e) {
				throw new RuntimeException(e);
			}
		} else {
			return response;
		}
	}

	private static BasicNameValuePair pair(String key, String val) {
		return new BasicNameValuePair(key, val);
	}

}
