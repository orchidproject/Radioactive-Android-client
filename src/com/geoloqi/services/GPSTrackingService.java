// the whole class shall be removed, there is no point to implement an extra service
 



package com.geoloqi.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.geoloqi.interfaces.OrchidConstants;
import com.geoloqi.interfaces.LoggingConstants;

public class GPSTrackingService extends Service implements LocationListener,
		OrchidConstants {

	public static final String TAG = "GPSTrackingService";
	public static final String GPS_INTENT = "GPS";

	public static final String PARAM_LONGITUDE = "longitude";
	public static final String PARAM_LATITUDE = "latitude";
	public static final String PARAM_ACCURACY = "accuracy";

	private OutputStreamWriter fileOut;

	private LocationManager locationManager;

	private String myRole;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);


		createLogFile();

		// Acquire a reference to the system Location Manager
		locationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);

		// Register the listener with the Location Manager to receive location
		// updates
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				2000, 2f, this);

	}

	private void createLogFile() {
		String file = String
				.format("gps_%1$tY_%1$tm_%1$td_%1$tH:%1$tM:%1$tS.%1$tL.log",
						new Date());

		try {
			fileOut = new OutputStreamWriter(openFileOutput(file,
					Context.MODE_WORLD_READABLE));
			Log.i("SEB", "Created file " + file);
		} catch (FileNotFoundException e) {
			Log.e(LoggingConstants.RECORDING_TAG, "Could not create file "
					+ file + ": " + e);
			e.printStackTrace();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		locationManager.removeUpdates(this);
		if (fileOut != null) {
			try {
				fileOut.close();
			} catch (IOException e) {
				Log.e("SEB", "Could not close file: " + e);
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		// if (location.getA)
		Intent intent = new Intent(GPS_INTENT);
		intent.putExtra(PARAM_LONGITUDE, location.getLongitude());
		intent.putExtra(PARAM_LATITUDE, location.getLatitude());
		intent.putExtra(PARAM_ACCURACY, location.getAccuracy());
		//intent.putExtra("skill", myRole);
		Log.d(TAG, "BROADCAST lat: " +location.getLatitude() +", long: "+location.getLongitude() );
		sendBroadcast(intent);
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
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

}
