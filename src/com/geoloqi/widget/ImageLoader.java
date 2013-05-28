package com.geoloqi.widget;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStream;

import models.GameState;

import org.json.JSONObject;

import com.geoloqi.interfaces.OrchidConstants;


import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

public class ImageLoader implements OrchidConstants{
	private Bitmap[] task = new Bitmap[4];
	//private Bitmap[] player = new Bitmap[4];
	private Bitmap tick;
	private Bitmap cross;
	
	private boolean loaded = false;
	
	private static ImageLoader singleton =null;
	//for the first time, it must be loaded in an async task
	public static ImageLoader getImageLoader(){
		if(singleton == null)
			singleton = new ImageLoader();
		return singleton;
	}
	
	public ImageLoader(){
		loadImages();
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
	
	
	public void loadImages(){
		task[0] =  getBitmapFromURL(IMAGE_URL_BASE + "task_icon1.png");
		task[1] =  getBitmapFromURL(IMAGE_URL_BASE + "task_icon2.png");
		task[2] =  getBitmapFromURL(IMAGE_URL_BASE + "victim.png");
		task[3] =  getBitmapFromURL(IMAGE_URL_BASE + "task_icon4.png");
		/*player[0] =  getBitmapFromURL(IMAGE_URL_BASE + "medic.png");
		player[1] =  getBitmapFromURL(IMAGE_URL_BASE + "firefighter.png");
		player[2] =  getBitmapFromURL(IMAGE_URL_BASE + "soldier.png");
		player[3] =  getBitmapFromURL(IMAGE_URL_BASE + "transporter.png");*/
		//tick = getBitmapFromURL(IMAGE_URL_BASE + "tick.png");
		//cross = getBitmapFromURL(IMAGE_URL_BASE + "dead.png");
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
	
	public void loadPlayerImage(String initials, String skill, Callback callback){
		LoadImageTask loader = new LoadImageTask(
				URL_BASE+"player/"+skill.substring(0, 1)+"/"+skill.substring(1, 2)
				+ "/" + skill
				+"/map_icon.png",
				callback);
		loader.execute();
	}
	
	abstract static public class Callback {
		abstract public void callback(Bitmap bm);
		
	}
	private class LoadImageTask extends
		AsyncTask<Void, Void,Bitmap> {
		
		String url = null;
		Callback callback = null;
		public LoadImageTask(String url, Callback call){
			this.url = url;
			callback =  call;
		}

		@Override
		protected Bitmap doInBackground(Void... params) {
			
			return getBitmapFromURL(url);
		}

		@Override
		protected void onPostExecute(Bitmap res) {
				callback.callback(res);
		}
	}
}