package com.geoloqi.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.geoloqi.data.Game;
import com.geoloqi.interfaces.GeoloqiConstants;
import com.geoloqi.interfaces.LoggingConstants;
import com.geoloqi.interfaces.RPCException;
import com.geoloqi.interfaces.RoleMapping;
import com.geoloqi.mapattack.R;
import com.geoloqi.rpc.MapAttackClient;
import com.geoloqi.services.GeoloqiPositioning;
import com.geoloqi.widget.GameListArrayAdapter;

public class GameListActivity extends ListActivity implements OnClickListener,
		GeoloqiConstants {
	public static final String TAG = "GameListActivity";

	public static final String ORCHID_TAG = LoggingConstants.RECORDING_TAG;

	public static final String PARAM_GAME_LIST = "game_list";
	public static final String PARAM_NEAREST_INTERSECTION = "nearest_intersection";
	public static final String PARAM_SYNC_ON_START = "sync_on_start";

	public static final String PARAM_GAME_ID = "game_id";

	private static final int HELP_DIALOG = 0;
	private static final int CLEAR_HISTORY_DIALOG = 1;

	private boolean mSyncOnStart = true;
	private Intent mPositioningIntent;
	private ArrayList<Game> mGameList = null;
	private String mNearestIntersection = null;

	private Context context;
	private SharedPreferences sharedPreferences;

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.context = getApplicationContext();
		
		sharedPreferences  = getSharedPreferences(
				GeoloqiConstants.PREFERENCES_FILE, Context.MODE_PRIVATE);

		setContentView(R.layout.game_list_activity);

		// Find our views
		final Button refreshButton = (Button) findViewById(R.id.refresh_button);
		final Button clearHistoryButton = (Button) findViewById(R.id.clear_button);
		final Button helpButton = (Button) findViewById(R.id.help_button);
		final Button logoutButton = (Button) findViewById(R.id.logout_button);
		final TextView loginView = (TextView) findViewById(R.id.logged_in_label);
		loginView.setText(getLoggedInText());

		// Set our on click listeners
		refreshButton.setOnClickListener(this);
		clearHistoryButton.setOnClickListener(this);
		helpButton.setOnClickListener(this);
		logoutButton.setOnClickListener(this);

		// Reference our positioning service Intent
//		mPositioningIntent = new Intent(this, GeoloqiPositioning.class);

		if (savedInstanceState != null) {
			// Restore our saved instance state
			mSyncOnStart = savedInstanceState.getBoolean(PARAM_SYNC_ON_START,
					true);
			mNearestIntersection = savedInstanceState
					.getString(PARAM_NEAREST_INTERSECTION);
			mGameList = savedInstanceState
					.getParcelableArrayList(PARAM_GAME_LIST);

			setNearestIntersection(mNearestIntersection);
			populateGameList(mGameList);
		}

		if (mSyncOnStart || mGameList.isEmpty()) {
//			// Start our positioning service
//			stopService(mPositioningIntent);
//			startService(mPositioningIntent);

			// Search for nearby games
			setLoading(true);
			new RequestGamesListTask(this, getLastKnownLocation(), false)
					.execute();
		}
	}

	private String getLoggedInText() {
		String text = "";
		String initials = "";

		if (sharedPreferences != null) {
			initials = sharedPreferences.getString("initials", "");
			text = "Logged in as "+initials;
		}
		if (initials.equals("") || sharedPreferences == null) {   
			text = "Not logged in.";
			Intent logInActivity = new Intent(this, SignInActivity.class);
			startActivity(logInActivity);
		}
		return text;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mPositioningIntent != null)
			stopService(mPositioningIntent);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if (mGameList != null) {
			populateGameList(mGameList);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putBoolean(PARAM_SYNC_ON_START, false);
		outState.putString(PARAM_NEAREST_INTERSECTION, mNearestIntersection);
		outState.putParcelableArrayList(PARAM_GAME_LIST,
				(ArrayList<? extends Parcelable>) mGameList);
	}

	/**
	 * Populate the ListView with a new GameListArrayAdapter from the provided
	 * List of Game objects.
	 * 
	 * @param games
	 */
	private void populateGameList(final ArrayList<Game> games) {
		Log.d(ORCHID_TAG, "Populated list of games: " + games);
		setLoading(false);
		if (games != null) {
			String activeGame = sharedPreferences.getString("gameId", "");
			Log.d(TAG, "activeGame: "+activeGame);
			mGameList = games;
			for (int i = 0; i<mGameList.size();  i++) {
				String id = mGameList.get(i).id;
				if (id.equals(activeGame))
					mGameList.get(i).setAsActive();
				else 
					mGameList.get(i).setAsInactive();
			}
			setListAdapter(new GameListArrayAdapter(this,
					R.layout.game_list_element,
					mGameList.toArray(new Game[mGameList.size()])));
		}
	}

	/**
	 * Set the game list label with the nearest intersection.
	 * 
	 * @param intersection
	 */
	private void setNearestIntersection(final String intersection) {
		if (!TextUtils.isEmpty(intersection)) {
			mNearestIntersection = intersection;
			TextView textView = (TextView) findViewById(R.id.game_list_label);
			if (textView != null) {
				textView.setText(String.format("Games near %s", intersection));
			}
		}
	}

	/** Get the last known location from the device. */
	private Location getLastKnownLocation() {
		LocationManager lm = ((LocationManager) getSystemService(LOCATION_SERVICE));
		List<String> providers = lm.getAllProviders();
		for (String provider : providers) {
			Location last = lm.getLastKnownLocation(provider);
			if (last != null) {
				return last;
			}
		}
		return null;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		final Game selection = (Game) l.getItemAtPosition(position);

		// Initialise the MapAttackActivity for the indicated game
		Intent intent = new Intent(this, MapAttackActivity.class);
		intent.putExtra(MapAttackActivity.PARAM_GAME_ID, selection.id);
		
		if (sharedPreferences != null) {
			//check whether we have to logout of an existing game first.
			String currentId = sharedPreferences.getString("gameId", "");
			if (!currentId.equals(selection.id)) {
				//logout first
				Thread initialiseLogout = new LogoutServer();
				initialiseLogout.run();
				startActivity(intent);
			}
			else 
				startActivity(intent);
		}
		
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.refresh_button:
			new RequestGamesListTask(this, getLastKnownLocation()).execute();
			break;
		/*
		 * case R.id.geoloqi: final Intent geoloqiIntent = new
		 * Intent(Intent.ACTION_VIEW, Uri.parse("http://orchid.ac.uk/"));
		 * geoloqiIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		 * geoloqiIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		 * startActivity(geoloqiIntent); break;
		 */
		case R.id.clear_button:
			context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
					.edit().remove("userID").commit();
			context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
					.edit().remove("gameID").commit();
			sharedPreferences.edit().remove("gameId").commit();
			showDialog(CLEAR_HISTORY_DIALOG);
			break;
		case R.id.help_button:
			showDialog(HELP_DIALOG);
			break;
		case R.id.logout_button:
			logout();
			break;
		}
	}
	
	private void logout() {
		Editor prefs = (Editor) this.getSharedPreferences(
				GeoloqiConstants.PREFERENCES_FILE, Context.MODE_PRIVATE).edit();
		prefs.putString("initials", "");
		prefs.putString("name", "");
		sharedPreferences.edit().remove("userID").commit();
		sharedPreferences.edit().remove("gameID").commit();
		sharedPreferences.edit().remove("gameId").commit();
		prefs.commit();
		Intent logInActivity = new Intent(this, SignInActivity.class);
		startActivity(logInActivity);
	}

	/** Show or hide the loading indicator. */
	private void setLoading(boolean loading) {
		ProgressBar spinner = (ProgressBar) findViewById(R.id.loading);
		ListView listView = getListView();
		View emptyView = listView.getEmptyView();

		if (loading) {
			spinner.setVisibility(View.VISIBLE);
			listView.setVisibility(View.GONE);
			emptyView.setVisibility(View.GONE);
		} else {
			spinner.setVisibility(View.GONE);
			listView.setVisibility(View.VISIBLE);
			emptyView.setVisibility(View.GONE);
		}
	}

	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		switch (id) {
		case HELP_DIALOG:
			builder.setMessage(R.string.help_page).setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							;
						}
					});

			dialog = builder.create();
			break;
		case CLEAR_HISTORY_DIALOG:
			builder.setMessage(R.string.clear_history_dialog)
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									;
								}
							});

			dialog = builder.create();
			break;
		}
		return dialog;
	}
	
	private class LogoutServer extends Thread {
		
		@Override
		public void run() {
			final MapAttackClient client = MapAttackClient
					.getApplicationClient(GameListActivity.this);
			try {
				String currentId = sharedPreferences.getString("gameId", "");
				client.logoutGame(currentId);
			} catch (RPCException e) {
				Log.e(TAG, "Got an RPCException when trying to join the game!",
						e);
				Toast.makeText(GameListActivity.this, R.string.error_join_game,
						Toast.LENGTH_LONG).show();
				finish();
			}
			sharedPreferences.edit().remove("userID").commit();
			sharedPreferences.edit().remove("gameID").commit();
		}
		
	}

	/**
	 * A simple AsyncTask to request the game list from the server.
	 * 
	 * @TODO: Move this to an external class file.
	 * */
	private static class RequestGamesListTask extends
			AsyncTask<Void, Void, ArrayList<Game>> {
		private final Context mContext;
		// private final Location mLocation;

		private String mIntersection = null;
		private ProgressDialog mProgressDialog = null;

		public RequestGamesListTask(final Context context,
				final Location location) {
			this(context, location, true);
		}

		public RequestGamesListTask(final Context context,
				final Location location, final boolean displayDialog) {
			mContext = context;
			// mLocation = location;

			// Build a progress dialog
			if (displayDialog) {
				mProgressDialog = new ProgressDialog(context);
				mProgressDialog.setTitle(null);
				mProgressDialog.setMessage(context
						.getString(R.string.game_list_loading_text));
			}
		}

		@Override
		protected void onPreExecute() {
			// Show our progress dialog
			if (mProgressDialog != null) {
				mProgressDialog.show();
			}
		}
		
		

		@Override
		protected ArrayList<Game> doInBackground(Void... params) {

			// Get the MapAttackClient
			final MapAttackClient client = MapAttackClient
					.getApplicationClient(mContext);
			try {
				/*
				 * commented out from original because we don't need the
				 * location
				 * 
				 * if (mLocation != null) { // Get the nearest intersection
				 * mIntersection =
				 * client.getNearestIntersection(mLocation.getLatitude(),
				 * mLocation.getLongitude());
				 * 
				 * // Get the game list return
				 * client.getGames(mLocation.getLatitude(),
				 * mLocation.getLongitude()); } else {
				 */
				return client.getGames();
				// }
			} catch (RPCException e) {
				Log.e(TAG,
						"Got an RPCException when looking for nearby games.", e);
			}
			return new ArrayList<Game>();
		}

		@Override
		protected void onPostExecute(ArrayList<Game> games) {
			try {
				final GameListActivity activity = (GameListActivity) mContext;
				activity.setNearestIntersection(mIntersection);
				activity.populateGameList(games);
			} catch (ClassCastException e) {
				Log.w(TAG,
						"Got a ClassCastException when trying to update the game list!",
						e);
			}

			// Dismiss our progress dialog
			if (mProgressDialog != null) {
				mProgressDialog.dismiss();
			}
		}
	}
}
