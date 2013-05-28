package com.geoloqi.ui;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import models.GameState;
import models.Player;
import models.Task;

import com.geoloqi.interfaces.StateCallback;
import com.geoloqi.mapattack.R;
import com.geoloqi.rpc.MapAttackClient;
import com.geoloqi.services.GPSTracker;
import com.geoloqi.services.SocketIOManager;
import com.geoloqi.widget.ImageLoader;
import com.geoloqi.widget.ImageLoader.Callback;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.CircleOptions;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.graphics.Bitmap;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

public class GameActivity extends FragmentActivity implements ActionBar.TabListener, StateCallback{

	private ActionBar.Tab mapTab;
	private ActionBar.Tab msgTab;
	private ActionBar.Tab testTab;
	private ActionBar.Tab taskTab;
	private GPSTracker gps = null;
	private SocketIOManager socketIO =  null;
	
	private MapFragment mMapFragment=null;
	private MsgListViewFragment mMsgFragment=null;
	private TestFragment mTestFragment = null;
	
	
	
	private ArrayList<Player> players = new ArrayList<Player>();
	private ArrayList<Task> tasks = new ArrayList<Task>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);
		
		// Set up the action bar to show tabs.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		mapTab = actionBar.newTab().setText("Map").setTabListener(this);
		msgTab = actionBar.newTab().setText("Message").setTabListener(this);
	    taskTab= actionBar.newTab().setText("Tasks").setTabListener(this);
	    testTab = actionBar.newTab().setText("Test").setTabListener(this);
		actionBar.addTab(mapTab);
		actionBar.addTab(msgTab);
		actionBar.addTab(taskTab);
		actionBar.addTab(testTab);
		
		gps = new GPSTracker(getApplicationContext());
		MapAttackClient mc = MapAttackClient.getApplicationClient(this);
		socketIO =  new SocketIOManager(this,mc.getGameId(),mc.getRoleId());
		
		
	}
	
	@Override
	public void onStart(){
		super.onStart();
		socketIO.connect();
		gps.start();
		GoogleMap map = mMapFragment.getMap();
		if (!GameState.getGameState(this).isLoaded()){
			MapAttackClient mc = MapAttackClient.getApplicationClient(this);
			GameState.getGameState(this).loadState(mc.getGameId());
		}
		
	}
	
	@Override
	public void onStop(){
		super.onStop();
		socketIO.disconnect();
		gps.stop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_game, menu);
		return true;
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction arg1) {
		
		
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
				//TODO probably better to use tag to retrive fregments
				FragmentManager fm= getSupportFragmentManager();
				if(tab == mapTab){
					if(mMapFragment == null){
						mMapFragment = new MapFragment();
						fm.beginTransaction().add(R.id.container,mMapFragment).commit();
					}
					else{
						//use show and hide to persist state
						fm.beginTransaction().show(mMapFragment).commit();
					}
					
				}
				else if (tab == msgTab){
					if(mMsgFragment == null){
						mMsgFragment = new MsgListViewFragment();
						mMsgFragment.setContext(this);
					    fm.beginTransaction().add(R.id.container,mMsgFragment).commit();
					}
					else{
						//use show and hide to persist state
						fm.beginTransaction().show(mMsgFragment).commit();
					}
					
				}
				else if (tab == taskTab){
					
				}
				else if(tab == testTab){
					if(mTestFragment == null){
						mTestFragment = new TestFragment();
						mTestFragment.setSocketIO(socketIO);
						mTestFragment.setContext(this);
						fm.beginTransaction().add(R.id.container,mTestFragment).commit();
					}
					else{
						//use show and hide to persist state
						fm.beginTransaction().show(mTestFragment).commit();
					}
					
				}
				
				
		
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction arg1) {
		FragmentManager fm= getSupportFragmentManager();
		if(tab == mapTab){		
				fm.beginTransaction().hide(mMapFragment).commit();	
		}
		else if (tab == msgTab){	
				fm.beginTransaction().hide(mMsgFragment).commit();	
		}
		else if (tab == testTab){
				fm.beginTransaction().hide(mTestFragment).commit();
		}
		else if (tab == taskTab){
			
		}
		
	}
	
	//it handles map behavior, 
	public  static class MapFragment extends Fragment{
			SupportMapFragment mapFragment ;
			

			@Override
			public View onCreateView(LayoutInflater inflater, ViewGroup container,
					Bundle savedInstanceState) {
				//this method is always called when tab selected
				
				View mContentView=inflater.inflate(R.layout.activity_game_fragment_map, container,false);
				
				
				mapFragment = new SupportMapFragment();
				getActivity().getSupportFragmentManager().beginTransaction()
				.replace(R.id.map_container, mapFragment).commit();
				
				
				return mContentView;
				
			}
			
			public GoogleMap getMap(){
				return mapFragment.getMap();
			}
			
			
			@Override
			public void onResume(){
				super.onResume();
			}
			
			@Override
			public void onStop(){
				super.onStop();
				
			}
			
			
			@Override
			public void onDestroy(){
				super.onDestroy();
			}
			
		}
	
	public  static class MsgFragment extends Fragment{
		

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			//this method is always called when tab selected
			
			View mContentView=inflater.inflate(R.layout.activity_game_fragment_msg, container,false);
			return mContentView;
		}
		
		
		@Override
		public void onResume(){
			super.onResume();
		}
		
		@Override
		public void onStop(){
			super.onStop();
			
		}
		
		
		@Override
		public void onDestroy(){
			super.onDestroy();
		}
		
	}

	
	//---------for game Logic here finally----------
	@Override
	public void update(JSONObject update) {
		
		if(update.optJSONObject("health")!=null){
			
		}
		else if(update.optJSONObject("location")!=null){
			JSONObject location =  update.optJSONObject("location");
			updatePlayer(location);
			
		}
		else if(update.optJSONObject("task")!=null){
			JSONObject task =  update.optJSONObject("task");
			updateTask(task);
		}
		
		
	}
	@Override
	public void bulkUpdate(JSONObject updates) {
		JSONArray ts = updates.optJSONArray("tasks");
		
		//dealing with tasks
		if(ts != null){
			for (int i = 0; i< ts.length(); i++){
				JSONObject update;
				try {
					update = ts.getJSONObject(i);
					Task t = getTaskById(update.getInt("id"));
					if(t == null){
						Task new_task =  new Task(update,mMapFragment.getMap());
						tasks.add(new_task);
					}
					else{
						t.update(update);
					}
				} catch (JSONException e) {
					Log.i("JSON info", "error parsing tasks");
				}
			}
		}
		

		JSONArray ps = updates.optJSONArray("players");
		
		//dealing with tasks
		if(ps != null){
			for (int i = 0; i< ps.length(); i++){
				JSONObject update;
				try {
					update = ps.getJSONObject(i);
					Player p = getPlayerById(update.getInt("id"));
					if(p == null){
						Player new_player =  new Player(update,mMapFragment.getMap());
						players.add(new_player);
					}
					else{
						p.update(update);
					}
				} catch (JSONException e) {
					Log.i("JSON info", "error parsing players");
				}		
			}
		}
		
		JSONArray dps = updates.optJSONArray("dropoffpoint");
		
		//dealing with tasks
		if(dps != null){
					for (int i = 0; i< dps.length(); i++){
						JSONObject update;
						try {
							update = dps.getJSONObject(i);
							double lat = update.getDouble("latitude");
							double lng = update.getDouble("longitude");
							mMapFragment.getMap().addCircle(new CircleOptions()
								.center(new LatLng(lat,lng))
								.radius(update.getInt("radius"))
							);
						} catch (JSONException e) {
							e.printStackTrace();
						}		
					}
		}
	}
	
	private Player getPlayerById(int id){
		Player player = null;
		for(Player p : players){
			if(p.getId() == id)
				player = p;
		}
		return player;
	}
	
	private Task getTaskById(int id){
		Task task  = null;
		for(Task t : tasks){
			if(t.getId() == id)
				task = t;
		}
		return task;
	}
	
	private void updatePlayer(JSONObject update){
		 Player p = getPlayerById(update.optInt("id"));
		 if(p == null){
				Player new_player =  new Player(update,mMapFragment.getMap());
				players.add(new_player);
		 }
		else{
				p.update(update);
		}
	}
	
	private void updateHealth(JSONObject update){
		//update UI
	}
	
	private void updateTask(JSONObject update){
		Task t = getTaskById(update.optInt("id"));
		if(t == null){
			Task new_task =  new Task(update,mMapFragment.getMap());
			tasks.add(new_task);
		}
		else{
			t.update(update);
		}
		
		String players =  update.optString("players");
		String[] carriers =  players.split(",");
		for (String carrier: carriers){
			// if(carrier == player id then update all the players 
		}
	}
	
	private void updatePlayerStatus(JSONObject update){
		Player p = getPlayerById(update.optInt("id"));
		if(p!=null){
			p.updateStatus(update);
		}
	}

	
	@Override
	public void setGameArea() {
		if(tasks.isEmpty()) return;
		
		LatLngBounds.Builder  builder = new LatLngBounds.Builder();
		for(Task t : tasks){
			builder.include(t.getLatLng());
		}
		mMapFragment.getMap().moveCamera(
				CameraUpdateFactory.newLatLngBounds(builder.build(), 50)
		);
	}
	


}
