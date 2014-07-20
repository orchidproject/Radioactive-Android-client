package models;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.geoloqi.interfaces.OrchidConstants;
import com.geoloqi.mapattack.R;
import com.geoloqi.widget.ImageLoader;
import com.geoloqi.widget.ImageLoader;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class Task implements OrchidConstants {
	//private static char[] char_mapping = {'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};
	private static String[] task_mapping = {"task_icon1","task_icon2","task_icon3","task_icon4"};
	
	private int id;
    private int status=0;//0- idle, 1- 2- 3- deflaut to 0
	private float lat;
	private float lng;
	private int type;
	
	//private GoogleMap mMap;
	private Marker mMarker;
	private boolean carried;
	private String initials;
	
	
	
	public Task(JSONObject su, final GoogleMap map){
		try {
			status = su.getInt("state");
			id = su.getInt("id");
			lat = (float) su.getDouble("latitude");
			lng = (float) su.getDouble("longitude");
			type = su.getInt("type");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		//map it to a string representation 
		int first = (this.id/10)%10;
		int second = this.id%10;
		this.initials =  first+""+second;
		
		//mMap =  map;
		ImageLoader loader = ImageLoader.getImageLoader();
		
		if(status==2){
			//assest loaded at the begining of the game
			Bitmap tick = loader.getTick();
			if(tick == null){
				loader.loadTaskImage(id,initials, "tick", 
						new ImageLoader.Callback() {
							
							@Override
							public void callback(Bitmap bm) {
								mMarker = map.addMarker(new MarkerOptions()
							     .position(new LatLng(lat, lng))
							     .icon(BitmapDescriptorFactory.fromBitmap(bm))
							     );
								
							}
						}
				);
			}
			else{
				mMarker = map.addMarker(new MarkerOptions()
					.position(new LatLng(lat, lng))
					.icon(BitmapDescriptorFactory.fromBitmap(tick))
				);
			}
		}
		else{
			//need dynamic loading
			loader.loadTaskImage(id,initials, task_mapping[type], 
					new ImageLoader.Callback() {
						
						@Override
						public void callback(Bitmap bm) {
							mMarker = map.addMarker(new MarkerOptions()
						     .position(new LatLng(lat, lng))
						     .icon(BitmapDescriptorFactory.fromBitmap(bm))
						     );
							
						}
					}
			);
		}
	}
	
	public String getTaskInitials(){
		return initials;
	}
	
	
	public void update(JSONObject su){
		int current_status=status;
		try {
			current_status = su.getInt("state");
			lat = (float) su.getDouble("latitude");
			lng = (float) su.getDouble("longitude");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		mMarker.setPosition(new LatLng(lat, lng));
		
		//mMap =  map;
		ImageLoader loader = ImageLoader.getImageLoader();
		if(status!=2 && current_status ==2){
		    status = current_status;
			Bitmap tick = ImageLoader.getImageLoader().getTick();
			if(tick == null){
				loader.loadTaskImage(id,initials, "tick", 
						new ImageLoader.Callback() {
							
							@Override
							public void callback(Bitmap bm) {
								mMarker.setIcon(BitmapDescriptorFactory.fromBitmap(bm));
								
							}
						}
				);
			}
			else{
				mMarker.setIcon(BitmapDescriptorFactory.fromBitmap(tick));
			}
		}
		
		
	}
	
	public Marker updateOnStaticMap(final GoogleMap map){
		
		final Marker m = map.addMarker(new MarkerOptions().position(new LatLng(lat,lng)));
		m.setPosition(new LatLng(lat, lng));
		
		//mMap =  map;
		ImageLoader loader = ImageLoader.getImageLoader();
		
		//need dynamic loading
		loader.loadTaskImage(id,initials, task_mapping[type], 
				new ImageLoader.Callback() {
					
					@Override
					public void callback(Bitmap bm) {
					     m.setIcon(BitmapDescriptorFactory.fromBitmap(bm));
					}
				}
		);
		return m;
			
	}
	
	private void setIconToMarker(String image, final Marker m){
		if(m==null){
			throw new RuntimeException("marker is null");
		}
		final ImageLoader loader = ImageLoader.getImageLoader();
		
		
		loader.loadPlayerImage(id,initials, image, 
				new ImageLoader.Callback() {
					
					@Override
					public void callback(Bitmap bm) {
						m.setIcon(BitmapDescriptorFactory.fromBitmap(bm));
					}
				}
		);
		
	}

	public int getId() {
		return id;
	}


	public LatLng getLatLng() {
	
		return new LatLng(lat,lng);
	}


	public boolean isCarried() {
		return carried;
	}


	public void setCarried(boolean carried) {
		this.carried = carried;
	}


	public int getType() {
		return type;
	}

	public boolean dropped() {
		if(status == 2){
			return true;
		}
		return false;
	}


}
