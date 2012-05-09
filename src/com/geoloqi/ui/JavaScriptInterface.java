package com.geoloqi.ui;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import com.geoloqi.services.IOSocketService;

public class JavaScriptInterface {
	Context mContext;
	IOSocketService service;

    /** Instantiate the interface and set the context */
    JavaScriptInterface(Context c, IOSocketService s) {
        mContext = c;
        service= s;
    }

    /** Show a toast from the web page */
    public void showToast(String toast) {
        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
    }
    
    
    public void sendMessage(String msg){
    	try{
    		if (service != null){
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
