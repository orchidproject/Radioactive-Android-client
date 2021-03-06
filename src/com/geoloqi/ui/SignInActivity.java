package com.geoloqi.ui;

import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.geoloqi.mapattack.R;
import com.geoloqi.interfaces.GeoloqiConstants;
import com.geoloqi.interfaces.RPCException;
import com.geoloqi.interfaces.RoleMapping;
import com.geoloqi.rpc.MapAttackClient;

public class SignInActivity extends Activity implements OnClickListener {
	public static final String TAG = "SignInActivity";

	/** Validates an email address. */
	public static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w\\.-]+@([\\w\\-]+\\.)+[a-z]{2,4}$",
			Pattern.CASE_INSENSITIVE);

	/** The id of the game to launch when finished. */
	private String mGameId;
	private String mRoleSting="medic";
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sign_in_activity);
		
		final Bundle extras = getIntent().getExtras();
		if (extras != null) {
			mGameId = extras.getString(TabbedMapActivity.PARAM_GAME_ID);
		}

		// Load saved user information
		final SharedPreferences sharedPreferences = this.getSharedPreferences(
				GeoloqiConstants.PREFERENCES_FILE, Context.MODE_PRIVATE);
		if (sharedPreferences != null) {
			final TextView initialsView = (TextView) findViewById(R.id.initials);
			final TextView nameView = (TextView) findViewById(R.id.name);
			
			initialsView.setText(sharedPreferences.getString("initials", ""));
			nameView.setText(sharedPreferences.getString("name", ""));
			mRoleSting = sharedPreferences.getString("role_string", null);
			
		}
		
		// Listen for form submission
		findViewById(R.id.submit_button).setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		Log.i("model", "model "+ Build.PRODUCT);
		switch (view.getId()) {
		case R.id.submit_button:
			final EditText initialsField = (EditText) findViewById(R.id.initials);
			final EditText nameField = (EditText) findViewById(R.id.name);

			final String initials = initialsField.getText().toString();
			final String name = nameField.getText().toString();

			Editor prefs = (Editor) this.getSharedPreferences(
					GeoloqiConstants.PREFERENCES_FILE, Context.MODE_PRIVATE).edit();
			prefs.putString("initials", initials);
			prefs.putString("name", name);
			
			//test the secret code
			if (name.equals("changerole")) {
				AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
				
				alertDialog.setTitle("Chose a role");
				final String[] items = {"medic","firefighter","soldier","transporter"};
				
			    alertDialog.setSingleChoiceItems(items , -1, new DialogInterface.OnClickListener() {
			    	public void onClick(DialogInterface dialog, int item){
			    		mRoleSting=RoleMapping.roleMap.get(item+1);
			    		MapAttackClient.getApplicationClient(SignInActivity.this).setRole(item+1);
			    		dialog.dismiss();
			        }
			    });
			    
			    alertDialog.create().show();
				return;
			}else if (name.equals("testmode")){
				if(TabbedMapActivity.testMode==false){
					Toast.makeText(this, "tese mode enabled",
							Toast.LENGTH_SHORT).show();
					TabbedMapActivity.testMode=true;
				}else{
					Toast.makeText(this, "tese mode disbled",
							Toast.LENGTH_SHORT).show();
					TabbedMapActivity.testMode=false;
				}
				
				
				return;
			}
			
			prefs.putString("role_string", mRoleSting);
			Log.i("role", "save role string"+ mRoleSting);
			prefs.commit();
			
			Intent intent = new Intent(this, GameListActivity.class);
			startActivity(intent);
			

		}
	}
	

	/** Stub */
	private void finishLogin(boolean result) {
		if (result) {
			if (!TextUtils.isEmpty(mGameId)) {
				// Launch the map attack activity

				//Log.i("AAA", "finishing login");
				Intent intent = new Intent(this, TabbedMapActivity.class);
				intent.putExtra(TabbedMapActivity.PARAM_GAME_ID, mGameId);
				startActivity(intent);
			} else {
				Log.e(TAG, "Got an empty game ID when trying to finish login!");
				Toast.makeText(this, R.string.error_invalid_game_id, Toast.LENGTH_LONG).show();
			}
		} else {
			Toast.makeText(this, R.string.error_join_game, Toast.LENGTH_LONG).show();
		}

		// Finish the login activity
		finish();
	}

	
}