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
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;
import android.widget.Toast;

import com.geoloqi.interfaces.GeoloqiConstants;
import com.geoloqi.interfaces.LoggingConstants;
import com.geoloqi.interfaces.RPCException;
import com.geoloqi.mapattack.R;
import com.geoloqi.rpc.AccountMonitor;
import com.geoloqi.rpc.MapAttackClient;
import com.geoloqi.services.GPSTrackingService;
import com.geoloqi.services.IOSocketInterface;
import com.geoloqi.services.IOSocketService;
import com.geoloqi.services.IOSocketService.LocalBinder;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.geoloqi.ui.JavaScriptInterface;
import java.lang.Float;

public class TabbedMapActivity extends TabActivity implements GeoloqiConstants {
	
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

	private String mGameId;
	private String msgViewUrl;
	private String mGameUrl;
	private WebView mWebView;
	private WebView msgsWebView;
	private Intent mPushNotificationIntent;
	private String mQrCodeReturn = "";
	private Intent mGPSIntent;
	private boolean servicesRunning = false;
	private MapAttackClient client;
	private InputMethodManager inputManager;
	private IOSocketService mService;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
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
		
		mPushNotificationIntent = new Intent(this, IOSocketService.class);
		mPushNotificationIntent.putExtra(GameListActivity.PARAM_GAME_ID,
				mGameId);

		mGPSIntent = new Intent(this, GPSTrackingService.class);
		
		tf = new TabHost.TabContentFactory() {
			
			@Override
			public View createTabContent(String tag) {
				
				//webview
				mWebView = (WebView) findViewById(R.id.webView);
				
				//TODO add load webview
				// Prepare the web view
				mWebView.clearCache(false);
				mWebView.setVerticalScrollBarEnabled(false);
				mWebView.setHorizontalScrollBarEnabled(false);

				mWebView.getSettings().setJavaScriptEnabled(true);
				mWebView.setWebViewClient(mWebViewClient);
				mWebView.setWebChromeClient(new WebChromeClient());
				
				return mWebView;
			}
		};
		
//		TabContentFactory msgTab = new TabHost.TabContentFactory() {
//			
//			@Override
//			public View createTabContent(String tag) {
//				// TODO 
//				msgsWebView  = (WebView) findViewById(R.id.msgWebView);
//				
//				msgsWebView.clearCache(false);
//				msgsWebView.setVerticalScrollBarEnabled(false);
//				msgsWebView.setHorizontalScrollBarEnabled(false);
//
//				msgsWebView.getSettings().setJavaScriptEnabled(true);
//				msgsWebView.setWebViewClient(mWebViewClient);
//				msgsWebView.setWebChromeClient(new WebChromeClient());
//				
//				return msgsWebView;
//			}
//		};
		
		msgsWebView  = (WebView) findViewById(R.id.msgWebView);
		
		msgsWebView.clearCache(false);
		msgsWebView.setVerticalScrollBarEnabled(false);
		msgsWebView.setHorizontalScrollBarEnabled(false);

		msgsWebView.getSettings().setJavaScriptEnabled(true);
		msgsWebView.setWebViewClient(mWebViewClient);
		msgsWebView.setWebChromeClient(new WebChromeClient());
		
		mTabHost.addTab(mTabHost.newTabSpec("webtab").setIndicator("Map").setContent(tf));
		mTabHost.addTab(mTabHost.newTabSpec("msgtab").setIndicator("Messages").setContent(R.id.msgView));
		mTabHost.addTab(mTabHost.newTabSpec("msgtab").setIndicator("Test").setContent(R.id.testView));
		
		mTabHost.setCurrentTab(0);
		
		setupMessage();
		
		setupTest();

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
		// Check for a valid account token
		if (!client.hasToken()) {
			// Kick user out to the sign in activity
			//TODO might not make sense as SignInActivity changed... @jef
			Log.i(TAG, "client has no token!");
			Intent intent = new Intent(this, SignInActivity.class);
			intent.putExtra(MapAttackActivity.PARAM_GAME_ID, mGameId);
			startActivity(intent);
			finish();
		} else {
			try {
				// Stop any previously started services and broadcast receivers
				stopServicesIfRunning();

			} catch (IllegalArgumentException e) {
				Log.w(TAG, "Trying to unregister an inactive push receiver.");
			}

			try {
				Log.i(TAG, "starting the MapAttackActivity");
				// Join the game
				client.joinGame(mGameId);

				Log.d(LoggingConstants.RECORDING_TAG, "Joined game " + mGameId);
				//note game id in prefs
				Editor prefs = (Editor) this.getSharedPreferences(
						GeoloqiConstants.PREFERENCES_FILE, Context.MODE_PRIVATE).edit();
				prefs.putString("gameId", mGameId);
				prefs.commit();
				
				String initials = this.getSharedPreferences(GeoloqiConstants.PREFERENCES_FILE, Context.MODE_PRIVATE).getString("initials", "");
				String userID = AccountMonitor.getUserID(this);
				mPushNotificationIntent.putExtra(PARAM_USER_ID, userID);
				mPushNotificationIntent.putExtra(PARAM_INITIALS, initials);
				bindService(mPushNotificationIntent, mConnection, Context.BIND_AUTO_CREATE);
				Log.i(TAG, "joined the game");

				// Start our services
				startServicesIfNotRunning();

				// Load the game into the WebView
				String webUrl = String.format("%s?id=%s", mGameUrl, userID);
//				String webUrl = "http://holt.mrl.nott.ac.uk:49992/game/mobile/4";
				Log.i(TAG, webUrl);
//				if (mWebView == null)
//					mWebView = (WebView) findViewById(R.id.webView);
				mWebView.loadUrl(webUrl);
				String msgUrl = String.format("%s?id=%s", msgViewUrl, userID);
				msgsWebView.loadUrl(msgUrl);
				Log.i(TAG, "web view loaded");

			} catch (RPCException e) {
				Log.e(TAG, "Got an RPCException when trying to join the game!",e);
				Toast.makeText(this, R.string.error_join_game,
						Toast.LENGTH_LONG).show();
				finish();
			}
		}
	}
	
	
	

	private synchronized void stopServicesIfRunning() {
		if (servicesRunning) {
			unregisterReceiver(mPushReceiver);
			unregisterReceiver(mStateReceiver);
			stopService(mPushNotificationIntent);
			stopService(mGPSIntent);
			servicesRunning = false;
		}
	}

	private synchronized void startServicesIfNotRunning() {
		if (!servicesRunning) {
			registerReceiver(mPushReceiver, new IntentFilter("PUSH"));
			registerReceiver(mStateReceiver, new IntentFilter("STATE"));
			Log.d(TAG, "STARTING GPS TRACKING SERVICE");
			startService(mPushNotificationIntent);
			bindService(mPushNotificationIntent,mConnection,BIND_AUTO_CREATE);
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
			mWebView.addJavascriptInterface(new JavaScriptInterface(getApplicationContext(),iIOSocket), "Android");
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
			unbindService(mConnection);
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
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.share:
			Intent shareIntent = new Intent(Intent.ACTION_SEND);
			shareIntent.setType("text/plain");
			shareIntent.putExtra(Intent.EXTRA_TEXT,
					String.format("Map Attack! %s #mapattack", mGameUrl));
			startActivity(Intent.createChooser(shareIntent, "Share this map: "));
			return true;
		case R.id.quit:
			finish();
			return true;
		case R.id.qrscan:
			try {
				Intent intent = new Intent(
						"com.google.zxing.client.android.SCAN");
				intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
				intent.putExtra("SCAN_FORMATS", "QR_CODE");

				// intent.putExtra("SCAN_WIDTH", 800);
				// intent.putExtra("SCAN_HEIGHT", 200);
				intent.putExtra("RESULT_DISPLAY_DURATION_MS", 1000L);
				intent.putExtra("PROMPT_MESSAGE", "Scan the QR Code on the Box");

				startActivityForResult(intent, IntentIntegrator.REQUEST_CODE);
				Log.i(QRTAG, "QR app started successfully");
				return true;
			} catch (ActivityNotFoundException e) {
				Log.e(QRTAG, "QR code app missing");
				showDialog(DIALOG_QRCODE_MISSING);
				return true;
			}
		}
		return false;
	}

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (resultCode == Activity.RESULT_OK) {
			IntentResult scanResult = IntentIntegrator.parseActivityResult(
					requestCode, resultCode, intent);
			Log.i(QRTAG, "result code is: " + resultCode);
			if (scanResult != null) {
				if (requestCode == IntentIntegrator.REQUEST_CODE) {
					if (resultCode == RESULT_OK) {
						mQrCodeReturn = intent.getStringExtra("SCAN_RESULT");
						// showDialog(DIALOG_QRCODE_SUCCESS);
						Uri uri = Uri.parse(mQrCodeReturn);
						startActivity(new Intent(Intent.ACTION_VIEW, uri));
					} else if (resultCode == RESULT_CANCELED) {
						showDialog(DIALOG_QRCODE_CANCEL);
					}
				}
			} else {
				// else continue with any other code you need in the method
				Log.i(QRTAG, "scan result was null");
			}
		}
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
			Log.d("Testing IO", String.format("Received JSON: %s", intent
					.getExtras().getString("json")));
			Log.d("Testing IO", String.format("Received JSON: %s", intent
					.getExtras().getString("event")));

			String toSend = String.format(
					"javascript:handleSocketData('%s','%s');",intent
					.getExtras().getString("event"),
					intent.getExtras().getString("json"));
							

			Log.i("Testing IO", "URL to open: " + toSend);
			// String.format("javascript:handleSocketData(\"%s\")",
			// intent.getExtras().getString("json"))

			mWebView.loadUrl(toSend);
			//msgsWebView.loadUrl(toSend);
		}
	};
	
	private BroadcastReceiver mStateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context ctxt, Intent intent) {
			Log.d("Testing IO", String.format("Received JSON: %s", intent
					.getExtras().getString("keys")));
			Log.d("Testing IO", String.format("Received JSON: %s", intent
					.getExtras().getString("values")));

			String toSend = String.format(
					"javascript:setState('%s','%s');",intent
					.getExtras().getString("keys"),
					intent.getExtras().getString("values"));
							

			Log.i("Testing IO", "URL to open: " + toSend);
			// String.format("javascript:handleSocketData(\"%s\")",
			// intent.getExtras().getString("json"))

			mWebView.loadUrl(toSend);
			//msgsWebView.loadUrl(toSend);
		}
	};
	
	
}
