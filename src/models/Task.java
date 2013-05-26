package models;

import org.json.JSONException;
import org.json.JSONObject;

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
    private int status;
	private float lat;
	private float lng;
	private int type;
	
	private GoogleMap mMap;
	private Marker mMarker;
	
	
	
	
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
		mMarker = map.addMarker(new MarkerOptions()
	     .position(new LatLng(lat, lng))
	     .icon(BitmapDescriptorFactory.fromBitmap(loader.getTaskImage(type)))
	     );
	}
	
	
	public void update(JSONObject su){
		try {
			status = su.getInt("state");
			lat = (float) su.getDouble("latitude");
			lng = (float) su.getDouble("longitude");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		mMarker.setPosition(new LatLng(lat, lng));
		
	}
	
	

	public int getId() {
		return id;
	}


	public LatLng getLatLng() {
	
		return new LatLng(lat,lng);
	}


}
