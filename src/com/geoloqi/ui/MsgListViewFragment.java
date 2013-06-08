package com.geoloqi.ui;

import org.json.JSONException;
import org.json.JSONObject;

import models.GameState;
import com.geoloqi.mapattack.R;
import com.geoloqi.widget.MsgArrayAdaptor;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ProgressBar;

public class MsgListViewFragment extends ListFragment {
	

	 // This is the Adapter being used to display the list's data
	 private MsgArrayAdaptor mAdapter;
	 
	 
	 @Override	
	 public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {

		View contentView=inflater.inflate(R.layout.activity_game_fragment_msg, container,false);
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

	        mAdapter = new MsgArrayAdaptor(getActivity());
	        setListAdapter(mAdapter);

	        // Prepare the loader.  Either re-connect with an existing one,
	        // or start a new one.
	        //getLoaderManager().initLoader(0, null, this);

	        //no need to use loader as this is just an example and data is small
	        //sample test
	        JSONObject test1=null;
			try {
				test1 = new JSONObject("{\"time_stamp\":0, \"message\":{\"content\": \"hello\"}}");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        mAdapter.add(test1);
	        mAdapter.add(test1);
	        mAdapter.add(test1);
	        mAdapter.add(test1);
	        mAdapter.add(test1);
	        

	 }
}
