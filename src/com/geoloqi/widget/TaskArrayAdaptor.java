package com.geoloqi.widget;

import models.InstructionV1;
import models.Player;

import org.json.JSONObject;

import com.geoloqi.mapattack.R;
import com.geoloqi.rpc.OrchidClient;
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
	private InstructionV1 peer_instruction=null;
	private int freshCount = 0;
	
	public TaskArrayAdaptor(Context context, SocketIOManager sm) {
		super(context, 0);
		mInflater =  LayoutInflater.from(context);
		sManager = sm;
		
	}
	
	@Override
	public void add(InstructionV1 v){
		super.add(v);
		freshCount++;
	}
	public void ackInstructionV1(int ins_id, Player player,int status){
		//validation 
		InstructionV1 i= getItem(0);
		int current_player_id = OrchidClient.getApplicationClient(getContext()).getPlayerId();
		if(i.getId() == ins_id && player.getId() == current_player_id){
			i.setStatus(status);
		}
		else if(peer_instruction.getId() == ins_id){
			peer_instruction.setStatus(status);
		}
		
		notifyDataSetChanged();
	}
	
	public void setPeerInstruction(InstructionV1 i){
		peer_instruction = i;
		notifyDataSetChanged();
	}
	
	private String caculateTime(int time_second){
		long timeStamp = System.currentTimeMillis();
		long sentTime =  time_second*1000;
		int difference = (int) (timeStamp-sentTime);
		int mins = difference/(60*1000);
		int sec = (difference-(mins*60*1000))/1000;
		return "sent "+ mins + " mins " +sec+ " secs"+ " ago";
		
	}
	
	private void updateTime(final TextView v, final int sent_time){
		
		 new Thread(new Runnable() {
			    public void run() {
			      int count = freshCount;
			       while(count==freshCount){
			    	  try {
						   Thread.sleep(5000);
					   } catch (InterruptedException e) {
						   e.printStackTrace();
					   }
			    	  v.post(new Runnable() {
			    	  
			    		  public void run() {
			    			  v.setText(caculateTime(sent_time));
			    		  }
			    	  });
			       }
			    }
			  }).start();
	}
	
	
	
	public View getView(final int position,View contentView,ViewGroup parent){
		final InstructionV1 in = getItem(position);
		if(in.getTask() == null){
			//modifiy the view
			contentView = mInflater.inflate(R.layout.activity_game_task_waiting_cell, parent, false);
			return contentView;
		}
		
        contentView = mInflater.inflate(R.layout.activity_game_task_list_cell_v3, parent, false);
        
        //populate the content
       // final TextView instruction_time = (TextView)contentView.findViewById(R.id.instruction_time);
        //updateTime(instruction_time,in.getTime());
        final TextView v = (TextView)contentView.findViewById(R.id.teammate_response);
        
        TextView instruction_task = (TextView)contentView.findViewById(R.id.instruction_task);
        instruction_task.setText("Evacuate task:        " + in.getTask());
            
        TextView instruction_direction = (TextView)contentView.findViewById(R.id.instruction_direction);
        instruction_direction.setText("Direction:        " + in.getDirection());
        
        
        final TextView status = (TextView)contentView.findViewById(R.id.instruction_status);  
        final ImageView img = (ImageView)contentView.findViewById(R.id.instruction_playericon);
        TextView instruction_teammate = (TextView)contentView.findViewById(R.id.instruction_teammate);
        
        Player p = in.getTeammate();
        if(p!=null){
        		instruction_teammate.setText("Team up with: " + in.getTeammate().getInitials());
            	ImageLoader.getImageLoader().loadPlayerImage(p.getId(), p.getInitials(), p.getSkill(),
            		
            		new ImageLoader.Callback() {
						@Override
						public void callback(Bitmap bm) {
							img.setImageBitmap(bm);						
						}
					});
        }
            
        Button accept = (Button)contentView.findViewById(R.id.instruction_accept);
        Button reject = (Button)contentView.findViewById(R.id.instruction_reject);
        switch(in.getStatus()){
        case 1:
            	status.setText("waiting for your response");
            	break;
        case 2:
            	status.setText("you have accepted the plan");
            	accept.setEnabled(false);
            	reject.setEnabled(true);
            	break;
        case 3:
            	status.setText("you have rejected the plan");
            	accept.setEnabled(true);
            	reject.setEnabled(false);
            	break;
        case 4: status.setText("discarded");
        		break;
        }
        
        if(peer_instruction!= null){
        	
        	String t_initial = "";
        	if(p!=null){
        		t_initial = p.getInitials();
        	}
        	switch(peer_instruction.getStatus()){
            case 1:
                	v.setText("response from " + t_initial +": " + "none");
                	break;
            case 2:
                	v.setText("response from " + t_initial +": "+ "accepted");
                	break;
            case 3:
                	v.setText("response from " + t_initial  +": "+ "rejected");
                	break;
            }
        }
            
        accept.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					sManager.sendInstructionAck(
							in.getId(), 
							2,
							OrchidClient.getApplicationClient(getContext()).getPlayerId()
					);
		}});
        
        reject.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				sManager.sendInstructionAck(
						in.getId(), 
						3,
						OrchidClient.getApplicationClient(getContext()).getPlayerId()
				);
		}});
        		
		//modify peer instruction here.
		return contentView;
	}

}
