package com.geoloqi.ui;

import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.geoloqi.interfaces.OrchidConstants;
import com.geoloqi.interfaces.RPCException;
import com.geoloqi.interfaces.RoleMapping;
import com.geoloqi.rpc.OrchidClient;

public class SignInActivity extends Activity implements OnClickListener {
	public static final String TAG = "SignInActivity";

	/** Validates an email address. */
	public static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w\\.-]+@([\\w\\-]+\\.)+[a-z]{2,4}$",
			Pattern.CASE_INSENSITIVE);

	/** The id of the game to launch when finished. */
	private String mRoleString="unset";
	private boolean isRoleSet=false;
	private int mRoleId;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sign_in_activity);
		
		// Listen for form submission
		setTextIndicaters();
		findViewById(R.id.submit_button).setOnClickListener(this);
	}
	private void setTextIndicaters(){
		final TextView testView1 = (TextView) findViewById(R.id.textView1);
		final TextView testView2 = (TextView) findViewById(R.id.textView2);
		final TextView testView3 = (TextView) findViewById(R.id.textView3);
		
		
		testView2.setText("extra sensors enabled: "+GameActivity.sensorEnabled);
		testView1.setText("test mode enabled: "+GameActivity.testMode);
		testView3.setText("role:"+mRoleString);
		
		
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
			
				//test the secret code
				if (name.equals("testmode")){
					if(GameActivity.testMode==false){
						Toast.makeText(this, "tese mode enabled",
								Toast.LENGTH_SHORT).show();
						GameActivity.testMode=true;
					}else{
						Toast.makeText(this, "tese mode disbled",
								Toast.LENGTH_SHORT).show();
						GameActivity.testMode=false;
					}
				
					setTextIndicaters();
					return;
				}
				else if (name.equals("sensors")){
					if(GameActivity.sensorEnabled==false){
						Toast.makeText(this, "extra sensor log enabled",
							Toast.LENGTH_SHORT).show();
						GameActivity.sensorEnabled=true;
					}else{
						Toast.makeText(this, "extra sensor log disabled",
							Toast.LENGTH_SHORT).show();
						GameActivity.sensorEnabled=false;
					}
				
					setTextIndicaters();
					return;
				}
			
				if(!isRoleSet){
					Toast.makeText(getApplicationContext(), "role not set", Toast.LENGTH_LONG).show();
					return;
				}else if(initials.length()!=2){
					Toast.makeText(getApplicationContext(), "need two letters as initials", Toast.LENGTH_LONG).show();
					return;
				}
					
				Intent intent = new Intent(this, GameListActivity.class);
				intent.putExtra("initials", initials);
				intent.putExtra("name", name);
				intent.putExtra("roleId", mRoleId+"");
				intent.putExtra("used", false);
				startActivity(intent);	
		}
	}
	
	public void setRole(View v){
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
		
		alertDialog.setTitle("Chose a role");
		final String[] items = {"medic","firefighter","soldier","transporter"};
		
	    alertDialog.setSingleChoiceItems(items , -1, new DialogInterface.OnClickListener() {
	    	

			public void onClick(DialogInterface dialog, int item){
	    		mRoleString=RoleMapping.roleMap.get(item);
	    		//consider delete this line
	    		//MapAttackClient.getApplicationClient(SignInActivity.this).setRole(item);
	    		mRoleId = item;
	    		setTextIndicaters();
	    		isRoleSet=true;
	    		dialog.dismiss();
	        }
	    });
	    
	    alertDialog.create().show();
		
	}

	

	
}