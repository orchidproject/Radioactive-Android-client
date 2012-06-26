package com.geoloqi.ui;
import android.content.Context;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.geoloqi.services.IOSocketInterface;
import com.geoloqi.services.IOSocketService;

public class JavaScriptInterface {
	Context mContext;
	IOSocketInterface service;

    /** Instantiate the interface and set the context */
    JavaScriptInterface(Context c, IOSocketInterface iIOSocket) {
        mContext = c;
        service= iIOSocket;
    }

    /** Show a toast from the web page */
    public void showToast(String toast) {
        //Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
    }
    
    public String disconnect(){
    	try{
    		if (service != null){
    			
    		}else{
    			Log.d("socket.io", "try to talk to socket.io when it is not initialized");
    		}
    	}
    	catch(Exception e){
    		e.printStackTrace();
    		
    	}
    	return "true";
    }
    
    public String connect(String msg){
    	try{
    		
    		if (service != null){
    			Toast.makeText(mContext, "lala", Toast.LENGTH_SHORT).show();
    			service.connect();
    			
    			
    		}else{
    			
    			Toast.makeText(mContext, "lalala", Toast.LENGTH_SHORT).show();
    		}
    	}
    	catch(Exception e){
    		e.printStackTrace();
    		
    	}
    	return "true";
    }
    
    public String request_info(){
    	try {
			return service.get_info();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
    }
    
    
    public String request_skill(){
    	try {
			return service.get_skill();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
    }
    
    public String request_initials(){
    	try {
			return service.get_initials();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
    }
    
    public String request_userID(){
    	try {
			return service.get_userID();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
    }
    
    public void sendMessage(String msg){
    	try{
    		if (service != null){
    			Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    			service.sendMsg(msg);
    		}else{
    			Log.d("socket.io", "try to talk to socket.io when it is not initialized");
    		}
    	}
    	catch(Exception e){
    		e.printStackTrace();
    		
    	}
    }
}
