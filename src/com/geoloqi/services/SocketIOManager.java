package com.geoloqi.services;

import java.io.IOException;
import java.nio.channels.NotYetConnectedException;
import java.util.Arrays;


import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;


import com.clwillingham.socket.io.IOSocket;
import com.clwillingham.socket.io.MessageCallback;
import com.geoloqi.interfaces.OrchidConstants;
import com.geoloqi.interfaces.StateCallback;



public class SocketIOManager implements OrchidConstants, MessageCallback{
	
	
	
	private int connectionState = 0; //state model 0 - not connected, 1 - connecting, 2 - connected, 3 - handshaked
	private IOSocket socket=null;
	private ConnectorThread connector =  null;
	private boolean testing = false;
	private StateCallback callback = null;
	/** The broadcast receiver used to push game data to the server. */
	private BroadcastReceiver mGPSReceiver = null;
	
	private int gameId;
	private String roleString;
	private int playerId;
	private String initials;
	private String name;
	
	private Context mContext=null;
	
	public SocketIOManager(StateCallback sc,int game_id, String role,String initials, int player_id, String player_name, Context con){
		callback = sc;
		this.gameId = game_id;
		this.roleString = role;
		this.playerId = player_id;
		this.initials= initials;
		this.name = player_name;
		this.mContext = con;
	}
	
	
	
	public void connect(){
		if(socket ==null){
			String url = "http://" + IOSOCKET_ADDRESS + ":" + IOSOCKET_PORT + "/";
			socket = new IOSocket(url, this);
		}
		
		if(connectionState == 0){
		    connector = new ConnectorThread(); 
		    connector.setDaemon(true);
			connector.start();
			connectionState = 1;
		}
		
		registerGPSReceiver();
		
	}
	
	public void disconnect(){
		
		//actively disconnect
		if(connectionState == 1){
			connector.reconnect = false;
			connector=null;
		}
		socket.disconnect();
		connectionState = 0;
		
		unregisterGPSReceiver();
	}

	
	class ConnectorThread extends Thread {
		public boolean reconnect;
		
		@Override
		public void run() {
			int counter = 0;
			reconnect = true;
			
			while ((connectionState==1) && reconnect) {
				try {
					counter++;
					socket.connect();
					connectionState = 2;
					Log.i("connection", "Connected to " + socket);
				} catch (IOException e) {
					connectionState = 1;
					e.getMessage();
				}
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.getMessage();
				}
				
				if (counter % 30 == 0) {
					Log.e("connection", String.format("Tried to connect for %d seconds, but no connection yet.",counter));
				}
			}
				
		}
		
	}
	
	
	private void joinGameChannel() {
		try {
			socket.emit("game-join", new JSONObject("{channel:"+gameId+",id:"+playerId+"}") );
			Log.i("SocketIOManager", "Connected to game " + 1);
			connectionState = 3;
			
		} catch (IOException e) {
			Log.e("SocketIOManager", "IOException in joinGame: " + e);
		}catch (JSONException e){
			Log.e("SocketIOManager", "JSON parse error in sendBackAck: " + e);
		}

	}
	
	//callbacks of socketIO.
	@Override
	public void on(String event, JSONObject... data) {
		// do handshake, other things give it to GameActivity for rendering.
		if (event == null) {
			Log.e("SocketIOManager", "Received null event.");
		} else {
			Log.i("SocketIOManager", String.format(
					"Received event %s from Web socket with objects %s", event,
					Arrays.toString(data)));
			if (event.equals("data")) {
				
				for (final JSONObject jsonObject : data) {
					Handler handler = new Handler(Looper.getMainLooper());
					//need to put it to mainthread
					handler.post(new Runnable(){

						@Override
						public void run() {
							callback.update(jsonObject);
						} 
					});
					
					
					/*try{
						//sendBackAck(jsonObject.getString("ackid"));
						jsonObject.put("time_stamp", (new Date()).getTime());
						//logWriter.appendLog(jsonObject.toString());
						
					}
					catch(Exception e){
						e.printStackTrace();
					}*/

				}
				
			} else if (event.equals("game")) {
				int counter = 0;
				boolean done = false;
				while (!done) {
					try {
						joinGameChannel();
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
				Log.w("SocketIOManager", "Could not handle event " + event);
			}
			
			
		}
	}

	@Override
	public void onMessage(String message) {}

	@Override
	public void onMessage(JSONObject json) {}

	@Override
	public void onConnect() {}

	@Override
	public void onDisconnect() {}
	
	/* ---- location listener -----*/
	private synchronized void unregisterGPSReceiver() {
		if (mGPSReceiver == null) {
			return;
		}
		mContext.unregisterReceiver(mGPSReceiver);
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
						GPSTracker.PARAM_LONGITUDE);
				double latitude = intent.getExtras().getDouble(
						GPSTracker.PARAM_LATITUDE);
				double accuracy = intent.getExtras().getDouble(
						GPSTracker.PARAM_ACCURACY);

				Log.i("location", String.format(
						"Received from local GPS: long:%f, lat:%f", longitude,
						latitude));
				JSONObject object = new JSONObject();

				try {
					object.put("longitude", longitude);
					object.put("latitude", latitude);
					object.put("accuracy", accuracy);

					object.put("skill", roleString);
					object.put("player_id", playerId);
					object.put("initials", initials);

					if (connectionState==3 && socket != null) {
						socket.emit("location-push", object);
						Toast.makeText(mContext, "location sent", Toast.LENGTH_SHORT).show();
					}

				} catch (JSONException e) {
					Log.e("location", "JSONException in gps push: " + e);
				} catch (Exception e) {
					Log.e("location", "IOException in gps push: " + e);
				}
			}

		
		};
		mContext.registerReceiver(mGPSReceiver, new IntentFilter(GPSTracker.GPS_INTENT));

	}

	
	
	
	//--- code for socket IO test-----
	
	public void startTest(float lat,float lng,int time){
		   
		   Log.i("Testing IO","starting");
		   if(connectionState == 3){
			   if(testing){
				   Log.i("Testing IO","test already started");
			   }
			   else{
				   testing=true;
				   TestThread tt= new TestThread();
				   tt.setLat(lat);
				   tt.setLng(lng);
				   tt.setInterval(time);
				   tt.start();
			   }
			   
		   }
		   else{
			   Log.i("Testing IO","socket io connection not estabilshed, test aborted");
		   }
	}
	
	public void stopTest() throws RemoteException { 
		   testing=false;
	}
	 
	public void sendMsg(String content){
			if(connectionState == 3){
				JSONObject messageObject = new JSONObject();
				try {
					messageObject.put("player_name",name );
					messageObject.put("player_initials", initials);
					messageObject.put("content", content);
					messageObject.put("player_skill", roleString);
					messageObject.put("player_id", playerId);
					int sent_time = (int) (System.currentTimeMillis()/1000);
					messageObject.put("timeStamp", sent_time);
					socket.emit("message", messageObject);
					 Log.i("Testing IO","msg data sent");
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
	}
	
	public void sendMsg(String content,int target1, int target2){
		if(connectionState == 3){
			JSONObject messageObject = new JSONObject();
			try {
				messageObject.put("player_name",name );
				messageObject.put("player_initials", initials);
				messageObject.put("content", content);
				messageObject.put("player_skill", roleString);
				messageObject.put("player_id", playerId);
				
				messageObject.put("target", target1);
				messageObject.put("target2", target2);
				
				int sent_time = (int) (System.currentTimeMillis()/1000);
				messageObject.put("timeStamp", sent_time);
				socket.emit("message", messageObject);
				Log.i("Testing IO","msg data sent");
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
}
	 
	 public void sendInstructionAck(int id, int status, int player_id){
			if(connectionState == 3){
				JSONObject object = new JSONObject();
				
				try {
					object.put("id", id);
					object.put("status", status);
					object.put("player_id", player_id);
					socket.emit("ack-instruction", object);
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
	}
	 
	public void sendMessageAck(int id, int player_id){
		
	}
	
	public void destroy(){
		testing=false;
		//prevent memo leak
		mContext = null;
	}
	
	private class TestThread extends Thread {
		float lat;
		float lng;
		int interval;
		public void setLat(float la){
			lat=la;
		}
		public void setLng(float ln){
			lng=ln;
		}
		public void setInterval(int inter){
			interval=inter;
		}
		
		@Override
		public void run() {
			while(testing){
				try {
					Log.i("Testing IO",String.format(
							"will sleep  %d.",
							interval));
					
					Thread.sleep(interval);
					
					
					JSONObject object = new JSONObject();
					object.put("longitude", lng);
					object.put("latitude", lat);
					object.put("accuracy", 1);
					
					
					object.put("skill", roleString);
					object.put("player_id", playerId);
					object.put("initials", initials);
					
					
					if (connectionState == 3) {
						Log.i("Testing IO", String.format(
								"Sending location-push %s. Skill is %s.",
								object, roleString));
						socket.emit("location-push", object);
						
						
					}else{
						Log.i("Testing IO", "No socket IO connection");
					}
				} catch (InterruptedException e) {
					testing=false;
					e.printStackTrace();
				} catch (JSONException e) {
					testing=false;
					e.printStackTrace();
				} catch (IOException e) {
					testing=false;
					e.printStackTrace();
				}
			}
		}
		
	}
}
