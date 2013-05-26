package models;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import com.geoloqi.ADB;
import com.geoloqi.interfaces.OrchidConstants;
import com.geoloqi.interfaces.StateCallback;
import com.geoloqi.rpc.MyRequest;
import com.geoloqi.widget.ImageLoader;


public class GameState implements OrchidConstants  {
	private static HttpClient client;
	private boolean loaded = false;
	private StateCallback callback = null;
	
	

	public static GameState getGameState(StateCallback callback){
		return new GameState(callback);
	}
	
	public GameState(StateCallback callback) {
		this.callback = callback;
	}
	
	private ArrayList<JSONObject> updates;//maybe for log purpose 
	
	public boolean isLoaded(){
		return loaded;
	}
	
	public void loadState(){
		new LoadGameStateTask(this).execute();
	}

	private JSONObject load(){
		MyRequest request = new MyRequest(MyRequest.GET, URL_BASE +"game/1/status.json");
		client=new DefaultHttpClient();
		ADB.log("param " + request.getRequest().getURI());
		
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
			throw new RuntimeException(e.getMessage());
		} catch (IOException e) {
			ADB.log("IOException: " + e.getMessage());
			throw new RuntimeException(e.getMessage());
		}

		if (response.has("error")) {
			try {
				throw new RuntimeException(response.getString("error"));
			} catch (JSONException e) {
				throw new RuntimeException(e);
			}
		} else {
			return response;
		}
	}
	private void updateGamesStates(){
		
	}
	
	
	/**
	 * A simple AsyncTask to request the game status from the server.
	 * 
	 * 
	 * */
	private static class LoadGameStateTask extends
			AsyncTask<Void, Void, JSONObject> {
		
		private ProgressDialog mProgressDialog = null;
		private GameState mState = null;
		

		public LoadGameStateTask( GameState state) {
			//mContext = context;
			mState = state;
		}

		@Override
		protected void onPreExecute() {
		}
		
		@Override
		protected JSONObject doInBackground(Void... params) {
			//init images
			ImageLoader.getImageLoader();
			return mState.load();
			
		}

		@Override
		protected void onPostExecute(JSONObject gameState) {
			
			
			JSONObject json = gameState;
			mState.loaded = true;
			mState.callback.bulkUpdate(gameState);
			mState.callback.setGameArea();
		}
	}
}
