package com.geoloqi.services;
import android.content.Context;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.util.Log;

public class SensorLogWriter{
	private final SensorManager mSensorManager;
    private final Sensor mAccelerometer;
    private final Sensor mMagnetic;
    private final AccListener mAccListener= new AccListener();
    private final MegnaticListener mMegnaticListener= new MegnaticListener();

    public SensorLogWriter(Context context) {
        mSensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }
    
    
    protected void start_update() {
        
        mSensorManager.registerListener(mAccListener, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(mMegnaticListener, mMagnetic, SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void stop_update() {
       
        mSensorManager.unregisterListener(mAccListener);
        mSensorManager.unregisterListener(mMegnaticListener);
    }
    
   
}


class MegnaticListener implements SensorEventListener{

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent arg0) {
		// TODO Auto-generated method stub
		Log.i("Role", "magnetic " + arg0.values[0]);
	}
	
}


class AccListener implements SensorEventListener{

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent arg0) {
		// TODO Auto-generated method stub
		Log.i("Role", "acc_data " + arg0.values[0]);

	}
	
}

