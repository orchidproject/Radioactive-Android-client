package com.geoloqi;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public abstract class ADB {

	public static boolean DEBUG = false;

	public static void log(String message) {
		Log.d("bpl", message);
	}

	public static void logo() {
		log("AtomicOrchid!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!weeeeeeeeeeeeeeeeeeeeeee");
		
	}

	public static Runnable makeToast(final Context context, final String text, final int duration) {
		return new Runnable() {
			@Override
			public void run() {
				Toast.makeText(context, text, duration).show();
			}
		};
	}

	//	public static doSynchronously(final Context context, Runnable runnable) {
	//		AsyncTask task = new AsyncTask<Void,Void,Void>() {
	//			
	//		}
	//	}
}
