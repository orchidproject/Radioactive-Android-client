package com.geoloqi.widget;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.io.InputStream;

import models.GameState;

import org.json.JSONObject;

import com.geoloqi.interfaces.OrchidConstants;
import com.geoloqi.rpc.OrchidClient;
import com.geoloqi.widget.ImageLoader.Callback;


import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.AsyncTask;
import android.view.Display;
import android.view.WindowManager;

public class ImageLoader implements OrchidConstants{
	private Bitmap[] task = new Bitmap[4];
	//private Bitmap[] player = new Bitmap[4];
	private Bitmap tick;
	private Bitmap cross;
	private Bitmap self_icon;
	private HashMap<Integer, Bitmap> playerImages = new HashMap<Integer, Bitmap>();
	private HashMap<Integer, Bitmap> taskImages = new HashMap<Integer, Bitmap>();
	private int resize_factor = 0;
	
	private boolean loaded = false;
	private static ImageLoader singleton =null;
	//for the first time, it must be loaded in an async task
	public static ImageLoader getImageLoader(){
		if(singleton == null)
			singleton = new ImageLoader();
		return singleton;
	}
	
	public Bitmap getSelfIcon(){
		return self_icon;
	}
	public Bitmap getTaskImage(int type){
		return task[type];
	}
	
	public Bitmap getTick(){
		return tick;
	}
	
	public Bitmap getCross(){
		return cross;
	}
	
	public void adjustIconSize(Context context){
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		@SuppressWarnings("deprecation")
		int width = display.getWidth();
		resize_factor = width/15;
	}
	
	public void loadImages(){
		task[0] =  getBitmapFromURL(IMAGE_URL_BASE + "task_icon1.png");
		task[1] =  getBitmapFromURL(IMAGE_URL_BASE + "task_icon2.png");
		task[2] =  getBitmapFromURL(IMAGE_URL_BASE + "victim.png");
		task[3] =  getBitmapFromURL(IMAGE_URL_BASE + "task_icon4.png");
		self_icon =  getBitmapFromURL(IMAGE_URL_BASE + "blue_dot.png");
		self_icon =  Bitmap.createScaledBitmap( self_icon , resize_factor,resize_factor, true);
		/*
		 * player[0] =  getBitmapFromURL(IMAGE_URL_BASE + "medic.png");
		   player[1] =  getBitmapFromURL(IMAGE_URL_BASE + "firefighter.png");
		   player[2] =  getBitmapFromURL(IMAGE_URL_BASE + "soldier.png");
		   player[3] =  getBitmapFromURL(IMAGE_URL_BASE + "transporter.png");
		*/
		tick = getBitmapFromURL(IMAGE_URL_BASE + "tick.png");
		cross = getBitmapFromURL(IMAGE_URL_BASE + "dead.png");
	}
	
	private Bitmap getBitmapFromURL(String src) {
	    try {
	        URL url = new URL(src);
	        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	        connection.setDoInput(true);
	        connection.connect();
	        InputStream input = connection.getInputStream();
	        Bitmap myBitmap = BitmapFactory.decodeStream(input);
	        return myBitmap;
	    } catch (IOException e) {
	        e.printStackTrace();
	        return null;
	    }
	}
	
	public void loadPlayerImage(int id, String initials, String skill, Callback callback){
		Bitmap bm  = playerImages.get(id);
		
		if(bm!=null){
			callback.callback(bm);
			return;
		}
		
		LoadPlayerImageTask loader = new LoadPlayerImageTask(id,
				URL_BASE+"player/"+initials.substring(0, 1)+"/"+initials.substring(1, 2)
				+ "/" + skill
				+"/map_icon.png",
				callback);
		loader.execute();
	}
	
	public void loadTaskImage(int id, String initials, String type, Callback callback){
		Bitmap bm  = taskImages.get(id);
		if(bm!=null){
			callback.callback(bm);
			return;
		}
		
		LoadTaskImageTask loader = new LoadTaskImageTask(id,
				URL_BASE+"player/"+initials.substring(0, 1)+"/"+initials.substring(1, 2)
				+ "/" + type
				+"/map_icon.png",
				callback);
		loader.execute();
	}
	
	public void loadImage(String string, Callback callback) {
		new LoadImageTask(URL_BASE+"img/"+string,callback).execute();
	}
	
	
	
//<-----aync tasks --------------->	
	abstract static public class Callback {
		abstract public void callback(Bitmap bm);
	}
	
	private class LoadPlayerImageTask extends
		AsyncTask<Void, Void,Bitmap> {
		
		String url = null;
		int player_id;
		Callback callback = null;
		public LoadPlayerImageTask(Integer id, String url, Callback call){
			this.player_id = id;
			this.url = url;
			callback =  call;
		}

		@Override
		protected Bitmap doInBackground(Void... params) {
			Bitmap resized = Bitmap.createScaledBitmap(getBitmapFromURL(url), resize_factor, resize_factor, true);
			return resized;
		}

		@Override
		protected void onPostExecute(Bitmap res) {
				playerImages.put(player_id, res);
				callback.callback(res);
		}
	}
	
	private class LoadTaskImageTask extends
	AsyncTask<Void, Void,Bitmap> {
	
		String url = null;
		int task_id;
		Callback callback = null;
		public LoadTaskImageTask(Integer id, String url, Callback call){
			this.task_id = id;
			this.url = url;
			callback =  call;
		}

		@Override
		protected Bitmap doInBackground(Void... params) {
			Bitmap resized = Bitmap.createScaledBitmap(getBitmapFromURL(url), resize_factor,resize_factor, true);
			return resized;
		}
	
		@Override
		protected void onPostExecute(Bitmap res) {
			taskImages.put(task_id, res);
			callback.callback(res);
		}
	}
	
	private class LoadImageTask extends
	AsyncTask<Void, Void,Bitmap> {
	
		String url = null;
		Callback callback = null;
		public LoadImageTask( String url, Callback call){
			
			this.url = url;
			callback =  call;
		}

		@Override
		protected Bitmap doInBackground(Void... params) {
		
			Bitmap resized = Bitmap.createScaledBitmap(getBitmapFromURL(url), resize_factor, resize_factor, true);
			return resized;
		}
	
		@Override
		protected void onPostExecute(Bitmap res) {
			callback.callback(res);
		}
	}

}
