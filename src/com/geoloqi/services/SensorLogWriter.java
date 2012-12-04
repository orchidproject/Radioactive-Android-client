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
    private final AccListener mAccListener= new AccListener(this);
    private final MegnaticListener mMegnaticListener= new MegnaticListener(this);
    private float[] latest_m_reading = new float[3];
    private float[] latest_a_reading = new float[3];

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
    
    public void setMagnetic(float[] value){
    	latest_m_reading=value;
    }
    
    public void setAccelerUpdate(float[] value){
   	  latest_a_reading=value;
   }
    
    public float[] getMagnetic(){
    	return latest_m_reading;
    }
    
    public float[] getAccelerUpdate(){
    	 return latest_a_reading;
    }
    
   
}


class MegnaticListener implements SensorEventListener{
	SensorLogWriter mSensor;
	public MegnaticListener(SensorLogWriter sensor) {
		mSensor=sensor;
	}
	
	
	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent arg0) {
		// TODO Auto-generated method stub
		Log.i("Role", "magnetic " + arg0.values[0]);
		mSensor.setMagnetic(arg0.values);
	}
	
}


class AccListener implements SensorEventListener{
	
	SensorLogWriter mSensor;
	public AccListener(SensorLogWriter sensor) {
		mSensor=sensor;
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent arg0) {
		// TODO Auto-generated method stub
		Log.i("Role", "acc_data " + arg0.values[0]);
		mSensor.setAccelerUpdate(arg0.values);
	}
	
}

