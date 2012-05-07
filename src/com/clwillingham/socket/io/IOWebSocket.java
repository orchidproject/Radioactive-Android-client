package com.clwillingham.socket.io;

import java.io.IOException;
import java.net.URI;
import java.nio.channels.NotYetConnectedException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import org.java_websocket.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

public class IOWebSocket extends WebSocketClient {

	private MessageCallback callback;
	private IOSocket ioSocket;
	private static int currentID = 0;
	private String namespace;

	public IOWebSocket(URI arg0, IOSocket ioSocket, MessageCallback callback) {
		super(arg0);
		this.callback = callback;
		this.ioSocket = ioSocket;
	}

	
	
	

	@Override
	public void onMessage(String arg0) {
		// TODO Auto-generated method stub
		System.out.println(arg0);
		IOMessage message = IOMessage.parseMsg(arg0);

		switch (message.getType()) {
		case IOMessage.HEARTBEAT:
			try {
				send("2::");
				System.out.println("HeartBeat written to server");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;

		case IOMessage.MESSAGE:
			callback.onMessage(message.getMessageData());
			break;

		case IOMessage.JSONMSG:
			try {
				callback.onMessage(new JSONObject(message.getMessageData()));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;

		case IOMessage.EVENT:
			try {
				JSONObject event = new JSONObject(message.getMessageData());

				String eventName = event.optString("name");
				JSONArray args = event.optJSONArray("args");

				if (args != null) {
					JSONObject[] argsArray = new JSONObject[args.length()];
					for (int i = 0; i < args.length(); i++) {
						argsArray[i] = args.getJSONObject(i);
					}
					callback.on(eventName, argsArray);
				} else {
					callback.on(eventName);
				}
				

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;

		case IOMessage.CONNECT:
			ioSocket.onConnect();
			break;

		case IOMessage.ACK:
		case IOMessage.ERROR:
		case IOMessage.DISCONNECT:
			// TODO
			break;
		}
	}

	@Override
	public void onOpen(ServerHandshake s){
		try {
			if (namespace != "")
				init(namespace);
		} catch (IOException e) {
			e.printStackTrace();
		}

		ioSocket.onOpen();
	}

	@Override
	//fake the interface by wenchaojiang
	public void onClose(int value1,String value2,boolean value3) {
		Log.d("IOWebSocket", "ioSocket.onClose()");
		ioSocket.onClose();
		ioSocket.onDisconnect();
	}

	public void init(String path) throws IOException {
		try {
			send("1::" + path);
		} catch (NotYetConnectedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void init(String path, String query) throws Exception {
		this.send("1::" + path + "?" + query);

	}

	public void sendMessage(IOMessage message) throws IOException {
		try {
			send(message.toString());
		} catch (Exception e) {
			Log.e("IOWebSocket", e.getMessage());
		}
	}

	public void sendMessage(String message) throws Exception {
		send(new Message(message).toString());
	}

	public static int genID() {
		currentID++;
		return currentID;

	}

	public void setNamespace(String ns) {
		namespace = ns;
	}

	public String getNamespace() {
		return namespace;
	}


	@Override
	public void onError(Exception arg0) {
		// TODO Auto-generated method stub
		arg0.printStackTrace();
	}

}
