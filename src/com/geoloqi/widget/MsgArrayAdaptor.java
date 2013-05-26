package com.geoloqi.widget;

import org.json.JSONObject;

import com.geoloqi.mapattack.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class MsgArrayAdaptor extends ArrayAdapter<JSONObject> {

	private LayoutInflater mInflater;
	private Context mContext;
	
	public MsgArrayAdaptor(Context context) {
		super(context, 0);
		mInflater =  LayoutInflater.from(context);
		mContext=context;
		
	}
	
	public View getView(final int position,View contentView,ViewGroup parent){
		if(contentView == null){
            contentView = mInflater.inflate(R.layout.activity_game_list_cell, parent, false);
            //populate the content
        }
		return contentView;
	}

}
