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
import com.geoloqi.rpc.OrchidClient;
import com.geoloqi.services.GPSTracker;
import com.geoloqi.services.SocketIOManager;
import com.geoloqi.widget.ImageLoader;
import com.geoloqi.widget.MsgArrayAdaptor;
import com.geoloqi.widget.TaskArrayAdaptor;
import com.geoloqi.widget.TaskMsgArrayAdaptor;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class GameActivity extends FragmentActivity implements ActionBar.TabListener, StateCallback{

	public static boolean testMode = false;
	public static boolean sensorEnabled = false;
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
	private TextView taskTextIndicator = null;
	
	private ArrayList<Player> players = new ArrayList<Player>();
	private ArrayList<Task> tasks = new ArrayList<Task>();
	
	private ProgressDialog mProgress;
	
	boolean alertShown = false;
	private MsgArrayAdaptor mMsgViewAdaptor;
	private int msgCount = 0;
	private TaskMsgArrayAdaptor mMsgTaskAdaptor;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);
		 getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		
		// Set up the action bar to show tabs.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayShowHomeEnabled(false);
		mapTab = actionBar.newTab().setText("Map").setTabListener(this);
		msgTab = actionBar.newTab().setText("Message").setTabListener(this);
	    taskTab= actionBar.newTab().setText("Tasks").setTabListener(this);
	   
		actionBar.addTab(mapTab);
		actionBar.addTab(msgTab);
		actionBar.addTab(taskTab);
		if(testMode){
			testTab = actionBar.newTab().setText("Test").setTabListener(this);
			actionBar.addTab(testTab);
		}
		
		
		
		gps = new GPSTracker(getApplicationContext());
		OrchidClient mc = OrchidClient.getApplicationClient(this);
		socketIO =  new SocketIOManager(this,mc.getGameId(),
				mc.getRoleString(),
				mc.getInitials(),
				mc.getPlayerId(),
				this
		);
		
		mTaskAdaptor = new TaskArrayAdaptor(this,socketIO);
		mMsgTaskAdaptor = new TaskMsgArrayAdaptor(this);
		mMsgViewAdaptor = new MsgArrayAdaptor(this);
		
	}
	
	@Override
	public void onStart(){
		super.onStart();
		socketIO.connect();
		gps.start();
		if (healthBar== null){
			
			healthBar = mMapFragment.getHealthBar();
			//record original width
			healthBarWidth =  healthBar.getLayoutParams().width;
			radiationView = mMapFragment.getRadiationView();
			taskIndicator = mMapFragment.getTaskIndicator();
			taskTextIndicator = mMapFragment.getTaskTextIndicator();
			mMapFragment.getFindMeButton().setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					findMe();
				}
			});
			mMapFragment.getGameAreaButton().setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					setGameArea();
				}
			});
			
		}
		OrchidClient mc = OrchidClient.getApplicationClient(this);
		GameState.getGameState(this).loadState(mc.getGameId());
		
		
			
	}
	
	@Override
	public void onStop(){
		super.onStop();
		//Timer.getInstance().stop();
		socketIO.disconnect();
		gps.stop();
		finish();
	}
	
	@Override
	public void onDestroy(){
		socketIO.destroy();
		gps.destroy();
		super.onDestroy();
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
	
	
	
	private void test(){
		//test unit
				new AsyncTask<Void,Void,Integer>(){

					@Override
					protected Integer doInBackground(Void... params) {
						int count = 100;
					    int user_id = OrchidClient.getApplicationClient(GameActivity.this).getPlayerId();
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
							/*final String json5;
							if(count%10==0){
								json5 = "[{\"id\":51, \"status\": 1,\"time\": 10000,\"teammate\": 50,\"task\": \"02\",\"direction\": \"southeast\" }]";
							}
							else{
								json5 = "[{\"id\":51, \"status\": 1,\"time\": 10000,\"teammate\": 58,\"task\": \"01\",\"direction\": \"southeast\" }]";
							}*/
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
						mMsgFragment.setAdaptor(mMsgViewAdaptor);
						mMsgFragment.setSocket(socketIO);
					    fm.beginTransaction().add(R.id.container,mMsgFragment).commit();
					}
					else{
						//use show and hide to persist state
						fm.beginTransaction().show(mMsgFragment).commit();
					}
					msgCount = 0;
					msgTab.setText("MESSAGE("+msgCount+")");
					
				}
				else if (tab == taskTab){
					if(mTaskFragment == null){
						mTaskFragment = new PlanListViewFragment();
						mTaskFragment.setListAdapter(mTaskAdaptor);
						mTaskFragment.setMsgAdapter(mMsgTaskAdaptor);
						mTaskFragment.setSocket(socketIO);
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
		//dismiss keyboard if any
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		
		FragmentManager fm= getSupportFragmentManager();
		if(tab == mapTab){	
				
				fm.beginTransaction().hide(mMapFragment).commit();	
		}
		else if (tab == msgTab){	
				imm.hideSoftInputFromWindow(mMsgFragment.getView().getWindowToken(), 0);
				fm.beginTransaction().hide(mMsgFragment).commit();	
		}
		else if (tab == testTab){
				imm.hideSoftInputFromWindow(mTestFragment.getView().getWindowToken(), 0);
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
				
				
				mapFragment = SupportMapFragment.newInstance(new GoogleMapOptions().mapType(1));
				
				getActivity().getSupportFragmentManager().beginTransaction()
				.replace(R.id.map_container, mapFragment).commit();
				
				
				
				return mContentView;
				
			}
			public Button getGameAreaButton() {
				if(getView()!=null)
					return (Button) getView().findViewById(R.id.btn_find_game_area);
				return null;
			}
			public Button getFindMeButton() {
				if(getView()!=null)
					return (Button) getView().findViewById(R.id.btn_find_me);
				return null;
			}
			@Override
			public void onResume(){
				super.onResume();
				if (imgView != null)
						return;
			    imgView = (ImageView) getView().findViewById(R.id.role_image);
			    OrchidClient mc = OrchidClient.getApplicationClient(getActivity());
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
			
			public TextView getTaskTextIndicator(){
				if(getView()!=null)
					return (TextView) getView().findViewById(R.id.task_text_indicator);
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
				mapFragment.getMap().setIndoorEnabled(false);
				return mapFragment.getMap();
			}
			
			
	}
	
	
	
	//---------for game Logic here finally----------
	public void update(JSONObject update) {
		
		if(update.optJSONObject("health")!=null){
			JSONObject health =  update.optJSONObject("health");
			updateHealth(health);
		}
		else if(update.optJSONObject("location")!=null){
			JSONObject location =  update.optJSONObject("location");
			updatePlayer(location);
			
		}
		else if(update.optJSONObject("message")!=null){
			JSONObject msg =  update.optJSONObject("message");
			updateMsg(msg);
			
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
		else if(update.optJSONObject("ack-instruction")!=null){
			JSONObject ack = update.optJSONObject("ack-instruction");
			updateInstructionAck(ack);
		}
		else if(update.optJSONObject("cleanup")!=null){
			cleanUpPlayer(update.optJSONObject("cleanup"));
		}
		else if(update.optString("system",null)!=null){
			updateSystemInfo(update.optString("system"));
		}
		
		
		
	}
	

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
						boolean isClient = false;
						if(OrchidClient.getApplicationClient(this).getPlayerId() == update.optInt("id")){
							isClient = true;
						}
						Player new_player =  Player.newPlayerFromPlayerInfo(update,mMapFragment.getMap(),isClient);
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
							int id = update.getInt("id");
							final double lat = update.getDouble("latitude");
							final double lng = update.getDouble("longitude");
							mMapFragment.getMap().addCircle(new CircleOptions()
								.center(new LatLng(lat,lng))
								.radius(update.getInt("radius"))
								.fillColor(0x330000FF)
								.strokeColor(0x330000FF)
							);
							ImageLoader loader = ImageLoader.getImageLoader();
					
							loader.loadImage("large_number/"+(id%10)+".png",
									new ImageLoader.Callback() {
										@Override
										public void callback(Bitmap bm) {
											mMapFragment.getMap().addMarker(new MarkerOptions()
												.position(new LatLng(lat,lng)).icon(BitmapDescriptorFactory.fromBitmap(bm)));
					
										}
									}
							);
							
						} catch (JSONException e) {
							e.printStackTrace();
						}		
				}
		}
		
		//manually push an instruciton
		Player p = getPlayerById(OrchidClient.getApplicationClient(this).getPlayerId());
		InstructionV1 fake_instruction = new InstructionV1(-1,0,1,"none",p,-1,-1);
		mTaskAdaptor.add(fake_instruction);
		JSONArray ins = updates.optJSONArray("instructions");
		updateInstructions(ins);
		//test();
	}
	
	private void cleanUpPlayer(JSONObject player_to_delete){
		int id = player_to_delete.optInt("player_id");
		if(OrchidClient.getApplicationClient(this).getPlayerId() == id){
			OrchidClient.getApplicationClient(this).logout();
			Toast.makeText(this, "you are foced to logout", Toast.LENGTH_LONG).show();
			finish();
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
		 Player p = getPlayerById(update.optInt("player_id"));
		 if(p == null){
			 	boolean isClient = false;
				if(OrchidClient.getApplicationClient(this).getPlayerId() == update.optInt("id")){
					isClient = true;
				}
				Player new_player =  Player.newPlayerFromPlayerLocation(update,mMapFragment.getMap(),isClient);
				players.add(new_player);
				
		 }
		else{
				p.update(update);
		}
	}
	
	private void updateHealth(JSONObject update){
		int user_id = OrchidClient.getApplicationClient(this).getPlayerId();
		
		if(user_id == update.optInt("player_id")){
			Double value = update.optDouble("value");
			ViewGroup.LayoutParams params = healthBar.getLayoutParams();
			params.width = (int) ((value/100.0)*healthBarWidth);
			healthBar.setLayoutParams(params);
		}
	}
	
	private void updateExposure(JSONObject update){
		int user_id = OrchidClient.getApplicationClient(this).getPlayerId();
		
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
		OrchidClient mc = OrchidClient.getApplicationClient(this);
		
		boolean carrying = false;
		if(carriers.length==2){
			for (String carrier: carriers){
				if(Integer.parseInt(carrier) == mc.getPlayerId()){
					carrying=true;		
					break;
				}
			}
		}
		
		if(t.isCarried()&&!carrying){
			// used to carry but not carrying
			taskIndicator.setImageBitmap(null);
			taskTextIndicator.setText("Picked up: None");
			t.setCarried(false);
			return;
		}else if(carrying&&!t.isCarried()){
			Bitmap bm = ImageLoader.getImageLoader().getTaskImage(t.getType());
			taskIndicator.setImageBitmap(bm);
			t.setCarried(true);
			taskTextIndicator.setText("Picked up: ");
		}
		
	}
	
	private void updatePlayerStatus(JSONObject update){
		Player p = getPlayerById(update.optInt("id"));
		if(p!=null){
			p.updateStatus(update);
		}
	}
	
	private void updateInstructionAck(JSONObject update){
		
		Player p = getPlayerById(update.optInt("player_id"));
		//assume only one instruction is in the adaptor
		if(p!=null){
			mTaskAdaptor.ackInstructionV1(update.optInt("id"),p,update.optInt("status"));
		}
		
		
	}
	private void updateSystemInfo(String msg){
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				this);
 
			// set title
			alertDialogBuilder.setTitle("System message");
		
			// set dialog message
			alertDialogBuilder
				.setMessage(msg)
				.setCancelable(true)
				.setPositiveButton("Dismiss",new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,int id) {
								//something?
								
							}
						  })
				.create()
				.show();
	}
	
	private void updateMsg(final JSONObject update){
		ActionBar actionBar = getActionBar();
		int target1 = update.optInt("target");
		int target2 = update.optInt("target2");
		int pid = OrchidClient.getApplicationClient(this).getPlayerId();
		
		if( target1 == 0 && target2 == 0 ){
			mMsgViewAdaptor.add(update);
			if(actionBar.getSelectedTab()!=msgTab){
				msgTab.setText("MESSAGE("+(++msgCount)+")");
			}
		}
		else if (target1 == pid || target2==pid) {
			mMsgTaskAdaptor.add(update);
		}
		
		//add code for pop up
	}
	
	private void updateInstructions(JSONArray update){
		if(update == null){
			return;
		}
		
		for (int i=0; i<update.length();i++){
			JSONObject in = update.optJSONObject(i);
			if(in == null){
				Toast.makeText(this, "error constructing instructions", Toast.LENGTH_LONG).show();
				continue;
			}
			int confirmed = in.optInt("confirmed");
			if(confirmed != 1){
				return;
			}
			
			int status = in.optInt("status");
			int id = in.optInt("id");
			int task = in.optInt("task");
			String direction = in.optString("direction");
			int teammate = in.optInt("teammate");
			int time = in.optInt("time");
			int player_id = in.optInt("player_id");
			Player player = getPlayerById(teammate);
			
			InstructionV1 instruction = new InstructionV1(id,time,status,direction,player,task,player_id);
			//Toast.makeText(this, player_id + "<-instruciton for player", Toast.LENGTH_SHORT ).show();
			int k = OrchidClient.getApplicationClient(this).getPlayerId();
			if(OrchidClient.getApplicationClient(this).getPlayerId() == player_id){
				mMsgTaskAdaptor.clear();
				mTaskAdaptor.clear();
				mTaskAdaptor.add(instruction);
				if(!alertShown){
					alertShown = true;
				}
				else{
					return;
				}
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		 
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
			else if(OrchidClient.getApplicationClient(this).getPlayerId() == teammate){
				mTaskAdaptor.setPeerInstruction(instruction);
			}
			
			
				
		}
		
	}
	
	public void findMe() {
		if(players.isEmpty()) return;
		
		
		
		int player_id = OrchidClient.getApplicationClient(this).getPlayerId();
		
		Player current_player = getPlayerById(player_id);
		if(current_player==null){
			Toast.makeText(this, "Error,player not fund", Toast.LENGTH_SHORT).show();
			return;
		}
		else if(current_player.getLatLng()==null){
			Toast.makeText(this, "Player location not updated, please wait", Toast.LENGTH_SHORT).show();
			return;
		}
		mMapFragment.getMap().moveCamera(
				CameraUpdateFactory.newLatLng(current_player.getLatLng())
		);
	}

	
	
	public void setGameArea() {
		if(tasks.isEmpty()) return;
		
		LatLngBounds.Builder  builder = new LatLngBounds.Builder();
		for(Task t : tasks){
			builder.include(t.getLatLng());
		}
		
		int player_id = OrchidClient.getApplicationClient(this).getPlayerId();
		Player current_player = getPlayerById(player_id);
		if(current_player!=null&&current_player.getLatLng()!=null){
			builder.include(current_player.getLatLng());
		}
		mMapFragment.getMap().moveCamera(
				CameraUpdateFactory.newLatLngBounds(builder.build(), 50)
		);
	}

	@Override
	public void preLoad() {
		//join game here
		mProgress = ProgressDialog.show(this,null,"Loading game...",false,false);
	}

	@Override
	public void afterLoad(JSONObject updates) {
		bulkUpdate(updates);
		setGameArea();
		if(mProgress!=null){
			mProgress.dismiss();
			mProgress = null;	
		}
		
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    // Handle the back button
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    	
	        builder.setMessage("Are you sure you want to leave the game?")
	               .setPositiveButton("YES", new DialogInterface.OnClickListener() {
	                   public void onClick(DialogInterface dialog, int id) {
	                	   finish();
	                   }
	               })
	               .setNegativeButton("NO", new DialogInterface.OnClickListener() {
	                   public void onClick(DialogInterface dialog, int id) {
	                       
	                   }
	               }).create().show();
	        
	        return false;
	    }

	    return super.onKeyDown(keyCode, event);
	}
}
