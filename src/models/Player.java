package models;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.widget.Toast;

import com.geoloqi.rpc.OrchidClient;
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
	private boolean isClient=false;
	
	
	public static final String[] roleMapping = {"medic","firefighter","soldier","transporter"};
	public static final String[] taskMapping = {};
	
	public static Player newPlayerFromPlayerLocation(JSONObject su, GoogleMap map, boolean isClient){
		final Player p = new Player(map);
		try {
			//null case player id end everthing
			//status = su.getInt("status");
			p.lat = (float) su.getDouble("latitude");
			p.lng = (float) su.getDouble("longitude");
			p.skill = su.getString("skill");
			p.id = su.getInt("player_id");
			p.initials = su.getString("initials");
			p.isClient = isClient;
		} catch (JSONException e) {
			e.printStackTrace();
		}
	
		p.mMarker = map.addMarker(new MarkerOptions().position(new LatLng(p.lat,p.lng)));
		p.setIcon(p.skill);
		return p;
	}
	
	public static Player newPlayerFromPlayerInfo(JSONObject su, GoogleMap map, boolean isClient){
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
			p.isClient = isClient;
			p.initials = su.getString("initials");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	    
		//do not draw if the player do not have a location.
		if(!drawable) return p;
		
		p.mMarker = map.addMarker(new MarkerOptions().position(new LatLng(p.lat,p.lng)));
		p.setIcon(p.skill);
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
			mMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(lat,lng)));
			setIcon(skill);
		}
		mMarker.setPosition(new LatLng(lat, lng));
	}
	
	public Marker updateOnStaticMap(GoogleMap sMap){
		Marker m = sMap.addMarker(new MarkerOptions().position(new LatLng(lat,lng)));
		setIconToMarker(skill, m);
		return m;
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
			setIcon(image);
		}
	}
	
	
	private void setIconToMarker(String image, final Marker m){
		if(m==null){
			throw new RuntimeException("marker is null");
		}
		final ImageLoader loader = ImageLoader.getImageLoader();
		//if(loader.getTick()==null){
			//for some reason, the self_icon can not be properly loaded, this is a fallback plan
			//Toast.makeText(context, "warning: tick icon is not loaded", Toast.LENGTH_SHORT).show();
		//}
		
		if(isClient){
			if(loader.getSelfIcon()==null){
				//for some reason, the self_icon can not be properly loaded, this is a fallback plan
				loader.loadImage("blue_dot",
						new ImageLoader.Callback() {
							@Override
							public void callback(Bitmap bm) {
								Bitmap resized = Bitmap.createScaledBitmap(bm, 20,20, true);
								m.setIcon(BitmapDescriptorFactory.fromBitmap(resized));
							}
						}
				);
				return;
			}
			m.setIcon(BitmapDescriptorFactory.fromBitmap(loader.getSelfIcon()));
			return;
		}
		
		loader.loadPlayerImage(id,initials, image, 
				new ImageLoader.Callback() {
					
					@Override
					public void callback(Bitmap bm) {
						//Bitmap resized = Bitmap.createScaledBitmap(bm, 100,100, true);
						m.setIcon(BitmapDescriptorFactory.fromBitmap(bm));
					}
				}
		);
		
	}
	private void setIcon(String image){
		if(mMarker==null){
			throw new RuntimeException("marker is null");
		}
		final ImageLoader loader = ImageLoader.getImageLoader();
		//if(loader.getTick()==null){
			//for some reason, the self_icon can not be properly loaded, this is a fallback plan
			//Toast.makeText(context, "warning: tick icon is not loaded", Toast.LENGTH_SHORT).show();
		//}
		
		if(isClient){
			if(loader.getSelfIcon()==null){
				//for some reason, the self_icon can not be properly loaded, this is a fallback plan
				loader.loadImage("blue_dot",
						new ImageLoader.Callback() {
							@Override
							public void callback(Bitmap bm) {
								Bitmap resized = Bitmap.createScaledBitmap(bm, 20,20, true);
								mMarker.setIcon(BitmapDescriptorFactory.fromBitmap(resized));
							}
						}
				);
				return;
			}
			mMarker.setIcon(BitmapDescriptorFactory.fromBitmap(loader.getSelfIcon()));
			return;
		}
		
		loader.loadPlayerImage(id,initials, image, 
				new ImageLoader.Callback() {
					
					@Override
					public void callback(Bitmap bm) {
						//Bitmap resized = Bitmap.createScaledBitmap(bm, 100,100, true);
						mMarker.setIcon(BitmapDescriptorFactory.fromBitmap(bm));
					}
				}
		);
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

	public LatLng getLatLng() {
		if(mMarker != null){
			return mMarker.getPosition();
		}else{
			return null;
		}
	}
	
}
