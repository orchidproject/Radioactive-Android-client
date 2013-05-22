package com.geoloqi.services;

import java.io.IOException;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class GPSTracker implements LocationListener {
	public static final String TAG = "GPSTrackingService";
	public static final String GPS_INTENT = "GPS";

	public static final String PARAM_LONGITUDE = "longitude";
	public static final String PARAM_LATITUDE = "latitude";
	public static final String PARAM_ACCURACY = "accuracy";
	
	private LocationManager mlocationManager;
	
	private Context appContext;
	
	public GPSTracker(Context c){
		appContext = c;
		mlocationManager = (LocationManager) c
				.getSystemService(Context.LOCATION_SERVICE);
		
	}
	public void start(){
		// Register the listener with the Location Manager to receive location
		// updates
		mlocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				2000, 2f, this);
		
		
	}
	public void stop(){
		mlocationManager.removeUpdates(this);
		
	}
	@Override
	public void onLocationChanged(Location location) {
		Intent intent = new Intent(GPS_INTENT);
		intent.putExtra(PARAM_LONGITUDE, location.getLongitude());
		intent.putExtra(PARAM_LATITUDE, location.getLatitude());
		intent.putExtra(PARAM_ACCURACY, location.getAccuracy());
		//intent.putExtra("skill", myRole);
		Log.d(TAG, "BROADCAST lat: " +location.getLatitude() +", long: "+location.getLongitude() );
		appContext.sendBroadcast(intent);
		/*code reseved for logging
		if (fileOut != null) {
			Date now = new Date();
			String logString = String.format("%d,%f,%f,%f\n", now.getTime(),
			location.getLatitude(), location.getLongitude(),
			location.getAccuracy());
			try {
				fileOut.write(logString);
				fileOut.flush();
			} catch (IOException e) {
				Log.e("SEB", "Could not write to file: " + e);
				e.printStackTrace();
			}
		}*/
		
	}
	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub
		
	}

}
