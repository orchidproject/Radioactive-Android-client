package com.geoloqi.rpc;

import java.io.IOException;
import java.util.ArrayList;

import models.Game;

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
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import com.geoloqi.ADB;
import com.geoloqi.interfaces.OrchidConstants;
import com.geoloqi.interfaces.RPCException;
import com.geoloqi.interfaces.RoleMapping;
import com.geoloqi.widget.ImageLoader.Callback;

public class MapAttackClient implements OrchidConstants {
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

	private int gameId = -1; // -1 is unloaded

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
	
	public boolean isLoaded(){
		if(gameId == -1){
			return false;
		}
		return true;
	}
	public int getGameId(){
		return gameId;
	}
	
	
	public void setRole(int role_id){
		//make sure it index begin with 0
		mMyRoleId = role_id;
		mMyRoleString = RoleMapping.roleMap.get(mMyRoleId);
		Log.i(TAG, "client" + "set+"+mMyRoleString+mMyRoleId);
	}

	private MapAttackClient(Context context) {
		this.context = context;
	}


	public ArrayList<Game> getGames() throws RPCException {
		return getGames(null, null);
	}

	public ArrayList<Game> getGames(Double latitude, Double longitude)
			throws RPCException {
		MyRequest request = new MyRequest(MyRequest.GET, GAME_LIST_ADDRESS
				+ (latitude == null ? "" : "&latitude=" + latitude
						+ "&longitude=" + longitude));
		
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

	public void joinGame(Callback call, String game_id){
		new JoinGameTask(call, game_id).execute();
	}
	
	public void login(String game_id) throws RPCException, JSONException {
		String email;
		
		{// Initialise variables
			SharedPreferences prefs = context.getSharedPreferences(
					PREFERENCES_FILE, Context.MODE_PRIVATE);
			Log.i(TAG, "Saving " + mMyRoleString);
			prefs.edit().putString(PARAM_USER_ROLE, mMyRoleString);
			
			//get variables here
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

		//get game id and user id, if null, it means it is a new user login
		String user_id = context.getSharedPreferences(PREFERENCES_FILE,
				Context.MODE_PRIVATE).getString("userID", null);
		String old_game_id = context.getSharedPreferences(PREFERENCES_FILE,
				Context.MODE_PRIVATE).getString("gameID", null);
		
		boolean rejoin = false;
		if (user_id != null && old_game_id.equals(game_id)) {
			request.addEntityParams(pair("id", user_id));
			Log.i(TAG, "trying to re-join game " + game_id + " with user id "
					+ user_id);
			rejoin = true;
		} else {
			Log.i(TAG, "trying to join game " + game_id + " with role "
					+ mMyRoleString + " (user id is " + user_id + ")");
		}

		
		JSONObject response = send(request);
		
		
		//save game user id to preferences
		if(!rejoin){ //if the player is not rejoin the game, it has a new id and it need to be saved in preference.
			Log.i(TAG,"you have been given user_id = "+ response.getString("user_id"));
			context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
					.edit().putString("userID", response.getString("user_id"))
					.commit();
		}
		context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
					.edit().putString("gameID", game_id).commit();
		
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
			
			response = new JSONObject(response_str);
			
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
	
	
	
	//--------asyncTask for login ---------------
	abstract static public class Callback {
		abstract public void callback(int status);
	}
	
	private class JoinGameTask extends
	AsyncTask<Void, Void,Integer> {
	
		Callback callback = null;
		String game_id = null;
		public JoinGameTask(Callback call,String game_id){
			callback =  call;
			this.game_id = game_id;
		}

		@Override
		protected Integer doInBackground(Void... params) {
			try {
				login(game_id);
			} catch (RPCException e) {
				return 1;
			} catch (JSONException e) {
				return 2;
			} catch (Exception e) {
				return 3;
			} 
			
			gameId = Integer.parseInt(game_id);
			return 0;
		}

		@Override
		protected void onPostExecute(Integer status) {
			callback.callback(status);
		}
	}

	

}
