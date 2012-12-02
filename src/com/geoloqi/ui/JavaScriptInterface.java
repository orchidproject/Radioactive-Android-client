package com.geoloqi.ui;
import android.content.Context;
import android.widget.Toast;
import com.geoloqi.services.IOSocketService;

public class JavaScriptInterface {
	Context mContext;
	TabbedMapActivity mTab;
    /** Instantiate the interface and set the context */
    JavaScriptInterface(Context c, TabbedMapActivity tab) {
        mContext = c;
        mTab=tab;
    }

    /** Show a toast from the web page */
    public void showToast(String toast) {
        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
    }
    
    
    public void sendMessage(String msg){
    	IOSocketService service = IOSocketService.getInstance();
    	service.sendMsg(msg);
    }
    
    
    public void newMessage(){
    	mTab.newMessage();
    }
}
