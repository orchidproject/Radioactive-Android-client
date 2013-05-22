package com.geoloqi.services;

import java.io.IOException;

import org.json.JSONObject;

import android.util.Log;
import android.widget.Toast;

import com.clwillingham.socket.io.IOSocket;
import com.clwillingham.socket.io.MessageCallback;
import com.geoloqi.interfaces.GeoloqiConstants;
import com.geoloqi.services.IOSocketService.ConnectorThread;

public class SocketIOManager implements GeoloqiConstants, MessageCallback{
	private int connectionState = 0; //state model 0 - not connected, 1 - connecting, 2 - connected
	private IOSocket socket=null;
	//private boolean initiateDisconnection = false;
	private ConnectorThread connector =  null;
	
	
	
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
	//callbacks of socketIO.
	@Override
	public void on(String event, JSONObject... data) {
		// TODO Auto-generated method stub
		// do handshake, other things give it to GameActivity for rendering.
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
}
