package models;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;

import com.geoloqi.widget.ImageLoader;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class Player {
	private int id;
	private float lat;
	private float lng;
	private int status;
	private String initials;
	private String skill;
	
	private GoogleMap mMap;
	private Marker mMarker;
	
	
	public static final String[] roleMapping = {"medic","firefighter","soldier","transporter"};
	public static final String[] taskMapping = {};
	
	public Player(JSONObject su, GoogleMap map){
		try {
			//status = su.getInt("status");
			lat = (float) su.getDouble("latitude");
			lng = (float) su.getDouble("longitude");
			skill = su.getString("skill");
			id = su.getInt("player_id");
			initials = su.getString("initials");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		mMap =  map;
		ImageLoader loader = ImageLoader.getImageLoader();
		loader.loadPlayerImage(initials, skill, 
				new ImageLoader.Callback() {
					
					@Override
					public void callback(Bitmap bm) {
						mMarker = mMap.addMarker(new MarkerOptions()
					     .position(new LatLng(lat, lng))
					     .icon(BitmapDescriptorFactory.fromBitmap(bm))
					     );
						
					}
				}
		);
		
	}
	
	public void update(JSONObject su){
		//any thing except id 
		try {
			lat = (float) su.getDouble("latitude");
			lng = (float) su.getDouble("longitude");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		mMarker.setPosition(new LatLng(lat, lng));
	}
	
	public void updateStatus(JSONObject update){
		if(mMarker!=null){
			ImageLoader loader = ImageLoader.getImageLoader();
			loader.loadPlayerImage(initials, "dead", 
					new ImageLoader.Callback() {
						
						@Override
						public void callback(Bitmap bm) {
							mMarker.remove();
							mMarker = mMap.addMarker(new MarkerOptions()
						     .position(new LatLng(lat, lng))
						     .icon(BitmapDescriptorFactory.fromBitmap(bm))
						     );
							
							
						}
					}
			);
		}
	}
	

	public int getId() {
		return id;
	}
	
}
