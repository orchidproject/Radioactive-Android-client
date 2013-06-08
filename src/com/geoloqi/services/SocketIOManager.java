package com.geoloqi.services;

import java.io.IOException;
import java.nio.channels.NotYetConnectedException;
import java.util.Arrays;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.clwillingham.socket.io.IOSocket;
import com.clwillingham.socket.io.MessageCallback;
import com.geoloqi.interfaces.OrchidConstants;
import com.geoloqi.interfaces.StateCallback;
import com.geoloqi.services.IOSocketService.ConnectorThread;
import com.geoloqi.services.IOSocketService.TestThread;

public class SocketIOManager implements OrchidConstants, MessageCallback{
	private int connectionState = 0; //state model 0 - not connected, 1 - connecting, 2 - connected, 3 - handshaked
	private IOSocket socket=null;
	//private boolean initiateDisconnection = false;
	private ConnectorThread connector =  null;
	private boolean testing = false;
	private StateCallback callback = null;
	
	private int gameId;
	private String roleString;
	private int playerId;
	private String initials;
	
	public SocketIOManager(StateCallback sc,int game_id, String role,String initials, int player_id){
		callback = sc;
		this.gameId = game_id;
		this.roleString = role;
		this.playerId = player_id;
		this.initials= initials;
	}
	
	
	
	public void connect(){
		if(socket ==null){
			String url = "http://" + IOSOCKET_ADDRESS + ":" + IOSOCKET_PORT + "/";
			socket = new IOSocket(url, this);
		}
		//initiateDisconnection = false;
		
		if(connectionState == 0){
		    connector = new ConnectorThread(); 
		    connector.setDaemon(true);
			connector.start();
			connectionState = 1;
		}
		
	}
	
	public void disconnect(){
		
		//actively disconnect
		//initiateDisconnection = true;
		if(connectionState == 1){
			connector.reconnect = false;
			connector=null;
		}
		socket.disconnect();
		connectionState = 0;
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
	
	
	public void sendMsg(String string){
		//socket.
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
	public void onMessage(String message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMessage(JSONObject json) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnect() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDisconnect() {
		// TODO Auto-generated method stub
		
	}
	
	public void sendInstructionAck(int id, int status){
		if(connectionState == 3){
			JSONObject object = new JSONObject();
			
			try {
				object.put("id", id);
				object.put("status", status);
				socket.emit("ack-instruction", object);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
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
