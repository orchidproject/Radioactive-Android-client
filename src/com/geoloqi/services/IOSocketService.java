package com.geoloqi.services;

import java.io.IOException;
import java.nio.channels.NotYetConnectedException;
import java.util.Arrays;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.clwillingham.socket.io.IOSocket;
import com.clwillingham.socket.io.MessageCallback;
import com.geoloqi.interfaces.GeoloqiConstants;
import com.geoloqi.interfaces.LoggingConstants;
import com.geoloqi.interfaces.RoleMapping;
import com.geoloqi.ui.GameListActivity;
import com.geoloqi.ui.TabbedMapActivity;

public class IOSocketService extends Service implements GeoloqiConstants,
		MessageCallback {
	//singleton
	
	private static IOSocketService mInstance;
	
	public static IOSocketService getInstance(){
		return mInstance;
	}
	
	
	protected static final String TAG = LoggingConstants.RECORDING_TAG;

	private IOSocket socket;

//	private ConnectSocketTask connector;
	private volatile ConnectorThread connector;

	private boolean connected = false;
	private boolean destroyed = false;

	private String mGameID;

	private String mUserID;
	private String mInitials;

	private String mMyRoleString;
	
	// private String skill;
	

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate(){
		//assign singleton
		try {
			if (mInstance == null){
				mInstance=this;
			}
			else{
				throw new Exception("");
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
	
	
	@Override
	public void onStart(Intent intent, int startId) {
		setRole(getApplicationContext());

		System.setProperty("java.net.preferIPv6Addresses", "false");
		String url = "http://" + IOSOCKET_ADDRESS + ":" + IOSOCKET_PORT + "/";
		Log.i(TAG, "Starting IO socket service at " + url);
		socket = new IOSocket(url, this);
		mGameID = intent.getStringExtra(GameListActivity.PARAM_GAME_ID);
		mUserID = intent.getStringExtra(TabbedMapActivity.PARAM_USER_ID);
		mInitials = intent.getStringExtra(TabbedMapActivity.PARAM_INITIALS);

		// skill = getSharedPreferences(GeoloqiConstants.PREFERENCES_FILE,
		// Context.MODE_PRIVATE).getString(
		// MapAttackClient.PARAM_USER_ROLE, "unknown");

		// skill = MapAttackClient.skill;

		// Log.i("Role", "My skill is " + skill);

		registerGPSReceiver();
		connectSocket();
	}

	private void setRole(Context context) {
		String imei = ((TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();

//		Integer mMyRoleId = RoleMapping.imeiMap.get(imei);
		Integer mMyRoleId = 4;
//		if (mMyRoleId == null) {
//			mMyRoleId = 2;
//		}
		mMyRoleString = RoleMapping.roleMap.get(mMyRoleId);

	}
	
	private class ConnectSocketTask extends AsyncTask<Void, Void, Void> {
	     protected Void doInBackground(Void... params) {
	    	 int counter = 0;
			connected = false;
					while (!connected) {
						try {
							counter++;
							socket.connect();
							connected = true;
							Log.i(TAG, "Connected to " + socket);
						} catch (IOException e) {
							e.printStackTrace();
						}
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						if (counter % 30 == 0) {
							Log.e(TAG,
									String.format(
											"Tried to connect for %d seconds, but no connection yet.",
											counter));
						}
					}
					return params[0];
	     }

	     protected void onProgressUpdate(Void progress) {
//	         setProgressPercent(progress[0]);
	     }

	     protected void onPostExecute(Void result) {
	         //
	     }
	 }
	
	class ConnectorThread extends Thread {
		public boolean running;
		
		@Override
		public void run() {
			int counter = 0;
			connected = false;
			running = true;
			
			while ((!connected) && running) {
				try {
					counter++;
					socket.connect();
					connected = true;
					Log.i(TAG, "Connected to " + socket);
				} catch (IOException e) {
					e.getMessage();
				}
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.getMessage();
				}
				
				if (counter % 30 == 0) {
					Log.e(TAG,
							String.format(
									"Tried to connect for %d seconds, but no connection yet.",
									counter));
				}
			}
				
		}
		
	}
	
	private synchronized void connectSocket() {
		
		//connect using AsyncTask instead of thread.
//		if (connector == null) 
//			connector = new ConnectSocketTask();
		
		if(connector == null){
		    connector = new ConnectorThread(); 
		}
		
		if (!connector.running)
			connector.setDaemon(true);
		connector.start();
		  
	}
	
	private synchronized void reconnectSocket() {
		stopConnecting();
		connectSocket();
	}
		
	public synchronized void stopConnecting(){
	  if(connector != null){
	    connector.running = false;
	    connector.interrupt();
	    while (connector.isAlive()) {
	    	try {
				connector.join();
			} catch (InterruptedException e) {
				Log.e(TAG, e.getMessage());
			}
	    }
	    connector = null;
	  }
	}



	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		onStart(intent, startId);
		return Service.START_REDELIVER_INTENT;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterGPSReceiver();
		destroyed = true;
		if (socket != null) {
			try {
//				socket.disconnect();
				stopConnecting();
				socket.disconnect();
				                              				
//				connector.cancel(true);
				Log.d(TAG, "thread stopped");
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
//		if (connector != null) {
//			try { 
//				connector.stop();
//				
//			} catch (Exception e) {
//				Log.e(TAG, e.getMessage());
//			}
//		}
		connected = false;
	}

	public void forward(String json) {
		Intent notifyPush = new Intent("PUSH");
		notifyPush.putExtra("json", json);
		Log.i(TAG, "Pushing notification to UI: " + json);
		sendBroadcast(notifyPush);
	}

	// ////// SOCKET CALLBACK METHODS ///////

	@Override
	public void on(String event, JSONObject... data) {
		if (event == null) {
			Log.e(TAG, "Received null event.");
		} else {
			Log.i(TAG, String.format(
					"Received event %s from Web socket with objects %s", event,
					Arrays.toString(data)));
			if (event.equals("data")) {
				for (JSONObject jsonObject : data) {
					forward(jsonObject.toString());

					try{
						sendBackAck(jsonObject.getString("ackid"));
					}
					catch(Exception e){
						e.printStackTrace();
					}

				}
				
			} else if (event.equals("game")) {
				int counter = 0;
				boolean done = false;
				while (!done) {
					try {
						joinGame();
						Thread.sleep(1000);
						done = true;
					} catch (NotYetConnectedException nye) {
						if (counter++ > 10) {
							throw nye;
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			} else {
				Log.w(TAG, "Could not handle event " + event);
			}
			
			
		}
	}
	
	private void sendBackAck(String ackid){
		try {
		socket.emit("ack",new JSONObject("{ackid:"+ackid+",channel:"+mGameID+"}"));
		} catch (IOException e) {
			Log.e(TAG, "IOException in sendBackAck: " + e);
		}catch (JSONException e){
			Log.e(TAG, "JSON parse error in sendBackAck: " + e);
		}
	}
	
	private void joinGame() {
		try {
			socket.emit("game-join", mGameID);
			Log.i(TAG, "Connected to game " + mGameID);
		} catch (IOException e) {
			Log.e(TAG, "IOException in joinGame: " + e);
		}

	}

	@Override
	public void onMessage(String message) {
		Log.i(TAG, String.format("Received message %s.", message));
	}

	@Override
	public void onMessage(JSONObject json) {
		Log.i(TAG, String.format("Received JSON %s.", json));
	}

	@Override
	public void onConnect() {
		Log.i(TAG, "Socket onConnect() called.");
	}

	@Override
	public void onDisconnect() {
		//why on unregisterGPS?
		unregisterGPSReceiver();
		connected = false;
		if (!destroyed) {
			Log.e(TAG, "Lost connection to socket. Re-connecting.");
			reconnectSocket();
		} else {
			Log.i(TAG, "Connection closed on destroy.");
		}
	}
	
	public synchronized void sendMsg(String msg){
		//must be json, no check here
		try {
			socket.send(msg);
			Log.i(TAG, "information sent " + msg);
		} catch (IOException e) {
			Log.e(TAG, "IOException in sending: " + e);
		}
		
	}

	private synchronized void unregisterGPSReceiver() {
		if (mGPSReceiver == null) {
			return;
		}
		unregisterReceiver(mGPSReceiver);
		mGPSReceiver = null;
	}

	private synchronized void registerGPSReceiver() {
		if (mGPSReceiver != null) {
			return;
		}
		mGPSReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context ctxt, Intent intent) {
				double longitude = intent.getExtras().getDouble(
						GPSTrackingService.PARAM_LONGITUDE);
				double latitude = intent.getExtras().getDouble(
						GPSTrackingService.PARAM_LATITUDE);

				Log.i("Testing IO", String.format(
						"Received from local GPS: long:%f, lat:%f", longitude,
						latitude));
				JSONObject object = new JSONObject();

				try {
					object.put("longitude", longitude);
					object.put("latitude", latitude);
					String skill = mMyRoleString;
					object.put("skill", skill);
					object.put("player_id", mUserID);
					object.put("initials", mInitials);
					if (connected && socket != null) {
						Log.i("Testing IO", String.format(
								"Sending location-push %s. Skill is %s.",
								object, skill));
						socket.emit("location-push", object);
						socket.send("LOCATION EMITTED WITH LAT: "+latitude +", LONG: "+longitude);
					}

				} catch (JSONException e) {
					Log.e(TAG, "JSONException in gps push: " + e);
				} catch (IOException e) {
					Log.e(TAG, "IOException in gps push: " + e);
				}
			}
		};
		registerReceiver(mGPSReceiver, new IntentFilter(
				GPSTrackingService.GPS_INTENT));

	}

	/** The broadcast receiver used to push game data to the server. */
	private BroadcastReceiver mGPSReceiver = null;

}
