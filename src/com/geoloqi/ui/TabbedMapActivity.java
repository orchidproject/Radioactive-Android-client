package com.geoloqi.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TabActivity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabWidget;
import android.widget.Toast;

import com.geoloqi.interfaces.OrchidConstants;
import com.geoloqi.interfaces.LoggingConstants;
import com.geoloqi.interfaces.RPCException;
import com.geoloqi.mapattack.R;
import com.geoloqi.rpc.AccountMonitor;
import com.geoloqi.rpc.MapAttackClient;
import com.geoloqi.services.GPSTrackingService;
import com.geoloqi.services.IOSocketInterface;
import com.geoloqi.services.IOSocketService;
import com.geoloqi.ui.JavaScriptInterface;
import java.lang.Float;
import android.widget.TextView;

public class TabbedMapActivity extends TabActivity implements OrchidConstants {
	
	public static boolean testMode=false;

	public static boolean sensorEnabled=false;
	
	private TabHost mTabHost;
	private TabContentFactory tf;
	private EditText msgEditor;
	
	private EditText testLatEditor;
	private EditText testLngEditor;
	private EditText testInterval;
	
	public static final String TAG = "TabbedMapActivity";
	public static final String PARAM_GAME_ID = "game_id";
	public static final int SCAN_QR_CODE = 0;
	private static final int DIALOG_QRCODE_SUCCESS = 0;
	private static final int DIALOG_QRCODE_CANCEL = 1;
	private static final int DIALOG_QRCODE_MISSING = 2;
	private static final String QRTAG = "QR_CODE_TAG";
	public static final String PARAM_USER_ID = "user_id";
	public static final String PARAM_INITIALS = "initials";

	public static final String PARAM_ROLEID = "role_id";
	public static final String PARAM_SENSOR_ENABLED = "sensor_enabled";

	private String mGameId;
	private String msgViewUrl;
	private String mGameUrl;
	private WebView mWebView;
	private WebView msgsWebView;
	private Intent mSocketIoIntent;
	private String mQrCodeReturn = "";
	private Intent mGPSIntent;
	private boolean servicesRunning = false;
	private MapAttackClient client;
	private InputMethodManager inputManager;
	
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//clean cache
		/*getApplicationContext().deleteDatabase("webview.db");
		getApplicationContext().deleteDatabase("webviewCache.db");
		*/
		inputManager = (InputMethodManager) TabbedMapActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE); 
		
		setContentView(R.layout.tabbed_main);
		mTabHost = getTabHost();
		
		final Bundle extras = getIntent().getExtras();
		if (extras != null) {
			mGameId = getIntent().getExtras().getString(PARAM_GAME_ID);
			Log.i(TAG, "extras!=null and mGameId is " + mGameId);
		}

		// Keep the screen lit while this Activity is visible
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		// Show the loading indicator
		setLoading(true);
		
		// Build game
		mGameUrl = String.format(GAME_URL_BASE + mGameId);
		msgViewUrl = String.format(GAME_URL_BASE + mGameId +"/messages");
		
		Log.i(TAG, "Game id is: " + mGameId);
		
		mSocketIoIntent = new Intent(this, IOSocketService.class);
		mSocketIoIntent.putExtra(GameListActivity.PARAM_GAME_ID,
				mGameId);
		mSocketIoIntent.putExtra(TabbedMapActivity.PARAM_SENSOR_ENABLED,
				TabbedMapActivity.sensorEnabled);

		mGPSIntent = new Intent(this, GPSTrackingService.class);
		
		tf = new TabHost.TabContentFactory() {
			
			@Override
			public View createTabContent(String tag) {
				
				//webview
				mWebView = (WebView) findViewById(R.id.webView);
				
				//TODO add load webview
				// Prepare the web view
				mWebView.clearCache(true);
				mWebView.setVerticalScrollBarEnabled(false);
				mWebView.setHorizontalScrollBarEnabled(false);

				mWebView.getSettings().setJavaScriptEnabled(true);
				mWebView.setWebViewClient(mWebViewClient);
				mWebView.setWebChromeClient(new WebChromeClient());
				
				mWebView.addJavascriptInterface(new JavaScriptInterface(getApplicationContext(),TabbedMapActivity.this), "Android");
				
				return mWebView;
			}
		};
		

		
		msgsWebView  = (WebView) findViewById(R.id.msgWebView);
		
		msgsWebView.clearCache(false);
		msgsWebView.setVerticalScrollBarEnabled(false);
		msgsWebView.setHorizontalScrollBarEnabled(false);

		msgsWebView.getSettings().setJavaScriptEnabled(true);
		msgsWebView.setWebViewClient(mWebViewClient);
		msgsWebView.setWebChromeClient(new WebChromeClient());
		
		mTabHost.addTab(mTabHost.newTabSpec("webtab").setIndicator("Map").setContent(tf));
		mTabHost.addTab(mTabHost.newTabSpec("msgtab").setIndicator("Messages").setContent(R.id.msgView));
		setupMessage();
		mTabHost.addTab(mTabHost.newTabSpec("testab").setIndicator("Test").setContent(R.id.testView));
		setupTest();
		
		
		if(testMode==false){
			getTabWidget().getChildAt(2).setVisibility(View.GONE);
		}
		
		mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {

		       @Override
		       public void onTabChanged(String arg0) {
		    	   if(arg0=="msgtab" || arg0=="webtab"){
		    		   String toSend = "javascript:clearNewMessage();";
		   				mWebView.loadUrl(toSend);
		    	   }
		    	   
		       }
		});
		
		
		mTabHost.setCurrentTab(0);
		

	}
	
	public void newMessage(){
		// a hooker used to hook msg event from webview
		//change tab content
		//TabWidget vTabs = getTabWidget();
		//RelativeLayout rLayout = (RelativeLayout) vTabs.getChildAt(1);
		//((TextView) rLayout.findViewById(android.R.id.title)).setText("NewTabText");
	}
	
	private void setupMessage(){
		msgEditor = (EditText) findViewById(R.id.msgEditor);
		Button sendBtn = (Button) findViewById(R.id.send_btn);
		sendBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//get the text
				String msg = msgEditor.getText().toString();
				//TODO: post to server
				if (client != null) {
					if (!msg.equals("")) { 
						client.sendMessage(mGameId, msg);
						msgEditor.setText("");
						msgEditor.clearFocus();
						inputManager.hideSoftInputFromWindow(TabbedMapActivity.this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
					}
				}
			}
		});
		
	}
	
	private void setupTest(){
		testLatEditor = (EditText) findViewById(R.id.testLng);
		testLngEditor = (EditText) findViewById(R.id.testLat);
		testInterval = (EditText) findViewById(R.id.testInter);
		//deflault values
		testLatEditor.setText("52.9550");
		testLngEditor.setText("-1.18840");
		testInterval.setText("3000");
		
		Button startBtn = (Button) findViewById(R.id.test_btn);
		startBtn.setOnClickListener(new View.OnClickListener() {
			boolean started=false;
			@Override
			public void onClick(View v) {
				//get the text
				String lat = testLatEditor.getText().toString();
				String lng = testLngEditor.getText().toString();
				String time = testInterval.getText().toString();
				
				//TODO: post to server
				if (!started) {
					//validate
					float flat;
					float flng;
					int   itime;
					try{
						flat=new Float(lat);
						flng=new Float(lng);
						itime=new Integer(time);
						
						iIOSocket.startTest(flat, flng, itime);
					}catch(NumberFormatException e){
						Toast.makeText(getApplicationContext(), "wrong lat/lng/time format", Toast.LENGTH_SHORT).show();
						
						return;
					}catch(RemoteException e){
						Toast.makeText(getApplicationContext(), "remote exception", Toast.LENGTH_SHORT).show();
						return;
					}
					
					((Button)v).setText("stop");
					testLatEditor.setEnabled(false);
					testLngEditor.setEnabled(false);
					testInterval.setEnabled(false);
					started=true;
				}
				else{
					try {
						iIOSocket.stopTest();
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					((Button)v).setText("start");
					testLatEditor.setEnabled(true);
					testLngEditor.setEnabled(true);
					testInterval.setEnabled(true);
					started=false;
				}
			}
		});
	}
	
	
	

	@Override
	public void onStart() {
		super.onStart();

		client = MapAttackClient
				.getApplicationClient(this);
		
		
		try {
				// Stop any previously started services and broadcast receivers
				stopServicesIfRunning();

		} catch (IllegalArgumentException e) {
				Log.w(TAG, "Trying to unregister an inactive push receiver.");
		}

		try {
				// Join the game
				client.joinGame(mGameId);
				
				Log.d(LoggingConstants.RECORDING_TAG, "Joined game " + mGameId);
				//note game id in prefs
				Editor prefs = (Editor) this.getSharedPreferences(
						OrchidConstants.PREFERENCES_FILE, Context.MODE_PRIVATE).edit();
				prefs.putString("gameId", mGameId);
				prefs.commit();
				
				String initials = this.getSharedPreferences(OrchidConstants.PREFERENCES_FILE, Context.MODE_PRIVATE).getString("initials", "");
				
				//BEGIN FROM HERE
				String roleString = this.getSharedPreferences(OrchidConstants.PREFERENCES_FILE, Context.MODE_PRIVATE).getString("role_string", "medic");
				
				String userID = AccountMonitor.getUserID(this);
				mSocketIoIntent.putExtra(PARAM_USER_ID, userID);
				mSocketIoIntent.putExtra(PARAM_INITIALS, initials);
				mSocketIoIntent.putExtra(MapAttackClient.PARAM_USER_ROLE, roleString);
				
			
				
				Log.i(TAG, "joined the game");

				// Start our services
				startServicesIfNotRunning();

				// Load the game into the WebView
				String webUrl = String.format("%s?id=%s", mGameUrl, userID);

				Log.i(TAG, webUrl);

				mWebView.loadUrl(webUrl);
				String msgUrl = String.format("%s?id=%s", msgViewUrl, userID);
				msgsWebView.loadUrl(msgUrl);
				Log.i(TAG, "web view loaded");

			} catch (RPCException e) {
				Log.e(TAG, "Got an RPCException when trying to join the game!",
						e);
				Toast.makeText(this, R.string.error_join_game,
						Toast.LENGTH_LONG).show();
				finish();
			}
	}
	

	private synchronized void stopServicesIfRunning() {
		if (servicesRunning) {
			unregisterReceiver(mPushReceiver);
			stopService(mSocketIoIntent);
			stopService(mGPSIntent);
			servicesRunning = false;
		}
	}

	private synchronized void startServicesIfNotRunning() {
		if (!servicesRunning) {
			registerReceiver(mPushReceiver, new IntentFilter("PUSH"));
			Log.d(TAG, "STARTING GPS TRACKING SERVICE");
			startService(mSocketIoIntent);
			bindService(mSocketIoIntent,mConnection,BIND_AUTO_CREATE);
			startService(mGPSIntent);
			servicesRunning = true;
		}
	}
	
	IOSocketInterface iIOSocket;
	private ServiceConnection mConnection = new ServiceConnection() {
	   // Called when the connection with the service disconnects unexpectedly
	    public void onServiceDisconnected(ComponentName className) {
	        Log.e(TAG, "Service has unexpectedly disconnected");
	        iIOSocket = null;
	    }

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			iIOSocket = IOSocketInterface.Stub.asInterface(service);
		}
	};

	@Override
	protected void onPause() {
		super.onPause();
		stopServicesIfRunning();
		this.finish();
	}

	@Override
	protected void onResume() {
		super.onResume();
		startServicesIfNotRunning();
	}

	@Override
	public void onStop() {
		super.onStop();
		try {
			stopServicesIfRunning();
		} catch (IllegalArgumentException e) {
			Log.w(TAG, "Trying to unregister an inactive push receiver.");
		}
		this.finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.game_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	



	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		switch (id) {
		case DIALOG_QRCODE_SUCCESS:
			builder.setMessage("The QR Code says: " + mQrCodeReturn)
					.setPositiveButton("OK", null);

			dialog = builder.create();
			break;
		case DIALOG_QRCODE_CANCEL:
			builder.setMessage(
					"You cancelled the QR Code scan. Your results "
							+ "cannot be saved. Please try again.")
					.setPositiveButton("OK", null);
			dialog = builder.create();
			break;
		case DIALOG_QRCODE_MISSING:
			builder.setMessage(
					"There is no QR reader application in your system."
							+ "\nPlease install one.").setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							;
						}
					});
			dialog = builder.create();
			break;
		}
		return dialog;
	}
	
	//confirmation to quit game
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK)
	    {
	        new AlertDialog.Builder(this).setMessage("Do you want to exit the game?")
	        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int which) {
	                // TODO Auto-generated method stub
	                finish();
	            }
	        }).setNegativeButton("NO", null).show();

	    }
	    return super.onKeyDown(keyCode, event);

	}

	/** Show or hide the loading indicator. */
	private void setLoading(boolean loading) {
		ProgressBar spinner = (ProgressBar) findViewById(R.id.loading);

		if (loading) {
			spinner.setVisibility(View.VISIBLE);
//			mTabHost.addTab(mTabHost.newTabSpec("webtab").setIndicator("Map").setContent(R.id.loading));
//			mWebView.setVisibility(View.GONE);
		} else {
			spinner.setVisibility(View.GONE);
//			mTabHost.addTab(mTabHost.newTabSpec("webtab").setIndicator("Map").setContent(tf));
//			mWebView.setVisibility(View.VISIBLE);
		}
	}

	/** A reference to the WebViewClient that hosts the MapAttack game. */
	private WebViewClient mWebViewClient = new WebViewClient() {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);

			// Make WebView visible and hide loading indicator
			setLoading(false);
		}
	};

	/** The broadcast receiver used to push game data to the server. */
	private BroadcastReceiver mPushReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context ctxt, Intent intent) {
			Log.i("Testing IO", String.format("Received JSON: %s", intent
					.getExtras().getString("json")));

			String toSend = String.format(
					"javascript:handleSocketData('%s');", intent.getExtras()
							.getString("json"));
							
		    Log.i("Testing IO", "URL to open: " + toSend);

			mWebView.loadUrl(toSend);
			msgsWebView.loadUrl(toSend);
		}
	};
}
