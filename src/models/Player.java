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
	
	public static Player newPlayerFromPlayerLocation(JSONObject su, GoogleMap map){
		final Player p = new Player(map);
		try {
			//null case player id end everthing
			//status = su.getInt("status");
			p.lat = (float) su.getDouble("latitude");
			p.lng = (float) su.getDouble("longitude");
			p.skill = su.getString("skill");
			p.id = su.getInt("player_id");
			p.initials = su.getString("initials");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	
		ImageLoader loader = ImageLoader.getImageLoader();
		loader.loadPlayerImage(p.id,p.initials, p.skill, 
				new ImageLoader.Callback() {
					
					@Override
					public void callback(Bitmap bm) {
						p.mMarker = p.mMap.addMarker(new MarkerOptions()
					     .position(new LatLng(p.lat, p.lng))
					     .icon(BitmapDescriptorFactory.fromBitmap(bm))
					     );
						
					}
				}
		);
		return p;
	}
	
	public static Player newPlayerFromPlayerInfo(JSONObject su, GoogleMap map){
		final Player p = new Player(map);
		boolean drawable = true;
		try {
			//defensive code
			//status = su.getInt("status");
			
			if(!Double.isNaN(su.optDouble("latitude")) ){
				p.lat = (float) su.getDouble("latitude");
			}else{
				drawable = false;
			}
			
			if(!Double.isNaN(su.optDouble("longitude")) ){
				p.lng = (float) su.getDouble("longitude");
			}
			else{
				drawable = false;
			}
			
			
			//skill, id, different from location object
			p.skill = roleMapping[su.getInt("skill")];
			p.id = su.getInt("id");
			
			p.initials = su.getString("initials");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	    
		//do not draw if the player do not have a location.
		if(!drawable) return p;
		
		ImageLoader loader = ImageLoader.getImageLoader();
		loader.loadPlayerImage(p.id,p.initials, p.skill, 
				new ImageLoader.Callback() {
					
					@Override
					public void callback(Bitmap bm) {
						p.mMarker = p.mMap.addMarker(new MarkerOptions()
					     .position(new LatLng(p.lat, p.lng))
					     .icon(BitmapDescriptorFactory.fromBitmap(bm))
					     );
						
					}
				}
		);
		return p;
	}
	private Player(GoogleMap map){
		mMap =  map;
	}
	
	
	
	public void update(JSONObject su){
		//any thing except id 
		try {
			lat = (float) su.getDouble("latitude");
			lng = (float) su.getDouble("longitude");
		} catch (JSONException e) {
			//no marker made
			return;
		}
		if(mMarker==null){
			
		}
		mMarker.setPosition(new LatLng(lat, lng));
	}
	
	public void updateStatus(JSONObject update){
		
		final String s = update.optString("status");
		String image = skill;
		if(s.equals("incapicatated")){
			image="dead";
		}
		else if(s.equals("normal")){
			image=skill;
		}
		
		if(mMarker!=null){
			final ImageLoader loader = ImageLoader.getImageLoader();
			loader.loadPlayerImage(id,initials, image, 
					new ImageLoader.Callback() {
						
						@Override
						public void callback(Bitmap bm) {
							mMarker.setIcon(BitmapDescriptorFactory.fromBitmap(bm));
							
							
						}
					}
			);
		}
	}
	

	public int getId() {
		return id;
	}

	public String getInitials() {
		return initials;
	}

	public String getSkill() {
		return skill;
	}
	
}
