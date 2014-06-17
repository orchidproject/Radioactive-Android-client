package com.geoloqi.services;

import java.util.ArrayList;

import android.widget.TextView;

public class Timer {
	private boolean started = false;
	private Thread counter = null;
	private ArrayList<Temp> listeners = new  ArrayList<Temp>();
	private static Timer single = null;
	public static Timer getInstance(){
		if (single == null){
			single =  new Timer();
		}
		return single;
	}
	
	private Timer(){}
	
	public static abstract class Temp{
		public abstract int getTimeStamp();
		public abstract TextView getTextView();
	}
	
	public void start(){
		if(counter == null){
			started = true;
			counter = new Thread(new Runnable(){
				
				public String caculateTime(int time_second){
					long timeStamp = System.currentTimeMillis();
					long sentTime =  time_second*1000;
					int difference = (int) (timeStamp-sentTime);
					int mins = difference/(60*1000);
					int sec = (difference-(mins*60*1000))/1000;
					return "sent "+ mins + " mins " +sec+ " secs"+ " ago";
				}

				@Override
				public void run() {
					while(started){
						for(final Temp te : listeners){
						   try {
							   Thread.sleep(5000);
						   } catch (InterruptedException e) {
							   e.printStackTrace();
						   }
				    	   te.getTextView().post(new Runnable() {
				    	  
				    		  public void run() {
				    			  te.getTextView().setText(caculateTime(te.getTimeStamp()));
				    		  }
				    	  });
					
						}
					}
					
				}
				
			});
			counter.start();
		}
	}
	public void stop(){
		started = false;
		counter = null;
	}
	
	public void addListener(Temp t){
		listeners.add(t);
	}

}
