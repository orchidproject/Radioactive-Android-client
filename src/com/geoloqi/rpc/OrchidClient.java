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
import android.os.AsyncTask;
import android.util.Log;
import com.geoloqi.ADB;
import com.geoloqi.interfaces.OrchidConstants;
import com.geoloqi.interfaces.RPCException;
import com.geoloqi.interfaces.RoleMapping;

public class OrchidClient implements OrchidConstants {
	private final String TAG = "MapAttackClient";

	private static HttpClient client;
	private static OrchidClient singleton = null;
	private Context context;

	
	
	//user information
	private String mInitials=null;
	private int gameId = -1; // -1 is unloaded
	private int userId =  -1;// unassigined userName
	private int mRoleId =  -1;//unassigned
	private String mMyRoleString=null;
	private boolean isLogin = false;
	private AsyncTask<Void, Void, Integer> mJoinGameTask;

	private boolean rejoin;
	private String name;

	

	public final static String PARAM_USER_ROLE = "userRole";

	public static OrchidClient getApplicationClient(Context context) {
		if (singleton == null) {
			//a lock required?
			singleton = new OrchidClient(context);
		}
		return singleton;
	}

	public int getRoleId(){	
		return mRoleId;
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
	
	public int getPlayerId(){
		return userId;
	}
	
	public String getRoleString(){
		return mMyRoleString;
	}
	
	public String getInitials() {
		return mInitials;
	}
	
	private void setRole(int role_id){
		//make sure it index begin with 0
		mRoleId = role_id;
		mMyRoleString = RoleMapping.roleMap.get(mRoleId);
		Log.i(TAG, "client" + "set+"+mMyRoleString+mRoleId);
	}

	private OrchidClient(Context context) {
		this.context = context;
		SharedPreferences sp = context.getSharedPreferences(OrchidConstants.PREFERENCES_FILE, Context.MODE_PRIVATE);
		//pupulate fields
		//get game id and user id, if null, it means it is a new user login
		String old_user_id = sp.getString("userID", null);
		String old_game_id = sp.getString("gameID", null);
		String old_role = sp.getString("roleId", null);
		String old_initials = sp.getString("initials", null);
		String old_name = sp.getString("name", null);
		//check integratiy
		if(old_user_id!=null&&
		   old_game_id!=null&&
		   old_role!=null&&
		   old_initials!=null
		   )
		{
			setRole(Integer.valueOf(old_role));
			gameId = Integer.valueOf(old_game_id);
			mInitials = old_initials;
			userId = Integer.valueOf(old_user_id);
			isLogin=true;
			name = old_name;
		}
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

	public void joinGame(Callback call, String game_id,String initials, String name, String roleId){
		mJoinGameTask = new JoinGameTask(call, game_id, initials, name, roleId);
		mJoinGameTask.execute();
	}
	
	public JSONObject login(String game_id, String initials, String name, String roleId) throws RPCException, JSONException {
	
		MyRequest request;
		String url = URL_BASE + "game/" + game_id + "/join";
		request = new MyRequest(MyRequest.POST, url);
		Log.i(TAG, "joining at " + url);
		if(isLogin&&(gameId+"").equals(game_id)){
			request.addEntityParams(pair("id", userId+""));
			rejoin = true;
		}
		else{
			request.addEntityParams(
					pair("role_id",Integer.valueOf(roleId).toString()),
					pair("name", name),
					pair("initials", initials),
					pair("email", "default@some.com"));
			rejoin = false;
		}
		
		return send(request);
		
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
		String game_id_to_join = null;
		String initials_to_join = null;
		String name_to_join = null;
		String role_id_to_join =null;
		
		public JoinGameTask(Callback call,String game_id, String initials, String name,String roleId){
			callback =  call;
			game_id_to_join = game_id;
			initials_to_join = initials;
			name_to_join = name;
			role_id_to_join = roleId;
		}

		@Override
		protected Integer doInBackground(Void... params) {
			JSONObject response = null;
			try {
				response = login(
						game_id_to_join,
						initials_to_join,
						name_to_join,
						role_id_to_join
					);
				
			} catch (RPCException e) {
				return 1;
			} catch (JSONException e) {
				return 2;
			} catch (Exception e) {
				e.printStackTrace();
				return 3;
			} 
			
			//if game changes, save game id, other info keep same;
			if(rejoin) {
				isLogin = true;
				return 0;
			}
			
			//save everything here
			Log.i(TAG,"you have been given user_id = "+ response.optString("user_id","null"));
			String user_id = response.optString("user_id",null);
			
			userId = Integer.parseInt(user_id);
			gameId = Integer.parseInt(game_id_to_join);	
			mInitials = initials_to_join;
			setRole(Integer.valueOf(role_id_to_join));

			context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
						.edit().putString("userID", user_id).commit();
			context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
					.edit().putString("gameID", game_id_to_join).commit();
			context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
						.edit().putString("initials", initials_to_join).commit();
			context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
						.edit().putString("roleId", role_id_to_join).commit();
			context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
						.edit().putString("name", name_to_join).commit();
			
			isLogin = true;
			return 0;
		}
		

		@Override
		protected void onPostExecute(Integer status) {		
			callback.callback(status);		
		}
	}

	public boolean isLoggin() {
		
		if(mRoleId!=-1&&gameId!=-1&&userId!=-1&&mInitials!=null&&isLogin){
			return true;
		}else{
			isLogin=false;
			return false;
		}
	}

	public void logout() {
		mRoleId=-1;
		gameId=-1;
		userId=-1;
		mInitials=null;
		mMyRoleString =null;
		isLogin=false;
		context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE).edit().clear().commit();
	}

	public String getName() {
		
		return name;
	}

}
