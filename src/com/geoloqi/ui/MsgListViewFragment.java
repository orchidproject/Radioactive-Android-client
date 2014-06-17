package com.geoloqi.ui;

import org.json.JSONException;
import org.json.JSONObject;

import models.GameState;
import com.geoloqi.mapattack.R;
import com.geoloqi.services.SocketIOManager;
import com.geoloqi.widget.MsgArrayAdaptor;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class MsgListViewFragment extends ListFragment {
	

	 // This is the Adapter being used to display the list's data
	 private MsgArrayAdaptor mAdapter;
	 SocketIOManager socket =null;
	 private Thread timer = null;
	 private boolean updateTime = false;
	 
		
	public void setAdaptor(MsgArrayAdaptor mMsgViewAdaptor) {
			
			mAdapter = mMsgViewAdaptor;
	}
	public void setSocket(SocketIOManager so){
			socket = so;
	}
	 
	 @Override	
	 public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {

		View contentView=inflater.inflate(R.layout.activity_game_fragment_msg, container,false);
		Button send_button = (Button) contentView.findViewById(R.id.msg_send);
		final EditText msgView = (EditText) contentView.findViewById(R.id.msg_content);
		
		send_button.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				String text =  msgView.getText().toString();
				if((!text.equals(""))&&socket!=null){
					msgView.setText("");
					InputMethodManager imm = (InputMethodManager)MsgListViewFragment.this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(msgView.getWindowToken(), 0);
					socket.sendMsg(text);
				}
				
			}});
		 //same send action as send button
		 msgView.setOnEditorActionListener(new OnEditorActionListener() {
	        
			@Override
			public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
				String text =  msgView.getText().toString();
				msgView.setText("");
				InputMethodManager imm = (InputMethodManager)MsgListViewFragment.this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(msgView.getWindowToken(), 0);
				socket.sendMsg(text);
				return true;
			}
	    });
		
		return contentView;
	 }
	 
	

	 @Override
	 public void onViewCreated(View view,Bundle savedInstanceState){
		    
		 	
		 	//View footer = getLayoutInflater(savedInstanceState).inflate(R.layout.activity_game_fragment_msg_footer, null);
		 	//getListView().addFooterView(footer);
		    //initialize list view??
		    getListView();
		 	if(mAdapter == null){
	        	mAdapter = new MsgArrayAdaptor(getActivity());
	        }
	        setListAdapter(mAdapter);
	        startUpdateTimeIfNot();
	       
	 }
	 
	 @Override
	 public void onDestroyView(){
		 updateTime=false;
		 timer = null;
		 super.onDestroyView();
	 }
	 
	
		
	 private void startUpdateTimeIfNot(){
			if(timer==null){
				updateTime=true;
			   timer= new Thread(new Runnable() {
				    public void run() {
				    	while (updateTime){
				    		try {
			    				Thread.sleep(5000);//update rate
			    			} catch (InterruptedException e) {
			    				e.printStackTrace();
			    			}
				    		if (updateTime){
				    		//for every element, concurrency problem might happen, when added to adapter but not update view yet.
				    			for(int i=0;i<getListView().getChildCount();i++){
				    			
				    				final int count = getListView().getFirstVisiblePosition()+i;
				    				View child = (View)getListView().getChildAt(i);
				    				final TextView timerView = (TextView)child.findViewById(R.id.msg_timer);
				    				if(timerView==null) continue;
				    				timerView.post(new Runnable() {
				    	  
				    					public void run() {
				    						timerView.setText(mAdapter.caculateTime(mAdapter.getItem(count).optInt("timeStamp")));
				    					}
				    				});
				    			}
				    		}
				    	 }
				       }
				    
				  });
			   timer.start();
			}
		}

	
}
