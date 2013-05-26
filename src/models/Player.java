package models;

import org.json.JSONException;
import org.json.JSONObject;

import com.geoloqi.widget.ImageLoader;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class Player {
	private int id;
	private int health;
	private float lat;
	private float lng;
	private int status;
	private String initials;
	
	private GoogleMap mMap;
	private Marker mMarker;
	
	public Player(JSONObject su, GoogleMap map){
		try {
			status = su.getInt("status");
			lat = (float) su.getDouble("latitude");
			lng = (float) su.getDouble("longitude");
			id = su.getInt("id");
			initials = su.getString("initials");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		mMap =  map;
		ImageLoader loader = ImageLoader.getImageLoader();
		mMarker = map.addMarker(new MarkerOptions()
	     .position(new LatLng(lat, lng))
	     .icon(BitmapDescriptorFactory.fromBitmap(loader.getTaskImage(0)))
	     );
	}
	
	public void update(JSONObject su){
		//any thing except id 
		try {
			status = su.getInt("status");
			lat = (float) su.getDouble("latitude");
			lng = (float) su.getDouble("longitude");
			id = su.getInt("id");
			initials = su.getString("initials");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		mMarker.setPosition(new LatLng(lat, lng));
	}

	public int getId() {
		return id;
	}
	
}
