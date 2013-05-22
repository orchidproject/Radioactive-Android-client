package com.geoloqi.ui;

import com.geoloqi.mapattack.R;
import com.geoloqi.mapattack.R.layout;
import com.geoloqi.mapattack.R.menu;
import com.geoloqi.services.GPSTracker;
import com.geoloqi.services.SocketIOManager;
import com.google.android.gms.maps.SupportMapFragment;


import android.os.Bundle;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

public class GameActivity extends FragmentActivity implements ActionBar.TabListener{

	private ActionBar.Tab mapTab;
	private ActionBar.Tab msgTab;
	private ActionBar.Tab testTab;
	private GPSTracker gps = null;
	private SocketIOManager socketIO =  null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);
		
		// Set up the action bar to show tabs.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		mapTab = actionBar.newTab().setText("Map").setTabListener(this);
		msgTab = actionBar.newTab().setText("Message").setTabListener(this);
	    testTab= actionBar.newTab().setText("Tasks").setTabListener(this);
		actionBar.addTab(mapTab);
		actionBar.addTab(msgTab);
		actionBar.addTab(testTab);
		
		gps = new GPSTracker(getApplicationContext());
		socketIO =  new SocketIOManager();
	}
	@Override
	public void onStart(){
		super.onStart();
		socketIO.connect();
		gps.start();
		
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
		
				if(tab == mapTab){
					
					MapFragment mapFragment = new MapFragment();
					FragmentManager fm= getSupportFragmentManager();
					fm.beginTransaction().attach(mapFragment).add(R.id.container,mapFragment).commit();
					
				}
				else if (tab == msgTab){
					
				}
				else if (tab == testTab){
					
				}
		
	}

	@Override
	public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {
		// TODO Auto-generated method stub
		
	}
	
	//it handles map behavior, 
	public  static class MapFragment extends Fragment{
	
	
			
			
			
			public MapFragment() {
				
			}

			@Override
			public View onCreateView(LayoutInflater inflater, ViewGroup container,
					Bundle savedInstanceState) {
				//this method is always called when tab selected
				
				View mContentView=inflater.inflate(R.layout.activity_game_fragment_map, container,false);
				
				
				SupportMapFragment mapFragment = new SupportMapFragment();
				getActivity().getSupportFragmentManager().beginTransaction()
				.replace(R.id.map_container, mapFragment).commit();
				
				
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

}
