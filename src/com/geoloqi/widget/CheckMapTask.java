package com.geoloqi.widget;

import com.geoloqi.ui.GameActivity;

import android.os.AsyncTask;

public class CheckMapTask extends AsyncTask<Void,Void,Integer> {

	GameActivity ga = null;
	public CheckMapTask(GameActivity ga){
		this.ga = ga;
	}
	@Override
	protected Integer doInBackground(Void... params) {
		return 0;
	}
	
	
	

}
