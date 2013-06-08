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
	
	public void loadState(int game_id){
		new LoadGameStateTask(game_id).execute();
	}

	private JSONObject load(int game_id){
		MyRequest request = new MyRequest(MyRequest.GET, URL_BASE +"game/"+game_id+"/status.json");
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
	
	
	
	
	/**
	 * A simple AsyncTask to request the game status from the server.
	 * 
	 * 
	 * */
	private class LoadGameStateTask extends
			AsyncTask<Void, Void, JSONObject> {
		
		private ProgressDialog mProgressDialog = null;
		private int game_id;
		

		public LoadGameStateTask(int game_id) {
			this.game_id =  game_id;
		}

		@Override
		protected void onPreExecute() {
		}
		
		@Override
		protected JSONObject doInBackground(Void... params) {
			//init images
			ImageLoader.getImageLoader().loadImages();
			return load(game_id);
			
		}

		@Override
		protected void onPostExecute(JSONObject gameState) {
			
			
			//JSONObject json = gameState;
			loaded = true;
			callback.bulkUpdate(gameState);
			callback.setGameArea();
		}
	}
}
