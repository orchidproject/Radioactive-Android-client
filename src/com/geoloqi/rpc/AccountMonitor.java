package com.geoloqi.rpc;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import android.content.Context;

import com.geoloqi.interfaces.OrchidConstants;
import com.geoloqi.interfaces.RPCException;

public class AccountMonitor {

	private static ReentrantLock lock = new ReentrantLock();
	private static Condition userIDReceived = lock.newCondition();

	

	public static String getUserID(Context context) {
		lock.lock();
		try {
			if (!context.getSharedPreferences(OrchidConstants.PREFERENCES_FILE, Context.MODE_PRIVATE).contains("userID")) {
				userIDReceived.awaitUninterruptibly();
			}
			return context.getSharedPreferences(OrchidConstants.PREFERENCES_FILE, Context.MODE_PRIVATE).getString("userID", null);
		} finally {
			lock.unlock();
		}
	}

}
