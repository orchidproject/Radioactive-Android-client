package com.geoloqi.ui;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import models.GameState;
import models.InstructionV1;
import models.Player;
import models.Task;

import com.geoloqi.interfaces.StateCallback;
import com.geoloqi.mapattack.R;
import com.geoloqi.rpc.MapAttackClient;
import com.geoloqi.services.GPSTracker;
import com.geoloqi.services.SocketIOManager;
import com.geoloqi.widget.ImageLoader;
import com.geoloqi.widget.ImageLoader.Callback;
import com.geoloqi.widget.TaskArrayAdaptor;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.CircleOptions;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
	private PlanListViewFragment mTaskFragment = null;
	private TaskArrayAdaptor mTaskAdaptor = null;
	
	private View healthBar = null;
	private int healthBarWidth = 0;
	private TextView radiationView = null;
	private ImageView taskIndicator = null;
	
	private ArrayList<Player> players = new ArrayList<Player>();
	private ArrayList<Task> tasks = new ArrayList<Task>();
	
	boolean alertShown = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);
		
		// Set up the action bar to show tabs.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayShowHomeEnabled(false);
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
		socketIO =  new SocketIOManager(this,mc.getGameId(),
				mc.getRoleString(),
				mc.getInitials(),
				mc.getPlayerId()
		);
		
		mTaskAdaptor = new TaskArrayAdaptor(this,socketIO);
		
	}
	
	@Override
	public void onStart(){
		super.onStart();
		socketIO.connect();
		gps.start();
		GoogleMap map = mMapFragment.getMap();
		if (healthBar== null){
			
			
			healthBar = mMapFragment.getHealthBar();
			//record original width
			healthBarWidth =  healthBar.getLayoutParams().width;
			radiationView = mMapFragment.getRadiationView();
			taskIndicator = mMapFragment.getTaskIndicator();
			
		}
		
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
	public void onResume(){
		super.onResume();
	}
	
	private void test(){
		//test unit
				new AsyncTask<Void,Void,Integer>(){

					@Override
					protected Integer doInBackground(Void... params) {
						int count = 100;
					    int user_id = MapAttackClient.getApplicationClient(GameActivity.this).getPlayerId();
						Handler handler = new Handler(Looper.getMainLooper());
						while(count>0){
							//wait for map initialization
							if(mMapFragment.getMap()==null)
								continue;
							count -= 5;
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							final String json = "{\"player_id\":"+user_id+",\"value\":"+count+"}";
							final String json2 = "{\"player_id\":"+user_id+",\"value\":"+count+"}";
							final String json3;
							final String json4;
							if(count%10!=0){
								json3 = "{\"id\":100, \"latitude\":0, \"longitude\":0, \"type\": 0 ,\"players\":\"57,1\",\"state\": 1}";
								json4 = "{\"id\":51, \"status\": \"normal\"}";
							}
							else {
								json3 = "{\"id\":100, \"latitude\":0, \"longitude\":0, \"type\": 0 ,\"players\":\"49,1\",\"state\": 2}";
								json4 = "{\"id\":51, \"status\": \"incapicatated\"}";
							}
							final String json5;
							if(count%10==0){
								json5 = "[{\"id\":51, \"status\": 1,\"time\": 10000,\"teammate\": 50,\"task\": \"02\",\"direction\": \"southeast\" }]";
							}
							else{
								json5 = "[{\"id\":51, \"status\": 1,\"time\": 10000,\"teammate\": 58,\"task\": \"01\",\"direction\": \"southeast\" }]";
							}
							final int c = count;
							//need to put it to mainthread
							handler.post(new Runnable(){

								@Override
								public void run(){
									
									try {
										
										updateHealth(new JSONObject(json));
										updateExposure(new JSONObject(json2));
										updateTask(new JSONObject(json3));
										updatePlayerStatus(new JSONObject(json4));
										if (c%10==0){
											//updateInstructions(new JSONArray(json5));
										}
									} catch (JSONException e) {
										e.printStackTrace();
									}
								} 
							});
						}
						return 1;
					}
					
				}.execute();
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
					    fm.beginTransaction().add(R.id.container,mMsgFragment).commit();
					}
					else{
						//use show and hide to persist state
						fm.beginTransaction().show(mMsgFragment).commit();
					}
					
				}
				else if (tab == taskTab){
					if(mTaskFragment == null){
						mTaskFragment = new PlanListViewFragment();
						mTaskFragment.setListAdapter(mTaskAdaptor);
						fm.beginTransaction().add(R.id.container,mTaskFragment).commit();
					}
					else{
						//use show and hide to persist state
						fm.beginTransaction().show(mTaskFragment).commit();
					}
					
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
				fm.beginTransaction().hide(mTaskFragment).commit();
		}
		
	}
	
	//it handles map behavior, 
	public  static class MapFragment extends Fragment{
			SupportMapFragment mapFragment ;
			ImageView imgView;

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
			@Override
			public void onResume(){
				super.onResume();
				if (imgView != null)
						return;
			    imgView = (ImageView) getView().findViewById(R.id.role_image);
			    MapAttackClient mc = MapAttackClient.getApplicationClient(getActivity());
			    int player_id = mc.getPlayerId();
			    String initials = mc.getInitials();
			    String role = mc.getRoleString();
				ImageLoader.getImageLoader().loadPlayerImage(player_id,initials,role,new ImageLoader.Callback() {
					
					@Override
					public void callback(Bitmap bm) {
						imgView.setImageBitmap(bm);
					}
				});
				
			}
			
			
			public ImageView getTaskIndicator(){
				if(getView()!=null)
					return (ImageView) getView().findViewById(R.id.task_indicator);
				return null;
			}
			
			public View getHealthBar(){
				if(getView()!=null)
					return getView().findViewById(R.id.health_bar);
				return null;
			}
			
			public TextView getRadiationView(){
				if(getView()!=null)
					return (TextView) getView().findViewById(R.id.radiation_panel);
				return null;
			}
			
			public GoogleMap getMap(){
				return mapFragment.getMap();
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
		
	}
	
/*	public  static class TaskFragment extends Fragment{
		

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			//this method is always called when tab selected
			
			View mContentView=inflater.inflate(R.layout.activity_game_fragment_task, container,false);
			return mContentView;
		}
		
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			final Button accept = (Button) getView().findViewById(R.id.accept_task);
			//final Button reject = (Button) getView().findViewById(R.id.reject_task);
			final TextView  status = (TextView) getView().findViewById(R.id.task_status);
			accept.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					accept.setEnabled(false);
					reject.setEnabled(true);
					status.setText("you have accepted the task");
					
				}
				
			});
			
			//reject.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					reject.setEnabled(false);
					accept.setEnabled(true);
					status.setText("you have rejected the task");
				}
				
			});
			
		}
		
	}
*/

	
	//---------for game Logic here finally----------
	@Override
	public void update(JSONObject update) {
		
		if(update.optJSONObject("health")!=null){
			JSONObject health =  update.optJSONObject("health");
			updateHealth(health);
		}
		else if(update.optJSONObject("location")!=null){
			JSONObject location =  update.optJSONObject("location");
			updatePlayer(location);
			
		}
		else if(update.optJSONObject("task")!=null){
			JSONObject task =  update.optJSONObject("task");
			updateTask(task);
		}
		else if(update.optJSONObject("player")!=null){
			
		}
		else if(update.optJSONObject("exposure")!=null){
			JSONObject exposure =  update.optJSONObject("exposure");
			updateExposure(exposure);
		}
		//may have mutiple instructions
		else if(update.optJSONArray("instructions")!=null){
			JSONArray array = update.optJSONArray("instructions");
			updateInstructions(array);
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
						Player new_player =  Player.newPlayerFromPlayerInfo(update,mMapFragment.getMap());
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
		
		JSONArray dps = updates.optJSONArray("dropoffpoints");
		
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
								.fillColor(0x330000FF)
								.strokeColor(0x330000FF)
							);
						} catch (JSONException e) {
							e.printStackTrace();
						}		
					}
		}
		test();
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
		 Player p = getPlayerById(update.optInt("player_id"));
		 if(p == null){
				Player new_player =  Player.newPlayerFromPlayerLocation(update,mMapFragment.getMap());
				players.add(new_player);
		 }
		else{
				p.update(update);
		}
	}
	
	private void updateHealth(JSONObject update){
		int user_id = MapAttackClient.getApplicationClient(this).getPlayerId();
		
		if(user_id == update.optInt("player_id")){
			Double value = update.optDouble("value");
			ViewGroup.LayoutParams params = healthBar.getLayoutParams();
			params.width = (int) ((value/100.0)*healthBarWidth);
			healthBar.setLayoutParams(params);
		}
	}
	
	private void updateExposure(JSONObject update){
		int user_id = MapAttackClient.getApplicationClient(this).getPlayerId();
		
		if(user_id == update.optInt("player_id")){
			Integer value = update.optInt("value");
			radiationView.setText(value+"");
		}
	}
	
	private void updateTask(JSONObject update){
		Task t = getTaskById(update.optInt("id"));
		if(t == null){
			t =  new Task(update,mMapFragment.getMap());
			tasks.add(t);
		}
		else{
			t.update(update);
		}
		
		
		//set Indicators
		String players =  update.optString("players");
		String[] carriers =  players.split(",");
		MapAttackClient mc = MapAttackClient.getApplicationClient(this);
		
		boolean carrying = false;
		for (String carrier: carriers){
			if(Integer.parseInt(carrier) == mc.getPlayerId()){
				carrying=true;		
				break;
			}
		}
		
		if(t.isCarried()&&!carrying){
			// used to carry but not carrying
			taskIndicator.setImageBitmap(null);
			t.setCarried(false);
			return;
		}else if(carrying&&!t.isCarried()){
			Bitmap bm = ImageLoader.getImageLoader().getTaskImage(t.getType());
			taskIndicator.setImageBitmap(bm);
			t.setCarried(true);
		}
		
	}
	
	private void updatePlayerStatus(JSONObject update){
		Player p = getPlayerById(update.optInt("id"));
		if(p!=null){
			p.updateStatus(update);
		}
	}
	
	private void updateInstructions(JSONArray update){
		//the adaptor need to be move outside, making it controlled by GameActivity.
		 
		mTaskAdaptor.clear();
		for (int i=0; i<update.length();i++){
			JSONObject in = update.optJSONObject(i);
			if(in == null){
				Toast.makeText(this, "error constructing instructions", Toast.LENGTH_LONG).show();
				continue;
			}
			
			int status = in.optInt("status");
			int id = in.optInt("id");
			String task = in.optString("task");
			String direction = in.optString("direction");
			int teammate = in.optInt("teammate");
			int time = in.optInt("time");
			Player player = getPlayerById(teammate);
			mTaskAdaptor.add(new InstructionV1(id,time,status,direction,player,task));
			
			if(!alertShown){
				alertShown = true;
			}
			else{
				return;
			}
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					this);
	 
			// set title
			alertDialogBuilder.setTitle("New instruction");
			
			// set dialog message
			alertDialogBuilder
					.setMessage("Click to view the new instruction")
					.setCancelable(false)
					.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,int id) {
							alertShown = false;
							taskTab.select();
							
						}
					  })
					.create()
					.show();
				
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
