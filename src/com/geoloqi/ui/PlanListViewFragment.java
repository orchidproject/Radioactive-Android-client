package com.geoloqi.ui;

import org.json.JSONException;
import org.json.JSONObject;

import models.GameState;
import models.InstructionV1;

import com.geoloqi.mapattack.R;
import com.geoloqi.rpc.OrchidClient;
import com.geoloqi.services.SocketIOManager;
import com.geoloqi.widget.MsgArrayAdaptor;
import com.geoloqi.widget.TaskArrayAdaptor;
import com.geoloqi.widget.TaskMsgArrayAdaptor;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class PlanListViewFragment extends ListFragment {
	

	 // This is the Adapter being used to display the list's data
	 private TaskMsgArrayAdaptor mAdapter;
	 private SocketIOManager socket;
	
	 
	 @Override	
	 public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {

		View contentView=inflater.inflate(R.layout.activity_game_fragment_task, container,false);
		return contentView;
	 }


	 @Override
	 public void onViewCreated(View view,Bundle savedInstanceState){
			// Create a progress bar to display while the list loads
	        ProgressBar progressBar = new ProgressBar(getActivity());
	        progressBar.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, Gravity.CENTER));
	        progressBar.setIndeterminate(true);
	        getListView().setEmptyView(progressBar);

	        // Must add the progress bar to the root of the layout
	        ViewGroup root = (ViewGroup) getActivity().findViewById(android.R.id.content);
	        root.addView(progressBar);
	        
	        //test
	        ListView taskMsgView = (ListView) getActivity().findViewById(R.id.tmsg_content);
	        MsgArrayAdaptor adapter = new MsgArrayAdaptor(getActivity());
	        JSONObject j = new JSONObject();
	        try {
	        	j.put("player_initials", "aa");
	 	        j.put("player_id", "1");
	 	        j.put("player_skill", "medic");
	 	        j.put("content", "aaa");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	       
	        /*
	         * adapter.add(j);
	        adapter.add(j);
	        adapter.add(j);
	        adapter.add(j);
	        adapter.add(j);
	        adapter.add(j);
	        adapter.add(j);*/
	        
	        //set up message interface
			Button send_button = (Button) getActivity().findViewById(R.id.tmsg_send);
			final EditText msgView = (EditText) getActivity().findViewById(R.id.tmsg_edit);
			
			final int player_id =  OrchidClient.getApplicationClient(getActivity()).getPlayerId();
			send_button.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					String text =  msgView.getText().toString();
					if((!text.equals(""))&&socket!=null){
						msgView.setText("");
						InputMethodManager imm = (InputMethodManager)PlanListViewFragment.this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(msgView.getWindowToken(), 0);
						socket.sendMsg(text,player_id,getTeammate());
					}
					
			 }});
			
			 //same send action as send button
			 msgView.setOnEditorActionListener(new OnEditorActionListener() {
		        
				@Override
				public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
					String text =  msgView.getText().toString();
					msgView.setText("");
					InputMethodManager imm = (InputMethodManager)PlanListViewFragment.this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(msgView.getWindowToken(), 0);
					socket.sendMsg(text,player_id,getTeammate());
					return true;
				}
		    });
			
	        taskMsgView.setAdapter(mAdapter);
	 }
	 

    
	public void setMsgAdapter(TaskMsgArrayAdaptor a) {
		mAdapter = a;
	}
	public void setSocket(SocketIOManager so){
		socket = so;
	}
	
	private int getTeammate(){
		ListAdapter a = this.getListAdapter();
		InstructionV1 v = (InstructionV1)a.getItem(0);
		if(v!=null && v.getTask()!= null){
			return v.getTeammate().getId();
		}
		else{
			return 0;
		}
	}
}