package com.geoloqi.ui;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import com.geoloqi.mapattack.R;
import com.geoloqi.services.SocketIOManager;

import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class TestFragment extends Fragment{
	private EditText testLatEditor;
	private EditText testLngEditor;
	private EditText testInterval;
	
	private boolean loaded = false;
	private SocketIOManager sm;
	private Context context;
	
	public void setSocketIO(SocketIOManager sm){
		this.sm = sm;
	}
	public void setContext(Context context){
		this.context = context;
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View contentView=inflater.inflate(R.layout.activity_game_fragment_test, container,false);
		return contentView;
	}
	
	@Override
	public void onResume(){
		super.onResume();
		if(!loaded){
			setupTest();
		}
		
	}
	

	private void setupTest(){
		testLatEditor = (EditText) getView().findViewById(R.id.testLng);
		testLngEditor = (EditText) getView().findViewById(R.id.testLat);
		testInterval = (EditText) getView().findViewById(R.id.testInter);
		//deflault values
		testLatEditor.setText("52.9550");
		testLngEditor.setText("-1.18840");
		testInterval.setText("3000");
		
		Button startBtn = (Button) getView().findViewById(R.id.test_btn);
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
						
						sm.startTest(flat, flng, itime);
					}catch(NumberFormatException e){
						Toast.makeText(context, "wrong lat/lng/time format", Toast.LENGTH_SHORT).show();
						
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
						sm.stopTest();
					} catch (RemoteException e) {
						
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
		loaded = true;
	}
	
}
