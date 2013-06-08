package models;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;

import com.geoloqi.interfaces.OrchidConstants;
import com.geoloqi.widget.ImageLoader;
import com.geoloqi.widget.ImageLoader;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class Task implements OrchidConstants {
	private int id;
    private int status=0;//0- idle, 1- 2- 3- deflaut to 0
	private float lat;
	private float lng;
	private int type;
	
	private GoogleMap mMap;
	private Marker mMarker;
	private boolean carried;
	
	
	
	
	public Task(JSONObject su, GoogleMap map){
		try {
			status = su.getInt("state");
			id = su.getInt("id");
			lat = (float) su.getDouble("latitude");
			lng = (float) su.getDouble("longitude");
			type = su.getInt("type");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		mMap =  map;
		ImageLoader loader = ImageLoader.getImageLoader();
		
		Bitmap bm;
		if(status==2)
			bm = loader.getTick();
		else
			bm = loader.getTaskImage(type);
			
		mMarker = map.addMarker(new MarkerOptions()
	     .position(new LatLng(lat, lng))
	     .icon(BitmapDescriptorFactory.fromBitmap(bm))
	     );
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
		
		if(status!=2 && current_status ==2){
		    status = current_status;
			Bitmap tick = ImageLoader.getImageLoader().getTick();
			mMarker.setIcon(BitmapDescriptorFactory.fromBitmap(tick));
		}
		
		
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


}
