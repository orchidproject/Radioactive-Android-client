package com.geoloqi.ui;

import com.geoloqi.mapattack.R;
import com.geoloqi.mapattack.R.layout;
import com.geoloqi.mapattack.R.menu;


import android.os.Bundle;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

public class GameActivity extends FragmentActivity implements ActionBar.TabListener{

	ActionBar.Tab mapTab;
	ActionBar.Tab msgTab;
	ActionBar.Tab testTab;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);
		
		// Set up the action bar to show tabs.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		mapTab = actionBar.newTab().setText("Map").setTabListener(this);
		msgTab = actionBar.newTab().setText("Message").setTabListener(this);
	    testTab= actionBar.newTab().setText("Test").setTabListener(this);
		actionBar.addTab(mapTab);
		actionBar.addTab(msgTab);
		actionBar.addTab(testTab);
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
				//mMapAdapter = new MapAdapter(MainActivity.this);
				//getChildFragmentManager().beginTransaction().replace(R.id.map_container, mMapAdapter.getMapFragment()).commit();

				//mMapAdapter.addAll(ItemCollection.getItemCollection().getData());
				//create mapAdapter and assign a map
				
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
