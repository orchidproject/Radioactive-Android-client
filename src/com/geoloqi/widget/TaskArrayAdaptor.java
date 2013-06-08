package com.geoloqi.widget;

import models.InstructionV1;
import models.Player;

import org.json.JSONObject;

import com.geoloqi.mapattack.R;
import com.geoloqi.services.SocketIOManager;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class TaskArrayAdaptor extends ArrayAdapter<InstructionV1> {

	private LayoutInflater mInflater;
	private SocketIOManager sManager;
	
	public TaskArrayAdaptor(Context context, SocketIOManager sm) {
		super(context, 0);
		mInflater =  LayoutInflater.from(context);
		sManager = sm;
		
	}
	
	public View getView(final int position,View contentView,ViewGroup parent){
		final InstructionV1 in = getItem(position);
		if(contentView == null){
            contentView = mInflater.inflate(R.layout.activity_game_task_list_cell, parent, false);
            //populate the content
            TextView d = (TextView)contentView.findViewById(R.id.instruction_task);
            d.setText("Evacuate task:        " + in.getTask());
            //d = (TextView)contentView.findViewById(R.id.instruction_teammate);
            //d.setText("Team up with:" + in.getTeammate());
            d = (TextView)contentView.findViewById(R.id.instruction_direction);
            d.setText("Direction:        " + in.getDirection());
            d = (TextView)contentView.findViewById(R.id.instruction_time);
            d.setText("Sent:     " + in.getTime() + " ago");
            final TextView status = (TextView)contentView.findViewById(R.id.instruction_status);
            
            final ImageView img = (ImageView)contentView.findViewById(R.id.instruction_playericon);
            Player p = in.getPlayer();
            if(p!=null){
            	ImageLoader.getImageLoader().loadPlayerImage(p.getId(), p.getInitials(), p.getSkill(),
            		
            		new ImageLoader.Callback() {
						@Override
						public void callback(Bitmap bm) {
							img.setImageBitmap(bm);						
						}
					});
            }
            
            
            switch(in.getStatus()){
            case 1:
            	status.setText("waiting for your response");
            	break;
            case 2:
            	status.setText("accepted");
            	break;
            case 3:
            	status.setText("discarded");
            	break;
            }
            
            Button button = (Button)contentView.findViewById(R.id.instruction_accept);
            button.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					sManager.sendInstructionAck(in.getId(), in.getStatus());
				}});
        }
		return contentView;
	}

}
